package cc.invictusgames.invictus.chat;

import cc.invictusgames.ilib.chat.LocalChatChannel;
import org.bukkit.entity.Player;

import java.util.List;

public abstract class FilteredLocalChatChannel extends LocalChatChannel {

    private final boolean ignoreChatRestrictions;

    public FilteredLocalChatChannel(String name,
                                    String displayName,
                                    String permission,
                                    List<String> aliases,
                                    char prefix,
                                    int priority) {
        this(name, displayName, permission, aliases, prefix, priority, false);
    }

    public FilteredLocalChatChannel(String name,
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
        return FilteredChatChannel.canChat(player, message, this, ignoreChatRestrictions);
    }
}
