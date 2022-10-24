package cc.invictusgames.invictus.base.menu.staffmode;

import cc.invictusgames.ilib.builder.ItemBuilder;
import cc.invictusgames.ilib.menu.Button;
import cc.invictusgames.ilib.menu.Menu;
import cc.invictusgames.ilib.menu.fill.FillTemplate;
import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.ilib.utils.TimeUtils;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 20.04.2020 / 16:15
 * Invictus / cc.invictusgames.invictus.spigot.base.menu.staffmode
 */

@RequiredArgsConstructor
public class ExamineBlockMenu extends Menu {

    private final InventoryHolder block;

    @Override
    public String getTitle(Player player) {
        return "[S] " + block.getInventory().getType().getDefaultTitle();
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        if (block instanceof Chest) {
            for (int index = 0; index < block.getInventory().getContents().length; index++) {
                buttons.put(index, new ItemButton(block.getInventory().getContents()[index]));
            }
            return buttons;
        }

        if (block instanceof Furnace) {
            Furnace furnace = (Furnace) block;
            double percentage = ((furnace.getCookTime() / 20.0D) / 9.0D) * 100;
            String fuelLeft = TimeUtils.formatHHMMSS(furnace.getBurnTime() / 20, TimeUnit.SECONDS);

            ItemStack item = new ItemBuilder(Material.FURNACE)
                    .setLore(CC.GOLD + "Progress: " + CC.WHITE + (percentage > 100 ? 100 : Math.round(percentage)) +
                                    "%",
                            CC.GOLD + "Fuel left: " + CC.WHITE + fuelLeft)
                    .build();

            buttons.put(11, new ItemButton(furnace.getInventory().getFuel()));
            buttons.put(12, new ItemButton(item));
            buttons.put(13, new ItemButton(furnace.getInventory().getSmelting()));
            buttons.put(15, new ItemButton(furnace.getInventory().getResult()));
            return buttons;
        }

        if (block instanceof Hopper) {
            int startIndex = 11;
            for (int index = 0; index < block.getInventory().getContents().length; index++) {
                buttons.put(startIndex + index, new ItemButton(block.getInventory().getContents()[index]));
            }
        }

        if (block instanceof BrewingStand) {
            BrewingStand brewingStand = (BrewingStand) block;
            double percentage = ((brewingStand.getBrewingTime() / 20.0D) / 9.0D) * 100;

            ItemStack item = new ItemBuilder(Material.BREWING_STAND_ITEM)
                    .setLore(CC.GOLD + "Progress: " + CC.WHITE + (percentage > 100 ? 100 : Math.round(percentage)) +
                            "%")
                    .build();

            buttons.put(4, new ItemButton(brewingStand.getInventory().getIngredient()));
            buttons.put(13, new ItemButton(item));

            for (int i = 0; i < 3; i++) {
                buttons.put(21 + i, new ItemButton(brewingStand.getInventory().getItem(i)));
            }

        }

        if (block instanceof Dropper || block instanceof Dispenser) {
            int startIndex = 3;
            for (int i = 0; i < 9; i++) {
                buttons.put(startIndex + i, new ItemButton(block.getInventory().getItem(i)));
                if (i == 2)
                    startIndex = 9;

                if (i == 5)
                    startIndex = 15;
            }
        }

        return buttons;
    }

    @Override
    public int getSize() {
        if (block instanceof Chest)
            return block.getInventory().getSize();

        if (block instanceof Furnace
                || block instanceof Hopper
                || block instanceof BrewingStand
                || block instanceof Dropper
                || block instanceof Dispenser)
            return 27;

        return -1;
    }

    @Override
    public FillTemplate getFillTemplate() {
        if (block instanceof Furnace
                || block instanceof Hopper
                || block instanceof BrewingStand
                || block instanceof Dropper
                || block instanceof Dispenser)
            return FillTemplate.FILL;

        return null;
    }

    @RequiredArgsConstructor
    public class ItemButton extends Button {

        private final ItemStack item;

        @Override
        public ItemStack getItem(Player player) {
            return item;
        }
    }
}
