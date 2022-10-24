package cc.invictusgames.invictus.disguise.procedure;

import cc.invictusgames.invictus.disguise.BukkitDisguiseService;
import cc.invictusgames.invictus.profile.Profile;
import cc.invictusgames.invictus.rank.Rank;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 18.06.2020 / 16:43
 * Invictus / cc.invictusgames.invictus.spigot.disguise.procedure
 */

@RequiredArgsConstructor
@Data
public class DisguiseProcedure {

    private final Profile profile;
    private String name;
    private Rank rank;
    private String skinName;
    private BukkitDisguiseService.SkinPreset preset;

}
