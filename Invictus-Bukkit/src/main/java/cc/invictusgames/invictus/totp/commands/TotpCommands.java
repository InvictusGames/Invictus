package cc.invictusgames.invictus.totp.commands;

import cc.invictusgames.ilib.command.annotation.Command;
import cc.invictusgames.ilib.command.annotation.Param;
import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.ilib.uuid.UUIDCache;
import cc.invictusgames.invictus.InvictusBukkit;
import cc.invictusgames.invictus.InvictusBukkitPlugin;
import cc.invictusgames.invictus.profile.Profile;
import cc.invictusgames.invictus.totp.prompt.DisclaimerPrompt;
import cc.invictusgames.invictus.utils.NetworkBroadcastPacket;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.entity.Player;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 18.10.2020 / 02:03
 * Invictus / cc.invictusgames.invictus.spigot.totp.commands
 */

@RequiredArgsConstructor
public class TotpCommands {

    private final InvictusBukkit invictus;

    @Command(names = {"setup2fa", "2fasetup"},
             permission = "invictus.command.setup2fa",
             description = "Enable 2FA to verify your identity.",
             playerOnly = true,
             async = true)
    public boolean setup2fa(Player sender) {
        if (invictus.getRedisService().executeBackendCommand(redis ->
                redis.exists("totp:" + sender.getUniqueId().toString() + ":secretKey"))) {
            sender.sendMessage(CC.RED + "You already have 2FA set up!");
            return false;
        }

        Conversation conversation = new ConversationFactory(InvictusBukkitPlugin.getInstance())
                .withFirstPrompt(new DisclaimerPrompt(invictus))
                .withLocalEcho(false)
                .thatExcludesNonPlayersWithMessage("No")
                .buildConversation(sender);
        sender.beginConversation(conversation);
        return true;
    }

