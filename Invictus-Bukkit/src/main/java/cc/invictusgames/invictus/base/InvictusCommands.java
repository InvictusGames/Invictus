package cc.invictusgames.invictus.base;

import cc.invictusgames.ilib.command.CommandService;
import cc.invictusgames.ilib.command.annotation.Command;
import cc.invictusgames.ilib.command.annotation.CommandCooldown;
import cc.invictusgames.ilib.command.annotation.Param;
import cc.invictusgames.ilib.command.parameter.ParameterType;
import cc.invictusgames.ilib.configuration.ConfigurationService;
import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.ilib.utils.Timings;
import cc.invictusgames.invictus.Invictus;
import cc.invictusgames.invictus.InvictusBukkit;
import cc.invictusgames.invictus.InvictusBukkitPlugin;
import cc.invictusgames.invictus.config.MainConfig;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.lang.reflect.Field;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 24.06.2021 / 02:35
 * Invictus / cc.invictusgames.invictus.base
 */

@RequiredArgsConstructor
public class InvictusCommands {

    private final InvictusBukkit invictus;

    @CommandCooldown(time = 5, global = true)
    @Command(names = {"invictus reload config"},
             permission = "op",
             description = "Reload the invictus config")
    public boolean reloadConfig(CommandSender sender) {
        Timings timings = new Timings("invictus-reload-config").startTimings();
        ConfigurationService configurationService = InvictusBukkitPlugin.getInstance().getConfigurationService();
        MainConfig mainConfig = configurationService.loadConfiguration(MainConfig.class,
                new File(InvictusBukkitPlugin.getInstance().getDataFolder(), "config.json"));

        invictus.setMainConfig(mainConfig);
        Invictus.getInstance().setMainConfig(mainConfig);

        sender.sendMessage(CC.format("&eReloaded config in &c%dms&e.",
                timings.stopTimings().calculateDifference()));
        return true;
    }

    @CommandCooldown(time = 5, global = true)
    @Command(names = {"invictus reload messages"},
             permission = "op",
             description = "Reload the messages")
    public boolean reloadMessages(CommandSender sender) {
        Timings timings = new Timings("invictus-reload-messages").startTimings();

        invictus.getMessageService().loadMessages();

        sender.sendMessage(CC.format("&eReloaded messages in &c%dms&e.",
                timings.stopTimings().calculateDifference()));
        return true;
    }

    @CommandCooldown(time = 5, global = true)
    @Command(names = {"invictus reload ranks"},
             permission = "op",
             description = "Reload all ranks")
    public boolean reloadRanks(CommandSender sender) {
        Timings timings = new Timings("invictus-reload-ranks").startTimings();
        invictus.getRankService().loadRanks(() ->
                sender.sendMessage(CC.format("&eReloaded ranks in &c%dms&e.",
                        timings.stopTimings().calculateDifference())));
        return true;
    }

    @CommandCooldown(time = 5, global = true)
    @Command(names = {"invictus reload tags"},
             permission = "op",
             description = "Reload all tags")
    public boolean reloadTags(CommandSender sender) {
        Timings timings = new Timings("invictus-reload-tags").startTimings();
        invictus.getTagService().loadTags();
        sender.sendMessage(CC.format("&eReloaded tags in &c%dms&e.",
                timings.stopTimings().calculateDifference()));
        return true;
    }

    @CommandCooldown(time = 5, global = true)
    @Command(names = {"invictus reload banphrases"},
             permission = "op",
             description = "Reload all banphrases")
    public boolean reloadBanphrases(CommandSender sender) {
        Timings timings = new Timings("invictus-reload-banphrases").startTimings();
        invictus.getBanphraseService().loadBanphrases(() ->
                sender.sendMessage(CC.format("&eReloaded banphrases in &c%dms&e.",
                        timings.stopTimings().calculateDifference())));
        return true;
    }

