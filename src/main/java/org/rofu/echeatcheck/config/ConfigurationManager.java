package org.rofu.echeatcheck.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.logging.Level;

public final class ConfigurationManager {

    private final JavaPlugin plugin;

    private PluginConfig pluginConfig;
    private MessagesConfig messagesConfig;
    private TemplatesConfig templatesConfig;

    private FileConfiguration config;
    private FileConfiguration messages;
    private FileConfiguration templates;

    private File configFile;
    private File messagesFile;
    private File templatesFile;

    public ConfigurationManager(@NotNull JavaPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "Плагин не может быть null");
        setup();
    }

    private void setup() {
        if (!plugin.getDataFolder().exists()) {
            boolean created = plugin.getDataFolder().mkdirs();
            if (!created) {
                plugin.getLogger().warning("Не удалось создать папку плагина");
            }
        }

        loadConfigs();
    }

    private void loadConfigs() {
        configFile = new File(plugin.getDataFolder(), "config.yml");
        saveDefaultConfig(configFile, "config.yml");
        config = YamlConfiguration.loadConfiguration(configFile);

        messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        saveDefaultConfig(messagesFile, "messages.yml");
        messages = YamlConfiguration.loadConfiguration(messagesFile);

        templatesFile = new File(plugin.getDataFolder(), "templates.yml");
        saveDefaultConfig(templatesFile, "templates.yml");
        templates = YamlConfiguration.loadConfiguration(templatesFile);

        pluginConfig = new PluginConfig(config);
        messagesConfig = new MessagesConfig();
        messagesConfig.loadFromConfig(messages);
        templatesConfig = new TemplatesConfig();
        templatesConfig.loadFromConfig(templates);
    }

    private void saveDefaultConfig(@NotNull File file, @NotNull String resourceName) {
        if (!file.exists()) {
            try (InputStream inputStream = plugin.getResource(resourceName)) {
                if (inputStream != null) {
                    YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(
                            new InputStreamReader(inputStream, StandardCharsets.UTF_8)
                    );
                    defaultConfig.save(file);
                } else {
                    plugin.saveResource(resourceName, false);
                }
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Не удалось сохранить конфигурацию по умолчанию: " + resourceName, e);
            }
        }
    }

    public void reload() {
        try {
            config = YamlConfiguration.loadConfiguration(configFile);
            messages = YamlConfiguration.loadConfiguration(messagesFile);
            templates = YamlConfiguration.loadConfiguration(templatesFile);

            InputStream configStream = plugin.getResource("config.yml");
            InputStream messagesStream = plugin.getResource("messages.yml");
            InputStream templatesStream = plugin.getResource("templates.yml");

            if (configStream != null) {
                config.setDefaults(YamlConfiguration.loadConfiguration(
                        new InputStreamReader(configStream, StandardCharsets.UTF_8)
                ));
            }

            if (messagesStream != null) {
                messages.setDefaults(YamlConfiguration.loadConfiguration(
                        new InputStreamReader(messagesStream, StandardCharsets.UTF_8)
                ));
            }

            if (templatesStream != null) {
                templates.setDefaults(YamlConfiguration.loadConfiguration(
                        new InputStreamReader(templatesStream, StandardCharsets.UTF_8)
                ));
            }

            pluginConfig = new PluginConfig(config);
            messagesConfig = new MessagesConfig();
            messagesConfig.loadFromConfig(messages);
            templatesConfig = new TemplatesConfig();
            templatesConfig.loadFromConfig(templates);

            plugin.getLogger().info("Конфигурации перезагружены");

        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Не удалось перезагрузить конфигурации", e);
        }
    }

    public void save() {
        try {
            if (config != null && configFile != null) {
                config.save(configFile);
            }
            if (messages != null && messagesFile != null) {
                messages.save(messagesFile);
            }
            if (templates != null && templatesFile != null) {
                templates.save(templatesFile);
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Не удалось сохранить конфигурации", e);
        }
    }

    @NotNull
    public PluginConfig getPluginConfig() {
        return pluginConfig;
    }

    @NotNull
    public MessagesConfig getMessagesConfig() {
        return messagesConfig;
    }

    @NotNull
    public TemplatesConfig getTemplatesConfig() {
        return templatesConfig;
    }

    @NotNull
    public FileConfiguration getConfig() {
        return config;
    }

    @NotNull
    public FileConfiguration getMessages() {
        return messages;
    }

    @NotNull
    public FileConfiguration getTemplates() {
        return templates;
    }
}