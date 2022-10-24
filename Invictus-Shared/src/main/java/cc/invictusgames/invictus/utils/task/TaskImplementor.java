package cc.invictusgames.invictus.utils.task;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 30.11.2020 / 00:22
 * Invictus / cc.invictusgames.invictus.spigot.utils.task
 */

public interface TaskImplementor {

    void run(Runnable runnable);

    void runAsync(Runnable runnable);

    void runLater(Runnable runnable, long delay);

    void runLaterAsync(Runnable runnable, long delay);

    void runTimer(Runnable runnable, long delay, long interval);

    void runTimerAsync(Runnable runnable, long delay, long interval);

}
