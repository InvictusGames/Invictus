package cc.invictusgames.invictus.tag;

import cc.invictusgames.ilib.utils.json.JsonBuilder;
import com.google.gson.JsonObject;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class Tag {

    private final String name;
    private String displayName;

    public Tag(JsonObject object) {
        this.name = object.get("name").getAsString();
        this.displayName = object.get("displayName").getAsString();
    }


    public JsonObject toJson() {
        return new JsonBuilder()
                .add("name", name)
                .add("displayName", displayName)
                .build();
    }

}

