package cc.invictusgames.invictus.listener;

import cc.invictusgames.invictus.InvictusBukkit;
import cc.invictusgames.invictus.base.StaffMode;
import cc.invictusgames.invictus.profile.Profile;
import cc.invictusgames.invictus.profile.packets.ProfileServerSwitchPacket;
import cc.invictusgames.invictus.queue.packet.QueuePlayerLeavePacket;
import cc.invictusgames.invictus.utils.NetworkBroadcastPacket;
import cc.invictusgames.invictus.utils.ProfileInventory;
import cc.invictusgames.invictus.utils.Tasks;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 28.02.2020 / 19:17
 * Invictus / cc.invictusgames.invictus.spigot.listener
 */

@RequiredArgsConstructor
public class PlayerQuitListener implements Listener {


    private final InvictusBukkit invictus;
    @Getter
    @Setter
    private static boolean asyncKicks = true;

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        handlePlayerQuit(player, true);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerKick(PlayerKickEvent event) {
        Player player = event.getPlayer();
        handlePlayerQuit(player, asyncKicks);
    }

    public void handlePlayerQuit(Player player, boolean async) {
        Profile profile = invictus.getProfileService().getProfile(player);
        if (profile == null)
            return;

        UUID uuid = player.getUniqueId();
        invictus.getRedisService().publish(new ProfileServerSwitchPacket(uuid,
                invictus.getServerName()));

        if (async)
            Tasks.runAsync(() -> invictus.getQueueService().resetQueueData(uuid));

        boolean hasPermission = player.hasPermission("invictus.staff");

        Tasks.runLaterAsync(() -> {
            if (!invictus.getConfirmedSwitch().contains(profile.getUuid())) {
                if (hasPermission) {
                    invictus.getRedisService().publish(new NetworkBroadcastPacket(
                            invictus.getMessageService().formatMessage(
                                    "staff.quit",
                                    profile.getRealCurrentGrant().getRank().getPrefix()
                                            + profile.getName() + profile.getRealCurrentGrant().getRank().getSuffix(),
                                    invictus.getServerName()
                            ),
                            "invictus.staff",
                            true
                    ));
                }
                profile.setLastServer(null);
                profile.setJoinTime(-1);
                invictus.getRedisService().publish(new QueuePlayerLeavePacket(profile.getUuid()));
            } else {
                invictus.getConfirmedSwitch().remove(profile.getUuid());
            }

            profile.setLastSeen(System.currentTimeMillis());
            profile.getSession().stopTimings();
            profile.save(() -> {}, true);
        }, 20);

        StaffMode staffMode = StaffMode.get(player);
        if (staffMode.isEnabled())
            staffMode.toggleEnabled(true);

        StaffModeListener.removeLastLocation(player);

        if (profile.isFrozen()) {
            FreezeListener.getFrozenCache().add(profile.getUuid());
            invictus.getRedisService().publish(new NetworkBroadcastPacket(
                    invictus.getMessageService().formatMessage(
                            "staff.freeze.quit",
                            invictus.getServerName(),
                            profile.getDisplayName()
                    ),
                    "invictus.staff",
                    true
            ));
        }
        invictus.getPermissionService().uninjectPlayer(player);

        invictus.getProfileService().removeProfile(uuid);

        if (ProfileInventory.getCache().containsKey(uuid))
            ProfileInventory.getCache().get(uuid).handleQuit(player);
    }

}
