package cc.invictusgames.invictus.punishment.template.menu;

import cc.invictusgames.ilib.menu.Button;
import cc.invictusgames.ilib.menu.Menu;
import cc.invictusgames.invictus.profile.Profile;
import cc.invictusgames.invictus.punishment.template.PunishmentTemplate;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class OffenseMenu extends Menu {

    private final Profile target;
    private final PunishmentTemplate template;

    @Override
    public String getTitle(Player player) {
        return "Select a offense: " + template.getReason();
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        for (PunishmentTemplate.Offense offense : template.getOffenses()) {
            buttons.put(buttons.size(), new OffenseButton(offense));
        }

        return buttons;
    }

    @RequiredArgsConstructor
    public class OffenseButton extends Button {

        private final PunishmentTemplate.Offense offense;

        @Override
        public ItemStack getItem(Player player) {
            return offense.toItem();
        }

        @Override
        public void click(Player player, int slot, ClickType clickType, int hotbarButton) {
            offense.execute(player, template.getReason(), target.getName(), true);
            player.closeInventory();
        }
    }
}
