package cc.invictusgames.invictus.prime.menu;

import cc.invictusgames.ilib.builder.ItemBuilder;
import cc.invictusgames.ilib.menu.Button;
import cc.invictusgames.ilib.menu.Menu;
import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.ilib.utils.TimeUtils;
import cc.invictusgames.invictus.InvictusBukkit;
import cc.invictusgames.invictus.grant.Grant;
import cc.invictusgames.invictus.playersetting.InvictusSettings;
import cc.invictusgames.invictus.profile.Profile;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class PrimeMenu extends Menu {

    private final InvictusBukkit invictus;

    @Override
    public String getTitle(Player player) {
        return "Prime";
    }

    @Override
    public int getSize() {
        return 27;
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        buttons.put(10, new ClaimRewardsButton(invictus.getProfileService().getProfile(player)));
        buttons.put(13, new PrimeStatusButton());
        buttons.put(16, new PrimeColorButton());
        return buttons;
    }

    public class PrimeStatusButton extends Button {

        @Override
        public ItemStack getItem(Player player) {
            Profile profile = invictus.getProfileService().getProfile(player);

            Optional<Grant> prime = Optional.empty();
            for (Grant grant : profile.getActiveGrants()) {
                if (grant.getRank().getName().equalsIgnoreCase("prime"))
                    prime = Optional.of(grant);
            }

            return new ItemBuilder(Material.SKULL_ITEM, 3)
                    .setSkullOwner(profile.getName())
                    .setDisplayName(ChatColor.YELLOW + CC.BOLD + "Prime Status Information")
                    .setLore(
                            CC.MENU_BAR,
                            CC.format("&ePrime Status: %s",
                                    CC.colorBoolean(profile.hasPrimeStatus(), "yes", "no")),
                            CC.format(
                                    "&eTime left: &c%s",
                                    prime.map(grant -> grant.getDuration() == -1 ? "Permanent"
                                                    : TimeUtils.formatTimeShort(grant.getRemainingTime()))
                                            .orElse("None")
                            ),
                            " ",
                            CC.translate("&7&oRenew your prime status at "
                                    + invictus.getMessageService().formatMessage("network-store")),
                            CC.MENU_BAR
                    )
                    .build();
        }
    }

    public class PrimeColorButton extends Button {

        @Override
        public ItemStack getItem(Player player) {
            return new ItemBuilder(Material.INK_SACK,
                    PrimeColorMenu.convertToDyeColor(InvictusSettings.PRIME_COLOR.get(player)).getDyeData())
                    .setDisplayName(CC.YELLOW + CC.BOLD + "Icon Color")
                    .build();
        }

        @Override
        public void click(Player player, int slot, ClickType clickType, int hotbarButton) {
            Profile profile = invictus.getProfileService().getProfile(player);
            if (profile.hasPrimeStatus())
                new PrimeColorMenu(invictus).openMenu(player);
            else player.sendMessage(CC.format(
                    "&cPurchase &ePrime Status &cat &e%s &cto access this feature.",
                    invictus.getMessageService().formatMessage("network-store")
            ));
        }
    }

    public class ClaimRewardsButton extends Button {

        private final Profile profile;
        private long end = -1;

        public ClaimRewardsButton(Profile profile) {
            this.profile = profile;
            if (InvictusBukkit.getPrimeRewardProvider() != null)
                end = invictus.getRedisService().executeBackendCommand(redis -> {
                    String key = "prime:" + profile.getUuid().toString() + ":cooldown";
                    if (redis.hexists(key, InvictusBukkit.getPrimeRewardProvider().getServerName()))
                        return Long.parseLong(redis.hget(key, InvictusBukkit.getPrimeRewardProvider().getServerName()));
                    return -1L;
                });
        }

        @Override
        public ItemStack getItem(Player player) {
            List<String> lore = new ArrayList<>();
            lore.add(CC.MENU_BAR);
            if (InvictusBukkit.getPrimeRewardProvider() == null)
                lore.add(CC.RED + "No rewards to claim");
            else {
                lore.add(CC.YELLOW + "Rewards:");

                for (String s : InvictusBukkit.getPrimeRewardProvider().getDescription(player)) {
                    lore.add(CC.YELLOW + " - " + CC.RED + s);
                }

                lore.add(" ");
                if (!profile.hasPrimeStatus())
                    lore.add(CC.format("&7&oPurchase prime status at %s",
                            invictus.getMessageService().formatMessage("network-store")));
                else if (end > System.currentTimeMillis())
                    lore.add(CC.YELLOW + "Claimable in: " + CC.RED + TimeUtils.formatTimeShort(end - System.currentTimeMillis()));
                else lore.add(CC.YELLOW + "Click here to claim your rewards");
            }
            lore.add(CC.MENU_BAR);

            return new ItemBuilder(Material.TRIPWIRE_HOOK)
                    .setDisplayName(CC.YELLOW + CC.BOLD + "Claim Rewards")
                    .setLore(lore)
                    .build();
        }

        @Override
        public void click(Player player, int slot, ClickType clickType, int hotbarButton) {
            if (!profile.hasPrimeStatus()) {
                player.sendMessage(CC.RED + "Purchase " + CC.YELLOW + "Prime Status" + CC.RED + " at "
                        + CC.YELLOW + invictus.getMessageService().formatMessage("network-store") + CC.RED
                        + " to access this feature.");
                return;
            }

            if (InvictusBukkit.getPrimeRewardProvider() == null) {
                player.sendMessage(CC.RED + "No rewards to claim.");
                return;
            }

            if (end > System.currentTimeMillis()) {
                player.sendMessage(CC.RED + "You can claim your rewards again in "
                        + CC.YELLOW + TimeUtils.formatDetailed(end - System.currentTimeMillis())
                        + CC.RED + ".");
                return;
            }

            end = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(7);
            invictus.getRedisService().executeBackendCommand(redis -> {
                redis.hset(
                        "prime:" + profile.getUuid().toString() + ":cooldown",
                        InvictusBukkit.getPrimeRewardProvider().getServerName(),
                        String.valueOf(end)
                );
                return null;
            });
            player.closeInventory();
            InvictusBukkit.getPrimeRewardProvider().getCommands(player).forEach(command ->
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), String.format(command, player.getName())));
            Bukkit.broadcastMessage(" ");
            Bukkit.broadcastMessage(profile.getDisplayName() + CC.GRAY + " has redeemed their " + CC.YELLOW + "Prime" +
                    " Status" + CC.GRAY + " rewards.");
            Bukkit.broadcastMessage(CC.GRAY + CC.ITALIC + "Purchase prime status at "
                    + invictus.getMessageService().formatMessage("network-store") + ".");
            Bukkit.broadcastMessage(" ");
        }
    }
}
