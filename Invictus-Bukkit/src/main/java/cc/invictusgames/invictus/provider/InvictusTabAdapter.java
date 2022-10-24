package cc.invictusgames.invictus.provider;

import cc.invictusgames.ilib.tab.PlayerTab;
import cc.invictusgames.ilib.tab.TabAdapter;
import cc.invictusgames.ilib.tab.TabEntry;
import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.ilib.visibility.VisibilityService;
import cc.invictusgames.invictus.Invictus;
import cc.invictusgames.invictus.base.StaffMode;
import cc.invictusgames.invictus.base.commands.BaseCommands;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class InvictusTabAdapter implements TabAdapter {

    public static final InvictusTabAdapter INSTANCE = new InvictusTabAdapter(Invictus.getInstance());

    private final Invictus invictus;

    @Override
    public Table<Integer, Integer, TabEntry> getEntries(Player player) {
        Table<Integer, Integer, TabEntry> entries = HashBasedTable.create();

        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        players.sort(BaseCommands.PLAYER_COMPARATOR);

        int i = 0;
        int x = 0;
        int y = 0;
        for (Player current : players) {
            if (!VisibilityService.getOnlineTreatProvider().apply(current, player))
                continue;

            if (++i >= (PlayerTab.is1_8(player) ? 80 : 60))
                break;

            entries.put(x++, y, new TabEntry((StaffMode.isStaffMode(current) ? CC.GRAY + "*" : "")
                    + invictus.getProfileService().getProfile(current).getDisplayName())
                    .adaptPlayer(current));

            if (x >= (PlayerTab.is1_8(player) ? 4 : 3)) {
                x = 0;
                y++;
            }

        }

        return entries;
    }

    @Override
    public String getHeader(Player player) {
        return "";
    }

    @Override
    public String getFooter(Player player) {
        return "";
    }
}
