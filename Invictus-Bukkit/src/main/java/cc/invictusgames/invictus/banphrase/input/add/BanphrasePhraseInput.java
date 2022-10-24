package cc.invictusgames.invictus.banphrase.input.add;

import cc.invictusgames.ilib.chatinput.ChatInput;
import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.invictus.banphrase.menu.add.SelectMuteModeMenu;
import cc.invictusgames.invictus.banphrase.procedure.BanphraseAddProcedure;

public class BanphrasePhraseInput extends ChatInput<String> {

    public BanphrasePhraseInput(BanphraseAddProcedure procedure) {
        super(String.class);

        text(CC.translate("&ePlease enter the phrase for the banphrase, or say &ccancel &eto cancel."));
        escapeMessage(CC.RED + "You cancelled the banphrase creation procedure.");

        accept((player, input) -> {
            procedure.setPhrase(input);
            new SelectMuteModeMenu(procedure).openMenu(player);
            return true;
        });
    }
}
