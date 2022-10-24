package cc.invictusgames.invictus.disguise;

import cc.invictusgames.ilib.utils.callback.Callable;
import cc.invictusgames.ilib.utils.json.JsonBuilder;
import cc.invictusgames.invictus.Invictus;
import cc.invictusgames.invictus.connection.RequestHandler;
import cc.invictusgames.invictus.connection.RequestResponse;
import cc.invictusgames.invictus.rank.Rank;
import cc.invictusgames.invictus.utils.Tasks;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 20.06.2020 / 10:52
 * Invictus / cc.invictusgames.invictus.spigot.disguise
 */

@RequiredArgsConstructor
@Data
public class DisguiseData {

    private final Invictus invictus;
    private UUID uuid;

    private String disguiseName = "N/A";
    private Rank disguiseRank;

    private String texture;
    private String signature;

    private List<DisguiseLogEntry> logs = new ArrayList<>();

    public DisguiseData(Invictus invictus, UUID uuid) {
        this.invictus = invictus;
        this.uuid = uuid;
        this.disguiseRank = invictus.getRankService().getDefaultRank();
    }

    public DisguiseData(Invictus invictus, JsonObject object) {
        this.invictus = invictus;
        this.uuid = UUID.fromString(object.get("uuid").getAsString());
        this.disguiseName = object.get("disguiseName").getAsString();

        Rank rank = invictus.getRankService().getRank(UUID.fromString(object.get("disguiseRank").getAsString()));
        this.disguiseRank = rank == null ? invictus.getRankService().getDefaultRank() : rank;

        if (object.has("texture"))
            this.texture = object.get("texture").getAsString();

        if (object.has("signature"))
            this.signature = object.get("signature").getAsString();

        object.get("logs").getAsJsonArray().forEach(element -> logs.add(new DisguiseLogEntry(element.getAsJsonObject())));
    }

    public JsonObject toJson() {
        JsonBuilder builder = new JsonBuilder();
        builder.add("uuid", this.uuid);
        builder.add("disguiseName", this.disguiseName);
        builder.add("disguiseNameLowerCase", this.disguiseName.toLowerCase());

        if (this.disguiseRank == null) {
            builder.add("disguiseRank", invictus.getRankService().getDefaultRank().getUuid());
        } else {
            builder.add("disguiseRank", this.disguiseRank.getUuid().toString());
        }

        builder.add("texture", this.texture);
        builder.add("signature", this.signature);

        JsonArray logArray = new JsonArray();
        logs.forEach(log -> logArray.add(log.toJson()));
        builder.add("logs", logArray);
        return builder.build();
    }

    public void save(Callable callable, boolean async) {
        if (async) {
            Tasks.runAsync(() -> save(callable, false));
            return;
        }

        JsonObject body = toJson();
        RequestResponse response = RequestHandler.put("disguise", body);
        if (!response.wasSuccessful()) {
            if (response.getCode() == 404) {
                RequestResponse createResponse = RequestHandler.post("disguise", body);
                if (!createResponse.wasSuccessful())
                    DisguiseService.LOG.warning(String.format(
                            "Could not create disguise data of %s: %s (%d)",
                            uuid.toString(),
                            createResponse.getErrorMessage(),
                            createResponse.getCode()
                    ));

            } else DisguiseService.LOG.warning(String.format(
                    "Could not save disguise data of %s: %s (%d)",
                    uuid.toString(),
                    response.getErrorMessage(),
                    response.getCode()
            ));
        }

        callable.callback();
    }

}
