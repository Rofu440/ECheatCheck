package org.rofu.echeatcheck.core.core.task;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.rofu.echeatcheck.core.CheatCheck;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public final class MovementCheckTask {

    private final JavaPlugin plugin;
    private final CheatCheck cheatCheck;
    private final Logger logger;

    private BukkitTask task;
    private final AtomicBoolean running;
    private final Map<UUID, Location> lastPlayerLocations;
    private final Map<UUID, Long> lastMovementTimes;

    private static final long MOVEMENT_CHECK_INTERVAL = 20L;
    private static final long NO_MOVEMENT_THRESHOLD = 5000L;

    public MovementCheckTask(@NotNull CheatCheck cheatCheck) {
        this.cheatCheck = Objects.requireNonNull(cheatCheck, "Проверка не может быть null");
        this.plugin = cheatCheck.getPlugin();
        this.logger = cheatCheck.getLogger();
        this.running = new AtomicBoolean(false);
        this.lastPlayerLocations = new HashMap<>();
        this.lastMovementTimes = new HashMap<>();
    }

    public void start() {
        if (running.get()) {
            return;
        }

        running.set(true);
        var suspect = cheatCheck.getSuspect();
        if (suspect != null && suspect.isOnline()) {
            lastPlayerLocations.put(suspect.getUniqueId(), suspect.getLocation().clone());
            lastMovementTimes.put(suspect.getUniqueId(), System.currentTimeMillis());
        }

        task = Bukkit.getScheduler().runTaskTimer(plugin, this::run, 0L, MOVEMENT_CHECK_INTERVAL);
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

        lastPlayerLocations.clear();
        lastMovementTimes.clear();
    }

    private void run() {
        if (!running.get()) {
            return;
        }

        try {
            checkSuspectMovement();
        } catch (Exception e) {
            logger.severe("Ошибка в задаче проверки движения для игрока " +
                    cheatCheck.getSuspectName() + ": " + e.getMessage());
            e.printStackTrace();
            stop();
        }
    }

    private void checkSuspectMovement() {
        var suspect = cheatCheck.getSuspect();
        if (suspect == null || !suspect.isOnline()) {
            stop();
            return;
        }

        UUID suspectId = suspect.getUniqueId();
        Location currentLocation = suspect.getLocation();
        Location lastLocation = lastPlayerLocations.get(suspectId);

        if (lastLocation == null) {
            lastPlayerLocations.put(suspectId, currentLocation.clone());
            lastMovementTimes.put(suspectId, System.currentTimeMillis());
            return;
        }

        boolean hasMoved = hasPlayerMoved(lastLocation, currentLocation);

        if (hasMoved) {
            lastPlayerLocations.put(suspectId, currentLocation.clone());
            lastMovementTimes.put(suspectId, System.currentTimeMillis());
            cheatCheck.handlePlayerMove(suspect);
        } else {
            Long lastMovementTime = lastMovementTimes.get(suspectId);
            if (lastMovementTime != null) {
                long timeWithoutMovement = System.currentTimeMillis() - lastMovementTime;

                if (timeWithoutMovement > NO_MOVEMENT_THRESHOLD) {
                    handleNoMovement(suspect, timeWithoutMovement);
                }
            }
        }
    }

    private boolean hasPlayerMoved(@NotNull Location oldLocation, @NotNull Location newLocation) {
        Objects.requireNonNull(oldLocation, "Старая позиция не может быть null");
        Objects.requireNonNull(newLocation, "Новая позиция не может быть null");
        if (!oldLocation.getWorld().equals(newLocation.getWorld())) {
            return true;
        }

        double distance = oldLocation.distance(newLocation);
        return distance > 0.01;
    }

    private void handleNoMovement(@NotNull org.bukkit.entity.Player player, long timeWithoutMovement) {
        Objects.requireNonNull(player, "Игрок не может быть null");
        cheatCheck.getActionBarManager().showCheatCheckActionBar(player);
        cheatCheck.getSoundManager().playTimeWarningSound(player);
    }

    public void updatePlayerLocation(@NotNull org.bukkit.entity.Player player) {
        Objects.requireNonNull(player, "Игрок не может быть null");

        if (!running.get()) {
            return;
        }

        lastPlayerLocations.put(player.getUniqueId(), player.getLocation().clone());
        lastMovementTimes.put(player.getUniqueId(), System.currentTimeMillis());
    }

    public boolean isRunning() {
        return running.get();
    }

    public long getTimeWithoutMovement(@NotNull org.bukkit.entity.Player player) {
        Objects.requireNonNull(player, "Игрок не может быть null");

        Long lastMovementTime = lastMovementTimes.get(player.getUniqueId());
        if (lastMovementTime == null) {
            return 0;
        }

        return System.currentTimeMillis() - lastMovementTime;
    }

    public boolean hasPlayerMovedRecently(@NotNull org.bukkit.entity.Player player, long thresholdMillis) {
        Objects.requireNonNull(player, "Игрок не может быть null");

        Long lastMovementTime = lastMovementTimes.get(player.getUniqueId());
        if (lastMovementTime == null) {
            return false;
        }

        return System.currentTimeMillis() - lastMovementTime < thresholdMillis;
    }
}