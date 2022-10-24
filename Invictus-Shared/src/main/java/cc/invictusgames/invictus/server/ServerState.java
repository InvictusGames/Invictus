package cc.invictusgames.invictus.server;

import cc.invictusgames.ilib.utils.CC;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 13.09.2020 / 22:27
 * Invictus / cc.invictusgames.invictus.spigot.server
 */

@Getter
@AllArgsConstructor
public enum ServerState {

    ONLINE(CC.GREEN + "Online"),
    OFFLINE(CC.RED + "Offline", CC.RED + "Offline " + CC.GRAY + "(Plugin Disabled)"),
    WHITELISTED(CC.YELLOW + "Whitelisted"),
    HEARTBEAT_TIMEOUT(CC.RED + "Offline", CC.RED + "Offline " + CC.GRAY + "(Heartbeat Timeout)"),
    UNKNOWN(CC.RED + "Offline", CC.RED + "Offline " + CC.GRAY + "(Unknown)");

    private final String displayName;
    private final String internalName;

    ServerState(String displayName) {
        this(displayName, displayName);
    }

}
