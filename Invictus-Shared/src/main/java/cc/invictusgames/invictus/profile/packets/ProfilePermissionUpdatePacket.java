package cc.invictusgames.invictus.profile.packets;

import cc.invictusgames.ilib.redis.packet.Packet;
import cc.invictusgames.invictus.Invictus;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 09.10.2020 / 16:30
 * Invictus / cc.invictusgames.invictus.spigot.profile.packets
 */

@NoArgsConstructor
@AllArgsConstructor
public class ProfilePermissionUpdatePacket implements Packet {

    private UUID uuid;

    @Override
    public void receive() {
        Invictus.getInstance().updatePermissions(uuid);
    }
}
