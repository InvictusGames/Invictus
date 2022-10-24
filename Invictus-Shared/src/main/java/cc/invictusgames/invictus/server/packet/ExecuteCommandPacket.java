package cc.invictusgames.invictus.server.packet;

import cc.invictusgames.ilib.redis.packet.Packet;
import cc.invictusgames.invictus.Invictus;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.logging.Logger;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 22.10.2020 / 17:10
 * Invictus / cc.invictusgames.invictus.spigot.server.packet
 */

@NoArgsConstructor
@AllArgsConstructor
public class ExecuteCommandPacket implements Packet {

    private static final Invictus invictus = Invictus.getInstance();
    private static final Logger LOG = invictus.getLogFactory().newLogger("CommandBroadcast");

    private String executor = "Console";
    private String server = null;
    private String scope = null;
    private String command = "";

    @Override
    public void receive() {
        if (server == null && scope == null && !invictus.getServerGroup().equals("proxy")) {
            LOG.info("Executing command '" + command + "' on all servers by " + executor + ".");
            invictus.dispatchConsoleCommand(command);
            return;
        }

        if (invictus.getMainConfig().getServerName().equalsIgnoreCase(server)) {
            LOG.info("Executing command '" + command + "' by " + executor + ".");
            invictus.dispatchConsoleCommand(command);
            return;
        }

        if (invictus.getServerGroup().equalsIgnoreCase(scope)) {
            LOG.info("Executing command '" + command + "' on all " + scope + " servers by " + executor + ".");
            invictus.dispatchConsoleCommand(command);
            return;
        }
    }
}
