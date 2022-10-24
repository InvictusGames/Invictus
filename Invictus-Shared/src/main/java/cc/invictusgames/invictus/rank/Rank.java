package cc.invictusgames.invictus.rank;

import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.ilib.utils.callback.Callable;
import cc.invictusgames.ilib.utils.json.JsonBuilder;
import cc.invictusgames.invictus.Invictus;
import cc.invictusgames.invictus.connection.RequestHandler;
import cc.invictusgames.invictus.connection.RequestResponse;
import cc.invictusgames.invictus.rank.packets.RankUpdatePacket;
import cc.invictusgames.invictus.utils.Tasks;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.Data;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.command.CommandSender;

import java.util.*;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 14.02.2020 / 12:06
 * Invictus / cc.invictusgames.invictus.spigot.rank
 */

@Data
public class Rank {

    public static final Comparator<Rank> COMPARATOR =
            Collections.reverseOrder(Comparator.comparingInt(Rank::getWeight));

    private final Invictus invictus;
    private final UUID uuid;
    private String name = "Unknown";
    private String prefix = CC.WHITE;
    private String suffix = CC.WHITE;
    private String color = CC.WHITE;
    private String chatColor = CC.WHITE;
    private String discordId;
    private String staffDiscordId;

    private int weight = 0;
    private int queuePriority = 0;

    private boolean defaultRank = false;
    private boolean disguisable = false;

    private List<String> permissions = new ArrayList<>();
    private List<String> localPermissions = new ArrayList<>();
    private List<Rank> inherits = new ArrayList<>();

    public Rank(Invictus invictus, JsonObject object) {
        this.invictus = invictus;
        this.uuid = UUID.fromString(object.get("uuid").getAsString());
        this.name = object.get("name").getAsString();
        this.prefix = object.get("prefix").getAsString();
        this.suffix = object.get("suffix").getAsString();
        this.color = object.get("color").getAsString();
        this.chatColor = object.get("chatColor").getAsString();
        this.weight = object.get("weight").getAsInt();
        this.queuePriority = object.get("queuePriority").getAsInt();
        this.defaultRank = object.get("defaultRank").getAsBoolean();
        this.disguisable = object.get("disguisable").getAsBoolean();
        object.get("permissions").getAsJsonArray().forEach(element -> permissions.add(element.getAsString()));

        if (object.has("discordId"))
            this.discordId = object.get("discordId").getAsString();

        if (object.has("staffDiscordId"))
            this.staffDiscordId = object.get("staffDiscordId").getAsString();

        this.localPermissions = invictus.getLocalPermissions(this);
    }

    public Rank(Invictus invictus, String name) {
        this.invictus = invictus;
        this.uuid = UUID.randomUUID();
        this.name = name;
    }

    public void save(CommandSender sender, Callable callable) {
        this.save(sender, true, callable);
    }

    public void save(CommandSender sender, boolean update, Callable callable) {
        Tasks.runAsync(() -> {
            invictus.saveLocalPermissions(this);

            JsonObject object = toJson();
            RequestResponse response = RequestHandler.put("rank", object);

            if (!response.wasSuccessful())
                sender.sendMessage(CC.format("&cCould not save rank &e%s&c: %s (%d)",
                        name, response.getErrorMessage(), response.getCode()));
            else if (update)
                invictus.getRedisService().publish(new RankUpdatePacket(uuid));
            callable.callback();
        });
    }

    public JsonObject toJson() {
        JsonBuilder builder = new JsonBuilder();
        builder.add("uuid", this.uuid);
        builder.add("name", this.name);
        builder.add("prefix", this.prefix);
        builder.add("suffix", this.suffix);
        builder.add("color", this.color);
        builder.add("chatColor", this.chatColor);
        builder.add("weight", this.weight);
        builder.add("queuePriority", queuePriority);
        builder.add("defaultRank", this.defaultRank);
        builder.add("disguisable", this.disguisable);

        JsonArray array = new JsonArray();
        permissions.forEach(array::add);
        builder.add("permissions", array);

        builder.add("discordId", this.discordId);
        builder.add("staffDiscordId", this.staffDiscordId);

        final JsonArray inheritArray = new JsonArray();
        inherits.forEach(inherit -> inheritArray.add(inherit.getUuid().toString()));
        builder.add("inherits", inheritArray);
        return builder.build();
    }

    public String getDisplayName() {
        return this.color + this.name.replace('-', ' ');
    }

    public List<String> getInheritPermissions() {
        List<String> inheritPermissions = new ArrayList<>();
        for (Rank inherit : inherits) {
            inheritPermissions.addAll(inherit.getAllPermissions());
        }
        return inheritPermissions;
    }

    public List<String> getAllPermissions() {
        List<String> allPermissions = new ArrayList<>();
        allPermissions.addAll(this.getInheritPermissions());
        allPermissions.addAll(this.permissions);
        allPermissions.addAll(this.localPermissions);
        return allPermissions;
    }

    public DyeColor getDyeColor() {
        String input = this.color.replaceAll("§", "")
                .replaceAll("l", "")
                .replaceAll("o", "")
                .replaceAll("n", "")
                .replaceAll("m", "")
                .replaceAll("k", "");

        switch (input) {
            case "a":
                return DyeColor.LIME;
            case "b":
                return DyeColor.LIGHT_BLUE;
            case "c":
            case "4":
                return DyeColor.RED;
            case "d":
                return DyeColor.MAGENTA;
            case "e":
                return DyeColor.YELLOW;
            case "f":
                return DyeColor.WHITE;
            case "1":
            case "9":
                return DyeColor.BLUE;
            case "2":
                return DyeColor.GREEN;
            case "3":
                return DyeColor.CYAN;
            case "5":
                return DyeColor.PURPLE;
            case "6":
                return DyeColor.ORANGE;
            case "7":
                return DyeColor.SILVER;
            case "8":
                return DyeColor.GRAY;
            case "0":
            default:
                return DyeColor.BLACK;
        }
    }

    public Color getBukkitColor() {
        String input = this.color.replaceAll("§", "")
                .replaceAll("l", "")
                .replaceAll("o", "")
                .replaceAll("n", "")
                .replaceAll("m", "")
                .replaceAll("k", "");

        switch (input) {
            case "a":
                return Color.fromRGB(85, 255, 85);
            case "b":
                return Color.fromRGB(85, 255, 255);
            case "c":
                return Color.fromRGB(255, 85, 85);
            case "d":
                return Color.fromRGB(255, 85, 255);
            case "e":
                return Color.fromRGB(255, 255, 85);
            case "f":
                return Color.fromRGB(255, 255, 255);
            case "0":
                return Color.fromRGB(0, 0, 0);
            case "1":
                return Color.fromRGB(0, 0, 170);
            case "2":
                return Color.fromRGB(0, 170, 0);
            case "3":
                return Color.fromRGB(0, 170, 170);
            case "4":
                return Color.fromRGB(170, 0, 0);
            case "5":
                return Color.fromRGB(170, 0, 170);
            case "6":
                return Color.fromRGB(255, 170, 0);
            case "7":
                return Color.fromRGB(170, 170, 170);
            case "8":
                return Color.fromRGB(85, 85, 85);
            case "9":
                return Color.fromRGB(85, 85, 255);
        }

        return Color.fromRGB(0, 0, 0);
    }

}
