package com.example;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CommandScheduler {
    private static final List<ScheduledAction> TASKS = new ArrayList<>();

    /**
     * Record holding the target world tick and the Java Runnable action to execute.
     */
    private record ScheduledAction(long executeAtTick, Runnable action) {}

    /**
     * Call this inside ModInitializer.onInitialize() to register the tick listener.
     */
    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(CommandScheduler::onServerTick);
    }

    /**
     * Schedule any Java code/method to run after a delay in seconds.
     *
     * @param server The MinecraftServer instance
     * @param delayInSeconds Delay float (e.g., 12.2f)
     * @param action The Java logic to execute (e.g. () -> myModMethod())
     */
    public static void scheduleAction(MinecraftServer server, float delayInSeconds, Runnable action) {
        long currentTicks = server.getOverworld().getTime();
        long delayInTicks = Math.round(delayInSeconds * 25.0f);
        long targetTick = currentTicks + delayInTicks;

        TASKS.add(new ScheduledAction(targetTick, action));
    }

    /**
     * Convenience method to schedule a string command (vanilla or mod custom command).
     *
     * @param server The MinecraftServer instance
     * @param delayInSeconds Delay float (e.g., 12.2f)
     * @param command Command string to run (without leading slash, e.g. "mycmd")
     */
    public static void scheduleCommand(MinecraftServer server, float delayInSeconds, String command) {
        scheduleAction(server, delayInSeconds, () -> {
            server.getCommandManager().executeWithPrefix(
                    server.getCommandSource(),
                    command
            );
        });
    }

    /**
     * Process tasks every server tick.
     */
    private static void onServerTick(MinecraftServer server) {
        if (TASKS.isEmpty()) return;

        long currentTicks = server.getOverworld().getTime();
        Iterator<ScheduledAction> iterator = TASKS.iterator();

        while (iterator.hasNext()) {
            ScheduledAction task = iterator.next();
            if (currentTicks >= task.executeAtTick()) {
                // Execute the scheduled Java Runnable action
                task.action().run();
                iterator.remove(); // Remove task once executed
            }
        }
    }
}