package cc.invictusgames.invictus.provider.placeholder;

import cc.invictusgames.ilib.placeholder.adapter.PlaceholderAdapter;
import cc.invictusgames.invictus.Invictus;
import cc.invictusgames.invictus.rank.Rank;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public class RankPlaceholderAdapter implements PlaceholderAdapter {

    private final Invictus invictus;

    @Override
    public String getIdentifier() {
        return "rank";
    }

    @Override
    public String getPlaceholder(Player player, String placeholder) {
        String[] args = placeholder.split(":");
        placeholder = args[0];

        Rank rank = null;
        if (args.length == 2)
            rank = invictus.getRankService().getRank(args[1]);

        if (rank == null)
            rank = invictus.getProfileService().getProfile(player).getRealCurrentGrant().getRank();

        if (placeholder.equalsIgnoreCase("name"))
            return rank.getName();

        if (placeholder.equalsIgnoreCase("displayName"))
            return rank.getDisplayName();

        if (placeholder.equalsIgnoreCase("prefix"))
            return rank.getPrefix();

        if (placeholder.equalsIgnoreCase("suffix"))
            return rank.getSuffix();

        if (placeholder.equalsIgnoreCase("color"))
            return rank.getColor();

        if (placeholder.equalsIgnoreCase("chatColor"))
            return rank.getChatColor();

        return null;
    }
}
