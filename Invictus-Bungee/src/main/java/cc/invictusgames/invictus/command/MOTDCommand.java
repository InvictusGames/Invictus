package cc.invictusgames.invictus.command;

import cc.invictusgames.invictus.motd.MotdConfig;
import joptsimple.internal.Strings;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

public class MOTDCommand extends Command {

    private final MotdConfig motdConfig;

    public MOTDCommand(MotdConfig motdConfig) {
        super("motd", "invictus.command.motd");
        this.motdConfig = motdConfig;
    }

    @Override
    public void execute(CommandSender commandSender, String[] args) {
        if (args.length < 1) {
            commandSender.sendMessages(getUsage());
            return;
        }

        if (args[0].equalsIgnoreCase("setline")) {
            if (args.length < 3) {
                commandSender.sendMessages(getUsage());
                return;
            }

            int line;
            try {
                line = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                commandSender.sendMessage(ChatColor.RED + "This is not a valid line.");
                return;
            }

            switch (line) {
                case 1: {
                    motdConfig.setOne(StringUtils.join(args, " ", 2, args.length));
                    break;
                }
                case 2: {
                    motdConfig.setTwo(StringUtils.join(args, " ", 2, args.length));
                    break;
                }
            }

            motdConfig.saveConfig();
            commandSender.sendMessage(ChatColor.GREEN + "You have changed the motd!");
            return;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            commandSender.sendMessage(ChatColor.GREEN + "You have reloaded the MOTD config.");
            motdConfig.saveConfig();
            return;
        }

        if (args[0].equalsIgnoreCase("clear")) {
            commandSender.sendMessage(ChatColor.GREEN + "You have cleared the motd!");
            motdConfig.clearLines();
            motdConfig.saveConfig();
            return;
        }

        commandSender.sendMessages(getUsage());
    }


    public String[] getUsage() {
        return Arrays.stream(new String[]{
                "&7&m" + Strings.repeat('-', 30),
                "&c/motd setline <line> <motd>",
                "&c/motd clear",
                "&7&m" + Strings.repeat('-', 30)
        }).map(s -> ChatColor.translateAlternateColorCodes('&', s))
                .toArray(String[]::new);
    }
}
