package cc.invictusgames.invictus.base.commands;

import cc.invictusgames.ilib.command.annotation.Command;
import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.ilib.visibility.VisibilityService;
import cc.invictusgames.invictus.InvictusBukkit;
import cc.invictusgames.invictus.base.StaffMode;
import cc.invictusgames.invictus.playersetting.InvictusSettings;
import cc.invictusgames.invictus.profile.Profile;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 08.06.2020 / 19:41
 * Invictus / cc.invictusgames.invictus.spigot.base.commands
 */

@RequiredArgsConstructor
public class StaffModeCommands {

    private final InvictusBukkit invictus;

    @Command(names = {"staffmode", "staff", "modmode", "mod", "hackermode", "h", "v"},
             permission = "invictus.command.staffmode",
             playerOnly = true)
    public boolean staffmode(Player sender) {
        StaffMode.get(sender).toggleEnabled(false);
        return true;
    }

    @Command(names = {"vanish"},
             permission = "invictus.command.staffmode",
             playerOnly = true)
    public boolean vanish(Player sender) {
        Profile profile = invictus.getProfileService().getProfile(sender);

        if (!sender.hasPermission("invictus.command.vanish")) {
            return staffmode(sender);
        }

        StaffMode.get(sender).toggleVanish(false);
        return true;
    }

    @Command(names = {"hidestaff", "showstaff"},
             permission = "invictus.command.hidestaff",
             playerOnly = true)
    public boolean hidestaff(Player sender) {
        InvictusSettings.STAFF_SHOWN.set(sender, !InvictusSettings.STAFF_SHOWN.get(sender));
        sender.sendMessage(CC.format("&6Vanished staff members are now %s&6.",
                CC.colorBoolean(InvictusSettings.STAFF_SHOWN.get(sender), "shown", "hidden")));
        VisibilityService.update(sender);
        return true;
    }

    @Command(names = {"teststaffmode"},
             permission = "op",
             playerOnly = true)
    public boolean testStaffmode(Player sender) {
        Profile profile = invictus.getProfileService().getProfile(sender);
        sender.sendMessage("staffMode=" + StaffMode.isStaffMode(sender));
        sender.sendMessage("vanished=" + StaffMode.isVanished(sender));
        sender.sendMessage("bypassPermission=" + sender.hasPermission("invictus.command.build"));
        sender.sendMessage("collidesWithEntities=" + sender.spigot().getCollidesWithEntities());
        sender.sendMessage("affectsSpawning=" + sender.spigot().getAffectsSpawning());
        return true;
    }

    @Command(names = {"despawnentity"},
             permission = "invictus.command.despawnentity",
             playerOnly = true,
             description = "Despawn your last clicked entity")
    public boolean despawnEntity(Player sender) {
        StaffMode staffMode = StaffMode.get(sender);
        if (staffMode.getDespawningEntity() == null) {
            sender.sendMessage(CC.RED + "There is no Entity to despawn.");
            return false;
        }

        staffMode.getDespawningEntity().remove();
        sender.sendMessage(CC.GOLD + "Successfully despawned the "
                + CC.WHITE + staffMode.getDespawningEntity().getType().getName()
                + CC.GOLD + ".");
        staffMode.setDespawningEntity(null);
        return true;
    }

    @Command(names = {"amivisible", "amivis", "vis?", "v?"},
             permission = "invictus.command.amivisible",
             description = "Test if you are visible",
             playerOnly = true)
    public boolean amIVisible(Player sender) {
        sender.sendMessage(CC.GOLD + "You are "
                + CC.colorBoolean(StaffMode.isStaffMode(sender), "in", "not in")
                + CC.GOLD + " Staff Mode, and are "
                + CC.colorBoolean(StaffMode.isVanished(sender), "invisible", "visible")
                + CC.GOLD + ".");
        return true;
    }

}
