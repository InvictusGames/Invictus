package cc.invictusgames.invictus.vote.handler.impl;

import cc.invictusgames.invictus.connection.RequestHandler;
import cc.invictusgames.invictus.connection.RequestResponse;
import cc.invictusgames.invictus.vote.handler.VoteHandler;

import java.util.UUID;

public class DiscordSyncHandler implements VoteHandler {

    @Override
    public String getServiceName() {
        return "DiscordSync";
    }

    @Override
    public String getFancyName() {
        return "Discord Sync (/sync)";
    }

    @Override
    public int getPriority() {
        return 4;
    }

    @Override
    public boolean hasVoted(UUID uuid) {
        RequestResponse response = RequestHandler.get("discord/issynced/%s", uuid.toString());

        return response.wasSuccessful()
                && response.asObject().has("synced")
                && response.asObject().get("synced").getAsBoolean();
    }

    @Override
    public void setVotedAt(UUID uuid, long timestamp) { }
}
