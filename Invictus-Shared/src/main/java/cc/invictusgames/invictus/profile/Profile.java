package cc.invictusgames.invictus.profile;

import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.ilib.utils.Timings;
import cc.invictusgames.ilib.utils.callback.Callable;
import cc.invictusgames.ilib.utils.json.JsonBuilder;
import cc.invictusgames.invictus.IllegalSystemTypeException;
import cc.invictusgames.invictus.Invictus;
import cc.invictusgames.invictus.SystemType;
import cc.invictusgames.invictus.connection.RequestHandler;
import cc.invictusgames.invictus.connection.RequestResponse;
import cc.invictusgames.invictus.disguise.DisguiseData;
import cc.invictusgames.invictus.grant.Grant;
import cc.invictusgames.invictus.grant.procedure.GrantProcedure;
import cc.invictusgames.invictus.profile.packets.ProfileUpdatePacket;
import cc.invictusgames.invictus.punishment.Punishment;
import cc.invictusgames.invictus.rank.Rank;
import cc.invictusgames.invictus.tag.Tag;
import cc.invictusgames.invictus.utils.Tasks;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.Data;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 18.02.2020 / 21:00
 * Invictus / cc.invictusgames.invictus.spigot.profile
 */

@Data
public class Profile {

    public static final Comparator<Profile> WEIGHT_COMPARATOR =
            Collections.reverseOrder(Comparator.comparingInt(profile
                    -> profile.getCurrentGrant().getRank().getWeight()));

    public static final Comparator<Profile> REAL_WEIGHT_COMPARATOR =
            Collections.reverseOrder(Comparator.comparingInt(profile
                    -> profile.getRealCurrentGrant().getRank().getWeight()));

    private final Invictus invictus;
    private final UUID uuid;
    private final Lock lock = new ReentrantLock();
    private String name = "N/A";
    private String lastIp = "N/A";
    private List<String> knownIps = new ArrayList<>();

    private ProfileOptions options;

    private Tag activeTag;

    private CopyOnWriteArrayList<Grant> activeGrants = new CopyOnWriteArrayList<>();
    private List<Punishment> punishments = new ArrayList<>();
    private List<Profile> alts = null;
    private List<String> permissions = new ArrayList<>();

    private long firstLogin = System.currentTimeMillis();
    private long lastSeen = System.currentTimeMillis();
    private long joinTime = -1;
    private long lastSpeakMillis;

    private Timings session;
    private long playTime = 0;

    private String lastServer = null;
    private boolean nitroBoosted = false;
    private boolean frozen = false;
    private boolean requiresAuthentication = false;
    private int authenticationFailures = 0;

    private DisguiseData disguiseData;
    private boolean isDisguised = false;
    private String disguiseName = "N/A";

    private GrantProcedure grantProcedure = null;

    public Profile(Invictus invictus, JsonObject object) {
        this.invictus = invictus;
        this.uuid = UUID.fromString(object.get("uuid").getAsString());
        this.session = new Timings(name + "-session");
        update(object);
    }

    public Profile(Invictus invictus, UUID uuid, String name) {
        this.invictus = invictus;
        this.uuid = uuid;
        this.name = name;
        this.session = new Timings(name + "-session");
        this.disguiseData = new DisguiseData(invictus, uuid);
        this.options = new ProfileOptions();
    }

    public JsonObject toJson() {
        JsonBuilder builder = new JsonBuilder();
        builder.add("uuid", uuid);
        builder.add("name", name);
        builder.add("lastIp", lastIp);

        JsonArray knownIpsArray = new JsonArray();
        knownIps.forEach(knownIpsArray::add);
        builder.add("knownIps", knownIpsArray);

        builder.add("options", options.toJson());

        JsonArray permissionsArray = new JsonArray();
        permissions.forEach(permissionsArray::add);
        builder.add("permissions", permissionsArray);

        builder.add("firstLogin", firstLogin);
        builder.add("lastSeen", lastSeen);
        builder.add("joinTime", joinTime);
        builder.add("playTime", playTime + session.calculateDifference());
        builder.add("lastServer", lastServer);
        builder.add("activeTag", activeTag == null ? null : activeTag.getName());
        return builder.build();
    }

