package cc.invictusgames.invictus.base.commands;

import cc.invictusgames.ilib.command.CommandService;
import cc.invictusgames.ilib.command.annotation.Command;
import cc.invictusgames.ilib.command.annotation.Param;
import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.ilib.utils.ChatMessage;
import cc.invictusgames.ilib.utils.Statics;
import cc.invictusgames.ilib.utils.TimeUtils;
import cc.invictusgames.ilib.visibility.VisibilityService;
import cc.invictusgames.invictus.Invictus;
import cc.invictusgames.invictus.InvictusBukkit;
import cc.invictusgames.invictus.base.StaffMode;
import cc.invictusgames.invictus.base.packet.JumpToPacket;
import cc.invictusgames.invictus.listener.StaffModeListener;
import cc.invictusgames.invictus.playersetting.InvictusSettings;
import cc.invictusgames.invictus.profile.Profile;
import cc.invictusgames.invictus.rank.Rank;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 08.06.2020 / 18:59
 * Invictus / cc.invictusgames.invictus.spigot.base.commands
 */

@RequiredArgsConstructor
public class BaseCommands {

    public static final Comparator<Player> PLAYER_COMPARATOR = (player, other) -> {
        Profile profile = Invictus.getInstance().getProfileService().getProfile(player);
        Profile otherProfile = Invictus.getInstance().getProfileService().getProfile(other);

        Rank rank = profile.getCurrentGrant().getRank();
        Rank otherRank = otherProfile.getCurrentGrant().getRank();

        if (rank.getWeight() == otherRank.getWeight())
            return player.getName().compareTo(other.getName());

        return otherRank.getWeight() - rank.getWeight();
    };

    private final InvictusBukkit invictus;

    @Command(names = {"heal"},
             permission = "invictus.command.heal",
             description = "Heal a player")
    public boolean heal(CommandSender sender, @Param(name = "player", defaultValue = "@self") Player target) {
        if ((!sender.equals(target)) && (!sender.hasPermission("invictus.command.heal.other"))) {
            sender.sendMessage(CC.RED + "You are not allowed to heal other players.");
            return false;
        }

        Profile profile = invictus.getProfileService().getProfile(target);

        target.setHealth(target.getMaxHealth());
        target.setFoodLevel(20);
        target.setSaturation(10.0F);
        target.sendMessage(CC.GOLD + "You have been healed.");
        if (!sender.equals(target))
            sender.sendMessage(CC.format("%s &6has been healed.", profile.getDisplayName(sender)));
        return true;
    }

    @Command(names = {"feed"},
             permission = "invictus.command.feed",
             description = "Feed a player")
    public boolean feed(CommandSender sender, @Param(name = "player", defaultValue = "@self") Player target) {
        if ((!sender.equals(target)) && (!sender.hasPermission("invictus.command.feed.other"))) {
            sender.sendMessage(CC.RED + "You are not allowed to feed other players.");
            return false;
        }

        Profile profile = invictus.getProfileService().getProfile(target);

        target.setFoodLevel(20);
        target.setSaturation(10.0F);
        target.sendMessage(CC.GOLD + "You have been fed.");
        if (!sender.equals(target))
            sender.sendMessage(CC.format("%s &6has been fed.", profile.getDisplayName(sender)));
        return true;
    }

    @Command(names = {"fly"},
             permission = "invictus.command.fly",
             description = "Toggle a players flight mode")
    public boolean fly(CommandSender sender, @Param(name = "player", defaultValue = "@self") Player target) {
        if ((!sender.equals(target)) && (!sender.hasPermission("invictus.command.fly.other"))) {
            sender.sendMessage(CC.RED + "You are not allowed to change the flight mode of other players.");
            return false;
        }

        Profile profile = invictus.getProfileService().getProfile(target);

        target.setAllowFlight(!target.getAllowFlight());
        target.setFlying(target.getAllowFlight());
        target.sendMessage(CC.format("&6Your flight mode was set to &f%b&6.", target.getAllowFlight()));
        if (!sender.equals(target))
            sender.sendMessage(CC.format("%s&6's flight mode was set to &f%b&6.",
                    profile.getDisplayName(sender), target.getAllowFlight()));
        return true;
    }

