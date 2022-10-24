package cc.invictusgames.invictus.disguise.menu;

import cc.invictusgames.ilib.builder.ItemBuilder;
import cc.invictusgames.ilib.chatinput.ChatInput;
import cc.invictusgames.ilib.menu.Button;
import cc.invictusgames.ilib.menu.page.PagedMenu;
import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.ilib.utils.InventoryUtils;
import cc.invictusgames.ilib.uuid.UUIDCache;
import cc.invictusgames.invictus.InvictusBukkit;
import cc.invictusgames.invictus.disguise.BukkitDisguiseService;
import cc.invictusgames.invictus.disguise.procedure.DisguiseProcedure;
import cc.invictusgames.invictus.utils.Tasks;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 18.06.2020 / 17:18
 * Invictus / cc.invictusgames.invictus.spigot.disguise.menu
 */

@RequiredArgsConstructor
@AllArgsConstructor
public class DisguiseSkinMenu extends PagedMenu {

    private final InvictusBukkit invictus;
    private final DisguiseProcedure procedure;

    private boolean showHidden = false;

    @Override
    public Map<Integer, Button> getAllPagesButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        for (BukkitDisguiseService.SkinPreset preset : invictus.getBukkitDisguiseService().getSkinPresets()) {
            if (!preset.isHidden() || showHidden)
                buttons.put(buttons.size(), new SkinButton(preset));
        }

        return buttons;
    }

    @Override
    public Map<Integer, Button> getGlobalButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        buttons.put(2, new OwnSkinButton());
        if (player.hasPermission("invictus.disguise.chooseskin"))
            buttons.put(3, new CustomSkinButton());
        buttons.put(5, new RandomSkinButton());
        buttons.put(6, new NameSkinButton());
        if (player.hasPermission("invictus.disguise.admin"))
            buttons.put(7, new ToggleHiddenButton());
        return buttons;
    }

    @Override
    public String getRawTitle(Player player) {
        return "Pick a skin";
    }

    @Override
    public boolean isAutoUpdate() {
        return false;
    }

    private void finalizeProcedure() {
        procedure.getProfile().player().sendMessage(CC.YELLOW + "Disguising...");

        Tasks.runAsync(() -> {
            BukkitDisguiseService.SkinPreset preset = procedure.getPreset();
            if (preset == null)
                preset = invictus.getBukkitDisguiseService().parseSkinPreset(procedure.getName(),
                        procedure.getSkinName(), true);

            if (preset == null) {
                procedure.getProfile().player().sendMessage(CC.RED + "Something went wrong parsing your skin.");
                return;
            }

            invictus.getBukkitDisguiseService().disguise(
                    procedure.getProfile(),
                    procedure.getRank(),
                    procedure.getName(),
                    preset,
                    true
            );

            procedure.getProfile().player().sendMessage(CC.format(
                    "&aYou are now disguised as %s%s &a(in the skin of &e%s&a).",
                    procedure.getProfile().getCurrentGrant().getRank().getPrefix(),
                    procedure.getName(),
                    preset.getName()
            ));
            if (UUIDCache.getUuid(procedure.getName()) != null)
                procedure.getProfile().player().sendMessage(
                        CC.RED + CC.BOLD + "Warning: " + CC.RED + procedure.getName() +
                                " has joined the server before. If they log on again you will be undisguised.");
            else procedure.getProfile().player().sendMessage(CC.RED + CC.BOLD + "Warning: " + CC.RED +
                    "If the player belonging to your disguised name logs on to the server you will be undisguised.");
        });
    }

    @RequiredArgsConstructor
    public class SkinButton extends Button {

        private final BukkitDisguiseService.SkinPreset entry;

        @Override
        public ItemStack getItem(Player player) {
            return new ItemBuilder(InventoryUtils.createTexturedSkull(entry.getTexture(), entry.getSignature()))
                    .setDisplayName(CC.YELLOW + CC.BOLD + entry.getName())
                    .build();
        }

        @Override
        public void click(Player player, int slot, ClickType clickType, int hotbarButton) {
            procedure.setPreset(entry);
            player.getOpenInventory().close();
            finalizeProcedure();
        }
    }

    public class OwnSkinButton extends Button {

        @Override
        public ItemStack getItem(Player player) {
            return new ItemBuilder(Material.SKULL_ITEM, 3)
                    .setDisplayName(CC.YELLOW + CC.BOLD + "Your own skin")
                    .setSkullOwner(procedure.getProfile().getName())
                    .build();
        }

        @Override
        public void click(Player player, int slot, ClickType clickType, int hotbarButton) {
            procedure.setSkinName(procedure.getProfile().getName());
            player.getOpenInventory().close();
            finalizeProcedure();
        }
    }

    public class NameSkinButton extends Button {

        @Override
        public ItemStack getItem(Player player) {
            return new ItemBuilder(Material.SKULL_ITEM, 3)
                    .setDisplayName(CC.YELLOW + CC.BOLD + procedure.getName())
                    .setSkullOwner(procedure.getName())
                    .build();
        }

        @Override
        public void click(Player player, int slot, ClickType clickType, int hotbarButton) {
            procedure.setSkinName(procedure.getName());
            player.getOpenInventory().close();
            finalizeProcedure();
        }
    }

    public class RandomSkinButton extends Button {

        @Override
        public ItemStack getItem(Player player) {
            return new ItemBuilder(Material.EMERALD)
                    .setDisplayName(CC.YELLOW + CC.BOLD + "Random Skin")
                    .build();
        }

        @Override
        public void click(Player player, int slot, ClickType clickType, int hotbarButton) {
            List<BukkitDisguiseService.SkinPreset> skins = invictus.getBukkitDisguiseService().getSkinPresets();
            if (skins.isEmpty()) {
                player.sendMessage(CC.RED + "There are currently no skins available");
                return;
            }

            BukkitDisguiseService.SkinPreset skin = skins.get(new Random().nextInt(skins.size()));
            procedure.setPreset(skin);
            player.getOpenInventory().close();
            finalizeProcedure();
        }
    }

    public class CustomSkinButton extends Button {

        @Override
        public ItemStack getItem(Player player) {
            return new ItemBuilder(Material.SIGN).setDisplayName(CC.YELLOW + CC.BOLD + "Enter a skin").build();
        }

        @Override
        public void click(Player player, int slot, ClickType clickType, int hotbarButton) {
            player.getOpenInventory().close();
            new ChatInput<String>(String.class)
                    .text(CC.translate("&ePlease enter the name of the skin you would like to disguise as, " +
                            "or say &ccancel &eto cancel."))
                    .escapeMessage(CC.RED + "You cancelled the disguise procedure.")
                    .accept((player1, input) -> {
                        procedure.setSkinName(input);
                        finalizeProcedure();
                        return true;
                    }).send(player);
        }
    }

    @RequiredArgsConstructor
    public class ToggleHiddenButton extends Button {

        @Override
        public ItemStack getItem(Player player) {
            return new ItemBuilder(showHidden ? Material.REDSTONE_TORCH_ON : Material.LEVER)
                    .setDisplayName((showHidden ? CC.RED + CC.BOLD + "Toggle" : CC.GREEN + CC.BOLD + "Show")
                            + " Hidden Skins")
                    .build();
        }

        @Override
        public void click(Player player, int slot, ClickType clickType, int hotbarButton) {
            showHidden = !showHidden;
            player.getOpenInventory().close();
            openMenu(player);
        }
    }
}
