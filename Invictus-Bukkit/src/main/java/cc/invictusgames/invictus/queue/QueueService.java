package cc.invictusgames.invictus.queue;

import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.ilib.utils.UUIDUtils;
import cc.invictusgames.invictus.InvictusBukkit;
import cc.invictusgames.invictus.queue.packet.QueueLeavePacket;
import cc.invictusgames.invictus.utils.PlayerMessagePacket;
import cc.invictusgames.invictus.utils.Tasks;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 27.10.2020 / 22:18
 * Invictus / cc.invictusgames.invictus.spigot.queue
 */

@RequiredArgsConstructor
public class QueueService {

    private final InvictusBukkit invictus;

    public List<String> getQueues(UUID uuid) {
        return new ArrayList<>(invictus.getRedisService().executeBackendCommand(redis ->
                redis.hgetAll(String.format(Queue.POSITION_FORMAT, uuid.toString())).keySet()));
    }

    public boolean isQueueingFor(UUID uuid, String server) {
        return getQueues(uuid).contains(server);
    }

    public int getPosition(UUID uuid, String server) {
        return Integer.parseInt(invictus.getRedisService().executeBackendCommand(redis ->
                redis.hget(String.format(Queue.POSITION_FORMAT, uuid.toString()), server)));
    }

    public String getPrimaryQueue(UUID uuid) {
        List<String> queues = getQueues(uuid);
        return queues.isEmpty() ? null : queues.get(0);
    }

    public List<UUID> getQueueing(String server) {
        return invictus.getRedisService().executeBackendCommand(redis -> {
            if (!redis.exists(String.format(Queue.PLAYERS_FORMAT, server)))
                return new ArrayList<>();

            if (redis.get(String.format(Queue.PLAYERS_FORMAT, server)).isEmpty())
                return new ArrayList<>();

            List<UUID> queueing = new ArrayList<>();
            for (String s : redis.get(String.format(Queue.PLAYERS_FORMAT, server)).split(";")) {
                if (!UUIDUtils.isUUID(s))
                    continue;

                queueing.add(UUID.fromString(s));
            }

            return queueing;
        });
    }

    public void resetQueueData(UUID uuid) {
        getQueues(uuid).forEach(server -> {
            invictus.getRedisService().publish(new QueueLeavePacket(server, uuid));
            invictus.getRedisService().executeBackendCommand(redis -> {
                redis.del(String.format(Queue.POSITION_FORMAT, uuid.toString()));
                redis.hdel(String.format(Queue.WEIGHT_FORMAT, server), uuid.toString());

                if (!redis.exists(String.format(Queue.PLAYERS_FORMAT, server)))
                    return new ArrayList<>();

                if (redis.get(String.format(Queue.PLAYERS_FORMAT, server)).isEmpty())
                    return new ArrayList<>();

                List<String> inQueue = new ArrayList<>();
                for (String s : redis.get(String.format(Queue.PLAYERS_FORMAT, server)).split(";")) {
                    if (!UUIDUtils.isUUID(s))
                        continue;

                    inQueue.add(s);
                }

                inQueue.remove(uuid.toString());
                redis.set(String.format(Queue.PLAYERS_FORMAT, server), StringUtils.join(inQueue, ";"));
                inQueue.clear();
                return null;
            });
        });
    }

    public void startTask() {
        AtomicLong lastMessage = new AtomicLong(System.currentTimeMillis());
        Tasks.runTimerAsync(() -> {
            if (System.currentTimeMillis() >= lastMessage.get() + TimeUnit.SECONDS.toMillis(15)) {
                AtomicInteger position = new AtomicInteger(0);

                synchronized (invictus.getQueue().getPlayers()) {
                    invictus.getQueue().getPlayers().forEach(player ->
                            invictus.getRedisService().publish(new PlayerMessagePacket(player,
                                    CC.format("&6You are position &f%d &6out of &f%d &6in the &f%s &6queue.",
                                            position.incrementAndGet(), invictus.getQueue().getPlayers().size(),
                                            invictus.getServerName()),
                                    CC.translate("&7&oYou can purchase a rank at &6&o"
                                            + invictus.getMessageService().formatMessage("network-store")
                                            + " &7&oto get a higher priority."))));

                    lastMessage.set(System.currentTimeMillis());
                }
            }

            if (invictus.getMainConfig().isQueuePaused() /*|| Bukkit.hasWhitelist()*/)
                return;

            for (int i = 0; i < invictus.getMainConfig().getQueueRate(); i++)
                invictus.getQueue().sendNext();
        }, 20L, 20L);
    }


}
