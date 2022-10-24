package cc.invictusgames.invictus.server.parameter;

import cc.invictusgames.ilib.command.parameter.ParameterType;
import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.invictus.server.ServerInfo;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 25.10.2020 / 21:15
 * Invictus / cc.invictusgames.invictus.spigot.server
 */

public class ServerInfoParameter implements ParameterType<ServerInfo> {

    @Override
    public ServerInfo parse(CommandSender sender, String source) {
        ServerInfo parsed = ServerInfo.getServerInfo(source);
        if (parsed == null)
            sender.sendMessage(CC.RED + "Server " + CC.YELLOW + source + CC.RED + " not found.");

        return parsed;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, List<String> flags) {
        return ServerInfo.getServers().stream()
                .map(ServerInfo::getName)
                .collect(Collectors.toList());
    }
}
