package cc.invictusgames.invictus.punishment.packets;

import cc.invictusgames.ilib.redis.packet.Packet;
import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.ilib.utils.ChatMessage;
import cc.invictusgames.invictus.InvictusBukkit;
import cc.invictusgames.invictus.playersetting.InvictusSettings;
import cc.invictusgames.invictus.punishment.Punishment;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
public class PunishmentRemovePacket implements Packet {

    private static final InvictusBukkit INVICTUS = InvictusBukkit.getBukkitInstance();

    private UUID uuid;
    private String executor;
    private Punishment.PunishmentType type;
    private String reason;
    private boolean silent;

    @Override
    public void receive() {
        INVICTUS.getProfileService().loadProfile(uuid, profile -> {
            ChatMessage message = new ChatMessage(CC.format(
                    "%s&a was%s&a %s by %s&a.",
                    profile.getRealDisplayName(),
                    silent ? " " + CC.YELLOW + "silently" : "",
                    type.getRemoveContext().toLowerCase(),
                    executor
            ));

            message.hoverText(
                    CC.format("&eReason: &c" + reason)
            );

            for (Player current : Bukkit.getOnlinePlayers()) {
                if (current.hasPermission("invictus.staff") && InvictusSettings.STAFF_MESSAGES.get(current))
                    message.send(current);
                else if (!silent) current.sendMessage(message.build().toLegacyText());
            }

            message.send(Bukkit.getConsoleSender());
        }, true);
    }
}
