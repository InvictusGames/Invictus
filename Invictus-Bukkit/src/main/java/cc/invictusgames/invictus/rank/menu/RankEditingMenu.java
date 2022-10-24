package cc.invictusgames.invictus.rank.menu;

import cc.invictusgames.ilib.builder.ItemBuilder;
import cc.invictusgames.ilib.chatinput.ChatInput;
import cc.invictusgames.ilib.menu.Button;
import cc.invictusgames.ilib.menu.Menu;
import cc.invictusgames.ilib.menu.buttons.BackButton;
import cc.invictusgames.ilib.menu.fill.FillTemplate;
import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.invictus.InvictusBukkit;
import cc.invictusgames.invictus.profile.Profile;
import cc.invictusgames.invictus.rank.Rank;
import lombok.RequiredArgsConstructor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 20.06.2020 / 16:49
 * Invictus / cc.invictusgames.invictus.spigot.rank.menu
 */

@RequiredArgsConstructor
public class RankEditingMenu extends Menu {

    public static final Map<UUID, UUID> RANK_SETUPS = new HashMap<>();

    private final InvictusBukkit invictus;
    private final Profile profile;
    private final Rank rank;
    private boolean save = false;

    @Override
    public String getTitle(Player player) {
        return "Editing: " + rank.getDisplayName();
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        buttons.put(10, new SetWeightButton());
        buttons.put(11, new AddPermissionButton(false));
        buttons.put(12, new AddPermissionButton(true));
        buttons.put(13, new ToggleInheritButton());
        buttons.put(14, new SetPrefixButton());
        buttons.put(15, new SetColorButton());
        buttons.put(16, new ToggleDisguisableButton());

        buttons.put(19, new SetQueuePriorityButton());
        buttons.put(20, new RemovePermissionButton(false));
        buttons.put(21, new RemovePermissionButton(true));
        buttons.put(23, new SetSuffixButton());
        buttons.put(24, new SetChatColorButton());

        /*buttons.put(10, new SetDefaultButton());
        buttons.put(11, new AddPermissionButton(false));
        buttons.put(12, new AddPermissionButton(true));
        buttons.put(14, new SetPrefixButton());
        buttons.put(15, new SetColorButton());
        buttons.put(16, new ToggleInheritButton());

        buttons.put(19, new ToggleDisguisableButton());
        buttons.put(20, new RemovePermissionButton(false));
        buttons.put(21, new RemovePermissionButton(true));
        buttons.put(23, new SetSuffixButton());
        buttons.put(24, new SetChatColorButton());
        buttons.put(25, new SetWeightButton());*/

        buttons.put(35, new BackButton(new RankEditOverviewMenu(invictus, profile)));
        return buttons;
    }

    @Override
    public int getSize() {
        return 36;
    }

    @Override
    public boolean isClickUpdate() {
        return true;
    }

    @Override
    public FillTemplate getFillTemplate() {
        return FillTemplate.FILL;
    }

    @Override
    public void onClose(Player player) {
        if (save) {
            rank.save(player, () -> {});
        }
    }

    @RequiredArgsConstructor
    public class AddPermissionButton extends Button {

        private final boolean local;

        @Override
        public ItemStack getItem(Player player) {
            return new ItemBuilder(local ? Material.WOOD_BUTTON : Material.STONE_BUTTON)
                    .setDisplayName(CC.format("&e&lAdd %sPermission", local ? "local " : ""))
                    .setLore(CC.format(
                            "&e%sPermissions: &c%d",
                            local ? "Local " : "",
                            local ? rank.getLocalPermissions().size() : rank.getPermissions().size()
                    )).build();
        }

