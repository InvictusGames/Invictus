package cc.invictusgames.invictus.punishment.packets;

import cc.invictusgames.ilib.redis.packet.Packet;
import cc.invictusgames.invictus.Invictus;
import cc.invictusgames.invictus.profile.Profile;
import cc.invictusgames.invictus.profile.packets.ProfileUpdatePacket;
import cc.invictusgames.invictus.punishment.Punishment;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 09.03.2021 / 15:08
 * Invictus / cc.invictusgames.invictus.punishment.packets
 */

@NoArgsConstructor
public class StaffRollbackPacket implements Packet {

    private static final Invictus INVICTUS = Invictus.getInstance();

    private UUID punishedBy;

    public StaffRollbackPacket(UUID punishedBy) {
        this.punishedBy = punishedBy;
    }

    @Override
    public void receive() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Profile profile = INVICTUS.getProfileService().getProfile(player);
            for (Punishment punishment : profile.getPunishments()) {
                if (punishment.isActive()
                        && !punishment.isRemoved()
                        && punishment.getPunishedBy().equals(punishedBy.toString())) {
                    INVICTUS.getRedisService().publish(new ProfileUpdatePacket(profile.getUuid()));
                    break;
                }
            }
        }
    }
}
