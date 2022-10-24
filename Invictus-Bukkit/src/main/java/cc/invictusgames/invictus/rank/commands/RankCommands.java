package cc.invictusgames.invictus.rank.commands;

import cc.invictusgames.ilib.command.annotation.Command;
import cc.invictusgames.ilib.command.annotation.Flag;
import cc.invictusgames.ilib.command.annotation.Param;
import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.ilib.utils.ChatMessage;
import cc.invictusgames.ilib.utils.PasteUtils;
import cc.invictusgames.invictus.Invictus;
import cc.invictusgames.invictus.InvictusBukkit;
import cc.invictusgames.invictus.connection.RequestHandler;
import cc.invictusgames.invictus.connection.RequestResponse;
import cc.invictusgames.invictus.profile.Profile;
import cc.invictusgames.invictus.rank.Rank;
import cc.invictusgames.invictus.rank.menu.RankEditOverviewMenu;
import cc.invictusgames.invictus.rank.menu.RankEditingMenu;
import cc.invictusgames.invictus.rank.packets.RankCreatePacket;
import cc.invictusgames.invictus.rank.packets.RankDeletePacket;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 08.06.2020 / 21:45
 * Invictus / cc.invictusgames.invictus.spigot.rank.commands
 */

@RequiredArgsConstructor
public class RankCommands {

    public static final RankCommands INSTANCE = new RankCommands(InvictusBukkit.getBukkitInstance());

    private final InvictusBukkit invictus;

    @Command(names = {"rank list"},
             permission = "rank.command.argument.list",
             description = "List all available ranks")
    public boolean rankList(CommandSender sender,
                            @Flag(names = {"-priority"}, description = "Sort the ranks by queue priority") boolean priority) {
        sender.sendMessage(CC.SMALL_CHAT_BAR);
        sender.sendMessage(CC.RED + CC.BOLD + "Ranks");
        List<Rank> ranks = priority ? invictus.getRankService().getRanksSortedPriority()
                : invictus.getRankService().getRanksSorted();
        for (Rank rank : ranks) {
            List<String> hover = Arrays.asList(
                    CC.format(" &eName: &f%s", rank.getName()),
                    CC.format(" &eColor: %sExample", rank.getColor()),
                    CC.format(" &eChat Color: %sExample", rank.getChatColor()),
                    CC.format(" &ePrefix: %sExample", rank.getPrefix()),
                    CC.format(" &eSuffix: &fExample%s", rank.getSuffix()),
                    " ",
                    CC.translate("&7&oClick to show more info"));

            new ChatMessage(
                    CC.format(
                            "&f - %s &f(%s: %d)%s",
                            rank.getDisplayName(),
                            priority ? "Priority" : "Weight",
                            priority ? rank.getQueuePriority() : rank.getWeight(),
                            rank.isDefaultRank() ? " (Default)" : ""))
                    .hoverText(String.join("\n", hover))
                    .runCommand("/rank info " + rank.getName())
                    .send(sender);
        }
        sender.sendMessage(CC.SMALL_CHAT_BAR);
        return true;
    }

    @Command(names = {"rank info"},
             permission = "rank.command.argument.info",
             description = "View information about a specific rank")
    public boolean rankInfo(CommandSender sender, @Param(name = "rank") Rank rank) {
        sender.sendMessage(CC.SMALL_CHAT_BAR);
        sender.sendMessage(CC.RED + CC.BOLD + "Rank Information");
        sender.sendMessage(CC.format(" &eName: &f%s", rank.getName()));
        sender.sendMessage(CC.format(" &eColor: %sExample", rank.getColor()));
        sender.sendMessage(CC.format(" &eChat Color: %sExample", rank.getChatColor()));
        sender.sendMessage(CC.format(" &ePrefix: %sExample", rank.getPrefix()));
        sender.sendMessage(CC.format(" &ePrefix: &fExample%s", rank.getSuffix()));
        sender.sendMessage(CC.format(" &eWeight: &f%d", rank.getWeight()));
        sender.sendMessage(CC.format(" &eQueue Priority: &f%d", rank.getQueuePriority()));
        sender.sendMessage(CC.format(" &eDefault: %s",
                CC.colorBoolean(rank.isDefaultRank(), "true", "false")));
        sender.sendMessage(CC.format(" &eDiscord ID: &f%s", rank.getDiscordId()));
        sender.sendMessage(CC.format(" &eStaff-Discord ID: &f%s", rank.getStaffDiscordId()));
        sender.sendMessage(CC.format(" &eInherits: &f(%d)", rank.getInherits().size()));
        rank.getInherits().forEach(inherit -> sender.sendMessage(CC.format("&f - %s", inherit.getDisplayName())));
        sender.sendMessage(CC.format(" &ePermissions: &f(%d) %s",
                rank.getPermissions().size(), String.join(", ", rank.getPermissions())));
        sender.sendMessage(CC.format(" &eLocal Permissions: &f(%d) %s",
                rank.getLocalPermissions().size(), String.join(", ", rank.getLocalPermissions())));
        sender.sendMessage(CC.format(" &eInherited Permissions: &f(%d) %s",
                rank.getInheritPermissions().size(), String.join(", ", rank.getInheritPermissions())));
        sender.sendMessage(CC.SMALL_CHAT_BAR);
        return true;
    }

