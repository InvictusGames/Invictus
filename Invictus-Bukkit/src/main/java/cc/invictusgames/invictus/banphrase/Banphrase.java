package cc.invictusgames.invictus.banphrase;

import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.ilib.utils.ChatMessage;
import cc.invictusgames.ilib.utils.TimeUtils;
import cc.invictusgames.invictus.Invictus;
import cc.invictusgames.invictus.playersetting.InvictusSettings;
import cc.invictusgames.invictus.profile.Profile;
import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 16.06.2021 / 19:02
 * Invictus / cc.invictusgames.invictus.banphrase
 */

@Data
public class Banphrase implements Comparable<Banphrase> {

    private final UUID id;
    private String name;

    private String phrase;
    private BanphraseOperator operator = BanphraseOperator.CONTAINS;
    private Pattern pattern = null;

    private MuteMode muteMode = MuteMode.NONE;
    private long duration = TimeUnit.MINUTES.toMillis(5);

    private boolean enabled = true;
    private boolean caseSensitive = false;

    public Banphrase(String name, String phrase) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.phrase = phrase;
    }

    public Banphrase(JsonObject object) {
        this.id = UUID.fromString(object.get("id").getAsString());
        this.name = object.get("name").getAsString();
        this.phrase = object.get("phrase").getAsString();
        this.operator = BanphraseOperator.valueOf(object.get("operator").getAsString());
        this.muteMode = MuteMode.valueOf(object.get("muteMode").getAsString());
        this.duration = object.get("duration").getAsLong();
        this.enabled = object.get("enabled").getAsBoolean();
        this.caseSensitive = object.get("caseSensitive").getAsBoolean();

        if (!caseSensitive && operator != BanphraseOperator.REGEX)
            this.phrase = phrase.toLowerCase();

        if (operator == BanphraseOperator.REGEX)
            pattern = caseSensitive
                    ? Pattern.compile(phrase)
                    : Pattern.compile(phrase, Pattern.CASE_INSENSITIVE);
    }

    public boolean matches(String message) {
        if (!caseSensitive)
            message = message.toLowerCase();

        switch (operator) {
            case CONTAINS:
                return message.contains(phrase);

            case EXACT:
                return message.equals(phrase);

            case STARTS_WITH:
                return message.startsWith(phrase);

            case ENDS_WITH:
                return message.endsWith(phrase);

            case REGEX:
                return pattern.matcher(message).find();
        }

        return false;
    }

    public void setPhrase(String phrase) {
        if (!caseSensitive && operator != BanphraseOperator.REGEX)
            this.phrase = phrase.toLowerCase();
        else this.phrase = phrase;
    }

    @Override
    public int compareTo(@NotNull Banphrase other) {
        if (duration == -1 && other.duration == -1)
            return 0;

        if (other.duration == -1)
            return -1;

        if (duration == -1)
            return 1;

        if (duration == other.getDuration())
            return 0;

        return duration > other.getDuration() ? 1 : -1;
    }

    public String colorize(String message) {
        Matcher matcher;
        if (pattern != null)
            matcher = pattern.matcher(message);
        else matcher = Pattern.compile((operator == BanphraseOperator.STARTS_WITH ? "^" : "") + phrase
                        + (operator == BanphraseOperator.ENDS_WITH ? "$" : ""),
                caseSensitive ? 0 : Pattern.CASE_INSENSITIVE).matcher(message);

        while (matcher.find()) {
            message = message.replaceFirst(matcher.group(),
                    CC.YELLOW + CC.UNDER_LINE + matcher.group() + CC.YELLOW);
        }
        return CC.YELLOW + message;
    }

    public void punish(String message, Profile sender, Profile target) {
        StringBuilder broadcast = new StringBuilder()
                .append(CC.RED).append(CC.BOLD).append("[Filtered] ");
        if (target == null) {
            broadcast.append(getDisplayName(sender, null))
                    .append(CC.GRAY).append(": ");
        } else {
            broadcast.append(CC.GRAY).append("(")
                    .append(getDisplayName(sender, null))
                    .append(CC.GRAY).append(" to ")
                    .append(getDisplayName(target, null))
                    .append(CC.GRAY).append(") ");
        }

        broadcast.append(colorize(message));

        if (muteMode == MuteMode.SUGGEST) {
            ChatMessage chatMessage = new ChatMessage(broadcast.toString());

            chatMessage.suggestCommand(String.format(
                    "/mute %s %s Matched Banphrase: %s",
                    sender.getName(),
                    duration == -1 ? "@perm" : TimeUtils.formatTimeShort(duration).replace(" ", ""),
                    name
            ));

            chatMessage.hoverText(CC.format("&eClick to mute &c%s", sender.getName()));

            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.hasPermission("invictus.staff") && InvictusSettings.STAFF_MESSAGES.get(player))
                    chatMessage.send(player);
            }

            Bukkit.getConsoleSender().sendMessage(broadcast.toString());
            return;
        }

        if (muteMode == MuteMode.MUTE)
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), String.format(
                    "mute %s %s Matched Banphrase: %s",
                    sender.getName(),
                    duration == -1 ? "@perm" : TimeUtils.formatTimeShort(duration).replace(" ", ""),
                    name
            ));

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("invictus.staff") && InvictusSettings.STAFF_MESSAGES.get(player))
                player.sendMessage(broadcast.toString());
        }

        Bukkit.getConsoleSender().sendMessage(broadcast.toString());
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

    @Getter
    @AllArgsConstructor
    public enum BanphraseOperator {

        CONTAINS("Contains", "Checks if the message contains the phrase"),
        EXACT("Exact match", "Only matches if the message is exactly the phrase"),
        REGEX("Regular Expression", "Checks if a regular expression matches the phrase"),
        STARTS_WITH("Starts with", "Checks if the message starts with the phrase"),
        ENDS_WITH("Ends with", "Checks if the message ends with the phrase");

        private final String display;
        private final String description;

    }

    @Getter
    @AllArgsConstructor
    public enum MuteMode {

        MUTE("Mute", "Automatically mute the sender if the phrase matches"),
        SUGGEST("Suggest", "Suggest a prepared mute command for staff to run if the phrase matches"),
        NONE("None", "Do nothing, only filters the message out if the phrase matches");

        private final String display;
        private final String description;

    }
}
