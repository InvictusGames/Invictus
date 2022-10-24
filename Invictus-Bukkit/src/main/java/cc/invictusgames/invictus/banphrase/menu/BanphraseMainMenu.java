package cc.invictusgames.invictus.banphrase.menu;

import cc.invictusgames.ilib.builder.ItemBuilder;
import cc.invictusgames.ilib.menu.Button;
import cc.invictusgames.ilib.menu.page.PagedMenu;
import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.ilib.utils.TimeUtils;
import cc.invictusgames.invictus.InvictusBukkit;
import cc.invictusgames.invictus.banphrase.Banphrase;
import cc.invictusgames.invictus.banphrase.input.add.BanphraseNameInput;
import cc.invictusgames.invictus.banphrase.menu.edit.BanphraseEditMenu;
import cc.invictusgames.invictus.banphrase.procedure.BanphraseAddProcedure;
import lombok.RequiredArgsConstructor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 16.06.2021 / 20:33
 * Invictus / cc.invictusgames.invictus.banphrase.menu
 */

@RequiredArgsConstructor
public class BanphraseMainMenu extends PagedMenu {

    private final InvictusBukkit invictus;

    @Override
    public Map<Integer, Button> getAllPagesButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        List<Banphrase> banphrases = new ArrayList<>(invictus.getBanphraseService().getBanphrases());
        banphrases.sort(Comparator.comparing(Banphrase::getName));
        banphrases.forEach(banphrase -> buttons.put(buttons.size(), new BanphraseButton(banphrase)));
        return buttons;
    }

    @Override
    public Map<Integer, Button> getGlobalButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        buttons.put(4, new AddBanphraseButton());
        return buttons;
    }

    @Override
    public String getRawTitle(Player player) {
        return "Banphrases";
    }

    @RequiredArgsConstructor
    public class BanphraseButton extends Button {

        private final Banphrase banphrase;

        @Override
        public ItemStack getItem(Player player) {
            List<String> lore = new ArrayList<>();
            lore.add(CC.MENU_BAR);
            lore.add(CC.format("&ePhrase: &c%s", banphrase.getPhrase()));
            lore.add(CC.format("&eOperator: &c%s", banphrase.getOperator().getDisplay()));
            lore.add(CC.format("&eMute Mode: &c%s", banphrase.getMuteMode().getDisplay()));

            if (banphrase.getMuteMode() != Banphrase.MuteMode.NONE)
                lore.add(CC.format("&eDuration: &c%s", TimeUtils.formatTimeShort(banphrase.getDuration())));

            lore.add(CC.format("&eCase sensitive: %s",
                    CC.colorBoolean(banphrase.isCaseSensitive(), "yes", "no")));
            lore.add(CC.MENU_BAR);

            return new ItemBuilder(banphrase.isEnabled() ? Material.PAPER : Material.EMPTY_MAP)
                    .setDisplayName(CC.YELLOW + CC.BOLD + banphrase.getName())
                    .setLore(lore)
                    .build();
        }

        @Override
        public void click(Player player, int slot, ClickType clickType, int hotbarButton) {
            new BanphraseEditMenu(invictus, banphrase).openMenu(player);
        }
    }

    public class AddBanphraseButton extends Button {

        @Override
        public ItemStack getItem(Player player) {
            return new ItemBuilder(Material.WOOL, DyeColor.LIME.getWoolData())
                    .setDisplayName(CC.GREEN + CC.BOLD + "Add Banphrase")
                    .build();
        }

        @Override
        public void click(Player player, int slot, ClickType clickType, int hotbarButton) {
            player.getOpenInventory().close();
            BanphraseAddProcedure procedure =
                    new BanphraseAddProcedure(invictus.getProfileService().getProfile(player));
            new BanphraseNameInput(procedure).send(player);
        }
    }
}
