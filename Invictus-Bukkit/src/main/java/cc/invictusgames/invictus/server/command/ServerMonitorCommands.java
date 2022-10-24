package cc.invictusgames.invictus.server.command;

import cc.invictusgames.ilib.command.annotation.Command;
import cc.invictusgames.ilib.command.annotation.Param;
import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.invictus.Invictus;
import cc.invictusgames.invictus.server.menu.ServerListMenu;
import cc.invictusgames.invictus.server.packet.ExecuteCommandPacket;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 14.09.2020 / 00:10
 * Invictus / cc.invictusgames.invictus.spigot.server.command
 */

@RequiredArgsConstructor
public class ServerMonitorCommands {

    private final Invictus invictus;

    public static boolean SEND_PACKET = true;

    @Command(names = {"servermanager list", "sm list"},
             permission = "servermanager.command.argument.list",
             description = "List all available servers",
             playerOnly = true)
    public boolean smList(Player sender) {
        new ServerListMenu(invictus).openMenu(sender);
        return true;
    }

    @Command(names = {"servers"},
             permission = "servermanager.command.argument.list",
             description = "List all available servers",
             playerOnly = true)
    public boolean servers(Player sender) {
        return smList(sender);
    }

    @Command(names = {"sm toggleupdate"},
             permission = "op",
             hidden = true,
             description = "Toggle if the update packet of this server gets send (DEBUG ONLY!)")
    public boolean smToggleUpdate(CommandSender sender) {
        ServerMonitorCommands.SEND_PACKET = !ServerMonitorCommands.SEND_PACKET;
        sender.sendMessage(CC.YELLOW + "You have " + CC.colorBoolean(ServerMonitorCommands.SEND_PACKET)
                + CC.YELLOW + " the sending of the update packet for this server.");
        return true;
    }

    @Command(names = {"sm sendtogroup"}, permission = "owner")
    public boolean smSendToGroup(CommandSender sender,
                                 @Param(name = "scope") String scope,
                                 @Param(name = "command", wildcard = true) String command) {
        invictus.getRedisService().publish(new ExecuteCommandPacket(sender.getName(), null, scope, command));
        sender.sendMessage(CC.format(
                "&aExecuting &e%s &aon all &e%s &aservers.",
                command,
                scope
        ));
        return true;
    }

    @Command(names = {"sm sendto"}, permission = "owner")
    public boolean smSendToServer(CommandSender sender,
                                  @Param(name = "server") String server,
                                  @Param(name = "command", wildcard = true) String command) {
        invictus.getRedisService().publish(new ExecuteCommandPacket(sender.getName(), server, null, command));
        sender.sendMessage(CC.format(
                "&aExecuting &e%s &aon &e%s&a.",
                command,
                server
        ));
        return true;
    }

    @Command(names = {"sm sendtoall"}, permission = "owner")
    public boolean smSendToAll(CommandSender sender, @Param(name = "command", wildcard = true) String command) {
        invictus.getRedisService().publish(new ExecuteCommandPacket(sender.getName(), null, null, command));
        sender.sendMessage(CC.format(
                "&aExecuting &e%s &aon &aall servers.",
                command
        ));
        return true;
    }

}
