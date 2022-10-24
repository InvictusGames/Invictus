package cc.invictusgames.invictus.banphrase.menu.add;

import cc.invictusgames.ilib.builder.ItemBuilder;
import cc.invictusgames.ilib.menu.Button;
import cc.invictusgames.ilib.menu.Menu;
import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.invictus.banphrase.Banphrase;
import cc.invictusgames.invictus.banphrase.input.add.BanphrasePhraseInput;
import cc.invictusgames.invictus.banphrase.procedure.BanphraseAddProcedure;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 16.06.2021 / 20:56
 * Invictus / cc.invictusgames.invictus.banphrase.menu.add
 */

@RequiredArgsConstructor
public class SelectBanphraseOperatorMenu extends Menu {

    private final BanphraseAddProcedure procedure;

    @Override
    public String getTitle(Player player) {
        return "Select operator";
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        for (Banphrase.BanphraseOperator operator : Banphrase.BanphraseOperator.values()) {
            buttons.put(buttons.size(), new OperatorButton(operator));
        }
        return buttons;
    }

    @RequiredArgsConstructor
    public class OperatorButton extends Button {

        private final Banphrase.BanphraseOperator operator;

        @Override
        public ItemStack getItem(Player player) {
            return new ItemBuilder(Material.PAPER)
                    .setDisplayName(CC.YELLOW + CC.BOLD + operator.getDisplay())
                    .setLore(CC.YELLOW + operator.getDescription())
                    .build();
        }

        @Override
        public void click(Player player, int slot, ClickType clickType, int hotbarButton) {
            procedure.setOperator(operator);
            player.getOpenInventory().close();
            new BanphrasePhraseInput(procedure).send(player);
        }
    }
}
