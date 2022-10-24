package cc.invictusgames.invictus.bossbar.config;

import cc.invictusgames.ilib.configuration.StaticConfiguration;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
@NoArgsConstructor
public class BossBarConfig implements StaticConfiguration {

    private final List<BossBarEntry> animations = Collections.singletonList(new BossBarEntry());

}
