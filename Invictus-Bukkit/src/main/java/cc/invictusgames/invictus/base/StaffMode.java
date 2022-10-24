package cc.invictusgames.invictus.base;

import cc.invictusgames.ilib.builder.ItemBuilder;
import cc.invictusgames.ilib.combatlogger.CombatLogger;
import cc.invictusgames.ilib.menu.hotbaritem.HotbarItem;
import cc.invictusgames.ilib.utils.AngleUtils;
import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.ilib.utils.ChatMessage;
import cc.invictusgames.ilib.utils.Debugger;
import cc.invictusgames.ilib.visibility.VisibilityService;
import cc.invictusgames.invictus.IllegalSystemTypeException;
import cc.invictusgames.invictus.InvictusBukkit;
import cc.invictusgames.invictus.SystemType;
import cc.invictusgames.invictus.base.event.staffmode.PostStaffModeToggleEvent;
import cc.invictusgames.invictus.base.event.staffmode.StaffModeToggleEvent;
import cc.invictusgames.invictus.base.event.vanish.PostVanishToggleEvent;
import cc.invictusgames.invictus.base.event.vanish.VanishToggleEvent;
import cc.invictusgames.invictus.base.menu.staffmode.OnlineStaffMenu;
import cc.invictusgames.invictus.permission.ProfilePermissible;
import cc.invictusgames.invictus.profile.Profile;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 20.04.2020 / 15:19
 * Invictus / cc.invictusgames.invictus.spigot.profile
 */

@Data
@RequiredArgsConstructor
public class StaffMode {

    public static final ItemStack COMPASS = new ItemBuilder(Material.COMPASS)
            .setDisplayName(CC.PINK + "Teleport Compass")
            .build();

    public static final ItemStack INSPECT = new ItemBuilder(Material.BOOK)
            .setDisplayName(CC.PINK + "Examine Inventory")
            .build();

    public static final ItemStack CARPET = new ItemBuilder(Material.CARPET, DyeColor.ORANGE.getWoolData())
            .setDisplayName(" ")
            .build();

    public static final ItemStack AXE = new ItemBuilder(Material.WOOD_AXE)
            .setDisplayName(CC.PINK + "World Edit")
            .build();

    public static final ItemStack RANDOM_TP = new ItemBuilder(Material.NETHER_BRICK_ITEM)
            .setDisplayName(CC.PINK + "Random Teleport")
            .build();

    public static final ItemStack LAST_FIGHT = new ItemBuilder(Material.EMERALD)
            .setDisplayName(CC.PINK + "Last Fight Teleport")
            .build();

    public static final ItemStack VANISH_ON = new ItemBuilder(Material.INK_SACK, DyeColor.LIME.getDyeData())
            .setDisplayName(CC.PINK + "Become Invisible")
            .build();

    public static final ItemStack VANISH_OFF = new ItemBuilder(Material.INK_SACK, DyeColor.GRAY.getDyeData())
            .setDisplayName(CC.PINK + "Become Visible")
            .build();

    @Getter
    private static final List<UUID> openInventories = new ArrayList<>();

    private static final HashSet<Byte> TRANSPARENT = new HashSet<>();

    @Getter
    @Setter
    private static PlayTimeGetter playTimeGetter = PlayTimeGetter.DEFAULT;

    private static final Map<UUID, StaffMode> STAFF_MODE_MAP = new HashMap<>();

    public static StaffMode get(Player player) {
        STAFF_MODE_MAP.putIfAbsent(player.getUniqueId(), new StaffMode(InvictusBukkit.getBukkitInstance(),
                InvictusBukkit.getBukkitInstance().getProfileService().getProfile(player)));
        return STAFF_MODE_MAP.get(player.getUniqueId());
    }

    public static boolean isVanished(Player player) {
        return get(player).isVanished();
    }

    public static boolean isStaffMode(Player player) {
        return get(player).isEnabled();
    }

    @Getter
    @Setter
    private static UUID lastHit;
    @Getter
    @Setter
    private static Long lastHitTime = System.currentTimeMillis();

    private final InvictusBukkit invictus;

    private final Profile profile;

