package cc.invictusgames.invictus.profile;

import cc.invictusgames.ilib.utils.callback.TypeCallable;
import cc.invictusgames.invictus.Invictus;
import cc.invictusgames.invictus.utils.Tasks;
import com.mojang.authlib.GameProfile;
import lombok.Getter;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import net.minecraft.server.v1_8_R3.PlayerInteractManager;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 04.06.2020 / 11:24
 * Invictus / cc.invictusgames.invictus.spigot.profile
 */

@Getter
public class UnloadedProfile {

    private final Invictus invictus;
    private final UUID uuid;
    private final String name;

    public UnloadedProfile(Invictus invictus, UUID uuid, String name) {
        this.invictus = invictus;
        this.uuid = uuid;
        this.name = name;
    }

    public void load(TypeCallable<Profile> callable, boolean async) {
        invictus.getProfileService().loadProfile(uuid, callable, async);
    }

    public void loadPlayer(TypeCallable<Player> callable, boolean async) {
        if (async) {
            Tasks.runAsync(() -> loadPlayer(callable, false));
            return;
        }

        if (Bukkit.getPlayer(uuid) != null) {
            callable.callback(Bukkit.getPlayer(uuid));
            return;
        }

        if (!Bukkit.getOfflinePlayer(uuid).hasPlayedBefore()) {
            callable.callback(null);
            return;
        }

        MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
        EntityPlayer entity = new EntityPlayer(server, server.getWorldServer(0), new GameProfile(uuid, name),
                new PlayerInteractManager(server.getWorld()));
        Player player = entity.getBukkitEntity();

        if (player != null) {
            player.loadData();
        }
        callable.callback(player);
    }

    public void loadBoth(TypeCallable<Pair<Profile, Player>> callable, boolean async) {
        if (async) {
            Tasks.runAsync(() -> loadBoth(callable, false));
            return;
        }

        load(profile -> {
            loadPlayer(player -> {
                callable.callback(new ImmutablePair<>(profile, player));
            }, false);
        }, false);
    }
}
