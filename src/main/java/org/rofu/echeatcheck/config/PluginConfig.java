package org.rofu.echeatcheck.config;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class PluginConfig {

    private final FileConfiguration config;

    private int defaultTime;
    private int maxTime;
    private int movementCheckTime;
    private int pingDisplayTime;

    private Location reviseLocation;
    private Location finishLocation;

    private String onQuitCommand;
    private String onTimeoutCommand;

    private boolean chatSoundEnabled;
    private String chatSound;

    private boolean autoMessagesEnabled;
    private int autoMessagesInterval;
    private List<String> autoMessages;

    private String adminToPlayerFormat;
    private String playerToAdminFormat;
    private String suspectToAdminFormat;
    private String adminToSuspectFormat;

    private Map<String, BossBarConfig> bossBarConfigs;

    private String actionBarCheatCheck;

    public static class BossBarConfig {
        private final String text;
        private final String color;

        public BossBarConfig(@NotNull String text, @NotNull String color) {
            this.text = Objects.requireNonNull(text, "Текст боссбара не может быть null");
            this.color = Objects.requireNonNull(color, "Цвет боссбара не может быть null");
        }

        @NotNull
        public String getText() {
            return text;
        }

        @NotNull
        public String getColor() {
            return color;
        }
    }

    public PluginConfig(@NotNull FileConfiguration config) {
        this.config = Objects.requireNonNull(config, "Конфигурация не может быть null");
        load();
    }

    private void load() {
        this.defaultTime = config.getInt("settings.default_time", 180);
        this.maxTime = config.getInt("settings.max_time", 300);
        this.movementCheckTime = config.getInt("settings.movement_check_time", 5);
        this.pingDisplayTime = config.getInt("settings.ping_display_time", 5);

        this.reviseLocation = loadLocation("settings.teleport_revise");
        this.finishLocation = loadLocation("settings.teleport_finish");

        this.onQuitCommand = config.getString("commands.on_quit", "tempban %player% 90d Пункт 4.3.1");
        this.onTimeoutCommand = config.getString("commands.on_timeout", "tempban %player% 90d Пункт 4.3.1");

        this.chatSoundEnabled = config.getBoolean("chat_sound.enabled", true);
        this.chatSound = config.getString("chat_sound.sound", "UI_BUTTON_CLICK");

        this.autoMessagesEnabled = config.getBoolean("auto_messages.enabled", true);
        this.autoMessagesInterval = config.getInt("auto_messages.interval", 15);
        this.autoMessages = config.getStringList("auto_messages.messages");

        this.adminToPlayerFormat = config.getString("chat_format.admin_to_player",
                "&e[&6Вы &7» &6%player_name%&e] &7» &c%message%");
        this.playerToAdminFormat = config.getString("chat_format.player_to_admin",
                "&e[&6%player_name% &7» &6Вы&e] &7» &c%message%");
        this.suspectToAdminFormat = config.getString("chat_format.suspect_to_admin",
                "&e[&6Вы &7» &6Проверяющий&e] &7» &c%message%");
        this.adminToSuspectFormat = config.getString("chat_format.admin_to_suspect",
                "&e[&6Проверяющий&7» &6Вы&e] &7» &c%message%");

        this.bossBarConfigs = loadBossBarConfigs();

        this.actionBarCheatCheck = config.getString("actionbar.cheat_check", "&cДопенифся?)");

        validate();
    }

    @Nullable
    private Location loadLocation(@NotNull String path) {
        ConfigurationSection section = config.getConfigurationSection(path);
        if (section == null) {
            return null;
        }

        String worldName = section.getString("world", "world");
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            return null;
        }

        double x = section.getDouble("x", 0);
        double y = section.getDouble("y", 100);
        double z = section.getDouble("z", 0);
        float yaw = (float) section.getDouble("yaw", 0);
        float pitch = (float) section.getDouble("pitch", 0);

        return new Location(world, x, y, z, yaw, pitch);
    }

    @NotNull
    private Map<String, BossBarConfig> loadBossBarConfigs() {
        Map<String, BossBarConfig> configs = new HashMap<>();
        ConfigurationSection section = config.getConfigurationSection("bossbar");

        if (section != null) {
            for (String key : section.getKeys(false)) {
                ConfigurationSection bossbarSection = section.getConfigurationSection(key);
                if (bossbarSection != null) {
                    String text = bossbarSection.getString("text", "");
                    String color = bossbarSection.getString("color", "RED");
                    configs.put(key, new BossBarConfig(text, color));
                }
            }
        }

        return Collections.unmodifiableMap(configs);
    }

    private void validate() {
        if (defaultTime <= 0) {
            throw new IllegalStateException("default_time должен быть положительным числом");
        }
        if (maxTime <= 0) {
            throw new IllegalStateException("max_time должен быть положительным числом");
        }
        if (defaultTime > maxTime) {
            throw new IllegalStateException("default_time не может быть больше max_time");
        }
        if (movementCheckTime <= 0) {
            throw new IllegalStateException("movement_check_time должен быть положительным числом");
        }
        if (pingDisplayTime <= 0) {
            throw new IllegalStateException("ping_display_time должен быть положительным числом");
        }
        if (autoMessagesInterval <= 0) {
            throw new IllegalStateException("auto_messages.interval должен быть положительным числом");
        }
    }

    public int getDefaultTime() {
        return defaultTime;
    }

    public int getMaxTime() {
        return maxTime;
    }

    public int getMovementCheckTime() {
        return movementCheckTime;
    }

    public int getPingDisplayTime() {
        return pingDisplayTime;
    }

    @Nullable
    public Location getReviseLocation() {
        return reviseLocation != null ? reviseLocation.clone() : null;
    }

    @Nullable
    public Location getFinishLocation() {
        return finishLocation != null ? finishLocation.clone() : null;
    }

    @NotNull
    public String getOnQuitCommand() {
        return onQuitCommand;
    }

    @NotNull
    public String getOnTimeoutCommand() {
        return onTimeoutCommand;
    }

    public boolean isChatSoundEnabled() {
        return chatSoundEnabled;
    }

    @NotNull
    public String getChatSound() {
        return chatSound;
    }

    public boolean isAutoMessagesEnabled() {
        return autoMessagesEnabled;
    }

    public int getAutoMessagesInterval() {
        return autoMessagesInterval;
    }

    @NotNull
    public List<String> getAutoMessages() {
        return new ArrayList<>(autoMessages);
    }

    @NotNull
    public String getAdminToPlayerFormat() {
        return adminToPlayerFormat;
    }

    @NotNull
    public String getPlayerToAdminFormat() {
        return playerToAdminFormat;
    }

    @NotNull
    public String getSuspectToAdminFormat() {
        return suspectToAdminFormat;
    }

    @NotNull
    public String getAdminToSuspectFormat() {
        return adminToSuspectFormat;
    }

    @Nullable
    public BossBarConfig getBossBarConfig(@NotNull String key) {
        return bossBarConfigs.get(key);
    }

    @NotNull
    public String getActionBarCheatCheck() {
        return actionBarCheatCheck;
    }

    public boolean isReviseLocationAvailable() {
        return reviseLocation != null && reviseLocation.getWorld() != null;
    }

    public boolean isFinishLocationAvailable() {
        return finishLocation != null && finishLocation.getWorld() != null;
    }
}