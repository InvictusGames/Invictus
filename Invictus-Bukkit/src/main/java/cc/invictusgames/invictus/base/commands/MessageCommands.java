package cc.invictusgames.invictus.base.commands;

import cc.invictusgames.ilib.command.annotation.Command;
import cc.invictusgames.ilib.command.annotation.Param;
import cc.invictusgames.ilib.messages.page.PagedMessage;
import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.ilib.utils.TimeUtils;
import cc.invictusgames.ilib.utils.UUIDUtils;
import cc.invictusgames.ilib.uuid.UUIDCache;
import cc.invictusgames.invictus.Invictus;
import cc.invictusgames.invictus.InvictusBukkit;
import cc.invictusgames.invictus.banphrase.Banphrase;
import cc.invictusgames.invictus.playersetting.InvictusSettings;
import cc.invictusgames.invictus.profile.Profile;
import cc.invictusgames.invictus.punishment.Punishment;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 08.06.2020 / 19:53
 * Invictus / cc.invictusgames.invictus.spigot.base.commands
 */

@RequiredArgsConstructor
public class MessageCommands {

    private final InvictusBukkit invictus;
    private final Map<UUID, UUID> messaging = new HashMap<>();

    @Command(names = {"message", "msg", "m", "tell", "whisper", "w"},
             permission = "player")
    public boolean message(Player sender,
                           @Param(name = "player") Player target,
                           @Param(name = "message", wildcard = true) String message) {
        handleMessage(
                invictus.getProfileService().getProfile(sender),
                invictus.getProfileService().getProfile(target),
                message
        );
        return true;
    }

    @Command(names = {"reply", "r", "respond"},
             permission = "player")
    public boolean reply(Player sender, @Param(name = "message", wildcard = true) String message) {
        UUID uuid = messaging.getOrDefault(sender.getUniqueId(), null);
        if (uuid == null) {
            sender.sendMessage(CC.RED + "You are not in a conversation.");
            return false;
        }

        Player target = Bukkit.getPlayer(uuid);
        if (target == null) {
            sender.sendMessage(CC.format("&e%s &cis no longer online.", UUIDCache.getName(uuid)));
            return false;
        }

        handleMessage(
                invictus.getProfileService().getProfile(sender),
                invictus.getProfileService().getProfile(target),
                message
        );
        return true;
    }

    @Command(names = {"togglemessages", "toggleprivatemessages", "togglepm", "tpm"},
             permission = "player")
    public boolean togglemessages(Player sender) {
        InvictusSettings.PRIVATE_MESSAGES.set(sender, !InvictusSettings.PRIVATE_MESSAGES.get(sender));
        sender.sendMessage(CC.format("&eYou have %s &eprivate messages.",
                CC.colorBoolean(InvictusSettings.PRIVATE_MESSAGES.get(sender))));
        return true;
    }

    @Command(names = {"togglesounds", "sounds"},
             permission = "player")
    public boolean togglesounds(Player sender) {
        InvictusSettings.MESSAGING_SOUNDS.set(sender, !InvictusSettings.MESSAGING_SOUNDS.get(sender));
        sender.sendMessage(CC.format("&eYou have %s &emessaging sounds.",
                CC.colorBoolean(InvictusSettings.MESSAGING_SOUNDS.get(sender))));
        return true;
    }

