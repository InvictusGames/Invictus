package cc.invictusgames.invictus.antivpn.command;

import cc.invictusgames.ilib.command.annotation.Command;
import cc.invictusgames.invictus.InvictusBukkit;
import lombok.RequiredArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

@RequiredArgsConstructor
public class AntiVPNCommands {

    private final InvictusBukkit invictus;

    @Command(names = "antivpn enable",
             permission = "owner",
             description = "Enable antivpn")
    public boolean enable(CommandSender sender) {
        invictus.getMainConfig().setAntiVPN(true);
        invictus.saveMainConfig();
        sender.sendMessage(ChatColor.YELLOW + "AntiVPN status has been set to true.");
        return true;
    }

    @Command(names = "antivpn disable",
             permission = "owner",
             description = "Disable antivpn")
    public boolean disable(CommandSender sender) {
        invictus.getMainConfig().setAntiVPN(false);
        invictus.saveMainConfig();
        sender.sendMessage(ChatColor.YELLOW + "AntiVPN status has been set to false.");
        return true;
    }

}
