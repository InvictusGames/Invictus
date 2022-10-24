package cc.invictusgames.invictus.vote.command;

import cc.invictusgames.ilib.command.annotation.Command;
import cc.invictusgames.ilib.command.annotation.Param;
import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.invictus.Invictus;
import cc.invictusgames.invictus.profile.Profile;
import cc.invictusgames.invictus.vote.VoteService;
import cc.invictusgames.invictus.vote.handler.VoteHandler;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

@RequiredArgsConstructor
public class VoteCommand {

    private static final String TICK_SIGN = CC.GREEN + CC.BOLD + "✔";
    private static final String X_SIGN = CC.RED + CC.BOLD + "✕";

    private final VoteService voteService;

    @Command(names = {"vote"},
            description = "Vote for the server",
            playerOnly = true,
            async = true)
    public boolean vote(Player sender) {
        sender.sendMessage(" ");
        sender.sendMessage(CC.GOLD + CC.BOLD + "Vote for Brave");
        sender.sendMessage(CC.GRAY + "Follow the steps below to claim your free rank.");
        sender.sendMessage(" ");

        for (VoteHandler handler : voteService.getHandler()) {
            sender.sendMessage(CC.format(
                    "&7 - &6&lStep %d&7: &f%s %s",
                    handler.getPriority(),
                    handler.getFancyName(),
                    handler.hasVoted(sender.getUniqueId()) ? TICK_SIGN : X_SIGN
            ));
        }

        sender.sendMessage(" ");
        sender.sendMessage(CC.GRAY + "Once you have completed all steps, type "
                + CC.PINK + "/claimrank" + CC.GRAY + ".");
        sender.sendMessage(" ");
        return true;
    }

    @Command(names = {"voteinfo"},
            permission = "invictus.command.voteinfo",
            description = "Check someones vote status")
    public boolean voteInfo(CommandSender sender, @Param(name = "target") UUID target) {
        for (VoteHandler handler : voteService.getHandler())
            sender.sendMessage(CC.YELLOW + handler.getServiceName() + ": "
                    + CC.colorBoolean(handler.hasVoted(target), "Yes", "No"));
        return true;
    }

    @Command(names = {"claimrank"},
            description = "Claim your free rank from voting",
            playerOnly = true,
            async = true)
    public boolean claimrank(Player sender) {
        for (VoteHandler voteHandler : voteService.getHandler()) {
            if (!voteHandler.hasVoted(sender.getUniqueId())) {
                sender.sendMessage(CC.RED + "You have not completed all the steps in /vote.");
                return false;
            }
        }

        Profile profile = Invictus.getInstance().getProfileService().getProfile(sender);
        if (profile.getRealCurrentGrant().getRank().getWeight() >= 10) {
            sender.sendMessage(CC.RED + "You already have a higher or equal rank.");
            return false;
        }

        Bukkit.broadcastMessage(" ");
        Bukkit.broadcastMessage(CC.format("&6%s &7has claimed their free &fIron &7Rank.", sender.getName()));
        Bukkit.broadcastMessage(CC.format("&7&oYou can do the same by typing &6/%s&7.", "claimrank"));
        Bukkit.broadcastMessage(" ");

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), String.format("cgrant %s %s perm global Claimed.",
                sender.getName(), "Iron"));
        return true;
    }

}
