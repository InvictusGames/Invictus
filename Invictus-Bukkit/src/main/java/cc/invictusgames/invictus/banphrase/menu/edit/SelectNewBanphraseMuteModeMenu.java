package cc.invictusgames.invictus.banphrase.menu.edit;

import cc.invictusgames.ilib.builder.ItemBuilder;
import cc.invictusgames.ilib.menu.Button;
import cc.invictusgames.ilib.menu.Menu;
import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.ilib.utils.json.JsonBuilder;
import cc.invictusgames.invictus.Invictus;
import cc.invictusgames.invictus.banphrase.Banphrase;
import cc.invictusgames.invictus.banphrase.packets.BanphraseReloadPacket;
import cc.invictusgames.invictus.connection.RequestHandler;
import cc.invictusgames.invictus.connection.RequestResponse;
import cc.invictusgames.invictus.utils.Tasks;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 17.06.2021 / 20:13
 * Invictus / cc.invictusgames.invictus.banphrase.menu.edit
 */

@RequiredArgsConstructor
public class SelectNewBanphraseMuteModeMenu extends Menu {

    private final Invictus invictus;
    private final Banphrase banphrase;

    @Override
    public String getTitle(Player player) {
        return "Select new operator";
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
            player.getOpenInventory().close();

            Tasks.runAsync(() -> {
                RequestResponse response = RequestHandler.put("banphrase/%s",
                        new JsonBuilder().add("muteMode", muteMode.name()).build(),
                        banphrase.getId().toString());

                if (!response.wasSuccessful()) {
                    player.sendMessage(CC.format("&cCould not update banphrase: %s (%d)",
                            response.getErrorMessage(), response.getCode()));
                    return;
                }

                banphrase.setMuteMode(muteMode);
                invictus.getRedisService().publish(new BanphraseReloadPacket());
                player.sendMessage(CC.format("&eYou changed the mute mode of &c%s &eto &c%s&e.",
                        banphrase.getName(), muteMode.getDisplay()));
            });
        }
    }
}
