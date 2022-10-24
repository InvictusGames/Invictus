package cc.invictusgames.invictus.punishment.menu;

import cc.invictusgames.ilib.builder.ItemBuilder;
import cc.invictusgames.ilib.menu.Button;
import cc.invictusgames.ilib.menu.Menu;
import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.invictus.InvictusBukkit;
import cc.invictusgames.invictus.profile.Profile;
import cc.invictusgames.invictus.punishment.Punishment;
import lombok.RequiredArgsConstructor;
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
 * 22.02.2020 / 19:03
 * Invictus / cc.invictusgames.invictus.spigot.punishment.menu
 */

@RequiredArgsConstructor
public class PunishmentsMainMenu extends Menu {

    private final InvictusBukkit invictus;
    private final Profile target;

    @Override
    public String getTitle(Player player) {
        return "Punishments: " + target.getRealDisplayName();
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        if (player.hasPermission("invictus.punishments.viewblacklists")
                || (target.getActivePunishment(Punishment.PunishmentType.BLACKLIST) != null
                && player.hasPermission("invictus.punishments.viewblacklist.light"))) {
            buttons.put(1, new PunishmentButton(Punishment.PunishmentType.WARN));
            buttons.put(3, new PunishmentButton(Punishment.PunishmentType.MUTE));
            buttons.put(5, new PunishmentButton(Punishment.PunishmentType.BAN));
            if (player.hasPermission("invictus.punishments.viewblacklists"))
                buttons.put(7, new PunishmentButton(Punishment.PunishmentType.BLACKLIST));
            else
                buttons.put(7, new BlacklistNoPermissionButton());
        } else if (player.hasPermission("invictus.punishments.fullview")){
            buttons.put(1, new PunishmentButton(Punishment.PunishmentType.WARN));
            buttons.put(4, new PunishmentButton(Punishment.PunishmentType.MUTE));
            buttons.put(7, new PunishmentButton(Punishment.PunishmentType.BAN));
        } else
            buttons.put(4, new PunishmentButton(Punishment.PunishmentType.MUTE));
        return buttons;
    }

    @RequiredArgsConstructor
    public class PunishmentButton extends Button {

        private final Punishment.PunishmentType type;

        @Override
        public ItemStack getItem(Player player) {
            short subId = 0;
            if (type.equals(Punishment.PunishmentType.BLACKLIST)) {
                subId = DyeColor.BLACK.getWoolData();
            } else if (type.equals(Punishment.PunishmentType.BAN)) {
                subId = DyeColor.RED.getWoolData();
            } else if (type.equals(Punishment.PunishmentType.MUTE)) {
                subId = DyeColor.ORANGE.getWoolData();
            } else if (type.equals(Punishment.PunishmentType.WARN)) {
                subId = DyeColor.YELLOW.getWoolData();
            }
            List<String> lore = new ArrayList<>();
            lore.add(CC.MENU_BAR);
            lore.add(CC.YELLOW + "Total " + type.getName() + "s: " + CC.RED + target.getPunishments(type).size());
            if ((type.equals(Punishment.PunishmentType.BAN)) || (type.equals(Punishment.PunishmentType.BLACKLIST)) ||
                    (type.equals(Punishment.PunishmentType.MUTE))) {
                lore.add(CC.YELLOW + "Currently " + type.getContext() + ": " +
                        CC.colorBoolean(target.getActivePunishment(type) != null, "Yes", "No"));
            }
            lore.add(CC.MENU_BAR);
            return new ItemBuilder(Material.WOOL, subId)
                    .setDisplayName(CC.RED + CC.BOLD + type.getName() + "s")
                    .setLore(lore)
                    .build();
        }

        @Override
        public void click(Player player, int slot, ClickType clickType, int hotbarButton) {
            player.sendMessage(CC.format("&eLoading %ss of %s...", type.getName(), target.getName()));
            new PunishmentsMenu(invictus, target, type).openMenu(player);
        }
    }

    public class BlacklistNoPermissionButton extends Button {

        @Override
        public ItemStack getItem(Player player) {
            return new ItemBuilder(Material.WOOL, DyeColor.BLACK.getWoolData())
                    .setDisplayName(CC.RED + CC.BOLD + "Blacklists")
                    .setLore(CC.MENU_BAR,
                            CC.YELLOW + "This player has 1 punishment",
                            CC.YELLOW + "you are not permitted to view.",
                            CC.MENU_BAR)
                    .build();
        }
    }
}
