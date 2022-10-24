package cc.invictusgames.invictus.base.redisconvertion;

import cc.invictusgames.ilib.ILib;
import cc.invictusgames.ilib.command.annotation.Command;
import cc.invictusgames.ilib.configuration.defaults.MongoConfig;
import cc.invictusgames.ilib.mongo.MongoService;
import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.ilib.uuid.UUIDCache;
import cc.invictusgames.invictus.InvictusBukkit;
import cc.invictusgames.invictus.InvictusBukkitPlugin;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 15.11.2020 / 00:03
 * Invictus / cc.invictusgames.invictus.spigot.base.redisconvertion
 */

@RequiredArgsConstructor
public class RedisConvertCommands {

    private final InvictusBukkit invictus;

    @Command(names = {"redisconvert savetotp"}, permission = "op", description = "Save TOTP-Data", async = true)
    public boolean saveTotp(CommandSender sender) {
        sender.sendMessage(CC.BLUE + "Starting redis pull...");
        File file = new File(InvictusBukkitPlugin.getInstance().getDataFolder(), "redisData.json");
        RedisDataConfig config = invictus.getConfigurationService().loadConfiguration(RedisDataConfig.class, file);
        invictus.getRedisService().executeBackendCommand(redis -> {
            redis.keys("totp:*").forEach(key -> config.getTotpData().putIfAbsent(key, redis.get(key)));
            sender.sendMessage(CC.GREEN + "Finished redis pull.");
            return null;
        });

        sender.sendMessage(CC.BLUE + "Starting save...");
        try {
            invictus.getConfigurationService().saveConfiguration(config, file);
            sender.sendMessage(CC.GREEN + "Save successful.");
        } catch (IOException e) {
            e.printStackTrace();
            sender.sendMessage(CC.RED + "Save failed.");
        }
        return true;
    }

    @Command(names = {"redisconvert loadtotp"}, permission = "op", description = "Load TOTP-Data", async = true)
    public boolean loadTotp(CommandSender sender) {
        sender.sendMessage(CC.BLUE + "Starting redis update...");
        File file = new File(InvictusBukkitPlugin.getInstance().getDataFolder(), "redisData.json");
        RedisDataConfig config = invictus.getConfigurationService().loadConfiguration(RedisDataConfig.class, file);
        invictus.getRedisService().executeBackendCommand(redis -> {
            config.getTotpData().forEach(redis::set);
            sender.sendMessage(CC.GREEN + "Finished redis update.");
            return null;
        });
        return true;
    }

    @Command(names = {"redisconvert saveprime"}, permission = "op", description = "Save Prime-Data", async = true)
    public boolean savePrime(CommandSender sender) {
        sender.sendMessage(CC.BLUE + "Starting redis pull...");
        File file = new File(InvictusBukkitPlugin.getInstance().getDataFolder(), "redisData.json");
        RedisDataConfig config = invictus.getConfigurationService().loadConfiguration(RedisDataConfig.class, file);
        invictus.getRedisService().executeBackendCommand(redis -> {
            redis.keys("prime:*").forEach(key -> {
                config.getPrimeData().putIfAbsent(key, new HashMap<>());
                config.getPrimeData().get(key).putAll(redis.hgetAll(key));
            });
            sender.sendMessage(CC.GREEN + "Finished redis pull.");
            return null;
        });

        sender.sendMessage(CC.BLUE + "Starting save...");
        try {
            invictus.getConfigurationService().saveConfiguration(config, file);
            sender.sendMessage(CC.GREEN + "Save successful.");
        } catch (IOException e) {
            e.printStackTrace();
            sender.sendMessage(CC.RED + "Save failed.");
        }
        return true;
    }

    @Command(names = {"redisconvert loadprime"}, permission = "op", description = "Load Prime-Data", async = true)
    public boolean loadPrime(CommandSender sender) {
        sender.sendMessage(CC.BLUE + "Starting redis update.");
        File file = new File(InvictusBukkitPlugin.getInstance().getDataFolder(), "redisData.json");
        RedisDataConfig config = invictus.getConfigurationService().loadConfiguration(RedisDataConfig.class, file);
        invictus.getRedisService().executeBackendCommand(redis -> {
            config.getPrimeData().forEach((key, values) -> values.forEach((field, value) -> redis.hset(key, field,
                    value)));
            sender.sendMessage(CC.GREEN + "Finished redis update.");
            return null;
        });
        return true;
    }

    @Command(names = {"redisconvert uuidcache"}, permission = "op", description = "Parse UUIDCache from current profiles", async = true)
    public boolean uuidCache(CommandSender sender) {
        sender.sendMessage(CC.BLUE + "Starting converting the UUIDCache...");

        AtomicInteger skipped = new AtomicInteger();
        AtomicInteger converted = new AtomicInteger();
        List<UUID> nullNames = new ArrayList<>();
        MongoService mongoService = new MongoService(new MongoConfig(), "invictus");
        mongoService.connect();

        for (Document document : mongoService.getCollection("profiles").find()) {
            UUID uuid = UUID.fromString(document.getString("uuid"));
            String name = document.getString("name");

            if (name == null || name.equalsIgnoreCase("null") || name.equalsIgnoreCase("N/A")) {
                sender.sendMessage(CC.format("&c! &9Name of &c%s &9is &e%s &9in the database.", uuid.toString(), name));
                nullNames.add(uuid);
                continue;
            }

            if ((UUIDCache.getName(uuid) != null && UUIDCache.getName(uuid).equals(name))
                    && (UUIDCache.getUuid(name) != null && UUIDCache.getUuid(name) == uuid)) {
                sender.sendMessage(CC.format("&c! &9Name of &c%s &9is already set in the database.", uuid.toString()));
                skipped.getAndIncrement();
                continue;
            }

            UUIDCache.updateLocally(uuid, null, name);
            converted.getAndIncrement();
        }

        sender.sendMessage(CC.format(
                "&9Converted &a%d &9uuids. Existing in cache &e%d&9. Null-Names &c%d&9.",
                converted.get(),
                skipped.get(),
                nullNames.size()
        ));

        sender.sendMessage(CC.GREEN + "Saving to redis...");
        ILib.getInstance().getUuidCache().saveAll();
        sender.sendMessage(CC.GREEN + "Finished saving.");
        return true;
    }

}