    @Command(names = {"speed"},
             permission = "invictus.command.speed",
             description = "Change a players walk or fly speed")
    public boolean speed(CommandSender sender, @Param(name = "player", defaultValue = "@self") Player target,
                         @Param(name = "speed|reset") String s) {
        if ((!sender.equals(target)) && (!sender.hasPermission("invictus.command.speed.other"))) {
            sender.sendMessage(CC.RED + "You are not allowed to change the speed of other players.");
            return false;
        }

        Profile profile = invictus.getProfileService().getProfile(target);

        Float multiplier;

        if (s.equalsIgnoreCase("reset")) {
            multiplier = target.isFlying() ? 2.0f : 1.0f;
        } else {
            multiplier = CommandService.getParameter(Float.TYPE).parse(sender, s);
            if (multiplier == null)
                return false;
        }

        float speed = (target.isFlying() ? 0.1f : 0.2f) * multiplier;

        try {
            if (target.isFlying())
                target.setFlySpeed(speed);
            else target.setWalkSpeed(speed);
        } catch (IllegalArgumentException e) {
            sender.sendMessage(CC.format(
                    "&c%s speed multiplier must be between %d and %d.",
                    target.isFlying() ? "Fly" : "Walk",
                    target.isFlying() ? -10 : 5,
                    target.isFlying() ? -10 : -5
            ));
            return false;
        }

        target.sendMessage(CC.format("&6Your %s speed was set to &f%s&6.",
                target.isFlying() ? "fly" : "walk", multiplier));
        if (!sender.equals(target))
            sender.sendMessage(CC.format("%s&6's %s speed was set to &f%s&6.",
                    profile.getDisplayName(sender), target.isFlying() ? "fly" : "walk", multiplier));
        return true;
    }

    @Command(names = {"list", "who", "online", "players"})
    public boolean list(CommandSender sender) {
        /*if (true) {
            sender.sendMessage(CC.format("&fThere is currently &2%d &fconnected to &2%s&f.",
                    Bukkit.getOnlinePlayers().size(), invictus.getServerName()));
            return true;
        }*/

        List<String> playerNames = new ArrayList<>();
        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        players.sort(PLAYER_COMPARATOR);

        for (Player player : players) {
            StringBuilder name = new StringBuilder();
            if (StaffMode.isVanished(player))
                name.append(CC.YELLOW)
                        .append("[V] ");

            Profile profile = invictus.getProfileService().getProfile(player);
            if (profile.hasPrimeStatus())
                name.append(InvictusSettings.PRIME_COLOR.get(player).toString())
                        .append(Invictus.PRIME_ICON);

            name.append(profile.getDisplayName(sender));

            if (VisibilityService.getOnlineTreatProvider().apply(player, sender))
                playerNames.add(name.toString());
        }

        List<String> rankNames = new ArrayList<>();
        for (Rank rank : invictus.getRankService().getRanksSorted()) {
            if (rank.getWeight() >= 0)
                rankNames.add(rank.getDisplayName());
        }

        sender.sendMessage(CC.CHAT_BAR);
        sender.sendMessage(StringUtils.join(rankNames, CC.WHITE + ", "));
        sender.sendMessage("");
        sender.sendMessage(CC.GRAY + "(" + CC.WHITE + playerNames.size() + CC.GRAY + "/" + CC.WHITE
                + Bukkit.getMaxPlayers() + CC.GRAY + ") " + StringUtils.join(playerNames, CC.WHITE + ", "));
        sender.sendMessage(CC.CHAT_BAR);

        players.clear();
        playerNames.clear();
        rankNames.clear();
        return true;
    }

