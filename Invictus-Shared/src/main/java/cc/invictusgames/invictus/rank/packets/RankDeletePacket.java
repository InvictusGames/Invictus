package cc.invictusgames.invictus.rank.packets;

import cc.invictusgames.ilib.redis.packet.Packet;
import cc.invictusgames.invictus.Invictus;
import cc.invictusgames.invictus.rank.Rank;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 18.02.2020 / 19:26
 * Invictus / cc.invictusgames.invictus.spigot.rank.packets
 */

@NoArgsConstructor
public class RankDeletePacket implements Packet {

    private static final Invictus invictus = Invictus.getInstance();
    private UUID uuid;

    public RankDeletePacket(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public void receive() {
        Rank rank = invictus.getRankService().getRank(uuid);
        if (rank != null) {
            invictus.handleRankDeletion(rank);
        }
        invictus.getRankService().deleteRank(uuid, () -> { });
    }
}
