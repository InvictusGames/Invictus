package cc.invictusgames.invictus.base.commands;

import cc.invictusgames.ilib.command.annotation.Command;
import cc.invictusgames.ilib.command.annotation.Param;
import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.invictus.InvictusBukkit;
import cc.invictusgames.invictus.profile.Profile;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.WordUtils;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 04.06.2020 / 11:05
 * Invictus / cc.invictusgames.invictus.spigot.base.commands
 */

@RequiredArgsConstructor
public class GamemodeCommands {

    private final InvictusBukkit invictus;

    @Command(names = {"gamemode", "gm"},
             permission = "invictus.command.gamemode",
             description = "Set the gamemode of a player")
    public boolean gamemode(CommandSender sender,
                            @Param(name = "mode", defaultValue = "@toggle") GameMode mode,
                            @Param(name = "player", defaultValue = "@self") Player target) {
        execute(sender, target, mode);
        return true;
    }

    @Command(names = {"gms", "gm0"},
             permission = "invictus.command.gamemode",
             description = "Set the gamemode of a player to survival")
    public boolean gms(CommandSender sender, @Param(name = "player", defaultValue = "@self") Player target) {
        return execute(sender, target, GameMode.SURVIVAL);
    }

    @Command(names = {"gmc", "gm1"},
             permission = "invictus.command.gamemode",
             description = "Set the gamemode of a player to creative")
    public boolean gmc(CommandSender sender, @Param(name = "player", defaultValue = "@self") Player target) {
        return execute(sender, target, GameMode.CREATIVE);
    }

    @Command(names = {"gma", "gm2"},
             permission = "invictus.command.gamemode",
             description = "Set the gamemode of a player to adventure")
    public boolean gma(CommandSender sender, @Param(name = "player", defaultValue = "@self") Player target) {
        return execute(sender, target, GameMode.ADVENTURE);
    }

    private boolean execute(CommandSender sender, Player target, GameMode mode) {
        if ((!sender.equals(target)) && (!sender.hasPermission("invictus.command.gamemode.other"))) {
            sender.sendMessage(CC.RED + "You are not allowed to change the gamemode of other players");
            return false;
        }

        Profile profile = invictus.getProfileService().getProfile(target);

        target.setGameMode(mode);
        target.sendMessage(CC.format("&6You are now in &f%s &6mode.", WordUtils.capitalizeFully(mode.name())));
        if (!sender.equals(target))
            sender.sendMessage(CC.format("%s &6is now in &f%s &6mode.",
                    profile.getDisplayName(sender), WordUtils.capitalizeFully(mode.name())));
        return true;
    }
}
