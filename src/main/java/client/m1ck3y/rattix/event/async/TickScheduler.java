package client.m1ck3y.rattix.event.async;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Asynchronous tick task scheduler.
 * Aligned with LiquidBounce event/async/TickScheduler.
 */
public class TickScheduler {
    private static final Queue<Runnable> SCHEDULED_TASKS = new ConcurrentLinkedQueue<>();

    public static void schedule(Runnable runnable) {
        if (runnable != null) {
            SCHEDULED_TASKS.add(runnable);
        }
    }

    public static void onTick() {
        Runnable task;
        while ((task = SCHEDULED_TASKS.poll()) != null) {
            try {
                task.run();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }
}
