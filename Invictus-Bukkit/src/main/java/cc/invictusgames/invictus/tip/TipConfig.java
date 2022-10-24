package cc.invictusgames.invictus.tip;

import cc.invictusgames.ilib.configuration.StaticConfiguration;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@NoArgsConstructor
@Data
public class TipConfig implements StaticConfiguration {

    private List<List<String>> globalTips =
            Collections.singletonList(Collections.singletonList("&7[&dTip&7] &fBreathing keeps you alive."));

    private Map<String, List<List<String>>> serverTips = new HashMap<>();

}
