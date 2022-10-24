package cc.invictusgames.invictus.listener;

import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.invictus.InvictusBungee;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.apache.commons.lang3.StringUtils;


@RequiredArgsConstructor
public class MotdListener implements Listener {

    private final InvictusBungee invictus;

    @EventHandler
    public void onPing(ProxyPingEvent event) {
        ServerPing response = event.getResponse();
        if (invictus.getMainConfig().getMaintenanceLevel() > 0)
            response.setVersion(new ServerPing.Protocol("Maintenance", 100));

        response.setDescription(StringUtils.join(CC.translate(invictus.getMotdConfig().getLines()), "\n"));
        event.setResponse(response);
    }
}
