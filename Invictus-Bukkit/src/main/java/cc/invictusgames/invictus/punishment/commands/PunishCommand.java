package cc.invictusgames.invictus.punishment.commands;

import cc.invictusgames.ilib.command.annotation.Command;
import cc.invictusgames.ilib.command.annotation.Param;
import cc.invictusgames.invictus.profile.Profile;
import cc.invictusgames.invictus.punishment.template.menu.PunishMenu;
import org.bukkit.entity.Player;

public class PunishCommand {

    @Command(names = {"punish", "p"},
             permission = "invictus.command.punish",
             description = "Open the punishments template selection",
             async = true)
    public boolean punish(Player sender, @Param(name = "target") Profile target) {
        new PunishMenu(target).openMenu(sender);
        return true;
    }

}
