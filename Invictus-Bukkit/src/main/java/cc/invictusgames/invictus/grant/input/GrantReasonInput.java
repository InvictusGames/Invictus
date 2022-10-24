package cc.invictusgames.invictus.grant.input;

import cc.invictusgames.ilib.chatinput.ChatInput;
import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.invictus.InvictusBukkit;
import cc.invictusgames.invictus.grant.menu.GrantScopesMenu;
import cc.invictusgames.invictus.profile.Profile;

public class GrantReasonInput extends ChatInput<String> {

    public GrantReasonInput(InvictusBukkit invictus) {
        super(String.class);
        text(CC.translate("&ePlease enter the reason for this grant, or say &ccancel &eto cancel."));
        escapeMessage(CC.RED + "You cancelled the grant procedure.");

        accept((player, input) -> {
            Profile profile = invictus.getProfileService().getProfile(player);
            if (profile.getGrantProcedure() == null) {
                player.sendMessage(CC.RED + "You're not in a granting process, idk how you even got this prompt");
                return true;
            }

            profile.getGrantProcedure().setReason(input);
            new GrantScopesMenu(invictus, profile).openMenu(player);
            return true;
        });
    }
}
