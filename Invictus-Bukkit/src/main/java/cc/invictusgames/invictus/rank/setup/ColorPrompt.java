package cc.invictusgames.invictus.rank.setup;

import cc.invictusgames.ilib.chatinput.ChatInput;
import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.invictus.Invictus;
import cc.invictusgames.invictus.rank.Rank;
import cc.invictusgames.invictus.rank.commands.RankCommands;
import cc.invictusgames.invictus.rank.menu.RankEditingMenu;
import cc.invictusgames.invictus.utils.Tasks;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ColorPrompt extends ChatInput<String> {

    public ColorPrompt(Invictus invictus) {
        super(String.class);
        text(CC.translate("&ePlease enter the color for this rank, or type &ccancel &eto cancel."));
        escapeMessage(CC.RED + "You cancelled the further rank setup.");
        onCancel(player -> RankEditingMenu.RANK_SETUPS.remove(player.getUniqueId()));

        accept((player, input) -> {
            if (input.contains(" ")) {
                player.sendMessage(CC.RED + "The color cannot contain a white space.");
                return false;
            }

            UUID rankId = RankEditingMenu.RANK_SETUPS.get(player.getUniqueId());
            Rank rank = rankId == null ? null : invictus.getRankService().getRank(rankId);
            if (rank == null) {
                player.sendMessage(CC.RED + "The rank you were setting up no longer exists.");
                return true;
            }

            RankCommands.INSTANCE.rankSetColor(player, rank, input);
            // next: prefix
            return true;
        });
    }
}
