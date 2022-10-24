package cc.invictusgames.invictus.queue.packet;

import cc.invictusgames.ilib.redis.packet.Packet;
import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.invictus.InvictusBukkit;
import cc.invictusgames.invictus.InvictusBukkitPlugin;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 25.10.2020 / 21:20
 * Invictus / cc.invictusgames.invictus.spigot.queue.queue
 */

@NoArgsConstructor
@AllArgsConstructor
public class QueueSendPlayerPacket implements Packet {

    private static final InvictusBukkit invictus = InvictusBukkit.getBukkitInstance();

    private String queueName;
    private UUID playerUuid;

    @Override
    public void receive() {
        Player player = Bukkit.getPlayer(playerUuid);
        if (player == null)
            return;

        player.sendMessage(CC.GOLD + "Connecting you to " + CC.WHITE + queueName + CC.GOLD + "...");
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(this.queueName);
        player.sendPluginMessage(InvictusBukkitPlugin.getInstance(), "BungeeCord", out.toByteArray());
    }
}
