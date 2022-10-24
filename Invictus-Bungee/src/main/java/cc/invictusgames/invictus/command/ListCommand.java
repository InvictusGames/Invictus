package cc.invictusgames.invictus.command;

import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.invictus.Invictus;
import cc.invictusgames.invictus.profile.Profile;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.Set;

public class ListCommand extends Command {

    public ListCommand() {
        super("glist");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Set<String> playerNames = new HashSet<>();
        for (ServerInfo serverInfo : ProxyServer.getInstance().getServersCopy().values()) {
            for (ProxiedPlayer player : serverInfo.getPlayers()) {
                Profile profile = Invictus.getInstance().getProfileService().getProfile(player.getUniqueId());
                playerNames.add(profile.getDisplayName());
            }

            sender.sendMessage(CC.format(
                    "&a[%s] &e(%d) &r%s",
                    serverInfo.getName(),
                    serverInfo.getPlayers().size(),
                    StringUtils.join(playerNames, ChatColor.WHITE + ", ")
            ));
            playerNames.clear();
        }

        sender.sendMessage("Total players online: " + ProxyServer.getInstance().getOnlineCount());
    }

}