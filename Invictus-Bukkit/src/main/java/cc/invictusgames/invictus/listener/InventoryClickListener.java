package cc.invictusgames.invictus.listener;

import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.invictus.base.StaffMode;
import cc.invictusgames.invictus.utils.ProfileInventory;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 22.06.2020 / 21:01
 * Invictus / cc.invictusgames.invictus.spigot.listener
 */

public class InventoryClickListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (ProfileInventory.getOpen().contains(player.getUniqueId())) {

            if (event.getClickedInventory() == null)
                return;

            if ((event.getRawSlot() >= 40) && (event.getRawSlot() <= 44)) {
                event.setCancelled(true);
                return;
            }

            if (!player.hasPermission("invictus.invsee.edit")) {
                event.setCancelled(true);
                player.sendMessage(CC.RED + "You are not allowed to edit inventories.");
                return;
            }
        }

        if (StaffMode.getOpenInventories().contains(player.getUniqueId())) {
            if (event.getClickedInventory() == null)
                return;

            if (!player.hasPermission("invictus.invsee.edit")) {
                event.setCancelled(true);
                player.sendMessage(CC.RED + "You are not allowed to edit inventories.");
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        StaffMode.getOpenInventories().remove(event.getPlayer().getUniqueId());
    }

}