    @Command(names = {"kill", "slay"},
             permission = "invictus.command.kill")
    public boolean kill(CommandSender sender, @Param(name = "player", defaultValue = "@self") Player target) {
        if ((!sender.equals(target)) && (!sender.hasPermission("invictus.command.kill.other"))) {
            sender.sendMessage(CC.RED + "You are not allowed to kill other players.");
            return false;
        }

        Profile profile = invictus.getProfileService().getProfile(target);

        target.setHealth(0.0D);
        target.sendMessage(CC.GOLD + "You have been killed.");
        if (!sender.equals(target))
            sender.sendMessage(CC.format("%s &6has been killed.", profile.getDisplayName(sender)));
        return true;
    }

    @Command(names = {"seen"},
             permission = "invictus.command.seen",
             description = "See an players online status",
             async = true)
    public boolean seen(CommandSender sender, @Param(name = "player") Profile target) {
        ChatMessage message;
        if (target.getLastServer() == null)
            message = new ChatMessage(CC.format(
                    "%s &6is currently offline.",
                    target.getDisplayName(sender)
            )).hoverText(CC.format(
                    "&6Last seen at &f%s&6.\n" +
                            "&7(%s ago)",
                    TimeUtils.formatDate(target.getLastSeen(), InvictusSettings.TIME_ZONE.get(sender)),
                    TimeUtils.formatTimeShort(System.currentTimeMillis() - target.getLastSeen())
            ));
        else message = new ChatMessage(CC.format(
                "%s &6is currently on &f%s&6.",
                target.getDisplayName(sender),
                target.getLastServer()
        )).hoverText(CC.format(
                "&6Online for &f%s&6.",
                TimeUtils.formatTimeShort(System.currentTimeMillis() - target.getJoinTime())
        ));

        sender.sendMessage(CC.SMALL_CHAT_BAR);
        message.send(sender);
        sender.sendMessage(CC.GOLD + "Check out their info on the website:");
        new ChatMessage(CC.WHITE + "brave.rip/profile/" + target.getName())
                .hoverText(CC.GREEN + "Click to view their profile.")
                .send(sender);
        sender.sendMessage(CC.SMALL_CHAT_BAR);
        return true;
    }

    @Command(names = {"jumpto", "jtp"},
             permission = "invictus.command.jumpto",
             description = "Jump to a players server",
             playerOnly = true,
             async = true)
    public boolean jumpTo(Player sender, @Param(name = "player") Profile target) {
        if (target.getLastServer() == null) {
            sender.sendMessage(CC.format("&cPlayer &e%s &cis not online.", target.getName()));
            return true;
        }

        if (target.player() != null) {
            Bukkit.dispatchCommand(sender, String.format(StaffModeListener.JUMP_TO_TELEPORT_COMMAND, target.getName()));
            return true;
        }

        sender.sendMessage(CC.format("&6Jumping to %s &6on &f%s&6...",
                target.getDisplayName(sender), target.getLastServer()));
        invictus.getRedisService().publish(new JumpToPacket(sender.getUniqueId(), target.getUuid()));
        return true;
    }

    @Command(names = {"raw"},
             permission = "invictus.command.raw",
             description = "Send a raw message to a player (Supports color codes)")
    public boolean raw(CommandSender sender,
                       @Param(name = "player") Player target,
                       @Param(name = "message", wildcard = true) String message) {
        target.sendMessage(CC.translate(message));
        return true;
    }

    @Command(names = {"bcraw"},
             permission = "invictus.command.bcraw",
             description = "Broadcast a raw message to the server (Supports color codes)")
    public boolean bcraw(CommandSender sender, @Param(name = "message", wildcard = true) String message) {
        Bukkit.broadcastMessage(CC.translate(message));
        return true;
    }

