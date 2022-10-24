package cc.invictusgames.invictus.listener;

import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.invictus.InvictusBukkit;
import cc.invictusgames.invictus.profile.Profile;
import cc.invictusgames.invictus.profile.packets.ProfileUpdatePacket;
import cc.invictusgames.invictus.utils.NetworkBroadcastPacket;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 24.03.2020 / 03:47
 * Invictus / cc.invictusgames.invictus.spigot.listener
 */

@RequiredArgsConstructor
public class PlayerLoginListener implements Listener {

    private static final Map<UUID, Boolean> OP_LIST_STATE = new ConcurrentHashMap<>();

    private final InvictusBukkit invictus;

    public static void setOpListState(UUID uuid, boolean state) {
        OP_LIST_STATE.put(uuid, state);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();

        Profile profile = invictus.getProfileService().getProfile(player);

        if (profile == null) {
            event.disallow(
                    PlayerLoginEvent.Result.KICK_OTHER,
                    CC.RED + "An occurred while loading your profile. If this continues to happen " +
                            "please contact the server administration."
            );
            return;
        }

        if (Bukkit.getPlayerExact(profile.getDisguiseName()) != null) {
            event.disallow(
                    PlayerLoginEvent.Result.KICK_OTHER,
                    CC.RED + "The player belonging to your disguise name is on that server, you cannot join."
            );
        } else if (Bukkit.getPlayerExact(profile.getName()) != null) {
            Profile target = invictus.getProfileService().getProfile(Bukkit.getPlayerExact(profile.getName()));
            if (!target.getUuid().equals(profile.getUuid())) {
                invictus.getBukkitDisguiseService().undisguise(target, true);
                target.player().sendMessage(CC.RED + "The player belonging to your disguise name has joined the " +
                        "server. You have been undisguised.");
            }
        }

        if (Bukkit.getOnlinePlayers().size() >= Bukkit.getMaxPlayers()
                && event.getResult() == PlayerLoginEvent.Result.KICK_FULL
                && player.hasPermission("invictus.maxslots.bypass")) {
            event.allow();
        }

        boolean state = OP_LIST_STATE.getOrDefault(player.getUniqueId(), false);
        if (player.isOp() && !state) {
            player.setOp(false);
            invictus.getRedisService().publish(new NetworkBroadcastPacket(
                    CC.format(
                            "&4&l[OpList] &7[%s] %s &chad op but was not on the op list.",
                            invictus.getServerName(),
                            profile.getRealDisplayName()
                    ),
                    "invictus.admin",
                    true
            ));
        }

        if (state && !player.isOp() && player.hasPermission("invictus.command.op"))
            player.setOp(true);

        OP_LIST_STATE.remove(player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerLoginLowest(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        invictus.getPermissionService().injectPlayer(player);

    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLoginMonitor(PlayerLoginEvent event) {
        if (event.getResult() == PlayerLoginEvent.Result.ALLOWED)
            return;

        Profile profile = invictus.getProfileService().getProfile(event.getPlayer());
        if (profile != null) {
            invictus.getProfileService().removeProfile(profile.getUuid());
            invictus.getRedisService().publish(new ProfileUpdatePacket(profile.getUuid()));
        }
    }

}
