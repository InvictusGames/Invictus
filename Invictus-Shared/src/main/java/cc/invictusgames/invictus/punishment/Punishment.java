package cc.invictusgames.invictus.punishment;

import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.ilib.utils.TimeUtils;
import cc.invictusgames.ilib.utils.UUIDUtils;
import cc.invictusgames.ilib.uuid.UUIDCache;
import cc.invictusgames.invictus.IllegalSystemTypeException;
import cc.invictusgames.invictus.Invictus;
import cc.invictusgames.invictus.SystemType;
import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.bukkit.command.CommandSender;

import java.util.TimeZone;
import java.util.UUID;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 18.02.2020 / 20:38
 * Invictus / cc.invictusgames.invictus.spigot.punishment
 */

@AllArgsConstructor
@Data
public class Punishment {

    private final Invictus invictus;
    private final UUID id;
    private final UUID uuid;
    private final PunishmentType punishmentType;

    private String punishedBy = "N/A";
    private long punishedAt = System.currentTimeMillis();
    private String punishedReason = "N/A";
    private String punishedServerType = "N/A";
    private String punishedServer = "N/A";

    private String removedBy = "N/A";
    private long removedAt = -1;
    private String removedReason = "N/A";

    private long duration = -1;
    private long end = -1;
    private boolean removed = false;

    public Punishment(Invictus invictus, UUID uuid, PunishmentType punishmentType, String punishedBy,
                      String punishedReason, long duration) {
        this.invictus = invictus;
        this.id = UUID.randomUUID();
        this.uuid = uuid;
        this.punishmentType = punishmentType;
        this.punishedBy = punishedBy;
        this.punishedReason = punishedReason;
        this.punishedServerType = "Server";
        this.punishedServer = invictus.getServerName();
        this.duration = duration;
        this.end = duration == -1 ? -1 : punishedAt + duration;
    }

    public Punishment(Invictus invictus, JsonObject object) {
        this.invictus = invictus;
        this.id = UUID.fromString(object.get("id").getAsString());
        this.uuid = UUID.fromString(object.get("uuid").getAsString());
        this.punishmentType = PunishmentType.valueOf(object.get("punishmentType").getAsString());
        this.punishedBy = object.get("punishedBy").getAsString();
        this.punishedAt = object.get("punishedAt").getAsLong();
        this.punishedReason = object.get("punishedReason").getAsString();
        this.punishedServerType = object.get("punishedServerType").getAsString();
        this.punishedServer = object.get("punishedServer").getAsString();
        this.removedBy = object.get("removedBy").getAsString();
        this.removedAt = object.get("removedAt").getAsLong();
        this.removedReason = object.get("removedReason").getAsString();
        this.duration = object.get("duration").getAsLong();
        this.end = object.get("end").getAsLong();
        this.removed = object.get("removed").getAsBoolean();
    }

    public boolean isActive() {
        if (end == -1) {
            return true;
        }
        return end >= System.currentTimeMillis();
    }

    public long getRemainingTime() {
        if (this.end == -1) {
            return this.end;
        }
        return this.end - System.currentTimeMillis();
    }

    public String resolvePunishedBy() {
        return UUIDUtils.isUUID(this.punishedBy) ? UUIDCache.getName(UUID.fromString(this.punishedBy)) : punishedBy;
    }

    public String resolveRemovedBy() {
        return UUIDUtils.isUUID(this.removedBy) ? UUIDCache.getName(UUID.fromString(this.removedBy)) : removedBy;
    }

    public void send(CommandSender sender) {
        IllegalSystemTypeException.checkOrThrow(SystemType.BUKKIT);

        StringBuilder builder = new StringBuilder();
        builder.append(
                CC.GRAY + TimeUtils.formatTimeAgo(punishedAt) + ": "
                        + CC.RED + punishmentType.getContext() + " by "
                        + CC.YELLOW + (sender.hasPermission("invictus.punishments.viewexecutor")
                        ? resolvePunishedBy() : CC.ITALIC + "Hidden" + CC.YELLOW) + ": "
                        + CC.RED + punishedReason
                        + CC.GRAY + " (" + CC.YELLOW + (duration == -1
                        ? "Permanent punishment" : "Duration: " + TimeUtils.formatDetailed(duration)) + CC.GRAY + ")"
        );
        if (removed) {
            builder.append(
                    CC.GRAY + " * "
                            + CC.RED + "Removed: "
                            + CC.YELLOW + (sender.hasPermission("invictus.punishments.viewexecutor")
                            ? resolveRemovedBy() : CC.ITALIC + "Hidden" + CC.YELLOW) + ": "
                            + CC.RED + removedReason
            );
        } else if (!this.isActive()) {
            builder.append(CC.GRAY + " * " + CC.GREEN + "Expired");
        } else {
            builder.append(CC.GRAY + " * " + CC.DRED + "Active");
        }
        sender.sendMessage(builder.toString());
    }

    public String formatPasteEntry(CommandSender sender, TimeZone timeZone) {
        IllegalSystemTypeException.checkOrThrow(SystemType.BUKKIT);

        StringBuilder builder = new StringBuilder();
        builder.append("(" + TimeUtils.formatDate(punishedAt, timeZone) + "|" + punishedAt + ") ");
        builder.append(punishmentType.getContext() + " by ");
        if (sender.hasPermission("invictus.punishments.viewexecutor"))
            builder.append(resolvePunishedBy() + ": ");
        else
            builder.append("Hidden: ");

        builder.append(punishedReason + " ");
        builder.append("(" + (duration == -1 ? "Permanent punishment" :
                "Duration: " + TimeUtils.formatDetailed(duration)) + ")");
        if (removed) {
            builder.append(" | Removed ");
            if (sender.hasPermission("invictus.punishments.viewexecutor"))
                builder.append(resolveRemovedBy() + ": ");
            else
                builder.append("Hidden: ");
            builder.append(removedReason);
        } else if (!this.isActive()) {
            builder.append(" | Expired");
        } else {
            builder.append(" | Active");
        }
        return builder.toString();
    }

    @AllArgsConstructor
    @Getter
    public enum PunishmentType {
        WARN("Warn", "Warned", ""),
        KICK("Kick", "Kicked", ""),
        MUTE("Mute", "Muted", "Unmuted"),
        BAN("Ban", "Banned", "Unbanned"),
        BLACKLIST("Blacklist", "Blacklisted", "Unblacklisted");

        private String name;
        private String context;
        private String removeContext;

        public boolean isOverwritable() {
            return this == BAN
                    || this == MUTE
                    || this == BLACKLIST;
        }
    }
}
