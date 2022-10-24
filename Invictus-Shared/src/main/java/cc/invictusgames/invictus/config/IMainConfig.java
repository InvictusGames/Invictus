package cc.invictusgames.invictus.config;

import cc.invictusgames.ilib.configuration.StaticConfiguration;
import cc.invictusgames.ilib.configuration.defaults.RedisConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 30.11.2020 / 14:34
 * Invictus / cc.invictusgames.invictus.spigot.config
 */

@NoArgsConstructor
@Data
@EqualsAndHashCode
@ToString
public abstract class IMainConfig implements StaticConfiguration {

    private String serverName = "server";
    private String serverGroup = "group";

    private String backendHost = "http://localhost:8080/";
    private String backendKey = "1234567890";
    private RedisConfig redisConfig = new RedisConfig();

    private int staffWeight = 160;
    private int adminWeight = 210;
    private int ownerWeight = 280;

}
