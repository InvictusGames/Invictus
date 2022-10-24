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

public class BanphraseNewNameInput extends ChatInput<String> {

    public BanphraseNewNameInput(Invictus invictus, Banphrase banphrase) {
        super(String.class);
        text(CC.translate("&ePlease enter the new name for this banphrase, " +
                "or say &ccancel &eto cancel."));
        escapeMessage(CC.RED + "You cancelled the banphrase rename procedure.");

        accept((player, input) -> {
            Tasks.runAsync(() -> {
                RequestResponse response = RequestHandler.put("banphrase/%s",
                        new JsonBuilder().add("name", input).build(),
                        banphrase.getId().toString());

                if (!response.wasSuccessful()) {
                    player.sendMessage(CC.format("&cCould not update banphrase: %s (%d)",
                            response.getErrorMessage(), response.getCode()));
                    return;
                }

                banphrase.setName(input);
                invictus.getRedisService().publish(new BanphraseReloadPacket());
                player.sendMessage(CC.format("&eYou renamed the banphrase to &c%s&e.",
                        banphrase.getName()));
            });
            return true;
        });
    }
}