    @Command(names = {"socialspy list"},
             permission = "invictus.command.socialspy.list",
             description = "List all players that you are spying on",
             playerOnly = true)
    public boolean socialspyList(Player sender, @Param(name = "page", defaultValue = "1") int page) {
        Profile profile = invictus.getProfileService().getProfile(sender);
        if (profile.getOptions().getSocialSpy().isEmpty()) {
            sender.sendMessage(ChatColor.RED + "You are not spying on anyone.");
            return false;
        }

        new PagedMessage<String>() {
            @Override
            public List<String> getHeader(int page, int maxPages) {
                List<String> header = new ArrayList<>();
                header.add(CC.SMALL_CHAT_BAR);
                header.add(CC.RED + CC.BOLD + "Spy List " + CC.GRAY + "(Page " + page + "/" + maxPages + ")");
                return header;
            }

            @Override
            public List<String> getFooter(int page, int maxPages) {
                List<String> footer = new ArrayList<>();
                footer.add(" ");
                footer.add(CC.YELLOW + "Use " + CC.RED + "/socialspy list <page> " + CC.YELLOW + "to view more " +
                        "entries.");
                footer.add(CC.SMALL_CHAT_BAR);
                return footer;
            }

            @Override
            public void send(CommandSender sender, String s) {
                String name = UUIDUtils.isUUID(s) ? UUIDCache.getName(UUID.fromString(s)) : s;
                sender.sendMessage(CC.WHITE + " - " + CC.YELLOW + name);
            }
        }.display(sender, profile.getOptions().getSocialSpy(), page);
        return true;
    }

    @Command(names = {"socialspy add"},
             permission = "invictus.command.socialspy.add",
             description = "Add a player to your spy list",
             playerOnly = true,
             async = true)
    public boolean socialspyAdd(Player sender, @Param(name = "target") Profile target) {
        Profile profile = invictus.getProfileService().getProfile(sender);
        if (profile.getOptions().getSocialSpy().contains(target.getUuid().toString())) {
            sender.sendMessage(CC.format("&cYou are already spying on the messages of &e%s&c.", target.getName()));
            return false;
        }

        profile.getOptions().getSocialSpy().add(target.getUuid().toString());
        sender.sendMessage(CC.format("&eYou are now spying on the messages of &c%s&e.", target.getName()));
        profile.save(() -> {
        }, true);
        return true;
    }

    @Command(names = {"socialspy addall"},
             permission = "invictus.command.socialspy.add",
             description = "Add all players to your spy list",
             playerOnly = true)
    public boolean socialspyAddAll(Player sender) {
        Profile profile = invictus.getProfileService().getProfile(sender);
        if (profile.getOptions().getSocialSpy().contains("@ALL")) {
            sender.sendMessage(CC.RED + "You are already spying on the messages of " + CC.YELLOW + "all players"
                    + CC.RED + ".");
            return false;
        }

        profile.getOptions().getSocialSpy().add("@ALL");
        sender.sendMessage(CC.YELLOW + "You are now spying on the messages of " + CC.RED + "all players"
                + CC.YELLOW + ".");
        profile.save(() -> {
        }, true);
        return true;
    }

    @Command(names = {"socialspy remove"},
             permission = "invictus.command.socialspy.add",
             description = "Remove a player from your spy list",
             playerOnly = true,
             async = true)
    public boolean socialspyRemove(Player sender, @Param(name = "target") Profile target) {
        Profile profile = invictus.getProfileService().getProfile(sender);
        if (!profile.getOptions().getSocialSpy().contains(target.getUuid().toString())) {
            sender.sendMessage(CC.format("&cYou are not spying on the messages of &e%s&c.", target.getName()));
            return false;
        }

        profile.getOptions().getSocialSpy().remove(target.getUuid().toString());
        sender.sendMessage(CC.format("&eYou are no longer spying on the messages of &c%s&e.", target.getName()));
        profile.save(() -> {
        }, true);
        return true;
    }

    @Command(names = {"socialspy removeall"},
             permission = "invictus.command.socialspy.remove",
             description = "Remove all players from your spy list",
             playerOnly = true)
    public boolean socialspyRemoveAll(Player sender) {
        Profile profile = invictus.getProfileService().getProfile(sender);
        if (!profile.getOptions().getSocialSpy().contains("@ALL")) {
            sender.sendMessage(CC.RED + "You are not spying on the messages of " + CC.YELLOW + "all players"
                    + CC.RED + ".");
            return false;
        }

        profile.getOptions().getSocialSpy().remove("@ALL");
        sender.sendMessage(CC.YELLOW + "You are no longer spying on the messages of " + CC.RED + "all players"
                + CC.YELLOW + ".");
        profile.save(() -> {
        }, true);
        return true;
    }

