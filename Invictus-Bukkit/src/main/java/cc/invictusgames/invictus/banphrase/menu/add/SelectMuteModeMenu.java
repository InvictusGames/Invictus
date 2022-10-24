package cc.invictusgames.invictus.banphrase.menu.add;

import cc.invictusgames.ilib.builder.ItemBuilder;
import cc.invictusgames.ilib.menu.Button;
import cc.invictusgames.ilib.menu.Menu;
import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.invictus.banphrase.Banphrase;
import cc.invictusgames.invictus.banphrase.input.add.BanphraseDurationInput;
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
 * 16.06.2021 / 21:00
 * Invictus / cc.invictusgames.invictus.banphrase.menu.add
 */

@RequiredArgsConstructor
public class SelectMuteModeMenu extends Menu {

    private final BanphraseAddProcedure procedure;

    @Override
    public String getTitle(Player player) {
        return "Select mute mode";
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        for (Banphrase.MuteMode muteMode : Banphrase.MuteMode.values()) {
            buttons.put(buttons.size(), new MuteModeButton(muteMode));
        }
        return buttons;
    }

    @RequiredArgsConstructor
    public class MuteModeButton extends Button {

        private final Banphrase.MuteMode muteMode;

        @Override
        public ItemStack getItem(Player player) {
            return new ItemBuilder(Material.PAPER)
                    .setDisplayName(CC.YELLOW + CC.BOLD + muteMode.getDisplay())
                    .setLore(CC.YELLOW + muteMode.getDescription())
                    .build();
        }

        @Override
        public void click(Player player, int slot, ClickType clickType, int hotbarButton) {
            procedure.setMuteMode(muteMode);
            player.getOpenInventory().close();
            if (muteMode != Banphrase.MuteMode.NONE)
                new BanphraseDurationInput(procedure).send(player);
            else procedure.finish();
        }
    }
}
