package cc.invictusgames.invictus;

import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.invictus.profile.Profile;
import cc.invictusgames.invictus.server.ServerInfo;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ReconnectHandler;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@RequiredArgsConstructor
public class InvictusReconnectHandler implements ReconnectHandler {

    private final InvictusBungee invictus;

    @Override
    public net.md_5.bungee.api.config.ServerInfo getServer(ProxiedPlayer player) {
        Profile profile = Invictus.getInstance().getProfileService().getProfile(player.getUniqueId());

        List<ServerInfo> available = new ArrayList<>();
        if (profile.getRealCurrentGrant().getRank().getWeight() >= invictus.getMainConfig().getRestrictedHubWeight()
                || player.hasPermission("invictus.bungee.restrictedhub")) {
            for (ServerInfo server : ServerInfo.getServers()) {
                if (server.isOnline() && server.getGrantScope().equalsIgnoreCase("hub-restricted"))
                    available.add(server);
            }
        }

        if (available.isEmpty()) {
            for (ServerInfo server : ServerInfo.getServers()) {
                if (server.isOnline() && server.getGrantScope().equalsIgnoreCase("hub"))
                    available.add(server);
            }
        }

        if (available.isEmpty()) {
            player.disconnect(CC.RED + "Could not find a suitable hub to connect you to.");
            return null;
        }

        ServerInfo info = available.get(ThreadLocalRandom.current().nextInt(available.size()));
        player.sendMessage(CC.format("&6Connecting you to &f%s&6...", info.getName()));
        return ProxyServer.getInstance().getServerInfo(info.getName());
    }

    @Override
    public void setServer(ProxiedPlayer proxiedPlayer) {
        //empty
    }

    @Override
    public void save() {
        //empty
    }

    @Override
    public void close() {
        //empty
    }
}
