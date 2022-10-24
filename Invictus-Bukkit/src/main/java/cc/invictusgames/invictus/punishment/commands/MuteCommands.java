package cc.invictusgames.invictus.punishment.commands;

import cc.invictusgames.ilib.command.annotation.Command;
import cc.invictusgames.ilib.command.annotation.Flag;
import cc.invictusgames.ilib.command.annotation.Param;
import cc.invictusgames.ilib.command.parameter.defaults.Duration;
import cc.invictusgames.ilib.redis.packet.Packet;
import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.ilib.utils.TimeUtils;
import cc.invictusgames.invictus.InvictusBukkit;
import cc.invictusgames.invictus.profile.Profile;
import cc.invictusgames.invictus.punishment.Punishment;
import cc.invictusgames.invictus.punishment.packets.PunishmentCreatePacket;
import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.concurrent.TimeUnit;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 08.06.2020 / 20:29
 * Invictus / cc.invictusgames.invictus.spigot.punishment.commands
 */

@RequiredArgsConstructor
public class MuteCommands {

    private static final long MAX_TEMPMUTE_DURATION = TimeUnit.DAYS.toMillis(7);

    private final InvictusBukkit invictus;

    @Command(names = {"mute"},
             permission = "invictus.command.mute",
             description = "Mute a player",
             async = true)
    public boolean mute(
            CommandSender sender,
            @Param(name = "player") Profile target,
            @Param(name = "duration", defaultValue = "@perm") String durationString,
            @Param(name = "reason", wildcard = true) String reasonParam,
            @Flag(names = {"a", "-announce", "p", "-public"}, description = "Announce the mute", defaultValue = true)
            boolean silent) {

        String reason = reasonParam;
        long duration = -1;
        if (!durationString.equals("@perm")) {
            long parsed = TimeUtils.parseTime(durationString);
            if (parsed == -1)
                reason = durationString + " " + reason;
            else
                duration = parsed;
        }

        mute(sender, target, reason, duration, silent);

        return true;
    }

    @Command(names = {"tempmute", "tmute", "tm"},
             permission = "invictus.command.tempmute",
             description = "Temporarily mute a player",
             async = true)
    public boolean tempmute(
            CommandSender sender,
            @Param(name = "player") Profile target,
            @Param(name = "duration") Duration duration,
            @Param(name = "reason", wildcard = true) String reason,
            @Flag(names = {"a", "-announce", "p", "-public"}, description = "Announce the mute", defaultValue = true)
            boolean silent) {
        if (!sender.hasPermission("invictus.command.mute") && duration.isPermanent()) {
            sender.sendMessage(CC.format("&cYou cannot create a mute this long. Maximum time allowed: %s.",
                    TimeUtils.formatDetailed(MAX_TEMPMUTE_DURATION)));
            return false;
        }

        if (!sender.hasPermission("invictus.tempmute.bypass") && duration.getDuration() > MAX_TEMPMUTE_DURATION) {
            sender.sendMessage(CC.format("&cYou cannot create a mute this long. Maximum time allowed: %s.",
                    TimeUtils.formatDetailed(MAX_TEMPMUTE_DURATION)));
            return false;
        }

        mute(sender, target, reason, duration.getDuration(), silent);
        return true;
    }

    @Command(names = {"unmute"},
             permission = "invictus.command.unmute",
             description = "Remove an players active mute.",
             async = true)
    public boolean unmute(
            CommandSender sender,
            @Param(name = "player") Profile target,
            @Param(name = "reason", wildcard = true) String reason,
            @Flag(names = {"a", "-announce", "p", "-public"}, description = "Announce the unmute", defaultValue = true)
            boolean silent) {
        Punishment activePunishment = target.getActivePunishment(Punishment.PunishmentType.MUTE);
        if (activePunishment == null) {
            sender.sendMessage(CC.format("&e%s &cis not muted.", target.getName()));
            return false;
        }

        if (invictus.getBukkitPunishmentService().removePunishment(
                sender, target, activePunishment, reason, silent, true))
            sender.sendMessage(CC.GREEN + "Unmuted " + target.getRealDisplayName() + CC.GREEN + ".");
        return true;
    }

    private void mute(CommandSender sender, Profile target, String reason, long duration, boolean silent) {
        Punishment punishment = invictus.getBukkitPunishmentService().createPunishment(
                sender,
                target,
                Punishment.PunishmentType.MUTE,
                duration,
                reason,
                Sets.newHashSet(silent ? "silent" : "")
        );

        if (punishment != null)
            sender.sendMessage(CC.GREEN + "Muted " + target.getRealDisplayName() + CC.GREEN
                    + (punishment.getDuration() == -1
                    ? CC.GREEN + " permanently"
                    : " for " + CC.YELLOW + TimeUtils.formatDetailed(punishment.getDuration()))
                    + CC.GREEN + ".");

    }

}