    private ItemStack[] inventory = new ItemStack[36];
    private ItemStack[] armor = new ItemStack[4];
    private GameMode gameMode = GameMode.SURVIVAL;

    private boolean enabled = false;
    private boolean vanished = false;

    private HotbarItem inspectItem;
    private HotbarItem teleportItem;
    private HotbarItem vanishItem;
    private HotbarItem onlineStaffItem;
    private HotbarItem lastFightTeleportItem;

    private Entity despawningEntity;

    public boolean toggleEnabled(boolean silent) {
        IllegalSystemTypeException.checkOrThrow(SystemType.BUKKIT);

        Player player = profile.player();
        StaffModeToggleEvent event = new StaffModeToggleEvent(player, !enabled);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }

        this.enabled = !enabled;

        if (!this.enabled) {
            if (this.vanished) {
                this.toggleVanish(true);
            }

            HotbarItem.unregisterItem(player, this.inspectItem.getClass());
            HotbarItem.unregisterItem(player, this.teleportItem.getClass());
            HotbarItem.unregisterItem(player, this.lastFightTeleportItem.getClass());
            HotbarItem.unregisterItem(player, this.vanishItem.getClass());
            HotbarItem.unregisterItem(player, this.onlineStaffItem.getClass());
            HotbarItem.unregisterItem(player, CompassItem.class);

            player.getInventory().setContents(this.inventory);
            player.getInventory().setArmorContents(this.armor);
            player.setGameMode(this.gameMode);

            ProfilePermissible permissible = invictus.getPermissionService().getProfilePermissible(player);
            if (permissible != null) {
                permissible.getTempPermissions().remove("worldedit.navigation.thru.tool");
                permissible.getTempPermissions().remove("worldedit.navigation.jump.tool");
            }

        } else {

            this.inventory = player.getInventory().getContents();
            this.armor = player.getInventory().getArmorContents();
            this.gameMode = player.getGameMode();

            player.getInventory().clear();
            player.getInventory().setArmorContents(null);
            player.setGameMode(GameMode.CREATIVE);

            if (!this.vanished) {
                this.toggleVanish(true);
            }

            this.inspectItem = new InspectItem(player);
            this.teleportItem = new TeleportItem(player);
            this.vanishItem = new VanishItem(player);
            this.onlineStaffItem = new OnlineStaffItem(player);
            this.lastFightTeleportItem = new LastFightTeleportItem(player);

            int slot = 0;
            if (Bukkit.getPluginManager().isPluginEnabled("WorldEdit"))
                player.getInventory().setItem(slot++, StaffMode.COMPASS);
            //else player.getInventory().setItem(slot++, new CompassItem(player).getItem());

            player.getInventory().setItem(slot++, this.inspectItem.getItem());

            if (player.hasPermission("worldedit.wand")
                    && Bukkit.getPluginManager().isPluginEnabled("WorldEdit"))
                player.getInventory().setItem(slot++, StaffMode.AXE);

            player.getInventory().setItem(slot, StaffMode.CARPET);
            player.getInventory().setItem(slot = 6, this.teleportItem.getItem());
            player.getInventory().setItem(++slot, this.onlineStaffItem.getItem());
            player.getInventory().setItem(++slot, this.vanishItem.getItem());

            ProfilePermissible permissible = invictus.getPermissionService().getProfilePermissible(player);
            if (permissible != null && Bukkit.getPluginManager().isPluginEnabled("WorldEdit")) {
                permissible.getTempPermissions().put("worldedit.navigation.thru.tool", true);
                permissible.getTempPermissions().put("worldedit.navigation.jump.tool", true);
            }
        }

        if (!silent)
            player.sendMessage(CC.GOLD + "Staff Mode: " + CC.colorBoolean(enabled, true));

