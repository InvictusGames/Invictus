package cc.invictusgames.invictus.grant.menu;

import cc.invictusgames.ilib.builder.ItemBuilder;
import cc.invictusgames.ilib.chatinput.ChatInput;
import cc.invictusgames.ilib.menu.Button;
import cc.invictusgames.ilib.menu.page.PagedMenu;
import cc.invictusgames.ilib.redis.packet.Packet;
import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.ilib.utils.TimeUtils;
import cc.invictusgames.ilib.utils.UUIDUtils;
import cc.invictusgames.ilib.uuid.UUIDCache;
import cc.invictusgames.invictus.InvictusBukkit;
import cc.invictusgames.invictus.connection.RequestResponse;
import cc.invictusgames.invictus.grant.Grant;
import cc.invictusgames.invictus.grant.input.GrantRemoveInput;
import cc.invictusgames.invictus.grant.packets.GrantRemovePacket;
import cc.invictusgames.invictus.playersetting.InvictusSettings;
import cc.invictusgames.invictus.profile.Profile;
import cc.invictusgames.invictus.profile.packets.ProfileUpdatePacket;
import cc.invictusgames.invictus.rank.Rank;
import cc.invictusgames.invictus.utils.Tasks;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 20.02.2020 / 21:08
 * Invictus / cc.invictusgames.invictus.spigot.grant.menu
 */

@RequiredArgsConstructor
public class GrantsMenu extends PagedMenu {

    private final InvictusBukkit invictus;
    private final Profile target;
    private final List<Grant> grants;

    @Override
    public Map<Integer, Button> getAllPagesButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        grants.sort(Comparator.comparingLong(Grant::getGrantedAt).reversed());
        grants.forEach(grant -> buttons.put(buttons.size(), new GrantButton(grant)));
        return buttons;
    }

    @Override
    public String getRawTitle(Player player) {
        return "Grants: " + target.getRealDisplayName();
    }

    @Override
    public void onClose(Player player) {
        grants.clear();
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
        return profile.getRealCurrentGrant().getRank().getWeight() > rank.getWeight()
                && player.hasPermission("invictus.grant." + rank.getName())
                && player.hasPermission("invictus.grants.remove");
    }

    @RequiredArgsConstructor
    public class GrantButton extends Button {

        private final Grant grant;

        @Override
        public ItemStack getItem(Player player) {
            List<String> lore = new ArrayList<>();
            lore.add(CC.MENU_BAR);
            lore.add(CC.YELLOW + "By: " + CC.RED + (UUIDUtils.isUUID(grant.getGrantedBy()) ?
                    UUIDCache.getName(UUID.fromString(grant.getGrantedBy())) : grant.getGrantedBy()));
            lore.add(CC.YELLOW + "Reason: " + CC.RED + grant.getGrantedReason());
            List<String> scopes = new ArrayList<>();
            grant.getScopes().forEach(scope -> scopes.add(WordUtils.capitalizeFully(scope)));
            lore.add(CC.YELLOW + "Scopes: " + CC.RED + StringUtils.join(scopes, ", "));
            lore.add(CC.YELLOW + "Rank: " + CC.RED + grant.getRank().getDisplayName());
            if (grant.isRemoved()) {
                lore.add(CC.MENU_BAR);
                lore.add(CC.RED + "Removed: ");
                lore.add(CC.YELLOW + ((UUIDUtils.isUUID(grant.getRemovedBy()) ?
                        UUIDCache.getName(UUID.fromString(grant.getRemovedBy())) : grant.getRemovedBy()))
                        + ": " + CC.RED + grant.getRemovedReason());
                lore.add(CC.RED + "at " + CC.YELLOW + TimeUtils.formatDate(grant.getRemovedAt(),
                        InvictusSettings.TIME_ZONE.get(player)));
                lore.add(" ");
                lore.add(CC.YELLOW + "Duration: " + TimeUtils.formatTimeShort(grant.getDuration()));
            } else if (!grant.isActive()) {
                lore.add(CC.YELLOW + "Duration: " + TimeUtils.formatTimeShort(grant.getDuration()));
                lore.add(CC.GREEN + "Expired");
            } else {
                lore.add(CC.MENU_BAR);
                if (grant.getDuration() == -1)
                    lore.add(CC.YELLOW + "This is a permanent grant.");
                else lore.add(CC.YELLOW + "Time remaining: " + CC.RED
                        + TimeUtils.formatTimeShort(grant.getRemainingTime()));
                if (canGrant(player, grant.getRank())) {
                    lore.add(" ");
                    lore.add(CC.RED + CC.BOLD + "Click to remove this grant");
                }
            }
            lore.add(CC.MENU_BAR);
            return new ItemBuilder(Material.WOOL, (grant.isRemoved() || !grant.isActive() ?
                    DyeColor.RED.getWoolData() : DyeColor.LIME.getWoolData()))
                    .setDisplayName((grant.isActive() && !grant.isRemoved() ? CC.GREEN : CC.RED) + CC.BOLD
                            + TimeUtils.formatDate(grant.getGrantedAt(), InvictusSettings.TIME_ZONE.get(player)))
                    .setLore(lore)
                    .build();
        }

        @Override
        public void click(Player player, int slot, ClickType clickType, int hotbarButton) {
            if ((grant.isRemoved()) || (!grant.isActive()) || !canGrant(player, grant.getRank()))
                return;

            player.getOpenInventory().close();
            new GrantRemoveInput(invictus, target, grant).send(player);
        }
    }
}
