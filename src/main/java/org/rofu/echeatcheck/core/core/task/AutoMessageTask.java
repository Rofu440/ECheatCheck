package org.rofu.echeatcheck.core.core.task;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.rofu.echeatcheck.core.CheatCheck;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public final class AutoMessageTask {

    private final JavaPlugin plugin;
    private final CheatCheck cheatCheck;
    private final Logger logger;

    private BukkitTask task;
    private final AtomicBoolean running;
    private long intervalTicks;
    private int messageIndex;

    public AutoMessageTask(@NotNull CheatCheck cheatCheck) {
        this.cheatCheck = Objects.requireNonNull(cheatCheck, "Проверка не может быть null");
        this.plugin = cheatCheck.getPlugin();
        this.logger = cheatCheck.getLogger();
        this.running = new AtomicBoolean(false);
        this.intervalTicks = 20L * cheatCheck.getConfig().getAutoMessagesInterval();
        this.messageIndex = 0;
    }

    public void start() {
        if (running.get()) {
            return;
        }

        if (!cheatCheck.getConfig().isAutoMessagesEnabled()) {
            return;
        }

        running.set(true);
        messageIndex = 0;

        task = Bukkit.getScheduler().runTaskTimer(plugin, this::run, 0L, intervalTicks);
    }

    public void stop() {
        if (!running.get()) {
            return;
        }

        running.set(false);

        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    private void run() {
        if (!running.get()) {
            return;
        }

        try {
            sendNextAutoMessage();
        } catch (Exception e) {
            logger.severe("Ошибка в задаче автоматических сообщений для игрока " +
                    cheatCheck.getSuspectName() + ": " + e.getMessage());
            e.printStackTrace();
            stop();
        }
    }

    private void sendNextAutoMessage() {
        if (!cheatCheck.getConfig().isAutoMessagesEnabled()) {
            return;
        }

        var messages = cheatCheck.getConfig().getAutoMessages();
        if (messages.isEmpty()) {
            return;
        }

        var suspect = cheatCheck.getSuspect();
        if (suspect == null || !suspect.isOnline()) {
            return;
        }

        if (messageIndex < messages.size()) {
            String message = messages.get(messageIndex);
            suspect.sendMessage(org.rofu.echeatcheck.util.ColorUtil.colorize(message));
            messageIndex++;
        } else {
            messageIndex = 0;
            if (!messages.isEmpty()) {
                String message = messages.get(0);
                suspect.sendMessage(org.rofu.echeatcheck.util.ColorUtil.colorize(message));
                messageIndex++;
            }
        }

        if (cheatCheck.getConfig().isChatSoundEnabled()) {
            cheatCheck.getSoundManager().playChatSound(suspect);
        }
    }

    public void resetMessageIndex() {
        messageIndex = 0;
    }

    public boolean isRunning() {
        return running.get();
    }

    public void setIntervalSeconds(int seconds) {
        if (seconds <= 0) {
            throw new IllegalArgumentException("Интервал должен быть положительным");
        }

        this.intervalTicks = 20L * seconds;
        if (running.get()) {
            stop();
            start();
        }
    }

    public int getIntervalSeconds() {
        return (int) (intervalTicks / 20L);
    }

    public int getMessageIndex() {
        return messageIndex;
    }
}