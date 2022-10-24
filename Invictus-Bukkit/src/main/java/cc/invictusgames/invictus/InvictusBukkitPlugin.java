package cc.invictusgames.invictus;

import cc.invictusgames.ilib.configuration.ConfigurationService;
import cc.invictusgames.ilib.configuration.JsonConfigurationService;
import cc.invictusgames.invictus.base.StaffMode;
import cc.invictusgames.invictus.config.MainConfig;
import cc.invictusgames.invictus.connection.RequestHandler;
import cc.invictusgames.invictus.listener.PlayerQuitListener;
import cc.invictusgames.invictus.utils.Tasks;
import cc.invictusgames.invictus.utils.task.BukkitTaskImplementor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 12.12.2020 / 16:45
 * Invictus / cc.invictusgames.invictus.spigot
 */

@Getter
public class InvictusBukkitPlugin extends JavaPlugin {

    @Getter
    private static InvictusBukkitPlugin instance;

    private ConfigurationService configurationService;

    @Setter
    private MainConfig mainConfig;

    @Override
    public void onEnable() {
        InvictusBukkitPlugin.instance = this;

        this.configurationService = new JsonConfigurationService();
        this.mainConfig = configurationService.loadConfiguration(MainConfig.class,
                new File(getDataFolder(), "config.json"));
        Tasks.setTaskImplementor(new BukkitTaskImplementor(InvictusBukkitPlugin.getInstance()));

        new InvictusBukkit(this);

        Bukkit.getWorlds().stream()
                .flatMap(world -> world.getEntities().stream())
                .filter(entity -> !(entity instanceof Player))
                .forEach(Entity::remove);
    }

    @Override
    public void onDisable() {
        RequestHandler.sendBackLog();
        InvictusBukkit.getBukkitInstance().getQueue().save();

        PlayerQuitListener.setAsyncKicks(false);

        Bukkit.getOnlinePlayers().stream()
                .map(InvictusBukkit.getBukkitInstance().getProfileService()::getProfile)
                .forEach(profile -> {
                    if (StaffMode.isStaffMode(profile.player()))
                        StaffMode.get(profile.player()).toggleEnabled(true);

                    InvictusBukkit.getBukkitInstance().getQueueService().resetQueueData(profile.getUuid());

                    profile.setLastSeen(System.currentTimeMillis());
                    profile.setLastServer(null);
                    profile.getSession().stopTimings();
                    profile.setJoinTime(-1);
                    profile.save(() -> {}, false);
                });
    }
}
