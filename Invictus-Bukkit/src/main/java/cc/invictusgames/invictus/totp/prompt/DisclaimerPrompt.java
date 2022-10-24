package cc.invictusgames.invictus.totp.prompt;

import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.invictus.InvictusBukkit;
import lombok.RequiredArgsConstructor;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 18.10.2020 / 02:04
 * Invictus / cc.invictusgames.invictus.spigot.totp.chatinput
 */

@RequiredArgsConstructor
public class DisclaimerPrompt extends StringPrompt {

    private static final String MESSAGE = "&c&lTake a minute to read over this, it's important.\n &c2FA is required " +
            "for a staff member to protect against hackers getting into their Minecraft account and doing serious " +
            "damage to the server. If you enable 2FA, a code will be sent to your 2FA-Device every time you log in " +
            "with a different IP-Address.\n &c&lIf you lose your 2FA-Device, you won't be able to log in anymore.\n " +
            "&eIf you've read the above and would like to proceed, type &ayes &ein chat. Otherwise, type &cno&e.";

    private final InvictusBukkit invictus;

    @Override
    public String getPromptText(ConversationContext context) {
        return CC.translate(MESSAGE);
    }

    @Override
    public Prompt acceptInput(ConversationContext context, String input) {
        if (input.equalsIgnoreCase("yes")) {
            return new QRCodeScanPrompt(invictus);
        }

        context.getForWhom().sendRawMessage(CC.GREEN + "Cancelled 2FA Setup.");
        return Prompt.END_OF_CONVERSATION;
    }
}
