package cc.invictusgames.invictus.grant.menu;


import cc.invictusgames.ilib.builder.ItemBuilder;
import cc.invictusgames.ilib.chatinput.ChatInput;
import cc.invictusgames.ilib.chatinput.ChatInputChain;
import cc.invictusgames.ilib.menu.Button;
import cc.invictusgames.ilib.menu.Menu;
import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.ilib.utils.TimeUtils;
import cc.invictusgames.invictus.Invictus;
import cc.invictusgames.invictus.InvictusBukkit;
import cc.invictusgames.invictus.grant.input.GrantDurationInput;
import cc.invictusgames.invictus.grant.input.GrantReasonInput;
import cc.invictusgames.invictus.profile.Profile;
import cc.invictusgames.invictus.utils.Tasks;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 19.02.2020 / 18:16
 * Invictus / cc.invictusgames.invictus.spigot.grant.menu
 */

@RequiredArgsConstructor
public class GrantDurationMenu extends Menu {

    private static final long[] DURATION_BUTTONS = new long[]{
            TimeUnit.DAYS.toMillis(1),
            TimeUnit.DAYS.toMillis(3),
            TimeUnit.DAYS.toMillis(7),
            TimeUnit.DAYS.toMillis(14),
            TimeUnit.DAYS.toMillis(30),
            TimeUnit.DAYS.toMillis(60),
            TimeUnit.DAYS.toMillis(90)
    };

    private static final ChatInput<String> REASON_INPUT = new GrantReasonInput(InvictusBukkit.getBukkitInstance());

    private static final ChatInputChain DURATION_REASON_CHAIN = new ChatInputChain()
            .next(new GrantDurationInput(Invictus.getInstance()))
            .next(new GrantReasonInput(InvictusBukkit.getBukkitInstance()));


    private final InvictusBukkit invictus;
    private final Profile profile;
    private boolean clicked = false;

    @Override
    public String getTitle(Player player) {
        return "Select a duration " + profile.getGrantProcedure().getRank().getDisplayName();
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        for (long duration : DURATION_BUTTONS)
            buttons.put(buttons.size(), new DurationButton(duration, Material.PAPER));

        buttons.put(buttons.size(), new DurationButton(-1, Material.EMPTY_MAP));

        int size = calculateSize(buttons) - 1;
        int slot = buttons.get(size) == null ? size : size + 9;
        buttons.put(slot, new Button() {
            @Override
            public ItemStack getItem(Player player) {
                return new ItemBuilder(Material.BOOK)
                        .setDisplayName(CC.YELLOW + CC.BOLD + "Custom")
                        .build();
            }

            @Override
            public void click(Player player, int slot, ClickType clickType, int hotbarButton) {
                clicked = true;
                player.closeInventory();
                DURATION_REASON_CHAIN.start(player);
            }
        });
        return buttons;
    }

    @Override
    public void onClose(Player player) {
        if (!clicked) {
            profile.setGrantProcedure(null);
            player.sendMessage(CC.RED + "You cancelled the grant procedure.");
        }
    }

    @RequiredArgsConstructor
    public class DurationButton extends Button {

        private final long duration;
        private final Material material;

        @Override
        public ItemStack getItem(Player player) {
            return new ItemBuilder(material)
                    .setDisplayName(CC.YELLOW + CC.BOLD + TimeUtils.formatDetailed(duration))
                    .build();
        }

        @Override
        public void click(Player player, int slot, ClickType clickType, int hotbarButton) {
            clicked = true;
            profile.getGrantProcedure().setDuration(duration);
            player.closeInventory();
            REASON_INPUT.send(player);
        }
    }
}
