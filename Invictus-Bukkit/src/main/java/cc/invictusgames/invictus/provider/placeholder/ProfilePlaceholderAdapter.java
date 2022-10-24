package cc.invictusgames.invictus.provider.placeholder;

import cc.invictusgames.ilib.placeholder.adapter.PlaceholderAdapter;
import cc.invictusgames.invictus.Invictus;
import cc.invictusgames.invictus.profile.Profile;
import cc.invictusgames.invictus.rank.Rank;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public class ProfilePlaceholderAdapter implements PlaceholderAdapter {

    private final Invictus invictus;

    @Override
    public String getIdentifier() {
        return "profile";
    }

    @Override
    public String getPlaceholder(Player player, String placeholder) {
        String[] args = placeholder.split(":");
        placeholder = args[0];

        Profile profile = invictus.getProfileService().getProfile(player);

        if (placeholder.equalsIgnoreCase("scopeRank") && args.length == 2) {
            String scope = args[1];

            return profile.getRealCurrentGrantOn(scope).getRank().getName();
        }

        if (placeholder.equalsIgnoreCase("realName"))
            return profile.getName();

        if (placeholder.equalsIgnoreCase("realDisplayName"))
            return profile.getRealDisplayName();

        return null;
    }
}