    @Command(names = {"auth", "authenticate", "2fa"},
             description = "Authenticate with 2FA to verify your identity.",
             playerOnly = true,
             async = true)
    public boolean auth(Player sender, @Param(name = "code", wildcard = true) String input) {
        Profile profile = invictus.getProfileService().getProfile(sender);
        if (!profile.isRequiresAuthentication()) {
            sender.sendMessage(CC.RED + "You don't need to authenticate at the moment.");
            return false;
        }

        if (!invictus.getRedisService().executeBackendCommand(redis ->
                redis.exists("totp:" + profile.getUuid().toString() + ":secretKey"))) {
            sender.sendMessage(CC.RED + "You don't have 2FA set up. Please contact the server administration.");
            return false;
        }

        input = input.replace(" ", "").replace("-", "");
        int code;
        try {
            code = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            sender.sendMessage(CC.YELLOW + input + CC.RED + " is not a valid code.");
            profile.setAuthenticationFailures(profile.getAuthenticationFailures() + 1);

            invictus.getRedisService().publish(new NetworkBroadcastPacket(
                    CC.format(
                            "&4&l[TOTP] &7[%s] %s &centered a wrong authentication code. &7(%d/3)",
                            invictus.getServerName(),
                            profile.getRealDisplayName(),
                            profile.getAuthenticationFailures()
                    ),
                    "invictus.admin",
                    true
            ));

            if (profile.getAuthenticationFailures() >= 3) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                        "ban " + sender.getName() + " [TOTP] Failed Authentication");
                return false;
            }
            return false;
        }

        if (invictus.getTotpService().getAuthenticator().authorizeUser(sender.getUniqueId().toString(), code)) {
            profile.setRequiresAuthentication(false);
            profile.setAuthenticationFailures(0);
            sender.sendMessage(CC.GREEN + "Your identity has been verified.");
            String ip = hashIp(sender.getAddress().getAddress().getHostAddress());
            profile.setLastIp(ip);
            if (!profile.getKnownIps().contains(ip)) {
                profile.getKnownIps().add(ip);
            }
            profile.save(() -> {
            }, false);
            return true;
        }

        sender.sendMessage(CC.YELLOW + input + CC.RED + " is not a valid code.");
        profile.setAuthenticationFailures(profile.getAuthenticationFailures() + 1);

        invictus.getRedisService().publish(new NetworkBroadcastPacket(
                CC.format(
                        "&4&l[TOTP] &7[%s] %s &centered a wrong authentication code. &7(%d/3)",
                        invictus.getServerName(),
                        profile.getRealDisplayName(),
                        profile.getAuthenticationFailures()
                ),
                "invictus.admin",
                true
        ));

        if (profile.getAuthenticationFailures() >= 3) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                    "ban " + sender.getName() + " [TOTP] Failed Authentication");
        }
        return false;
    }

    @Command(names = {"totp delete"},
             permission = "owner",
             description = "Forcefully delete the TOTP data of a player",
             async = true)
    public boolean totpDelete(CommandSender sender, @Param(name = "target") UUID uuid) {
        invictus.getRedisService().executeBackendCommand(redis -> {
            redis.del(
                    "totp:" + uuid.toString() + ":secretKey",
                    "totp:" + uuid.toString() + ":validationCode",
                    "totp:" + uuid.toString() + ":scratchCodes"
            );
            return null;
        });
        sender.sendMessage(CC.GREEN + "Successfully deleted all 2FA data of " + CC.YELLOW + UUIDCache.getName(uuid)
                + CC.GREEN + ".");
        return true;
    }

    @Command(names = {"totp info"},
             permission = "owner",
             description = "View TOTP data of a player",
             async = true)
    public boolean totpInfo(CommandSender sender, @Param(name = "target") UUID uuid) {
        return invictus.getRedisService().executeBackendCommand(redis -> {
            if (!redis.exists("totp:" + uuid.toString() + ":secretKey")) {
                sender.sendMessage(CC.YELLOW + UUIDCache.getName(uuid) + CC.RED + " does not have 2FA set up.");
                return false;
            }

            sender.sendMessage(CC.SMALL_CHAT_BAR);
            sender.sendMessage(CC.GOLD + CC.GOLD + "TOTP Info");
            sender.sendMessage(CC.YELLOW + " Player: " + CC.GOLD + CC.RIGHT_ARROW + " " + CC.WHITE
                    + UUIDCache.getName(uuid));
            sender.sendMessage(CC.YELLOW + " Secret: " + CC.GOLD + CC.RIGHT_ARROW + " " + CC.WHITE +
                    redis.get("totp:" + uuid.toString() + ":secretKey"));
            sender.sendMessage(CC.YELLOW + " Validation Code: " + CC.GOLD + CC.RIGHT_ARROW + " " + CC.WHITE +
                    redis.get("totp:" + uuid.toString() + ":validationCode"));
            sender.sendMessage(CC.YELLOW + " Scratch Codes: " + CC.GOLD + CC.RIGHT_ARROW + " " + CC.WHITE +
                    redis.get("totp:" + uuid.toString() + ":scratchCodes").replace(",", ", "));
            sender.sendMessage(CC.SMALL_CHAT_BAR);
            return true;
        });
    }


    //temp lol
    @Command(names = {"totp forceunlock"},
             permission = "owner",
             description = "Force unlock a player")
    public boolean totpForceUnlock(CommandSender sender, @Param(name = "target") Player target) {
        Profile profile = invictus.getProfileService().getProfile(target);
        profile.setRequiresAuthentication(false);
        profile.setAuthenticationFailures(0);
        sender.sendMessage(CC.GREEN + "Your identity has been verified.");
        String ip = hashIp(target.getAddress().getAddress().getHostAddress());
        profile.setLastIp(ip);

        if (!profile.getKnownIps().contains(ip))
            profile.getKnownIps().add(ip);

        profile.save(() -> { }, false);
        target.sendMessage(CC.BLUE + "You have been given 2fa bypass.");
        return true;
    }

    @Command(names = {"totp forcelockme"},
             permission = "op",
             description = "Forces you to authenticate yourself",
             playerOnly = true)
    public boolean totpForceAuthMe(Player sender) {
        Profile profile = invictus.getProfileService().getProfile(sender);
        profile.setRequiresAuthentication(true);
        sender.sendMessage(CC.BLUE + "You have been forced to authenticate.");
        return true;
    }

    private String hashIp(String input) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(input.getBytes());
            return new BigInteger(1, messageDigest.digest()).toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "N/A";
    }

}
