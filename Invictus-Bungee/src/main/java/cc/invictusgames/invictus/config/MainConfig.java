package cc.invictusgames.invictus.config;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 13.12.2020 / 03:56
 * Invictus / cc.invictusgames.invictus.spigot.config
 */

@NoArgsConstructor
@Data
@EqualsAndHashCode
@ToString
public class MainConfig extends IMainConfig {

    private int maintenanceLevel = 0;
    private List<UUID> maintenanceList = new ArrayList<>();
    private int restrictedHubWeight = 65;

}
