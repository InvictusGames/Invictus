package cc.invictusgames.invictus.grant.procedure;

import cc.invictusgames.invictus.grant.Grant;
import cc.invictusgames.invictus.profile.Profile;
import cc.invictusgames.invictus.rank.Rank;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 19.02.2020 / 18:04
 * Invictus / cc.invictusgames.invictus.spigot.grant.procedure
 */

@RequiredArgsConstructor
@Data
public class GrantProcedure {

    private final Profile profile;
    private final Profile target;
    private Grant grant;

    private Rank rank;
    private String reason = "";
    private long duration = -1;
    private List<String> scopes = new ArrayList<>();

}
