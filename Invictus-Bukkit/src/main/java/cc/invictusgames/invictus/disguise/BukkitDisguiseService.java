package cc.invictusgames.invictus.disguise;

import cc.invictusgames.ilib.scoreboard.ScoreboardService;
import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.ilib.utils.Timings;
import cc.invictusgames.ilib.utils.callback.TypeCallable;
import cc.invictusgames.invictus.Invictus;
import cc.invictusgames.invictus.connection.RequestHandler;
import cc.invictusgames.invictus.connection.RequestResponse;
import cc.invictusgames.invictus.disguise.event.ProfileDisguiseEvent;
import cc.invictusgames.invictus.disguise.event.ProfileUnDisguiseEvent;
import cc.invictusgames.invictus.profile.Profile;
import cc.invictusgames.invictus.rank.Rank;
import cc.invictusgames.invictus.utils.Tasks;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 17.06.2020 / 23:38
 * Invictus / cc.invictusgames.invictus.spigot
 */

@RequiredArgsConstructor
public class BukkitDisguiseService {

    private static final Logger LOG = Invictus.getInstance().getLogFactory().newLogger(BukkitDisguiseService.class);

    private final Invictus invictus;

    private static final Map<UUID, Property> ORIGINAL_TEXTURES = new HashMap<>();
    private static final JsonParser JSON_PARSER = new JsonParser();
    public static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,16}$");

    @Getter
    private final List<String> namePresets = new ArrayList<>();
    @Getter
    private final List<SkinPreset> skinPresets = new ArrayList<>();

    public void loadPresets() {
        Tasks.runAsync(() -> {
            namePresets.clear();

            LOG.config("Loading name presets...");
            Timings timings = new Timings("disguise-preset-loading").startTimings();

            RequestHandler.get("disguise/presets/names").asArray()
                    .forEach(element -> namePresets.add(element.getAsString()));
            namePresets.sort(String.CASE_INSENSITIVE_ORDER);

            LOG.info(String.format("Loaded %d name presets in %dms",
                    namePresets.size(), timings.stopTimings().calculateDifference()));

            skinPresets.clear();

            LOG.config("Loading skin presets");
            timings.startTimings();

            RequestHandler.get("disguise/presets/skins").asArray().forEach(element -> {
                JsonObject object = element.getAsJsonObject();
                skinPresets.add(new SkinPreset(
                        object.get("name").getAsString(),
                        object.get("hidden").getAsBoolean(),
                        object.get("texture").getAsString(),
                        object.get("signature").getAsString()
                ));
            });
            
            skinPresets.sort((o1, o2) -> String.CASE_INSENSITIVE_ORDER.compare(o1.getName(), o2.getName()));

            LOG.info(String.format("Loaded %d skin presets in %dms",
                    skinPresets.size(), timings.stopTimings().calculateDifference()));
        });
    }

    public SkinPreset parseSkinPreset(String name, String skinName, boolean useSteve) {
        try {
            String response = getResponse("https://api.minetools.eu/uuid/" + skinName);
            JsonObject parsed = JSON_PARSER.parse(response).getAsJsonObject();
            String uuid = parsed.get("id").getAsString();

            response = getResponse("https://api.minetools.eu/profile/" + uuid);
            parsed = JSON_PARSER.parse(response).getAsJsonObject();
            JsonObject properties = parsed.get("raw").getAsJsonObject()
                    .get("properties").getAsJsonArray().get(0).getAsJsonObject();
            return new SkinPreset(
                    skinName,
                    properties.get("value").getAsString(),
                    properties.get("signature").getAsString()
            );
        } catch (Exception e) {
            if (useSteve)
                return new SkinPreset(
                        "Steve",
                        Base64Coder.encodeString("{textures:{SKIN:{url:\"http://assets.mojang" +
                                ".com/SkinTemplates/steve.png\"}}}"),
                        null
                );

            e.printStackTrace();
            return null;
        }
    }

    public void getDisguiseEntries(CommandSender sender, String disguiseName,
                                   TypeCallable<List<DisguiseLogEntry>> callable, boolean async) {
        if (async) {
            Tasks.runAsync(() -> getDisguiseEntries(sender, disguiseName, callable, false));
            return;
        }

        List<DisguiseLogEntry> toReturn = new ArrayList<>();
        RequestResponse response = RequestHandler.get("disguise/%s/namelogs", disguiseName.toLowerCase());
        if (response.wasSuccessful())
            response.asArray().forEach(element -> toReturn.add(new DisguiseLogEntry(element.getAsJsonObject())));
        else sender.sendMessage(CC.format(
                "&cCould not load namelogs of &e%s&c: %s (%d)",
                disguiseName,
                response.getErrorMessage(),
                response.getCode()
        ));
        Collections.reverse(toReturn);
        callable.callback(toReturn);
    }

    public void disguise(Profile profile, Rank rank, String name, SkinPreset entry, boolean log) {
        Player player = profile.player();
        GameProfile gameProfile = ((CraftPlayer) player).getProfile();

        ORIGINAL_TEXTURES.put(player.getUniqueId(),
                gameProfile.getProperties().get("textures").toArray(new Property[0])[0]);

        Property texture = new Property("textures", entry.getTexture(), entry.getSignature());

        gameProfile.getProperties().clear();
        gameProfile.getProperties().put("textures", texture);
        setFieldValue(gameProfile, "name", name);

        profile.setDisguised(true);
        profile.setDisguiseName(name);

        profile.getDisguiseData().setDisguiseName(name);
        profile.getDisguiseData().setDisguiseRank(rank);
        profile.getDisguiseData().setTexture(entry.getTexture());
        profile.getDisguiseData().setSignature(entry.getSignature());
        if (log) {
            profile.getDisguiseData().getLogs().add(new DisguiseLogEntry(
                    profile.getUuid(),
                    name,
                    rank.getName(),
                    System.currentTimeMillis(),
                    -1
            ));
        }

        player.setDisplayName(profile.getDisplayName());

        refreshPlayer(player);

        Bukkit.getPluginManager().callEvent(new ProfileDisguiseEvent(player, name, rank));

        if (log) {
            profile.save(() -> {}, true);
        }
    }

    public void undisguise(Profile profile, boolean log) {
        Player player = profile.player();
        if (player != null) {
            GameProfile gameProfile = ((CraftPlayer) player).getProfile();

            if (ORIGINAL_TEXTURES.containsKey(player.getUniqueId())) {
                Property texture = ORIGINAL_TEXTURES.get(player.getUniqueId());
                gameProfile.getProperties().clear();
                gameProfile.getProperties().put("textures", texture);
            }

            setFieldValue(gameProfile, "name", profile.getName());
        }

        profile.setDisguised(false);
        profile.setDisguiseName("N/A");

        profile.getDisguiseData().setDisguiseName("N/A");
        profile.getDisguiseData().setDisguiseRank(invictus.getRankService().getDefaultRank());
        profile.getDisguiseData().setTexture(null);
        profile.getDisguiseData().setSignature(null);
        if (log && !profile.getDisguiseData().getLogs().isEmpty()) {
            DisguiseLogEntry entry =
                    profile.getDisguiseData().getLogs().get(profile.getDisguiseData().getLogs().size() - 1);
            entry.setRemovedAt(System.currentTimeMillis());
        }

        if (player != null) {
            Bukkit.getPluginManager().callEvent(new ProfileUnDisguiseEvent(player));
            player.setDisplayName(profile.getDisplayName());
            refreshPlayer(player);
        }

        if (log)
            profile.save(() -> {}, true);
    }

    private void refreshPlayer(Player player) {
        Tasks.run(() -> {
            List<Player> seeing = new ArrayList<>();
            for (Player current : Bukkit.getOnlinePlayers()) {
                if (current.canSee(player)) {
                    seeing.add(current);
                    current.hidePlayer(player);
                }
            }
            seeing.forEach(current -> current.showPlayer(player));
            ScoreboardService.forceUpdateNameTags();
        });
    }


    private static void setFieldValue(Object instance, String fieldName, Object value) {
        try {
            Field field = instance.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(instance, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private static String getResponse(String urlString) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(urlString).openConnection();
            connection.setReadTimeout(5000);
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                StringBuilder response = new StringBuilder();
                new BufferedReader(new InputStreamReader(connection.getInputStream())).lines().forEach(response::append);
                return response.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    @RequiredArgsConstructor
    @AllArgsConstructor
    @Getter
    public static class SkinPreset {

        private final String name;
        private boolean hidden = false;
        private final String texture;
        private final String signature;

    }

}
