package cc.invictusgames.invictus.utils;

import cc.invictusgames.ilib.redis.packet.Packet;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 02.10.2020 / 22:21
 * Invictus / cc.invictusgames.invictus.spigot.utils
 */

@NoArgsConstructor
public class PlayerMessagePacket implements Packet {

    private UUID player;
    private List<String> message;

    public PlayerMessagePacket(UUID player, String message) {
        this.player = player;
        this.message = Collections.singletonList(message);
    }

    public PlayerMessagePacket(UUID player, String... message) {
        this.player = player;
        this.message = Arrays.asList(message);
    }

    @Override
    public void receive() {
        if (Bukkit.getPlayer(player) != null) {
            message.forEach(Bukkit.getPlayer(player)::sendMessage);
        }
    }
}
