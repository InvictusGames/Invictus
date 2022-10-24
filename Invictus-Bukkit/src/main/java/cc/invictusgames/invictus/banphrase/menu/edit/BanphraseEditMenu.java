package cc.invictusgames.invictus.banphrase.menu.edit;

import cc.invictusgames.ilib.builder.ItemBuilder;
import cc.invictusgames.ilib.chatinput.ChatInput;
import cc.invictusgames.ilib.command.parameter.defaults.Duration;
import cc.invictusgames.ilib.menu.Button;
import cc.invictusgames.ilib.menu.Menu;
import cc.invictusgames.ilib.menu.fill.FillTemplate;
import cc.invictusgames.ilib.menu.menu.ConfirmationMenu;
import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.ilib.utils.TimeUtils;
import cc.invictusgames.ilib.utils.json.JsonBuilder;
import cc.invictusgames.invictus.Invictus;
import cc.invictusgames.invictus.banphrase.Banphrase;
import cc.invictusgames.invictus.banphrase.input.edit.BanphraseNewDurationInput;
import cc.invictusgames.invictus.banphrase.input.edit.BanphraseNewNameInput;
import cc.invictusgames.invictus.banphrase.input.edit.BanphraseNewPhraseInput;
import cc.invictusgames.invictus.banphrase.menu.add.SelectMuteModeMenu;
import cc.invictusgames.invictus.banphrase.packets.BanphraseReloadPacket;
import cc.invictusgames.invictus.connection.RequestHandler;
import cc.invictusgames.invictus.connection.RequestResponse;
import cc.invictusgames.invictus.utils.Tasks;
import lombok.RequiredArgsConstructor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 17.06.2021 / 19:27
 * Invictus / cc.invictusgames.invictus.banphrase.menu
 */

@RequiredArgsConstructor
public class BanphraseEditMenu extends Menu {

    private final Invictus invictus;
    private final Banphrase banphrase;

    @Override
    public String getTitle(Player player) {
        return "Editing: " + banphrase.getName();
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        buttons.put(9, new RenameBanphraseButton());
        buttons.put(10, new ChangePhraseButton());
        buttons.put(11, new ChangeOperatorButton());
        buttons.put(12, new ChangeMuteModeButton());
        buttons.put(14, new ChangeDurationButton());
        buttons.put(15, new ToggleCaseSensitiveButton());
        buttons.put(16, new ToggleEnabledButton());
        buttons.put(17, new DeleteBanphraseButton());
        return buttons;
    }

    @Override
    public FillTemplate getFillTemplate() {
        return FillTemplate.FILL;
    }

    @Override
    public int getSize() {
        return 27;
    }

    public class RenameBanphraseButton extends Button {

        @Override
        public ItemStack getItem(Player player) {
            return new ItemBuilder(Material.ANVIL)
                    .setDisplayName(CC.YELLOW + CC.BOLD + "Rename Banphrase")
                    .setLore(CC.format("&eName: &c%s", banphrase.getName()))
                    .build();
        }

        @Override
        public void click(Player player, int slot, ClickType clickType, int hotbarButton) {
            player.getOpenInventory().close();
            new BanphraseNewNameInput(invictus, banphrase).send(player);
        }
    }

    public class ChangePhraseButton extends Button {

        @Override
        public ItemStack getItem(Player player) {
            return new ItemBuilder(Material.SIGN)
                    .setDisplayName(CC.YELLOW + CC.BOLD + "Change Phrase")
                    .setLore(CC.format("&ePhrase: &c%s", banphrase.getPhrase()))
                    .build();
        }

        @Override
        public void click(Player player, int slot, ClickType clickType, int hotbarButton) {
            player.getOpenInventory().close();
            new BanphraseNewPhraseInput(invictus, banphrase).send(player);
        }
    }

    public class ChangeOperatorButton extends Button {

        @Override
        public ItemStack getItem(Player player) {
            return new ItemBuilder(Material.REDSTONE)
                    .setDisplayName(CC.YELLOW + CC.BOLD + "Change Operator")
                    .setLore(CC.format("&eOperator: &c%s", banphrase.getOperator().getDisplay()))
                    .build();
        }

        @Override
        public void click(Player player, int slot, ClickType clickType, int hotbarButton) {
            new SelectNewBanphraseOperatorMenu(invictus, banphrase).openMenu(player);
        }
    }

    public class ChangeMuteModeButton extends Button {

        @Override
        public ItemStack getItem(Player player) {
            return new ItemBuilder(Material.PAPER)
                    .setDisplayName(CC.YELLOW + CC.BOLD + "Change Mute Mode")
                    .setLore(CC.format("&eMute Mode: &c%s", banphrase.getMuteMode().getDisplay()))
                    .build();
        }

        @Override
        public void click(Player player, int slot, ClickType clickType, int hotbarButton) {
            new SelectNewBanphraseMuteModeMenu(invictus, banphrase).openMenu(player);
        }
    }