        @Override
        public void click(Player whoClicked, int slot, ClickType clickType, int hotbarButton) {
            whoClicked.getOpenInventory().close();
            new ChatInput<String>(String.class)
                    .text(CC.translate("&ePlease enter the permission you would like to add, " +
                            "or say &ccancel &eto cancel."))
                    .escapeMessage(CC.RED + "You cancelled the permission adding process.")
                    .onCancel(RankEditingMenu.this::openMenu)
                    .accept((player, input) -> {
                        if (local) {
                            if (rank.getLocalPermissions().contains(input.toLowerCase())) {
                                player.sendMessage(CC.format("&cRank &e%s &calready has permission &e%s&c.",
                                        rank.getName(), input));
                                return true;
                            }

                            rank.getLocalPermissions().add(input.toLowerCase());
                        } else {
                            if (rank.getPermissions().contains(input.toLowerCase())) {
                                player.sendMessage(CC.format("&cRank &e%s &calready has permission &e%s&c.",
                                        rank.getName(), input));
                                return true;
                            }

                            rank.getPermissions().add(input.toLowerCase());
                        }

                        rank.save(player, () -> {});
                        player.sendMessage(CC.format("&eYou added permission &c%s &eto rank %s&e.",
                                input, rank.getDisplayName()));
                        openMenu(player);
                        return true;
                    }).send(whoClicked);
        }
    }

    @RequiredArgsConstructor
    public class RemovePermissionButton extends Button {

        private final boolean local;

        @Override
        public ItemStack getItem(Player player) {
            return new ItemBuilder(local ? Material.WOOD_BUTTON : Material.STONE_BUTTON)
                    .setDisplayName(CC.format("&e&lRemove %sPermission", local ? "local " : ""))
                    .setLore(CC.format(
                            "&e%sPermissions: &c%d",
                            local ? "Local " : "",
                            local ? rank.getLocalPermissions().size() : rank.getPermissions().size()
                    )).build();
        }

        @Override
        public void click(Player whoClicked, int slot, ClickType clickType, int hotbarButton) {
            whoClicked.getOpenInventory().close();
            new ChatInput<String>(String.class)
                    .text(CC.translate("&ePlease enter the permission you would like to remove, " +
                            "or say &ccancel &eto cancel."))
                    .escapeMessage(CC.RED + "You cancelled the permission removal process.")
                    .onCancel(RankEditingMenu.this::openMenu)
                    .accept((player, input) -> {
                        if (local) {
                            if (!rank.getLocalPermissions().contains(input.toLowerCase())) {
                                player.sendMessage(CC.format("&cRank &e%s &cdoesn't have permission &e%s&c.",
                                        rank.getName(), input));
                                return true;
                            }
                            rank.getLocalPermissions().remove(input.toLowerCase());
                        } else {
                            if (!rank.getPermissions().contains(input.toLowerCase())) {
                                player.sendMessage(CC.format("&cRank &e%s &cdoesn't have permission &e%s&c.",
                                        rank.getName(), input));
                                return true;
                            }
                            rank.getPermissions().remove(input.toLowerCase());
                        }

                        rank.save(player, () -> {});
                        player.sendMessage(CC.format("&eYou removed permission &c%s &efrom rank %s&e.",
                                input, rank.getDisplayName()));
                        openMenu(player);
                        return true;
                    }).send(whoClicked);
        }
    }


    public class ToggleDisguisableButton extends Button {

        @Override
        public ItemStack getItem(Player player) {
            return new ItemBuilder(Material.INK_SACK, rank.isDisguisable()
                    ? DyeColor.LIME.getDyeData() : DyeColor.GRAY.getDyeData())
                    .setDisplayName(CC.YELLOW + CC.BOLD + "Toggle Disguisable")
                    .setLore(CC.YELLOW + "Disguisable: " + CC.colorBoolean(rank.isDisguisable(), "true", "false"))
                    .build();
        }

        @Override
        public void click(Player player, int slot, ClickType clickType, int hotbarButton) {
            rank.setDisguisable(!rank.isDisguisable());
            save = true;
            player.sendMessage(CC.format("&eYou set the disguisable status of %s &eto %s&e.",
                    rank.getDisplayName(), CC.colorBoolean(rank.isDisguisable())));
        }
    }

    public class SetPrefixButton extends Button {

        @Override
        public ItemStack getItem(Player player) {
            return new ItemBuilder(Material.SIGN)
                    .setDisplayName(CC.YELLOW + CC.BOLD + "Set Prefix")
                    .setLore(CC.format("&ePrefix: %sExample", rank.getPrefix()))
                    .build();
        }