    @Command(names = {"rank edit"},
             permission = "rank.command.argument.edit",
             playerOnly = true, description = "Edit an rank")
    public boolean rankEdit(Player sender, @Param(name = "rank", defaultValue = "@menu") String rankName) {
        Profile profile = invictus.getProfileService().getProfile(sender);

        if (rankName.equals("@menu")) {
            new RankEditOverviewMenu(invictus, profile).openMenu(sender);
            return true;
        }

        Rank rank = invictus.getRankService().getRank(rankName);

        if (rank == null) {
            sender.sendMessage(CC.format("&cRank &e%s &cnot found.", rankName));
            return false;
        }

        new RankEditingMenu(invictus, profile, rank).openMenu(sender);
        return true;
    }

    @Command(names = {"rank create"},
             permission = "rank.command.argument.create",
             description = "Create a new rank",
             async = true)
    public boolean rankCreate(CommandSender sender, @Param(name = "rank") String rankName) {
        return createRank(sender, rankName) != null;
    }

    public static Rank createRank(CommandSender sender, String rankName) {
        Invictus invictus = Invictus.getInstance();
        Rank rank = invictus.getRankService().getRank(rankName);
        if (rank != null) {
            sender.sendMessage(CC.format("&cRank &e%s &calready exists", rank.getName()));
            return null;
        }

        rank = new Rank(invictus, rankName);

        RequestResponse response = RequestHandler.post("rank", rank.toJson());
        if (!response.wasSuccessful()) {
            sender.sendMessage(CC.format("&cCould not create rank &e%s&c: %s (%d)",
                    rankName, response.getErrorMessage(), response.getCode()));
            return null;
        }

        invictus.getRedisService().publish(new RankCreatePacket(rank.getUuid()));
        sender.sendMessage(CC.format("&eYou created the rank %s&e.", rank.getDisplayName()));
        return rank;
    }

    @Command(names = {"rank delete"},
             permission = "rank.command.argument.delete",
             description = "Delete an existing rank")
    public boolean rankDelete(CommandSender sender, @Param(name = "rank") Rank rank) {
        sender.sendMessage(CC.format("&eYou deleted the rank %s&e.", rank.getDisplayName()));
        invictus.getRedisService().publish(new RankDeletePacket(rank.getUuid()));
        return true;
    }

    @Command(names = {"rank rename"},
             permission = "rank.command.argument.rename",
             description = "Rename an existing rank")
    public boolean rankRename(CommandSender sender,
                              @Param(name = "rank") Rank rank,
                              @Param(name = "newName") String string) {
        sender.sendMessage(CC.format("&eYou renamed %s &eto %s&c.",
                rank.getDisplayName(), rank.getColor() + string));
        rank.setName(string);
        rank.save(sender, () -> {
        });
        return true;
    }

    @Command(names = {"rank setdefault"},
             permission = "rank.command.argument.setdefault",
             description = "Set an rank as default rank")
    public boolean rankSetDefault(CommandSender sender, @Param(name = "rank") Rank rank) {
        invictus.getRankService().getRanks().forEach(current -> {
            if (current.isDefaultRank()) {
                current.setDefaultRank(false);
                current.save(sender, () -> {
                });
            }
        });

        rank.setDefaultRank(true);
        rank.save(sender, () -> {
        });

        sender.sendMessage(CC.format("&eYou set the rank %s &eas default rank.", rank.getDisplayName()));
        return true;
    }

