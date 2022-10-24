package cc.invictusgames.invictus.punishment;

import cc.invictusgames.ilib.redis.packet.Packet;
import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.ilib.utils.UUIDUtils;
import cc.invictusgames.ilib.uuid.UUIDCache;
import cc.invictusgames.invictus.Invictus;
import cc.invictusgames.invictus.connection.BackLogEntry;
import cc.invictusgames.invictus.connection.RequestResponse;
import cc.invictusgames.invictus.profile.packets.ProfileUpdatePacket;
import cc.invictusgames.invictus.utils.PlayerMessagePacket;
import okhttp3.Request;
import org.bukkit.Bukkit;

import java.util.UUID;

/**
 * @author langgezockt (langgezockt@gmail.com)
 * 02.03.2021 / 18:11
 * Invictus / cc.invictusgames.invictus.spigot.punishment
 */

public class PunishmentBackLogEntry extends BackLogEntry {

    private static final Invictus invictus = Invictus.getInstance();

    private final Punishment punishment;
    public PunishmentBackLogEntry(Punishment punishment, Request.Builder builder) {
        super(builder);
        this.punishment = punishment;
    }

    @Override
    public void onSend(RequestResponse response) {
        String message;

        if (!response.wasSuccessful())
            message = CC.format(
                    "&c[Punishment BackLog] Could not %s %s for &e%s&c: %s (%d)",
                    punishment.isRemoved() ? "remove" : "create",
                    punishment.getPunishmentType().getName(),
                    UUIDCache.getName(punishment.getUuid()),
                    response.getErrorMessage(),
                    response.getCode()
            );
        else message = CC.format(
                "&a[Punishment BackLog] Successfully %s %s for &e%s&a.",
                punishment.isRemoved() ? "removed" : "created",
                punishment.getPunishmentType().getName(),
                UUIDCache.getName(punishment.getUuid())
        );

        if (UUIDUtils.isUUID(punishment.getPunishedBy()))
            invictus.getRedisService().publish(new PlayerMessagePacket(
                    UUID.fromString(punishment.getPunishedBy()),
                    message
            ));
        else Bukkit.getConsoleSender().sendMessage(message);
    }
}
