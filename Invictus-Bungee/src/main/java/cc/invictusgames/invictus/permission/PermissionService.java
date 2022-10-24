package cc.invictusgames.invictus.permission;

import cc.invictusgames.invictus.InvictusBungee;
import cc.invictusgames.invictus.InvictusBungeePlugin;
import cc.invictusgames.invictus.profile.Profile;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 08.03.2021 / 06:16
 * Invictus / cc.invictusgames.invictus.permission
 */

@RequiredArgsConstructor
public class PermissionService {

    private final InvictusBungee invictus;

    public void updatePermissions(ProxiedPlayer proxiedPlayer) {
        Profile profile = invictus.getProfileService().getProfile(proxiedPlayer.getUniqueId());
        if (profile == null)
            return;

        if (proxiedPlayer.getPermissions() != null) {
            List<String> permissions = new ArrayList<>(proxiedPlayer.getPermissions());
            permissions.forEach(permission -> proxiedPlayer.setPermission(permission, false));
        }

        Map<String, Boolean> permissions = getEffectivePermissions(profile);
        permissions.forEach(proxiedPlayer::setPermission);
    }

    public Map<String, Boolean> getEffectivePermissions(Profile profile) {
        Map<String, Boolean> effectivePermissions = new HashMap<>();
        for (String group : profile.proxiedPlayer().getGroups()) {
            for (String permission : ProxyServer.getInstance().getConfigurationAdapter().getPermissions(group)) {
                effectivePermissions.put(permission, true);
            }
        }

        profile.getActiveGrants().forEach(grant -> effectivePermissions.putAll(convert(grant.getRank().getAllPermissions())));
        effectivePermissions.putAll(convert(profile.getPermissions()));
        return effectivePermissions;
    }

    public Map<String, Boolean> convert(List<String> list) {
        Map<String, Boolean> permissions = new HashMap<>();
        list.forEach(permission -> {
            if (permission.startsWith("-"))
                permissions.put(permission.substring(1), false);
            else permissions.put(permission, true);
        });
        return permissions;
    }


}
