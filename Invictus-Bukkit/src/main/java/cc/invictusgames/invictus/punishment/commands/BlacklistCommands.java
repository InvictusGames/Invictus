package cc.invictusgames.invictus.punishment.commands;

import cc.invictusgames.ilib.command.annotation.Command;
import cc.invictusgames.ilib.command.annotation.Flag;
import cc.invictusgames.ilib.command.annotation.Param;
import cc.invictusgames.ilib.redis.packet.Packet;
import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.invictus.InvictusBukkit;
import cc.invictusgames.invictus.profile.Profile;
import cc.invictusgames.invictus.punishment.Punishment;
import cc.invictusgames.invictus.punishment.packets.PunishmentCreatePacket;
import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 08.06.2020 / 20:55
 * Invictus / cc.invictusgames.invictus.spigot.punishment.commands
 */

@RequiredArgsConstructor
public class BlacklistCommands {

    private final InvictusBukkit invictus;

    @Command(names = {"blacklist"},
             permission = "invictus.command.blacklist",
             description = "Blacklist a player from the network",
             async = true)
    public boolean blacklist(
            CommandSender sender,
            @Param(name = "player") Profile target,
            @Param(name = "reason", wildcard = true) String reason,
            @Flag(names = {"a", "-announce", "p", "-public"}, description = "Announce the blacklist", defaultValue = true)
                    boolean silent) {
        Punishment punishment = invictus.getBukkitPunishmentService().createPunishment(
                sender,
                target,
                Punishment.PunishmentType.BLACKLIST,
                -1,
                reason,
                Sets.newHashSet(silent ? "silent" : "")
        );

        if (punishment != null)
            sender.sendMessage(CC.GREEN + "Blacklisted " + target.getRealDisplayName() + CC.GREEN + ".");
        return true;
    }

    @Command(names = {"unblacklist"},
             permission = "invictus.command.unblacklist",
             description = "Remove an players active blacklist",
             async = true)
    public boolean unblacklist(
            CommandSender sender,
            @Param(name = "player") Profile target,
            @Param(name = "reason", wildcard = true) String reason,
            @Flag(names = {"a", "-announce", "p", "-public"}, description = "Announce the unblacklist",
                  defaultValue = true) boolean silent) {
        Punishment activePunishment = target.getActivePunishment(Punishment.PunishmentType.BLACKLIST);
        if (activePunishment == null) {
            sender.sendMessage(CC.format("&e%s &cis not blacklisted.", target.getName()));
            return false;
        }

        if (invictus.getBukkitPunishmentService().removePunishment(
                sender, target, activePunishment, reason, silent, true))
            sender.sendMessage(CC.GREEN + "Unblacklisted " + target.getRealDisplayName() + CC.GREEN + ".");
        return true;
    }

}
