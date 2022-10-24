package cc.invictusgames.invictus.vote.handler;

import java.util.UUID;

public interface VoteHandler {

    String getServiceName();

    String getFancyName();

    int getPriority();

    boolean hasVoted(UUID uuid);

    void setVotedAt(UUID uuid, long timestamp);

}
