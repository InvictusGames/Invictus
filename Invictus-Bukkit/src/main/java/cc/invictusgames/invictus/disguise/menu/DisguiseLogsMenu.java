package cc.invictusgames.invictus.disguise.menu;

import cc.invictusgames.ilib.builder.ItemBuilder;
import cc.invictusgames.ilib.menu.Button;
import cc.invictusgames.ilib.menu.page.PagedMenu;
import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.ilib.utils.TimeUtils;
import cc.invictusgames.invictus.disguise.DisguiseLogEntry;
import cc.invictusgames.invictus.playersetting.InvictusSettings;
import cc.invictusgames.invictus.profile.Profile;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 20.06.2020 / 11:28
 * Invictus / cc.invictusgames.invictus.spigot.disguise.menu
 */

@RequiredArgsConstructor
public class DisguiseLogsMenu extends PagedMenu {

    private final Profile target;

    @Override
    public Map<Integer, Button> getAllPagesButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        List<DisguiseLogEntry> logs = new ArrayList<>(target.getDisguiseData().getLogs());
        Collections.reverse(logs);
        logs.forEach(entry -> buttons.put(buttons.size(), new DisguiseLogButton(entry)));
        return buttons;
    }

    @Override
    public String getRawTitle(Player player) {
        return "Disguises: " + target.getRealDisplayName();
    }

    @RequiredArgsConstructor
    public class DisguiseLogButton extends Button {

        private final DisguiseLogEntry entry;

        @Override
        public ItemStack getItem(Player player) {
            List<String> lore = new ArrayList<>();
            lore.add(CC.MENU_BAR);
            lore.add(CC.RED + entry.getName() + CC.YELLOW + " as " + CC.RED + entry.getRank());
            lore.add(" ");
            if (entry.getRemovedAt() != -1)
                lore.add(CC.RED + "Removed at " + CC.YELLOW + TimeUtils.formatDate(entry.getRemovedAt(),
                        InvictusSettings.TIME_ZONE.get(player)));
            else lore.add(CC.GREEN + "Active");
            lore.add(CC.MENU_BAR);
            return new ItemBuilder(Material.PAPER)
                    .setDisplayName((entry.getRemovedAt() == -1 ? CC.GREEN : CC.RED) + CC.BOLD
                            + TimeUtils.formatDate(entry.getTimeStamp(), InvictusSettings.TIME_ZONE.get(player)))
                    .setLore(lore)
                    .build();
        }
    }
}
