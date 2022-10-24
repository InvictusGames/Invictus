package cc.invictusgames.invictus.utils.task;

import cc.invictusgames.invictus.InvictusBungeePlugin;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ProxyServer;

import java.util.concurrent.TimeUnit;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 13.12.2020 / 03:10
 * Invictus / cc.invictusgames.invictus.spigot.utils.task
 */

@RequiredArgsConstructor
public class BungeeTaskImplementor implements TaskImplementor {

    private final InvictusBungeePlugin plugin;

    @Override
    public void run(Runnable runnable) {
        throw new UnsupportedOperationException("Cannot schedule sync tasks on BungeeCord.");
    }

    @Override
    public void runAsync(Runnable runnable) {
        ProxyServer.getInstance().getScheduler().runAsync(plugin, runnable);
    }

    @Override
    public void runLater(Runnable runnable, long delay) {
        throw new UnsupportedOperationException("Cannot schedule sync tasks on BungeeCord.");
    }

    @Override
    public void runLaterAsync(Runnable runnable, long delay) {
        ProxyServer.getInstance().getScheduler().schedule(plugin, runnable, delay, TimeUnit.SECONDS);
    }

    @Override
    public void runTimer(Runnable runnable, long delay, long interval) {
        throw new UnsupportedOperationException("Cannot schedule sync tasks on BungeeCord.");
    }

    @Override
    public void runTimerAsync(Runnable runnable, long delay, long interval) {
        ProxyServer.getInstance().getScheduler().schedule(plugin, runnable, delay, interval, TimeUnit.SECONDS);
    }
}
