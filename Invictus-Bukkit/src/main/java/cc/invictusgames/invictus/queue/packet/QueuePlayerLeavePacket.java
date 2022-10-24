package cc.invictusgames.invictus.queue.packet;

import cc.invictusgames.ilib.redis.packet.Packet;
import cc.invictusgames.invictus.InvictusBukkit;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 16.12.2020 / 09:12
 * Invictus / cc.invictusgames.invictus.spigot.queue.packet
 */

@RequiredArgsConstructor
public class QueuePlayerLeavePacket implements Packet {

    private static final InvictusBukkit invictus = InvictusBukkit.getBukkitInstance();

    private final UUID uuid;

    @Override
    public void receive() {
        if (invictus.getQueue().getPlayers().contains(uuid))
            invictus.getProfileService().loadProfile(uuid, profile -> invictus.getQueue().removePlayer(profile), true);
    }
}
