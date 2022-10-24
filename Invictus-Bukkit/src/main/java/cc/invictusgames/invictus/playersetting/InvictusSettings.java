package cc.invictusgames.invictus.playersetting;

import cc.invictusgames.ilib.builder.ItemBuilder;
import cc.invictusgames.ilib.playersetting.PlayerSetting;
import cc.invictusgames.ilib.playersetting.PlayerSettingProvider;
import cc.invictusgames.ilib.playersetting.impl.BooleanSetting;
import cc.invictusgames.ilib.utils.AdminBypass;
import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.ilib.utils.Statics;
import cc.invictusgames.ilib.visibility.VisibilityService;
import cc.invictusgames.invictus.Invictus;
import cc.invictusgames.invictus.prime.menu.PrimeColorMenu;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;

public class InvictusSettings implements PlayerSettingProvider {

    public static final BooleanSetting PRIVATE_MESSAGES
            = new BooleanSetting("invictus", "private_messages") {
        @Override
        public String getDisplayName() {
            return "Private Messages";
        }

        @Override
        public String getEnabledText() {
            return "Receive private messages";
        }

        @Override
        public String getDisabledText() {
            return "Block private messages";
        }

        @Override
        public List<String> getDescription() {
            return Arrays.asList(
                    CC.YELLOW + "If enabled, you will be able to",
                    CC.YELLOW + "receive private messages."
            );
        }

        @Override
        public MaterialData getMaterial() {
            return new MaterialData(Material.SIGN);
        }

        @Override
        public Boolean getDefaultValue() {
            return true;
        }
    };

    public static final PlayerSetting<ChatColor> PRIME_COLOR
            = new PlayerSetting<ChatColor>("invictus", "prime_color") {
        @Override
        public ChatColor getDefaultValue() {
            return ChatColor.YELLOW;
        }

        @Override
        public ChatColor parse(String input) {
            if (input == null || input.equals("null"))
                return null;

            return ChatColor.valueOf(input);
        }

        @Override
        public ItemStack getIcon(Player player) {
            return new ItemBuilder(Material.BOOK)
                    .setDisplayName(CC.YELLOW + CC.BOLD + "Prime Icon Color")
                    .setLore(
                            "",
                            CC.YELLOW + "Click to open the prime",
                            CC.YELLOW + "color selector."
                    ).build();
        }

        @Override
        public void click(Player player, ClickType clickType) {
            new PrimeColorMenu(Invictus.getInstance()).openMenu(player);
        }

        @Override
        public boolean canUpdate(Player player) {
            return Invictus.getInstance().getProfileService().getProfile(player).hasPrimeStatus()
                    || AdminBypass.isBypassing(player);
        }

        @Override
        public String toString(ChatColor value) {
            return value == null ? "null" : value.name();
        }
    };

    public static final BooleanSetting MESSAGING_SOUNDS
            = new BooleanSetting("invictus", "messaging_sounds") {
        @Override
        public String getDisplayName() {
            return "Messaging Sounds";
        }

        @Override
        public String getEnabledText() {
            return "Play a sound on private messages";
        }

        @Override
        public String getDisabledText() {
            return "Disable sound on private messages";
        }

        @Override
        public List<String> getDescription() {
            return Arrays.asList(
                    CC.YELLOW + "If enabled, you will hear a sound",
                    CC.YELLOW + "when you receive a private message."
            );
        }

        @Override
        public MaterialData getMaterial() {
            return new MaterialData(Material.NOTE_BLOCK);
        }

        @Override
        public Boolean getDefaultValue() {
            return true;
        }
    };

    public static final BooleanSetting STAFF_MESSAGES
            = new BooleanSetting("invictus", "staff_messages") {
        @Override
        public String getDisplayName() {
            return "Staff Messages";
        }

        @Override
        public String getEnabledText() {
            return "Staff messages are shown";
        }

        @Override
        public String getDisabledText() {
            return "Staff messages are hidden";
        }

        @Override
        public List<String> getDescription() {
            return Arrays.asList(
                    CC.YELLOW + "If enabled, you will be able to",
                    CC.YELLOW + "see staff only messages."
            );
        }

        @Override
        public MaterialData getMaterial() {
            return new MaterialData(Material.EYE_OF_ENDER);
        }

        @Override
        public Boolean getDefaultValue() {
            return true;
        }

        @Override
        public boolean canUpdate(Player player) {
            return player.hasPermission("invictus.command.togglestaffmessages");
        }
    };

    public static final BooleanSetting STAFF_SHOWN
            = new BooleanSetting("invictus", "staff_shown") {
        @Override
        public String getDisplayName() {
            return "Show Vanished Staff";
        }

        @Override
        public String getEnabledText() {
            return "Vanished staff is visible";
        }

        @Override
        public String getDisabledText() {
            return "Vanished staff is hidden";
        }

        @Override
        public List<String> getDescription() {
            return Arrays.asList(
                    CC.YELLOW + "If enabled, you will be able to",
                    CC.YELLOW + "see vanished staff members."
            );
        }

        @Override
        public MaterialData getMaterial() {
            return new MaterialData(Material.INK_SACK);
        }

        @Override
        public Boolean getDefaultValue() {
            return true;
        }

        @Override
        public boolean canUpdate(Player player) {
            return player.hasPermission("invictus.command.hidestaff");
        }

        @Override
        public void click(Player player, ClickType clickType) {
            super.click(player, clickType);
            VisibilityService.update(player);
        }
    };

    public static final PlayerSetting<TimeZone> TIME_ZONE
            = new PlayerSetting<TimeZone>("invictus", "time_zone") {
        @Override
        public TimeZone getDefaultValue() {
            return Statics.TIME_ZONE;
        }

        @Override
        public TimeZone parse(String input) {
            return TimeZone.getTimeZone(input);
        }

        @Override
        public ItemStack getIcon(Player player) {
            return new ItemBuilder(Material.AIR).build();
        }

        @Override
        public void click(Player player, ClickType clickType) { }

        @Override
        public String toString(TimeZone value) {
            return value.toZoneId().getId();
        }

        @Override
        public boolean canUpdate(Player player) {
            // returning false here will not show this in the settings menu for anyone
            return false;
        }
    };

    @Override
    public List<PlayerSetting> getProvidedSettings() {
        return Arrays.asList(
                PRIVATE_MESSAGES,
                MESSAGING_SOUNDS,
                PRIME_COLOR,
                STAFF_MESSAGES,
                STAFF_SHOWN,
                TIME_ZONE // even tho we don't show this in the menu, it still has to be provided to be loaded
        );
    }

    @Override
    public int getPriority() {
        return 1;
    }
}
