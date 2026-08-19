package com.example;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RealTimeScheduler {
    private static final List<RealTimeAction> TASKS = new ArrayList<>();

    // Store target time in real-world Unix milliseconds instead of world ticks
    private record RealTimeAction(long executeAtMs, Runnable action) {}

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(RealTimeScheduler::onServerTick);
    }

    public static void scheduleAction(float delayInSeconds, Runnable action) {
        long targetMs = System.currentTimeMillis() + Math.round(delayInSeconds * 1000.0f);
        TASKS.add(new RealTimeAction(targetMs, action));
    }

    public static void clearTasks(){
        TASKS.clear();
    }

    public static void shiftAllTasks(float delayInSeconds) {
        // Clamp the offset to strictly enforce the [-1.0, 1.0] bounds
        float clampedDelay = Math.max(-1.0f, Math.min(1.0f, delayInSeconds));
        long offsetMs = Math.round(clampedDelay * 1000.0f);

        // Replace each record with a new instance containing the shifted time
        for (int i = 0; i < TASKS.size(); i++) {
            RealTimeAction currentTask = TASKS.get(i);
            long newExecuteAtMs = currentTask.executeAtMs() + offsetMs;

            TASKS.set(i, new RealTimeAction(newExecuteAtMs, currentTask.action()));
        }
    }

    private static void onServerTick(MinecraftServer server) {
        if (TASKS.isEmpty()) return;

        long currentMs = System.currentTimeMillis();
        Iterator<RealTimeAction> iterator = TASKS.iterator();

        while (iterator.hasNext()) {
            RealTimeAction task = iterator.next();
            if (currentMs >= task.executeAtMs()) {
                task.action().run();
                iterator.remove();
            }
        }
    }
}