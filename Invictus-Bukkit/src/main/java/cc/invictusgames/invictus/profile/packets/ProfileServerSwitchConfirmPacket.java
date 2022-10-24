package cc.invictusgames.invictus.profile.packets;

import cc.invictusgames.ilib.redis.packet.Packet;
import cc.invictusgames.invictus.InvictusBukkit;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 12.10.2020 / 19:43
 * Invictus / cc.invictusgames.invictus.spigot.profile.packets
 */

@NoArgsConstructor
@AllArgsConstructor
public class ProfileServerSwitchConfirmPacket implements Packet {

    private static final InvictusBukkit invictus = InvictusBukkit.getBukkitInstance();

    private UUID uuid;
    private String server;

    @Override
    public void receive() {
        if (invictus.getServerName().equals(server))
            invictus.getConfirmedSwitch().add(uuid);
    }
}
