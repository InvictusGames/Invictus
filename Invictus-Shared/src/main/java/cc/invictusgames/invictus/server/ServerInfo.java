package cc.invictusgames.invictus.server;

import cc.invictusgames.invictus.Invictus;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 19.02.2020 / 19:51
 * Invictus / cc.invictusgames.invictus.spigot.server
 */

@Data
public class ServerInfo {

    private static final Map<String, ServerInfo> servers = new HashMap<>();
    public static final long MAX_TIMEOUT = 5000L;
    private static final Invictus invictus = Invictus.getInstance();

    private String name = "";
    private String grantScope = "";
    private long lastHeartbeat = System.currentTimeMillis();
    private ServerState state = ServerState.UNKNOWN;
    private int onlinePlayers = 0;
    private int maxPlayers = 0;
    private double tps = 0D;
    private double fullTick = 0D;
    private long usedMemory = 0L;
    private long allocatedMemory = 0L;
    private boolean queueEnabled = false;
    private boolean queuePaused = false;
    private int queueRate = 0;
    private int playersInQueue = 0;

    public ServerInfo(String name) {
        this.name = name;
        servers.put(name.toLowerCase(), this);
    }

    public boolean isOnline() {
        switch (state) {
            case ONLINE:
            case WHITELISTED:
                return true;
            default:
                return false;
        }
    }

    public boolean isProxy() {
        return grantScope.equals("proxy");
    }

    public int getOnlinePlayers() {
        return isOnline() ? onlinePlayers : 0;
    }

    public static ServerInfo getServerInfo(String name) {
        return servers.getOrDefault(name.toLowerCase(), null);
    }

    public static List<ServerInfo> getServers() {
        return new ArrayList<>(servers.values());
    }

    public static List<ServerInfo> getByGroup(String group) {
        return servers.values().stream()
                .filter(serverInfo -> serverInfo.getGrantScope().equalsIgnoreCase(group))
                .collect(Collectors.toList());
    }

    public static void updateServerInfo(String name, ServerInfo serverInfo) {
        invictus.handleServerInfoUpdate(name, serverInfo);
        servers.put(name.toLowerCase(), serverInfo);
    }

    public static int getGlobalPlayerCount() {
        return servers.values().stream()
                .filter(server -> !server.isProxy())
                .mapToInt(ServerInfo::getOnlinePlayers)
                .sum();
    }
}