    @Command(names = {"setspawner"},
             permission = "invictus.command.setspawner",
             description = "Change a spawner's type",
             playerOnly = true)
    public boolean setSpawner(Player sender, @Param(name = "mob") EntityType entity) {
        if (!entity.isAlive() || !entity.isSpawnable()) {
            sender.sendMessage(CC.RED + "Can only spawn living entities.");
            return false;
        }

        Block block = sender.getTargetBlock((Set<Material>) null, 5);
        if (block == null || !(block.getState() instanceof CreatureSpawner)) {
            sender.sendMessage(CC.RED + "You are not looking at a spawner.");
            return false;
        }

        CreatureSpawner spawner = (CreatureSpawner) block.getState();
        spawner.setSpawnedType(entity);
        spawner.update(true, true);
        sender.sendMessage(CC.format("&6This spawner now spawns &f%s&6.", entity.getName()));
        return true;
    }

    @Command(names = "playsound",
             permission = "invictus.command.playsound",
             description = "Play a sound to a player")
    public boolean playSound(
            CommandSender sender,
            @Param(name = "player", defaultValue = "@self") Player target,
            @Param(name = "sound") Sound sound,
            @Param(name = "volume", defaultValue = "1.0") float volume,
            @Param(name = "pitch", defaultValue = "1.0") float pitch) {
        target.playSound(target.getLocation(), sound, volume, pitch);
        Profile profile = invictus.getProfileService().getProfile(target);

        sender.sendMessage(CC.format(
                "&6Playing &f%s&6" + (sender == target ? "." : " to %s&6."),
                WordUtils.capitalizeFully(sound.name().replace("_", " ")),
                profile.getDisplayName(sender)
        ));
        return true;
    }

    @Command(names = {"craft", "workbench", "wb"},
             permission = "invictus.command.craft",
             description = "Open a mobile crafting table",
             playerOnly = true)
    public boolean craft(Player sender) {
        sender.openWorkbench(sender.getLocation(), true);
        return true;
    }

    @Command(names = {"ping", "ms"},
             description = "Check the ping of a player")
    public boolean ping(CommandSender sender, @Param(name = "target", defaultValue = "@self") Player target) {
        Profile profile = invictus.getProfileService().getProfile(target);
        int ping = ((CraftPlayer) target).getHandle().ping;

        if (sender == target)
            sender.sendMessage(CC.format("&6Your ping: &f%d", ping));
        else sender.sendMessage(CC.format("%s&6's ping: &f%d", profile.getDisplayName(sender), ping));
        return true;
    }

    @Command(names = {"settimezone"},
             description = "Set your time zone",
             playerOnly = true,
             async = true)
    public boolean setTimeZone(Player sender, @Param(name = "timeZone") String input) {
        TimeZone timeZone = TimeZone.getTimeZone(input);
        InvictusSettings.TIME_ZONE.set(sender, timeZone);
        sender.sendMessage(CC.format(
                "&eYou have set your time zone to &c%s (%s)&e. The current time there is &c%s&e.",
                timeZone.getDisplayName(),
                timeZone.toZoneId().getId(),
                TimeUtils.formatDate(System.currentTimeMillis(), timeZone)
        ));
        return true;
    }

    @Command(names = {"currenttime"}, description = "Get the current time")
    public boolean currentTime(CommandSender sender) {
        sender.sendMessage(CC.format(
                "&9Current server time (%s %s): &e%s",
                Statics.TIME_ZONE.getDisplayName(),
                Statics.TIME_ZONE.toZoneId().getId(),
                TimeUtils.formatDate(System.currentTimeMillis())
        ));

        sender.sendMessage(CC.format(
                "&9Your selected time (%s %s): &e%s",
                InvictusSettings.TIME_ZONE.get(sender).getDisplayName(),
                InvictusSettings.TIME_ZONE.get(sender).toZoneId().getId(),
                TimeUtils.formatDate(System.currentTimeMillis(), InvictusSettings.TIME_ZONE.get(sender))
        ));
        return true;
    }

}
