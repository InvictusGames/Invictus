package cc.invictusgames.invictus.queue;

import cc.invictusgames.ilib.utils.UUIDUtils;
import cc.invictusgames.invictus.Invictus;
import cc.invictusgames.invictus.profile.Profile;
import cc.invictusgames.invictus.queue.packet.QueueSendPlayerPacket;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 25.10.2020 / 21:30
 * Invictus / cc.invictusgames.invictus.spigot.queue
 */

@RequiredArgsConstructor
public class Queue {

    public static final String WEIGHT_FORMAT = "queue:%s:weight";
    public static final String PLAYERS_FORMAT = "queue:%s:players";
    public static final String POSITION_FORMAT = "queue-data:%s:position";

    @Getter
    private final LinkedList<UUID> players = new LinkedList<>();
    private final ConcurrentHashMap<UUID, Integer> weight = new ConcurrentHashMap<>();
    private final Invictus invictus;

    public int getJoinPosition(Profile profile) {
        if (players.isEmpty())
            return -1;

        int playerWeight = profile.getQueuePriority(invictus.getServerGroup());
        if (playerWeight == 0)
            return -1;

        for (int i = 0; i < players.size(); i++) {
            Integer otherWeight = weight.get(players.get(i));
            if (playerWeight >= otherWeight)
                return playerWeight > otherWeight ? i : i + 1;
        }

        return -1;
    }

    public void addPlayer(Profile profile) {
        if (players.contains(profile.getUuid()))
            return;
        int position = getJoinPosition(profile);
        if (position == -1)
            players.addLast(profile.getUuid());
        else players.add(position, profile.getUuid());

        weight.put(profile.getUuid(), profile.getRealCurrentGrantOn(invictus.getServerGroup()).getRank().getWeight()
                + (profile.hasPrimeStatus() ? 1 : 0));
        updatePositions();
    }

    public void removePlayer(Profile profile) {
        if (!players.contains(profile.getUuid()))
            return;

        players.remove(profile.getUuid());
        weight.remove(profile.getUuid());
        updatePositions();
        deleteQueueData(profile.getUuid());
    }

    public void save() {
        invictus.getRedisService().executeBackendCommand(redis -> {
            List<String> strings = new ArrayList<>();
            for (UUID player : players) {
                strings.add(player.toString());
            }

            redis.set(String.format(PLAYERS_FORMAT, invictus.getServerName()), StringUtils.join(strings, ";"));
            strings.clear();
            weight.forEach((uuid, value) -> redis.hset(
                    String.format(WEIGHT_FORMAT, invictus.getServerName()), uuid.toString(), String.valueOf(value))
            );
            return null;
        });
    }

    public void load() {
        invictus.getRedisService().executeBackendCommand(redis -> {
            players.clear();
            if (redis.exists(String.format(PLAYERS_FORMAT, invictus.getServerName()))) {
                players.addAll(Arrays.stream(redis.get(String.format(PLAYERS_FORMAT, invictus.getServerName())).split(";"))
                        .filter(UUIDUtils::isUUID)
                        .map(UUID::fromString)
                        .collect(Collectors.toList()));
            }

            weight.clear();
            redis.hgetAll(String.format(WEIGHT_FORMAT, invictus.getServerName()))
                    .forEach((uuid, value) -> weight.put(UUID.fromString(uuid), Integer.valueOf(value)));
            //players.addAll(weight.keySet());
            //players.sort(Comparator.comparingInt(weight::get));
            return null;
        });
    }

    private void updatePositions() {
        invictus.getRedisService().executeBackendCommand(redis -> {
            for (UUID uuid : players) {
                if (uuid == null)
                    continue;

                redis.hset(String.format(POSITION_FORMAT, uuid.toString()), invictus.getServerName(),
                        String.valueOf(players.indexOf(uuid)));
            }


            List<String> strings = new ArrayList<>();
            for (UUID player : players) {
                strings.add(player.toString());
            }

            redis.set(String.format(PLAYERS_FORMAT, invictus.getServerName()), StringUtils.join(strings, ";"));
            strings.clear();
            return null;
        });
        save();
    }

    private void deleteQueueData(UUID uuid) {
        invictus.getRedisService().executeBackendCommand(redis -> {
            redis.hdel(String.format(POSITION_FORMAT, uuid.toString()), invictus.getServerName());
            return null;
        });
    }

    public void sendNext() {
        if (players.isEmpty())
            return;

        if (Bukkit.getOnlinePlayers().size() >= Bukkit.getMaxPlayers())
            return;

        UUID next = players.removeFirst();
        if (next == null)
            return;

        invictus.getRedisService().publish(new QueueSendPlayerPacket(invictus.getServerName(), next));
        updatePositions();
        deleteQueueData(next);
    }

}
