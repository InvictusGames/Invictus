package cc.invictusgames.invictus.punishment.commands;

import cc.invictusgames.ilib.command.annotation.Command;
import cc.invictusgames.ilib.command.annotation.CommandCooldown;
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
public class BanCommands {

    private static final long MAX_TEMPBAN_DURATION = TimeUnit.DAYS.toMillis(7);

    private final InvictusBukkit invictus;

    @CommandCooldown(time = 3,
                     bypassPermission = "invictus.ban.cooldown.bypass")
    @Command(names = {"ban", "b"},
             permission = "invictus.command.ban",
             description = "Ban a player from the network",
             async = true)
    public boolean ban(
            CommandSender sender,
            @Param(name = "player") Profile target,
            @Param(name = "duration", defaultValue = "@perm") String durationString,
            @Param(name = "reason", wildcard = true) String reasonParam,
            @Flag(names = {"a", "-announce", "p", "-public"}, description = "Announce the ban", defaultValue = true)
                    boolean silent,
            @Flag(names = {"c", "-clear"}, description = "Clear the players inventory") boolean clear) {

        String reason = reasonParam;
        long duration = -1;
        if (!durationString.equals("@perm")) {
            long parsed = TimeUtils.parseTime(durationString);
            if (parsed == -1)
                reason = durationString + " " + reason;
            else
                duration = TimeUtils.parseTime(durationString);
        }

        ban(sender, target, reason, duration, silent, clear);
        return true;
    }

    @CommandCooldown(time = 3,
                     bypassPermission = "invictus.ban.cooldown.bypass")
    @Command(names = {"tempban", "tban", "tb"},
             permission = "invictus.command.tempban",
             description = "Temporarily ban a player from the network",
             async = true)
    public boolean tempban(
            CommandSender sender,
            @Param(name = "player") Profile target,
            @Param(name = "duration") Duration duration,
            @Param(name = "reason", wildcard = true) String reason,
            @Flag(names = {"a", "-announce", "p", "-public"}, description = "Announce the ban", defaultValue = true)
                    boolean silent,
            @Flag(names = {"c", "-clear"}, description = "Clear the players inventory") boolean clear) {
        if (!sender.hasPermission("invictus.command.ban") && duration.isPermanent()) {
            sender.sendMessage(CC.format("&cYou cannot create a ban this long. Maximum time allowed: %s.",
                    TimeUtils.formatDetailed(MAX_TEMPBAN_DURATION)));
            return false;
        }

        if (!sender.hasPermission("invictus.tempban.bypass") && duration.getDuration() > MAX_TEMPBAN_DURATION) {
            sender.sendMessage(CC.format("&cYou cannot create a ban this long. Maximum time allowed: %s.",
                    TimeUtils.formatDetailed(MAX_TEMPBAN_DURATION)));
            return false;
        }

        ban(sender, target, reason, duration.getDuration(), silent, clear);
        return true;
    }

    @Command(names = {"unban"},
             permission = "invictus.command.unban",
             description = "Remove an players active ban.",
             async = true)
    public boolean unban(
            CommandSender sender,
            @Param(name = "player") Profile target,
            @Param(name = "reason", wildcard = true) String reason,
            @Flag(names = {"a", "-announce", "p", "-public"}, description = "Announce the unban", defaultValue = true)
                    boolean silent) {

        Punishment activePunishment = target.getActivePunishment(Punishment.PunishmentType.BAN);
        if (activePunishment == null) {
            sender.sendMessage(CC.format("&e%s &cis not banned.", target.getName()));
            return false;
        }

        if (invictus.getBukkitPunishmentService().removePunishment(
                sender, target, activePunishment, reason, silent, true))
            sender.sendMessage(CC.GREEN + "Unbanned " + target.getRealDisplayName() + CC.GREEN + ".");
        return true;
    }

    private void ban(CommandSender sender,
                     Profile target,
                     String reason,
                     long duration,
                     boolean silent,
                     boolean clear) {
        Punishment punishment = invictus.getBukkitPunishmentService().createPunishment(
                sender,
                target,
                Punishment.PunishmentType.BAN,
                duration,
                reason,
                Sets.newHashSet(silent ? "silent" : "", clear ? "clear" : "")
        );

        if (punishment != null)
            sender.sendMessage(CC.GREEN + "Banned " + target.getRealDisplayName() + CC.GREEN
                    + (punishment.getDuration() == -1
                    ? CC.GREEN + " permanently"
                    : " for " + CC.YELLOW + TimeUtils.formatDetailed(punishment.getDuration()))
                    + CC.GREEN + ".");
    }

}
