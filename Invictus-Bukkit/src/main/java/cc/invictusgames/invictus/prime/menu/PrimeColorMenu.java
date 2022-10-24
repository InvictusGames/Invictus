package cc.invictusgames.invictus.prime.menu;

import cc.invictusgames.ilib.builder.ItemBuilder;
import cc.invictusgames.ilib.menu.Button;
import cc.invictusgames.ilib.menu.Menu;
import cc.invictusgames.ilib.menu.fill.FillTemplate;
import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.invictus.Invictus;
import cc.invictusgames.invictus.playersetting.InvictusSettings;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 29.10.2020 / 20:55
 * Invictus / cc.invictusgames.invictus.spigot.prime.menu
 */

@RequiredArgsConstructor
public class PrimeColorMenu extends Menu {

    private final Invictus invictus;

    @Override
    public String getTitle(Player player) {
        return "Prime Icon Color";
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        buttons.put(2, new ColorButton(ChatColor.WHITE));
        buttons.put(3, new ColorButton(ChatColor.GRAY));
        buttons.put(4, new ColorButton(ChatColor.DARK_GRAY));
        buttons.put(5, new ColorButton(ChatColor.YELLOW));
        buttons.put(6, new ColorButton(ChatColor.GOLD));
        buttons.put(11, new ColorButton(ChatColor.GREEN));
        buttons.put(12, new ColorButton(ChatColor.DARK_GREEN));
        buttons.put(13, new ColorButton(ChatColor.LIGHT_PURPLE));
        buttons.put(14, new ColorButton(ChatColor.DARK_PURPLE));
        buttons.put(15, new ColorButton(ChatColor.AQUA));
        buttons.put(20, new ColorButton(ChatColor.DARK_AQUA));
        buttons.put(21, new ColorButton(ChatColor.RED));
        buttons.put(22, new ColorButton(ChatColor.DARK_RED));
        buttons.put(23, new ColorButton(ChatColor.BLUE));
        buttons.put(24, new ColorButton(ChatColor.DARK_BLUE));
        return buttons;
    }

    @Override
    public FillTemplate getFillTemplate() {
        return FillTemplate.FILL;
    }

    public static DyeColor convertToDyeColor(ChatColor color) {
        switch (color) {
            case GREEN:
                return DyeColor.LIME;
            case AQUA:
                return DyeColor.LIGHT_BLUE;
            case RED:
            case DARK_RED:
                return DyeColor.RED;
            case LIGHT_PURPLE:
                return DyeColor.MAGENTA;
            case YELLOW:
                return DyeColor.YELLOW;
            case WHITE:
                return DyeColor.WHITE;
            case DARK_BLUE:
            case BLUE:
                return DyeColor.BLUE;
            case DARK_GREEN:
                return DyeColor.GREEN;
            case DARK_AQUA:
                return DyeColor.CYAN;
            case DARK_PURPLE:
                return DyeColor.PURPLE;
            case GOLD:
                return DyeColor.ORANGE;
            case GRAY:
                return DyeColor.SILVER;
            case DARK_GRAY:
                return DyeColor.GRAY;
            case BLACK:
            default:
                return DyeColor.BLACK;
        }
    }

    @RequiredArgsConstructor
    public class ColorButton extends Button {

        private final ChatColor color;

        @Override
        public ItemStack getItem(Player player) {
            return new ItemBuilder(Material.WOOL, convertToDyeColor(color).getWoolData())
                    .setDisplayName(color.toString() + CC.BOLD
                            + WordUtils.capitalizeFully(color.name().replace("_", " ")))
                    .setLore(
                            " ",
                            CC.YELLOW + "Preview: " + color.toString() + Invictus.PRIME_ICON
                    ).build();
        }

        @Override
        public void click(Player player, int slot, ClickType clickType, int hotbarButton) {
            InvictusSettings.PRIME_COLOR.set(player, color);
            player.sendMessage(CC.GREEN + "Your prime icon now looks like "
                    + color.toString() + Invictus.PRIME_ICON + CC.GREEN + ".");
        }
    }

}
