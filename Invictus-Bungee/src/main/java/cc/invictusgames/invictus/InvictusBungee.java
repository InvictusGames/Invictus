package cc.invictusgames.invictus;

import cc.invictusgames.ilib.configuration.ConfigurationService;
import cc.invictusgames.ilib.messages.MessageService;
import cc.invictusgames.ilib.messages.YamlMessageService;
import cc.invictusgames.ilib.utils.logging.BungeeLogFactory;
import cc.invictusgames.invictus.command.HubCommand;
import cc.invictusgames.invictus.command.ListCommand;
import cc.invictusgames.invictus.command.MOTDCommand;
import cc.invictusgames.invictus.command.MaintenanceCommand;
import cc.invictusgames.invictus.config.MainConfig;
import cc.invictusgames.invictus.listener.HubListener;
import cc.invictusgames.invictus.listener.LoginListener;
import cc.invictusgames.invictus.listener.MotdListener;
import cc.invictusgames.invictus.motd.MotdConfig;
import cc.invictusgames.invictus.permission.PermissionService;
import cc.invictusgames.invictus.profile.Profile;
import cc.invictusgames.invictus.rank.Rank;
import cc.invictusgames.invictus.server.ServerInfo;
import cc.invictusgames.invictus.server.ServerState;
import cc.invictusgames.invictus.server.packet.UpdateServerPacket;
import cc.invictusgames.invictus.utils.Tasks;
import lombok.Getter;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 13.12.2020 / 03:04
 * Invictus / cc.invictusgames.invictus.spigot
 */

@Getter
public class InvictusBungee extends Invictus {

    @Getter
    private static InvictusBungee bungeeInstance;

    private MessageService messageService;
    private ServerInfo serverInfo;
    private PermissionService permissionService;
    private MotdConfig motdConfig;

    public InvictusBungee(InvictusBungeePlugin plugin) {
        super(SystemType.BUNGEE,
                plugin.getLogger(),
                new BungeeLogFactory(plugin),
                plugin.getMainConfig());
    }

    @Override
    public void initialize() {
        InvictusBungee.bungeeInstance = this;

        this.serverInfo = new ServerInfo(getServerName());
        startServerMonitor();

        this.permissionService = new PermissionService(this);
        this.motdConfig = InvictusBungeePlugin.getInstance().getConfigurationService().loadConfiguration(MotdConfig.class,
                new File(InvictusBungeePlugin.getInstance().getDataFolder(), "motd.json"));

        InvictusBungeePlugin.getInstance().getProxy().setReconnectHandler(new InvictusReconnectHandler(this));

        Stream.of(
                new HubCommand(),
                new ListCommand(),
                new MOTDCommand(motdConfig),
                new MaintenanceCommand(this)
        ).forEach(command -> ProxyServer.getInstance().getPluginManager()
                .registerCommand(InvictusBungeePlugin.getInstance(), command));

        Stream.of(
                new LoginListener(this),
                new MotdListener(this),
                new HubListener(this)
        ).forEach(listener -> ProxyServer.getInstance().getPluginManager()
                .registerListener(InvictusBungeePlugin.getInstance(), listener));
    }

    @Override
    public void loadFiles() {
        this.messageService = new YamlMessageService("invictus", InvictusBungeePlugin.getInstance());
        messageService.loadMessages();
    }

    public void startServerMonitor() {
        Tasks.runTimerAsync(() -> {
            ServerInfo.getServers().stream()
                    .filter(server -> System.currentTimeMillis() - server.getLastHeartbeat() > ServerInfo.MAX_TIMEOUT
                            && server.getState() != ServerState.HEARTBEAT_TIMEOUT
                            && server.getState() != ServerState.OFFLINE)
                    .forEach(server -> server.setState(ServerState.HEARTBEAT_TIMEOUT));

            serverInfo.setLastHeartbeat(System.currentTimeMillis());
            serverInfo.setGrantScope(getServerGroup());
            serverInfo.setState(ServerState.ONLINE);
            serverInfo.setOnlinePlayers(ProxyServer.getInstance().getOnlineCount());
            serverInfo.setMaxPlayers(ProxyServer.getInstance().getConfigurationAdapter().getListeners().stream()
                    .mapToInt(ListenerInfo::getMaxPlayers).sum());
            serverInfo.setTps(0D);
            serverInfo.setFullTick(0D);
            serverInfo.setUsedMemory((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 2L / 1048576L);
            serverInfo.setAllocatedMemory(Runtime.getRuntime().totalMemory() / 1048576L);

            getRedisService().publish(new UpdateServerPacket(serverInfo));
        }, 1L, 1L);
    }

    @Override
    public void saveMainConfig() {
        try {
            getConfigurationService().saveConfiguration(this.getMainConfig(),
                    new File(InvictusBungeePlugin.getInstance().getDataFolder(), "config.json"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void dispatchConsoleCommand(String command) {
        ProxyServer.getInstance().getPluginManager().dispatchCommand(ProxyServer.getInstance().getConsole(), command);
    }

    @Override
    public void updatePermissions(UUID uuid) {
        ProxiedPlayer proxiedPlayer = InvictusBungeePlugin.getInstance().getProxy().getPlayer(uuid);
        if (proxiedPlayer == null)
            return;
        getPermissionService().updatePermissions(proxiedPlayer);
    }

    @Override
    public void updatePermissionsWithRank(Rank rank) {
        for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
            Profile profile = getProfileService().getProfile(player.getUniqueId());
            if (profile.hasGrantOf(rank))
                getPermissionService().updatePermissions(player);
        }
    }

    @Override
    public List<String> getLocalPermissions(Rank rank) {
        return new ArrayList<>();
    }

    @Override
    public void saveLocalPermissions(Rank rank) {
    }

    @Override
    public void handleRankDeletion(Rank rank) {
    }

    @Override
    public void handleServerInfoUpdate(String name, ServerInfo serverInfo) {

    }

    @Override
    public String getServerName() {
        return getMainConfig().getServerName();
    }

    @Override
    public String getServerGroup() {
        return getMainConfig().getServerGroup();
    }

    public ConfigurationService getConfigurationService() {
        return InvictusBungeePlugin.getInstance().getConfigurationService();
    }

    @Override
    public MainConfig getMainConfig() {
        return InvictusBungeePlugin.getInstance().getMainConfig();
    }
}
