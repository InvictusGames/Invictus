package cc.invictusgames.invictus.provider;

import cc.invictusgames.ilib.visibility.VisibilityAction;
import cc.invictusgames.ilib.visibility.VisibilityAdapter;
import cc.invictusgames.invictus.InvictusBukkit;
import cc.invictusgames.invictus.base.StaffMode;
import cc.invictusgames.invictus.playersetting.InvictusSettings;
import org.bukkit.entity.Player;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 24.10.2020 / 07:16
 * Invictus / cc.invictusgames.invictus.spigot.utils.visibility
 */

public class StaffModeVisibilityAdapter extends VisibilityAdapter {

    private final InvictusBukkit invictus;

    public StaffModeVisibilityAdapter(InvictusBukkit invictus) {
        super("Invictus Staff Mode Adapter", 10);
        this.invictus = invictus;
    }

    @Override
    public VisibilityAction canSee(Player player, Player target) {
        StaffMode staffMode = StaffMode.get(player);
        StaffMode targetStaffMode = StaffMode.get(target);
        if (!targetStaffMode.isVanished() && !targetStaffMode.isEnabled())
            return VisibilityAction.NEUTRAL;

        if (targetStaffMode.isEnabled() && !targetStaffMode.isVanished())
            return VisibilityAction.SHOW;

        if (!player.hasPermission("invictus.staff"))
            return VisibilityAction.HIDE;

        if (staffMode.isEnabled() && InvictusSettings.STAFF_SHOWN.get(player))
            return VisibilityAction.SHOW;

        if (!InvictusSettings.STAFF_SHOWN.get(player)|| !invictus.getMainConfig().isStaffVisible())
            return VisibilityAction.HIDE;

        return VisibilityAction.SHOW;
    }
}
