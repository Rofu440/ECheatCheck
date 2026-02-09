package org.rofu.echeatcheck;

import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.rofu.echeatcheck.command.CommandsExecutor;
import org.rofu.echeatcheck.command.CommandHandler;
import org.rofu.echeatcheck.command.TabComplete;
import org.rofu.echeatcheck.config.ConfigurationManager;
import org.rofu.echeatcheck.core.CheatCheckFactory;
import org.rofu.echeatcheck.core.CheatCheckManager;
import org.rofu.echeatcheck.listener.PlayerListener;

import java.util.Objects;
import java.util.logging.Logger;

public final class ECheatCheck extends JavaPlugin {

    private static ECheatCheck instance;

    private ConfigurationManager configManager;
    private CheatCheckManager checkManager;
    private CheatCheckFactory checkFactory;
    private CommandHandler commandHandler;

    @Override
    public void onEnable() {
        instance = this;
        Logger logger = getLogger();
        logger.info("Запуск плагина ECheatCheck...");

        try {
            configManager = new ConfigurationManager(this);
            checkManager = new CheatCheckManager(this, configManager);
            checkFactory = new CheatCheckFactory(this, configManager, checkManager);
            commandHandler = new CommandHandler(logger, checkManager, checkFactory);
            registerCommands();
            registerListeners();
            logger.info("Плагин ECheatCheck успешно запущен!");
        } catch (Exception e) {
            logger.severe("Ошибка при запуске плагина: " + e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("Выключение плагина ECheatCheck...");

        try {
            if (checkManager != null) {
                checkManager.shutdown();
            }

            if (configManager != null) {
                configManager.save();
            }

            getLogger().info("Плагин ECheatCheck успешно выключен");

        } catch (Exception e) {
            getLogger().severe("Ошибка при выключении плагина: " + e.getMessage());
            e.printStackTrace();
        } finally {
            instance = null;
        }
    }

    private void registerCommands() {
        Objects.requireNonNull(getCommand("revise")).setExecutor(new CommandsExecutor(commandHandler));
        Objects.requireNonNull(getCommand("revise")).setTabCompleter(new TabComplete(commandHandler));
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerListener(checkManager), this);
    }

    @NotNull
    public static ECheatCheck getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Плагин еще не инициализирован");
        }
        return instance;
    }

    @NotNull
    public ConfigurationManager getConfigManager() {
        return Objects.requireNonNull(configManager, "Менеджер конфигураций не инициализирован");
    }

    @NotNull
    public CheatCheckManager getCheckManager() {
        return Objects.requireNonNull(checkManager, "Менеджер проверок не инициализирован");
    }

    @NotNull
    public CheatCheckFactory getCheckFactory() {
        return Objects.requireNonNull(checkFactory, "Фабрика проверок не инициализирован");
    }
}