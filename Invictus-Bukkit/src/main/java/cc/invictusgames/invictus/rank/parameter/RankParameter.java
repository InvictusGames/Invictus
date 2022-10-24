package cc.invictusgames.invictus.rank.parameter;

import cc.invictusgames.ilib.command.parameter.ParameterType;
import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.invictus.Invictus;
import cc.invictusgames.invictus.rank.Rank;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 08.06.2020 / 21:39
 * Invictus / cc.invictusgames.invictus.spigot.rank.parameter
 */

public class RankParameter implements ParameterType<Rank> {

    private static final Invictus invictus = Invictus.getInstance();

    @Override
    public Rank parse(CommandSender sender, String source) {
        if (source.equals("@default")) {
            return invictus.getRankService().getDefaultRank();
        }
        Rank rank = invictus.getRankService().getRank(source);
        if (rank == null) {
            sender.sendMessage(CC.format("&cRank &e%s &cnot found.", source));
            return null;
        }
        return rank;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, List<String> flags) {
        List<String> completions = new ArrayList<>();
        for (Rank rank : invictus.getRankService().getRanksSorted()) {
            completions.add(rank.getName());
        }
        return completions;
    }
}
