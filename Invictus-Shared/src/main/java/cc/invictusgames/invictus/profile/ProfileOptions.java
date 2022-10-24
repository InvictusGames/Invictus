package cc.invictusgames.invictus.profile;

import cc.invictusgames.ilib.utils.json.JsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 02.04.2020 / 00:28
 * Invictus / cc.invictusgames.invictus.spigot.profile
 */

@Data
@NoArgsConstructor
public class ProfileOptions {

    private Map<String, String> customOptions = new HashMap<>();

    private List<String> socialSpy = new ArrayList<>();
    private List<UUID> ignoring = new ArrayList<>();

    public ProfileOptions(JsonObject object) {
        if (object.has("socialSpy")) {
            JsonArray array = object.get("socialSpy").getAsJsonArray();
            array.forEach(element -> socialSpy.add(element.getAsString()));
        }

        if (object.has("ignoring")) {
            JsonArray array = object.get("ignoring").getAsJsonArray();
            array.forEach(element -> ignoring.add(UUID.fromString(element.getAsString())));
        }

        if (object.has("customOptions")) {
            JsonObject optionsObject = object.get("customOptions").getAsJsonObject();
            optionsObject.entrySet().forEach(entry ->
                    customOptions.put(entry.getKey(), entry.getValue().getAsString()));
        }
    }

    public JsonObject toJson() {
        JsonBuilder builder = new JsonBuilder();

        JsonArray spyArray = new JsonArray();
        socialSpy.forEach(spyArray::add);
        builder.add("socialSpy", spyArray);

        JsonArray ignoringArray = new JsonArray();
        ignoring.forEach(uuid -> ignoringArray.add(uuid.toString()));
        builder.add("ignoring", ignoringArray);

        JsonObject optionsObject = new JsonObject();
        customOptions.forEach(optionsObject::addProperty);
        builder.add("customOptions", optionsObject);

        return builder.build();
    }

    public String getOption(String key) {
        return customOptions.get(key);
    }

    public String getOption(String key, String defaultValue) {
        return customOptions.getOrDefault(key, defaultValue);
    }

    public void setOption(String key, String value) {
        customOptions.put(key, value);
    }

}
