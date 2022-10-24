package cc.invictusgames.invictus.punishment.commands;

import cc.invictusgames.ilib.command.annotation.Command;
import cc.invictusgames.ilib.command.annotation.Param;
import cc.invictusgames.ilib.command.parameter.defaults.Duration;
import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.ilib.utils.TimeUtils;
import cc.invictusgames.ilib.utils.Timings;
import cc.invictusgames.ilib.utils.json.JsonBuilder;
import cc.invictusgames.invictus.InvictusBukkit;
import cc.invictusgames.invictus.connection.RequestHandler;
import cc.invictusgames.invictus.connection.RequestResponse;
import cc.invictusgames.invictus.profile.Profile;
import cc.invictusgames.invictus.punishment.packets.StaffRollbackPacket;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 08.03.2021 / 23:00
 * Invictus / cc.invictusgames.invictus.punishment.commands
 */

@RequiredArgsConstructor
public class StaffRollbackCommand {

    private final InvictusBukkit invictus;

    @Command(names = {"staffrollback"},
             permission = "invictus.command.staffrollback",
             description = "Rollback all punishments a player has done in a certain amount of time",
             async = true)
    public boolean staffRollback(CommandSender sender,
                                 @Param(name = "target") Profile target,
                                 @Param(name = "duration") Duration duration) {

        if (duration.isPermanent())
            sender.sendMessage(CC.format("&aStarting to rollback &eall &apunishments of %s&a...",
                    target.getRealDisplayName()));
        else sender.sendMessage(CC.format("&aStarting to rollback punishments in rage of &e%s &aof %s&a...",
                TimeUtils.formatDetailed(duration.getDuration()), target.getRealDisplayName()));

        JsonBuilder body = new JsonBuilder();
        body.add("punishedBy", target.getUuid());
        body.add("removedReason", "Staff Rollback");
        body.add("removedBy", sender instanceof Player ? ((Player) sender).getUniqueId().toString() : "Console");
        body.add("removedAt", System.currentTimeMillis());
        body.add("maxTime", System.currentTimeMillis() - duration.getDuration());

        Timings timings = new Timings("staff-rollback").startTimings();
        RequestResponse response = RequestHandler.put("punishment/staffrollback", body.build());
        timings.stopTimings();

        if (!response.wasSuccessful()) {
            sender.sendMessage(CC.format("Could not rollback punishments: %s (%d)",
                    response.getErrorMessage(), response.getCode()));
            return false;
        }

        sender.sendMessage(CC.format("&aRolled back &e%d &apunishments in &e%dms&a.",
                response.asObject().get("cleared").getAsInt(), timings.calculateDifference()));

        invictus.getRedisService().publish(new StaffRollbackPacket(target.getUuid()));
        return true;
    }

}
