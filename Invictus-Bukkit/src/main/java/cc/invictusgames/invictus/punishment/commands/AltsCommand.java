package cc.invictusgames.invictus.punishment.commands;

import cc.invictusgames.ilib.command.annotation.Command;
import cc.invictusgames.ilib.command.annotation.Param;
import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.ilib.utils.ChatMessage;
import cc.invictusgames.ilib.utils.Debugger;
import cc.invictusgames.ilib.utils.TimeUtils;
import cc.invictusgames.invictus.InvictusBukkit;
import cc.invictusgames.invictus.profile.Profile;
import cc.invictusgames.invictus.punishment.Punishment;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 26.03.2020 / 02:50
 * Invictus / cc.invictusgames.invictus.spigot.punishment.commands
 */

@RequiredArgsConstructor
public class AltsCommand {

    private final InvictusBukkit invictus;

    @Command(names = {"alts"},
             permission = "invictus.command.alts",
             description = "Check a players alternate accounts",
             async = true)
    public boolean alts(CommandSender sender, @Param(name = "player") Profile target) {
        if (target.getAlts() == null) {
            invictus.getProfileService().getAlts(target, alts -> {
                if (alts.isEmpty()) {
                    sender.sendMessage(CC.format("&e%s &chas no known alts.", target.getName()));
                    return;
                }

                sender.sendMessage(CC.format("&eAlts of %s: &e(%d) (&aOnline&e, &7Offline&e, &cBanned&e, " +
                                "&4Blacklisted&e)",
                        target.getRealDisplayName(), alts.size()));
                sendAltsMessage(sender, target, alts, false);
            }, true);
            return true;
        }

        if (target.getAlts().isEmpty()) {
            sender.sendMessage(CC.format("&e%s &chas no known alts.", target.getName()));
            return false;
        }

        sender.sendMessage(CC.format("&eAlts of %s: &e(%d) (&aOnline&e, &7Offline&e, &cBanned&e, &4Blacklisted&e)",
                target.getRealDisplayName(), target.getAlts().size()));
        sendAltsMessage(sender, target, target.getAlts(), false);
        return true;
    }

    @Command(names = {"ipreport"},
             permission = "invictus.command.ipreport",
             description = "Check the alts of all online players")
    public boolean ipreport(CommandSender sender) {
        sender.sendMessage(CC.translate("&eOnline players' alts: (&aOnline&e, &7Offline&e, &cBanned&e, " +
                "&4Blacklisted&e)"));
        Bukkit.getOnlinePlayers().forEach(target -> {
            Profile profile = invictus.getProfileService().getProfile(target);
            if (profile.getAlts() == null) {
                invictus.getProfileService().getAlts(profile, alts -> {
                    if (alts.isEmpty())
                        return;

                    sendAltsMessage(sender, profile, alts, true);
                }, true);
                return;
            }

            if (profile.getAlts().isEmpty())
                return;

            sendAltsMessage(sender, profile, profile.getAlts(), true);
        });
        return true;
    }

    public void sendAltsMessage(CommandSender sender, Profile profile, List<Profile> alts, boolean prefix) {
        ChatMessage message = new ChatMessage(prefix ? profile.getName() + " (" + alts.size() + "): " : "")
                .color(ChatColor.YELLOW);

        AtomicBoolean first = new AtomicBoolean(true);
        alts.forEach(alt -> {
            if (first.get()) {
                first.set(false);
            } else {
                message.add(", ").color(ChatColor.YELLOW);
            }
            Punishment blacklist = alt.getActivePunishment(Punishment.PunishmentType.BLACKLIST);
            Punishment ban = alt.getActivePunishment(Punishment.PunishmentType.BAN);

            List<String> hoverText = new ArrayList<>();

            message.add(alt.getName())
                    .runCommand("/c " + alt.getName());

            hoverText.add(CC.MENU_BAR);
            hoverText.add(CC.YELLOW + "Playtime: " + CC.RED + TimeUtils.formatTimeShort(alt.getTotalPlayTime()));
            hoverText.add(CC.YELLOW + "Last Seen: " + CC.RED + (alt.getLastServer() != null
                    ? "Online on " + CC.YELLOW + alt.getLastServer()
                    : TimeUtils.formatTimeShort(System.currentTimeMillis() - alt.getLastSeen()) + " ago"));
            if (blacklist != null && sender.hasPermission("invictus.punishments.viewblacklist")) {
                message.color(ChatColor.DARK_RED);

                hoverText.add(" ");
                hoverText.add(CC.RED + CC.BOLD + "Blacklisted:");
                hoverText.add(CC.YELLOW + "Reason: " + CC.RED + blacklist.getPunishedReason());
            } else if (ban != null) {
                message.color(ChatColor.RED);

                hoverText.add(" ");
                hoverText.add(CC.RED + CC.BOLD + "Banned:");
                hoverText.add(CC.YELLOW + "Remaining: " + CC.RED + TimeUtils.formatTimeShort(ban.getRemainingTime()));
                hoverText.add(CC.YELLOW + "Reason: " + CC.RED + ban.getPunishedReason());
            } else if (alt.player() != null)
                message.color(ChatColor.GREEN);
            else
                message.color(ChatColor.GRAY);

            hoverText.add(" ");
            hoverText.add(profile.getLastIp().equals(alt.getLastIp())
                    ? CC.GREEN + "Last ips directly match"
                    : CC.GRAY + "Last ips don't directly match");

            hoverText.add(CC.MENU_BAR);
            message.hoverText(String.join("\n", hoverText));
        });
        message.send(sender);
    }
}
