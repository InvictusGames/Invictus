package cc.invictusgames.invictus.totp.prompt;

import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.invictus.InvictusBukkit;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 18.10.2020 / 02:40
 * Invictus / cc.invictusgames.invictus.spigot.totp.chatinput
 */

public class QRCodeScanPrompt extends StringPrompt {

    private final InvictusBukkit invictus;
    private final GoogleAuthenticator authenticator;
    private GoogleAuthenticatorKey key;
    private int failures = 0;
    private ItemStack map;

    public QRCodeScanPrompt(InvictusBukkit invictus) {
        this.invictus = invictus;
        authenticator = invictus.getTotpService().getAuthenticator();
    }

    @Override
    public String getPromptText(ConversationContext context) {
        if (failures == 0) {
            Player player = (Player) context.getForWhom();
            key = authenticator.createCredentials(player.getUniqueId().toString());
            map = invictus.getTotpService().createQRMap(player, key);

            player.getInventory().addItem(map);
        }
        return CC.RED + "Scan the given map on your 2FA-Device. Once you've scanned the map, type the code displayed " +
                "on your 2FA-Device in chat.";
    }

    @Override
    public Prompt acceptInput(ConversationContext context, String input) {
        Player player = (Player) context.getForWhom();
        player.getInventory().remove(map);
        int code;
        try {
            code = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            if (testFailures(context)) {
                return Prompt.END_OF_CONVERSATION;
            }

            context.getForWhom().sendRawMessage(" ");
            context.getForWhom().sendRawMessage(CC.YELLOW + input + CC.RED + " isn't a valid code. Please try again.");
            return this;
        }

        if (authenticator.authorize(key.getKey(), code)) {
            context.getForWhom().sendRawMessage(CC.GREEN + "2FA setup completed successfully");
            return Prompt.END_OF_CONVERSATION;
        }

        if (testFailures(context)) {
            return Prompt.END_OF_CONVERSATION;
        }

        context.getForWhom().sendRawMessage(" ");
        context.getForWhom().sendRawMessage(CC.YELLOW + input + CC.RED + " isn't a valid code. Please try again.");
        return this;
    }

    private boolean testFailures(ConversationContext context) {
        Player player = (Player) context.getForWhom();
        if (failures++ >= 3) {
            context.getForWhom().sendRawMessage(CC.RED + "Cancelling 2FA setup due to too many incorrect codes.");
            invictus.getRedisService().executeBackendCommand(redis -> {
                redis.del(
                        "totp:" + player.getUniqueId().toString() + ":secretKey",
                        "totp:" + player.getUniqueId().toString() + ":validationCode",
                        "totp:" + player.getUniqueId().toString() + ":scratchCodes"
                );
                return null;
            });

            return true;
        }

        return false;
    }
}
