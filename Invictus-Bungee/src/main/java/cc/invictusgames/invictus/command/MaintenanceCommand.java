package cc.invictusgames.invictus.command;

import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.ilib.uuid.UUIDCache;
import cc.invictusgames.invictus.InvictusBungee;
import joptsimple.internal.Strings;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;

public class MaintenanceCommand extends Command {

    private final InvictusBungee invictus;

    public MaintenanceCommand(InvictusBungee invictus) {
        super("maintenance", "invictus.command.maintenance");
        this.invictus = invictus;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessages(getUsage());
            return;
        }

        if (args[0].equalsIgnoreCase("set") && args.length == 2) {
            Integer level = null;
            try {
                level = Integer.parseInt(args[1]);
            } catch (NumberFormatException e){
                sender.sendMessage(CC.RED + "Invalid level.");
                return;
            }

            invictus.getMainConfig().setMaintenanceLevel(level);
            invictus.saveMainConfig();
            sender.sendMessage(CC.format("&6Set maintenance level to &f%d&6.", level));
            return;
        }

        if (args[0].equalsIgnoreCase("add") && args.length == 2) {
            UUID uuid = UUIDCache.getUuid(args[1]);
            if (uuid == null) {
                sender.sendMessage(CC.RED + "Unknown player");
                return;
            }

            if (invictus.getMainConfig().getMaintenanceList().contains(uuid)) {
                sender.sendMessage(CC.RED + "Player already on list");
                return;
            }

            invictus.getMainConfig().getMaintenanceList().add(uuid);
            invictus.saveMainConfig();
            sender.sendMessage(CC.format("&6Added &f%s &6to the maintenance list.", UUIDCache.getName(uuid)));
            return;
        }

        if (args[0].equalsIgnoreCase("remove") && args.length == 2) {
            UUID uuid = UUIDCache.getUuid(args[1]);
            if (uuid == null) {
                sender.sendMessage(CC.RED + "Unknown player");
                return;
            }

            if (!invictus.getMainConfig().getMaintenanceList().contains(uuid)) {
                sender.sendMessage(CC.RED + "Player not on list");
                return;
            }

            invictus.getMainConfig().getMaintenanceList().remove(uuid);
            invictus.saveMainConfig();
            sender.sendMessage(CC.format("&6Removed &f%s &6from the maintenance list.", UUIDCache.getName(uuid)));
            return;
        }

        if (args[0].equalsIgnoreCase("list") && args.length == 1) {
            sender.sendMessage(CC.format("&6Maintenane list: &f%s",
                    invictus.getMainConfig().getMaintenanceList().stream()
                            .map(uuid -> CC.WHITE + UUIDCache.getName(uuid))
                            .collect(Collectors.joining(CC.GOLD + ", "))));
            return;
        }

        sender.sendMessages(getUsage());
    }

    public String[] getUsage() {
        return Arrays.stream(new String[]{
                "&7&m" + Strings.repeat('-', 30),
                "&c/maintenance set <level>",
                "&c/maintenance add <player>",
                "&c/maintenance remove <player>",
                "&c/maintenance list",
                "&7&m" + Strings.repeat('-', 30)
        }).map(s -> ChatColor.translateAlternateColorCodes('&', s))
                .toArray(String[]::new);
    }
}
