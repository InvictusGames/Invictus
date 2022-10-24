package cc.invictusgames.invictus.permission;

import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class InvictusSubscriptionMap implements Map<String, Map<Permissible, Boolean>> {

    @Override
    public Map<Permissible, Boolean> get(Object o) {
        return new ValueMap((String) o);
    }

    @Override public Map<Permissible, Boolean> put(String key, Map<Permissible, Boolean> value) { throw new UnsupportedOperationException(); }
    @Override public Map<Permissible, Boolean> remove(Object key) { throw new UnsupportedOperationException(); }
    @Override public void putAll(Map<? extends String, ? extends Map<Permissible, Boolean>> m) { throw new UnsupportedOperationException(); }
    @Override public void clear() { throw new UnsupportedOperationException(); }
    @Override public Set<String> keySet() { throw new UnsupportedOperationException(); }
    @Override public Collection<Map<Permissible, Boolean>> values() { throw new UnsupportedOperationException(); }
    @Override public Set<Entry<String, Map<Permissible, Boolean>>> entrySet() { throw new UnsupportedOperationException(); }
    @Override public int size() { return 0; }
    @Override public boolean isEmpty() { throw new UnsupportedOperationException(); }
    @Override public boolean containsKey(Object key) { throw new UnsupportedOperationException(); }
    @Override public boolean containsValue(Object value) { throw new UnsupportedOperationException(); }

    @RequiredArgsConstructor
    public class ValueMap implements Map<Permissible, Boolean> {

        private final String permission;

        @NotNull
        @Override
        public Set<Permissible> keySet() {
            Set<Permissible> set = Sets.newHashSet();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.hasPermission(permission))
                    set.add(player);
            }

            ConsoleCommandSender consoleSender = Bukkit.getConsoleSender();
            if (consoleSender.hasPermission(permission))
                set.add(consoleSender);

            return set;
        }

        @Override
        public int size() {
            return 1;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }
        @Nullable
        @Override
        public Boolean put(Permissible permissible, Boolean aBoolean) {
            return null;
        }

        @Override
        public Boolean remove(Object o) {
            return null;
        }

        @Override public void putAll(Map<? extends Permissible, ? extends Boolean> m) { throw new UnsupportedOperationException(); }
        @Override public void clear() { throw new UnsupportedOperationException(); }
        @Override public Collection<Boolean> values() { throw new UnsupportedOperationException(); }
        @Override public Set<Entry<Permissible, Boolean>> entrySet() { throw new UnsupportedOperationException(); }
        @Override public boolean containsKey(Object key) { throw new UnsupportedOperationException(); }
        @Override public boolean containsValue(Object value) { throw new UnsupportedOperationException(); }
        @Override public Boolean get(Object key) { throw new UnsupportedOperationException(); }
    }
}
