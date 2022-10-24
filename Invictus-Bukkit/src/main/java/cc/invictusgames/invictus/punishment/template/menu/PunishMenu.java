package cc.invictusgames.invictus.punishment.template.menu;

import cc.invictusgames.ilib.menu.Button;
import cc.invictusgames.ilib.menu.Menu;
import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.invictus.InvictusBukkit;
import cc.invictusgames.invictus.profile.Profile;
import cc.invictusgames.invictus.punishment.template.PunishmentTemplate;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class PunishMenu extends Menu {

    private final Profile target;

    @Override
    public String getTitle(Player player) {
        return "Select a punishment: " + target.getRealDisplayName();
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        int slot = 0;

        for (PunishmentTemplate template :
                InvictusBukkit.getBukkitInstance().getBukkitPunishmentService().getBanTemplates())
            buttons.put(slot++, new TemplateButton(template));

        while (slot % 9 != 0)
            slot++;

        for (PunishmentTemplate template :
                InvictusBukkit.getBukkitInstance().getBukkitPunishmentService().getMuteTemplates())
            buttons.put(slot++, new TemplateButton(template));

        return buttons;
    }

    @RequiredArgsConstructor
    public class TemplateButton extends Button {

        private final PunishmentTemplate template;

        @Override
        public ItemStack getItem(Player player) {
            return template.toItem();
        }

        @Override
        public void click(Player player, int slot, ClickType clickType, int hotbarButton) {
            if (template.getOffenses().isEmpty()) {
                player.sendMessage(CC.RED + "No offenses found.");
                return;
            }

            if (template.getOffenses().size() == 1) {
                PunishmentTemplate.Offense offense = template.getOffenses().get(0);
                offense.execute(player, template.getReason(), target.getName(), false);
                player.closeInventory();
                return;
            }

            new OffenseMenu(target, template).openMenu(player);
        }
    }
}
