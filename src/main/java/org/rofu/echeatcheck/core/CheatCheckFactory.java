package org.rofu.echeatcheck.core;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.rofu.echeatcheck.config.ConfigurationManager;

import java.util.Objects;

public final class CheatCheckFactory {

    private final JavaPlugin plugin;
    private final ConfigurationManager configManager;
    private final CheatCheckManager checkManager;

    public CheatCheckFactory(@NotNull JavaPlugin plugin,
                             @NotNull ConfigurationManager configManager,
                             @NotNull CheatCheckManager checkManager) {

        this.plugin = Objects.requireNonNull(plugin, "Плагин не может быть null");
        this.configManager = Objects.requireNonNull(configManager, "Менеджер конфигураций не может быть null");
        this.checkManager = Objects.requireNonNull(checkManager, "Менеджер проверок не может быть null");
    }

    @NotNull
    public CheatCheck createAndStartCheck(@NotNull Player admin, @NotNull Player suspect) {
        Objects.requireNonNull(admin, "Администратор не может быть null");
        Objects.requireNonNull(suspect, "Проверяемый не может быть null");
        CheatCheck cheatCheck = checkManager.createCheck(admin, suspect);
        checkManager.startCheck(cheatCheck);
        return cheatCheck;
    }

    @NotNull
    public CheatCheck createCheckWithTime(@NotNull Player admin,
                                          @NotNull Player suspect,
                                          int timeSeconds) {

        Objects.requireNonNull(admin, "Администратор не может быть null");
        Objects.requireNonNull(suspect, "Проверяемый не может быть null");

        if (timeSeconds <= 0) {
            throw new IllegalArgumentException("Время должно быть положительным");
        }

        int maxTime = configManager.getPluginConfig().getMaxTime();
        if (timeSeconds > maxTime) {
            throw new IllegalArgumentException("Время не может превышать " + maxTime + " секунд");
        }

        CheatCheck cheatCheck = checkManager.createCheck(admin, suspect);

        cheatCheck.setRemainingTime(timeSeconds);

        checkManager.startCheck(cheatCheck);

        return cheatCheck;
    }
}