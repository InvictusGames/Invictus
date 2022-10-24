package cc.invictusgames.invictus.rank.packets;

import cc.invictusgames.ilib.redis.packet.Packet;
import cc.invictusgames.invictus.Invictus;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 18.02.2020 / 19:08
 * Invictus / cc.invictusgames.invictus.spigot.rank.packets
 */

@NoArgsConstructor
public class RankCreatePacket implements Packet {

    private static final Invictus invictus = Invictus.getInstance();
    private UUID uuid;

    public RankCreatePacket(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public void receive() {
        invictus.getRankService().loadRank(this.uuid, (rank) -> { });
    }
}
