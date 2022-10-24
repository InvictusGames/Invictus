package cc.invictusgames.invictus.grant.commands;

import cc.invictusgames.ilib.command.annotation.Command;
import cc.invictusgames.ilib.command.annotation.Param;
import cc.invictusgames.ilib.command.parameter.defaults.Duration;
import cc.invictusgames.ilib.redis.packet.Packet;
import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.ilib.utils.TimeUtils;
import cc.invictusgames.ilib.utils.json.JsonBuilder;
import cc.invictusgames.invictus.InvictusBukkit;
import cc.invictusgames.invictus.connection.RequestHandler;
import cc.invictusgames.invictus.connection.RequestResponse;
import cc.invictusgames.invictus.grant.Grant;
import cc.invictusgames.invictus.grant.GrantClearBackLogEntry;
import cc.invictusgames.invictus.grant.menu.GrantRankMenu;
import cc.invictusgames.invictus.grant.menu.GrantsMenu;
import cc.invictusgames.invictus.grant.packets.GrantAddPacket;
import cc.invictusgames.invictus.grant.procedure.GrantProcedure;
import cc.invictusgames.invictus.profile.Profile;
import cc.invictusgames.invictus.profile.packets.ProfileUpdatePacket;
import cc.invictusgames.invictus.rank.Rank;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 19.02.2020 / 17:32
 * Invictus / cc.invictusgames.invictus.spigot.grant.commands
 */

@RequiredArgsConstructor
public class GrantCommands {

    private final InvictusBukkit invictus;

    @Command(names = {"grant"},
             permission = "invictus.command.grant",
             description = "Grant a rank to a player",
             playerOnly = true,
             async = true)
    public boolean grant(Player sender, @Param(name = "player") Profile target) {
        Profile profile = invictus.getProfileService().getProfile(sender);
        GrantProcedure grantProcedure = new GrantProcedure(profile, target);
        new GrantRankMenu(invictus, grantProcedure).openMenu(sender);
        return true;
    }

    @Command(names = {"grants"},
             permission = "invictus.command.grants",
             description = "Check a players grants",
             playerOnly = true,
             async = true)
    public boolean grants(Player sender, @Param(name = "player") Profile target) {
        sender.sendMessage(CC.YELLOW + "Loading grants of " + target.getName() + "...");
        RequestResponse response = RequestHandler.get("profile/%s/grants", target.getUuid().toString());
        if (!response.wasSuccessful()) {
            sender.sendMessage(CC.format("&cCould not load grants: %s (%d)",
                    response.getErrorMessage(), response.getCode()));
            return true;
        }

        List<Grant> grants = new ArrayList<>();
        response.asArray().forEach(element -> grants.add(new Grant(invictus, element.getAsJsonObject())));
        grants.removeIf(grant -> grant.getRank() == null);
        new GrantsMenu(invictus, target, grants).openMenu(sender);
        return true;
    }

    @Command(names = {"consolegrant", "cgrant"},
             permission = "console",
             description = "Grant a rank to a player",
             async = true)
    public boolean consolegrant(CommandSender sender,
                                @Param(name = "player") Profile target,
                                @Param(name = "rank") Rank rank,
                                @Param(name = "duration") Duration duration,
                                @Param(name = "scopes") String scope,
                                @Param(name = "reason", wildcard = true) String reason) {
        List<String> scopes = new ArrayList<>();
        if (!scope.contains(",")) {
            if (scope.equalsIgnoreCase("global")) {
                scopes.add("GLOBAL");
            } else {
                scopes.add(scope.toLowerCase());
            }
        } else {
            for (String s : scope.split(",")) {
                if (s.equalsIgnoreCase("global")) {
                    scopes.add("GLOBAL");
                } else {
                    scopes.add(s.toLowerCase());
                }
            }
        }

        Grant grant = new Grant(
                invictus,
                target.getUuid(),
                rank,
                sender instanceof Player ? ((Player) sender).getUniqueId().toString() : "Console",
                System.currentTimeMillis(),
                reason,
                duration.getDuration(),
                scopes
        );

        //Packet packet = new GrantAddPacket(target.getUuid(), rank.getUuid(), duration.getDuration());
        RequestResponse response = invictus.getBukkitProfileService().addGrant(target, grant);
        if (response.couldNotConnect()) {
            sender.sendMessage(CC.format("&cCould not connect to API to create grant. " +
                            "Adding grant to the queue. Error: %s (%d)",
                    response.getErrorMessage(), response.getCode()));
        } else if (!response.wasSuccessful()) {
            sender.sendMessage(CC.format("&cCould not create grant: %s (%d)",
                    response.getErrorMessage(), response.getCode()));
            return false;
        }

        if (grant.getDuration() == -1)
            sender.sendMessage(CC.format(
                    "&aYou've &epermanently &agranted %s&a the %s&a rank.",
                    target.getRealDisplayName(),
                    rank.getDisplayName()
            ));
        else
            sender.sendMessage(CC.format(
                    "&aYou've granted %s&a the %s&a rank for &e%s&a.",
                    target.getRealDisplayName(),
                    rank.getDisplayName(),
                    TimeUtils.formatDetailed(grant.getDuration())
            ));
        return true;
    }

    @Command(names = {"cleargrants"},
             permission = "console",
             description = "Clear all active grants of a player",
             async = true)
    public boolean clearGrants(CommandSender sender,
                               @Param(name = "player") Profile target,
                               @Param(name = "reason", wildcard = true) String reason) {
        JsonBuilder body = new JsonBuilder();
        body.add("removedAt", System.currentTimeMillis());
        body.add("removedBy", sender instanceof Player ? ((Player) sender).getUniqueId().toString() : "Console");
        body.add("removedReason", reason);
        RequestResponse response = RequestHandler.post("profile/%s/grants/clear",
                body.build(), target.getUuid().toString());

        if (response.couldNotConnect()) {
            sender.sendMessage(CC.format("&cCould not connect to API to clear grants. " +
                            "Adding request to the queue. Error: %s (%d)",
                    response.getErrorMessage(), response.getCode()));
            RequestHandler.addToBackLog(new GrantClearBackLogEntry(
                    target.getUuid(),
                    sender instanceof Player ? ((Player) sender).getUniqueId() : null,
                    response.getRequestBuilder()
            ));
            return true;
        } else if (!response.wasSuccessful()) {
            sender.sendMessage(CC.format("&cCould not clear grants: %s (%d)",
                    response.getErrorMessage(), response.getCode()));
            return false;
        }

        sender.sendMessage(CC.format("&aRemoved &e%d &agrants of %s&a.",
                response.asObject().get("removed").getAsInt(), target.getRealDisplayName()));

        invictus.getRedisService().publish(new ProfileUpdatePacket(target.getUuid()));
        return true;
    }

    public boolean canGrant(CommandSender sender, Rank rank) {
        if (!(sender instanceof Player)) {
            return true;
        }
        Player player = (Player) sender;
        Profile profile = invictus.getProfileService().getProfile(player);

        if (rank.isDefaultRank()) {
            return false;
        }

        if (profile.getRealCurrentGrant().getRank().getWeight() >= invictus.getMainConfig().getOwnerWeight()
                || profile.getUuid().equals(UUID.fromString("a507f314-d97c-43ca-bab6-99304a492827"))) {
            return true;
        }

        return profile.getRealCurrentGrant().getRank().getWeight() > rank.getWeight() && player.hasPermission(
                "invicuts.grant." + rank.getName());
    }
}
