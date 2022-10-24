package cc.invictusgames.invictus.base.commands;

import cc.invictusgames.invictus.Invictus;
import cc.invictusgames.invictus.profile.Profile;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 08.10.2020 / 23:17
 * Invictus / cc.invictusgames.invictus.spigot.base.commands
 */

public class ConvertCommands {

    private final Invictus invictus;
    //private final MongoService service;

    List<Profile> tempProfileCache = new ArrayList<>();

    public ConvertCommands(Invictus invictus) {
        this.invictus = invictus;
        /*this.service = new MongoService(invictus.getMainConfig().getMongoConfig(), "qUtilities");
        this.service.connect();*/
    }

    /*@Command(names = {"convertdb uuidcache"}, permission = "op", description = "Convert the UUIDCache", async = true)
    public boolean convertUuidCache(CommandSender sender) {
        sender.sendMessage(CC.BLUE + "Starting converting the UUIDCache...");

        AtomicInteger skipped = new AtomicInteger();
        AtomicInteger converted = new AtomicInteger();
        List<UUID> nullNames = new ArrayList<>();

            for (Document document : service.getCollection("players").find()) {
                UUID uuid = UUID.fromString(document.getString("uuid"));
                String name = document.getString("name");

                if (name == null || name == "null" || name == "N/A") {
                    nullNames.add(uuid);
                    continue;
                }

                if (UUIDCache.getName(uuid) == name && UUIDCache.getUuid(name) == uuid) {
                    sender.sendMessage(CC.format("&c! &9Name of &c%s &9is not set in the database.", uuid.toString()));
                    skipped.getAndIncrement();
                    continue;
                }

                UUIDCache.updateLocally(uuid, name);
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

    @Command(names = {"convertdb ranks"}, permission = "op", description = "Convert all ranks")
    public boolean convertRanks(CommandSender sender) {
        sender.sendMessage(CC.BLUE + "Starting converting ranks...");

        Tasks.runAsync(() -> {

            Map<UUID, Rank> tempCache = new HashMap<>();
            Map<UUID, List<String>> rankInherits = new HashMap<>();

            for (Document document : service.getCollection("ranks").find().sort(Filters.eq("weight", 1))) {
                Rank rank = new Rank(invictus, document.getString("name"));
                rank.setPrefix(CC.translate(document.getString("prefix")));
                rank.setSuffix(CC.translate(document.getString("suffix")));
                rank.setColor(CC.translate(document.getString("color")));
                rank.setChatColor(CC.translate(document.getString("chatColor")));
                rank.setWeight(document.getInteger("weight"));
                rank.setDefaultRank(document.getBoolean("defaultRank"));
                rank.setPermissions(document.getList("permissions", String.class));

                rankInherits.put(rank.getUuid(), document.getList("inhertis", String.class));
                tempCache.put(rank.getUuid(), rank);
                sender.sendMessage(CC.BLUE + "Converted rank " + rank.getDisplayName() + CC.BLUE + ".");
            }

            sender.sendMessage(CC.format("&aFinished converting &e%d &aranks, now adding inherits...", tempCache.size
            ()));

            rankInherits.forEach((uuid, strings) -> {
                if (strings == null) {
                    return;
                }
                strings.forEach(name -> {
                Rank rank = tempCache.get(uuid);
                Rank inherit = tempCache.values()
                        .stream()
                        .filter(value -> value.getName().equalsIgnoreCase(name))
                        .findAny()
                        .orElse(null);

                if (inherit != null) {
                    rank.getInherits().add(rank);
                    sender.sendMessage(CC.format(
                            "&9Added rank %s &9as an inherit for %s&9.",
                            inherit.getDisplayName(),
                            rank.getDisplayName()
                    ));
                }
                });
            });

            sender.sendMessage(CC.GREEN + "Finished adding inherits. Adding ranks to local cache and saving to mongo.
            ..");

            tempCache.values().forEach(rank -> {
                invictus.getRankService().cacheRank(rank);
                rank.save(sender, false, () -> { });
            });

            sender.sendMessage(CC.GREEN + "Finished saving.");
        });
        return true;
    }

    @Command(names = {"convertdb playerdata"}, permission = "op", description = "Convert all player data.", async =
    true)
    public boolean convertPlayerData(CommandSender sender) {
        sender.sendMessage(CC.GREEN + "Starting converting player data...");

            AtomicInteger converted = new AtomicInteger();
            List<Document> nullNames = new ArrayList<>();

            for (Document document : service.getCollection("players").find()) {
                UUID uuid = UUID.fromString(document.getString("uuid"));
                String name = document.getString("name");

                if (name == null || name == "null" || name == "N/A") {
                    nullNames.add(document);
                    sender.sendMessage(CC.format(
                            "&c&l! &9Name belonging to &e%s &9is &c%s&9. Skipping for later lookup.",
                            uuid.toString(),
                            name
                    ));
                    continue;
                }

                tempProfileCache.add(convert(sender, document));
                converted.getAndIncrement();
            }

            sender.sendMessage(CC.format(
                    "&9Converted &a%d &9Profiles, of which &c%d &9had no name set.",
                    converted.get(),
                    nullNames.size()
            ));

            sender.sendMessage(CC.BLUE + "Starting lookup of Null-Name profiles in redis cache...");
            AtomicInteger foundInRedis = new AtomicInteger();
            AtomicInteger foundInMojang = new AtomicInteger();
            AtomicInteger failed = new AtomicInteger();

            for (Document document : nullNames) {
                UUID uuid = UUID.fromString(document.getString("uuid"));
                String name = UUIDCache.getName(uuid);
                if (name != null) {
                    document.put("name", name);
                    foundInRedis.getAndIncrement();
                } else {
                    try {
                        String response = getResponse("https://api.minetools.eu/uuid/" + uuid.toString().replace("-",
                         ""));
                        JsonObject parsed = Statics.JSON_PARSER.parse(response).getAsJsonObject();
                        document.put("name", parsed.get("name").getAsString());
                        foundInMojang.getAndIncrement();
                    } catch (Exception e) {
                        sender.sendMessage(CC.format(
                                "&c&l! &9Failed to resolve name of &c%s&9. Skipping entry."
                        ));
                        failed.getAndIncrement();
                        continue;
                    }
                }

                tempProfileCache.add(convert(sender, document));
            }

            sender.sendMessage(CC.format(
                    "&9Converted &a%d &9Null-Name Profiles from redis, and &a%d &9from Mojang. Failed to convert
                    &c%d&9.",
                    foundInRedis.get(),
                    foundInMojang.get(),
                    failed.get()
            ));
        return true;
    }

    @Command(names = {"convertdb saveplayerdata"}, permission = "op", description = "Save player data", async = true)
    public boolean convertSavePlayerData(CommandSender sender) {
        sender.sendMessage(CC.BLUE + "Starting save...");
        tempProfileCache.forEach(profile -> profile.save(() -> { }, false));
        sender.sendMessage(CC.format("&9Saved &e%d &9Profiles."));
        return true;
    }

    private Profile convert(CommandSender sender, Document document) {
        UUID uuid = UUID.fromString(document.getString("uuid"));
        String name = document.getString("name");

        Profile profile = new Profile(invictus, uuid, name);

        Rank rank = invictus.getRankService().getRank(document.getString("rankName"));
        String grantedBy = document.getString("rankAddedBy");
        if (!grantedBy.equalsIgnoreCase("Console")) {
            if (UUIDCache.getUuid(grantedBy) != null) {
                grantedBy = UUIDCache.getUuid(grantedBy).toString();
            } else {
                grantedBy = "Console";
                sender.sendMessage(CC.format(
                        "&c&l! &9Granter of &e%s &9(&e%s&9) was not found in uuid cache, using &eConsole &9instead.",
                        name,
                        grantedBy
                ));
            }
        }

        Grant grant = new Grant(
                invictus,
                uuid,
                rank == null ? invictus.getRankService().getDefaultRank() : rank,
                grantedBy,
                document.getLong("rankAddedAt"),
                "Converted",
                document.getLong("rankDuration"),
                Collections.singletonList("GLOBAL")
        );
        profile.getGrants().add(grant);

        try {
            List<String> permissions = new ArrayList<>();
            if (document.containsKey("permissions")) {
                permissions = document.getList("permissions", String.class);
            }
            profile.setPermissions(permissions);

            List<String> blockedUsers = new ArrayList<>();
            if (document.containsKey("blockedUsers")) {
                blockedUsers = document.getList("blockedUsers", String.class);
            }

            profile.getOptions().setIgnoring(blockedUsers.stream()
                    .map(UUID::fromString)
                    .collect(Collectors.toList()));
        } catch (NullPointerException e) {
            System.out.println(name + " has some fucked up shit set");
        }
        profile.getOptions().setMessaging(document.getBoolean("receivingMessages"));

        //profile.save(() -> { }, false);
        return profile;
    }

    private static String getResponse(String urlString) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(urlString).openConnection();
            connection.setReadTimeout(5000);
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                StringBuilder response = new StringBuilder();
                new BufferedReader(new InputStreamReader(connection.getInputStream())).lines().forEach
                (response::append);
                return response.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }*/

}
