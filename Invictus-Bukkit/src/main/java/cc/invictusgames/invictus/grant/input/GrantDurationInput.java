package cc.invictusgames.invictus.grant.input;

import cc.invictusgames.ilib.chatinput.ChatInput;
import cc.invictusgames.ilib.command.parameter.defaults.Duration;
import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.invictus.Invictus;
import cc.invictusgames.invictus.profile.Profile;

public class GrantDurationInput extends ChatInput<Duration> {

    public GrantDurationInput(Invictus invictus) {
        super(Duration.class);
        text(CC.translate("&ePlease enter the duration for this grant (\"perm\" for permanent), " +
                "or say &ccancel &eto cancel."));
        escapeMessage(CC.RED + "You cancelled the grant procedure");

        accept((player, duration) -> {
            Profile profile = invictus.getProfileService().getProfile(player);
            if (profile.getGrantProcedure() == null) {
                player.sendMessage(CC.RED + "You're not in a granting process, idk how you even got this prompt");
                return true;
            }

            profile.getGrantProcedure().setDuration(duration.getDuration());
            return true;
        });
    }
}
