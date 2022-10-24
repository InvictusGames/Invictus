package cc.invictusgames.invictus.listener;

import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.invictus.InvictusBungee;
import cc.invictusgames.invictus.InvictusBungeePlugin;
import cc.invictusgames.invictus.rank.Rank;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;

@RequiredArgsConstructor
public class LoginListener implements Listener {

    private final InvictusBungee invictus;

    @EventHandler
    public void onPreLogin(LoginEvent event) {
        event.registerIntent(InvictusBungeePlugin.getInstance());

        String ip = hashIp(event.getConnection().getAddress().getHostName());
        invictus.getProfileService()
                .getProfileOrCreate(event.getConnection().getUniqueId(), event.getConnection().getName(), ip,
                        profile -> {
                            if (profile == null) {
                                event.setCancelled(true);
                                event.setCancelReason(CC.RED + "Failed to load your profile. Please try again or " +
                                        "contact the server administration.");
                                event.completeIntent(InvictusBungeePlugin.getInstance());
                                return;
                            }

                            if (!profile.getName().equals(event.getConnection().getName())) {
                                profile.setName(event.getConnection().getName());
                                profile.save(() -> { }, true);
                            }

                            if (invictus.getMainConfig().getMaintenanceLevel() > 0
                                    && profile.getRealCurrentGrant().getRank().getWeight() < invictus.getMainConfig().getMaintenanceLevel()
                                    && !invictus.getMainConfig().getMaintenanceList().contains(event.getConnection().getUniqueId())) {
                                Rank rank = getClosestMaintenanceRank();
                                event.setCancelReason(invictus.getMessageService().formatMessage("maintenance-kick",
                                        rank == null ? "???" : rank.getDisplayName())
                                        .replace("\\n", "\n"));
                                event.setCancelled(true);
                            }

                            event.completeIntent(InvictusBungeePlugin.getInstance());
                        }, true);
    }
    
    /*@EventHandler
    public void onLogin(LoginEvent event) {
        Profile profile = invictus.getProfileService().getProfile(event.getConnection().getUniqueId());
        if (invictus.getMainConfig().getMaintenanceLevel() > 0
                && profile.getRealCurrentGrant().getRank().getWeight() < invictus.getMainConfig().getMaintenanceLevel()
                && !invictus.getMainConfig().getMaintenanceList().contains(event.getConnection().getUniqueId())) {
            Rank rank = getClosestMaintenanceRank();
            event.setCancelReason(invictus.getMessageService().formatMessage("maintenance-kick",
                    rank == null ? "???" : rank.getDisplayName()));
            event.setCancelled(true);
        }
    }*/

    private Rank getClosestMaintenanceRank() {
        Rank closest = null;

        for (Rank rank : invictus.getRankService().getRanksSorted()) {
            if (rank.getWeight() >= invictus.getMainConfig().getMaintenanceLevel()) {
                if (closest == null) {
                    closest = rank;
                    continue;
                }

                if (rank.getWeight() > closest.getWeight())
                    closest = rank;
            }
        }

        return closest;
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

    @EventHandler
    public void onPostLogin(PostLoginEvent event) {
        InvictusBungee.getBungeeInstance().getPermissionService().updatePermissions(event.getPlayer());
    }

}
