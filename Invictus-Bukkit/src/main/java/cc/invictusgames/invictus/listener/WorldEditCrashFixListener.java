package cc.invictusgames.invictus.listener;

import cc.invictusgames.ilib.protocol.TinyProtocol;
import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.invictus.Invictus;
import cc.invictusgames.invictus.profile.Profile;
import cc.invictusgames.invictus.utils.NetworkBroadcastPacket;
import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayInTabComplete;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
public class WorldEditCrashFixListener implements TinyProtocol {

    private final Invictus invictus;

    private final Map<UUID, Integer> infractions = new HashMap<>();

    @Override
    public Packet handleIncomingPacket(Player player, Packet packet, ChannelHandlerContext ctx) {
        if (!(packet instanceof PacketPlayInTabComplete))
            return packet;

        PacketPlayInTabComplete tabComplete = (PacketPlayInTabComplete) packet;
        String message = tabComplete.a().toLowerCase();

        if (FreezeListener.isDisallowedWorldEditCommand(message)
                && (message.contains("for(") || message.contains("for ("))) {
            infractions.putIfAbsent(player.getUniqueId(), 0);
            infractions.put(player.getUniqueId(), infractions.get(player.getUniqueId()) + 1);

            Profile profile = invictus.getProfileService().getProfile(player);
            invictus.getRedisService().publish(new NetworkBroadcastPacket(
                    CC.format(
                            "&4&l[WE-Fix] &7[%s] %s &ctried to crash the server: &e%s&c. &7(%d/3)",
                            invictus.getServerName(),
                            profile.getRealDisplayName(),
                            message,
                            infractions.get(player.getUniqueId())
                    ),
                    "invictus.admin",
                    true
            ));

            if (infractions.get(player.getUniqueId()) >= 3) {
                infractions.remove(player.getUniqueId());
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                        String.format("ban %s [WE-Fix] Attempted Crash",
                                player.getName()));

                Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                        String.format("note add %s [WE-Fix] Crash command: %s",
                                player.getName(), message));
            }
            return null;
        }

        return packet;
    }

    @Override
    public Packet handleOutgoingPacket(Player player, Packet packet, ChannelHandlerContext channelHandlerContext) {
        return packet;
    }
}
