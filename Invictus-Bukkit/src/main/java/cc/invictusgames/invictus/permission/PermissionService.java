package cc.invictusgames.invictus.permission;

import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.ilib.utils.ReflectionUtil;
import cc.invictusgames.invictus.Invictus;
import cc.invictusgames.invictus.InvictusBukkit;
import cc.invictusgames.invictus.config.entry.LocalPermissionEntry;
import cc.invictusgames.invictus.grant.Grant;
import cc.invictusgames.invictus.profile.Profile;
import cc.invictusgames.invictus.rank.Rank;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftHumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissibleBase;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.SimplePluginManager;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 21.06.2020 / 18:41
 * Invictus / cc.invictusgames.invictus.spigot.permissions
 */

@RequiredArgsConstructor
public class PermissionService {

    private static final Logger LOG = Invictus.getInstance().getLogFactory().newLogger(PermissionService.class);

    private static final Field PERM_FIELD = ReflectionUtil.getField(CraftHumanEntity.class, "perm");
    private static final Field ATTACHMENTS_FIELD = ReflectionUtil.getField(PermissibleBase.class, "attachments");
    private static final Field PERM_SUBS_FIELD = ReflectionUtil.getField(SimplePluginManager.class, "permSubs");

    private final InvictusBukkit invictus;

    public void injectPlayer(Player player) {
        Profile profile = invictus.getProfileService().getProfile(player);
        if (profile == null) {
            LOG.warning(String.format("Tried to inject player without profile: %s (%s)",
                    player.getUniqueId(), player.getName()));
            return;
        }

        ProfilePermissible permissible = new ProfilePermissible(invictus, profile, player);

        PermissibleBase oldPermissible = ReflectionUtil.getFieldValue(PERM_FIELD, player);
        if (oldPermissible instanceof ProfilePermissible) {
            return;
        }

        List<PermissionAttachment> attachments = ReflectionUtil.getFieldValue(ATTACHMENTS_FIELD, oldPermissible);
        attachments.forEach(permissible::convertAttachment);
        attachments.clear();

        oldPermissible.clearPermissions();
        permissible.setOldPermissible(oldPermissible);

        ReflectionUtil.setFieldValue(PERM_FIELD, player, permissible);
        updatePermissions(player);
    }

    public void uninjectPlayer(Player player) {
        Profile profile = invictus.getProfileService().getProfile(player);
        if (profile == null) {
            LOG.warning(String.format("Tried to uninject player without profile: %s (%s)",
                    player.getUniqueId(), player.getName()));
            return;
        }

        ProfilePermissible permissible = getProfilePermissible(player);

        if (permissible == null) {
            return;
        }

        permissible.clearPermissions();

        PermissibleBase old;
        if (permissible.getOldPermissible() != null) {
            old = permissible.getOldPermissible();
        } else {
            old = new PermissibleBase(player);
        }

        ReflectionUtil.setFieldValue(PERM_FIELD, player, old);
    }

    public void updatePermissions(Player player) {
        Profile profile = invictus.getProfileService().getProfile(player);
        if (profile == null) {
            LOG.warning(String.format("Tried to update player without profile: %s (%s)",
                    player.getUniqueId(), player.getName()));
            return;
        }

        ProfilePermissible permissible = getProfilePermissible(player);
        permissible.clearPermissions();

        Set<Permission> defaults = Bukkit.getPluginManager().getDefaultPermissions(permissible.isOp());
        defaults.forEach(permission ->
                permissible.getPermissions().put(permission.getName().toLowerCase(),
                        permission.getDefault().getValue(permissible.isOp()))
        );

        permissible.getPermissions().putAll(getEffectivePermissions(profile));
        LocalPermissionEntry permissionEntry = invictus.getLocalPermissionConfig().getEntry(profile);
        if (permissionEntry != null) {
            permissible.getPermissions().putAll(convert(permissionEntry.getPermissions()));
        }
    }

    public void injectFakeSubscriptionMap() {
        InvictusSubscriptionMap subscriptionMap = new InvictusSubscriptionMap();
        ReflectionUtil.setFieldValue(PERM_SUBS_FIELD, Bukkit.getPluginManager(), subscriptionMap);
    }

    public ProfilePermissible getProfilePermissible(Player player) {
        PermissibleBase permissibleBase = ReflectionUtil.getFieldValue(PERM_FIELD, player);
        if (!(permissibleBase instanceof ProfilePermissible)) {
            return null;
        }

        return (ProfilePermissible) permissibleBase;
    }

    public Map<String, Boolean> getEffectivePermissions(Profile profile) {
        Map<String, Boolean> effectivePermissions = new HashMap<>();

        List<Grant> grants = new ArrayList<>(profile.getActiveGrants());
        grants.sort(Grant.COMPARATOR.reversed());
        for (Grant grant : grants) {
            effectivePermissions.putAll(convert(grant.getRank().getAllPermissions()));
        }
        grants.clear();


        effectivePermissions.putAll(convert(profile.getPermissions()));
        if (profile.isNitroBoosted()) {
            effectivePermissions.put("invictus.nitroboost", true);
            effectivePermissions.put("aresenic.gkit.nitro", true);
        }
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

    public List<String> getDebugInfo(Player player, String permission) {
        List<String> debugs = new ArrayList<>();
        Profile profile = invictus.getProfileService().getProfile(player);
        AtomicBoolean hasPermission = new AtomicBoolean(false);
        profile.getActiveGrants().stream()
                .map(Grant::getRank)
                .sorted(Rank.COMPARATOR.reversed()).forEach(rank -> {
            Map<String, Boolean> perms = convert(rank.getAllPermissions());
            String result = CC.GRAY + "NOT_SET";
            if (perms.containsKey(permission.toLowerCase())) {
                Boolean value = perms.get(permission.toLowerCase());
                result = CC.colorBoolean(value, "YES", "NEGATED");
                if (value)
                    hasPermission.set(true);
            }

            debugs.add(CC.BLUE + "Grant " + rank.getName() + ": " + result);
        });

        Map<String, Boolean> perms = convert(profile.getPermissions());
        String result = CC.GRAY + "NOT_SET";
        if (perms.containsKey(permission.toLowerCase())) {
            Boolean value = perms.get(permission.toLowerCase());
            result = CC.colorBoolean(value, "YES", "NEGATED");
            if (value)
                hasPermission.set(true);
        }

        debugs.add(CC.BLUE + "Profile: " + result);

        debugs.add(CC.format(
                "&9Result: &e%s %s &9permission &e%s&9.",
                player.getName(),
                CC.colorBoolean(hasPermission.get(), "has", "doesn't have"),
                permission
        ));
        return debugs;
    }
}
