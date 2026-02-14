package org.rofu.echeatcheck.core;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.rofu.echeatcheck.api.event.CheatCheckEvent;
import org.rofu.echeatcheck.api.event.CheckFinishEvent;
import org.rofu.echeatcheck.api.event.CheckStartEvent;
import org.rofu.echeatcheck.api.event.CheckStopEvent;
import org.rofu.echeatcheck.api.state.CheckState;
import org.rofu.echeatcheck.api.state.WaitingState;
import org.rofu.echeatcheck.config.PluginConfig;
import org.rofu.echeatcheck.config.TemplatesConfig;
import org.rofu.echeatcheck.core.core.actionbar.ActionBarManager;
import org.rofu.echeatcheck.core.core.bossbar.BossBarManager;
import org.rofu.echeatcheck.core.core.chat.ChatManager;
import org.rofu.echeatcheck.util.Validator;
import org.rofu.echeatcheck.util.sound.SoundManager;
import org.rofu.echeatcheck.util.teleport.TeleportManager;

import java.util.Objects;
import java.util.UUID;
import java.util.logging.Logger;

public class CheatCheck {

    private final JavaPlugin plugin;
    private final PluginConfig config;
    private final TemplatesConfig templatesConfig;
    private final Logger logger;

    private final UUID adminId;
    private final UUID suspectId;
    private final String suspectName;

    private final BossBarManager bossBarManager;
    private final ChatManager chatManager;
    private final SoundManager soundManager;
    private final TeleportManager teleportManager;
    private final ActionBarManager actionBarManager;

    private CheckState currentState;
    private long remainingTime;
    private boolean active;
    private volatile boolean finishing;

    public CheatCheck(@NotNull JavaPlugin plugin,
                      @NotNull PluginConfig config,
                      @NotNull TemplatesConfig templatesConfig,
                      @NotNull Player admin,
                      @NotNull Player suspect) {

        this.plugin = Objects.requireNonNull(plugin, "Плагин не может быть null");
        this.config = Objects.requireNonNull(config, "Конфигурация не может быть null");
        this.templatesConfig = Objects.requireNonNull(templatesConfig, "Конфигурация шаблонов не может быть null");
        this.logger = plugin.getLogger();

        this.adminId = Objects.requireNonNull(admin, "Администратор не может быть null").getUniqueId();
        this.suspectId = Objects.requireNonNull(suspect, "Проверяемый не может быть null").getUniqueId();
        this.suspectName = suspect.getName();

        this.soundManager = new SoundManager(config);
        this.bossBarManager = new BossBarManager(config);
        this.chatManager = new ChatManager(config,
                soundManager);
        this.teleportManager = new TeleportManager(config);
        this.actionBarManager = new ActionBarManager(config);

        this.remainingTime = config.getDefaultTime();
        this.active = false;
        this.finishing = false;

        this.currentState = new WaitingState(this);
    }

    public void start() {
        if (active) {
            throw new IllegalStateException("Проверка уже активна");
        }

        active = true;

        Player suspect = getSuspect();
        if (suspect != null && suspect.isOnline()) {
            try {
                teleportManager.teleportToRevise(suspect);
            } catch (Exception e) {
                logger.warning("Не удалось телепортировать игрока " + suspectName + ": " + e.getMessage());
            }
        }

        try {
            currentState.onEnter();
        } catch (Exception e) {
            logger.severe("Ошибка при входе в начальное состояние: " + e.getMessage());
            stop();
            throw e;
        }

        CheatCheckEvent event = new CheckStartEvent(this);
        Bukkit.getPluginManager().callEvent(event);
    }

    public void stop() {
        if (!active) {
            return;
        }

        active = false;
        finishing = false;

        try {
            currentState.onExit();
        } catch (Exception e) {
            logger.warning("Ошибка при выходе из состояния: " + e.getMessage());
        }

        Player suspect = getSuspect();
        if (suspect != null && suspect.isOnline()) {
            bossBarManager.hideBossBar(suspect);
            actionBarManager.clearActionBar(suspect);
            soundManager.stopAllSounds(suspect);
            try {
                teleportManager.returnToOriginalLocation(suspect);
            } catch (Exception e) {
                logger.warning("Не удалось вернуть игрока " + suspect.getName() +
                        " на оригинальную позицию: " + e.getMessage());
            }
        }

        Player admin = getAdmin();
        if (admin != null && admin.isOnline()) {
            bossBarManager.hideBossBar(admin);
        }

        CheatCheckEvent event = new CheckStopEvent(this);
        Bukkit.getPluginManager().callEvent(event);
    }

    public void transitionTo(@NotNull CheckState newState) {
        Objects.requireNonNull(newState, "Новое состояние не может быть null");

        try {
            currentState.onExit();
        } catch (Exception e) {
            logger.warning("Ошибка при выходе из состояния " +
                    currentState.getName() + ": " + e.getMessage());
        }

        currentState = newState;

        try {
            currentState.onEnter();
        } catch (Exception e) {
            logger.severe("Ошибка при входе в состояние " +
                    newState.getName() + ": " + e.getMessage());
            stop();
            throw e;
        }
    }

