package cc.invictusgames.invictus.tag.menu;

import cc.invictusgames.ilib.builder.ItemBuilder;
import cc.invictusgames.ilib.menu.Button;
import cc.invictusgames.ilib.menu.Menu;
import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.invictus.InvictusBukkit;
import cc.invictusgames.invictus.profile.Profile;
import cc.invictusgames.invictus.tag.Tag;
import lombok.RequiredArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class TagMenu extends Menu {

    private static final InvictusBukkit invictus = InvictusBukkit.getBukkitInstance();

    @Override
    public String getTitle(Player player) {
        return "Tags";
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        invictus.getTagService().getTagList().forEach(tag ->
                buttons.put(buttons.size(), new TagButton(tag)));

        int size = calculateSize(buttons) - 1;
        int slot = buttons.get(size) == null ? size : size + 9;
        buttons.put(slot, new ResetTagButton());
        return buttons;
    }

    @RequiredArgsConstructor
    public class TagButton extends Button {

        private final Tag tag;

        @Override
        public ItemStack getItem(Player player) {
            ItemBuilder builder = new ItemBuilder(Material.NAME_TAG)
                    .setDisplayName(tag.getDisplayName())
                    .setLore(
                            CC.MENU_BAR,
                            (player.hasPermission("invictus.tag." + tag.getName())
                                    ? ChatColor.GREEN + "Click to apply the "
                                    + ChatColor.WHITE + tag.getName() + ChatColor.GREEN + " tag."
                                    : ChatColor.RED + "You do not have access to this tag."),
                            CC.MENU_BAR
                    );

            if (player.isOp())
                builder.addToLore(CC.DGRAY + "invictus.tag." + tag.getName());

            return builder.build();
        }

        @Override
        public void click(Player player, int slot, ClickType clickType, int hotbarButton) {
            if (!player.hasPermission("invictus.tag." + tag.getName())) {
                player.sendMessage(ChatColor.RED + "You do not have access to apply this tag.");
                player.closeInventory();
                return;
            }

            Profile profile = invictus.getProfileService().getProfile(player.getUniqueId());
            profile.setActiveTag(tag);
            profile.save(() -> {}, true);
            player.closeInventory();

            player.sendMessage(ChatColor.YELLOW + "You have applied the "
                    + ChatColor.WHITE + tag.getName() + ChatColor.YELLOW + " tag.");
        }
    }

    public class ResetTagButton extends Button {

        @Override
        public ItemStack getItem(Player player) {
            return new ItemBuilder(Material.REDSTONE)
                    .setDisplayName(CC.RED + CC.BOLD + "Reset Tag")
                    .build();
        }

        @Override
        public void click(Player player, int slot, ClickType clickType, int hotbarButton) {
            Profile profile = invictus.getProfileService().getProfile(player.getUniqueId());
            profile.setActiveTag(null);
            profile.save(() -> {}, true);
            player.closeInventory();
            player.sendMessage(CC.YELLOW + "You have reset your tag.");
        }
    }
}
