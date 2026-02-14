package org.rofu.echeatcheck.core;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.rofu.echeatcheck.config.ConfigurationManager;
import org.rofu.echeatcheck.util.Validator;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class CheatCheckManager {

    private final JavaPlugin plugin;
    private final ConfigurationManager configManager;
    private final Logger logger;

    private final Map<UUID, CheatCheck> activeChecksBySuspect = new ConcurrentHashMap<>();
    private final Map<UUID, CheatCheck> activeChecksByAdmin = new ConcurrentHashMap<>();
    private final Set<CheatCheck> allActiveChecks = ConcurrentHashMap.newKeySet();

    private final ScheduledExecutorService scheduler;

    private final Map<UUID, Long> suspectCache = new ConcurrentHashMap<>();
    private static final long CACHE_TTL = TimeUnit.MINUTES.toMillis(5);

    public CheatCheckManager(@NotNull JavaPlugin plugin,
                             @NotNull ConfigurationManager configManager) {

        this.plugin = Objects.requireNonNull(plugin, "Плагин не может быть null");
        this.configManager = Objects.requireNonNull(configManager, "Менеджер конфигураций не может быть null");
        this.logger = plugin.getLogger();
        this.scheduler = Executors.newScheduledThreadPool(2);

        startCleanupTask();
        startUpdateTask();
    }

    @NotNull
    public CheatCheck createCheck(@NotNull Player admin, @NotNull Player suspect) {
        Validator.requireNonNull(admin, "Администратор не может быть null");
        Validator.requireNonNull(suspect, "Проверяемый не может быть null");

        if (activeChecksByAdmin.containsKey(admin.getUniqueId())) {
            throw new IllegalStateException("Администратор уже проводит проверку");
        }

        if (activeChecksBySuspect.containsKey(suspect.getUniqueId())) {
            throw new IllegalStateException("Игрок уже находится на проверке");
        }

        if (admin.getUniqueId().equals(suspect.getUniqueId())) {
            throw new IllegalArgumentException("Администратор не может проверить самого себя");
        }

        CheatCheck cheatCheck = new CheatCheck(
                plugin,
                configManager.getPluginConfig(),
                configManager.getTemplatesConfig(),
                admin,
                suspect
        );

        registerCheck(cheatCheck);

        return cheatCheck;
    }

    private void registerCheck(@NotNull CheatCheck cheatCheck) {
        Objects.requireNonNull(cheatCheck, "Проверка не может быть null");
        activeChecksBySuspect.put(cheatCheck.getSuspectId(), cheatCheck);
        activeChecksByAdmin.put(cheatCheck.getAdminId(), cheatCheck);
        allActiveChecks.add(cheatCheck);
        suspectCache.put(cheatCheck.getSuspectId(), System.currentTimeMillis());
    }

    private void unregisterCheck(@NotNull CheatCheck cheatCheck) {
        Objects.requireNonNull(cheatCheck, "Проверка не может быть null");

        activeChecksBySuspect.remove(cheatCheck.getSuspectId());
        activeChecksByAdmin.remove(cheatCheck.getAdminId());
        allActiveChecks.remove(cheatCheck);

        suspectCache.remove(cheatCheck.getSuspectId());
    }

    public void startCheck(@NotNull CheatCheck cheatCheck) {
        Validator.requireNonNull(cheatCheck, "Проверка не может быть null");

        try {
            cheatCheck.start();
        } catch (Exception e) {
            unregisterCheck(cheatCheck);
            throw e;
        }
    }

    public void stopCheck(@NotNull CheatCheck cheatCheck) {
        Validator.requireNonNull(cheatCheck, "Проверка не может быть null");

        cheatCheck.stop();
        unregisterCheck(cheatCheck);
    }

    public void stopAllChecks() {
        List<CheatCheck> checksToStop = new ArrayList<>(allActiveChecks);

        for (CheatCheck check : checksToStop) {
            try {
                stopCheck(check);
            } catch (Exception e) {
                logger.warning("Ошибка при остановке проверки: " + e.getMessage());
            }
        }
    }

    @Nullable
    public CheatCheck getCheckBySuspect(@NotNull Player suspect) {
        Validator.requireNonNull(suspect, "Проверяемый не может быть null");
        return activeChecksBySuspect.get(suspect.getUniqueId());
    }

    @Nullable
    public CheatCheck getCheckBySuspect(@NotNull UUID suspectId) {
        Validator.requireNonNull(suspectId, "ID проверяемого не может быть null");
        return activeChecksBySuspect.get(suspectId);
    }

    @Nullable
    public CheatCheck getCheckByAdmin(@NotNull Player admin) {
        Validator.requireNonNull(admin, "Администратор не может быть null");
        return activeChecksByAdmin.get(admin.getUniqueId());
    }

    @Nullable
    public CheatCheck getCheckByAdmin(@NotNull UUID adminId) {
        Validator.requireNonNull(adminId, "ID администратора не может быть null");
        return activeChecksByAdmin.get(adminId);
    }

    @Nullable
    public CheatCheck getCheckInvolvingPlayer(@NotNull Player player) {
        Validator.requireNonNull(player, "Игрок не может быть null");
        Long cacheTime = suspectCache.get(player.getUniqueId());
        if (cacheTime != null && System.currentTimeMillis() - cacheTime < CACHE_TTL) {
            CheatCheck check = activeChecksBySuspect.get(player.getUniqueId());
            if (check != null) {
                return check;
            }
        }

        CheatCheck check = activeChecksBySuspect.get(player.getUniqueId());
        if (check != null) {
            return check;
        }

        check = activeChecksByAdmin.get(player.getUniqueId());
        if (check != null) {
            return check;
        }

        for (CheatCheck activeCheck : allActiveChecks) {
            if (activeCheck.involvesPlayer(player)) {
                return activeCheck;
            }
        }

        return null;
    }

    public boolean isPlayerInCheck(@NotNull Player player) {
        return getCheckInvolvingPlayer(player) != null;
    }

    public boolean isPlayerSuspect(@NotNull Player player) {
        CheatCheck check = getCheckInvolvingPlayer(player);
        return check != null && check.isSuspect(player);
    }

    public boolean isPlayerAdmin(@NotNull Player player) {
        CheatCheck check = getCheckInvolvingPlayer(player);
        return check != null && check.isAdmin(player);
    }

    @NotNull
    public Collection<CheatCheck> getAllActiveChecks() {
        return Collections.unmodifiableCollection(allActiveChecks);
    }

    public int getActiveChecksCount() {
        return allActiveChecks.size();
    }

    private void startCleanupTask() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                cleanupCache();
            } catch (Exception e) {
                logger.warning("Ошибка при очистке кэша: " + e.getMessage());
            }
        }, 1, 1, TimeUnit.MINUTES);
    }

    private void startUpdateTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (CheatCheck check : allActiveChecks) {
                try {
                    check.update();
                } catch (Exception e) {
                    logger.warning("Ошибка при обновлении проверки " +
                            check.getSuspectName() + ": " + e.getMessage());
                }
            }
        }, 0L, 20L);
    }

    private void cleanupCache() {
        long currentTime = System.currentTimeMillis();
        Iterator<Map.Entry<UUID, Long>> iterator = suspectCache.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<UUID, Long> entry = iterator.next();
            if (currentTime - entry.getValue() > CACHE_TTL) {
                iterator.remove();
            }
        }
    }

    public void shutdown() {
        stopAllChecks();
        scheduler.shutdown();

        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }

        logger.info("Менеджер проверок выключен");
    }

    public boolean isAdminBusy(@NotNull Player admin) {
        return activeChecksByAdmin.containsKey(admin.getUniqueId());
    }

    public boolean isSuspectInCheck(@NotNull Player suspect) {
        return activeChecksBySuspect.containsKey(suspect.getUniqueId());
    }
}