package cc.invictusgames.invictus.rank.menu;

import cc.invictusgames.ilib.builder.ItemBuilder;
import cc.invictusgames.ilib.chatinput.ChatInputChain;
import cc.invictusgames.ilib.menu.Button;
import cc.invictusgames.ilib.menu.Menu;
import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.invictus.Invictus;
import cc.invictusgames.invictus.InvictusBukkit;
import cc.invictusgames.invictus.profile.Profile;
import cc.invictusgames.invictus.rank.Rank;
import cc.invictusgames.invictus.rank.setup.*;
import lombok.RequiredArgsConstructor;
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
 * 20.06.2020 / 18:43
 * Invictus / cc.invictusgames.invictus.spigot.rank.menu
 */

@RequiredArgsConstructor
public class RankEditOverviewMenu extends Menu {

    private static final ChatInputChain SETUP_CHAIN = new ChatInputChain()
            .next(new NamePrompt())
            .next(new ColorPrompt(Invictus.getInstance()))
            .next(new PrefixPrompt(Invictus.getInstance()))
            .next(new WeightPrompt(Invictus.getInstance()))
            .next(new QueuePriorityPrompt(Invictus.getInstance()));

    private final InvictusBukkit invictus;
    private final Profile profile;

    @Override
    public String getTitle(Player player) {
        return "Rank Editor";
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        invictus.getRankService().getRanksSorted().forEach(rank -> buttons.put(buttons.size(), new RankButton(rank)));
        buttons.put(buttons.size(), new SetupRankButton());
        return buttons;
    }

    @RequiredArgsConstructor
    public class RankButton extends Button {

        private final Rank rank;

        @Override
        public ItemStack getItem(Player player) {
            List<String> lore = new ArrayList<>();
            lore.add(CC.MENU_BAR);
            lore.add(CC.format("&eColor: %sExample", rank.getColor()));
            lore.add(CC.format("&eChat Color: %sExample", rank.getChatColor()));
            lore.add(" ");
            lore.add(CC.format("&ePrefix: %sExample", rank.getPrefix()));
            lore.add(CC.format("&eSuffix: &fExample%s", rank.getSuffix()));
            lore.add(" ");
            lore.add(CC.format("&eWeight: &c%d", rank.getWeight()));
            lore.add(CC.format("&eQueue Priority: &c%d", rank.getQueuePriority()));
            lore.add(" ");
            lore.add(CC.format("&eDefault: %s",
                    CC.colorBoolean(rank.isDefaultRank(), "true", "false")));
            lore.add(CC.format("&eDisguisable: %s",
                    CC.colorBoolean(rank.isDisguisable(), "true", "false")));
            lore.add(" ");
            lore.add(CC.format("&eInherits: &e%s", rank.getInherits().isEmpty() ? "None" : ""));
            if (!rank.getInherits().isEmpty()) {
                rank.getInherits().forEach(inherit -> lore.add(CC.GRAY + " - " + inherit.getDisplayName()));
            }
            lore.add(" ");
            lore.add(CC.format("&ePermissions: &c%d", rank.getPermissions().size()));
            lore.add(CC.format("&eLocal Permissions: &c%d", rank.getLocalPermissions().size()));
            lore.add(CC.format("&eInherited Permissions: &c%d", rank.getInheritPermissions().size()));
            lore.add(CC.MENU_BAR);

            return new ItemBuilder(Material.INK_SACK, rank.getDyeColor().getDyeData())
                    .setDisplayName(rank.getDisplayName())
                    .setLore(lore)
                    .build();
        }

        @Override
        public void click(Player player, int slot, ClickType clickType, int hotbarButton) {
            new RankEditingMenu(invictus, profile, rank).openMenu(player);
        }
    }

    public class SetupRankButton extends Button {

        @Override
        public ItemStack getItem(Player player) {
            return new ItemBuilder(Material.EMERALD)
                    .setDisplayName(CC.GREEN + CC.BOLD + "Setup new rank")
                    .build();
        }

        @Override
        public void click(Player player, int slot, ClickType clickType, int hotbarButton) {
            player.getOpenInventory().close();
            SETUP_CHAIN.start(player);
        }
    }
}