    public void update() {
        if (!active) {
            return;
        }

        try {
            currentState.onUpdate();
        } catch (Exception e) {
            logger.warning("Ошибка при обновлении состояния: " + e.getMessage());
        }
    }

    public void handleMessage(@NotNull Player sender, @NotNull String message) {
        Validator.requireNonNull(sender, "Отправитель не может быть null");
        Validator.requireNonEmpty(message, "Сообщение не может быть пустым");

        if (!active) {
            return;
        }

        try {
            currentState.onMessageReceived(sender, message);
        } catch (Exception e) {
            logger.warning("Ошибка при обработке сообщения от " +
                    sender.getName() + ": " + e.getMessage());
        }
    }

    public void handlePlayerMove(@NotNull Player player) {
        Validator.requireNonNull(player, "Игрок не может быть null");

        if (!active) {
            return;
        }

        try {
            currentState.onPlayerMove(player);
        } catch (Exception e) {
            logger.warning("Ошибка при обработке движения игрока " +
                    player.getName() + ": " + e.getMessage());
        }
    }

    public void handlePlayerQuit(@NotNull Player player) {
        Validator.requireNonNull(player, "Игрок не может быть null");

        if (!active) {
            return;
        }

        try {
            currentState.onPlayerQuit(player);
        } catch (Exception e) {
            logger.warning("Ошибка при обработке выхода игрока " +
                    player.getName() + ": " + e.getMessage());
        }
    }

    public void addTime(long seconds) {
        Validator.requirePositive(seconds, "Время должно быть положительным");

        if (!active) {
            throw new IllegalStateException("Проверка не активна");
        }

        try {
            currentState.onTimeAdded(seconds);
        } catch (Exception e) {
            logger.warning("Ошибка при добавлении времени: " + e.getMessage());
            throw e;
        }
    }

    public void freeze() {
        if (!active) {
            throw new IllegalStateException("Проверка не активна");
        }

        try {
            currentState.onFreeze();
        } catch (Exception e) {
            logger.warning("Ошибка при заморозке/разморозке: " + e.getMessage());
            throw e;
        }
    }

    public void finish(@NotNull String reason) {
        Validator.requireNonEmpty(reason, "Причина не может быть пустой");

        if (!active) {
            throw new IllegalStateException("Проверка не активна");
        }

        if (finishing) {
            logger.warning("Попытка повторного завершения проверки игрока " + suspectName);
            return;
        }

        finishing = true;

        try {
            Bukkit.getPluginManager().callEvent(
                    new CheckFinishEvent(this, reason)
            );

            currentState.onFinish(reason);

        } catch (Exception e) {
            logger.severe("Критическая ошибка при завершении проверки: " + e.getMessage());
            finishing = false;
            throw e;
        }
    }

    @NotNull
    public JavaPlugin getPlugin() {
        return plugin;
    }

    @NotNull
    public PluginConfig getConfig() {
        return config;
    }

    @NotNull
    public TemplatesConfig getTemplatesConfig() {
        return templatesConfig;
    }

    @NotNull
    public Logger getLogger() {
        return logger;
    }

    @Nullable
    public Player getAdmin() {
        return Bukkit.getPlayer(adminId);
    }

    @NotNull
    public String getAdminName() {
        Player admin = getAdmin();
        return admin != null ? admin.getName() : "Offline";
    }

    @Nullable
    public Player getSuspect() {
        return Bukkit.getPlayer(suspectId);
    }

    @NotNull
    public String getSuspectName() {
        return suspectName;
    }

    @NotNull
    public UUID getAdminId() {
        return adminId;
    }

    @NotNull
    public UUID getSuspectId() {
        return suspectId;
    }

    @NotNull
    public BossBarManager getBossBarManager() {
        return bossBarManager;
    }

    @NotNull
    public ChatManager getChatManager() {
        return chatManager;
    }

    @NotNull
    public SoundManager getSoundManager() {
        return soundManager;
    }

    @NotNull
    public TeleportManager getTeleportManager() {
        return teleportManager;
    }

    @NotNull
    public ActionBarManager getActionBarManager() {
        return actionBarManager;
    }

    @NotNull
    public CheckState getCurrentState() {
        return currentState;
    }

    public long getRemainingTime() {
        return remainingTime;
    }

    public void setRemainingTime(long remainingTime) {
        this.remainingTime = remainingTime;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isFinishing() {
        return finishing;
    }

    public boolean involvesPlayer(@NotNull Player player) {
        Validator.requireNonNull(player, "Игрок не может быть null");
        return player.getUniqueId().equals(adminId) ||
                player.getUniqueId().equals(suspectId);
    }

    public boolean isSuspect(@NotNull Player player) {
        Validator.requireNonNull(player, "Игрок не может быть null");
        return player.getUniqueId().equals(suspectId);
    }

    public boolean isAdmin(@NotNull Player player) {
        Validator.requireNonNull(player, "Игрок не может быть null");
        return player.getUniqueId().equals(adminId);
    }

}