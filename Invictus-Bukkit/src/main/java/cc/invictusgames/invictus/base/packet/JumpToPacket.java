package cc.invictusgames.invictus.base.packet;

import cc.invictusgames.ilib.redis.packet.Packet;
import cc.invictusgames.invictus.Invictus;
import cc.invictusgames.invictus.listener.StaffModeListener;
import cc.invictusgames.invictus.queue.packet.QueueSendPlayerPacket;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;

import java.util.UUID;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 07.11.2020 / 02:07
 * Invictus / cc.invictusgames.invictus.spigot.base.packet
 */

@NoArgsConstructor
@AllArgsConstructor
public class JumpToPacket implements Packet {

    private static final Invictus invictus = Invictus.getInstance();

    private UUID uuid;
    private UUID target;

    @Override
    public void receive() {
        if (Bukkit.getPlayer(target) != null) {
            StaffModeListener.JUMP_TO_TARGET.put(uuid, target);
            invictus.getRedisService().publish(new QueueSendPlayerPacket(invictus.getServerName(),
                    uuid));
        }
    }
}
