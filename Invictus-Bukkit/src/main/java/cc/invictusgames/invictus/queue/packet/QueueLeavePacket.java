package cc.invictusgames.invictus.queue.packet;

import cc.invictusgames.ilib.redis.packet.Packet;
import cc.invictusgames.invictus.InvictusBukkit;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 25.10.2020 / 21:18
 * Invictus / cc.invictusgames.invictus.spigot.queue.queue
 */

@NoArgsConstructor
@AllArgsConstructor
public class QueueLeavePacket implements Packet {

    private static final InvictusBukkit invictus = InvictusBukkit.getBukkitInstance();

    private String queueName;
    private UUID playerUuid;

    @Override
    public void receive() {
        if (invictus.getServerName().equals(queueName))
            invictus.getProfileService().loadProfile(playerUuid, profile ->
                    invictus.getQueue().removePlayer(profile), true);
    }


}
