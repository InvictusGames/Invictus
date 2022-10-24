package cc.invictusgames.invictus.base.redisconvertion;

import cc.invictusgames.ilib.configuration.StaticConfiguration;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 15.11.2020 / 00:01
 * Invictus / cc.invictusgames.invictus.spigot.base.redisconvertion
 */

@NoArgsConstructor
@Data
@EqualsAndHashCode
@ToString
public class RedisDataConfig implements StaticConfiguration {

    private Map<String, String> totpData = new HashMap<>();

    private Map<String, Map<String, String>> primeData = new HashMap<>();

}
