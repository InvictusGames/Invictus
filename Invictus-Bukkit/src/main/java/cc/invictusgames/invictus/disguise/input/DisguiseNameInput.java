package cc.invictusgames.invictus.disguise.input;

import cc.invictusgames.ilib.chatinput.ChatInput;
import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.ilib.uuid.UUIDCache;
import cc.invictusgames.invictus.InvictusBukkit;
import cc.invictusgames.invictus.disguise.BukkitDisguiseService;
import cc.invictusgames.invictus.disguise.menu.DisguiseNameMenu;
import cc.invictusgames.invictus.disguise.menu.DisguiseRankMenu;
import cc.invictusgames.invictus.disguise.procedure.DisguiseProcedure;

public class DisguiseNameInput extends ChatInput<String> {

    public DisguiseNameInput(InvictusBukkit invictus, DisguiseNameMenu menu, DisguiseProcedure procedure) {
        super(String.class);
        text(CC.translate("&ePlease enter the name you would like to disguise as, or say &ccancel &eto cancel."));
        escapeMessage(CC.RED + "You cancelled the disguise procedure.");

        accept((player, input) -> {
            if (!BukkitDisguiseService.NAME_PATTERN.matcher(input).matches()) {
                player.sendMessage(CC.YELLOW + input + CC.RED + " does not follow the minecraft name format. " +
                        "(3-16 Alphanumeric characters)");
                return false;
            }

            if ((UUIDCache.getUuid(input) != null) && (!player.hasPermission("invictus.disguise.admin"))) {
                player.sendMessage(CC.RED + "You can only disguise as players that have never joined the server " +
                        "before.");
                return false;
            }

            menu.checkAvailability(input, b -> {
                if (!b) {
                    player.sendMessage(CC.RED + "The name " + CC.YELLOW + input + CC.RED + " is already in use.");
                    return;
                }

                procedure.setName(input);
                new DisguiseRankMenu(invictus, procedure).openMenu(player);
            });

            return true;
        });
    }
}
