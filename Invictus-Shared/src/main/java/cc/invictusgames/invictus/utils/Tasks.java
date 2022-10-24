package cc.invictusgames.invictus.utils;

import cc.invictusgames.invictus.utils.task.TaskImplementor;
import lombok.Setter;

/**
 * @author Emilxyz (langgezockt@gmail.com)
 * 30.11.2020 / 00:22
 * Invictus / cc.invictusgames.invictus.spigot.utils
 */

public class Tasks {

    @Setter
    private static TaskImplementor taskImplementor;

    public static void run(Runnable runnable) {
        taskImplementor.run(runnable);
    }

    public static void runAsync(Runnable runnable) {
        taskImplementor.runAsync(runnable);
    }

    public static void runLater(Runnable runnable, long delay) {
        taskImplementor.runLater(runnable, delay);
    }

    public static void runLaterAsync(Runnable runnable, long delay) {
        taskImplementor.runLaterAsync(runnable, delay);
    }

    public static void runTimer(Runnable runnable, long delay, long interval) {
        taskImplementor.runTimer(runnable, delay, interval);
    }

    public static void runTimerAsync(Runnable runnable, long delay, long interval) {
        taskImplementor.runTimerAsync(runnable, delay, interval);
    }

}
