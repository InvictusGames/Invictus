package cc.invictusgames.invictus.utils;

import cc.invictusgames.ilib.redis.packet.Packet;
import cc.invictusgames.invictus.InvictusBukkit;
import cc.invictusgames.invictus.playersetting.InvictusSettings;
import cc.invictusgames.invictus.profile.Profile;
import org.bukkit.Bukkit;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 24.03.2020 / 03:08
 * Invictus / cc.invictusgames.invictus.spigot.utils
 */

public class NetworkBroadcastPacket implements Packet {

    private static final InvictusBukkit invictus = InvictusBukkit.getBukkitInstance();

    private final String message;
    private final String permission;
    private final boolean staff;
    private final String targetServer;

    public NetworkBroadcastPacket(String message) {
        this(message, null, false, null);
    }

    public NetworkBroadcastPacket(String message, String permission) {
        this(message, permission, false, null);
    }

    public NetworkBroadcastPacket(String message, String permission, boolean staff) {
        this(message, permission, staff, null);
    }

    public NetworkBroadcastPacket(String message, String permission, String targetServer) {
        this(message, permission, false, targetServer);
    }

    public NetworkBroadcastPacket(String message, String permission, boolean staff, String targetServer) {
        this.message = message;
        this.permission = permission;
        this.staff = staff;
        this.targetServer = targetServer;
    }

    @Override
    public void receive() {

        if (targetServer != null) {
            if (!invictus.getServerName().equals(targetServer)) {
                return;
            }
        }

        Bukkit.getConsoleSender().sendMessage(message);

        Bukkit.getOnlinePlayers().forEach(player -> {
            Profile profile = invictus.getProfileService().getProfile(player);

            if (permission != null && !player.hasPermission(permission)) {
                return;
            }

            if (staff) {
                if (profile != null
                        && !profile.isRequiresAuthentication()
                        && InvictusSettings.STAFF_MESSAGES.get(player))
                    player.sendMessage(message);
            } else player.sendMessage(message);
        });
    }
}
