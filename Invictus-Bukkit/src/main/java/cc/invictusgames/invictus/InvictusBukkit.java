package cc.invictusgames.invictus;

import cc.invictusgames.ilib.ILibBukkit;
import cc.invictusgames.ilib.chat.ChatService;
import cc.invictusgames.ilib.command.CommandService;
import cc.invictusgames.ilib.configuration.ConfigurationService;
import cc.invictusgames.ilib.messages.MessageService;
import cc.invictusgames.ilib.messages.YamlMessageService;
import cc.invictusgames.ilib.placeholder.PlaceholderService;
import cc.invictusgames.ilib.playersetting.PlayerSettingService;
import cc.invictusgames.ilib.protocol.ProtocolService;
import cc.invictusgames.ilib.scoreboard.ScoreboardService;
import cc.invictusgames.ilib.tab.TabService;
import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.ilib.utils.TimeUtils;
import cc.invictusgames.ilib.utils.logging.BukkitLogFactory;
import cc.invictusgames.ilib.visibility.VisibilityService;
import cc.invictusgames.invictus.antivpn.command.AntiVPNCommands;
import cc.invictusgames.invictus.banphrase.BanphraseService;
import cc.invictusgames.invictus.banphrase.commands.BanphraseCommands;
import cc.invictusgames.invictus.base.InvictusCommands;
import cc.invictusgames.invictus.base.StaffMode;
import cc.invictusgames.invictus.base.commands.*;
import cc.invictusgames.invictus.base.redisconvertion.RedisConvertCommands;
import cc.invictusgames.invictus.bossbar.BossBarProvider;
import cc.invictusgames.invictus.chat.impl.PublicChat;
import cc.invictusgames.invictus.chat.impl.StaffChat;
import cc.invictusgames.invictus.config.LocalPermissionConfig;
import cc.invictusgames.invictus.config.MainConfig;
import cc.invictusgames.invictus.config.entry.LocalPermissionEntry;
import cc.invictusgames.invictus.connection.command.ApiCommands;
import cc.invictusgames.invictus.discord.commands.SyncCommands;
import cc.invictusgames.invictus.disguise.BukkitDisguiseService;
import cc.invictusgames.invictus.disguise.commands.DisguiseCommands;
import cc.invictusgames.invictus.forum.commands.RegisterCommand;
import cc.invictusgames.invictus.grant.commands.GrantCommands;
import cc.invictusgames.invictus.listener.*;
import cc.invictusgames.invictus.note.command.NoteCommands;
import cc.invictusgames.invictus.permission.PermissionService;
import cc.invictusgames.invictus.permission.adapter.OwnerOnlyPermission;
import cc.invictusgames.invictus.playersetting.InvictusSettings;
import cc.invictusgames.invictus.prime.PrimeRewardProvider;
import cc.invictusgames.invictus.prime.command.PrimeCommand;
import cc.invictusgames.invictus.profile.BukkitProfileService;
import cc.invictusgames.invictus.profile.Profile;
import cc.invictusgames.invictus.profile.UnloadedProfile;
import cc.invictusgames.invictus.profile.commands.ProfileCommands;
import cc.invictusgames.invictus.profile.parameter.ProfileParameter;
import cc.invictusgames.invictus.profile.parameter.UnloadedProfileParameter;
import cc.invictusgames.invictus.provider.placeholder.ProfilePlaceholderAdapter;
import cc.invictusgames.invictus.provider.placeholder.RankPlaceholderAdapter;
import cc.invictusgames.invictus.punishment.BukkitPunishmentService;
import cc.invictusgames.invictus.punishment.commands.*;
import cc.invictusgames.invictus.queue.Queue;
import cc.invictusgames.invictus.queue.QueueService;
import cc.invictusgames.invictus.queue.commands.QueueCommands;
import cc.invictusgames.invictus.rank.Rank;
import cc.invictusgames.invictus.rank.commands.RankCommands;
import cc.invictusgames.invictus.rank.parameter.RankParameter;
import cc.invictusgames.invictus.server.ServerInfo;
import cc.invictusgames.invictus.server.ServerState;
import cc.invictusgames.invictus.server.command.ServerMonitorCommands;
import cc.invictusgames.invictus.server.packet.ServerStateChangePacket;
import cc.invictusgames.invictus.server.packet.UpdateServerPacket;
import cc.invictusgames.invictus.server.parameter.ServerInfoParameter;
import cc.invictusgames.invictus.tag.Tag;
import cc.invictusgames.invictus.tag.command.TagCommands;
import cc.invictusgames.invictus.tag.command.parameter.TagParameter;
import cc.invictusgames.invictus.tip.TipService;
import cc.invictusgames.invictus.totp.TotpService;
import cc.invictusgames.invictus.totp.commands.TotpCommands;
import cc.invictusgames.invictus.provider.RankNameTagAdapter;
import cc.invictusgames.invictus.provider.StaffModeNameTagAdapter;
import cc.invictusgames.invictus.provider.StaffModeVisibilityAdapter;
import cc.invictusgames.invictus.utils.NetworkBroadcastPacket;
import cc.invictusgames.invictus.utils.Tasks;
import cc.invictusgames.invictus.vote.VoteService;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 12.12.2020 / 16:43
 * Invictus / cc.invictusgames.invictus.spigot
 */

