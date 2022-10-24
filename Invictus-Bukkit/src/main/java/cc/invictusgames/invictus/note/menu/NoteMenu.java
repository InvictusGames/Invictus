package cc.invictusgames.invictus.note.menu;

import cc.invictusgames.ilib.builder.ItemBuilder;
import cc.invictusgames.ilib.chatinput.ChatInput;
import cc.invictusgames.ilib.menu.Button;
import cc.invictusgames.ilib.menu.menu.ConfirmationMenu;
import cc.invictusgames.ilib.menu.page.PagedMenu;
import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.ilib.utils.TimeUtils;
import cc.invictusgames.ilib.utils.UUIDUtils;
import cc.invictusgames.ilib.uuid.UUIDCache;
import cc.invictusgames.invictus.InvictusBukkit;
import cc.invictusgames.invictus.connection.RequestResponse;
import cc.invictusgames.invictus.note.Note;
import cc.invictusgames.invictus.playersetting.InvictusSettings;
import cc.invictusgames.invictus.profile.Profile;
import cc.invictusgames.invictus.profile.packets.ProfileUpdatePacket;
import cc.invictusgames.invictus.utils.Tasks;
import com.google.common.base.Splitter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 10.07.2020 / 04:46
 * Invictus / cc.invictusgames.invictus.spigot.note.menu
 */

@RequiredArgsConstructor
public class NoteMenu extends PagedMenu {

    private final InvictusBukkit invictus;
    private final Profile target;
    private final List<Note> notes;

    @Override
    public Map<Integer, Button> getAllPagesButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        notes.sort(Comparator.comparingLong(Note::getAddedAt).reversed());
        notes.forEach(note -> buttons.put(buttons.size(), new NoteButton(note)));
        return buttons;
    }

    @Override
    public Map<Integer, Button> getGlobalButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        buttons.put(4, new AddNoteButton());
        return buttons;
    }

    @Override
    public String getRawTitle(Player player) {
        return "Notes: " + target.getRealDisplayName();
    }

    @Override
    public void onClose(Player player) {
        notes.clear();
    }

    @RequiredArgsConstructor
    public class NoteButton extends Button {

        private final Note note;

        @Override
        public ItemStack getItem(Player player) {
            List<String> lore = new ArrayList<>();
            String addedOn = (note.getAddedOn().equals("Staff Panel") ?
                    "" : "Server" + CC.YELLOW + " : ") + CC.RED + note.getAddedOn();

            lore.add(CC.MENU_BAR);
            lore.add(CC.YELLOW + "By: " + CC.RED + (UUIDUtils.isUUID(note.getAddedBy())
                    ? UUIDCache.getName(UUID.fromString(note.getAddedBy())) : note.getAddedBy()));
            lore.add(CC.YELLOW + "Added on: " + CC.RED + addedOn);
            lore.addAll(split("Note", note.getNote()));

            if (player.hasPermission("invicuts.note.remove")) {
                lore.add(" ");
                lore.add(CC.RED + CC.BOLD + "Click to remove this note");
            }
            lore.add(CC.MENU_BAR);
            return new ItemBuilder(Material.PAPER)
                    .setDisplayName(CC.YELLOW + CC.BOLD
                            + TimeUtils.formatDate(note.getAddedAt(), InvictusSettings.TIME_ZONE.get(player)))
                    .setLore(lore)
                    .build();
        }

        @Override
        public void click(Player player, int slot, ClickType clickType, int hotbarButton) {
            if (!player.hasPermission("invictus.note.remove")) {
                return;
            }
            new ConfirmationMenu("Remove note?", "Yes", "No", b -> {
                if (b) {
                    Tasks.runAsync(() -> {
                        RequestResponse response = invictus.getBukkitProfileService().removeNote(target, note);
                        if (!response.wasSuccessful())
                            player.sendMessage(CC.format("&cCould not remove note: %s (%d)",
                                    response.getErrorMessage(), response.getCode()));
                        else {
                            player.sendMessage(CC.format("&aRemoved note of %s&a.", target.getRealDisplayName()));
                            invictus.getRedisService().publish(new ProfileUpdatePacket(target.getUuid()));
                        }
                        Tasks.runLater(() -> Bukkit.dispatchCommand(player, "notes "
                                + target.getName()), 1L);
                    });
                } else Tasks.runLater(() -> Bukkit.dispatchCommand(player, "notes "
                        + target.getName()), 1L);
            }).openMenu(player);
        }
    }

    public class AddNoteButton extends Button {

        @Override
        public ItemStack getItem(Player player) {
            return new ItemBuilder(Material.SIGN).setDisplayName(CC.GREEN + CC.BOLD + "Add Note").build();
        }

        @Override
        public void click(Player whoClicked, int slot, ClickType clickType, int hotbarButton) {
            whoClicked.closeInventory();
            new ChatInput<String>(String.class)
                    .text(CC.translate("&ePlease enter the note in chat, or say &ccancel &eto cancel."))
                    .escapeMessage(CC.RED + "You cancelled the note add procedure.")
                    .accept((player, input) -> {
                        Note note = new Note(
                                target.getUuid(),
                                player.getUniqueId().toString(),
                                input,
                                invictus.getServerName()
                        );

                        Tasks.runAsync(() -> {
                            RequestResponse response = invictus.getBukkitProfileService().addNote(target, note);
                            if (response.couldNotConnect()) {
                                player.sendMessage(CC.format("&cCould not connect to API to create note. " +
                                                "Adding note to the queue. Error: %s (%d)",
                                        response.getErrorMessage(), response.getCode()));
                            } else if (!response.wasSuccessful()) {
                                player.sendMessage(CC.format("&cCould not create note: %s (%d)",
                                        response.getErrorMessage(), response.getCode()));
                                return;
                            }

                            player.sendMessage(CC.format("&aNoted &e%s &aon %s&a.",
                                    input, target.getRealDisplayName()));

                            if (response.wasSuccessful())
                                invictus.getRedisService().publish(new ProfileUpdatePacket(target.getUuid()));
                            Bukkit.dispatchCommand(player, "notes " + target.getName());
                        });
                        return true;
                    }).send(whoClicked);
        }
    }

    public static List<String> split(String prefix, String note) {
        List<String> list = new ArrayList<>();
        List<String> split = new ArrayList<>(Splitter.fixedLength(25).splitToList(note));

        for (int i = 0; i < split.size(); i++) {
            String s = split.get(i);
            if (i < split.size() - 1 && !s.endsWith(" ") && !split.get(i + 1).startsWith(" ")) {
                s = s + "-";
            }
            list.add((i == 0 ? CC.YELLOW + prefix + ": " : "") + CC.RED + s.trim());
        }
        return list;
    }
}
