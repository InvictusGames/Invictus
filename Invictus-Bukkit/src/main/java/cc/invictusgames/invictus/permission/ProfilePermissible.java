package cc.invictusgames.invictus.permission;

import cc.invictusgames.invictus.InvictusBukkit;
import cc.invictusgames.invictus.profile.Profile;
import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissibleBase;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 21.06.2020 / 19:09
 * Invictus / cc.invictusgames.invictus.spigot.permission
 */

@Getter
@Setter
public class ProfilePermissible extends PermissibleBase {

    private final InvictusBukkit invictus;
    private final Profile profile;

    private final Map<String, Boolean> permissions = new HashMap<>();
    private final Map<String, Boolean> tempPermissions = new HashMap<>();
    private final List<ProfilePermissionAttachment> attachments = new ArrayList<>();

    private PermissibleBase oldPermissible;

    public ProfilePermissible(InvictusBukkit invictus, Profile profile, Player player) {
        super(player);
        this.invictus = invictus;
        this.profile = profile;
    }

    @Override
    public boolean isPermissionSet(Permission perm) {
        Preconditions.checkNotNull(perm);
        return isPermissionSet(perm.getName().toLowerCase());
    }

    @Override
    public boolean isPermissionSet(String name) {
        Preconditions.checkNotNull(name);
        return tempPermissions.containsKey(name.toLowerCase())
                || permissions.containsKey(name.toLowerCase());
    }

    @Override
    public boolean hasPermission(Permission perm) {
        return isPermissionSet(perm) ?
                getPermValue(perm.getName().toLowerCase()) :
                perm.getDefault().getValue(isOp());
    }

    @Override
    public boolean hasPermission(String inName) {
        if (isPermissionSet(inName.toLowerCase()))
            return getPermValue(inName.toLowerCase());

        Permission perm = Bukkit.getPluginManager().getPermission(inName.toLowerCase());
        return perm != null ? perm.getDefault().getValue(isOp()) :
                Permission.DEFAULT_PERMISSION.getValue(isOp());
    }

    private boolean getPermValue(String name) {
        name = name.toLowerCase();
        return tempPermissions.containsKey(name) ? tempPermissions.get(name) : permissions.get(name);
    }

    @Override
    public void recalculatePermissions() {
        if (profile != null && profile.player() != null)
            invictus.getPermissionService().updatePermissions(profile.player());
    }

    @Override
    public boolean isOp() {
        return super.isOp();
    }

    @Override
    public void setOp(boolean value) {
        super.setOp(value);
    }

    @Override
    public ProfilePermissionAttachment addAttachment(Plugin plugin) {
        return new ProfilePermissionAttachment(plugin, this);
    }

    public void convertAttachment(PermissionAttachment attachment) {
        attachment.getPermissions().forEach((s, b) -> permissions.put(s.toLowerCase(), b));
    }

    @Override
    public void removeAttachment(PermissionAttachment attachment) {
        attachments.remove(attachment);
    }

    @Override
    public synchronized void clearPermissions() {
        permissions.clear();
        if (attachments != null)
            attachments.forEach(ProfilePermissionAttachment::clear);
    }
}
