package cc.invictusgames.invictus.utils;

import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.ilib.utils.TimeUtils;
import cc.invictusgames.invictus.InvictusBukkit;
import cc.invictusgames.invictus.grant.Grant;
import cc.invictusgames.invictus.profile.Profile;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 26.06.2020 / 12:36
 * Invictus / cc.invictusgames.invictus.spigot.utils
 */

public class ActionLogger {

    private static final InvictusBukkit invictus = InvictusBukkit.getBukkitInstance();

    public static void logGrantAdd(String by, Profile to, Grant grant) {
        invictus.getRedisService().publish(new NetworkBroadcastPacket(
                String.format(
                        CC.translate("&c[LOG] &4%s &fgranted rank %s &f(&e%s&f) &fto %s &ffor &e%s"),
                        by,
                        grant.getRank().getDisplayName(),
                        TimeUtils.formatDetailed(grant.getDuration()),
                        to.getRealDisplayName(),
                        grant.getGrantedReason()
                ),
                "invictus.admin",
                true
        ));
    }

    public static void logGrantRemove(String by, Profile from, Grant grant) {
        invictus.getRedisService().publish(new NetworkBroadcastPacket(
                String.format(
                        CC.translate("&c[LOG] &4%s &fremoved grant %s &ffrom %s &ffor &e%s"),
                        by,
                        grant.getRank().getDisplayName(),
                        from.getRealDisplayName(),
                        grant.getRemovedReason()
                ),
                "invictus.admin",
                true
        ));
    }

}
