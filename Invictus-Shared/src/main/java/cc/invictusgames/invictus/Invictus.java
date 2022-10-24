package cc.invictusgames.invictus;

import cc.invictusgames.ilib.redis.RedisService;
import cc.invictusgames.ilib.task.impl.AsynchronousTaskChain;
import cc.invictusgames.ilib.utils.logging.LogFactory;
import cc.invictusgames.invictus.config.IMainConfig;
import cc.invictusgames.invictus.connection.RequestHandler;
import cc.invictusgames.invictus.disguise.DisguiseService;
import cc.invictusgames.invictus.profile.ProfileService;
import cc.invictusgames.invictus.punishment.PunishmentService;
import cc.invictusgames.invictus.rank.Rank;
import cc.invictusgames.invictus.rank.RankService;
import cc.invictusgames.invictus.server.ServerInfo;
import cc.invictusgames.invictus.tag.TagService;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 30.11.2020 / 00:15
 * Invictus / cc.invictusgames.invictus.spigot
 */

public abstract class Invictus {

    public static final AsynchronousTaskChain TASK_CHAIN
            = new AsynchronousTaskChain(1);

    //public static final String PRIME_ICON = "✦";
    public static final String PRIME_ICON = "✪";

    @Getter
    private static Invictus instance;
    @Getter
    private static SystemType systemType;
    @Getter
    private final Logger logger;
    @Getter
    private final LogFactory logFactory;
    @Getter
    @Setter
    private IMainConfig mainConfig;
    @Getter
    private final RedisService redisService;
    @Getter
    private final RankService rankService;
    @Getter
    private final ProfileService profileService;
    @Getter
    private final PunishmentService punishmentService;
    @Getter
    private final DisguiseService disguiseService;
    @Getter
    private final TagService tagService;

    public Invictus(SystemType systemType, Logger logger, LogFactory logFactory, IMainConfig mainConfig) {
        if (instance != null)
            throw new IllegalStateException("Already Initialized");

        Invictus.systemType = systemType;
        Invictus.instance = this;

        this.logger = logger;
        this.logFactory = logFactory;

        this.mainConfig = mainConfig;

        loadFiles();

        this.redisService = new RedisService("invictus", mainConfig.getRedisConfig());
        redisService.subscribe();

        this.rankService = new RankService(this);
        rankService.loadRanks(() -> {});
        this.profileService = new ProfileService(this);
        this.punishmentService = new PunishmentService(this);
        this.disguiseService = new DisguiseService(this);
        this.tagService = new TagService();
        tagService.loadTags();

        RequestHandler.startBackLogTask();

        initialize();
    }

    public abstract void initialize();

    public abstract void loadFiles();

    public abstract void saveMainConfig();

    public abstract void dispatchConsoleCommand(String command);

    public abstract void updatePermissions(UUID uuid);

    public abstract void updatePermissionsWithRank(Rank rank);

    public abstract List<String> getLocalPermissions(Rank rank);

    public abstract void saveLocalPermissions(Rank rank);

    public abstract void handleRankDeletion(Rank rank);

    public abstract void handleServerInfoUpdate(String name, ServerInfo serverInfo);

    public abstract String getServerName();

    public abstract String getServerGroup();

}
