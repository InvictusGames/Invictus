package cc.invictusgames.invictus.profile;

import cc.invictusgames.ilib.utils.callback.TypeCallable;
import cc.invictusgames.ilib.uuid.UUIDCache;
import cc.invictusgames.invictus.Invictus;
import cc.invictusgames.invictus.connection.RequestHandler;
import cc.invictusgames.invictus.connection.RequestResponse;
import cc.invictusgames.invictus.utils.Tasks;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.logging.Logger;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 18.02.2020 / 21:10
 * Invictus / cc.invictusgames.invictus.spigot.profile
 */

@RequiredArgsConstructor
public class ProfileService {

    public static final Logger LOG = Invictus.getInstance().getLogFactory().newLogger(ProfileService.class);

    private final Invictus invictus;

    @Getter
    private final Map<UUID, Profile> profiles = new HashMap<>();

    public void loadProfile(UUID uuid, TypeCallable<Profile> callable, boolean async) {
        if (uuid == null) {
            callable.callback(null);
            return;
        }

        if (getProfile(uuid) != null) {
            callable.callback(getProfile(uuid));
            return;
        }

        if (async) {
            Tasks.runAsync(() -> loadProfile(uuid, callable, false));
            return;
        }

        RequestResponse response = RequestHandler.get("profile/%s", uuid.toString());
        if (!response.wasSuccessful()) {
            callable.callback(null);
            return;
        }

        Profile profile = new Profile(invictus, response.asObject());
        if (getProfile(uuid) != null) {
            callable.callback(getProfile(uuid));
            return;
        }

        profiles.put(uuid, profile);
        callable.callback(profile);
    }

    public Profile loadProfile(UUID uuid) {
        Profile profile = getProfile(uuid);
        if (profile != null)
            return profile;

        RequestResponse response = RequestHandler.get("profile/%s", uuid.toString());
        if (!response.wasSuccessful()) {
            return null;
        }

        profile = new Profile(invictus, response.asObject());
        profiles.put(uuid, profile);
        return profile;
    }

    private void createProfile(UUID uuid, String name, String ip, TypeCallable<Profile> callable, boolean async) {
        if (async) {
            Tasks.runAsync(() -> createProfile(uuid, name, ip, callable, false));
            return;
        }

        Profile profile = new Profile(invictus, uuid, name);
        profile.setLastIp(ip);
        profile.getKnownIps().add(ip);

        RequestResponse response = RequestHandler.post("profile", profile.toJson());
        if (response.wasSuccessful())
            loadProfile(uuid, callable, false);
        else {
            LOG.warning(String.format(
                    "Could not create profile for %s (%s): %s (%d)",
                    uuid.toString(),
                    name,
                    response.getErrorMessage(),
                    response.getCode()
            ));
            callable.callback(null);
        }
    }

    public void getProfileOrCreate(UUID uuid, String name, String ip, TypeCallable<Profile> callable, boolean async) {
        loadProfile(uuid, profile -> {
            if (profile != null) {
                callable.callback(profile);
                return;
            }

            createProfile(uuid, name, ip, callable, async);
        }, async);
    }

    public Profile getProfile(UUID uuid) {
        return profiles.getOrDefault(uuid, null);
    }

    public Profile getProfile(Player player) {
        return getProfile(player.getUniqueId());
    }

    public void removeProfile(UUID uuid) {
        profiles.remove(uuid);
    }

    public void updateProfile(UUID uuid, TypeCallable<Profile> callable, boolean async) {
        if (async) {
            Tasks.runAsync(() -> updateProfile(uuid, callable, false));
            return;
        }

        Profile profile = getProfile(uuid);
        if (profile == null) {
            callable.callback(null);
            return;
        }

        RequestResponse response = RequestHandler.get("profile/%s", uuid.toString());
        if (response.wasSuccessful()) {
            profile.update(response.asObject());
            callable.callback(profile);
        } else {
            LOG.warning(String.format(
                    "Could not update profile of %s (%s): %s (%d)",
                    uuid.toString(),
                    UUIDCache.getName(uuid),
                    response.getErrorMessage(),
                    response.getCode()
            ));
            callable.callback(null);
        }

    }

    public List<Profile> getOnlineProfilesSorted() {
        List<Profile> sortedProfiles = new ArrayList<>();
        Bukkit.getOnlinePlayers().forEach(player -> sortedProfiles.add(getProfile(player)));
        sortedProfiles.sort(Comparator.comparingInt(profile -> profile.getCurrentGrant().getRank().getWeight()));
        Collections.reverse(sortedProfiles);
        return sortedProfiles;
    }

    public void getAlts(Profile profile, TypeCallable<List<Profile>> callable, boolean async) {
        if (async) {
            Tasks.runAsync(() -> getAlts(profile, callable, false));
            return;
        }

        RequestResponse response = RequestHandler.get("profile/%s/alts", profile.getUuid().toString());
        List<Profile> toReturn = new ArrayList<>();
        Set<UUID> uuidSet = new HashSet<>();

        if (response.wasSuccessful()) {
            response.asArray().forEach(element -> {
                JsonObject object = element.getAsJsonObject();
                UUID uuid = UUID.fromString(object.get("uuid").getAsString());
                if (profile.getUuid().equals(uuid) || uuidSet.contains(uuid))
                    return;

                Profile alt = new Profile(invictus, object);
                profiles.putIfAbsent(alt.getUuid(), alt);
                if (!uuidSet.contains(alt.getUuid())) {
                    toReturn.add(alt);
                    uuidSet.add(alt.getUuid());
                }
            });
            profile.setAlts(toReturn);
        } else LOG.warning(String.format(
                "Could not load alts of %s (%s): %s (%d)",
                profile.getUuid().toString(),
                profile.getName(),
                response.getErrorMessage(),
                response.getCode()
        ));

        callable.callback(toReturn);
    }

    public void cacheProfile(Profile profile) {
        profiles.put(profile.getUuid(), profile);
    }

}
