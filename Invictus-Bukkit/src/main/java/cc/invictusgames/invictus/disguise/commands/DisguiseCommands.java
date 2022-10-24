package cc.invictusgames.invictus.disguise.commands;

import cc.invictusgames.ilib.command.annotation.Command;
import cc.invictusgames.ilib.command.annotation.Flag;
import cc.invictusgames.ilib.command.annotation.Param;
import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.ilib.utils.PasteUtils;
import cc.invictusgames.ilib.utils.TimeUtils;
import cc.invictusgames.ilib.utils.callback.TypeCallable;
import cc.invictusgames.ilib.uuid.UUIDCache;
import cc.invictusgames.invictus.InvictusBukkit;
import cc.invictusgames.invictus.connection.RequestHandler;
import cc.invictusgames.invictus.connection.RequestResponse;
import cc.invictusgames.invictus.disguise.BukkitDisguiseService;
import cc.invictusgames.invictus.disguise.DisguiseLogEntry;
import cc.invictusgames.invictus.disguise.menu.DisguiseLogsMenu;
import cc.invictusgames.invictus.disguise.menu.DisguiseNameLogsMenu;
import cc.invictusgames.invictus.disguise.menu.DisguiseNameMenu;
import cc.invictusgames.invictus.disguise.menu.DisguiseRankMenu;
import cc.invictusgames.invictus.disguise.procedure.DisguiseProcedure;
import cc.invictusgames.invictus.playersetting.InvictusSettings;
import cc.invictusgames.invictus.profile.Profile;
import cc.invictusgames.invictus.utils.PlayerMessagePacket;
import cc.invictusgames.invictus.utils.Tasks;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 18.06.2020 / 00:54
 * Invictus / cc.invictusgames.invictus.spigot.disguise.commands
 */

@RequiredArgsConstructor
public class DisguiseCommands {

    private static final int MAX_DISGUISES = 3;
    private static final long DISGUISE_COOLDOWN = TimeUnit.DAYS.toMillis(1);

    private final InvictusBukkit invictus;

    @Command(names = {"disguise", "d"},
             permission = "invictus.command.disguise",
             description = "Disguise yourself",
             playerOnly = true,
             async = true)
    public boolean disguise(Player sender, @Param(name = "name", defaultValue = "@random") String name) {
        Profile profile = invictus.getProfileService().getProfile(sender);

        if (profile.isDisguised()) {
            sender.sendMessage(CC.RED + "You are already disguised.");
            return false;
        }


        if (!sender.hasPermission("invictus.disguise.unlimited")) {

            int i = 0;
            DisguiseLogEntry entry = null;
            for (DisguiseLogEntry log : profile.getDisguiseData().getLogs()) {
                if (log.getTimeStamp() > System.currentTimeMillis() - DISGUISE_COOLDOWN) {
                    i++;

                    if (entry == null) {
                        entry = log;
                        continue;
                    }

                    if (log.getTimeStamp() < entry.getTimeStamp())
                        entry = log;
                }
            }

            if (i >= MAX_DISGUISES) {
                sender.sendMessage(CC.format(
                        "&cYou have already disguised &e%d&c/&e%d&c times a day. You can disguise again in &e%s&c.",
                        i,
                        MAX_DISGUISES,
                        TimeUtils.formatDetailed(entry.getTimeStamp() + DISGUISE_COOLDOWN
                                - System.currentTimeMillis())
                ));
                return false;
            }
        }

        if (name.equals("@random") || !sender.hasPermission("invictus.disguise.choosename")) {
            DisguiseProcedure procedure = new DisguiseProcedure(profile);
            getRandomName(randomName -> {
                if (randomName == null) {
                    sender.sendMessage(CC.RED + "Failed to find an available name after 10 attempts.");
                    return;
                }

                procedure.setName(randomName);
                new DisguiseRankMenu(invictus, procedure).openMenu(sender);
            }, true);
            return true;
        }

        if (name.equals("@admin") && sender.hasPermission("invictus.disguise.admin")) {
            new DisguiseNameMenu(invictus, new DisguiseProcedure(profile)).openMenu(sender);
            return true;
        }

        if (!BukkitDisguiseService.NAME_PATTERN.matcher(name).matches()) {
            sender.sendMessage(CC.format(
                    "&e%s &cdoes not follow the minecraft name format. (3-16 Alphanumeric characters)",
                    name
            ));
            return false;
        }

        if ((UUIDCache.getUuid(name) != null) && (!sender.hasPermission("invictus.disguise.admin"))) {
            sender.sendMessage(CC.RED + "You can only disguise as players that have never joined the server before.");
            return false;
        }

        if (Bukkit.getPlayerExact(name) != null) {
            sender.sendMessage(CC.RED + "You cannot disguise as an online player.");
            return false;
        }

        DisguiseProcedure procedure = new DisguiseProcedure(profile);
        procedure.setName(name);
        new DisguiseRankMenu(invictus, procedure).openMenu(sender);
        return true;
    }

