package cc.invictusgames.invictus.config.entry;

import cc.invictusgames.ilib.configuration.StaticConfiguration;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 18.02.2020 / 23:13
 * Invictus / cc.invictusgames.invictus.spigot.config.entry
 */

@NoArgsConstructor
@Data
@EqualsAndHashCode
@ToString
public class LocalPermissionEntry implements StaticConfiguration {

    private String uuid = "";
    private ArrayList<String> permissions = new ArrayList<>();
}
