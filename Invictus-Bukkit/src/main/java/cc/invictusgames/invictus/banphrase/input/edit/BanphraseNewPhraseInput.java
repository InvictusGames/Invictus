package cc.invictusgames.invictus.banphrase.input.edit;

import cc.invictusgames.ilib.chatinput.ChatInput;
import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.ilib.utils.json.JsonBuilder;
import cc.invictusgames.invictus.Invictus;
import cc.invictusgames.invictus.banphrase.Banphrase;
import cc.invictusgames.invictus.banphrase.packets.BanphraseReloadPacket;
import cc.invictusgames.invictus.connection.RequestHandler;
import cc.invictusgames.invictus.connection.RequestResponse;
import cc.invictusgames.invictus.utils.Tasks;

public class BanphraseNewPhraseInput extends ChatInput<String> {

    public BanphraseNewPhraseInput(Invictus invictus, Banphrase banphrase) {
        super(String.class);
        text(CC.translate("&ePlease enter the new phrase for this banphrase, " +
                "or say &ccancel &eto cancel."));
        escapeMessage(CC.RED + "You cancelled the banphrase edit procedure.");

        accept((player, input) -> {
            Tasks.runAsync(() -> {
                RequestResponse response = RequestHandler.put("banphrase/%s",
                        new JsonBuilder().add("phrase", input).build(),
                        banphrase.getId().toString());

                if (!response.wasSuccessful()) {
                    player.sendMessage(CC.format("&cCould not update banphrase: %s (%d)",
                            response.getErrorMessage(), response.getCode()));
                    return;
                }

                banphrase.setPhrase(input);
                invictus.getRedisService().publish(new BanphraseReloadPacket());
                player.sendMessage(CC.format("&eYou changed the phrase of &c%s &eto &c%s&e.",
                        banphrase.getName(), banphrase.getPhrase()));
            });
            return true;
        });
    }
}
