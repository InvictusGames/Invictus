package cc.invictusgames.invictus.disguise;

import cc.invictusgames.ilib.utils.TimeUtils;
import cc.invictusgames.ilib.utils.json.JsonBuilder;
import cc.invictusgames.ilib.uuid.UUIDCache;
import cc.invictusgames.invictus.profile.Profile;
import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.TimeZone;
import java.util.UUID;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 20.06.2020 / 10:54
 * Invictus / cc.invictusgames.invictus.spigot.disguise
 */

@AllArgsConstructor
@Data
public class DisguiseLogEntry {

    private UUID uuid;
    private String name;
    private String rank;
    private long timeStamp;
    private long removedAt = -1;

    public DisguiseLogEntry(JsonObject object) {
        this.uuid = UUID.fromString(object.get("uuid").getAsString());
        this.name = object.get("name").getAsString();
        this.rank = object.get("rank").getAsString();
        this.timeStamp = object.get("timeStamp").getAsLong();
        this.removedAt = object.get("removedAt").getAsLong();
    }

    public JsonObject toJson() {
        return new JsonBuilder()
                .add("uuid", uuid)
                .add("name", name)
                .add("rank", rank)
                .add("timeStamp", timeStamp)
                .add("removedAt", removedAt)
                .build();
    }

    public String formatPasteEntry(TimeZone timeZone, Profile target) {
        return "(" + TimeUtils.formatDate(timeStamp, timeZone) + "|" + timeStamp + ") " + target.getName() + ": "
                + name + "(" + rank + ")" + (removedAt == -1 ? ""
                : " - Removed at (" + TimeUtils.formatDate(removedAt, timeZone) + "|" + removedAt + ")");
    }

    public String resolveRealName() {
        return uuid == null || UUIDCache.getName(uuid) == null ? "?" : UUIDCache.getName(uuid);
    }

}