        @Override
        public void click(Player whoClicked, int slot, ClickType clickType, int hotbarButton) {
            whoClicked.getOpenInventory().close();
            new ChatInput<String>(String.class)
                    .text(CC.translate("&ePlease enter the new prefix for this rank, " +
                            "or say &ccancel &eto cancel."))
                    .escapeMessage(CC.RED + "You cancelled the prefix change.")
                    .onCancel(RankEditingMenu.this::openMenu)
                    .accept((player, input) -> {
                        rank.setPrefix(CC.translate(input));
                        rank.save(player, () -> {});
                        player.sendMessage(CC.format("&eYou set the prefix of %s &eto %sExample&e.",
                                rank.getDisplayName(), rank.getPrefix()));
                        openMenu(player);
                        return true;
                    }).send(whoClicked);
        }
    }

    public class SetSuffixButton extends Button {

        @Override
        public ItemStack getItem(Player player) {
            return new ItemBuilder(Material.SIGN)
                    .setDisplayName(CC.YELLOW + CC.BOLD + "Set Suffix")
                    .setLore(CC.format("&eSuffix: &fExample%s", rank.getSuffix()))
                    .build();
        }

        @Override
        public void click(Player whoClicked, int slot, ClickType clickType, int hotbarButton) {
            whoClicked.getOpenInventory().close();
            new ChatInput<String>(String.class)
                    .text(CC.translate("&ePlease enter the new suffix for this rank, " +
                            "or say &ccancel &eto cancel."))
                    .escapeMessage(CC.RED + "You cancelled the suffix change.")
                    .onCancel(RankEditingMenu.this::openMenu)
                    .accept((player, input) -> {
                        rank.setSuffix(CC.translate(input));
                        rank.save(player, () -> {});
                        player.sendMessage(CC.format("&eYou set the suffix of %s &eto %sExample&e.",
                                rank.getDisplayName(), rank.getSuffix()));
                        openMenu(player);
                        return true;
                    }).send(whoClicked);
        }
    }

    public class SetColorButton extends Button {

        @Override
        public ItemStack getItem(Player player) {
            return new ItemBuilder(Material.PAPER)
                    .setDisplayName(CC.YELLOW + CC.BOLD + "Set Color")
                    .setLore(CC.format("&eColor: %sExample", rank.getColor()))
                    .build();
        }

        @Override
        public void click(Player whoClicked, int slot, ClickType clickType, int hotbarButton) {
            whoClicked.getOpenInventory().close();
            new ChatInput<String>(String.class)
                    .text(CC.translate("&ePlease enter the new color for this rank, " +
                            "or say &ccancel &eto cancel."))
                    .escapeMessage(CC.RED + "You cancelled the color change.")
                    .onCancel(RankEditingMenu.this::openMenu)
                    .accept((player, input) -> {
                        if (input.contains(" ")) {
                            player.sendMessage(CC.RED + "The color cannot contain a white space.");
                            return false;
                        }

                        rank.setColor(CC.translate(input));
                        rank.save(player, () -> {});
                        player.sendMessage(CC.format("&eYou set the color of %s &eto %sExample&e.",
                                rank.getDisplayName(), rank.getColor()));
                        openMenu(player);
                        return true;
                    }).send(whoClicked);
        }
    }

    public class SetChatColorButton extends Button {

        @Override
        public ItemStack getItem(Player player) {
            return new ItemBuilder(Material.PAPER)
                    .setDisplayName(CC.YELLOW + CC.BOLD + "Set Chat Color")
                    .setLore(CC.format("&eChat Color: %sExample", rank.getChatColor()))
                    .build();
        }

        @Override
        public void click(Player whoClicked, int slot, ClickType clickType, int hotbarButton) {
            whoClicked.getOpenInventory().close();
            new ChatInput<String>(String.class)
                    .text(CC.translate("&ePlease enter the new chat color for this rank, " +
                            "or say &ccancel &eto cancel."))
                    .escapeMessage(CC.RED + "You cancelled the chat color change.")
                    .onCancel(RankEditingMenu.this::openMenu)
                    .accept((player, input) -> {
                        if (input.contains(" ")) {
                            player.sendMessage(CC.RED + "The chat color cannot contain a white space.");
                            return false;
                        }

                        rank.setChatColor(CC.translate(input));
                        rank.save(player, () -> {});
                        player.sendMessage(CC.format("&eYou set the chat color of %s &eto %sExample&e.",
                                rank.getDisplayName(), rank.getColor()));
                        openMenu(player);
                        return true;
                    }).send(whoClicked);
        }
    }

