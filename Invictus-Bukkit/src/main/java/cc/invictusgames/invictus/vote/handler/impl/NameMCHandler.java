package cc.invictusgames.invictus.vote.handler.impl;

import cc.invictusgames.invictus.vote.handler.VoteHandler;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

public class NameMCHandler implements VoteHandler {

    private static final String NAMEMC_LINK = "https://api.namemc.com/server/%s/likes?profile=%s";

    @Override
    public String getServiceName() {
        return "NameMC";
    }

    @Override
    public String getFancyName() {
        return "https://bit.ly/3jWmyAJ";
    }

    @Override
    public int getPriority() {
        return 3;
    }

    @Override
    public boolean hasVoted(UUID uuid) {
        return getResponse(String.format(NAMEMC_LINK, "brave.rip", uuid.toString())).equals("true");
    }

    @Override
    public void setVotedAt(UUID uuid, long timestamp) { }

    private String getResponse(String urlString) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(urlString).openConnection();
            connection.setReadTimeout(5000);
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                StringBuilder response = new StringBuilder();
                new BufferedReader(new InputStreamReader(connection.getInputStream())).lines().forEach(response::append);
                return response.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
