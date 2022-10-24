package cc.invictusgames.invictus.grant;

import cc.invictusgames.ilib.redis.packet.Packet;
import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.ilib.utils.UUIDUtils;
import cc.invictusgames.ilib.uuid.UUIDCache;
import cc.invictusgames.invictus.InvictusBukkit;
import cc.invictusgames.invictus.connection.BackLogEntry;
import cc.invictusgames.invictus.connection.RequestResponse;
import cc.invictusgames.invictus.profile.packets.ProfileUpdatePacket;
import cc.invictusgames.invictus.utils.PlayerMessagePacket;
import okhttp3.Request;
import org.bukkit.Bukkit;

import java.util.UUID;

/**
 * @author langgezockt (langgezockt@gmail.com)
 * 03.03.2021 / 04:16
 * Invictus / cc.invictusgames.invictus.spigot.grant
 */

public class GrantBackLogEntry extends BackLogEntry {

    private static final InvictusBukkit invictus = InvictusBukkit.getBukkitInstance();

    private final Grant grant;
    private final UUID uuid;

    public GrantBackLogEntry(Grant grant, UUID uuid, Request.Builder builder) {
        super(builder);
        this.grant = grant;
        this.uuid = uuid;
    }

    @Override
    public void onSend(RequestResponse response) {
        String message;

        if (!response.wasSuccessful())
            message = CC.format(
                    "&c[Grant BackLog] Could not %s grant for &e%s&c: %s (%d)",
                    grant.isRemoved() ? "remove" : "create",
                    UUIDCache.getName(uuid),
                    response.getErrorMessage(),
                    response.getCode()
            );
        else message = CC.format(
                "&a[Grant BackLog] Successfully %s grant for &e%s&a.",
                grant.isRemoved() ? "removed" : "created",
                UUIDCache.getName(uuid)
        );

        if (UUIDUtils.isUUID(grant.getGrantedBy()))
            invictus.getRedisService().publish(new PlayerMessagePacket(
                    UUID.fromString(grant.getGrantedBy()),
                    message
            ));
        else Bukkit.getConsoleSender().sendMessage(message);
    }
}
