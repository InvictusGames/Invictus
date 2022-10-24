package cc.invictusgames.invictus.tip;

import cc.invictusgames.ilib.ILib;
import cc.invictusgames.ilib.utils.CC;
import cc.invictusgames.invictus.InvictusBukkit;
import cc.invictusgames.invictus.InvictusBukkitPlugin;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@RequiredArgsConstructor
public class TipService {

    private final InvictusBukkit invictus;

    private final AtomicInteger currentTip = new AtomicInteger();
    private final List<List<String>> tips = new ArrayList<>();

    public void loadTips() {
        TipConfig config = invictus.getConfigurationService().loadConfiguration(TipConfig.class,
                new File(ILib.getInstance().getMainConfig().getMessagesPath(), "invictus-tips.json"));
        tips.clear();
        tips.addAll(config.getGlobalTips());
        if (config.getServerTips().containsKey(invictus.getServerName()))
            tips.addAll(config.getServerTips().get(invictus.getServerName()));
    }

    public void startTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (tips.size() <= 0)
                    return;

                if (currentTip.get() >= tips.size())
                    currentTip.set(0);

                tips.get(currentTip.getAndIncrement()).forEach(message -> Bukkit.broadcastMessage(CC.translate(message)));
            }
        }.runTaskTimerAsynchronously(InvictusBukkitPlugin.getInstance(),
                TimeUnit.MINUTES.toSeconds(1L) * 20,
                TimeUnit.MINUTES.toSeconds(1L) * 20);
    }

}
