package cc.invictusgames.invictus.listener;

import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.invictus.InvictusBukkit;
import cc.invictusgames.invictus.base.StaffMode;
import cc.invictusgames.invictus.base.menu.staffmode.ExamineBlockMenu;
import cc.invictusgames.invictus.profile.Profile;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Openable;
import org.bukkit.material.Redstone;

import java.util.*;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 11.07.2020 / 10:02
 * Invictus / cc.invictusgames.invictus.spigot.listener
 */

@RequiredArgsConstructor
public class StaffModeListener implements Listener {

    private final InvictusBukkit invictus;
    private static final Map<UUID, Location> LAST_LOCATION = new HashMap<>();
    public static final Map<UUID, UUID> JUMP_TO_TARGET = new HashMap<>();
    public static String JUMP_TO_TELEPORT_COMMAND = "tp %s";

    private static final List<Material> DENY_INTERACT = Arrays.asList(
            Material.FLINT_AND_STEEL,
            Material.FIREBALL,
            Material.MONSTER_EGG,
            Material.MINECART,
            Material.COMMAND_MINECART,
            Material.EXPLOSIVE_MINECART,
            Material.HOPPER_MINECART,
            Material.POWERED_MINECART,
            Material.STORAGE_MINECART,
            Material.BOAT,
            Material.ITEM_FRAME,
            Material.PAINTING,
            Material.SNOW
    );

    private static final List<ItemStack> DROP_PROTECTED = Arrays.asList(
            StaffMode.COMPASS,
            StaffMode.INSPECT,
            StaffMode.CARPET,
            StaffMode.AXE,
            StaffMode.RANDOM_TP,
            StaffMode.LAST_FIGHT,
            StaffMode.VANISH_ON,
            StaffMode.VANISH_OFF
    );

