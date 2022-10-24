package cc.invictusgames.invictus.grant.packets;

import cc.invictusgames.ilib.redis.packet.Packet;
import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.ilib.utils.TimeUtils;
import cc.invictusgames.invictus.InvictusBukkit;
import cc.invictusgames.invictus.rank.Rank;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 18.02.2020 / 20:30
 * Invictus / cc.invictusgames.invictus.spigot.grant.packets
 */

@NoArgsConstructor
public class GrantAddPacket implements Packet {

    private static final InvictusBukkit invictus = InvictusBukkit.getBukkitInstance();
    private UUID uuid;
    private UUID rankUuid;
    private long duration;

    public GrantAddPacket(UUID uuid, UUID rankUuid, long duration) {
        this.uuid = uuid;
        this.rankUuid = rankUuid;
        this.duration = duration;
    }

    @Override
    public void receive() {
        Player player = Bukkit.getPlayer(uuid);
        Rank rank = invictus.getRankService().getRank(rankUuid);
        if (player == null) {
            return;
        }

        invictus.getPermissionService().updatePermissions(player);

        if (duration == -1)
            player.sendMessage(CC.format(
                    "&aYou've been &epermanently &agranted the %s&a rank.",
                    rank.getDisplayName()
            ));
        else
            player.sendMessage(CC.format(
                    "&aYou've been granted the %s&a rank for &e%s&a.",
                    rank.getDisplayName(),
                    TimeUtils.formatDetailed(duration)
            ));
    }
}
