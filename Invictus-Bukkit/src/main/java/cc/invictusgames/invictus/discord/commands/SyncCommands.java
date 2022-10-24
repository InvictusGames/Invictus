package cc.invictusgames.invictus.discord.commands;

import cc.invictusgames.ilib.command.annotation.Command;
import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.ilib.utils.ChatMessage;
import cc.invictusgames.ilib.utils.json.JsonBuilder;
import cc.invictusgames.invictus.InvictusBukkit;
import cc.invictusgames.invictus.connection.RequestHandler;
import cc.invictusgames.invictus.connection.RequestResponse;
import cc.invictusgames.invictus.profile.Profile;
import lombok.RequiredArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 20.10.2020 / 05:11
 * Invictus / cc.invictusgames.invictus.spigot.discord.commands
 */

@RequiredArgsConstructor
public class SyncCommands {

    private static final char[] CHARS = "abcdefghijklmnopqrstuvw0123456789".toCharArray();

    private final InvictusBukkit invictus;

    @Command(names = {"sync"},
             description = "Sync your minecraft account with discord",
             playerOnly = true,
             async = true)
    public boolean sync(Player sender) {
        Profile profile = invictus.getProfileService().getProfile(sender);
        String code = getRandomCode();

        JsonBuilder body = new JsonBuilder();
        body.add("uuid", profile.getUuid());
        body.add("code", code);

        RequestResponse response = RequestHandler.post("discord/ingame", body.build());
        if (!response.wasSuccessful()) {
            if (response.getCode() == 409)
                sender.sendMessage(CC.RED + "Failed to generate valid code. " +
                        "Please try again or contact server administration.");
            else sender.sendMessage(CC.format("&cFailed to complete sync request. Error: %s (%d)",
                    response.getErrorMessage(), response.getCode()));
            return false;
        }

        if (response.getCode() == 201) {
            ChatMessage message = new ChatMessage("You have started the synchronization. Join our discord at ")
                    .color(ChatColor.YELLOW);
            message.add(invictus.getMessageService().formatMessage("discord-link")).color(ChatColor.AQUA);
            message.add(" and type ").color(ChatColor.YELLOW);
            message.add("-sync " + code)
                    .color(ChatColor.AQUA)
                    .hoverText(CC.YELLOW + "Click here to copy.")
                    .suggestCommand("-sync " + code);
            message.add(" in #sync.").color(ChatColor.YELLOW);
            message.send(sender);
            return true;
        }

        if (response.asObject().has("code")) {
            code = response.asObject().get("code").getAsString();
            ChatMessage message = new ChatMessage("You have already requested a synchronization. Your code is: ")
                    .color(ChatColor.YELLOW);
            message.add(code)
                    .color(ChatColor.AQUA)
                    .hoverText(CC.YELLOW + "Click here to copy.")
                    .suggestCommand(code);
            message.add(".").color(ChatColor.YELLOW);
            message.send(sender);
            return false;
        }

        if (response.asObject().has("alreadySynced")) {
            sender.sendMessage(CC.RED + "You have already synced your account.");
            return false;
        }

        sender.sendMessage("How the fuck did you get here???");
        return true;
    }

    private String getRandomCode() {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            code.append(CHARS[ThreadLocalRandom.current().nextInt(CHARS.length)]);
        }
        return code.toString();
    }

}
