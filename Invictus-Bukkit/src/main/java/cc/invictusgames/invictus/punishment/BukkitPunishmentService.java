package cc.invictusgames.invictus.punishment;

import cc.invictusgames.ilib.ILib;
import cc.invictusgames.ilib.redis.packet.Packet;
import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.ilib.utils.json.JsonBuilder;
import cc.invictusgames.invictus.InvictusBukkit;
import cc.invictusgames.invictus.InvictusBukkitPlugin;
import cc.invictusgames.invictus.connection.RequestHandler;
import cc.invictusgames.invictus.connection.RequestResponse;
import cc.invictusgames.invictus.profile.Profile;
import cc.invictusgames.invictus.profile.packets.ProfileUpdatePacket;
import cc.invictusgames.invictus.punishment.packets.PunishmentRemovePacket;
import cc.invictusgames.invictus.punishment.template.PunishmentTemplate;
import cc.invictusgames.invictus.punishment.template.PunishmentTemplateConfig;
import com.google.gson.JsonArray;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


/**
 * @author langgezockt (langgezockt@gmail.com)
 * 02.03.2021 / 18:17
 * Invictus / cc.invictusgames.invictus.spigot.punishment
 */

@RequiredArgsConstructor
@Getter
public class BukkitPunishmentService {

    private final InvictusBukkit invictus;

    private final List<PunishmentTemplate> banTemplates = new ArrayList<>();

    private final List<PunishmentTemplate> muteTemplates = new ArrayList<>();

    public Punishment createPunishment(CommandSender sender,
                                       Profile target,
                                       Punishment.PunishmentType type,
                                       long duration,
                                       String reason,
                                       Set<String> flags) {
        if (sender instanceof Player && !invictus.getProfileService().getProfile((Player) sender).canInteract(target)) {
            sender.sendMessage(CC.format("&cYou cannot %s other staff members.", type.name().toLowerCase()));
            return null;
        }

        if (type == Punishment.PunishmentType.BAN
                && target.getActivePunishment(Punishment.PunishmentType.BLACKLIST) != null) {
            if (sender.hasPermission("invictus.punishments.viewblacklist")
                    || sender.hasPermission("invictus.punishments.viewblacklist.light"))
                sender.sendMessage(CC.format("&e%s &cis currently blacklisted and cannot be banned."));
            else sender.sendMessage(CC.format("&e%s &ccannot be banned at the moment."));
            return null;
        }

        Punishment activePunishment = target.getActivePunishment(type);
        if (activePunishment != null && type.isOverwritable()) {
            if (sender.hasPermission(String.format("invictus.punishment.%s.overwrite", type.name()))) {
                if (!removePunishment(sender, target, activePunishment, "Overwritten", true, false))
                    return null;
            } else {
                sender.sendMessage(CC.format("&e%s &cis already %s.", target.getName(), type.getContext()));
                return null;
            }
        }

        Punishment punishment = new Punishment(
                invictus,
                target.getUuid(),
                type,
                (sender instanceof Player ? ((Player) sender).getUniqueId().toString() : "Console"),
                reason,
                duration
        );

        JsonBuilder builder = new JsonBuilder();
        builder.add("id", punishment.getId());
        builder.add("punishmentType", punishment.getPunishmentType().name());
        builder.add("punishedBy", punishment.getPunishedBy());
        builder.add("punishedAt", punishment.getPunishedAt());
        builder.add("punishedReason", punishment.getPunishedReason());
        builder.add("punishedServerType", punishment.getPunishedServerType());
        builder.add("punishedServer", punishment.getPunishedServer());
        builder.add("duration", punishment.getDuration());
        builder.add("end", punishment.getEnd());

        JsonArray array = new JsonArray();
        flags.forEach(array::add);
        builder.add("flags", array);

        RequestResponse response = RequestHandler.post("punishment/%s", builder.build(),
                punishment.getUuid().toString());
        if (response.couldNotConnect()) {
            RequestHandler.addToBackLog(new PunishmentBackLogEntry(punishment, response.getRequestBuilder()));
            sender.sendMessage(CC.format("&cCould not connect to API to create punishment. " +
                            "Adding punishment to the queue. Error: %s (%d)",
                    response.getErrorMessage(), response.getCode()));
        } else if (!response.wasSuccessful()) {
            sender.sendMessage(CC.format("&cCould not create punishment: %s (%d)",
                    response.getErrorMessage(), response.getCode()));
            return null;
        }

        target.getPunishments().add(punishment);
        return punishment;
    }

    public boolean removePunishment(CommandSender sender,
                                    Profile target,
                                    Punishment punishment,
                                    String reason,
                                    boolean silent,
                                    boolean removeOnAlts) {
        JsonBuilder builder = new JsonBuilder();
        builder.add("removedBy", sender instanceof Player
                ? ((Player) sender).getUniqueId().toString() : "Console");
        builder.add("removedAt", System.currentTimeMillis());
        builder.add("removedReason", reason);
        builder.add("removed", true);
        builder.add("silent", silent);

        RequestResponse response = RequestHandler.put("punishment/%s", builder.build(), punishment.getId().toString());
        if (response.couldNotConnect())
            sender.sendMessage(CC.format("&cCould not connect to API to remove punishment. " +
                            "Adding request to the queue. Error: %s (%d)",
                    response.getErrorMessage(), response.getCode()));
        else if (!response.wasSuccessful()) {
            sender.sendMessage(CC.format("&cCould not remove punishment: %s (%d)",
                    response.getErrorMessage(), response.getCode()));
            return false;
        }

        punishment.setRemovedBy(sender instanceof Player ? ((Player) sender).getUniqueId().toString() : "Console");
        punishment.setRemovedAt(System.currentTimeMillis());
        punishment.setRemovedReason(reason);
        punishment.setRemoved(true);

        if (response.couldNotConnect())
            RequestHandler.addToBackLog(new PunishmentBackLogEntry(punishment, response.getRequestBuilder()));

        if (removeOnAlts && punishment.getPunishmentType().isOverwritable()) {
            if (target.getAlts() != null)
                checkAlts(sender, reason, punishment.getPunishmentType(), target.getAlts());
            else
                invictus.getProfileService().getAlts(target, alts ->
                        checkAlts(sender, reason, punishment.getPunishmentType(), alts), true);
        }

        return true;
    }

    private void checkAlts(CommandSender sender, String reason, Punishment.PunishmentType type, List<Profile> alts) {
        alts.forEach(alt -> {
            Punishment activePunishment = alt.getActivePunishment(type);
            if (activePunishment != null)
                removePunishment(sender, alt, activePunishment, reason, true, true);
        });
    }

    public void loadTemplates() {
        banTemplates.clear();
        muteTemplates.clear();

        PunishmentTemplateConfig config = invictus.getConfigurationService().loadConfiguration(
                PunishmentTemplateConfig.class,
                new File(ILib.getInstance().getMainConfig().getMessagesPath(), "punishment-templates.json")
        );

        banTemplates.addAll(config.getBans());
        muteTemplates.addAll(config.getMutes());

        config = invictus.getConfigurationService().loadConfiguration(
                PunishmentTemplateConfig.class,
                new File(InvictusBukkitPlugin.getInstance().getDataFolder(), "punishment-templates.json")
        );

        banTemplates.addAll(config.getBans());
        muteTemplates.addAll(config.getMutes());
    }

}
