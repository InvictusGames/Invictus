package cc.invictusgames.invictus.note;

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
 * 03.03.2021 / 04:55
 * Invictus / cc.invictusgames.invictus.spigot.note
 */

public class NoteBackLogEntry extends BackLogEntry {

    private static final Invictus invictus = Invictus.getInstance();

    private final Note note;
    private final UUID uuid;

    public NoteBackLogEntry(Note note, UUID uuid, Request.Builder builder) {
        super(builder);
        this.note = note;
        this.uuid = uuid;
    }

    @Override
    public void onSend(RequestResponse response) {
        String message;

        if (!response.wasSuccessful())
            message = CC.format(
                    "&c[Note BackLog] Could not create note for &e%s&c: %s (%d)",
                    UUIDCache.getName(uuid),
                    response.getErrorMessage(),
                    response.getCode()
            );
        else message = CC.format(
                "&a[Punishment BackLog] Successfully created note for &e%s&a.",
                UUIDCache.getName(uuid)
        );

        if (UUIDUtils.isUUID(note.getAddedBy()))
            invictus.getRedisService().publish(new PlayerMessagePacket(
                    UUID.fromString(note.getAddedBy()),
                    message
            ));
        else Bukkit.getConsoleSender().sendMessage(message);

        if (response.wasSuccessful())
            invictus.getRedisService().publish(new ProfileUpdatePacket(uuid));
    }
}
