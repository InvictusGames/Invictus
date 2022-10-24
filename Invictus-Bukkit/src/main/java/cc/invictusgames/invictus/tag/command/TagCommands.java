package cc.invictusgames.invictus.tag.command;

import cc.invictusgames.ilib.command.annotation.Command;
import cc.invictusgames.ilib.command.annotation.Param;
import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.invictus.InvictusBukkit;
import cc.invictusgames.invictus.connection.RequestHandler;
import cc.invictusgames.invictus.connection.RequestResponse;
import cc.invictusgames.invictus.tag.Tag;
import cc.invictusgames.invictus.tag.menu.TagMenu;
import cc.invictusgames.invictus.tag.packet.TagReloadPacket;
import lombok.RequiredArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public class TagCommands {

    private final InvictusBukkit invictus;

    @Command(names = {"tag", "tags"},
             async = true,
             description = "Open the tag menu")
    public boolean tagsCommand(CommandSender sender) {
        Player player = (Player) sender;
        new TagMenu().openMenu(player);
        return true;
    }

    @Command(names = "tag create",
             permission = "tag.command.argument.create",
             async = true,
             description = "Create a tag.")
    public boolean tagCreate(CommandSender sender,
                             @Param(name = "name") String name,
                             @Param(name = "displayName") String displayName) {
        if (invictus.getTagService().doesTagExist(name)) {
            sender.sendMessage(CC.format("&cTag &e%s &calready exists.", name));
            return true;
        }

        Tag tag = new Tag(name);
        tag.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));

        RequestResponse response = RequestHandler.post("tag", tag.toJson());
        if (!response.wasSuccessful()) {
            sender.sendMessage(CC.format("&cCould not create tag &e%s&c: %s (%d)",
                    name, response.getErrorMessage(), response.getCode()));
            return false;
        }

        invictus.getTagService().getTagList().add(tag);
        invictus.getRedisService().publish(new TagReloadPacket());
        return true;
    }

    @Command(names = "tag delete",
             permission = "tag.command.argument.delete",
             async = true,
             description = "Delete a tag.")
    public boolean tagDelete(CommandSender sender, @Param(name = "tag") Tag tag) {
        RequestResponse response = RequestHandler.delete("tag/%s", tag.getName());
        if (!response.wasSuccessful()) {
            sender.sendMessage(CC.format("&cCould not delete tag &e%s&c: %s (%d)",
                    tag.getName(), response.getErrorMessage(), response.getCode()));
            return false;
        }

        invictus.getTagService().getTagList().remove(tag);
        sender.sendMessage(CC.GREEN + "Deleted");
        invictus.getRedisService().publish(new TagReloadPacket());
        return true;
    }
}
