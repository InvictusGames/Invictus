package cc.invictusgames.invictus.command;

import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.invictus.InvictusBungeePlugin;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class HubCommand extends Command {

    public HubCommand() {
        super("hub");
    }

    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        if (!(commandSender instanceof ProxiedPlayer))
            return;

        ProxiedPlayer proxiedPlayer = (ProxiedPlayer) commandSender;
        ServerInfo serverInfo = null;
        boolean chooseHub = false;

        if (commandSender.hasPermission("invictus.staff")
                && strings.length == 1) {
            serverInfo = BungeeCord.getInstance().getServerInfo(strings[0]);

            if (serverInfo == null) {
                proxiedPlayer.sendMessage(ChatColor.RED + "This hub does not exist.");
                return;
            }

            if (serverInfo.getName().toLowerCase().startsWith("hub-"))
                chooseHub = true;
            else proxiedPlayer.sendMessage(CC.RED + "This is not a hub!");
        }

        if (proxiedPlayer.getServer().getInfo().getName().toLowerCase().startsWith("hub-")
                && strings.length == 0) {
            proxiedPlayer.sendMessage(CC.RED + "You are already on a hub.");
            return;
        }

        if (!chooseHub)
            serverInfo = InvictusBungeePlugin.getInstance().getProxy().getReconnectHandler().getServer(proxiedPlayer);
        else proxiedPlayer.sendMessage(CC.format("&6Connecting you to &f%s&6...", serverInfo.getName()));

        proxiedPlayer.connect(serverInfo);
    }
}
