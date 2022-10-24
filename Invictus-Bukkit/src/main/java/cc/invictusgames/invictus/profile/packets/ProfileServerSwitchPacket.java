package cc.invictusgames.invictus.profile.packets;

import cc.invictusgames.ilib.redis.packet.Packet;
import cc.invictusgames.invictus.InvictusBukkit;
import cc.invictusgames.invictus.profile.Profile;
import cc.invictusgames.invictus.utils.NetworkBroadcastPacket;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 12.10.2020 / 19:42
 * Invictus / cc.invictusgames.invictus.spigot.profile.packets
 */

@NoArgsConstructor
@AllArgsConstructor
public class ProfileServerSwitchPacket implements Packet {

    private static final InvictusBukkit invictus = InvictusBukkit.getBukkitInstance();

    private UUID uuid;
    private String server;

    @Override
    public void receive() {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null)
            return;

        if (server.equals(invictus.getServerName()))
            return;

        invictus.getRedisService().publish(new ProfileServerSwitchConfirmPacket(uuid, server));
        Profile profile = invictus.getProfileService().getProfile(player);

        if (profile == null)
            return;

        if (player.hasPermission("invictus.staff")) {
            invictus.getRedisService().publish(new NetworkBroadcastPacket(
                    invictus.getMessageService().formatMessage(
                            "staff.switch",
                            profile.getRealCurrentGrant().getRank().getPrefix()
                                    + profile.getName() + profile.getRealCurrentGrant().getRank().getSuffix(),
                            invictus.getServerName(),
                            server
                    ),
                    "invictus.staff",
                    true
            ));
        }
    }
}
