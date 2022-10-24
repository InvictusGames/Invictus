package cc.invictusgames.invictus.provider;

import cc.invictusgames.ilib.scoreboard.nametag.NameTag;
import cc.invictusgames.ilib.scoreboard.nametag.NameTagAdapter;
import cc.invictusgames.invictus.InvictusBukkit;
import cc.invictusgames.invictus.profile.Profile;
import org.bukkit.entity.Player;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 21.10.2020 / 13:17
 * Invictus / cc.invictusgames.invictus.spigot.utils.nametag
 */

public class RankNameTagAdapter extends NameTagAdapter {

    private final InvictusBukkit invictus;

    public RankNameTagAdapter(InvictusBukkit invictus) {
        super("Invictus Rank Adapter", 1);
        this.invictus = invictus;
    }

    @Override
    public NameTag getNameTag(Player player, Player target) {
        Profile profile = invictus.getProfileService().getProfile(target);
        return new NameTag(
                profile.getCurrentGrant().getRank().getName(),
                profile.getCurrentGrant().getRank().getColor(),
                ""
        );
    }
}
