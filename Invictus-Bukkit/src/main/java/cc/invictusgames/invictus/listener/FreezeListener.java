package cc.invictusgames.invictus.listener;

import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.invictus.Invictus;
import cc.invictusgames.invictus.profile.Profile;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 10.10.2020 / 19:55
 * Invictus / cc.invictusgames.invictus.spigot.listener
 */

@RequiredArgsConstructor
public class FreezeListener implements Listener {

    private final Invictus invictus;
    @Getter
    private static final List<UUID> frozenCache = new ArrayList<>();
    private static final List<String> ALLOWED_COMMANDS = Arrays.asList("/freeze", "/ss", "/auth", "/authenticate",
            "/2fa", "/message", "/msg", "/m", "/tell", "/whisper", "/w", "/reply", "/r",
            "/respond");

    // How is there still SO FUCKING MANY CRASH EXPLOITS IN WORLD EDIT LIKE FIX YOUR SHIT PLUGIN HELLO!?
    private static final List<String> DISALLOWED_WORLDEDIT_COMMANDS = Arrays.asList("calc", "eval", "evaluate",
            "to", "targetoffset", "solve", "targetmask");

    public static boolean isDisallowedWorldEditCommand(String message) {
        for (String command : FreezeListener.DISALLOWED_WORLDEDIT_COMMANDS) {
            if (message.startsWith("/worldedit:")
                    || message.startsWith("/" + command + " ")
                    || message.startsWith("//" + command)
                    || message.startsWith("/worldedit:" + command)
                    || message.startsWith("/worldedit:/" + command)) {
                return true;
            }
        }

        return false;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Profile profile = invictus.getProfileService().getProfile(event.getPlayer());
        isFrozen(event.getPlayer(), true);
        if (frozenCache.contains(profile.getUuid())) {
            profile.setFrozen(true);
            frozenCache.remove(profile.getUuid());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!isFrozen(event.getPlayer(), false))
            return;

        Location from = event.getFrom();
        Location to = event.getTo();
        if (from.getX() != to.getX() || from.getZ() != to.getZ())
            event.setTo(event.getFrom());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (isFrozen(event.getPlayer(), true))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        if (isFrozen(event.getPlayer(), true))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (isFrozen(event.getPlayer(), true))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        String message = event.getMessage().toLowerCase();
        if (isDisallowedWorldEditCommand(message)) {
            event.setCancelled(true);
            return;
        }

        for (String allowedCommand : FreezeListener.ALLOWED_COMMANDS) {
            if (message.startsWith(allowedCommand))
                return;
        }

        if (isFrozen(event.getPlayer(), true))
            event.setCancelled(true);

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player && isFrozen((Player) event.getEntity(), false))
            event.setCancelled(true);
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Player player = parseDamager(event.getEntity());
        Player damager = parseDamager(event.getDamager());

        if (player == null || damager == null)
            return;

        Profile profile = invictus.getProfileService().getProfile(player);
        if (profile == null)
            return;

        if (profile.isFrozen()) {
            damager.sendMessage(profile.getDisplayName(damager) + CC.RED + " is currently frozen and cannot be " +
                    "damaged.");
            event.setCancelled(true);
            return;
        }

        profile = invictus.getProfileService().getProfile(damager);
        if (profile.isFrozen()) {
            damager.sendMessage(CC.RED + "You cannot do this whilst frozen.");
            event.setCancelled(true);
        }
    }

    private Player parseDamager(Entity entity) {
        if (entity instanceof Player)
            return (Player) entity;

        if (entity instanceof Projectile && ((Projectile) entity).getShooter() instanceof Player)
            return (Player) ((Projectile) entity).getShooter();

        return null;
    }


    private boolean isFrozen(Player player, boolean message) {
        Profile profile = invictus.getProfileService().getProfile(player);

        if (profile == null)
            return false;

        if (profile.isRequiresAuthentication()) {
            if (message)
                player.sendMessage(CC.RED + "Please authenticate using " + CC.YELLOW + "/auth <code>"
                        + CC.RED + ".");
            return true;
        }

        if (profile.isFrozen()) {
            if (message)
                player.sendMessage(CC.RED + "You cannot do this whilst frozen.");
            return true;
        }

        return false;
    }

}
