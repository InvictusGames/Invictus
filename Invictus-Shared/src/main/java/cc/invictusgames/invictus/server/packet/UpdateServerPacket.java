package cc.invictusgames.invictus.server.packet;

import cc.invictusgames.ilib.redis.packet.Packet;
import cc.invictusgames.invictus.server.ServerInfo;
import lombok.RequiredArgsConstructor;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 19.02.2020 / 19:52
 * Invictus / cc.invictusgames.invictus.spigot.server.packet
 */

@RequiredArgsConstructor
public class UpdateServerPacket implements Packet {

    private final ServerInfo serverInfo;

    @Override
    public void receive() {
        ServerInfo.updateServerInfo(serverInfo.getName(), serverInfo);
    }
}
