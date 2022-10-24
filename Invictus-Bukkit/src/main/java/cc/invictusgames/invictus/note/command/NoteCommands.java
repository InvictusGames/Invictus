package cc.invictusgames.invictus.note.command;

import cc.invictusgames.ilib.command.annotation.Command;
import cc.invictusgames.ilib.command.annotation.Param;
import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.invictus.InvictusBukkit;
import cc.invictusgames.invictus.connection.RequestHandler;
import cc.invictusgames.invictus.connection.RequestResponse;
import cc.invictusgames.invictus.note.Note;
import cc.invictusgames.invictus.note.menu.NoteMenu;
import cc.invictusgames.invictus.profile.Profile;
import cc.invictusgames.invictus.profile.packets.ProfileUpdatePacket;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 10.07.2020 / 05:00
 * Invictus / cc.invictusgames.invictus.spigot.note.command
 */

@RequiredArgsConstructor
public class NoteCommands {

    private final InvictusBukkit invictus;

    @Command(names = {"note check"},
             permission = "note.command.argument.check",
             description = "Check the notes of a player",
             playerOnly = true,
             async = true)
    public boolean noteCheck(Player sender, @Param(name = "player") Profile target) {
        sender.sendMessage(CC.YELLOW + "Loading Notes of " + target.getName() + "...");
        RequestResponse response = RequestHandler.get("profile/%s/notes", target.getUuid().toString());
        if (!response.wasSuccessful()) {
            sender.sendMessage(CC.format("&cCould not load notes: %s (%d)",
                    response.getErrorMessage(), response.getCode()));
            return true;
        }

        List<Note> notes = new ArrayList<>();
        response.asArray().forEach(element -> notes.add(new Note(element.getAsJsonObject())));
        new NoteMenu(invictus, target, notes).openMenu(sender);
        return true;
    }

    @Command(names = {"notes"},
             permission = "note.command.argument.check",
             description = "Check the notes of a player",
             playerOnly = true,
             async = true)
    public boolean notes(Player sender, @Param(name = "player") Profile target) {
        return noteCheck(sender, target);
    }

    @Command(names = {"note add"},
             permission = "note.command.argument.add",
             description = "Add a note to a player",
             async = true)
    public boolean noteAdd(CommandSender sender,
                           @Param(name = "player") Profile target,
                           @Param(name = "note", wildcard = true) String message) {
        Note note = new Note(
                target.getUuid(),
                sender instanceof Player ? ((Player) sender).getUniqueId().toString() : "Console",
                message,
                invictus.getServerName()
        );

        RequestResponse response = invictus.getBukkitProfileService().addNote(target, note);
        if (response.couldNotConnect()) {
            sender.sendMessage(CC.format("&cCould not connect to API to create note. " +
                            "Adding note to the queue. Error: %s (%d)",
                    response.getErrorMessage(), response.getCode()));
        } else if (!response.wasSuccessful()) {
            sender.sendMessage(CC.format("&cCould not create note: %s (%d)",
                    response.getErrorMessage(), response.getCode()));
            return false;
        }

        sender.sendMessage(CC.format("&aNoted &e%s &aon %s&a.", message, target.getRealDisplayName()));

        if (response.wasSuccessful())
            invictus.getRedisService().publish(new ProfileUpdatePacket(target.getUuid()));
        return true;
    }

}
