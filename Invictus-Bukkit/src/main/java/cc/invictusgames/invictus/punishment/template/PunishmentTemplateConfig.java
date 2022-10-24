package cc.invictusgames.invictus.punishment.template;

import cc.invictusgames.ilib.configuration.StaticConfiguration;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@NoArgsConstructor
@Data
public class PunishmentTemplateConfig implements StaticConfiguration {

    private List<PunishmentTemplate> bans = Collections.singletonList(new PunishmentTemplate());

    private List<PunishmentTemplate> mutes = new ArrayList<>();

}
