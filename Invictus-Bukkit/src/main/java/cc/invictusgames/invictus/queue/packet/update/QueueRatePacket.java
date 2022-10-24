package cc.invictusgames.invictus.queue.packet.update;

import cc.invictusgames.ilib.redis.packet.Packet;
import cc.invictusgames.invictus.InvictusBukkit;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 31.10.2020 / 11:17
 * Invictus / cc.invictusgames.invictus.spigot.queue.packet
 */

@NoArgsConstructor
@AllArgsConstructor
public class QueueRatePacket implements Packet {

    private static final InvictusBukkit invictus = InvictusBukkit.getBukkitInstance();

    private String queueName;
    private int rate;

    @Override
    public void receive() {
        if (invictus.getServerName().equals(queueName)) {
            invictus.getMainConfig().setQueueRate(rate);
            invictus.saveMainConfig();
        }
    }
}
