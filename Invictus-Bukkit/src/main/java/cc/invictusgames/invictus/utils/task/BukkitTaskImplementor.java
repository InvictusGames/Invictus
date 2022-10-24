package cc.invictusgames.invictus.utils.task;

import cc.invictusgames.invictus.Invictus;
import cc.invictusgames.invictus.InvictusBukkitPlugin;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 12.12.2020 / 17:51
 * Invictus / cc.invictusgames.invictus.spigot.utils.tasks
 */

@RequiredArgsConstructor
public class BukkitTaskImplementor implements TaskImplementor {

    private final InvictusBukkitPlugin plugin;

    @Override
    public void run(Runnable runnable) {
        Bukkit.getScheduler().runTask(plugin, runnable);
    }

    @Override
    public void runAsync(Runnable runnable) {
        Invictus.TASK_CHAIN.run(runnable);
    }

    @Override
    public void runLater(Runnable runnable, long delay) {
        Bukkit.getScheduler().runTaskLater(plugin, runnable, delay);
    }

    @Override
    public void runLaterAsync(Runnable runnable, long delay) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, runnable, delay);
    }

    @Override
    public void runTimer(Runnable runnable, long delay, long interval) {
        Bukkit.getScheduler().runTaskTimer(plugin, runnable, delay, interval);
    }

    @Override
    public void runTimerAsync(Runnable runnable, long delay, long interval) {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, runnable, delay, interval);
    }
}