    @Command(names = {"undisguise", "ud", "und"},
             permission = "invictus.command.undisguise",
             description = "Undisguise yourself",
             playerOnly = true)
    public boolean undisguise(Player sender) {
        Profile profile = invictus.getProfileService().getProfile(sender);

        if (!profile.isDisguised()) {
            sender.sendMessage(CC.RED + "You are not disguised.");
            return false;
        }

        invictus.getBukkitDisguiseService().undisguise(profile, true);
        sender.sendMessage(CC.GREEN + "You are no longer disguised.");
        profile.save(() -> {
        }, true);
        return true;
    }

    @Command(names = {"forceundisguise", "forceud"},
             permission = "invictus.command.forceundisguise",
             description = "Force undisguise a player",
             playerOnly = true,
             async = true)
    public boolean forceUndisguise(CommandSender sender, @Param(name = "target") Profile target) {
        if (!target.isDisguised()) {
            sender.sendMessage(CC.format("&e%s &cis not disguised.", target.getName()));
            return false;
        }

        invictus.getBukkitDisguiseService().undisguise(target, true);
        invictus.getRedisService().publish(new PlayerMessagePacket(target.getUuid(),
                CC.RED + "Your disguise has been forcefully removed by a staff member."));
        sender.sendMessage(CC.format("&aForcefully removed disguise of %s&a.", target.getRealDisplayName()));
        return true;
    }

    @Command(names = {"disguiselist", "dl"},
             permission = "invictus.command.disguiselist",
             description = "List all disguised players")
    public boolean disguiseList(CommandSender sender) {
        List<String> entries = new ArrayList<>();
        for (Profile profile : invictus.getProfileService().getOnlineProfilesSorted()) {
            if (profile.isDisguised())
                entries.add(CC.format(
                        " %s &eas %s",
                        profile.getRealCurrentGrant().getRank().getPrefix() + profile.getName(),
                        profile.getCurrentGrant().getRank().getPrefix() + profile.getDisguiseName()
                ));
        }

        if (entries.isEmpty()) {
            sender.sendMessage(CC.RED + "There are no disguised players on this server.");
            return true;
        }

        sender.sendMessage(CC.format("&eDisplaying &c%d &edisguised player%s.",
                entries.size(), entries.size() == 1 ? "" : "s"));
        entries.forEach(sender::sendMessage);
        entries.clear();
        return true;
    }

    @Command(names = {"disguiselogs", "dlogs"},
             permission = "invictus.command.disguiselogs",
             description = "Check the disguise logs of a player",
             playerOnly = true,
             async = true)
    public boolean disguiseLogs(
            Player sender,
            @Param(name = "player") Profile target,
            @Flag(names = {"p", "-paste"}, description = "Upload the logs to hastebin") boolean paste) {
        if (target == null) {
            return false;
        }

        if (paste) {
            sender.sendMessage(CC.YELLOW + "Uploading disguise logs of " + target.getName() + CC.YELLOW + "...");
            String url = pasteLogs(sender, target);
            if (url == null) {
                sender.sendMessage(CC.RED + "Failed to upload logs of " + CC.YELLOW + target.getName() + CC.RED + ".");
                return false;
            }

            sender.sendMessage(CC.GOLD + "Logs URL: " + CC.WHITE + url);
            return true;
        }

        new DisguiseLogsMenu(target).openMenu(sender);
        return true;
    }

    @Command(names = {"namelogs"},
             permission = "invictus.command.namelogs",
             description = "Check the logs of a disguise name",
             playerOnly = true,
             async = true)
    public void nameLogs(Player sender, @Param(name = "name") String name) {
        invictus.getBukkitDisguiseService().getDisguiseEntries(sender, name,
                logs -> new DisguiseNameLogsMenu(name, logs).openMenu(sender), true);
    }

    private String pasteLogs(CommandSender sender, Profile target) {
        StringBuilder builder = new StringBuilder();
        List<DisguiseLogEntry> logs = new ArrayList<>(target.getDisguiseData().getLogs());
        Collections.reverse(logs);
        builder.append("Disguise logs of ").append(target.getUuid().toString()).append(":\n");
        logs.forEach(log -> builder.append(log.formatPasteEntry(InvictusSettings.TIME_ZONE.get(sender), target))
                .append("\n"));
        return PasteUtils.paste(builder.toString(), false);
    }

    private int attempts = 0;

    public void getRandomName(TypeCallable<String> callable, boolean async) {
        if (async) {
            Tasks.runAsync(() -> getRandomName(callable, false));
            return;
        }

        attempts++;
        List<String> names = invictus.getBukkitDisguiseService().getNamePresets();

        if (names.isEmpty()) {
            callable.callback(null);
            attempts = 0;
            return;
        }

        String name = names.get(new Random().nextInt(names.size()));
        boolean available = false;
        RequestResponse response = RequestHandler.get("disguise/%s/available", name);
        if (response.wasSuccessful()
                && response.asObject().has("available")
                && response.asObject().get("available").getAsBoolean())
            available = true;

        if (attempts > 10) {
            callable.callback(null);
            return;
        }

        if (!available || UUIDCache.getUuid(name) != null) {
            getRandomName(callable, false);
            return;
        }

        callable.callback(name);
        attempts = 0;
    }

}
