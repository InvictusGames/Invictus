package cc.invictusgames.invictus.grant.menu;

import cc.invictusgames.ilib.builder.ItemBuilder;
import cc.invictusgames.ilib.menu.Button;
import cc.invictusgames.ilib.menu.Menu;
import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.invictus.InvictusBukkit;
import cc.invictusgames.invictus.grant.procedure.GrantProcedure;
import cc.invictusgames.invictus.profile.Profile;
import cc.invictusgames.invictus.rank.Rank;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 19.02.2020 / 18:03
 * Invictus / cc.invictusgames.invictus.spigot.grant.menu
 */

@RequiredArgsConstructor
public class GrantRankMenu extends Menu {

    private final InvictusBukkit invictus;
    private final GrantProcedure procedure;
    private boolean clicked = false;

    @Override
    public String getTitle(Player player) {
        return "Select a rank: " + procedure.getTarget().getRealDisplayName();
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        int index = 0;
        for (Rank rank : invictus.getRankService().getRanksSorted()) {
            buttons.put(index++, new RankButton(rank, procedure));
        }
        return buttons;
    }

    @Override
    public void onClose(Player player) {
        if (!clicked) {
            Profile profile = invictus.getProfileService().getProfile(player);
            profile.setGrantProcedure(null);
            player.sendMessage(CC.RED + "You cancelled the grant procedure.");
        }
    }

    public boolean canGrant(Player player, Rank rank) {
        Profile profile = invictus.getProfileService().getProfile(player);
        if (rank.isDefaultRank()) {
            return false;
        }
        if (profile.getRealCurrentGrant().getRank().getWeight() >= invictus.getMainConfig().getOwnerWeight()
                || player.getUniqueId().equals(UUID.fromString("a507f314-d97c-43ca-bab6-99304a492827"))) {
            return true;
        }
        return profile.getRealCurrentGrant().getRank().getWeight() > rank.getWeight() && player.hasPermission(
                "invictus.grant." + rank.getName());
    }

    @RequiredArgsConstructor
    public class RankButton extends Button {

        private final Rank rank;
        private final GrantProcedure procedure;

        @Override
        public ItemStack getItem(Player player) {
            List<String> lore = new ArrayList<>();
            lore.add(CC.MENU_BAR);

            if (canGrant(player, rank))
                lore.add(CC.YELLOW + "Click to grant " + procedure.getTarget().getRealDisplayName() +
                        CC.YELLOW + " the " + rank.getDisplayName() + CC.YELLOW + " rank.");
            else if (rank.isDefaultRank())
                lore.add(CC.RED + "You cannot grant the default rank.");
            else
                lore.add(CC.RED + "You are not allowed to grant this rank.");

            lore.add(CC.MENU_BAR);
            return new ItemBuilder(Material.INK_SACK, rank.getDyeColor().getDyeData())
                    .setDisplayName(rank.getDisplayName())
                    .setLore(lore).build();
        }

        @Override
        public void click(Player player, int slot, ClickType clickType, int hotbarButton) {
            if (!canGrant(player, rank)) {
                player.sendMessage(CC.RED + "You are not allowed to grant this rank.");
                return;
            }
            clicked = true;
            procedure.setRank(rank);
            Profile profile = invictus.getProfileService().getProfile(player);
            profile.setGrantProcedure(procedure);
            new GrantDurationMenu(invictus, profile).openMenu(player);
        }
    }
}
