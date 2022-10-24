package cc.invictusgames.invictus.forum.commands;

import cc.invictusgames.ilib.command.annotation.Command;
import cc.invictusgames.ilib.command.annotation.CommandCooldown;
import cc.invictusgames.ilib.command.annotation.Param;
import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.ilib.utils.json.JsonBuilder;
import cc.invictusgames.invictus.connection.RequestHandler;
import cc.invictusgames.invictus.connection.RequestResponse;
import com.google.gson.JsonObject;
import org.bukkit.entity.Player;

import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

public class RegisterCommand {

    private static final char[] CHARS = "abcdefghijklmnopqrstuvw0123456789".toCharArray();
    private static final Pattern EMAIL_VALIDATE_PATTERN
            = Pattern.compile("^[a-zA-Z0-9_!#$%&’*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$");

    @Command(names = {"register"},
             permission = "xd",
             description = "Register for an forum account",
             async = true)
    @CommandCooldown(time = 5)
    public boolean register(Player sender, @Param(name = "email") String email) {
        if (!EMAIL_VALIDATE_PATTERN.matcher(email).matches()) {
            sender.sendMessage(CC.RED + "That is not an valid email address.");
            return false;
        }

        sender.sendMessage(CC.GREEN + "Attempting to send confirmation email...");

        JsonBuilder body = new JsonBuilder();
        body.add("token", getRandomId());
        body.add("email", email);

        RequestResponse response = RequestHandler.post(
                "forum/account/sendRegistration/%s",
                body.build(),
                sender.getUniqueId().toString()
        );

        if (!response.wasSuccessful()) {
            if (response.getResponse() != null && response.getResponse().getAsJsonObject().has("registered"))
                sender.sendMessage(CC.RED + "You already registered an forum account.");
            else if (response.getResponse() != null && response.getResponse().getAsJsonObject().has("emailInUse"))
                sender.sendMessage(CC.RED + "This email address is already in use.");
            else if (response.getCode() == 409)
                sender.sendMessage(CC.RED + "Failed to generate valid code. " +
                        "Please try again or contact server administration.");
            else sender.sendMessage(CC.format("&cFailed to complete registration request. Error: %s (%d)",
                        response.getErrorMessage(), response.getCode()));
            return true;
        }

        sender.sendMessage(CC.GREEN + "We've sent you an email containing further instructions " +
                "on how to activate your account.");
        return true;
    }


    private String getRandomId() {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 32; i++) {
            code.append(CHARS[ThreadLocalRandom.current().nextInt(CHARS.length)]);
        }
        return code.toString();
    }

}
