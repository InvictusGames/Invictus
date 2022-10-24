package cc.invictusgames.invictus.listener;

import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.invictus.Invictus;
import cc.invictusgames.invictus.InvictusBukkit;
import cc.invictusgames.invictus.playersetting.InvictusSettings;
import cc.invictusgames.invictus.profile.Profile;
import cc.invictusgames.invictus.utils.NetworkBroadcastPacket;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 20.02.2020 / 14:03
 * Invictus / cc.invictusgames.invictus.spigot.listener
 */

@RequiredArgsConstructor
public class AsyncPlayerChatListener implements Listener {

    private final InvictusBukkit invictus;

    @EventHandler(priority = EventPriority.HIGH)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        Profile profile = invictus.getProfileService().getProfile(player);

        if (profile == null || event.isCancelled()) {
            return;
        }

        if (profile.isRequiresAuthentication()) {
            player.sendMessage(CC.RED + "Please authenticate using " + CC.YELLOW + "/auth <code>"
                    + CC.RED +".");
            event.setCancelled(true);
            return;
        }

        if (profile.isFrozen()) {
            String message = CC.RED + CC.BOLD + "[Frozen] " + getDisplayName(profile, null)
                    + CC.GRAY + ": " + CC.YELLOW + event.getMessage();
            invictus.getRedisService().publish(new NetworkBroadcastPacket(
                    message,
                    "invictus.staff",
                    true,
                    invictus.getServerName()

            ));
            player.sendMessage(message);
            event.setCancelled(true);
        }
    }

    private String getDisplayName(Profile profile, Player target) {
        String primeIcon = CC.GRAY + "[" + InvictusSettings.PRIME_COLOR.get(profile.player())
                + Invictus.PRIME_ICON + CC.GRAY + "] ";
        return (profile.hasPrimeStatus() ? primeIcon : "") +
                profile.getCurrentGrant().getRank().getPrefix() +
                (profile.isDisguised() ? profile.getDisguiseName() : profile.getName()) +
                profile.getCurrentGrant().getRank().getSuffix() +
                ((target == null || target.hasPermission("invictus.disguise.bypass")) && profile.isDisguised() ?
                        CC.GRAY + "(" + profile.getName() + ")" : "");
    }

}