    public class ChangeDurationButton extends Button {

        @Override
        public ItemStack getItem(Player player) {
            return new ItemBuilder(Material.EMPTY_MAP)
                    .setDisplayName(CC.YELLOW + CC.BOLD + "Change Duration")
                    .setLore(CC.format("&eDuration: &c%s", TimeUtils.formatTimeShort(banphrase.getDuration())))
                    .build();
        }

        @Override
        public void click(Player player, int slot, ClickType clickType, int hotbarButton) {
            player.getOpenInventory().close();
            new BanphraseNewDurationInput(invictus, banphrase).send(player);
        }
    }

    public class ToggleCaseSensitiveButton extends Button {

        @Override
        public ItemStack getItem(Player player) {
            return new ItemBuilder(Material.INK_SACK,
                    banphrase.isCaseSensitive() ? DyeColor.LIME.getDyeData() : DyeColor.GRAY.getDyeData())
                    .setDisplayName(CC.YELLOW + CC.BOLD + "Toggle Case-Sensitive")
                    .setLore(CC.format("&eCase-Sensitive: &c%s",
                            CC.colorBoolean(banphrase.isCaseSensitive(), "yes", "no")))
                    .build();
        }

        @Override
        public void click(Player player, int slot, ClickType clickType, int hotbarButton) {
            player.getOpenInventory().close();

            Tasks.runAsync(() -> {
                RequestResponse response = RequestHandler.put("banphrase/%s",
                        new JsonBuilder().add("caseSensitive", !banphrase.isCaseSensitive()).build(),
                        banphrase.getId().toString());

                if (!response.wasSuccessful()) {
                    player.sendMessage(CC.format("&cCould not update banphrase: %s (%d)",
                            response.getErrorMessage(), response.getCode()));
                    return;
                }

                banphrase.setCaseSensitive(!banphrase.isCaseSensitive());
                invictus.getRedisService().publish(new BanphraseReloadPacket());
                player.sendMessage(CC.format("&eYou toggled the case sensitive status of &c%s &eto &c%s&e.",
                        banphrase.getName(), CC.colorBoolean(banphrase.isCaseSensitive())));
            });
        }
    }

    public class ToggleEnabledButton extends Button {

        @Override
        public ItemStack getItem(Player player) {
            return new ItemBuilder(Material.INK_SACK,
                    banphrase.isEnabled() ? DyeColor.LIME.getDyeData() : DyeColor.GRAY.getDyeData())
                    .setDisplayName(CC.YELLOW + CC.BOLD + "Toggle Enabled")
                    .setLore(CC.format("&eEnabled: &c%s",
                            CC.colorBoolean(banphrase.isEnabled(), "yes", "no")))
                    .build();
        }

        @Override
        public void click(Player player, int slot, ClickType clickType, int hotbarButton) {
            player.getOpenInventory().close();

            Tasks.runAsync(() -> {
                RequestResponse response = RequestHandler.put("banphrase/%s",
                        new JsonBuilder().add("enabled", !banphrase.isEnabled()).build(),
                        banphrase.getId().toString());

                if (!response.wasSuccessful()) {
                    player.sendMessage(CC.format("&cCould not update banphrase: %s (%d)",
                            response.getErrorMessage(), response.getCode()));
                    return;
                }

                banphrase.setEnabled(!banphrase.isEnabled());
                invictus.getRedisService().publish(new BanphraseReloadPacket());
                player.sendMessage(CC.format("&eYou toggled the enabled status of &c%s &eto &c%s&e.",
                        banphrase.getName(), CC.colorBoolean(banphrase.isEnabled())));
            });
        }
    }

    public class DeleteBanphraseButton extends Button {

        @Override
        public ItemStack getItem(Player player) {
            return new ItemBuilder(Material.TNT)
                    .setDisplayName(CC.RED + CC.BOLD + "Delete Banphrase")
                    .build();
        }

        @Override
        public void click(Player player, int slot, ClickType clickType, int hotbarButton) {
            player.getOpenInventory().close();

            new ConfirmationMenu("Delete Banphrase?", "Yes", "No", b -> {
                if (!b) {
                    Tasks.runLater(() -> openMenu(player), 1L);
                    return;
                }

                Tasks.runAsync(() -> {
                    RequestResponse response = RequestHandler.delete("banphrase/%s",
                            banphrase.getId().toString());

                    if (!response.wasSuccessful()) {
                        player.sendMessage(CC.format("&cCould not delete banphrase: %s (%d)",
                                response.getErrorMessage(), response.getCode()));
                        return;
                    }

                    invictus.getRedisService().publish(new BanphraseReloadPacket());
                    player.sendMessage(CC.format("&eYou deleted the &c%s &ebanphrase.", banphrase.getName()));
                });
            }).openMenu(player);
        }
    }

}
