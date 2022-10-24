package cc.invictusgames.invictus.banphrase.input.add;

import cc.invictusgames.ilib.chatinput.ChatInput;
import cc.invictusgames.ilib.command.parameter.defaults.Duration;
import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.invictus.banphrase.procedure.BanphraseAddProcedure;

public class BanphraseDurationInput extends ChatInput<Duration> {

    public BanphraseDurationInput(BanphraseAddProcedure procedure) {
        super(Duration.class);
        text(CC.translate("&ePlease enter the duration for the banphrase " +
                "(\"perm\" for permanent), or say &ccancel &eto cancel."));
        escapeMessage(CC.RED + "You cancelled the banphrase creation procedure.");

        accept((player, input) -> {
            procedure.setDuration(input.getDuration());
            procedure.finish();
            return true;
        });
    }
}