    @Command(names = {"ignore list"},
             permission = "player",
             description = "List all players that you are ignoring")
    public boolean ignoreList(Player sender, @Param(name = "page", defaultValue = "1") int page) {
        Profile profile = invictus.getProfileService().getProfile(sender);
        if (profile.getOptions().getIgnoring().isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "You are not ignoring anyone.");
            return false;
        }

        new PagedMessage<UUID>() {
            @Override
            public List<String> getHeader(int page, int maxPages) {
                List<String> header = new ArrayList<>();
                header.add(CC.SMALL_CHAT_BAR);
                header.add(CC.RED + CC.BOLD + "Ignore List " + CC.GRAY + "(Page " + page + "/" + maxPages + ")");
                return header;
            }

            @Override
            public List<String> getFooter(int page, int maxPages) {
                List<String> footer = new ArrayList<>();
                footer.add(" ");
                footer.add(CC.YELLOW + "Use " + CC.RED + "/ignore list <page> " + CC.YELLOW + "to view more entries.");
                footer.add(CC.SMALL_CHAT_BAR);
                return footer;
            }

            @Override
            public void send(CommandSender sender, UUID uuid) {
                sender.sendMessage(CC.WHITE + " - " + CC.YELLOW + UUIDCache.getName(uuid));
            }
        }.display(sender, profile.getOptions().getIgnoring(), page);
        return true;
    }

    @Command(names = {"ignore clear"},
             permission = "player",
             description = "Clear your ignore list")
    public boolean ignoreClear(Player sender) {
        Profile profile = invictus.getProfileService().getProfile(sender);
        int size = profile.getOptions().getIgnoring().size();
        profile.getOptions().getIgnoring().clear();
        sender.sendMessage(CC.format("&eYour ignore list has been cleared (Removed &c%d &eplayers)", size));
        profile.save(() -> {
        }, true);
        return true;
    }

    @Command(names = {"ignore add"},
             permission = "player",
             description = "Add a player to your ignore list",
             async = true)
    public boolean ignoreAdd(Player sender, @Param(name = "player") Profile target) {
        Profile profile = invictus.getProfileService().getProfile(sender);
        if (profile.getOptions().getIgnoring().contains(target.getUuid())) {
            sender.sendMessage(CC.format("&eYou are already ignoring &e%s&c.",
                    target.isDisguised() ? target.getDisguiseName() : target.getName()));
            return false;
        }

        profile.getOptions().getIgnoring().add(target.getUuid());
        profile.save(() -> {
        }, true);
        sender.sendMessage(CC.format("&eYou are now ignoring &c%s&e.",
                target.isDisguised() ? target.getDisguiseName() : target.getName()));
        return true;
    }

    @Command(names = {"ignore remove"},
             permission = "player",
             description = "Remove a player to from ignore list",
             async = true)
    public boolean ignoreRemove(Player sender, @Param(name = "player") Profile target) {
        Profile profile = invictus.getProfileService().getProfile(sender);
        if (!profile.getOptions().getIgnoring().contains(target.getUuid())) {
            sender.sendMessage(CC.format("&cYou are not ignoring &e%s&c.",
                    target.isDisguised() ? target.getDisguiseName() : target.getName()));
            return false;
        }

        profile.getOptions().getIgnoring().remove(target.getUuid());
        profile.save(() -> {
        }, true);
        sender.sendMessage(CC.format("&eYou are no longer ignoring &c%s&e.",
                target.isDisguised() ? target.getDisguiseName() : target.getName()));
        return true;
    }

    private void handleMessage(Profile profile, Profile target, String message) {
        Punishment mute = profile.getActivePunishment(Punishment.PunishmentType.MUTE);

        if (mute != null) {
            if (mute.getDuration() == -1)
                profile.player().sendMessage(CC.format(
                        "&cYou have been permanently muted due to &e%s&c.",
                        mute.getPunishedReason()
                ));
            else profile.player().sendMessage(CC.format(
                    "&cYou have been muted for &e%s &cdue to &e%s&c. This punishment expires in &e%s&c.",
                    TimeUtils.formatDetailed(mute.getDuration()),
                    mute.getPunishedReason(),
                    TimeUtils.formatDetailed(mute.getRemainingTime())
            ));

            return;
        }

        if (!InvictusSettings.PRIVATE_MESSAGES.get(profile.player())
                && !profile.player().hasPermission("invictus.staff")) {
            profile.player().sendMessage(CC.RED + "You have private messages disabled.");
            return;
        }

        if (!InvictusSettings.PRIVATE_MESSAGES.get(target.player())
                && !profile.player().hasPermission("invictus.staff")) {
            profile.player().sendMessage(CC.format("&e%s &chas their private messages disabled",
                    target.player().getName()));
            return;
        }

        if (profile.getOptions().getIgnoring().contains(target.getUuid())
                && !profile.player().hasPermission("invictus.staff")) {
            profile.player().sendMessage(CC.format("&cYou are ignoring &e%s&c.", target.player().getName()));
            return;
        }

        if (target.getOptions().getIgnoring().contains(profile.getUuid())
                && !profile.player().hasPermission("invictus.staff")) {
            target.player().sendMessage(CC.format("&cYou cannot message &e%s&c.", target.player().getName()));
            return;
        }

        Banphrase banphrase = invictus.getBanphraseService().checkBanned(message);
        if (banphrase != null && !profile.player().hasPermission("invictus.filter.bypass")) {
            banphrase.punish(message, profile, target);
            profile.player().sendMessage(CC.GRAY + "(To " +
                    getDisplayName(target, profile.player()) +
                    CC.GRAY + ") " + message);
            return;
        }

        profile.player().sendMessage(CC.GRAY + "(To " +
                getDisplayName(target, profile.player()) +
                CC.GRAY + ") " + message);
        target.player().sendMessage(CC.GRAY + "(From " +
                getDisplayName(profile, target.player()) +
                CC.GRAY + ") " + message);
        if (InvictusSettings.MESSAGING_SOUNDS.get(target.player()))
            target.player().playSound(target.player().getLocation(), Sound.SUCCESSFUL_HIT, 1.0f, 0.1f);

        messaging.put(profile.getUuid(), target.getUuid());
        messaging.put(target.getUuid(), profile.getUuid());

        for (Player current : Bukkit.getOnlinePlayers()) {
            if (!current.hasPermission("invictus.socialspy"))
                continue;

            if (current.getUniqueId().equals(profile.getUuid()) || current.getUniqueId().equals(target.getUuid()))
                continue;

            Profile currentProfile = invictus.getProfileService().getProfile(current);
            if (currentProfile.getOptions().getSocialSpy().contains("@ALL")
                    || currentProfile.getOptions().getSocialSpy().contains(profile.getUuid().toString())
                    || currentProfile.getOptions().getSocialSpy().contains(target.getUuid().toString()))
                current.sendMessage(CC.GRAY + "(" +
                        getDisplayName(profile, current) +
                        CC.GRAY + " to " +
                        getDisplayName(target, current) +
                        CC.GRAY + ") " + message);
        }

        if (!InvictusSettings.PRIVATE_MESSAGES.get(profile.player())
                && profile.player().hasPermission("invictus.staff"))
            profile.player().sendMessage(ChatColor.RED + "You have private messages disabled, " +
                    "they won't be able to respond.");
    }

    private String getDisplayName(Profile profile, Player target) {
        String primeIcon = CC.GRAY + "[" + InvictusSettings.PRIME_COLOR.get(profile.player())
                + Invictus.PRIME_ICON + CC.GRAY + "] ";
        return (profile.hasPrimeStatus() ? primeIcon : "") +
                profile.getCurrentGrant().getRank().getPrefix() +
                (profile.isDisguised() ? profile.getDisguiseName() : profile.getName()) +
                profile.getCurrentGrant().getRank().getSuffix() +
                ((target == null || target.hasPermission("invictus.disguise.bypass")) && profile.isDisguised() ?
                        CC.GRAY + "(" + profile.getName() + ")" : "");
    }


}