    public class ToggleInheritButton extends Button {

        @Override
        public ItemStack getItem(Player player) {
            List<String> lore = new ArrayList<>();
            if (rank.getInherits().isEmpty()) {
                lore.add(CC.YELLOW + "Inherits: " + CC.RED + "None");
            } else {
                lore.add(CC.YELLOW + "Inherits: ");
                rank.getInherits().forEach(inherit -> lore.add(CC.GRAY + " - " + inherit.getDisplayName()));
            }

            return new ItemBuilder(Material.BOOK)
                    .setDisplayName(CC.YELLOW + CC.BOLD + "Toggle Inherit")
                    .setLore(lore)
                    .build();
        }

        @Override
        public void click(Player whoClicked, int slot, ClickType clickType, int hotbarButton) {
            whoClicked.getOpenInventory().close();
            new ChatInput<Rank>(Rank.class)
                    .text(CC.translate("&ePlease enter the name of the child, " +
                            "or say &ccancel &eto cancel."))
                    .escapeMessage(CC.RED + "You cancelled the inherit toggling.")
                    .onCancel(RankEditingMenu.this::openMenu)
                    .accept((player, input) -> {
                        if (rank.getInherits().contains(input)) {
                            rank.getInherits().remove(input);
                            player.sendMessage(CC.format("&eYou made %s &eno longer inherit %s&e.",
                                    rank.getDisplayName(), input.getDisplayName()));
                        } else {
                            rank.getInherits().add(input);
                            player.sendMessage(CC.format("&eYou made %s &einherit %s&e.",
                                    rank.getDisplayName(), input.getDisplayName()));
                        }

                        rank.save(player, () -> {});
                        openMenu(player);
                        return true;
                    }).send(whoClicked);
        }
    }

    public class SetWeightButton extends Button {

        @Override
        public ItemStack getItem(Player player) {
            return new ItemBuilder(Material.LEVER)
                    .setDisplayName(CC.YELLOW + CC.BOLD + "Set Weight")
                    .setLore(CC.format("&eWeight: &c%d", rank.getWeight()))
                    .build();
        }

        @Override
        public void click(Player whoClicked, int slot, ClickType clickType, int hotbarButton) {
            whoClicked.getOpenInventory().close();
            new ChatInput<Integer>(Integer.class)
                    .text(CC.translate("&ePlease enter the new weight for this rank, " +
                            "or say &ccancel &eto cancel."))
                    .escapeMessage(CC.RED + "You cancelled the weight change.")
                    .onCancel(RankEditingMenu.this::openMenu)
                    .accept((player, input) -> {
                        rank.setWeight(input);
                        rank.save(player, () -> {});
                        player.sendMessage(CC.format("&eYou set the weight of %s &eto &c%d&e.",
                                rank.getDisplayName(), rank.getWeight()));
                        openMenu(player);
                        return true;
                    }).send(whoClicked);
        }
    }

    public class SetQueuePriorityButton extends Button {

        @Override
        public ItemStack getItem(Player player) {
            return new ItemBuilder(Material.LEVER)
                    .setDisplayName(CC.YELLOW + CC.BOLD + "Set Queue Priority")
                    .setLore(CC.format("&eQueue Priority: &c%d", rank.getQueuePriority()))
                    .build();
        }

        @Override
        public void click(Player whoClicked, int slot, ClickType clickType, int hotbarButton) {
            whoClicked.getOpenInventory().close();
            new ChatInput<Integer>(Integer.class)
                    .text(CC.translate("&ePlease enter the new queue priority for this rank, " +
                            "or say &ccancel &eto cancel."))
                    .escapeMessage(CC.RED + "You cancelled the queue priority change.")
                    .onCancel(RankEditingMenu.this::openMenu)
                    .accept((player, input) -> {
                        rank.setQueuePriority(input);
                        rank.save(player, () -> {});
                        player.sendMessage(CC.format("&eYou set the queue priority of %s &eto &c%d&e.",
                                rank.getDisplayName(), rank.getQueuePriority()));
                        openMenu(player);
                        return true;
                    }).send(whoClicked);
        }
    }

}
