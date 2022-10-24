package cc.invictusgames.invictus.permission;

import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 21.06.2020 / 19:16
 * Invictus / cc.invictusgames.invictus.spigot.permission
 */

public class ProfilePermissionAttachment extends PermissionAttachment {

    private final Plugin plugin;
    private final ProfilePermissible permissible;
    private final Map<String, Boolean> permissions = new HashMap<>();

    public ProfilePermissionAttachment(Plugin plugin, Permissible permissible) {
        super(plugin, permissible);
        this.plugin = plugin;
        this.permissible = (ProfilePermissible) permissible;
    }


    @Override
    public Map<String, Boolean> getPermissions() {
        return permissions;
    }

    @Override
    public void setPermission(Permission perm, boolean value) {
        setPermission(perm.getName(), value);
    }

    @Override
    public void setPermission(String name, boolean value) {
        permissions.put(name.toLowerCase(), value);
        permissible.getPermissions().put(name.toLowerCase(), value);
    }

    @Override
    public void unsetPermission(Permission perm) {
        unsetPermission(perm.getName());
    }

    @Override
    public void unsetPermission(String name) {
        permissions.remove(name.toLowerCase());
        permissible.getPermissions().remove(name.toLowerCase());
    }

    public void clear() {
        permissions.forEach((perm, value) -> unsetPermission(perm));
    }
}
