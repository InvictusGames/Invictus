package cc.invictusgames.invictus.profile.commands;

import cc.invictusgames.ilib.command.annotation.Command;
import cc.invictusgames.ilib.command.annotation.Param;
import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.ilib.utils.TimeUtils;
import cc.invictusgames.ilib.uuid.UUIDCache;
import cc.invictusgames.invictus.InvictusBukkit;
import cc.invictusgames.invictus.connection.RequestHandler;
import cc.invictusgames.invictus.connection.RequestResponse;
import cc.invictusgames.invictus.playersetting.InvictusSettings;
import cc.invictusgames.invictus.profile.Profile;
import cc.invictusgames.invictus.profile.packets.ProfilePermissionUpdatePacket;
import cc.invictusgames.invictus.profile.packets.ProfileUpdatePacket;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 01.04.2020 / 21:47
 * Invictus / cc.invictusgames.invictus.spigot.profile.command
 */

@RequiredArgsConstructor
public class ProfileCommands {

    private final InvictusBukkit invictus;

    @Command(names = {"profile info"},
             permission = "profile.command.argument.info",
             description = "View information about a player",
             async = true)
    public boolean profileInfo(CommandSender sender, @Param(name = "player") Profile target) {
        sender.sendMessage(CC.SMALL_CHAT_BAR);
        sender.sendMessage(CC.GOLD + CC.BOLD + "Profile Info");
        sender.sendMessage(CC.YELLOW + " Name " + CC.GOLD + CC.RIGHT_ARROW + " " + CC.WHITE + target.getName());
        sender.sendMessage(CC.YELLOW + " Rank " + CC.GOLD + CC.RIGHT_ARROW + " "
                + target.getRealCurrentGrant().getRank().getDisplayName());
        sender.sendMessage(CC.YELLOW + " First Joined " + CC.GOLD + CC.RIGHT_ARROW + " "
                + CC.WHITE + TimeUtils.formatDate(target.getFirstLogin(), InvictusSettings.TIME_ZONE.get(sender)));
        sender.sendMessage(CC.YELLOW + " Last Seen " + CC.GOLD + CC.RIGHT_ARROW + " " + CC.WHITE
                + (target.getLastServer() == null
                ? TimeUtils.formatDate(target.getLastSeen(), InvictusSettings.TIME_ZONE.get(sender))
                : "Online on " + target.getLastServer()));
        sender.sendMessage(CC.YELLOW + " Playtime " + CC.GOLD + CC.RIGHT_ARROW + " " + CC.WHITE
                + TimeUtils.formatDetailed(target.getTotalPlayTime()));
        sender.sendMessage(CC.YELLOW + " Permissions " + CC.GOLD + CC.RIGHT_ARROW + " "
                + CC.WHITE + "(" + target.getPermissions().size() + ") "
                + String.join(", ", target.getPermissions()));

        RequestResponse response = RequestHandler.get("discord/hasboosted/%s", target.getUuid().toString());
        boolean boosted = response.wasSuccessful() && response.asObject().get("boosted").getAsBoolean();

        sender.sendMessage(CC.YELLOW + " Nitro Boosted " + CC.GOLD + CC.RIGHT_ARROW + " " +
                CC.colorBoolean(boosted, "yes", "no"));
        sender.sendMessage(CC.SMALL_CHAT_BAR);
        return true;
    }

    @Command(names = {"profile addperm", "profile addpermission"},
             permission = "profile.command.argument.addperm",
             description = "Add a permission to a player",
             async = true)
    public boolean profileAddPerm(CommandSender sender,
                                  @Param(name = "player") Profile target,
                                  @Param(name = "permission") String permission) {
        if (target.getPermissions().contains(permission.toLowerCase())) {
            sender.sendMessage(CC.format("&cProfile &e%s &calready has permission &e%s&c.",
                    target.getName(), permission));
            return false;
        }

        target.getPermissions().add(permission.toLowerCase());
        target.save(() -> { }, true);
        invictus.getRedisService().publish(new ProfilePermissionUpdatePacket(target.getUuid()));
        sender.sendMessage(CC.format("&eAdded permission &c%s &eto profile %s&e.",
                permission, target.getRealDisplayName()));
        return true;
    }

    @Command(names = {"profile delperm", "profile deletepermission", "profile removeperm", "profile removepermission"},
             permission = "profile.command.argument.delperm",
             description = "Remove a permission from a player",
             async = true)
    public boolean profileDelPerm(CommandSender sender,
                                  @Param(name = "player") Profile target,
                                  @Param(name = "permission") String permission) {
        if (!target.getPermissions().contains(permission.toLowerCase())) {
            sender.sendMessage(CC.format("&cProfile &e%s &cdoesn't have permission &e%s&c.",
                    target.getName(), permission));
            return false;
        }

        target.getPermissions().remove(permission.toLowerCase());
        target.save(() -> { }, true);
        invictus.getRedisService().publish(new ProfilePermissionUpdatePacket(target.getUuid()));
        sender.sendMessage(CC.format("&eRemoved permission &c%s &efrom profile %s&e.",
                permission, target.getRealDisplayName()));
        return true;
    }

    @Command(names = {"profile forceupdate"},
             permission = "op",
             hidden = true,
             description = "Forcefully queue an update for a profile")
    public boolean forceupdateprofile(CommandSender sender, @Param(name = "player") UUID uuid) {
        sender.sendMessage(CC.YELLOW + "Forcefully queueing profile update for " + UUIDCache.getName(uuid) + ".");
        invictus.getRedisService().publish(new ProfileUpdatePacket(uuid));
        return true;
    }

    @Command(names = {"profile permdebug", "profile hasperm"},
             permission = "op",
             description = "Test if a player has a permission")
    public boolean permdebug(CommandSender sender,
                             @Param(name = "player") Player target,
                             @Param(name = "permission") String permission) {
        invictus.getPermissionService().getDebugInfo(target, permission).forEach(sender::sendMessage);
        sender.sendMessage(CC.format(
                "&9Bukkit Result: &e%s %s &9permission &e%s&9.",
                target.getName(),
                CC.colorBoolean(target.hasPermission(permission), "has", "doesn't have"),
                permission
        ));

        if (target.isOp())
            sender.sendMessage(CC.DRED + CC.BOLD + "!" + CC.RED
                    + " Player is op, bukkit result will always return true unless negated.");
        return true;
    }
}
