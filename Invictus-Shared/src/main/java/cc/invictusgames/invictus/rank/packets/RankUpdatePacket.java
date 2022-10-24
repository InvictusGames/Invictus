package cc.invictusgames.invictus.rank.packets;

import cc.invictusgames.ilib.redis.packet.Packet;
import cc.invictusgames.invictus.Invictus;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 18.02.2020 / 19:22
 * Invictus / cc.invictusgames.invictus.spigot.rank.packets
 */

@NoArgsConstructor
public class RankUpdatePacket implements Packet {

    private UUID uuid;

    public RankUpdatePacket(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public void receive() {
        Invictus.getInstance().getRankService().updateRank(uuid, () -> { });
    }
}
