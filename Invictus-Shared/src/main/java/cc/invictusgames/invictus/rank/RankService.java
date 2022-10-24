package cc.invictusgames.invictus.rank;

import cc.invictusgames.ilib.utils.Timings;
import cc.invictusgames.ilib.utils.callback.Callable;
import cc.invictusgames.ilib.utils.callback.TypeCallable;
import cc.invictusgames.invictus.Invictus;
import cc.invictusgames.invictus.connection.RequestHandler;
import cc.invictusgames.invictus.connection.RequestResponse;
import cc.invictusgames.invictus.utils.Tasks;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 18.02.2020 / 17:50
 * Invictus / cc.invictusgames.invictus.spigot.rank
 */

@RequiredArgsConstructor
public class RankService {

    public static final Logger LOG = Invictus.getInstance().getLogFactory().newLogger(RankService.class);

    private final Invictus invictus;
    @Getter
    private boolean loaded = false;
    private final Map<UUID, Rank> ranks = new ConcurrentHashMap<>();

    public void loadRanks(Callable callable) {
        Tasks.runAsync(() -> {
            LOG.config("Loading ranks...");
            Timings timings = new Timings("rank-loading").startTimings();
            ranks.clear();

            RequestResponse response = RequestHandler.get("rank");
            if (!response.wasSuccessful()) {
                LOG.warning(String.format("Could not load ranks: %s (%d)",
                        response.getErrorMessage(), response.getCode()));
                return;
            }

            JsonArray rankArray = response.asArray();
            rankArray.forEach(object -> {
                Rank rank = new Rank(invictus, object.getAsJsonObject());
                ranks.put(rank.getUuid(), rank);
            });

            for (Rank rank : ranks.values()) {
                response = RequestHandler.get("rank/%s", rank.getUuid().toString());
                if (!response.wasSuccessful()) {
                    LOG.warning(String.format("Could not load inherits for %s: %s (%d)",
                            rank.getName(), response.getErrorMessage(), response.getCode()));
                    continue;
                }

                JsonObject object = response.asObject();
                if (!object.has("inherits"))
                    continue;

                object.get("inherits").getAsJsonArray().forEach(element -> {
                    Rank inherit = getRank(UUID.fromString(element.getAsString()));
                    if (inherit != null)
                        rank.getInherits().add(inherit);
                });
            }

            LOG.info(String.format("Loaded %d ranks in %dms",
                    ranks.size(), timings.stopTimings().calculateDifference()));
            loaded = true;
            callable.callback();
        });
    }

    public void loadRank(UUID uuid, TypeCallable<Rank> callable) {
        Tasks.runAsync(() -> {
            RequestResponse response = RequestHandler.get("rank/%s", uuid.toString());
            if (!response.wasSuccessful()) {
                LOG.warning(String.format("Could not load rank %s: %s (%d)",
                        uuid.toString(), response.getErrorMessage(), response.getCode()));
                return;
            }

            JsonObject object = response.asObject();
            Rank rank = new Rank(invictus, object);
            object.get("inherits").getAsJsonArray().forEach(element -> {
                Rank inherit = getRank(UUID.fromString(element.getAsString()));
                if (inherit != null)
                    rank.getInherits().add(inherit);
            });
            ranks.put(rank.getUuid(), rank);
            callable.callback(rank);
        });
    }

    public void deleteRank(UUID uuid, Callable callable) {
        Tasks.runAsync(() -> {
            RequestResponse response = RequestHandler.delete("rank/%s", uuid.toString());
            if (response.getCode() != 404 && !response.wasSuccessful()) {
                LOG.warning(String.format("Could not delete rank %s: %s (%d)",
                        uuid.toString(), response.getErrorMessage(), response.getCode()));
                return;
            }

            Rank rank = getRank(uuid);
            if (rank != null) {
                LOG.config(String.format("Deleting rank %s...", rank.getName()));
                ranks.remove(rank.getUuid());
                invictus.updatePermissionsWithRank(rank);
            }

            callable.callback();
        });
    }

    public void updateRank(UUID uuid, Callable callable) {
        Rank rank = getRank(uuid);
        if (rank != null)
            LOG.config(String.format("Updating rank %s...", rank.getName()));

        loadRank(uuid, (newRank) -> {
            invictus.updatePermissionsWithRank(newRank);
            callable.callback();
            ranks.put(newRank.getUuid(), newRank);
        });
    }

    public Rank getRank(UUID uuid) {
        return ranks.get(uuid);
    }

    public Rank getRank(String name) {
        for (Rank rank : ranks.values()) {
            if (rank.getName().equalsIgnoreCase(name))
                return rank;
        }

        return null;
    }

    public Rank getDefaultRank() {
        Rank found = null;
        for (Rank rank : ranks.values()) {
            if (rank.isDefaultRank()) {
                found = rank;
                break;
            }
        }

        if (found != null)
            return found;

        LOG.info("Default rank missing, creating a new one");
        found = new Rank(invictus, "Member");
        found.setDefaultRank(true);

        RequestResponse response = RequestHandler.post("rank", found.toJson());
        if (!response.wasSuccessful())
            LOG.warning(String.format("Could not create default rank: %s (%d)",
                    response.getErrorMessage(), response.getCode()));

        ranks.put(found.getUuid(), found);

        return found;
    }

    public List<Rank> getRanks() {
        return new ArrayList<>(ranks.values());
    }

    public List<Rank> getRanksSorted() {
        List<Rank> sortedRanks = new ArrayList<>(this.ranks.values());
        sortedRanks.sort(Comparator.comparingInt(Rank::getWeight));
        Collections.reverse(sortedRanks);
        return sortedRanks;
    }

    public List<Rank> getRanksSortedPriority() {
        List<Rank> sortedRanks = new ArrayList<>(this.ranks.values());
        sortedRanks.sort(Comparator.comparingInt(Rank::getQueuePriority));
        Collections.reverse(sortedRanks);
        return sortedRanks;
    }

    public void cacheRank(Rank rank) {
        this.ranks.put(rank.getUuid(), rank);
    }

}
