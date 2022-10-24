package cc.invictusgames.invictus.listener;

import cc.invictusgames.invictus.InvictusBungee;
import cc.invictusgames.invictus.InvictusBungeePlugin;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 08.03.2021 / 01:36
 * Invictus / cc.invictusgames.invictus.hub.listener
 */

@RequiredArgsConstructor
public class HubListener implements Listener {

    private final InvictusBungee invictus;

    @EventHandler
    public void onServerKick(ServerKickEvent event) {
        String kickMessage = ChatColor.stripColor(event.getKickReason());
        ProxiedPlayer player = event.getPlayer();

        if (kickMessage.startsWith("You have been kicked") || kickMessage.startsWith("Your account has been"))
            return;

        if (event.getCancelServer() == null || !event.getCancelServer().getName().toLowerCase().startsWith("hub-"))
            event.setCancelServer(ProxyServer.getInstance().getReconnectHandler().getServer(player));

        event.setCancelled(true);
        player.sendMessage(ChatColor.RED + "Kicked from " + ChatColor.GOLD
                + event.getKickedFrom().getName() + ChatColor.RED + ": " + ChatColor.WHITE + event.getKickReason());
    }

}
