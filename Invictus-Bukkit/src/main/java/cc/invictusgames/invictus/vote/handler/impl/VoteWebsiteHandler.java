package cc.invictusgames.invictus.vote.handler.impl;

import cc.invictusgames.invictus.Invictus;
import cc.invictusgames.invictus.vote.handler.VoteHandler;
import lombok.RequiredArgsConstructor;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class VoteWebsiteHandler implements VoteHandler {

    private final Invictus invictus;
    private final String serviceName;
    private final String fancyName;
    private final int priority;

    @Override
    public String getServiceName() {
        return serviceName;
    }

    @Override
    public String getFancyName() {
        return fancyName;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public boolean hasVoted(UUID uuid) {
        if (!invictus.getRedisService().executeBackendCommand(redis ->
                redis.hexists("votes:" + uuid.toString(), serviceName.toLowerCase())))
            return false;

        long votedAt = Long.parseLong(invictus.getRedisService().executeBackendCommand(redis ->
                redis.hget("votes:" + uuid.toString(), serviceName.toLowerCase())));
        return votedAt + TimeUnit.DAYS.toMillis(1L) > System.currentTimeMillis();
    }

    @Override
    public void setVotedAt(UUID uuid, long timestamp) {
        invictus.getRedisService().executeBackendCommand(redis ->
                redis.hset("votes:" + uuid.toString(), serviceName.toLowerCase(), String.valueOf(timestamp)));
    }
}