    public void save(Callable callable, boolean async) {
        if (!async)
            this.lock.lock();
        try {
            if (async) {
                Tasks.runAsync(() -> save(callable, false));
                return;
            }

            this.disguiseData.save(() -> {}, false);

            RequestResponse response = RequestHandler.put("profile", toJson());
            if (response.wasSuccessful())
                invictus.getRedisService().publish(new ProfileUpdatePacket(this.uuid));
            else ProfileService.LOG.warning(String.format(
                    "Could not save profile of %s (%s): %s (%d)",
                    uuid.toString(),
                    name,
                    response.getErrorMessage(),
                    response.getCode()
            ));
            callable.callback();
        } finally {
            if (!async)
                this.lock.unlock();
        }
    }

    public void update(JsonObject object) {
        this.lock.lock();
        try {
            name = object.get("name").getAsString();
            lastIp = object.get("lastIp").getAsString();

            knownIps.clear();
            object.get("knownIps").getAsJsonArray().forEach(element ->
                    knownIps.add(element.getAsString()));

            this.options = new ProfileOptions(object.get("options").getAsJsonObject());

            this.activeGrants.clear();
            object.get("activeGrants").getAsJsonArray().forEach(element ->
                    activeGrants.add(new Grant(invictus, element.getAsJsonObject())));

            this.permissions.clear();
            if (object.has("permissions")) {
                object.get("permissions").getAsJsonArray().forEach(element ->
                        permissions.add(element.getAsString()));
            }

            this.punishments.clear();
            punishments.addAll(invictus.getPunishmentService().getPunishments(uuid));

            this.disguiseData = invictus.getDisguiseService().getDisguiseData(this.uuid);
            this.isDisguised = !disguiseData.getDisguiseName().equals("N/A");
            this.disguiseName = disguiseData.getDisguiseName();

            if (object.has("activeTag"))
                this.activeTag = invictus.getTagService().getTag(object.get("activeTag").getAsString());

            this.firstLogin = object.get("firstLogin").getAsLong();
            this.lastSeen = object.get("lastSeen").getAsLong();
            this.playTime = object.get("playTime").getAsLong();
            this.joinTime = object.get("joinTime").getAsLong();

            if (object.has("lastServer"))
                this.lastServer = object.get("lastServer").getAsString();
            else this.lastServer = null;

            if (Invictus.getSystemType() == SystemType.BUKKIT) {
                if (Bukkit.getPlayer(this.uuid) != null)
                    Bukkit.getPlayer(this.uuid).setDisplayName(this.getDisplayName());
            }
        } finally {
            this.lock.unlock();
        }
    }

    public boolean canInteract(Profile other) {
        long weight = this.getRealCurrentGrant().getRank().getWeight();
        long otherWeight = other.getRealCurrentGrant().getRank().getWeight();

        if (weight >= invictus.getMainConfig().getOwnerWeight())
            return true;

        if (weight >= invictus.getMainConfig().getAdminWeight()
                && otherWeight < invictus.getMainConfig().getAdminWeight())
            return true;

        return otherWeight < invictus.getMainConfig().getStaffWeight();
    }

    public List<Grant> getAllActiveGrants() {
        List<Grant> list = new ArrayList<>();
        for (Grant grant : activeGrants) {
            if (grant.isActive() && !grant.isRemoved() && grant.getRank() != null)
                list.add(grant);
        }

        list.sort(Grant.COMPARATOR.reversed());
        return list;
    }

    public List<Grant> getActiveGrants() {
        List<Grant> activeGrants = this.getAllActiveGrants();
        activeGrants.removeIf(grant -> !grant.isActiveOnScope());
        return activeGrants;
    }

    public List<Grant> getActiveGrantsOn(String scope) {
        List<Grant> activeGrants = getAllActiveGrants();
        activeGrants.removeIf(grant -> !grant.isActiveOn(scope));
        return activeGrants;
    }

    public boolean hasGrantOf(Rank rank) {
        for (Grant grant : getActiveGrants()) {
            if (grant.getUuid().equals(rank.getUuid()))
                return true;
        }

        return false;
    }

    public boolean hasGrantOf(String rank) {
        for (Grant grant : getActiveGrants()) {
            if (grant.getRank().getName().equalsIgnoreCase(rank))
                return true;
        }

        return false;
    }

    public Grant getCurrentGrant() {
        if (this.isDisguised) {
            return new Grant(
                    this.invictus,
                    this.uuid,
                    this.disguiseData.getDisguiseRank(),
                    "Console",
                    System.currentTimeMillis(),
                    "Disgused",
                    -1,
                    Collections.singletonList("GLOBAL")
            );
        }

        return this.getRealCurrentGrant();
    }

