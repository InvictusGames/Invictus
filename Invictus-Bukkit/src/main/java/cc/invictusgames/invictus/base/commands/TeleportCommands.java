package cc.invictusgames.invictus.base.commands;

import cc.invictusgames.ilib.combatlogger.CombatLogger;
import cc.invictusgames.ilib.command.annotation.Command;
import cc.invictusgames.ilib.command.annotation.Flag;
import cc.invictusgames.ilib.command.annotation.Param;
import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.invictus.InvictusBukkit;
import cc.invictusgames.invictus.base.RandomTeleportAdapter;
import cc.invictusgames.invictus.base.StaffMode;
import cc.invictusgames.invictus.listener.StaffModeListener;
import cc.invictusgames.invictus.profile.Profile;
import cc.invictusgames.invictus.profile.UnloadedProfile;
import cc.invictusgames.invictus.utils.Tasks;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 08.06.2020 / 18:24
 * Invictus / cc.invictusgames.invictus.spigot.base.commands
 */

@RequiredArgsConstructor
public class TeleportCommands {

    @Getter
    @Setter
    private static final RandomTeleportAdapter randomTeleportAdapter = RandomTeleportAdapter.DEFAULT;

    private final InvictusBukkit invictus;

    @Command(names = {"teleport", "tp", "tpto", "tele"},
             permission = "invictus.command.teleport",
             description = "Teleport a player to a player")
    public boolean teleport(CommandSender sender,
                            @Param(name = "player", defaultValue = "@self") UnloadedProfile unloadedProfile,
                            @Param(name = "target") UnloadedProfile unloadedTarget) {
        unloadedProfile.load(profile -> {
            unloadedTarget.loadBoth(pair -> {
                Profile target = pair.getLeft();
                if (target == null || profile == null) {
                    return;
                }

                if ((!sender.equals(profile.player())) && (!sender.hasPermission("invictus.command.teleport.other"))) {
                    sender.sendMessage(CC.RED + "You are not allowed to teleport other players.");
                    return;
                }

                if (profile.player() == null) {
                    sender.sendMessage(CC.format("&cPlayer &e%s &cis not online.", profile.getName()));
                    return;
                }

                Player targetPlayer = pair.getRight();

                Tasks.run(() -> profile.player().teleport(targetPlayer));

                profile.player().sendMessage(CC.GOLD + "Teleporting you to " +
                        (targetPlayer.isOnline() ? "" : "offline player ") +
                        target.getDisplayName(profile.player()) + CC.GOLD + ".");
                if (!sender.equals(profile.player())) {
                    sender.sendMessage(CC.GOLD + "Teleporting " + profile.getDisplayName(sender) + CC.GOLD +
                            " to " + (targetPlayer.isOnline() ? "" : "offline player ") +
                            target.getDisplayName(sender) + CC.GOLD + ".");
                }
            }, true);
        }, true);
        return true;
    }

    @Command(names = {"teleporthere", "tphere", "s", "bring", "telehere"},
             permission = "invictus.command.teleporthere",
             description = "Teleport a player to you",
             playerOnly = true)
    public boolean teleporthere(Player player,
                                @Param(name = "player") UnloadedProfile unloadedTarget,
                                @Flag(names = {"o", "-offline"}, description = "Teleport an offline player")
                                        boolean offline) {
        unloadedTarget.loadBoth(pair -> {
            Profile target = pair.getLeft();
            if (target == null) {
                return;
            }
            Profile profile = invictus.getProfileService().getProfile(player);

            Player targetPlayer = pair.getRight();
            Tasks.run(() -> {
                if (targetPlayer.isOnline()) {
                    targetPlayer.teleport(player);
                } else {
                    if (!offline) {
                        player.sendMessage(CC.format("&cPlayer &e%s &cis not online.", target.getName()));
                        return;
                    }

                    Location location = player.getLocation();
                    ((CraftPlayer) targetPlayer).getHandle().spawnIn(((CraftWorld) location.getWorld()).getHandle());
                    ((CraftPlayer) targetPlayer).getHandle().setLocation(
                            location.getBlockX(),
                            location.getY(),
                            location.getZ(),
                            location.getYaw(),
                            location.getPitch()
                    );
                    targetPlayer.saveData();

                    if (CombatLogger.getLoggerMap().containsKey(target.getUuid())) {
                        CombatLogger combatLogger = CombatLogger.getLoggerMap().get(target.getUuid());
                        combatLogger.getSpawnedEntity().teleport(player);
                    }
                }
            });

            player.sendMessage(CC.GOLD + "Teleporting " +
                    (targetPlayer.isOnline() ? "" : "offline player ") +
                    target.getDisplayName(player) + CC.GOLD + " to you.");
            if (target.player() != null) {
                target.player().sendMessage(CC.GOLD + "Teleporting you to " +
                        profile.getDisplayName(target.player()) + CC.GOLD + ".");
            }
        }, true);
        return true;
    }

