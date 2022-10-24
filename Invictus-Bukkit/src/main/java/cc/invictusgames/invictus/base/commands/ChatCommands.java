package cc.invictusgames.invictus.base.commands;

import cc.invictusgames.ilib.command.annotation.Command;
import cc.invictusgames.ilib.command.annotation.Param;
import cc.invictusgames.ilib.utils.TimeUtils;
import cc.invictusgames.invictus.InvictusBukkit;
import cc.invictusgames.invictus.utils.NetworkBroadcastPacket;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class ChatCommands {

    private final InvictusBukkit invictus;

    @Command(names = {"clearchat", "cc"},
             permission = "invictus.command.clearchat",
             description = "Clear the server chat.")
    public boolean clearChatCommand(CommandSender sender) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.hasPermission("invictus.staff")) {
                player.sendMessage(new String[1000]);
                player.sendMessage(ChatColor.LIGHT_PURPLE + "Chat has been cleared by a staff member.");
            }
        }

        String displayName = (sender instanceof Player)
                ? invictus.getProfileService().getProfile(((Player) sender).getUniqueId()).getRealDisplayName()
                : ChatColor.DARK_RED + "Console";

        invictus.getRedisService()
                .publish(new NetworkBroadcastPacket(invictus.getMessageService().formatMessage("staff.clearchat",
                        invictus.getServerName(), displayName), "invictus.staff", true));
        return true;
    }

    @Command(names = {"slowchat"},
             permission = "invictus.command.slowchat",
             description = "Slow the server chat.")
    public boolean slowChatCommand(CommandSender sender,
                                   @Param(name = "delay", defaultValue = "unslow") String delayString) {

        long delay;

        if (delayString.equalsIgnoreCase("unslow")) {
            if (invictus.getMainConfig().getSlowChatDelay() == -1)
                delay = TimeUnit.SECONDS.toMillis(5L);
            else delay = -1;
        } else {
            delay = TimeUtils.parseTime(delayString);

            if (delay == -1) {
                sender.sendMessage(ChatColor.RED + "Invalid delay.");
                return true;
            }
        }

        invictus.getMainConfig().setSlowChatDelay(delay);
        invictus.saveMainConfig();

        String displayName = (sender instanceof Player)
                ? invictus.getProfileService().getProfile(((Player) sender).getUniqueId()).getRealDisplayName()
                : ChatColor.DARK_RED + "Console";

        invictus.getRedisService().publish(
                new NetworkBroadcastPacket(
                        invictus.getMessageService().formatMessage("staff.slowchat."
                                        + (delay > -1 ? "slowed" : "unslowed"),
                                invictus.getServerName(),
                                displayName,
                                TimeUtils.formatDetailed(delay)),
                        "invictus.staff",
                        true
                ));
        return true;
    }

    @Command(names = {"mutechat"},
             permission = "invictus.command.mutechat",
             description = "Mute the server chat.")
    public boolean muteChatCommand(CommandSender sender) {
        boolean chatMuted = !invictus.getMainConfig().isChatMuted();
        invictus.getMainConfig().setChatMuted(chatMuted);
        invictus.saveMainConfig();

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.hasPermission("invictus.staff"))
                player.sendMessage(ChatColor.LIGHT_PURPLE + "Chat has been "
                        + (chatMuted ? "muted" : "unmuted") + " by a staff member.");
        }

        String displayName = (sender instanceof Player)
                ? invictus.getProfileService().getProfile(((Player) sender).getUniqueId()).getRealDisplayName()
                : ChatColor.DARK_RED + "Console";

        invictus.getRedisService().publish(
                new NetworkBroadcastPacket(
                        invictus.getMessageService().formatMessage("staff.mutechat."
                                        + (chatMuted ? "muted" : "unmuted"),
                                invictus.getServerName(),
                                displayName),
                        "invictus.staff",
                        true
                ));
        return true;
    }
}
