package cc.invictusgames.invictus.base.menu.staffmode;

import cc.invictusgames.ilib.builder.ItemBuilder;
import cc.invictusgames.ilib.menu.Button;
import cc.invictusgames.ilib.menu.Menu;
import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.ilib.utils.TimeUtils;
import cc.invictusgames.invictus.Invictus;
import cc.invictusgames.invictus.profile.Profile;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
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
 * 20.04.2020 / 15:53
 * Invictus / cc.invictusgames.invictus.spigot.base.menu.staffmode
 */

@RequiredArgsConstructor
public class ExamineMenu extends Menu {

    private final Invictus invictus;
    private final Profile target;

    @Override
    public String getTitle(Player player) {
        return "Inventory: " + target.getDisplayName(player);
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        if ((target.player() == null) || (!target.player().isOnline())) {
            player.closeInventory();
            return new HashMap<>();
        }

        Map<Integer, Button> buttons = new HashMap<>();
        for (int index = 0; index < target.player().getInventory().getContents().length; index++) {
            buttons.put(index, new ItemButton(target.player().getInventory().getContents()[index]));
        }
        buttons.put(36, new ItemButton(target.player().getInventory().getHelmet()));
        buttons.put(37, new ItemButton(target.player().getInventory().getChestplate()));
        buttons.put(38, new ItemButton(target.player().getInventory().getLeggings()));
        buttons.put(39, new ItemButton(target.player().getInventory().getBoots()));

        buttons.put(40, Button.createPlaceholder());
        buttons.put(41, Button.createPlaceholder());

        int offset = player.hasPermission("invictus.command.clear.other") ? 0 : 1;
        buttons.put(42 + offset, new HealthButton());
        buttons.put(43 + offset, new EffectButton());
        if (player.hasPermission("invictus.command.clear.other")) {
            buttons.put(44, new ClearButton());
        }
        return buttons;
    }

    @RequiredArgsConstructor
    public class ItemButton extends Button {

        private final ItemStack item;

        @Override
        public ItemStack getItem(Player player) {
            return item;
        }
    }

    public class HealthButton extends Button {

        @Override
        public ItemStack getItem(Player player) {
            return new ItemBuilder(Material.INK_SACK, DyeColor.RED.getDyeData())
                    .setDisplayName(CC.GOLD + CC.BOLD + "Player's Health")
                    .setAmount((int) target.player().getHealth())
                    .build();
        }
    }

    public class EffectButton extends Button {

        @Override
        public ItemStack getItem(Player player) {
            List<String> lore = new ArrayList<>();
            target.player().getActivePotionEffects().forEach(effect -> {
                String effectName = WordUtils.capitalizeFully(effect.getType().getName().replace("_", " "));
                String effectDuration = TimeUtils.formatHHMMSS(effect.getDuration() / 20);
                lore.add(CC.GOLD + effectName + " " + (effect.getAmplifier() + 1) + ": " + CC.WHITE + effectDuration);
            });

            return new ItemBuilder(Material.BLAZE_POWDER)
                    .setDisplayName(CC.GOLD + CC.BOLD + "Active Effects")
                    .setLore(lore)
                    .build();
        }
    }

    public class ClearButton extends Button {

        @Override
        public ItemStack getItem(Player player) {
            return new ItemBuilder(Material.BOOK_AND_QUILL)
                    .setDisplayName(CC.GOLD + CC.BOLD + "Clear Inventory")
                    .build();
        }

        @Override
        public void click(Player player, int slot, ClickType clickType, int hotbarButton) {
            Bukkit.dispatchCommand(player, "clear " + target.getName());
        }
    }
}
