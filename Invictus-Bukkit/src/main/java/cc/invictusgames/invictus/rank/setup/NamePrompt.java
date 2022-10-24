package cc.invictusgames.invictus.rank.setup;

import cc.invictusgames.ilib.chatinput.ChatInput;
import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.invictus.rank.Rank;
import cc.invictusgames.invictus.rank.commands.RankCommands;
import cc.invictusgames.invictus.rank.menu.RankEditingMenu;

public class NamePrompt extends ChatInput<String> {

    public NamePrompt() {
        super(String.class);
        text(CC.translate("&ePlease enter the name for the rank, or say &ccancel &eto cancel."));
        escapeMessage(CC.RED + "You cancelled the rank creation.");
        onCancel(player -> RankEditingMenu.RANK_SETUPS.remove(player.getUniqueId()));

        accept((player, input) -> {
            if (input.contains(" ")) {
                player.sendMessage(CC.RED + "The name cannot contain a white space.");
                return false;
            }

            Rank rank = RankCommands.createRank(player, input);
            if (rank != null)
                RankEditingMenu.RANK_SETUPS.put(player.getUniqueId(), rank.getUuid());
            // next: color
            return true;
        });
    }
}
