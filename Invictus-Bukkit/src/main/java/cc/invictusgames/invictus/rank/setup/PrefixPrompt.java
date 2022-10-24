package cc.invictusgames.invictus.rank.setup;

import cc.invictusgames.ilib.chatinput.ChatInput;
import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.ilib.utils.ChatMessage;
import cc.invictusgames.invictus.Invictus;
import cc.invictusgames.invictus.rank.Rank;
import cc.invictusgames.invictus.rank.commands.RankCommands;
import cc.invictusgames.invictus.rank.menu.RankEditingMenu;
import cc.invictusgames.invictus.utils.Tasks;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PrefixPrompt extends ChatInput<String> {

    private final Invictus invictus;

    public PrefixPrompt(Invictus invictus) {
        super(String.class);
        this.invictus = invictus;

        text(CC.translate("&ePlease enter the prefix for this rank, or type &ccancel &eto cancel."));
        escapeMessage(CC.RED + "You cancelled the further rank setup.");
        onCancel(player -> RankEditingMenu.RANK_SETUPS.remove(player.getUniqueId()));

        accept((player, input) -> {
            UUID rankId = RankEditingMenu.RANK_SETUPS.get(player.getUniqueId());
            Rank rank = rankId == null ? null : invictus.getRankService().getRank(rankId);
            if (rank == null) {
                player.sendMessage(CC.RED + "The rank you were setting up no longer exists.");
                return true;
            }

            RankCommands.INSTANCE.rankSetPrefix(player, rank, input);
            // next: weight
            return true;
        });
    }

    @Override
    public void send(Player player) {
        super.send(player);

        UUID rankId = RankEditingMenu.RANK_SETUPS.get(player.getUniqueId());
        Rank rank = rankId == null ? null : invictus.getRankService().getRank(rankId);

        if (rank == null)
            return;

        String color = rank.getColor().replace(ChatColor.COLOR_CHAR, '&');
        String suggested = String.format("&7[%s&7] %s",
                color + rank.getName().replace('-', ' '), color);

        new ChatMessage(CC.format(
                "&e(Click to get the suggested %sExample&e)",
                CC.translate(suggested)
        )).suggestCommand(suggested).send(player);
    }
}
