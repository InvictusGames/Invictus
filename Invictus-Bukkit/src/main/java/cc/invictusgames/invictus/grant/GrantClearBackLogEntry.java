package cc.invictusgames.invictus.grant;

import cc.invictusgames.ilib.utils.CC;
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
 * @author Emilxyz (langgezockt@gmail.com)
 * 08.03.2021 / 05:00
 * Invictus / cc.invictusgames.invictus.grant
 */

public class GrantClearBackLogEntry extends BackLogEntry {

    private static final InvictusBukkit invictus = InvictusBukkit.getBukkitInstance();

    private final UUID uuid;
    private final UUID clearedBy;

    public GrantClearBackLogEntry(UUID uuid, UUID clearedBy, Request.Builder builder) {
        super(builder);
        this.uuid = uuid;
        this.clearedBy = clearedBy;
    }

    @Override
    public void onSend(RequestResponse response) {
        String message;
        if (!response.wasSuccessful())
            message = CC.format("&c[Grant BackLog] Could not clear grants of &e%s&c: %s (%d)",
                    UUIDCache.getName(uuid), response.getErrorMessage(), response.getCode());
        else message = CC.format("&a[Grant BackLog] Successfully cleared &e%d &agrants of &e%s&a.",
                response.asObject().get("removed").getAsInt(), UUIDCache.getName(uuid));

        if (clearedBy == null)
            Bukkit.getConsoleSender().sendMessage(message);
        else invictus.getRedisService().publish(new PlayerMessagePacket(clearedBy, message));

        if (response.wasSuccessful())
            invictus.getRedisService().publish(new ProfileUpdatePacket(uuid));
    }
}
