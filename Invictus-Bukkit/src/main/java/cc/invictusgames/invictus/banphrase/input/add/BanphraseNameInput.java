package cc.invictusgames.invictus.banphrase.input.add;

import cc.invictusgames.ilib.chatinput.ChatInput;
import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.invictus.banphrase.menu.add.SelectBanphraseOperatorMenu;
import cc.invictusgames.invictus.banphrase.procedure.BanphraseAddProcedure;

public class BanphraseNameInput extends ChatInput<String> {

    public BanphraseNameInput(BanphraseAddProcedure procedure) {
        super(String.class);

        text(CC.translate("&ePlease enter the name for the banphrase, or say &ccancel &eto cancel."));
        escapeMessage(CC.RED + "You cancelled the banphrase creation procedure.");

        accept((player, input) -> {
            procedure.setName(input);
            new SelectBanphraseOperatorMenu(procedure).openMenu(player);
            return true;
        });
    }
}
