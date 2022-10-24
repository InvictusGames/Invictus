package cc.invictusgames.invictus.base.menu.staffmode;

import cc.invictusgames.ilib.builder.ItemBuilder;
import cc.invictusgames.ilib.menu.Button;
import cc.invictusgames.ilib.menu.Menu;
import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.ilib.utils.TimeUtils;
import cc.invictusgames.invictus.Invictus;
import cc.invictusgames.invictus.base.StaffMode;
import cc.invictusgames.invictus.profile.Profile;
import cc.invictusgames.invictus.rank.Rank;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 12.10.2020 / 17:21
 * Invictus / cc.invictusgames.invictus.spigot.base.menu.staffmode
 */

@RequiredArgsConstructor
public class OnlineStaffMenu extends Menu {

    private final Invictus invictus;

    @Override
    public String getTitle(Player player) {
        return "Online Staff";
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        Comparator<Profile> comparator =
                Comparator.comparingInt(profile -> profile.getRealCurrentGrant().getRank().getWeight());

        List<Profile> profiles = new ArrayList<>();
        for (Player current : Bukkit.getOnlinePlayers()) {
            Profile profile = invictus.getProfileService().getProfile(current);
            if (current.hasPermission("invictus.staff"))
                profiles.add(profile);
        }

        profiles.sort(comparator.reversed());
        for (Profile profile : profiles) {
            buttons.put(buttons.size(), new StaffButton(profile));
        }

        profiles.clear();
        return buttons;
    }

    @RequiredArgsConstructor
    public class StaffButton extends Button {

        private final Profile profile;

        @Override
        public ItemStack getItem(Player player) {
            Rank rank = profile.getRealCurrentGrant().getRank();
            StaffMode staffMode = StaffMode.get(profile.player());
            long playTime = StaffMode.getPlayTimeGetter().getPlayTime(profile.player());

            List<String> lore = new ArrayList<>();
            lore.add(CC.MENU_BAR);
            lore.add(CC.YELLOW + "Staff Mode: " + CC.colorBoolean(staffMode.isEnabled(), true));
            lore.add(CC.YELLOW + "Vanished: " + CC.colorBoolean(staffMode.isVanished(), "Yes", "No"));
            if (playTime != -1) {
                lore.add(CC.YELLOW + "Playtime: " + CC.RED + TimeUtils.formatDetailed(playTime));
            }
            lore.add(" ");
            lore.add(CC.YELLOW + "Click to teleport");
            lore.add(CC.MENU_BAR);

            return new ItemBuilder(Material.SKULL_ITEM, 3)
                    .setSkullOwner(profile.getName())
                    .setDisplayName(rank.getPrefix() + profile.getName() + rank.getSuffix())
                    .setLore(lore).build();
        }

        @Override
        public void click(Player player, int slot, ClickType clickType, int hotbarButton) {
            Bukkit.dispatchCommand(player, "tp " + profile.getName());
        }
    }

    @Override
    public boolean isAutoUpdate() {
        return false;
    }

    @Override
    public boolean isClickUpdate() {
        return true;
    }
}
