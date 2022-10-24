package cc.invictusgames.invictus.listener;

import cc.invictusgames.ilib.redis.packet.Packet;
import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.ilib.utils.TimeUtils;
import cc.invictusgames.ilib.utils.json.JsonBuilder;
import cc.invictusgames.invictus.InvictusBukkit;
import cc.invictusgames.invictus.connection.RequestHandler;
import cc.invictusgames.invictus.connection.RequestResponse;
import cc.invictusgames.invictus.profile.Profile;
import cc.invictusgames.invictus.punishment.Punishment;
import cc.invictusgames.invictus.punishment.packets.PunishmentCreatePacket;
import cc.invictusgames.invictus.utils.NetworkBroadcastPacket;
import cc.invictusgames.invictus.utils.Tasks;
import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 19.02.2020 / 19:29
 * Invictus / cc.invictusgames.invictus.spigot.listener
 */

@RequiredArgsConstructor
public class PlayerPreLoginListener implements Listener {

    private final InvictusBukkit invictus;

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        if ((!invictus.getRankService().isLoaded())) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, CC.RED + "You cannot join while the server is " +
                    "starting.");
            return;
        }

        if (Bukkit.getPlayer(event.getUniqueId()) != null) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
                    CC.RED + "You are already logged in on the server. Try again in a few seconds.");
            Tasks.runLater(() -> Bukkit.getPlayer(event.getUniqueId()).kickPlayer(
                    CC.RED + "You have been logged in from another location."), 20L);
            return;
        }

        String ip = hashIp(event.getAddress().getHostAddress());

        JsonBuilder body = new JsonBuilder();
        body.add("name", event.getName());
        body.add("ip", ip);
        body.add("timeStamp", System.currentTimeMillis());
        body.add("server", invictus.getServerName());
        body.add("grantScope", invictus.getServerGroup());

        RequestResponse response = RequestHandler.post("profile/%s/login",
                body.build(), event.getUniqueId().toString());
        if (!response.wasSuccessful()) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, CC.format("&cFailed to load profile: %s (%d)",
                    response.getErrorMessage(), response.getCode()));
            return;
        }

        JsonObject object = response.asObject();
        Profile profile = new Profile(invictus, object.get("profile").getAsJsonObject());
        boolean evasionPunishment = object.has("evasionPunishment")
                && object.get("evasionPunishment").getAsBoolean();

        if (object.has("activePunishment") && evasionPunishment) {
            Punishment punishment = new Punishment(invictus, object.get("activePunishment").getAsJsonObject());
            if (punishment.getPunishmentType() == Punishment.PunishmentType.BLACKLIST
                    || punishment.getPunishmentType() == Punishment.PunishmentType.BAN)
                invictus.getRedisService().publish(new PunishmentCreatePacket(
                        event.getUniqueId(),
                        punishment.getPunishmentType(),
                        CC.DRED + CC.BOLD + "Console",
                        punishment.getPunishedReason(),
                        punishment.getPunishedServerType(),
                        punishment.getPunishedServer(),
                        punishment.getDuration(),
                        Sets.newHashSet("silent")
                ));
        }

        if (object.has("activePunishment") && (!evasionPunishment
                || !profile.hasGrantOf("evasion-bypass"))
                && !invictus.getServerGroup().equals("sync")) {
            Punishment punishment = new Punishment(invictus, object.get("activePunishment").getAsJsonObject());
            if (punishment.getPunishmentType() == Punishment.PunishmentType.BLACKLIST) {
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, CC.format(
                        "&cYour account has been blacklisted from the %s Network.\n\n" +
                                "&cThis type of punishment cannot be appealed.",
                        invictus.getMessageService().formatMessage("network-name")
                ));
                return;
            }

            if (punishment.getPunishmentType() == Punishment.PunishmentType.BAN) {
                if (punishment.getDuration() == -1)
                    event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, CC.format(
                            "&cYour account has been suspended from the %s Network.\n\n"
                                    + "&cIf you feel this punishment is unfair, you may appeal at &e%s&c.",
                            invictus.getMessageService().formatMessage("network-name"),
                            invictus.getMessageService().formatMessage("discord-link")
                    ));
                else event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, CC.format(
                        "&cYour account has been suspended from the %s Network.\n" +
                                "&cThis punishment expires in &e%s&c.\n\n" +
                                "&cIf you feel this punishment is unfair, you may appeal at &e%s&c.",
                        invictus.getMessageService().formatMessage("network-name"),
                        TimeUtils.formatDetailed(punishment.getRemainingTime()),
                        invictus.getMessageService().formatMessage("discord-link")
                ));
                return;
            }
        }

        invictus.getProfileService().getProfiles().put(profile.getUuid(), profile);
        profile.setNitroBoosted(object.get("boosted").getAsBoolean());
        //profile.setNitroBoosted(response.wasSuccessful() && response.asObject().get("boosted").getAsBoolean());

        if (invictus.getMainConfig().isAntiVPN()
                && !profile.hasGrantOf("vpn-bypass")
                && object.has("isOnVPN")
                && object.get("isOnVPN").getAsBoolean())
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, ChatColor.RED
                    + "You are not allowed to connect with a vpn.");


        response = RequestHandler.get("oplist");
        if (!response.wasSuccessful())
            return;

        JsonPrimitive uuidPrimitive = new JsonPrimitive(event.getUniqueId().toString());
        PlayerLoginListener.setOpListState(event.getUniqueId(), response.asArray().contains(uuidPrimitive));
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