        Bukkit.getPluginManager().callEvent(new PostStaffModeToggleEvent(player, enabled));
        return true;
    }

    public boolean toggleVanish(boolean silent) {
        IllegalSystemTypeException.checkOrThrow(SystemType.BUKKIT);

        VanishToggleEvent event = new VanishToggleEvent(profile.player(), !vanished);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }

        this.vanished = !vanished;

        if (!vanished) {
            profile.player().spigot().setCollidesWithEntities(true);
            profile.player().spigot().setAffectsSpawning(true);
            VisibilityService.update(profile.player());
        } else {
            profile.player().spigot().setCollidesWithEntities(false);
            profile.player().spigot().setAffectsSpawning(false);
            VisibilityService.update(profile.player());
        }

        if (this.enabled) {
            if (this.vanishItem == null) {
                this.vanishItem = new VanishItem(profile.player());
            }
            profile.player().getInventory().setItem(8, this.vanishItem.getItem());
        }

        if (!silent)
            profile.player().sendMessage(CC.GOLD + "Vanish: " + CC.colorBoolean(vanished, true));
        Bukkit.getPluginManager().callEvent(new PostVanishToggleEvent(profile.player(), vanished));
        return true;
    }

    public class CompassItem extends HotbarItem {


        private final Player player;

        public CompassItem(Player player) {
            super(player);
            this.player = player;
        }

        @Override
        public ItemStack getItem() {
            return COMPASS;
        }

        @Override
        public void click(Action action, Block block) {
            if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
                Block targetBlock = player.getTargetBlock(TRANSPARENT, 50);
                if (targetBlock == null
                        || targetBlock.getType().isTransparent()
                        || targetBlock.getY() >= targetBlock.getWorld().getMaxHeight()) {
                    player.sendMessage(CC.RED + "No block in sight. (Or too far)");
                    return;
                }

                boolean foundBlock = false;
                while (!foundBlock) {
                    targetBlock = targetBlock.getRelative(BlockFace.UP);
                    if (targetBlock.getY() >= targetBlock.getWorld().getMaxHeight())
                        break;

                    if (targetBlock.getType().isTransparent()
                            && targetBlock.getRelative(BlockFace.UP).getType().isTransparent()) {
                        foundBlock = true;
                    }
                }

                if (!foundBlock) {
                    player.sendMessage(CC.RED + "No block in sight. (Or too far)");
                    return;
                }

                Location clone = targetBlock.getLocation().clone();
                clone.setYaw(player.getLocation().getYaw());
                clone.setPitch(player.getLocation().getPitch());
                clone.add(0.5, 0, 0.5);
                player.teleport(clone);
                return;
            }

            if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
                BlockFace blockFace = AngleUtils.pitchToFace(player.getLocation().getPitch());
                if (blockFace == null)
                    blockFace = AngleUtils.yawToFace(player.getLocation().getYaw());

                if (blockFace == null) {
                    player.sendMessage(CC.RED + "Something went wrong getting the direction you're looking into." +
                            " < 0 or > 360");
                    return;
                }

                BlockFace faceToCheck = blockFace == BlockFace.DOWN ? BlockFace.DOWN : BlockFace.UP;

                Block targetBlock = player.getTargetBlock(TRANSPARENT, 50);
                if (targetBlock == null
                        || targetBlock.getType().isTransparent()
                        || targetBlock.getY() >= targetBlock.getWorld().getMaxHeight()) {
                    player.sendMessage(CC.RED + "Nothing to pass through.");
                    return;
                }

                targetBlock = player.getLocation().getBlock();
                int distance = 0;

                boolean passedThroughBlock = false;
                boolean foundBlock = false;
                while (!foundBlock) {
                    if (distance++ >= 50) {
                        Debugger.debug(player, "distance: " + distance);
                        break;
                    }

                    targetBlock = targetBlock.getRelative(blockFace);

                    if (!targetBlock.getType().isTransparent())
                        passedThroughBlock = true;


                    if (passedThroughBlock
                            && targetBlock.getType().isTransparent()
                            && targetBlock.getRelative(faceToCheck).getType().isTransparent()) {
                        foundBlock = true;
                    }
                }

                if (!foundBlock) {
                    player.sendMessage(CC.RED + "Nothing to pass through.");
                    return;
                }

                Location clone = targetBlock.getLocation().clone();
                clone.setYaw(player.getLocation().getYaw());
                clone.setPitch(player.getLocation().getPitch());
                clone.add(0.5, 0, 0.5);
                player.teleport(clone);
            }
        }

        @Override
        public void clickEntity(Entity entity) {

        }
    }

    public class InspectItem extends HotbarItem {

        public InspectItem(Player player) {
            super(player);
        }

        @Override
        public ItemStack getItem() {
            return INSPECT;
        }

        @Override
        public void click(Action action, Block block) {
        }

        @Override
        public void clickEntity(Entity entity) {
            if (entity instanceof Player) {
                Bukkit.dispatchCommand(profile.player(), "invsee " + ((Player) entity).getName());
                return;
            }

            if (entity.hasMetadata(CombatLogger.METADATA)) {
                for (MetadataValue metadata : entity.getMetadata(CombatLogger.METADATA)) {
                    new ChatMessage(CC.GOLD + "Click to here despawn this "
                            + CC.WHITE + entity.getType().getName() + CC.GOLD + ".")
                            .runCommand("/despawncombatlogger " + metadata.asString())
                            .hoverText(CC.YELLOW + "Click here to despawn.")
                            .send(profile.player());
                }
                return;
            }

            if (profile.player().hasPermission("invictus.command.despawnentity")) {
                despawningEntity = entity;
                ChatMessage message = new ChatMessage(CC.GOLD + "Click here to despawn this "
                        + CC.WHITE + entity.getType().getName() + CC.GOLD + ".")
                        .runCommand("/despawnentity")
                        .hoverText(CC.YELLOW + "Click here to despawn.");
                message.send(profile.player());
            }
        }
    }

    public class TeleportItem extends HotbarItem {

        private final Player player;

        public TeleportItem(Player player) {
            super(player);
            this.player = player;
        }

        @Override
        public ItemStack getItem() {
            return RANDOM_TP;
        }

        @Override
        public void click(Action action, Block block) {
            if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
                Bukkit.dispatchCommand(profile.player(), "rtp");
                return;
            }

            player.getInventory().setItem(6, lastFightTeleportItem.getItem());
        }

        @Override
        public void clickEntity(Entity entity) {
        }
    }

    public class LastFightTeleportItem extends HotbarItem {

        private final Player player;

        public LastFightTeleportItem(Player player) {
            super(player);
            this.player = player;
        }

        @Override
        public ItemStack getItem() {
            return LAST_FIGHT;
        }

        @Override
        public void click(Action action, Block block) {
            if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
                if (lastHit == null)
                    return;

                if (System.currentTimeMillis() - lastHitTime > TimeUnit.SECONDS.toMillis(15)) {
                    player.sendMessage(ChatColor.RED + "There has been no fights within the last 15 seconds.");
                    return;
                }

                Player target = Bukkit.getPlayer(lastHit);
                if (target == null) {
                    player.sendMessage(ChatColor.RED + "This player is no longer online.");
                    return;
                }

                Bukkit.dispatchCommand(player, "tp " + target.getName());
                return;
            }

            player.getInventory().setItem(6, teleportItem.getItem());
        }

        @Override
        public void clickEntity(Entity entity) {
        }
    }

    public class VanishItem extends HotbarItem {

        public VanishItem(Player player) {
            super(player);
        }

        @Override
        public ItemStack getItem() {

            return vanished ? VANISH_OFF : VANISH_ON;
        }

        @Override
        public void click(Action action, Block block) {
            if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK)
                toggleVanish(true);
        }

        @Override
        public void clickEntity(Entity entity) {
        }
    }

    public class OnlineStaffItem extends HotbarItem {

        private final Player player;

        public OnlineStaffItem(Player player) {
            super(player);
            this.player = player;
        }


        @Override
        public ItemStack getItem() {
            return new ItemBuilder(Material.SKULL_ITEM, 3)
                    .setDisplayName(CC.PINK + "Online Staff")
                    .setSkullOwner(player.getName())
                    .build();
        }

        @Override
        public void click(Action action, Block block) {
            if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK)
                new OnlineStaffMenu(invictus).openMenu(player);
        }

        @Override
        public void clickEntity(Entity entity) {
        }
    }

    static {
        for (Material material : Material.values()) {
            if (material.isTransparent())
                TRANSPARENT.add((byte) material.getId());
        }
    }
}
