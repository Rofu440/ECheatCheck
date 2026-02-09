package org.rofu.echeatcheck.core.core.task;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.rofu.echeatcheck.core.CheatCheck;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public final class TimerTask {

    private final JavaPlugin plugin;
    private final CheatCheck cheatCheck;
    private final Logger logger;

    private BukkitTask task;
    private final AtomicBoolean running;
    private final AtomicBoolean paused;
    private long intervalTicks;

    public TimerTask(@NotNull CheatCheck cheatCheck) {
        this.cheatCheck = Objects.requireNonNull(cheatCheck, "Проверка не может быть null");
        this.plugin = cheatCheck.getPlugin();
        this.logger = cheatCheck.getLogger();
        this.running = new AtomicBoolean(false);
        this.paused = new AtomicBoolean(false);
        this.intervalTicks = 20L;
    }

    public void start() {
        if (running.get()) {
            return;
        }

        running.set(true);
        paused.set(false);

        task = Bukkit.getScheduler().runTaskTimer(plugin, this::run, 0L, intervalTicks);

        logger.info("Таймер проверки для игрока " +
                cheatCheck.getSuspectName() + " запущен");
    }

    public void stop() {
        if (!running.get()) {
            return;
        }

        running.set(false);
        paused.set(false);

        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    public void pause() {
        if (!running.get()) {
            return;
        }

        paused.set(true);
    }

    public void resume() {
        if (!running.get()) {
            return;
        }

        paused.set(false);
    }

    private void run() {
        if (!running.get() || paused.get()) {
            return;
        }

        try {
            cheatCheck.update();
            checkTimeWarnings();
        } catch (Exception e) {
            logger.severe("Ошибка в задаче таймера для игрока " +
                    cheatCheck.getSuspectName() + ": " + e.getMessage());
            e.printStackTrace();
            stop();
        }
    }

    private void checkTimeWarnings() {
        long remainingTime = cheatCheck.getRemainingTime();

        if (remainingTime <= 0) {
            return;
        }

        if (remainingTime == 60 || remainingTime == 30 ||
                remainingTime <= 10 && remainingTime >= 1) {
            cheatCheck.getActionBarManager().showTimeWarningActionBar(
                    cheatCheck.getSuspect(), remainingTime);
            if (remainingTime <= 10) {
                cheatCheck.getSoundManager().playCriticalTimeSound(cheatCheck.getSuspect());
            } else {
                cheatCheck.getSoundManager().playTimeWarningSound(cheatCheck.getSuspect());
            }
        }
    }

    public boolean isRunning() {
        return running.get();
    }

    public boolean isPaused() {
        return paused.get();
    }

    public void setIntervalTicks(long intervalTicks) {
        if (intervalTicks <= 0) {
            throw new IllegalArgumentException("Интервал должен быть положительным");
        }

        this.intervalTicks = intervalTicks;

        if (running.get()) {
            stop();
            start();
        }
    }

    public long getIntervalTicks() {
        return intervalTicks;
    }
}