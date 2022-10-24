package cc.invictusgames.invictus.grant.menu;

import cc.invictusgames.ilib.builder.ItemBuilder;
import cc.invictusgames.ilib.menu.Button;
import cc.invictusgames.ilib.menu.Menu;
import cc.invictusgames.ilib.redis.packet.Packet;
import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.ilib.utils.TimeUtils;
import cc.invictusgames.invictus.InvictusBukkit;
import cc.invictusgames.invictus.connection.RequestResponse;
import cc.invictusgames.invictus.grant.Grant;
import cc.invictusgames.invictus.grant.packets.GrantAddPacket;
import cc.invictusgames.invictus.grant.procedure.GrantProcedure;
import cc.invictusgames.invictus.profile.Profile;
import cc.invictusgames.invictus.profile.packets.ProfileUpdatePacket;
import cc.invictusgames.invictus.server.ServerInfo;
import cc.invictusgames.invictus.utils.Tasks;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 19.02.2020 / 18:35
 * Invictus / cc.invictusgames.invictus.spigot.grant.menu
 */
@RequiredArgsConstructor
public class GrantScopesMenu extends Menu {

    private final InvictusBukkit invictus;
    private final Profile profile;
    private List<String> scopes = new ArrayList<>();
    private boolean clicked = false;

    @Override
    public String getTitle(Player player) {
        return "Select scopes: " + profile.getGrantProcedure().getRank().getDisplayName();
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        List<String> existingScopes = new ArrayList<>();
        int index = 0;
        for (ServerInfo server : ServerInfo.getServers()) {
            if (!existingScopes.contains(server.getGrantScope().toLowerCase())) {
                buttons.put(index++, new ScopeButton(server.getGrantScope().toLowerCase()));
                existingScopes.add(server.getGrantScope().toLowerCase());
            }
        }
        buttons.put(22, new ScopeButton("GLOBAL"));
        buttons.put(31, new Button() {
            @Override
            public ItemStack getItem(Player player) {
                GrantProcedure procedure = profile.getGrantProcedure();
                if (scopes.isEmpty()) {
                    return new ItemBuilder(Material.WOOD_SWORD)
                            .setDisplayName(CC.RED + CC.BOLD + "Confirm and grant")
                            .setLore(
                                    CC.MENU_BAR,
                                    CC.RED + "Please select at least one scope.",
                                    CC.MENU_BAR
                            ).build();
                }
                return new ItemBuilder(Material.DIAMOND_SWORD)
                        .setDisplayName(CC.GREEN + CC.BOLD + "Confirm and grant")
                        .setLore(
                                CC.MENU_BAR,
                                CC.format("&eClick to grant %s &ethe %s &erank",
                                        procedure.getTarget().getRealDisplayName(),
                                        procedure.getRank().getDisplayName()),
                                CC.YELLOW + (scopes.contains("GLOBAL") ? "This grant will be " + CC.RED + "Global"
                                        : "This grant will apply on: " + CC.RED + StringUtils.join(scopes, ", ")),
                                CC.format("&eReasoning: &c%s", procedure.getReason()),
                                CC.format("&eDuration: &c%s", TimeUtils.formatDetailed(procedure.getDuration())),
                                CC.MENU_BAR
                        ).build();
            }

            @Override
            public void click(Player player, int slot, ClickType clickType, int hotbarButton) {
                if (scopes.isEmpty()) {
                    return;
                }

                clicked = true;
                player.closeInventory();

                Tasks.runAsync(() -> {
                    GrantProcedure procedure = profile.getGrantProcedure();
                    Profile target = procedure.getTarget();
                    Grant grant = new Grant(
                            invictus,
                            procedure.getTarget().getUuid(),
                            procedure.getRank(),
                            procedure.getProfile().getUuid().toString(),
                            System.currentTimeMillis(),
                            procedure.getReason(),
                            procedure.getDuration(),
                            scopes
                    );

                    /*Packet packet = new GrantAddPacket(target.getUuid(), grant.getRank().getUuid(),
                            grant.getDuration());*/
                    RequestResponse response = invictus.getBukkitProfileService().addGrant(target, grant);
                    if (response.couldNotConnect()) {
                        player.sendMessage(CC.format("&cCould not connect to API to create grant. " +
                                        "Adding grant to the queue. Error: %s (%d)",
                                response.getErrorMessage(), response.getCode()));
                    } else if (!response.wasSuccessful()) {
                        player.sendMessage(CC.format("&cCould not create grant: %s (%d)",
                                response.getErrorMessage(), response.getCode()));
                        return;
                    }

                    if (grant.getDuration() == -1)
                        player.sendMessage(CC.format(
                                "&aYou've &epermanently &agranted %s&a the %s&a rank.",
                                target.getRealDisplayName(),
                                procedure.getRank().getDisplayName()
                        ));
                    else
                        player.sendMessage(CC.format(
                                "&aYou've granted %s&a the %s&a rank for &e%s&a.",
                                target.getRealDisplayName(),
                                procedure.getRank().getDisplayName(),
                                TimeUtils.formatDetailed(grant.getDuration())
                        ));
                });
            }
        });
        return buttons;
    }

    @Override
    public void onClose(Player player) {
        if (!clicked) {
            Profile profile = invictus.getProfileService().getProfile(player);
            profile.setGrantProcedure(null);
            player.sendMessage(CC.RED + "You cancelled the grant procedure.");
        }
    }

    @Override
    public boolean isAutoUpdate() {
        return false;
    }

    @Override
    public boolean isClickUpdate() {
        return true;
    }

    @RequiredArgsConstructor
    public class ScopeButton extends Button {

        private final String server;

        @Override
        public ItemStack getItem(Player player) {
            return new ItemBuilder(Material.WOOL, (scopes.contains(server) ? DyeColor.LIME.getWoolData() :
                    DyeColor.SILVER.getWoolData()))
                    .setDisplayName((scopes.contains(server) ? CC.GREEN : CC.GRAY) + CC.BOLD + WordUtils.capitalizeFully(server))
                    .build();
        }

        @Override
        public void click(Player player, int slot, ClickType clickType, int hotbarButton) {
            if (!scopes.contains(server))
                scopes.add(server);
            else
                scopes.remove(server);

        }
    }
}
