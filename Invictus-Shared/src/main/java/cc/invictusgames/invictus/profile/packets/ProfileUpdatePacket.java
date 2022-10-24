package cc.invictusgames.invictus.profile.packets;

import cc.invictusgames.ilib.redis.packet.Packet;
import cc.invictusgames.invictus.Invictus;
import cc.invictusgames.invictus.profile.Profile;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 01.04.2020 / 21:33
 * Invictus / cc.invictusgames.invictus.spigot.profile.packets
 */

@NoArgsConstructor
public class ProfileUpdatePacket implements Packet {

    private static final Invictus invictus = Invictus.getInstance();

    private UUID uuid;

    public ProfileUpdatePacket(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public void receive() {
        Profile profile = invictus.getProfileService().getProfile(uuid);
        if (profile == null) {
            return;
        }

        invictus.getProfileService().updateProfile(uuid, unused -> { }, true);
    }
}