    @Command(names = {"rank clearperms", "rank clearpermissions"},
             permission = "console",
             description = "Clear the permissions of an rank")
    public boolean rankClearPerms(CommandSender sender,
                                  @Param(name = "rank") Rank rank,
                                  @Param(name = "prefix", defaultValue = "@none") String prefix) {
        rank.getPermissions().removeIf(permission -> prefix.equals("@none") || !permission.startsWith(prefix));
        rank.getLocalPermissions().removeIf(permission -> prefix.equals("@none") || !permission.startsWith(prefix));
        rank.save(sender, () -> {
        });
        sender.sendMessage(CC.format("&eCleared all permissions of %s&e.", rank.getDisplayName()));
        return true;
    }

    @Command(names = {"rank exportperms", "rank exportpermissions"},
             permission = "rank.command.argument.exportperms",
             description = "Export the permissions of an rank",
             async = true)
    public boolean rankExportPerms(CommandSender sender,
                                   @Param(name = "rank") Rank rank,
                                   @Param(name = "prefix", defaultValue = "@none") String prefix) {
        sender.sendMessage(CC.format("&eUploading permissions of %s&e.", rank.getDisplayName()));

        StringBuilder builder = new StringBuilder();
        builder.append("Permissions of ").append(rank.getName()).append(":").append("\n\n");
        builder.append("Global: ").append("\n");

        for (String permission : rank.getPermissions()) {
            if (prefix.equals("@none") || permission.startsWith(prefix))
                builder.append(permission).append("\n");
        }

        builder.append("\nLocal on ").append(invictus.getServerName()).append(":");

        for (String permission : rank.getLocalPermissions()) {
            if (prefix.equals("@none") || permission.startsWith(prefix))
                builder.append(permission).append("\n");
        }

        String url = PasteUtils.paste(builder.toString(), false);
        if (url == null) {
            sender.sendMessage(CC.format("&cFailed to upload permissions of &e%s&c.", rank.getName()));
            return false;
        }

        sender.sendMessage(CC.format("&ePermissions: &c%s", url));
        return true;
    }

    @Command(names = {"rank addperm", "rank addpermission"},
             permission = "rank.command.argument.addperm",
             description = "Add a permission to a rank")
    public boolean rankAddPerm(CommandSender sender,
                               @Param(name = "rank") Rank rank,
                               @Param(name = "permission") String permission) {
        if (rank.getPermissions().contains(permission.toLowerCase())) {
            sender.sendMessage(CC.format("&cRank &e%s &calready has permission &e%s&c.",
                    rank.getName(), permission));
            return false;
        }

        rank.getPermissions().add(permission.toLowerCase());
        rank.save(sender, () -> {
        });
        sender.sendMessage(CC.format("&eYou added permission &c%s &eto rank %s&e.",
                permission, rank.getDisplayName()));
        return true;
    }

    @Command(names = {"rank delperm", "rank delpermission", "rank removeperm", "rank removepermission"},
             permission = "rank.command.argument.delperm",
             description = "Remove a permission from an rank")
    public boolean rankDelPerm(CommandSender sender,
                               @Param(name = "rank") Rank rank,
                               @Param(name = "permission") String permission) {
        if (!rank.getPermissions().contains(permission.toLowerCase())) {
            sender.sendMessage(CC.format("&cRank &e%s &cdoesn't have permission &e%s&c.",
                    rank.getName(), permission));
            return false;
        }

        rank.getPermissions().remove(permission.toLowerCase());
        rank.save(sender, () -> {
        });
        sender.sendMessage(CC.format("&eYou removed permission &c%s &efrom rank %s&e.",
                permission, rank.getDisplayName()));
        return true;
    }

