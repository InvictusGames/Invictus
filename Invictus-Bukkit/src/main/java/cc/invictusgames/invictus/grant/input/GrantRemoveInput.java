package cc.invictusgames.invictus.grant.input;

import cc.invictusgames.ilib.chatinput.ChatInput;
import cc.invictusgames.ilib.redis.packet.Packet;
import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.invictus.InvictusBukkit;
import cc.invictusgames.invictus.connection.RequestResponse;
import cc.invictusgames.invictus.grant.Grant;
import cc.invictusgames.invictus.grant.packets.GrantRemovePacket;
import cc.invictusgames.invictus.profile.Profile;
import cc.invictusgames.invictus.profile.packets.ProfileUpdatePacket;
import cc.invictusgames.invictus.utils.Tasks;

public class GrantRemoveInput extends ChatInput<String> {

    public GrantRemoveInput(InvictusBukkit invictus, Profile target, Grant grant) {
        super(String.class);
        text(CC.translate("&ePlease enter the reason for the removal of this grant, or say &ccancel &eto cancel."));
        escapeMessage(CC.RED + "You cancelled the grant removal.");

        accept((player, input) -> {
            grant.setRemovedAt(System.currentTimeMillis());
            grant.setRemovedBy(player.getUniqueId().toString());
            grant.setRemovedReason(input);
            grant.setRemoved(true);

            Tasks.runAsync(() -> {
                //Packet packet = new GrantRemovePacket(target.getUuid(), grant.getRank().getUuid());
                RequestResponse response = invictus.getBukkitProfileService().removeGrant(target, grant);
                if (response.couldNotConnect()) {
                    player.sendMessage(CC.format("&cCould not connect to API to remove grant. " +
                                    "Adding grant to the queue. Error: %s (%d)",
                            response.getErrorMessage(), response.getCode()));
                } else if (!response.wasSuccessful()) {
                    player.sendMessage(CC.format("&cCould not remove grant: %s (%d)",
                            response.getErrorMessage(), response.getCode()));
                    return;
                }

                player.sendMessage(CC.format(
                        "&aYou've removed a %s&a grant from %s&a.",
                        grant.getRank().getDisplayName(),
                        target.getRealDisplayName()
                ));
            });
            return true;
        });
    }
}