    @Command(names = {"teleportall", "tpall", "teleall"},
             permission = "invictus.command.teleportall",
             description = "Teleport all online players to you",
             playerOnly = true)
    public boolean teleportall(Player player) {
        Profile profile = invictus.getProfileService().getProfile(player);
        for (Player current : Bukkit.getOnlinePlayers()) {
            current.teleport(player);
            current.sendMessage(CC.GOLD + "Teleporting you to " + profile.getDisplayName(current) + CC.GOLD + ".");
        }
        player.sendMessage(CC.GOLD + "Teleporting all online players to you.");
        return true;
    }

    @Command(names = {"teleportposition", "tppos", "telepos"},
             permission = "invictus.command.teleportposition",
             description = "Teleport a player to a location")
    public boolean teleportposition(CommandSender sender,
                                    @Param(name = "player", defaultValue = "@self") UnloadedProfile unloadedProfile,
                                    @Param(name = "x") double x,
                                    @Param(name = "y") double y,
                                    @Param(name = "z") double z) {
        unloadedProfile.load(target -> {
            if (target == null) {
                return;
            }

            if (target.player() == null) {
                sender.sendMessage(CC.format("&cPlayer &e%s &cis not online.", target.getName()));
                return;
            }

            if ((!sender.equals(target.player())) && (!sender.hasPermission("invictus.command.teleportposition.other"))) {
                sender.sendMessage(CC.RED + "You are not allowed to teleport other players.");
                return;
            }

            Location location = new Location(
                    target.player().getWorld(),
                    x % 1.0 == 0.0 ? (x >= 0.0 ? x + 0.5 : x - 0.5) : x,
                    y,
                    z % 1.0 == 0.0 ? (z >= 0.0 ? z + 0.5 : z - 0.5) : z
            );
            String locationString = CC.format("&e[&f%s&e, &f%s&e, &f%s&e]", location.getX(), location.getY(),
                    location.getZ());
            Tasks.run(() -> target.player().teleport(location));
            target.player().sendMessage(CC.GOLD + "Teleporting you to " + locationString + CC.GOLD + ".");
            if (!sender.equals(target.player())) {
                sender.sendMessage(CC.GOLD + "Teleporting " + target.getDisplayName(sender) + CC.GOLD + " to " + locationString + CC.GOLD + ".");
            }
        }, true);
        return true;
    }

    @Command(names = {"randomteleport", "ranomtp", "rtp"},
             permission = "invictus.command.randomteleport",
             description = "Teleport to a random player",
             playerOnly = true)
    public boolean randomTeleport(Player sender) {
        List<Player> players = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (StaffMode.isStaffMode(player) || StaffMode.isVanished(player))
                continue;

            if (sender == player)
                continue;

            if (player.getLocation().getBlockX() == 0 && player.getLocation().getBlockZ() == 0)
                continue;

            if (randomTeleportAdapter.isValidTarget(player, sender))
                players.add(player);
        }

        if (players.isEmpty()) {
            sender.sendMessage(CC.RED + "There are currently no players available to teleport.");
            return false;
        }

        Player target = players.get(ThreadLocalRandom.current().nextInt(players.size()));
        Bukkit.dispatchCommand(sender, "tp " + target.getName());
        players.clear();
        return true;
    }

    @Command(names = {"back"},
             permission = "invictus.command.back",
             description = "Teleport to your last location",
             playerOnly = true)
    public boolean back(Player sender) {
        Location location = StaffModeListener.getLastLocation(sender);
        if (location == null) {
            sender.sendMessage(CC.RED + "No previous location recorded.");
            return false;
        }

        sender.teleport(location);
        sender.sendMessage(CC.GOLD + "Teleporting you to your last recorded location.");
        return true;
    }

}