    public Grant getRealCurrentGrant() {
        Grant grant = null;

        for (Grant current : this.getActiveGrants()) {
            if (grant == null) {
                grant = current;
                continue;
            }
            if (current.getRank().getWeight() > grant.getRank().getWeight()) {
                grant = current;
            }
        }

        if (grant == null) {
            grant = new Grant(
                    this.invictus,
                    this.uuid,
                    invictus.getRankService().getDefaultRank(),
                    "Console",
                    System.currentTimeMillis(),
                    "Default Grant",
                    -1,
                    Collections.singletonList("GLOBAL")
            );
        }

        return grant;
    }

    public Grant getCurrentGrantOn(String scope) {
        if (this.isDisguised) {
            return new Grant(
                    this.invictus,
                    this.uuid,
                    this.disguiseData.getDisguiseRank(),
                    "Console",
                    System.currentTimeMillis(),
                    "Disgused",
                    -1,
                    Collections.singletonList("GLOBAL")
            );
        }

        return this.getRealCurrentGrantOn(scope);
    }

    public Grant getRealCurrentGrantOn(String scope) {
        Grant grant = null;

        for (Grant current : this.getActiveGrantsOn(scope)) {
            if (grant == null) {
                grant = current;
                continue;
            }
            if (current.getRank().getWeight() > grant.getRank().getWeight()) {
                grant = current;
            }
        }

        if (grant == null) {
            grant = new Grant(
                    this.invictus,
                    this.uuid,
                    invictus.getRankService().getDefaultRank(),
                    "Console",
                    System.currentTimeMillis(),
                    "Default Grant",
                    -1,
                    Collections.singletonList("GLOBAL")
            );
        }

        return grant;
    }

    public int getQueuePriority(String scope) {
        Grant grant = null;

        for (Grant current : this.getActiveGrantsOn(scope)) {
            if (grant == null) {
                grant = current;
                continue;
            }
            if (current.getRank().getQueuePriority() > grant.getRank().getQueuePriority()) {
                grant = current;
            }
        }

        return (grant == null ? 0 : grant.getRank().getQueuePriority()) + (hasPrimeStatus() ? 1 : 0);
    }

    public List<Punishment> getPunishments(Punishment.PunishmentType type) {
        List<Punishment> list = new ArrayList<>();
        for (Punishment punishment : punishments) {
            if (punishment.getPunishmentType().equals(type))
                list.add(punishment);
        }
        return list;
    }

    public Punishment getActivePunishment(Punishment.PunishmentType type) {
        for (Punishment punishment : punishments) {
            if (punishment.isActive() && !punishment.isRemoved() && punishment.getPunishmentType().equals(type))
                return punishment;
        }
        return null;
    }

    public String getDisplayName() {
        return this.getCurrentGrant().getRank().getColor() + (this.isDisguised ? this.disguiseName : this.name);
    }

    public String getDisplayName(CommandSender target) {
        IllegalSystemTypeException.checkOrThrow(SystemType.BUKKIT);

        return this.getDisplayName() +
                (((target == null || target.hasPermission("invictus.disguise.bypass")) && this.isDisguised) ?
                        CC.GRAY + "(" + this.name + ")" : "");
    }

    public String getDisplayName(net.md_5.bungee.api.CommandSender target) {
        IllegalSystemTypeException.checkOrThrow(SystemType.BUNGEE);

        return this.getDisplayName() +
                (((target == null || target.hasPermission("invictus.disguise.bypass")) && this.isDisguised) ?
                        CC.GRAY + "(" + this.name + ")" : "");
    }


    public String getRealDisplayName() {
        return this.getRealCurrentGrant().getRank().getColor() + this.name;
    }

    public Player player() {
        IllegalSystemTypeException.checkOrThrow(SystemType.BUKKIT);

        return Bukkit.getPlayer(this.uuid);
    }

    public ProxiedPlayer proxiedPlayer() {
        IllegalSystemTypeException.checkOrThrow(SystemType.BUNGEE);

        return ProxyServer.getInstance().getPlayer(this.uuid);
    }

    public long getTotalPlayTime() {
        return playTime + session.calculateDifference();
    }

    public boolean hasPrimeStatus() {
        return hasGrantOf("prime");
    }

}
