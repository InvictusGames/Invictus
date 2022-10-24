package cc.invictusgames.invictus.base.commands;

import cc.invictusgames.ilib.command.annotation.Command;
import cc.invictusgames.ilib.command.annotation.CommandCooldown;
import cc.invictusgames.ilib.command.annotation.Param;
import cc.invictusgames.ilib.redis.RedisCommand;
import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.invictus.InvictusBukkit;
import cc.invictusgames.invictus.profile.Profile;
import cc.invictusgames.invictus.utils.NetworkBroadcastPacket;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 14.09.2020 / 17:42
 * Invictus / cc.invictusgames.invictus.spigot.base.commands
 */

@RequiredArgsConstructor
public class ReportCommands {

    private final InvictusBukkit invictus;

    @Command(names = {"report"},
             description = "Report a player",
             async = true)
    @CommandCooldown(time = 2,
                     timeUnit = TimeUnit.MINUTES)
    public boolean report(Player sender,
                          @Param(name = "player") Player targetPlayer,
                          @Param(name = "reason", wildcard = true) String reason) {
        if (sender == targetPlayer) {
            sender.sendMessage(CC.RED + "You cannot report yourself.");
            return false;
        }

        Profile profile = invictus.getProfileService().getProfile(sender);
        Profile target = invictus.getProfileService().getProfile(targetPlayer);

        AtomicInteger amount = new AtomicInteger();
        invictus.getRedisService().executeBackendCommand((RedisCommand<String>) redis -> {
            if (redis.exists("reports:" + target.getUuid().toString())) {
                amount.set(Math.toIntExact(redis.zcount(
                        "reports:" + target.getUuid().toString(),
                        System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(10),
                        System.currentTimeMillis())));
            }

            redis.zadd("reports:" + target.getUuid().toString(),
                    System.currentTimeMillis(),
                    String.valueOf(System.currentTimeMillis()));
            redis.expire("reports:" + target.getUuid().toString(), (int) TimeUnit.MINUTES.toSeconds(10));
            return null;
        });

        invictus.getRedisService().publish(new NetworkBroadcastPacket(
                invictus.getMessageService().formatMessage(
                        "staff.report",
                        invictus.getServerName(),
                        target.getDisplayName((CommandSender) null),
                        amount.get() + 1,
                        profile.getDisplayName((CommandSender) null),
                        CC.strip(reason)
                ),
                "invictus.staff",
                true
        ));

        sender.sendMessage(CC.GREEN + "Your report has been sent to all online staff members.");
        return true;
    }

    @Command(names = {"request", "helpop"},
             description = "Request assistance from the staff team")
    @CommandCooldown(time = 2,
                     timeUnit = TimeUnit.MINUTES)
    public boolean request(Player sender, @Param(name = "message", wildcard = true) String message) {
        Profile profile = invictus.getProfileService().getProfile(sender);
        invictus.getRedisService().publish(new NetworkBroadcastPacket(
                invictus.getMessageService().formatMessage(
                        "staff.request",
                        invictus.getServerName(),
                        profile.getDisplayName((CommandSender) null),
                        CC.strip(message)
                ),
                "invictus.staff",
                true
        ));
        sender.sendMessage(CC.GREEN + "Your request has been sent to all online staff members.");
        return true;
    }

}
