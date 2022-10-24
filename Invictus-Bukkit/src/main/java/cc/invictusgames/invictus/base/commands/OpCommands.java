package cc.invictusgames.invictus.base.commands;

import cc.invictusgames.ilib.command.annotation.Command;
import cc.invictusgames.ilib.command.annotation.Param;
import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.ilib.uuid.UUIDCache;
import cc.invictusgames.invictus.connection.RequestHandler;
import cc.invictusgames.invictus.connection.RequestResponse;
import com.google.gson.JsonPrimitive;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class OpCommands {

    @Command(names = {"op"},
             permission = "invictus.command.op",
             description = "Op someone",
             async = true)
    public boolean op(CommandSender sender, @Param(name = "target") UUID target) {
        RequestResponse response = RequestHandler.get("oplist");
        if (!response.wasSuccessful()) {
            sender.sendMessage(CC.format("&cCould not complete request: %s (%d)",
                    response.getErrorMessage(), response.getCode()));
            return false;
        }

        if (!response.asArray().contains(new JsonPrimitive(target.toString()))) {
            sender.sendMessage(CC.RED + UUIDCache.getName(target) + " is not allowed to be op.");
            return false;
        }


        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(target);
        offlinePlayer.setOp(true);
        org.bukkit.command.Command.broadcastCommandMessage(sender, CC.format("&6Opped &f%s&6.",
                UUIDCache.getName(target)));
        return true;
    }

    @Command(names = {"deop"},
             permission = "invictus.command.deop",
             description = "Deop someone",
             async = true)
    public boolean deop(CommandSender sender, @Param(name = "target") UUID target) {
        if (sender instanceof Player) {
            RequestResponse response = RequestHandler.get("oplist");
            if (!response.wasSuccessful()) {
                sender.sendMessage(CC.format("&cCould not complete request: %s (%d)",
                        response.getErrorMessage(), response.getCode()));
                return false;
            }

            if (!response.asArray().contains(new JsonPrimitive(((Player) sender).getUniqueId().toString()))) {
                sender.sendMessage(CC.RED + "You are not allowed to deop people.");
                return false;
            }
        }

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(target);
        offlinePlayer.setOp(false);
        org.bukkit.command.Command.broadcastCommandMessage(sender, CC.format("&6De-opped &f%s&6.",
                UUIDCache.getName(target)));
        return true;
    }

}
