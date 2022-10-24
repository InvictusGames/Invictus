package cc.invictusgames.invictus.server.packet;

import cc.invictusgames.ilib.redis.packet.Packet;
import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.invictus.Invictus;
import cc.invictusgames.invictus.server.ServerInfo;
import cc.invictusgames.invictus.server.ServerState;
import cc.invictusgames.invictus.utils.NetworkBroadcastPacket;
import lombok.AllArgsConstructor;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 13.09.2020 / 23:56
 * Invictus / cc.invictusgames.invictus.spigot.server.packet
 */

@AllArgsConstructor
public class ServerStateChangePacket implements Packet {

    private static final Invictus invictus = Invictus.getInstance();

    private final String name;
    private final ServerState state;

    @Override
    public void receive() {
        ServerInfo server = ServerInfo.getServerInfo(name);

        if (server != null) {
            server.setState(state);
            server.setLastHeartbeat(System.currentTimeMillis());
        }

        invictus.getRedisService().publish(new NetworkBroadcastPacket(
                CC.format(
                        "&8[&cServer Monitor&8] &fStatus of &e%s&f changed to %s&f.",
                        name,
                        state.getInternalName()
                ),
                "invictus.admin",
                true,
                invictus.getMainConfig().getServerName()
        ));
    }
}
