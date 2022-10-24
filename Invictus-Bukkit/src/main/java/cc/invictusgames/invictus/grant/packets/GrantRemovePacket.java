package cc.invictusgames.invictus.grant.packets;

import cc.invictusgames.ilib.redis.packet.Packet;
import cc.invictusgames.ilib.utils.CC;
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
public class GrantRemovePacket implements Packet {

    private static final InvictusBukkit invictus = InvictusBukkit.getBukkitInstance();
    private UUID uuid;
    private UUID rankUuid;

    public GrantRemovePacket(UUID uuid, UUID rankUuid) {
        this.uuid = uuid;
        this.rankUuid = rankUuid;
    }

    @Override
    public void receive() {
        Player player = Bukkit.getPlayer(uuid);
        Rank rank = invictus.getRankService().getRank(rankUuid);
        if (player == null) {
            return;
        }

        invictus.getPermissionService().updatePermissions(player);

        player.sendMessage(CC.format(
                "&aYour %s&a grant has been removed.",
                rank.getDisplayName()
        ));
    }
}
