package cc.invictusgames.invictus.constant;

import cc.invictusgames.invictus.Invictus;
import com.google.common.collect.Sets;

import java.util.Set;

public abstract class Constant<T> {

    public static final Constant<Integer> STAFF_WEIGHT = new Constant<Integer>("staff_weight", 0) {
        @Override
        public Integer parse(String input) {
            Integer parsed = null;
            try {
                parsed = Integer.parseInt(input);
            } catch (NumberFormatException ignored) { }
            return parsed;
        }
    };

    public static final Constant<Integer> ADMIN_WEIGHT = new Constant<Integer>("admin_weight", 0) {
        @Override
        public Integer parse(String input) {
            Integer parsed = null;
            try {
                parsed = Integer.parseInt(input);
            } catch (NumberFormatException ignored) { }
            return parsed;
        }
    };

    public static final Constant<Integer> OWNER_WEIGHT = new Constant<Integer>("owner_weight", 0) {
        @Override
        public Integer parse(String input) {
            Integer parsed = null;
            try {
                parsed = Integer.parseInt(input);
            } catch (NumberFormatException ignored) { }
            return parsed;
        }
    };

    public static final Set<Constant> CONSTANTS = Sets.newHashSet(
            STAFF_WEIGHT,
            ADMIN_WEIGHT,
            OWNER_WEIGHT
    );

    private final String key;
    private T value;

    public Constant(String key, T initialValue) {
        this.key = key;
        this.value = initialValue;
    }

    public void loadValue() {
        String redisValue = Invictus.getInstance().getRedisService().executeBackendCommand(redis -> {
            if (!redis.hexists("invictus:constant", key))
                return null;

            return redis.hget("invictus:constant", key);
        });

        if (redisValue == null)
            saveValue();
        else value = parse(redisValue);
    }

    public void saveValue() {
        Invictus.getInstance().getRedisService().executeBackendCommand(redis ->
                redis.hset("invictus:constant", key, value.toString()));
    }

    public abstract T parse(String input);

    public T get() {
        return value;
    }

    public void set(T value) {
        this.value = value;
    }


}
