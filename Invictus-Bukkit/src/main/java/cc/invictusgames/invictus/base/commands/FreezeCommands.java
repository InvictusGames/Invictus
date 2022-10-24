package cc.invictusgames.invictus.base.commands;

import cc.invictusgames.ilib.command.annotation.Command;
import cc.invictusgames.ilib.command.annotation.Param;
import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.invictus.InvictusBukkit;
import cc.invictusgames.invictus.profile.Profile;
import cc.invictusgames.invictus.utils.NetworkBroadcastPacket;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 10.10.2020 / 20:40
 * Invictus / cc.invictusgames.invictus.spigot.base.commands
 */

@RequiredArgsConstructor
public class FreezeCommands {

    private final InvictusBukkit invictus;

    @Command(names = {"freeze", "ss"},
             permission = "invictus.command.freeze",
             description = "Freeze a player")
    public boolean freeze(CommandSender sender, @Param(name = "target") Player target) {
        String displayName = sender instanceof Player
                ? invictus.getProfileService().getProfile((Player) sender).getRealDisplayName()
                : CC.DRED + "Console";
        Profile profile = invictus.getProfileService().getProfile(target);

        boolean frozen = !profile.isFrozen();
        profile.setFrozen(frozen);

        invictus.getRedisService().publish(new NetworkBroadcastPacket(
                invictus.getMessageService().formatMessage(
                        "staff.freeze." + (frozen ? "frozen" : "unfrozen"),
                        invictus.getServerName(),
                        displayName,
                        profile.getRealDisplayName()
                ),
                "invictus.staff",
                true
        ));

        sender.sendMessage(profile.getDisplayName(sender) + CC.YELLOW + " is " + (frozen ? "now" : "no longer") + " " +
                "frozen.");

        if (frozen)
            invictus.getMessageService().sendMessage(target, "base.freeze.target-alert");
        return true;
    }

}
