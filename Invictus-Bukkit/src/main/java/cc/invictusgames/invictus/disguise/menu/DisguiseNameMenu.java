package cc.invictusgames.invictus.disguise.menu;

import cc.invictusgames.ilib.builder.ItemBuilder;
import cc.invictusgames.ilib.chatinput.ChatInput;
import cc.invictusgames.ilib.menu.Button;
import cc.invictusgames.ilib.menu.page.PagedMenu;
import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.ilib.utils.callback.TypeCallable;
import cc.invictusgames.ilib.uuid.UUIDCache;
import cc.invictusgames.invictus.InvictusBukkit;
import cc.invictusgames.invictus.connection.RequestHandler;
import cc.invictusgames.invictus.connection.RequestResponse;
import cc.invictusgames.invictus.disguise.BukkitDisguiseService;
import cc.invictusgames.invictus.disguise.input.DisguiseNameInput;
import cc.invictusgames.invictus.disguise.procedure.DisguiseProcedure;
import cc.invictusgames.invictus.utils.Tasks;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 18.06.2020 / 16:44
 * Invictus / cc.invictusgames.invictus.spigot.disguise
 */

@RequiredArgsConstructor
public class DisguiseNameMenu extends PagedMenu {

    private final InvictusBukkit invictus;
    private final DisguiseProcedure procedure;
    private final AtomicBoolean checking = new AtomicBoolean(false);

    @Override
    public Map<Integer, Button> getAllPagesButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        invictus.getBukkitDisguiseService().getNamePresets().forEach(entry -> buttons.put(buttons.size(),
                new NameButton(entry)));
        return buttons;
    }

    @Override
    public Map<Integer, Button> getGlobalButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        buttons.put(3, new CustomNameButton());
        buttons.put(5, new RandomNameButton());
        return buttons;
    }

    @Override
    public String getRawTitle(Player player) {
        return "Pick a name";
    }

    @Override
    public boolean isAutoUpdate() {
        return false;
    }

    @RequiredArgsConstructor
    public class NameButton extends Button {

        private final String name;

        @Override
        public ItemStack getItem(Player player) {
            return new ItemBuilder(Material.SKULL_ITEM, 3)
                    .setDisplayName(CC.YELLOW + CC.BOLD + name).build();
        }

        @Override
        public void click(Player player, int slot, ClickType clickType, int hotbarButton) {
            procedure.setName(name);
            new DisguiseRankMenu(invictus, procedure).openMenu(player);
        }
    }

    public class RandomNameButton extends Button {

        @Override
        public ItemStack getItem(Player player) {
            return new ItemBuilder(Material.EMERALD)
                    .setDisplayName(CC.YELLOW + CC.BOLD + "Random Name")
                    .build();
        }

        @Override
        public void click(Player player, int slot, ClickType clickType, int hotbarButton) {
            List<String> names = invictus.getBukkitDisguiseService().getNamePresets();
            if (names.isEmpty()) {
                player.sendMessage(CC.RED + "There are currently no names available.");
                return;
            }

            if (checking.get()) {
                return;
            }

            getRandomName(name -> {
                if (name == null) {
                    player.sendMessage(CC.RED + "Failed to find an available name after 10 attempts.");
                    return;
                }

                procedure.setName(name);
                new DisguiseRankMenu(invictus, procedure).openMenu(player);
            }, true);

        }
    }

    public class CustomNameButton extends Button {

        @Override
        public ItemStack getItem(Player player) {
            return new ItemBuilder(Material.SIGN).setDisplayName(CC.YELLOW + CC.BOLD + "Enter a name").build();
        }

        @Override
        public void click(Player player, int slot, ClickType clickType, int hotbarButton) {
            player.getOpenInventory().close();
            new DisguiseNameInput(invictus, DisguiseNameMenu.this, procedure).send(player);
        }
    }


    public void checkAvailability(String name, TypeCallable<Boolean> callable) {
        Tasks.runAsync(() -> {
            boolean available = false;
            RequestResponse response = RequestHandler.get("disguise/%s/available", name);
            if (response.wasSuccessful()
                    && response.asObject().has("available")
                    && response.asObject().get("available").getAsBoolean())
                available = true;
            callable.callback(available);
        });
    }

    private int attempts = 0;

    public void getRandomName(TypeCallable<String> callable, boolean async) {
        if (async) {
            Tasks.runAsync(() -> getRandomName(callable, false));
            return;
        }

        attempts++;
        List<String> names = invictus.getBukkitDisguiseService().getNamePresets();

        if (names.isEmpty()) {
            callable.callback(null);
            attempts = 0;
            return;
        }

        String name = names.get(new Random().nextInt(names.size()));
        boolean available = false;
        RequestResponse response = RequestHandler.get("disguise/%s/available", name);
        if (response.wasSuccessful()
                && response.asObject().has("available")
                && response.asObject().get("available").getAsBoolean())
            available = true;

        if (attempts > 10) {
            callable.callback(null);
            return;
        }

        if (!available || UUIDCache.getUuid(name) != null) {
            getRandomName(callable, false);
            return;
        }

        callable.callback(name);
        attempts = 0;
    }
}