    @Command(names = {"rank addpermlocal", "rank addpermissionlocal"},
             permission = "rank.command.argument.addpermlocal",
             description = "Add a local permission to a rank")
    public boolean rankAddPermLocal(CommandSender sender,
                                    @Param(name = "rank") Rank rank,
                                    @Param(name = "permission") String permission) {
        if (rank.getLocalPermissions().contains(permission.toLowerCase())) {
            sender.sendMessage(CC.format("&cRank &e%s &calready has permission &e%s&c.",
                    rank.getName(), permission));
            return false;
        }

        rank.getLocalPermissions().add(permission.toLowerCase());
        rank.save(sender, () -> {
        });
        sender.sendMessage(CC.format("&eYou added permission &c%s &eto rank %s&e.",
                permission, rank.getDisplayName()));
        return true;
    }

    @Command(names = {"rank delpermlocal", "rank delpermissionlocal", "rank removepermlocal",
            "rank removepermissionlocal"},
             permission = "rank.command.argument.delpermlocal",
             description = "Remove a local permission from an rank")
    public boolean rankDelPermLocal(CommandSender sender,
                                    @Param(name = "rank") Rank rank,
                                    @Param(name = "permission") String permission) {
        if (!rank.getLocalPermissions().contains(permission.toLowerCase())) {
            sender.sendMessage(CC.format("&cRank &e%s &cdoesn't have permission &e%s&c.",
                    rank.getName(), permission));
            return false;
        }

        rank.getLocalPermissions().remove(permission.toLowerCase());
        rank.save(sender, () -> {
        });
        sender.sendMessage(CC.format("&eYou removed permission &c%s &efrom rank %s&e.",
                permission, rank.getDisplayName()));
        return true;
    }

    @Command(names = {"rank inherit"},
             permission = "rank.command.argument.inherit",
             description = "Un-/Inherit a parent to/from a child")
    public boolean rankInherit(CommandSender sender,
                               @Param(name = "parent") Rank parent,
                               @Param(name = "child") Rank child) {
        if (parent.getInherits().contains(child)) {
            parent.getInherits().remove(child);
            sender.sendMessage(CC.format("&eYou made %s &eno longer inherit %s&e.",
                    parent.getDisplayName(), child.getDisplayName()));
        } else {
            parent.getInherits().add(child);
            sender.sendMessage(CC.format("&eYou made %s &einherit %s&e.",
                    parent.getDisplayName(), child.getDisplayName()));
        }
        parent.save(sender, () -> {
        });
        return true;
    }

    @Command(names = {"rank setweight", "rank weight"},
             permission = "rank.command.argument.setweight",
             description = "Set the weight of an rank")
    public boolean rankSetWeight(CommandSender sender,
                                 @Param(name = "rank") Rank rank,
                                 @Param(name = "weight") int weight) {
        rank.setWeight(weight);
        rank.save(sender, () -> {
        });
        sender.sendMessage(CC.format("&eYou set the weight of %s &eto &c%d&e.",
                rank.getDisplayName(), rank.getWeight()));
        return true;
    }

    @Command(names = {"rank setqueuepriority", "rank queuepriority"},
             permission = "rank.command.argument.setqueuepriority",
             description = "Set the queue priority of an rank")
    public boolean rankSetQueuePriority(CommandSender sender,
                                        @Param(name = "rank") Rank rank,
                                        @Param(name = "priority") int queuePriority) {
        rank.setQueuePriority(queuePriority);
        rank.save(sender, () -> {
        });
        sender.sendMessage(CC.format("&eYou set the queue priority of %s &eto &c%d&e.",
                rank.getDisplayName(), rank.getQueuePriority()));
        return true;
    }

    @Command(names = {"rank setdisguisable", "rank disguisable"},
             permission = "rank.command.argument.setdisguisable",
             description = "Set the disguisable status of a rank")
    public boolean rankSetDisguisable(CommandSender sender,
                                      @Param(name = "rank") Rank rank,
                                      @Param(name = "disguisable") boolean disguisable) {
        rank.setDisguisable(disguisable);
        rank.save(sender, () -> {
        });
        sender.sendMessage(CC.format("&eYou set the disguisable status of %s &eto %s&e.",
                rank.getDisplayName(), CC.colorBoolean(rank.isDisguisable())));
        return true;
    }

