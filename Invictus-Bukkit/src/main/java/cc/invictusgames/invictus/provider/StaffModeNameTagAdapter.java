package cc.invictusgames.invictus.provider;

import cc.invictusgames.ilib.scoreboard.nametag.NameTag;
import cc.invictusgames.ilib.scoreboard.nametag.NameTagAdapter;
import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.invictus.InvictusBukkit;
import cc.invictusgames.invictus.base.StaffMode;
import cc.invictusgames.invictus.profile.Profile;
import org.bukkit.entity.Player;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 21.10.2020 / 13:19
 * Invictus / cc.invictusgames.invictus.spigot.utils.nametag
 */

public class StaffModeNameTagAdapter extends NameTagAdapter {

    private final InvictusBukkit invictus;

    public StaffModeNameTagAdapter(InvictusBukkit invictus) {
        super("Invictus Staff Mode Adapter", 10);
        this.invictus = invictus;
    }

    @Override
    public NameTag getNameTag(Player player, Player target) {
        Profile profile = invictus.getProfileService().getProfile(target);

        if (profile == null)
            return null;

        if (StaffMode.isStaffMode(target) || StaffMode.isVanished(target))
            return new NameTag(
                    "*" + profile.getCurrentGrant().getRank().getName(),
                    CC.GRAY + "*" + profile.getCurrentGrant().getRank().getColor(),
                    "",
                    true
            );

        return null;
    }
}
