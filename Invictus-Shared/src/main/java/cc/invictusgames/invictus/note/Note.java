package cc.invictusgames.invictus.note;

import cc.invictusgames.ilib.utils.json.JsonBuilder;
import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 10.07.2020 / 04:38
 * Invictus / cc.invictusgames.invictus.spigot.note
 */
@AllArgsConstructor
@Data
public class Note {

    private final UUID id;
    private final UUID uuid;
    private String addedBy;
    private String note;
    private String addedOn;
    private long addedAt;

    public Note(JsonObject object) {
        this.id = UUID.fromString(object.get("id").getAsString());
        this.uuid = UUID.fromString(object.get("uuid").getAsString());
        this.addedBy = object.get("addedBy").getAsString();
        this.note = object.get("note").getAsString();
        this.addedOn = object.get("addedOn").getAsString();
        this.addedAt = object.get("addedAt").getAsLong();
    }

    public Note(UUID uuid, String addedBy, String note, String addedOn) {
        this.id = UUID.randomUUID();
        this.uuid = uuid;
        this.addedBy = addedBy;
        this.note = note;
        this.addedOn = addedOn;
        this.addedAt = System.currentTimeMillis();
    }

    public JsonObject toJson() {
        return new JsonBuilder()
                .add("id", id.toString())
                .add("uuid", uuid.toString())
                .add("addedBy", addedBy)
                .add("note", note)
                .add("addedOn", addedOn)
                .add("addedAt", addedAt)
                .build();
    }

}
