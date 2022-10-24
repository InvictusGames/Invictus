package cc.invictusgames.invictus.profile;

import cc.invictusgames.ilib.configuration.StaticConfiguration;
import cc.invictusgames.ilib.configuration.defaults.LocationConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 15.03.2020 / 14:14
 * Invictus / cc.invictusgames.invictus.spigot.profile
 */

@NoArgsConstructor
@Data
@EqualsAndHashCode
@ToString
public class OfflineProfile implements StaticConfiguration {

    public OfflineProfile(Player player) {
        LocationConfig location = new LocationConfig();
        location.setLocation(player.getLocation());
        this.uuid = player.getUniqueId();
        this.lastLocation = location;
    }

    private UUID uuid;
    private LocationConfig lastLocation;

}
