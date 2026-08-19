package com.example;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TaskScheduler {

    private static class ScheduledTask {
        int ticksRemaining;
        Runnable action;

        ScheduledTask(int ticks, Runnable action) {
            this.ticksRemaining = ticks;
            this.action = action;
        }
    }

    private static final List<ScheduledTask> TASKS = new ArrayList<>();

    public static void register() {
        // Listens to every server tick (20 ticks = 1 second)
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            Iterator<ScheduledTask> iterator = TASKS.iterator();
            while (iterator.hasNext()) {
                ScheduledTask task = iterator.next();
                task.ticksRemaining--;

                if (task.ticksRemaining <= 0) {
                    task.action.run(); // Run the command action
                    iterator.remove(); // Remove task once complete
                }
            }
        });
    }

    public static void schedule(int delayTicks, Runnable action) {
        TASKS.add(new ScheduledTask(delayTicks, action));
    }
}
