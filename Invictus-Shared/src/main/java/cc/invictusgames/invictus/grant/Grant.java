package cc.invictusgames.invictus.grant;

import cc.invictusgames.ilib.utils.json.JsonBuilder;
import cc.invictusgames.invictus.Invictus;
import cc.invictusgames.invictus.rank.Rank;
import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 18.02.2020 / 19:34
 * Invictus / cc.invictusgames.invictus.spigot.grant
 */

@AllArgsConstructor
@Data
public class Grant {

    public static final Comparator<Grant> COMPARATOR = Comparator.comparingInt(grant -> grant.getRank().getWeight());

    private final Invictus invictus;
    private final UUID id;
    private final UUID uuid;
    private UUID rank;

    private String grantedBy = "N/A";
    private long grantedAt = System.currentTimeMillis();
    private String grantedReason = "N/A";

    private String removedBy = "N/A";
    private long removedAt = -1;
    private String removedReason = "N/A";

    private List<String> scopes = new ArrayList<>();
    private long duration = -1;
    private long end = -1;
    private boolean removed = false;

    public Grant(Invictus invictus, UUID uuid, Rank rank, String grantedBy, long grantedAt, String grantedReason,
                 long duration, List<String> scopes) {
        this.invictus = invictus;
        this.id = UUID.randomUUID();
        this.uuid = uuid;
        this.rank = rank.getUuid();
        this.grantedBy = grantedBy;
        this.grantedAt = grantedAt;
        this.grantedReason = grantedReason;
        this.duration = duration;
        this.end = duration == -1 ? -1 : grantedAt + duration;
        this.scopes = scopes;
    }

    public Grant(Invictus invictus, JsonObject object) {
        this.invictus = invictus;
        this.id = UUID.fromString(object.get("id").getAsString());
        this.uuid = UUID.fromString(object.get("uuid").getAsString());
        this.rank = UUID.fromString(object.get("rank").getAsString());
        this.grantedBy = object.get("grantedBy").getAsString();
        this.grantedAt = object.get("grantedAt").getAsLong();
        this.grantedReason = object.get("grantedReason").getAsString();
        this.removedBy = object.get("removedBy").getAsString();
        this.removedAt = object.get("removedAt").getAsLong();
        this.removedReason = object.get("removedReason").getAsString();
        this.scopes = Arrays.asList(object.get("scopes").getAsString().split(","));
        this.duration = object.get("duration").getAsLong();
        this.end = object.get("end").getAsLong();
        this.removed = object.get("removed").getAsBoolean();
    }

    public JsonObject toJson() {
        return new JsonBuilder()
                .add("id", id.toString())
                .add("uuid", uuid.toString())
                .add("rank", rank.toString())
                .add("grantedBy", grantedBy)
                .add("grantedAt", grantedAt)
                .add("grantedReason", grantedReason)
                .add("removedBy", removedBy)
                .add("removedAt", removedAt)
                .add("removedReason", removedReason)
                .add("scopes", StringUtils.join(scopes, ","))
                .add("duration", duration)
                .add("end", end)
                .add("removed", removed)
                .build();
    }

    public boolean isActive() {
        if (end == -1) {
            return true;
        }
        return end >= System.currentTimeMillis();
    }

    public boolean isActiveOnScope() {
        return isActiveOn(invictus.getServerGroup());
    }

    public boolean isActiveOn(String scope) {
        if (scope.equalsIgnoreCase("GLOBAL") || scopes.contains("GLOBAL"))
            return true;

        return scopes.contains(scope.toLowerCase());
    }

    public long getRemainingTime() {
        return this.end - System.currentTimeMillis();
    }

    public Rank getRank() {
        return invictus.getRankService().getRank(rank);
    }

    public UUID getRankRaw() {
        return rank;
    }

}
