package cc.invictusgames.invictus.punishment.commands;

import cc.invictusgames.ilib.command.annotation.Command;
import cc.invictusgames.ilib.command.annotation.Flag;
import cc.invictusgames.ilib.command.annotation.Param;
import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.ilib.utils.Timings;
import cc.invictusgames.ilib.utils.json.JsonBuilder;
import cc.invictusgames.invictus.InvictusBukkit;
import cc.invictusgames.invictus.connection.RequestHandler;
import cc.invictusgames.invictus.connection.RequestResponse;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 23.12.2020 / 23:13
 * Invictus / cc.invictusgames.invictus.spigot.punishment.commands
 */

@RequiredArgsConstructor
public class ClearPunishmentsCommand {

    private final InvictusBukkit invictus;

    @Command(names = {"clearpunishments"},
             permission = "console",
             description = "Clear all punishments with a specific type",
             async = true)
    public boolean clearPunishments(
            CommandSender sender,
            @Param(name = "reason", wildcard = true) String reason,
            @Flag(names = {"b"}, description = "Clear Bans") boolean bans,
            @Flag(names = {"B"}, description = "Clear Blacklists") boolean blacklists,
            @Flag(names = {"m"}, description = "Clear Mutes") boolean mutes,
            @Flag(names = {"w"}, description = "Clear Warns") boolean warns,
            @Flag(names = {"k"}, description = "Clear Kicks") boolean kicks,
            @Flag(names = {"-all"}, description = "Clear All Punishments") boolean all) {
        List<String> types = new ArrayList<>();
        if (bans)
            types.add("BAN");

        if (blacklists)
            types.add("BLACKLIST");

        if (mutes)
            types.add("MUTE");

        if (warns)
            types.add("WARN");

        if (kicks)
            types.add("KICKS");

        if (all)
            types.add("ALL");

        sender.sendMessage(CC.GREEN + "Starting to clear...");

        JsonBuilder body = new JsonBuilder();
        body.add("removedBy", sender instanceof Player ? ((Player) sender).getUniqueId().toString() : "Console");
        body.add("removedReason", reason);
        body.add("removedAt", System.currentTimeMillis());
        body.add("types", String.join(",", types));

        Timings timings = new Timings("punishment-clear").startTimings();
        RequestResponse response = RequestHandler.put("punishment/clear", body.build());
        timings.stopTimings();

        if (!response.wasSuccessful()) {
            sender.sendMessage(CC.format("Could not clear punishments: %s (%d)",
                    response.getErrorMessage(), response.getCode()));
            return false;
        }

        sender.sendMessage(CC.format("&aCleared &e%d &apunishments in &e%dms&a.",
                response.asObject().get("cleared").getAsInt(), timings.calculateDifference()));
        return true;
    }

}
