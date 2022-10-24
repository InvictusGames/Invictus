package cc.invictusgames.invictus.chat;

import cc.invictusgames.ilib.chat.ChatChannel;
import cc.invictusgames.ilib.chat.ChatService;
import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.ilib.utils.TimeUtils;
import cc.invictusgames.invictus.InvictusBukkit;
import cc.invictusgames.invictus.banphrase.Banphrase;
import cc.invictusgames.invictus.profile.Profile;
import cc.invictusgames.invictus.punishment.Punishment;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;

public abstract class FilteredChatChannel extends ChatChannel {

    private final boolean ignoreChatRestrictions;

    public FilteredChatChannel(String name,
                               String displayName,
                               String permission,
                               List<String> aliases,
                               char prefix,
                               int priority) {
        this(name, displayName, permission, aliases, prefix, priority, false);
    }

    public FilteredChatChannel(String name,
                               String displayName,
                               String permission,
                               List<String> aliases,
                               char prefix,
                               int priority,
                               boolean ignoreChatRestrictions) {
        super(name, displayName, permission, aliases, prefix, priority);
        this.ignoreChatRestrictions = ignoreChatRestrictions;
    }

    @Override
    public boolean onChat(Player player, String message) {
        return canChat(player, message, this, ignoreChatRestrictions);
    }

    public static boolean canChat(Player player,
                                  String message,
                                  ChatChannel chatChannel,
                                  boolean ignoreChatRestrictions) {
        Profile profile = InvictusBukkit.getBukkitInstance().getProfileService().getProfile(player);
        Banphrase banphrase = InvictusBukkit.getBukkitInstance().getBanphraseService().checkBanned(message);
        if (banphrase != null && !player.hasPermission("invictus.filter.bypass")) {
            banphrase.punish(message, profile, null);
            player.sendMessage(String.format(
                    chatChannel.getFormat(player, player),
                    ChatService.getPrefixGetter().apply(player, player),
                    message
            ));
            return false;
        }

        if (profile.isRequiresAuthentication()) {
            player.sendMessage(CC.RED + "Please authenticate using " + CC.YELLOW + "/auth <code>"
                    + CC.RED + ".");
            return false;
        }

        Punishment mute = profile.getActivePunishment(Punishment.PunishmentType.MUTE);

        if (mute != null) {
            if (mute.getDuration() == -1)
                player.sendMessage(CC.format(
                        "&cYou have been permanently muted due to &e%s&c.",
                        mute.getPunishedReason()
                ));
            else player.sendMessage(CC.format(
                    "&cYou have been muted for &e%s &cdue to &e%s&c. This punishment expires in &e%s&c.",
                    TimeUtils.formatDetailed(mute.getDuration()),
                    mute.getPunishedReason(),
                    TimeUtils.formatDetailed(mute.getRemainingTime())
            ));
            return false;
        }

        if (InvictusBukkit.getBukkitInstance().getMainConfig().isChatMuted()
                && !player.hasPermission("invictus.mutechat.bypass")
                && !ignoreChatRestrictions) {
            player.sendMessage(ChatColor.RED + "The chat is currently muted, try again later.");
            return false;
        }

        if (InvictusBukkit.getBukkitInstance().getMainConfig().getSlowChatDelay() > -1
                && !player.hasPermission("invictus.slowchat.bypass")
                && !ignoreChatRestrictions) {

            long nextSpeak = profile.getLastSpeakMillis()
                    + InvictusBukkit.getBukkitInstance().getMainConfig().getSlowChatDelay();
            if (nextSpeak > System.currentTimeMillis()) {

                player.sendMessage(ChatColor.RED + "The chat is currently slowed, you can speak again in "
                        + ChatColor.YELLOW + TimeUtils.formatDetailed(profile.getLastSpeakMillis()
                        + InvictusBukkit.getBukkitInstance().getMainConfig().getSlowChatDelay()
                        - System.currentTimeMillis()) + ChatColor.RED + ".");

                return false;
            }

            profile.setLastSpeakMillis(System.currentTimeMillis());
        }

        return true;
    }
}