    @Command(names = {"rank setchatcolor", "rank chatcolor"},
             permission = "rank.command.argument.setchatcolor",
             description = "Set the chat color of an rank")
    public boolean rankSetChatColor(CommandSender sender,
                                    @Param(name = "rank") Rank rank,
                                    @Param(name = "color") String color) {
        rank.setChatColor(CC.translate(color));
        rank.save(sender, () -> {
        });
        sender.sendMessage(CC.format("&eYou set the chat color of %s &eto %sExample&e.",
                rank.getDisplayName(), rank.getChatColor()));
        return true;
    }

    @Command(names = {"rank setcolor", "rank color"},
             permission = "rank.command.argument.setcolor",
             description = "Set the color of an rank")
    public boolean rankSetColor(CommandSender sender,
                                @Param(name = "rank") Rank rank,
                                @Param(name = "color") String color) {
        rank.setColor(CC.translate(color));
        rank.save(sender, () -> {
        });
        sender.sendMessage(CC.format("&eYou set the color of %s &eto %sExample&e.",
                rank.getDisplayName(), rank.getColor()));
        return true;
    }

    @Command(names = {"rank setprefix", "rank prefix"},
             permission = "rank.command.argument.setprefix",
             description = "Set the prefix of an rank")
    public boolean rankSetPrefix(CommandSender sender,
                                 @Param(name = "rank") Rank rank,
                                 @Param(name = "prefix", wildcard = true) String prefix) {
        rank.setPrefix(CC.translate(prefix));
        rank.save(sender, () -> {
        });
        sender.sendMessage(CC.format("&eYou set the prefix of %s &eto %sExample&e.",
                rank.getDisplayName(), rank.getPrefix()));
        return true;
    }

    @Command(names = {"rank setsuffix", "rank suffix"},
             permission = "rank.command.argument.setsuffix",
             description = "Set the suffix of an rank")
    public boolean rankSetSuffix(CommandSender sender,
                                 @Param(name = "rank") Rank rank,
                                 @Param(name = "prefix", wildcard = true) String suffix) {
        rank.setSuffix(CC.translate(suffix));
        rank.save(sender, () -> {
        });
        sender.sendMessage(CC.format("&eYou set the suffix of %s &eto %sExample&e.",
                rank.getDisplayName(), rank.getSuffix()));
        return true;
    }

    @Command(names = {"rank setdiscordid", "rank discordid"},
             permission = "rank.command.argument.setdiscordid",
             description = "Set the discord id of an rank")
    public boolean rankSetDiscordId(CommandSender sender,
                                    @Param(name = "rank") Rank rank,
                                    @Param(name = "id") String id) {
        if (id.equalsIgnoreCase("null"))
            rank.setDiscordId(null);
        else
            rank.setDiscordId(id);

        rank.save(sender, () -> {
        });
        sender.sendMessage(CC.format("&eYou set the discord id of %s &eto &c%s&e.",
                rank.getDisplayName(), rank.getDiscordId()));
        return true;
    }

    @Command(names = {"rank setstaffdiscordid", "rank staffdiscordid"},
             permission = "rank.command.argument.setstaffdiscordid",
             description = "Set the staff discord id of an rank")
    public boolean rankSetStaffDiscordId(CommandSender sender,
                                         @Param(name = "rank") Rank rank,
                                         @Param(name = "id") String id) {
        if (id.equalsIgnoreCase("null"))
            rank.setStaffDiscordId(null);
        else
            rank.setStaffDiscordId(id);

        rank.save(sender, () -> {
        });
        sender.sendMessage(CC.format("&eYou set the staff discord id of %s &eto &c%s&e.",
                rank.getDisplayName(), rank.getStaffDiscordId()));
        return true;
    }

    /*@Command(names = {"rank merge"}, permission = "console", description = "Merge a rank into another", async = true)
    public boolean rankMerge(CommandSender sender, @Param(name = "rank") Rank rank, @Param(name = "toRank") Rank
    toRank) {
        int changed = 0;
        for (Document document : invictus.getMongoService().getProfiles().find(Filters.eq("grants.rank", rank.getUuid
        ().toString()))) {
            Profile profile = new Profile(invictus, document);
            profile.getGrants().stream()
                    .filter(grant -> grant.getRank().getUuid().equals(rank.getUuid()))
                    .forEach(grant -> grant.setRank(toRank));
            profile.save(() -> { }, false);
            changed++;
        }

        sender.sendMessage(CC.format("&aDone. (&e%d&a)", changed));
        return true;
    }*/

}
