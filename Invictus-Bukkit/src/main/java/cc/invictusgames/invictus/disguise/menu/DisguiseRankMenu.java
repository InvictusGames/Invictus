package cc.invictusgames.invictus.disguise.menu;

import cc.invictusgames.ilib.builder.ItemBuilder;
import cc.invictusgames.ilib.menu.Button;
import cc.invictusgames.ilib.menu.Menu;
import cc.invictusgames.ilib.menu.fill.FillTemplate;
import cc.invictusgames.invictus.InvictusBukkit;
import cc.invictusgames.invictus.disguise.procedure.DisguiseProcedure;
import cc.invictusgames.invictus.rank.Rank;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 18.06.2020 / 17:05
 * Invictus / cc.invictusgames.invictus.spigot.disguise.menu
 */

@RequiredArgsConstructor
public class DisguiseRankMenu extends Menu {

    private final InvictusBukkit invictus;
    private final DisguiseProcedure procedure;

    @Override
    public String getTitle(Player player) {
        return "Pick a rank";
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        List<Rank> ranks = new ArrayList<>();
        for (Rank rank : invictus.getRankService().getRanksSorted()) {
            if (rank.isDisguisable())
                ranks.add(rank);
        }

        int slot = 1;
        int row = 0;
        for (Rank rank : ranks) {
            if (slot > 8) {
                slot = 1;
                row++;
            }

            buttons.put(getSlot(row, slot), new RankButton(rank));
            slot += 2;
        }

        ranks.clear();
        return buttons;
    }

    @Override
    public FillTemplate getFillTemplate() {
        return FillTemplate.FILL;
    }

    @Override
    public boolean isAutoUpdate() {
        return false;
    }

    @RequiredArgsConstructor
    public class RankButton extends Button {

        private final Rank rank;

        @Override
        public ItemStack getItem(Player player) {
            return new ItemBuilder(Material.INK_SACK, rank.getDyeColor().getDyeData())
                    .setDisplayName(rank.getDisplayName())
                    .build();
        }

        @Override
        public void click(Player player, int slot, ClickType clickType, int hotbarButton) {
            procedure.setRank(rank);
            new DisguiseSkinMenu(invictus, procedure).openMenu(player);
        }
    }
}
