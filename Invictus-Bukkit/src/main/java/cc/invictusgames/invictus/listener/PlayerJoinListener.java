package cc.invictusgames.invictus.listener;

import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.ilib.utils.json.JsonBuilder;
import cc.invictusgames.invictus.InvictusBukkit;
import cc.invictusgames.invictus.connection.RequestHandler;
import cc.invictusgames.invictus.connection.RequestResponse;
import cc.invictusgames.invictus.disguise.BukkitDisguiseService;
import cc.invictusgames.invictus.profile.Profile;
import cc.invictusgames.invictus.profile.packets.ProfileUpdatePacket;
import cc.invictusgames.invictus.utils.NetworkBroadcastPacket;
import cc.invictusgames.invictus.utils.ProfileInventory;
import cc.invictusgames.invictus.utils.Tasks;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 15.03.2020 / 15:22
 * Invictus / cc.invictusgames.invictus.spigot.listener
 */

@RequiredArgsConstructor
public class PlayerJoinListener implements Listener {

    private final InvictusBukkit invictus;

    @EventHandler(/*priority = EventPriority.HIGHEST*/)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        Profile profile = invictus.getProfileService().getProfile(player);
        if (profile == null) {
            player.kickPlayer(CC.RED + "An occurred while loading your profile. If this continues to happen " +
                    "please contact the server administration");
            return;
        }

        if (profile.isDisguised()) {
            invictus.getBukkitDisguiseService().disguise(
                    profile,
                    profile.getDisguiseData().getDisguiseRank(),
                    profile.getDisguiseData().getDisguiseName(),
                    new BukkitDisguiseService.SkinPreset(
                            profile.getDisguiseData().getDisguiseName(),
                            profile.getDisguiseData().getTexture(),
                            profile.getDisguiseData().getSignature()
                    ),
                    false
            );
        }

        Tasks.runAsync(() -> {
            JsonBuilder body = new JsonBuilder();
            body.add("ip", hashIp(player.getAddress().getAddress().getHostAddress()));
            body.add("staff", player.hasPermission("invictus.staff"));
            body.add("server", invictus.getServerName());
            body.add("timeStamp", System.currentTimeMillis());

            RequestResponse response = RequestHandler.post("profile/%s/join", body.build(),
                    player.getUniqueId().toString());

            if (!response.wasSuccessful())
                return;

            player.setDisplayName(profile.getDisplayName());
            profile.getSession().startTimings();

            JsonObject object = response.asObject();
            profile.setRequiresAuthentication(object.has("requiresTotp")
                    && object.get("requiresTotp").getAsBoolean());

            if ((!object.has("lastServer")
                    || object.get("lastServer") == null
                    || object.get("lastServer").isJsonNull())
                    && player.hasPermission("invictus.staff")) {
                invictus.getRedisService().publish(new NetworkBroadcastPacket(
                        invictus.getMessageService().formatMessage(
                                "staff.join",
                                profile.getRealCurrentGrant().getRank().getPrefix()
                                        + profile.getName() + profile.getRealCurrentGrant().getRank().getSuffix(),
                                invictus.getServerName()
                        ),
                        "invictus.staff",
                        true
                ));
            }

            if (!invictus.getRedisService().executeBackendCommand(redis ->
                    redis.exists("totp:" + player.getUniqueId().toString() + ":secretKey"))
                    && player.hasPermission("invictus.staff")) {
                player.sendMessage(CC.RED + CC.BOLD + "Your two-factor authenticator is not set up. Start the " +
                        "setup now with " + CC.YELLOW + CC.BOLD + "/setup2fa" + CC.RED + CC.BOLD + ".");
            }

            if (profile.isDisguised() && !player.hasPermission("invictus.command.disguise")) {
                invictus.getBukkitDisguiseService().undisguise(profile, true);
                profile.save(() -> { }, true);
                player.sendMessage(CC.RED + "Your disguise has been removed as you are no longer allowed to disguise.");
            } else invictus.getRedisService().publish(new ProfileUpdatePacket(profile.getUuid()));


        });

        if (ProfileInventory.getCache().containsKey(player.getUniqueId()))
            ProfileInventory.getCache().get(player.getUniqueId()).handelJoin(player);
    }

    public String hashIp(String input) {
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
