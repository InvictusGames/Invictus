package cc.invictusgames.invictus.rank.setup;

import cc.invictusgames.ilib.chatinput.ChatInput;
import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.invictus.Invictus;
import cc.invictusgames.invictus.rank.Rank;
import cc.invictusgames.invictus.rank.commands.RankCommands;
import cc.invictusgames.invictus.rank.menu.RankEditingMenu;
import cc.invictusgames.invictus.utils.Tasks;
import org.bukkit.command.CommandSender;

import java.util.UUID;

public class WeightPrompt extends ChatInput<Integer> {

    public WeightPrompt(Invictus invictus) {
        super(Integer.class);
        text(CC.translate("&ePlease enter the weight for this rank, or type &ccancel &eto cancel."));
        escapeMessage(CC.RED + "You cancelled the further rank setup.");
        onCancel(player -> RankEditingMenu.RANK_SETUPS.remove(player.getUniqueId()));

        accept((player, input) -> {
            UUID rankId = RankEditingMenu.RANK_SETUPS.get(player.getUniqueId());
            Rank rank = rankId == null ? null : invictus.getRankService().getRank(rankId);
            if (rank == null) {
                player.sendMessage(CC.RED + "The rank you were setting up no longer exists.");
                return true;
            }

            RankCommands.INSTANCE.rankSetWeight(player, rank, input);
            // next: queue priority
            return true;
        });
    }
}
