package cc.invictusgames.invictus.punishment.commands;

import cc.invictusgames.ilib.command.annotation.Command;
import cc.invictusgames.ilib.command.annotation.Flag;
import cc.invictusgames.ilib.command.annotation.Param;
import cc.invictusgames.ilib.messages.page.PagedMessage;
import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.ilib.utils.PasteUtils;
import cc.invictusgames.invictus.InvictusBukkit;
import cc.invictusgames.invictus.playersetting.InvictusSettings;
import cc.invictusgames.invictus.profile.Profile;
import cc.invictusgames.invictus.punishment.Punishment;
import cc.invictusgames.invictus.punishment.menu.PunishmentsMainMenu;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 22.02.2020 / 21:08
 * Invictus / cc.invictusgames.invictus.spigot.punishment.commands
 */

@RequiredArgsConstructor
public class HistoryCommand {

    private final InvictusBukkit invictus;

    @Command(names = {"history", "hist", "check", "c"},
             permission = "invictus.command.history",
             description = "Check the punishment history of a player",
             async = true)
    public boolean history(
            CommandSender sender,
            @Param(name = "player") Profile target,
            @Param(name = "page", defaultValue = "1") int page,
            @Flag(names = {"t", "-text"}, description = "Print the history in chat") boolean text,
            @Flag(names = {"p", "-paste"}, description = "Upload the history to hastebin") boolean paste) {
        List<Punishment> punishments = new ArrayList<>(target.getPunishments());
        punishments.removeIf(punishment -> {
            if ((!sender.hasPermission("invictus.punishments.viewblacklist")) &&
                    (punishment.getPunishmentType().equals(Punishment.PunishmentType.BLACKLIST))) {
                return true;
            }

            return punishment.getPunishmentType().equals(Punishment.PunishmentType.KICK);
        });
        Collections.reverse(punishments);

        if (paste) {
            if (!sender.hasPermission("invictus.punishments.paste")) {
                sender.sendMessage(CC.RED + "You are not allowed to paste the history of players.");
                return false;
            }

            sender.sendMessage(CC.YELLOW + "Uploading punishment history of " + target.getName() + CC.YELLOW + "...");
            String url = pasteLogs(sender, punishments, target);
            if (url == null) {
                sender.sendMessage(CC.RED + "Failed to upload punishment history of " + CC.YELLOW + target.getName() + CC.RED + ".");
                return false;
            }

            sender.sendMessage(CC.GOLD + "History URL: " + CC.WHITE + url);
            return true;
        }

        if (text) {
            this.display(sender, punishments, target, page);
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(CC.RED + "Player only.");
            return false;
        }

        new PunishmentsMainMenu(invictus, target).openMenu((Player) sender);
        return true;
    }

    private String pasteLogs(CommandSender sender, List<Punishment> punishments, Profile target) {
        StringBuilder builder = new StringBuilder();
        builder.append("Punishments of ").append(target.getUuid().toString()).append(":\n");
        punishments.forEach(punishment -> builder.append(punishment.formatPasteEntry(sender,
                InvictusSettings.TIME_ZONE.get(sender))).append("\n"));
        return PasteUtils.paste(builder.toString(), false);
    }

    private void display(CommandSender sender, List<Punishment> messages, Profile target, int page) {
        new PagedMessage<Punishment>() {
            @Override
            public List<String> getHeader(int page, int maxPages) {
                List<String> header = new ArrayList<>();
                header.add(CC.SMALL_CHAT_BAR);
                header.add(CC.RED + CC.BOLD + "Punishments " + CC.GRAY + "(Page " + page + "/" + maxPages + ")");
                return header;
            }

            @Override
            public List<String> getFooter(int page, int maxPages) {
                List<String> footer = new ArrayList<>();
                footer.add(" ");
                footer.add(CC.YELLOW + "Use " + CC.RED + "/history " + target.getName() + " <page> " + CC.YELLOW +
                        "to view more entries.");
                footer.add(CC.SMALL_CHAT_BAR);
                return footer;
            }

            @Override
            public void send(CommandSender commandSender, Punishment punishment) {
                punishment.send(sender);
            }
        }.display(sender, messages, page);
    }
}
