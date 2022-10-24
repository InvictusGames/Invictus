package cc.invictusgames.invictus.motd;

import cc.invictusgames.ilib.configuration.StaticConfiguration;
import cc.invictusgames.invictus.InvictusBungeePlugin;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Data
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class MotdConfig implements StaticConfiguration {

    private String one = "There is no motd set!";
    private String two = "";

    public void clearLines() {
        one = "";
        two = "";
    }

    public List<String> getLines() {
        return Arrays.asList(one, two);
    }

    public void saveConfig() {
        try {
            InvictusBungeePlugin.getInstance().getConfigurationService().saveConfiguration(this,
                    new File(InvictusBungeePlugin.getInstance().getDataFolder(), "motd.json"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
