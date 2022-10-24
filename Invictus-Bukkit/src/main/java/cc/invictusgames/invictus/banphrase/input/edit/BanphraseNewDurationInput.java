package cc.invictusgames.invictus.banphrase.input.edit;

import cc.invictusgames.ilib.chatinput.ChatInput;
import cc.invictusgames.ilib.command.parameter.defaults.Duration;
import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.ilib.utils.TimeUtils;
import cc.invictusgames.ilib.utils.json.JsonBuilder;
import cc.invictusgames.invictus.Invictus;
import cc.invictusgames.invictus.banphrase.Banphrase;
import cc.invictusgames.invictus.banphrase.packets.BanphraseReloadPacket;
import cc.invictusgames.invictus.connection.RequestHandler;
import cc.invictusgames.invictus.connection.RequestResponse;
import cc.invictusgames.invictus.utils.Tasks;

public class BanphraseNewDurationInput extends ChatInput<Duration> {

    public BanphraseNewDurationInput(Invictus invictus, Banphrase banphrase) {
        super(Duration.class);
        text(CC.translate("&ePlease enter the new duration for this banphrase " +
                "(\"perm\" for permanent), or say &ccancel &eto cancel."));
        escapeMessage(CC.RED + "You cancelled the banphrase edit procedure.");

        accept((player, input) -> {
            Tasks.runAsync(() -> {
                RequestResponse response = RequestHandler.put("banphrase/%s",
                        new JsonBuilder().add("duration", input.getDuration()).build(),
                        banphrase.getId().toString());

                if (!response.wasSuccessful()) {
                    player.sendMessage(CC.format("&cCould not update banphrase: %s (%d)",
                            response.getErrorMessage(), response.getCode()));
                    return;
                }

                banphrase.setDuration(input.getDuration());
                invictus.getRedisService().publish(new BanphraseReloadPacket());
                player.sendMessage(CC.format("&eYou changed the duration of &c%s &eto &c%s&e.",
                        banphrase.getName(), TimeUtils.formatTimeShort(input.getDuration())));
            });
            return true;
        });
    }
}
