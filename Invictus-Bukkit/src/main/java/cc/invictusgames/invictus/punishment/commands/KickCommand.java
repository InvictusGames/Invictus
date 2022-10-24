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
 * 08.06.2020 / 21:01
 * Invictus / cc.invictusgames.invictus.spigot.punishment.commands
 */

@RequiredArgsConstructor
public class KickCommand {

    private final InvictusBukkit invictus;

    @Command(names = {"kick"},
             permission = "invictus.command.kick",
             description = "Kick a player from the network",
             async = true)
    public boolean kick(
            CommandSender sender,
            @Param(name = "player") Profile target,
            @Param(name = "reason", wildcard = true) String reason,
            @Flag(names = {"a", "-announce", "p", "-public"}, description = "Announce the kick", defaultValue = true)
                    boolean silent) {
        Punishment punishment = invictus.getBukkitPunishmentService().createPunishment(
                sender,
                target,
                Punishment.PunishmentType.KICK,
                -1,
                reason,
                Sets.newHashSet(silent ? "silent" : "")
        );

        if (punishment != null)
            sender.sendMessage(CC.GREEN + "Kicked " + target.getRealDisplayName() + CC.GREEN + ".");
        return true;
    }

}
