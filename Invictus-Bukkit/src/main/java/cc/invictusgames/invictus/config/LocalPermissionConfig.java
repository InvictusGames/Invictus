package cc.invictusgames.invictus.config;

import cc.invictusgames.ilib.configuration.StaticConfiguration;
import cc.invictusgames.invictus.config.entry.LocalPermissionEntry;
import cc.invictusgames.invictus.profile.Profile;
import cc.invictusgames.invictus.rank.Rank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 18.02.2020 / 23:13
 * Invictus / cc.invictusgames.invictus.spigot.config
 */

@NoArgsConstructor
@Data
@EqualsAndHashCode
@ToString
public class LocalPermissionConfig implements StaticConfiguration {

    List<LocalPermissionEntry> rankPermissions = new ArrayList<>();
    List<LocalPermissionEntry> playerPermissions = new ArrayList<>();

    public LocalPermissionEntry getEntry(Rank rank) {
        for (LocalPermissionEntry entry : rankPermissions) {
            if (entry.getUuid().equals(rank.getUuid().toString()))
                return entry;
        }

        return null;
    }

    public LocalPermissionEntry getEntry(Profile profile) {
        for (LocalPermissionEntry entry : playerPermissions) {
            if (entry.getUuid().equals(profile.getUuid().toString()))
                return entry;
        }

        return null;
    }
}
