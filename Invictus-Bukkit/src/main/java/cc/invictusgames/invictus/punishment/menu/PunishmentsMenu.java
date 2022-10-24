package cc.invictusgames.invictus.punishment.menu;

import cc.invictusgames.ilib.builder.ItemBuilder;
import cc.invictusgames.ilib.chatinput.ChatInput;
import cc.invictusgames.ilib.menu.Button;
import cc.invictusgames.ilib.menu.buttons.BackButton;
import cc.invictusgames.ilib.menu.page.PagedMenu;
import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.ilib.utils.TimeUtils;
import cc.invictusgames.invictus.InvictusBukkit;
import cc.invictusgames.invictus.note.menu.NoteMenu;
import cc.invictusgames.invictus.playersetting.InvictusSettings;
import cc.invictusgames.invictus.profile.Profile;
import cc.invictusgames.invictus.punishment.Punishment;
import cc.invictusgames.invictus.utils.Tasks;
import lombok.RequiredArgsConstructor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.*;


/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 22.02.2020 / 19:16
 * Invictus / cc.invictusgames.invictus.spigot.punishment.menu
 */

@RequiredArgsConstructor
public class PunishmentsMenu extends PagedMenu {

    private final InvictusBukkit invictus;
    private final Profile target;
    private final Punishment.PunishmentType type;

    @Override
    public String getRawTitle(Player player) {
        return type.getName() + "s: " + target.getRealDisplayName();
    }

    @Override
    public Map<Integer, Button> getGlobalButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        buttons.put(4, new BackButton(new PunishmentsMainMenu(invictus, target)));
        return buttons;
    }

    @Override
    public Map<Integer, Button> getAllPagesButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        List<Punishment> punishments = new ArrayList<>(target.getPunishments(type));
        punishments.sort(Comparator.comparingLong(Punishment::getPunishedAt).reversed());
        int index = 0;
        for (Punishment punishment : punishments) {
            buttons.put(index++, new PunishmentButton(punishment));
        }
        return buttons;
    }

    private boolean isRemovable(Player player, Punishment punishment) {
        return (punishment.getPunishmentType().equals(Punishment.PunishmentType.WARN)
                || punishment.getPunishmentType().equals(Punishment.PunishmentType.KICK))
                && punishment.isActive()
                && !punishment.isRemoved()
                && player.hasPermission("invictus.punishment." + punishment.getPunishmentType().name() + ".remove");
    }

    @RequiredArgsConstructor
    public class PunishmentButton extends Button {

        private final Punishment punishment;

        @Override
        public ItemStack getItem(Player player) {
            List<String> lore = new ArrayList<>();
            String addedOn = punishment.getPunishedServerType()
                    + (punishment.getPunishedServer().isEmpty()
                    ? "" : CC.YELLOW + " : " + CC.RED + punishment.getPunishedServer());

            lore.add(CC.MENU_BAR);

            if (player.hasPermission("invictus.punishments.viewexecutor"))
                lore.add(CC.YELLOW + "By: " + CC.RED + punishment.resolvePunishedBy());
            else
                lore.add(CC.YELLOW + "By: " + CC.RED + CC.ITALIC + "Hidden");

            lore.add(CC.YELLOW + "Added on: " + CC.RED + addedOn);
            //lore.add(CC.YELLOW + "Reason: " + CC.RED + punishment.getPunishedReason());
            lore.addAll(NoteMenu.split("Reason", punishment.getPunishedReason()));
            if (punishment.isRemoved()) {
                lore.add(CC.MENU_BAR);
                lore.add(CC.RED + "Removed:");
                if (player.hasPermission("invictus.punishments.viewexecutor"))
                    //lore.add(CC.YELLOW + punishment.resolveRemovedBy() + ": " + CC.RED + punishment
                    // .getRemovedReason());
                    lore.addAll(NoteMenu.split(punishment.resolveRemovedBy(), punishment.getRemovedReason()));
                else
                    //lore.add(CC.YELLOW + CC.ITALIC + "Redacted" + CC.YELLOW + ": " + CC.RED + punishment
                    // .getRemovedReason());
                    lore.addAll(NoteMenu.split(CC.ITALIC + "Hidden", punishment.getRemovedReason()));

                lore.add(CC.RED + "at " + CC.YELLOW + TimeUtils.formatDate(punishment.getRemovedAt(),
                        InvictusSettings.TIME_ZONE.get(player)));
                lore.add(" ");
                lore.add(CC.YELLOW + "Duration: " + TimeUtils.formatTimeShort(punishment.getDuration()));
            } else if (!punishment.isActive()) {
                lore.add(CC.YELLOW + "Duration: " + TimeUtils.formatTimeShort(punishment.getDuration()));
                lore.add(CC.GREEN + "Expired");
            } else {
                lore.add(CC.MENU_BAR);
                if (punishment.getDuration() == -1)
                    lore.add(CC.YELLOW + "This is a permanent punishment.");
                else lore.add(CC.YELLOW + "Time Remaining: " + CC.RED
                        + TimeUtils.formatTimeShort(punishment.getRemainingTime()));

                if (isRemovable(player, punishment)) {
                    lore.add(" ");
                    lore.add(CC.RED + CC.BOLD + "Click to remove this punishment");
                }
            }

            lore.add(CC.MENU_BAR);

            return new ItemBuilder(Material.WOOL, (punishment.isRemoved() || !punishment.isActive()
                    ? DyeColor.RED.getWoolData() : DyeColor.LIME.getWoolData()))
                    .setDisplayName((punishment.isActive() && !punishment.isRemoved() ? CC.GREEN : CC.RED) + CC.BOLD +
                            TimeUtils.formatDate(punishment.getPunishedAt(), InvictusSettings.TIME_ZONE.get(player)))
                    .setLore(lore)
                    .build();
        }

        @Override
        public void click(Player whoClicked, int slot, ClickType clickType, int hotbarButton) {
            if (!isRemovable(whoClicked, punishment))
                return;

            whoClicked.getOpenInventory().close();

            new ChatInput<String>(String.class)
                    .text(CC.translate("&ePlease enter the reason for the removal of this punishment, " +
                            "or say &ccancel &eto cancel."))
                    .escapeMessage(CC.RED + "You cancelled the punishment removal.")
                    .accept((player, input) -> {
                        Tasks.runAsync(() -> {
                            if (invictus.getBukkitPunishmentService().removePunishment(
                                    player, target, punishment, input, true, false))
                                player.sendMessage(CC.GREEN + "Removed " + punishment.getPunishmentType().getName()
                                        + " of " + target.getRealDisplayName() + CC.GREEN + " for "
                                        + CC.YELLOW + input + CC.GREEN + ".");
                        });
                        return true;
                    }).send(whoClicked);
        }
    }
}