@Getter
public class InvictusBukkit extends Invictus {

    @Getter
    private static InvictusBukkit bukkitInstance;

    @Getter
    @Setter
    private static PrimeRewardProvider primeRewardProvider = null;

    private LocalPermissionConfig localPermissionConfig;
    private MessageService messageService;
    private BukkitDisguiseService bukkitDisguiseService;
    private PermissionService permissionService;
    private TotpService totpService;
    private QueueService queueService;
    private BukkitPunishmentService bukkitPunishmentService;
    private BukkitProfileService bukkitProfileService;
    private BanphraseService banphraseService;
    private TipService tipService;
    private VoteService voteService;

    private final List<UUID> confirmedSwitch = new ArrayList<>();

    private ServerInfo serverInfo;
    private Queue queue;

    public InvictusBukkit(InvictusBukkitPlugin plugin) {
        super(SystemType.BUKKIT,
                plugin.getLogger(),
                new BukkitLogFactory(plugin),
                plugin.getMainConfig());
    }

    @Override
    public void initialize() {
        InvictusBukkit.bukkitInstance = this;

        InvictusBukkitPlugin.getInstance().getServer().getMessenger()
                .registerOutgoingPluginChannel(InvictusBukkitPlugin.getInstance(), "BungeeCord");

        init();
        registerCommands();
        registerListener(Bukkit.getPluginManager());
        startServerMonitor();
    }


    @Override
    public void loadFiles() {
        this.localPermissionConfig = getConfigurationService().loadConfiguration(LocalPermissionConfig.class,
                new File(InvictusBukkitPlugin.getInstance().getDataFolder(), "permissions.json"));
        this.messageService = new YamlMessageService("invictus", InvictusBukkitPlugin.getInstance());
    }

    public void init() {
        this.serverInfo = new ServerInfo(getServerName());
        this.queue = new Queue(this);
        queue.load();

        this.bukkitDisguiseService = new BukkitDisguiseService(this);
        bukkitDisguiseService.loadPresets();

        this.permissionService = new PermissionService(this);
        permissionService.injectFakeSubscriptionMap();

        this.totpService = new TotpService(this);

        this.queueService = new QueueService(this);
        queueService.startTask();

        getRedisService().publish(new ServerStateChangePacket(serverInfo.getName(), ServerState.ONLINE));

        this.bukkitPunishmentService = new BukkitPunishmentService(this);
        bukkitPunishmentService.loadTemplates();

        this.bukkitProfileService = new BukkitProfileService(this);

        this.banphraseService = new BanphraseService();
        banphraseService.loadBanphrases(() -> {});

        this.tipService = new TipService(this);
        tipService.loadTips();
        tipService.startTask();

        this.voteService = new VoteService(this);

        PlaceholderService.registerAdapter(new ProfilePlaceholderAdapter(this));
        PlaceholderService.registerAdapter(new RankPlaceholderAdapter(this));

        ScoreboardService.registerNameTagAdapter(new RankNameTagAdapter(this));
        ScoreboardService.registerNameTagAdapter(new StaffModeNameTagAdapter(this));

        TabService.setPlayerNameGetter(player -> {
            Profile profile = getProfileService().getProfile(player);
            return profile.isDisguised() ? profile.getDisguiseName() : player.getName();
        });

        VisibilityService.registerVisibilityAdapter(new StaffModeVisibilityAdapter(this));
        VisibilityService.setOnlineTreatProvider((player, sender) -> {
            if (!(sender instanceof Player))
                return true;

            StaffMode staffMode = StaffMode.get(player);
            if (!staffMode.isVanished())
                return true;

            return sender.hasPermission("invictus.staff") || ((Player) sender).canSee(player);
        });

        PlayerSettingService.registerProvider(new InvictusSettings());

        ILibBukkit.setServerNameGetter(unused -> getServerName());

        ChatService.setDefaultChannel(new PublicChat());
        ChatService.registerChatChannel(ChatService.getDefaultChannel());
        ChatService.registerChatChannel(StaffChat.getInstance());
        ChatService.setPrefixGetter((player, sender) -> {
            Profile profile = getProfileService().getProfile(player);
            String primeIcon = CC.GRAY + "[" + InvictusSettings.PRIME_COLOR.get(player)
                    + Invictus.PRIME_ICON + CC.GRAY + "]";
            String tag = profile.getActiveTag() == null ? "" : " " + profile.getActiveTag().getDisplayName();
            return (profile.hasPrimeStatus() ? primeIcon : "")
                    + profile.getCurrentGrant().getRank().getPrefix()
                    + player.getName()
                    + profile.getCurrentGrant().getRank().getSuffix()
                    + tag;
        });

//        new BossBarProvider(this);

        /*if (Bukkit.getPluginManager().isPluginEnabled("KarhuAPI")) {
            new AntiCheatHook(this);
        }*/
    }

