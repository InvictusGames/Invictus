package cc.invictusgames.invictus;

import cc.invictusgames.ilib.configuration.ConfigurationService;
import cc.invictusgames.ilib.configuration.JsonConfigurationService;
import cc.invictusgames.invictus.config.MainConfig;
import cc.invictusgames.invictus.connection.RequestHandler;
import cc.invictusgames.invictus.utils.Tasks;
import cc.invictusgames.invictus.utils.task.BungeeTaskImplementor;
import lombok.Getter;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.File;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 13.12.2020 / 03:04
 * Invictus / cc.invictusgames.invictus.spigot
 */

@Getter
public class InvictusBungeePlugin extends Plugin {

    @Getter
    private static InvictusBungeePlugin instance;

    private ConfigurationService configurationService;
    private MainConfig mainConfig;

    @Override
    public void onEnable() {
        InvictusBungeePlugin.instance = this;
        Tasks.setTaskImplementor(new BungeeTaskImplementor(this));
        this.configurationService = new JsonConfigurationService();
        this.mainConfig = configurationService.loadConfiguration(MainConfig.class, new File(getDataFolder(), "config" +
                ".json"));
        new InvictusBungee(this);
    }

    @Override
    public void onDisable() {
        RequestHandler.sendBackLog();
    }
}
