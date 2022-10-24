package cc.invictusgames.invictus.chat.impl;

import cc.invictusgames.ilib.playersetting.impl.ILibSettings;
import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.invictus.Invictus;
import cc.invictusgames.invictus.chat.FilteredChatChannel;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class PublicChat extends FilteredChatChannel {

    public PublicChat() {
        super("public",
                CC.RED + "Public",
                null,
                Arrays.asList("pc", "p", "pub", "global", "g", "gc"),
                '!',
                0);
    }

    @Override
    public boolean onChat(Player player, String message) {
        if (!ILibSettings.GLOBAL_CHAT.get(player)) {
            player.sendMessage(CC.RED + "You have the global chat disabled.");
            return false;
        }

        return super.onChat(player, message);
    }

    @Override
    public String getFormat(Player player, CommandSender sender) {
        if (sender instanceof Player && !ILibSettings.GLOBAL_CHAT.get((Player) sender))
            return null;

        return "%1$s" + CC.GRAY + ": " + getChatColor(player) + "%2$s";
    }

    public String getChatColor(Player player) {
        return Invictus.getInstance().getProfileService().getProfile(player).getCurrentGrant().getRank().getChatColor();
    }


}