    public void registerCommands() {

        // Citizens has a script command that registers a /sc alias, this messes with our staff
        // chat command so we unregister it
        try {
            Field commandMapField = Bukkit.getPluginManager().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            SimpleCommandMap commandMap = (SimpleCommandMap) commandMapField.get(Bukkit.getPluginManager());

            Field knownCommandsField = commandMap.getClass().getDeclaredField("knownCommands");
            knownCommandsField.setAccessible(true);
            Map<String, Command> knownCommands = (Map<String, Command>) knownCommandsField.get(commandMap);

            Command command = commandMap.getCommand("script");
            if (command != null) {
                knownCommands.remove(command.getName());
                command.getAliases().forEach(knownCommands::remove);
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }


        CommandService.registerParameter(Profile.class, new ProfileParameter(this));
        CommandService.registerParameter(UnloadedProfile.class, new UnloadedProfileParameter());
        CommandService.registerParameter(Rank.class, new RankParameter());
        CommandService.registerParameter(Tag.class, new TagParameter());
        CommandService.registerParameter(ServerInfo.class, new ServerInfoParameter());
        CommandService.registerPermissionAdapter(new OwnerOnlyPermission(this));
        CommandService.register(
                InvictusBukkitPlugin.getInstance(),
                new GamemodeCommands(this),
                new InventoryCommands(this),
                new ItemCommands(this),
                new TeleportCommands(this),
                new BaseCommands(this),
                new MessageCommands(this),
                new ProfileCommands(this),
                new HistoryCommand(this),
                new AltsCommand(this),
                new BanCommands(this),
                new MuteCommands(this),
                new BlacklistCommands(this),
                new KickCommand(this),
                new WarnCommand(this),
                new GrantCommands(this),
                RankCommands.INSTANCE,
                new DisguiseCommands(this),
                new StaffCommands(this),
                new StaffModeCommands(this),
                new NoteCommands(this),
                new ServerMonitorCommands(this),
                new ReportCommands(this),
                new ChatCommands(this),
                new FreezeCommands(this),
                new TagCommands(this),
                new TotpCommands(this),
                new PrimeCommand(this),
                new AntiVPNCommands(this),
                new SyncCommands(this),
                new ClearPunishmentsCommand(this),
                new ApiCommands(this),
                new QueueCommands(this),
                new StaffRollbackCommand(this),
                new BanphraseCommands(this),
                new InvictusCommands(this),
                new OpCommands(),
                new RedisConvertCommands(this),
                new PunishCommand(),
                new RegisterCommand()
        );
    }

    public void registerListener(PluginManager pluginManager) {
        Arrays.asList(
                new PlayerPreLoginListener(this),
                new AsyncPlayerChatListener(this),
                new PlayerQuitListener(this),
                new PlayerJoinListener(this),
                new PlayerLoginListener(this),
                new PlayerInteractListener(this),
                new InventoryClickListener(),
                new StaffModeListener(this),
                new FreezeListener(this)
        ).forEach(listener -> pluginManager.registerEvents(listener, InvictusBukkitPlugin.getInstance()));

        Arrays.asList(
                new WorldEditCrashFixListener(this)
                //new DisguiseListener(this)
        ).forEach(listener -> ProtocolService.registerTinyProtocol(InvictusBukkitPlugin.getInstance(), listener));
    }

    public void startServerMonitor() {
        Tasks.runTimerAsync(() -> {
            ServerInfo.getServers().stream()
                    .filter(server -> System.currentTimeMillis() - server.getLastHeartbeat() > ServerInfo.MAX_TIMEOUT
                            && server.getState() != ServerState.HEARTBEAT_TIMEOUT
                            && server.getState() != ServerState.OFFLINE)
                    .forEach(server -> {
                        server.setState(ServerState.HEARTBEAT_TIMEOUT);
                        getRedisService().publish(new NetworkBroadcastPacket(
                                CC.format(
                                        "&8[&cServer Monitor&8] &fStatus of &e%s&f changed to %s&f.",
                                        server.getName(),
                                        server.getState().getInternalName()
                                ),
                                "invictus.admin",
                                true,
                                getMainConfig().getServerName()
                        ));
                    });

            serverInfo.setLastHeartbeat(System.currentTimeMillis());
            serverInfo.setGrantScope(getServerGroup());
            serverInfo.setState(Bukkit.hasWhitelist() ? ServerState.WHITELISTED : ServerState.ONLINE);
            serverInfo.setOnlinePlayers(Bukkit.getOnlinePlayers().size());
            serverInfo.setMaxPlayers(Bukkit.getMaxPlayers());
            serverInfo.setTps(MinecraftServer.getServer().recentTps[0]);
            serverInfo.setFullTick(MinecraftServer.getServer().lastTickTime);
            serverInfo.setUsedMemory((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 2L / 1048576L);
            serverInfo.setAllocatedMemory(Runtime.getRuntime().totalMemory() / 1048576L);
            serverInfo.setQueueEnabled(getMainConfig().isQueueEnabled());
            serverInfo.setQueuePaused(getMainConfig().isQueuePaused());
            serverInfo.setQueueRate(getMainConfig().getQueueRate());
            serverInfo.setPlayersInQueue(queue.getPlayers().size());

            if (ServerMonitorCommands.SEND_PACKET) {
                getRedisService().publish(new UpdateServerPacket(serverInfo));
            }
        }, 20L, 20L);
    }

    @Override
    public void saveMainConfig() {
        try {
            getConfigurationService().saveConfiguration(this.getMainConfig(),
                    new File(InvictusBukkitPlugin.getInstance().getDataFolder(), "config.json"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveLocalPermissionConfig() {
        try {
            getConfigurationService().saveConfiguration(this.localPermissionConfig,
                    new File(InvictusBukkitPlugin.getInstance().getDataFolder(), "permissions.json"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void dispatchConsoleCommand(String command) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
    }

    @Override
    public void updatePermissions(UUID uuid) {
        if (Bukkit.getPlayer(uuid) != null)
            permissionService.updatePermissions(Bukkit.getPlayer(uuid));
    }

    @Override
    public void updatePermissionsWithRank(Rank rank) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Profile profile = getProfileService().getProfile(player);
            if (profile.hasGrantOf(rank))
                permissionService.updatePermissions(player);
        }
    }

    @Override
    public List<String> getLocalPermissions(Rank rank) {
        LocalPermissionEntry entry = localPermissionConfig.getEntry(rank);
        if (entry != null) {
            return entry.getPermissions();
        }

        entry = new LocalPermissionEntry();
        entry.setUuid(rank.getUuid().toString());
        localPermissionConfig.getRankPermissions().add(entry);
        saveLocalPermissionConfig();
        return new ArrayList<>();
    }

    @Override
    public void saveLocalPermissions(Rank rank) {
        LocalPermissionEntry entry = localPermissionConfig.getEntry(rank);
        if (entry != null) {
            entry.setPermissions(new ArrayList<>(rank.getLocalPermissions()));
        } else {
            entry = new LocalPermissionEntry();
            entry.setUuid(rank.getUuid().toString());
            entry.setPermissions(new ArrayList<>(rank.getLocalPermissions()));
            localPermissionConfig.getRankPermissions().add(entry);
        }
        saveLocalPermissionConfig();
    }

    @Override
    public void handleRankDeletion(Rank rank) {
        LocalPermissionEntry entry = localPermissionConfig.getEntry(rank);
        if (entry != null) {
            localPermissionConfig.getRankPermissions().remove(entry);
            saveLocalPermissionConfig();
        }
    }

    @Override
    public void handleServerInfoUpdate(String name, ServerInfo serverInfo) {
        ServerInfo old = ServerInfo.getServerInfo(name.toLowerCase());
        if (old == null)
            return;

        if (old.getState() == ServerState.HEARTBEAT_TIMEOUT) {
            getRedisService().publish(new NetworkBroadcastPacket(
                    CC.format(
                            "&8[&cServer Monitor&8] &fStatus of &e%s&f changed to %s &7(%s downtime)&f.",
                            serverInfo.getName(),
                            serverInfo.getState().getInternalName(),
                            TimeUtils.formatHHMMSS(
                                    serverInfo.getLastHeartbeat() - old.getLastHeartbeat(),
                                    true,
                                    TimeUnit.MILLISECONDS
                            )
                    ),
                    "invictus.admin",
                    true,
                    getMainConfig().getServerName()
            ));
        }
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
        return InvictusBukkitPlugin.getInstance().getConfigurationService();
    }

    public MainConfig getMainConfig() {
        return InvictusBukkitPlugin.getInstance().getMainConfig();
    }

}