    public static void addDropProtected(ItemStack... items) {
        DROP_PROTECTED.addAll(Arrays.asList(items));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Profile profile = invictus.getProfileService().getProfile(player.getUniqueId());

        if (player.hasPermission("invictus.command.staffmode") && invictus.getMainConfig().isStaffModeOnJoin())
            StaffMode.get(player).toggleEnabled(false);

        if (JUMP_TO_TARGET.containsKey(player.getUniqueId())) {
            Player target = Bukkit.getPlayer(JUMP_TO_TARGET.get(player.getUniqueId()));
            if (target != null)
                Bukkit.dispatchCommand(player, String.format(JUMP_TO_TELEPORT_COMMAND, target.getName()));
            JUMP_TO_TARGET.remove(player.getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!testBuild(event.getPlayer(), true)) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!testBuild(event.getPlayer(), true)) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        StaffMode staffMode = StaffMode.get(player);
        Block block = event.getClickedBlock();

        if (staffMode.isEnabled() && block != null) {
            if (block.getState() instanceof InventoryHolder && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                event.setCancelled(true);
                InventoryHolder holder = (InventoryHolder) block.getState();
                new ExamineBlockMenu(holder).openMenu(player);
                player.sendMessage(CC.GOLD + "Opening " + CC.WHITE +
                        holder.getInventory().getType().getDefaultTitle() + CC.GOLD + " silently.");
                return;
            }
        }

        if (!staffMode.isEnabled() && !staffMode.isVanished())
            return;


        if (event.getAction() == Action.PHYSICAL) {
            event.setCancelled(true);
            return;
        }

        if (!player.hasPermission("invictus.staffmode.build")) {
            if ((event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)
                    && (player.getItemInHand() != null && DENY_INTERACT.contains(player.getItemInHand().getType()))) {
                event.setCancelled(true);
                player.sendMessage(CC.RED + CC.BOLD + "You cannot do this while in staff mode.");
                return;
            }
        }

        if (staffMode.isVanished() || !player.hasPermission("invictus.staffmode.build")) {
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK
                    && block != null && (block.getState().getData() instanceof Openable
                    || block.getState().getData() instanceof Redstone)) {
                event.setCancelled(true);
                player.sendMessage(CC.RED + CC.BOLD + "You cannot do this while in staff mode.");
            }
        }
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player))
            return;

        Player player = (Player) event.getEntity().getShooter();
        StaffMode staffMode = StaffMode.get(player);

        if (!staffMode.isEnabled() && !staffMode.isVanished())
            return;

        if (staffMode.isVanished() || !player.hasPermission("invictus.staffmode.build")) {
            event.setCancelled(true);
            player.sendMessage(CC.RED + CC.BOLD + "You cannot do this while in staff mode.");
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        if (!testBuild(player, true) && event.getRightClicked() instanceof Vehicle) event.setCancelled(true);
    }

    @EventHandler
    public void onVehicleDestroy(VehicleDestroyEvent event) {
        if (!(event.getAttacker() instanceof Player))
            return;

        Player player = (Player) event.getAttacker();
        if (!testBuild(player, true)) event.setCancelled(true);
    }

    @EventHandler
    public void onVehicleDamage(VehicleDamageEvent event) {
        if (!(event.getAttacker() instanceof Player))
            return;

        Player player = (Player) event.getAttacker();
        if (!testBuild(player, true))
            event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();
        if (!testBuild(player, true)) event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerBucketFill(PlayerBucketFillEvent event) {
        Player player = event.getPlayer();
        if (!testBuild(player, true)) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        StaffMode staffMode = StaffMode.get(event.getPlayer());
        if (staffMode.isVanished() || staffMode.isEnabled()) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        StaffMode staffMode = StaffMode.get(player);

        if (!staffMode.isEnabled() && !staffMode.isVanished())
            return;

        if (!player.hasPermission("invictus.staffmode.build")) {
            event.setCancelled(true);
            return;
        }

        for (ItemStack itemStack : DROP_PROTECTED) {
            if (event.getItemDrop().getItemStack().isSimilar(itemStack)) {
                event.setCancelled(true);
                return;
            }
        }

        event.getItemDrop().remove();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Player player;
        if (event.getDamager() instanceof Player)
            player = (Player) event.getDamager();
        else if (event.getDamager() instanceof Projectile
                && ((Projectile) event.getDamager()).getShooter() instanceof Player)
            player = (Player) ((Projectile) event.getDamager()).getShooter();
        else return;

        StaffMode staffMode = StaffMode.get(player);

        if (staffMode.isEnabled() && (staffMode.isVanished() || !player.hasPermission("invictus.staffmode.build"))) {
            event.setCancelled(true);
            player.sendMessage(CC.RED + CC.BOLD + "You cannot do this while in staff mode.");
        }

        if (!event.isCancelled()) {
            StaffMode.setLastHit(player.getUniqueId());
            StaffMode.setLastHitTime(System.currentTimeMillis());
        }

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player))
            return;

        Player player = (Player) event.getEntity();
        Profile profile = invictus.getProfileService().getProfile(player);
        if (profile == null)
            return;

        StaffMode staffMode = StaffMode.get(player);
        if (staffMode.isEnabled() || staffMode.isVanished()) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPotionEffectAdd(PotionEffectAddEvent event) {
        if (!(event.getEntity() instanceof Player))
            return;

        Player player = (Player) event.getEntity();
        Profile profile = invictus.getProfileService().getProfile(player);

        if (profile == null)
            return;

        StaffMode staffMode = StaffMode.get(player);
        if (staffMode.isEnabled() || staffMode.isVanished())
            event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Profile profile = invictus.getProfileService().getProfile(player);
        if (profile == null)
            return;

        if (player.hasPermission("invictus.command.back"))
            LAST_LOCATION.put(player.getUniqueId(), player.getLocation());

        if (StaffMode.isStaffMode(player))
            event.getDrops().clear();
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (event.getCause().name().contains("PEARL") || event.getCause().name().contains("PORTAL")
                || !event.getPlayer().hasPermission("invictus.command.back"))
            return;

        LAST_LOCATION.put(event.getPlayer().getUniqueId(), event.getFrom());
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player))
            return;

        Player player = (Player) event.getEntity();
        if (StaffMode.isStaffMode(player)) {
            event.setCancelled(true);
            event.setFoodLevel(20);
            player.setSaturation(10F);
        }
    }

    private boolean testBuild(Player player, boolean message) {
        StaffMode staffMode = StaffMode.get(player);

        if (!staffMode.isEnabled() && !staffMode.isVanished())
            return true;

        if (staffMode.isEnabled() && player.hasPermission("invictus.staffmode.build"))
            return true;

        if (message)
            player.sendMessage(CC.RED + CC.BOLD + "You cannot do this while in staff mode.");
        return false;
    }

    public static void removeLastLocation(Player player) {
        LAST_LOCATION.remove(player.getUniqueId());
    }

    public static Location getLastLocation(Player player) {
        return LAST_LOCATION.get(player.getUniqueId());
    }
}