    @CommandCooldown(time = 5, global = true)
    @Command(names = {"invictus reload tips"},
             permission = "op",
             description = "Reload all tips")
    public boolean reloadTips(CommandSender sender) {
        Timings timings = new Timings("invictus-reload-tips").startTimings();
        InvictusBukkit.getBukkitInstance().getTipService().loadTips();
        sender.sendMessage(CC.format("&eReloaded tips in &c%dms&e.",
                timings.stopTimings().calculateDifference()));
        return true;
    }

    @CommandCooldown(time = 5, global = true)
    @Command(names = {"invictus reload punishment-templates"},
             permission = "op",
             description = "Reload all tips")
    public boolean reloadPunishmentTemplates(CommandSender sender) {
        Timings timings = new Timings("invictus-reload-punishment-templates").startTimings();
        InvictusBukkit.getBukkitInstance().getBukkitPunishmentService().loadTemplates();
        sender.sendMessage(CC.format("&eReloaded punishment templates in &c%dms&e.",
                timings.stopTimings().calculateDifference()));
        return true;
    }

    @Command(names = {"invictus config set"},
             permission = "owner",
             description = "Set a config value")
    public boolean configSet(CommandSender sender,
                             @Param(name = "field") String field,
                             @Param(name = "newValue") String input) {
        Field declaredField = null;
        try {
            declaredField = invictus.getMainConfig().getClass().getDeclaredField(field);
        } catch (NoSuchFieldException e) {
            try {
                declaredField = invictus.getMainConfig().getClass().getSuperclass().getDeclaredField(field);
            } catch (NoSuchFieldException ignored) { }
        }

        if (declaredField == null) {
            sender.sendMessage(CC.format("&cField &e%s &cnot found."));
            return false;
        }

        ParameterType<?> parameter = CommandService.getParameter(declaredField.getType());
        if (parameter == null) {
            sender.sendMessage(CC.format("&cCould not find ParameterType to parse &e%s&c.",
                    declaredField.getType().getName()));
            return false;
        }

        Object parsed = parameter.parse(sender, input);
        if (parsed == null)
            return false;

        boolean accessible = declaredField.isAccessible();
        declaredField.setAccessible(true);
        try {
            declaredField.set(invictus.getMainConfig(), parsed);
            sender.sendMessage(CC.format("&eSet field &c%s &eto &c%s&e.",
                    declaredField.getName(), parsed.toString()));
        } catch (IllegalAccessException e) {
            sender.sendMessage(CC.RED + "Something went wrong.");
        } finally {
            declaredField.setAccessible(accessible);
        }

        return true;
    }

    @Command(names = {"invictus config save"},
             permission = "owner",
             description = "Save the config")
    public boolean configSave(CommandSender sender) {
        invictus. saveMainConfig();
        sender.sendMessage(CC.GREEN + "Saved");
        return true;
    }

    @Command(names = {"invictus config read"},
             permission = "owner",
             description = "Read the current config",
             async = true)
    public boolean configSet(CommandSender sender) {
        MainConfig mainConfig = invictus.getMainConfig();

        sender.sendMessage(CC.SMALL_CHAT_BAR);
        sender.sendMessage(CC.RED + CC.BOLD + "Config Values");
        try {
            for (Field declaredField : mainConfig.getClass().getDeclaredFields()) {
                boolean accessible = declaredField.isAccessible();
                declaredField.setAccessible(true);
                sender.sendMessage(CC.YELLOW + declaredField.getName() + ": " + CC.RED + declaredField.get(mainConfig));
                declaredField.setAccessible(accessible);
            }

            for (Field declaredField : mainConfig.getClass().getSuperclass().getDeclaredFields()) {
                boolean accessible = declaredField.isAccessible();
                declaredField.setAccessible(true);
                sender.sendMessage(CC.YELLOW + declaredField.getName() + ": " + CC.RED + declaredField.get(mainConfig));
                declaredField.setAccessible(accessible);
            }
        } catch (IllegalAccessException ignored) { }
        sender.sendMessage(CC.SMALL_CHAT_BAR);
        return true;
    }

}
