package org.rofu.echeatcheck.core.core.bossbar;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.rofu.echeatcheck.config.PluginConfig;
import org.rofu.echeatcheck.util.ColorUtil;
import org.rofu.echeatcheck.util.TimeFormatter;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class BossBarManager {

    private final PluginConfig config;
    private final Map<UUID, BossBar> playerBossBars;
    private final Map<UUID, BossBarType> playerBossBarTypes;

    public BossBarManager(@NotNull PluginConfig config) {
        this.config = Objects.requireNonNull(config, "Конфигурация не может быть null");
        this.playerBossBars = new HashMap<>();
        this.playerBossBarTypes = new HashMap<>();
    }

    public void showWaitingBossBar(@NotNull Player player) {
        Objects.requireNonNull(player, "Игрок не может быть null");

        PluginConfig.BossBarConfig bossBarConfig = config.getBossBarConfig("waiting");
        if (bossBarConfig == null) {
            return;
        }

        String title = ColorUtil.colorize(bossBarConfig.getText());
        BarColor color = parseBarColor(bossBarConfig.getColor());

        BossBar bossBar = Bukkit.createBossBar(title, color, BarStyle.SOLID);
        bossBar.setProgress(1.0);
        bossBar.addPlayer(player);

        updatePlayerBossBar(player, bossBar, BossBarType.WAITING);
    }

    public void showAdminTimerBossBar(@NotNull Player admin, long remainingTime) {
        Objects.requireNonNull(admin, "Администратор не может быть null");

        PluginConfig.BossBarConfig bossBarConfig = config.getBossBarConfig("timer_admin");
        if (bossBarConfig == null) {
            return;
        }

        String timeFormatted = TimeFormatter.formatMMSS(remainingTime);
        String title = ColorUtil.colorize(bossBarConfig.getText().replace("%time%", timeFormatted));
        BarColor color = parseBarColor(bossBarConfig.getColor());

        BossBar bossBar = Bukkit.createBossBar(title, color, BarStyle.SOLID);
        updateTimerBossBar(bossBar, remainingTime, config.getMaxTime());
        bossBar.addPlayer(admin);

        updatePlayerBossBar(admin, bossBar, BossBarType.TIMER_ADMIN);
    }

    public void showPlayerTimerBossBar(@NotNull Player player, long remainingTime) {
        Objects.requireNonNull(player, "Игрок не может быть null");

        PluginConfig.BossBarConfig bossBarConfig = config.getBossBarConfig("timer_player");
        if (bossBarConfig == null) {
            return;
        }

        String timeFormatted = TimeFormatter.formatMMSS(remainingTime);
        String title = ColorUtil.colorize(bossBarConfig.getText().replace("%time%", timeFormatted));
        BarColor color = parseBarColor(bossBarConfig.getColor());

        BossBar bossBar = Bukkit.createBossBar(title, color, BarStyle.SOLID);
        updateTimerBossBar(bossBar, remainingTime, config.getMaxTime());
        bossBar.addPlayer(player);

        updatePlayerBossBar(player, bossBar, BossBarType.TIMER_PLAYER);
    }

    public void showFrozenAdminBossBar(@NotNull Player admin) {
        Objects.requireNonNull(admin, "Администратор не может быть null");

        PluginConfig.BossBarConfig bossBarConfig = config.getBossBarConfig("frozen_admin");
        if (bossBarConfig == null) {
            return;
        }

        String title = ColorUtil.colorize(bossBarConfig.getText());
        BarColor color = parseBarColor(bossBarConfig.getColor());

        BossBar bossBar = Bukkit.createBossBar(title, color, BarStyle.SOLID);
        bossBar.setProgress(1.0);
        bossBar.addPlayer(admin);

        updatePlayerBossBar(admin, bossBar, BossBarType.FROZEN_ADMIN);
    }

    public void showFrozenPlayerBossBar(@NotNull Player player) {
        Objects.requireNonNull(player, "Игрок не может быть null");

        PluginConfig.BossBarConfig bossBarConfig = config.getBossBarConfig("frozen_player");
        if (bossBarConfig == null) {
            return;
        }

        String title = ColorUtil.colorize(bossBarConfig.getText());
        BarColor color = parseBarColor(bossBarConfig.getColor());

        BossBar bossBar = Bukkit.createBossBar(title, color, BarStyle.SOLID);
        bossBar.setProgress(1.0);
        bossBar.addPlayer(player);

        updatePlayerBossBar(player, bossBar, BossBarType.FROZEN_PLAYER);
    }

    public void updateAdminTimerBossBar(@NotNull Player admin, long remainingTime) {
        Objects.requireNonNull(admin, "Администратор не может быть null");

        BossBar bossBar = playerBossBars.get(admin.getUniqueId());
        if (bossBar == null || playerBossBarTypes.get(admin.getUniqueId()) != BossBarType.TIMER_ADMIN) {
            showAdminTimerBossBar(admin, remainingTime);
            return;
        }

        PluginConfig.BossBarConfig bossBarConfig = config.getBossBarConfig("timer_admin");
        if (bossBarConfig == null) {
            return;
        }

        String timeFormatted = TimeFormatter.formatMMSS(remainingTime);
        String title = ColorUtil.colorize(bossBarConfig.getText().replace("%time%", timeFormatted));
        bossBar.setTitle(title);

        updateTimerBossBar(bossBar, remainingTime, config.getMaxTime());
    }

    public void updatePlayerTimerBossBar(@NotNull Player player, long remainingTime) {
        Objects.requireNonNull(player, "Игрок не может быть null");

        BossBar bossBar = playerBossBars.get(player.getUniqueId());
        if (bossBar == null || playerBossBarTypes.get(player.getUniqueId()) != BossBarType.TIMER_PLAYER) {
            showPlayerTimerBossBar(player, remainingTime);
            return;
        }

        PluginConfig.BossBarConfig bossBarConfig = config.getBossBarConfig("timer_player");
        if (bossBarConfig == null) {
            return;
        }

        String timeFormatted = TimeFormatter.formatMMSS(remainingTime);
        String title = ColorUtil.colorize(bossBarConfig.getText().replace("%time%", timeFormatted));
        bossBar.setTitle(title);

        updateTimerBossBar(bossBar, remainingTime, config.getMaxTime());
    }

    public void updateFrozenAdminBossBar(@NotNull Player admin) {
        Objects.requireNonNull(admin, "Администратор не может быть null");

        BossBar bossBar = playerBossBars.get(admin.getUniqueId());
        if (bossBar == null || playerBossBarTypes.get(admin.getUniqueId()) != BossBarType.FROZEN_ADMIN) {
            showFrozenAdminBossBar(admin);
            return;
        }

        PluginConfig.BossBarConfig bossBarConfig = config.getBossBarConfig("frozen_admin");
        if (bossBarConfig == null) {
            return;
        }

        String title = ColorUtil.colorize(bossBarConfig.getText());
        bossBar.setTitle(title);
    }

    public void updateFrozenPlayerBossBar(@NotNull Player player) {
        Objects.requireNonNull(player, "Игрок не может быть null");

        BossBar bossBar = playerBossBars.get(player.getUniqueId());
        if (bossBar == null || playerBossBarTypes.get(player.getUniqueId()) != BossBarType.FROZEN_PLAYER) {
            showFrozenPlayerBossBar(player);
            return;
        }

        PluginConfig.BossBarConfig bossBarConfig = config.getBossBarConfig("frozen_player");
        if (bossBarConfig == null) {
            return;
        }

        String title = ColorUtil.colorize(bossBarConfig.getText());
        bossBar.setTitle(title);
    }

    public void hideBossBar(@NotNull Player player) {
        Objects.requireNonNull(player, "Игрок не может быть null");

        BossBar bossBar = playerBossBars.remove(player.getUniqueId());
        playerBossBarTypes.remove(player.getUniqueId());

        if (bossBar != null) {
            bossBar.removePlayer(player);
            bossBar.setVisible(false);
        }
    }

    public void clearAllBossBars() {
        for (BossBar bossBar : playerBossBars.values()) {
            bossBar.removeAll();
            bossBar.setVisible(false);
        }

        playerBossBars.clear();
        playerBossBarTypes.clear();
    }

    private void updateTimerBossBar(@NotNull BossBar bossBar, long remainingTime, long maxTime) {
        Objects.requireNonNull(bossBar, "Боссбар не может быть null");

        if (maxTime <= 0) {
            bossBar.setProgress(1.0);
            return;
        }

        double progress = Math.max(0.0, Math.min(1.0, (double) remainingTime / maxTime));
        bossBar.setProgress(progress);

        if (progress > 0.5) {
            bossBar.setColor(BarColor.GREEN);
        } else if (progress > 0.25) {
            bossBar.setColor(BarColor.YELLOW);
        } else {
            bossBar.setColor(BarColor.RED);
        }
    }

    private void updatePlayerBossBar(@NotNull Player player, @NotNull BossBar bossBar, @NotNull BossBarType type) {
        Objects.requireNonNull(player, "Игрок не может быть null");
        Objects.requireNonNull(bossBar, "Боссбар не может быть null");
        Objects.requireNonNull(type, "Тип боссбара не может быть null");

        BossBar oldBossBar = playerBossBars.get(player.getUniqueId());
        if (oldBossBar != null) {
            oldBossBar.removePlayer(player);
        }

        playerBossBars.put(player.getUniqueId(), bossBar);
        playerBossBarTypes.put(player.getUniqueId(), type);
        bossBar.setVisible(true);
    }

    @NotNull
    private BarColor parseBarColor(@NotNull String colorString) {
        Objects.requireNonNull(colorString, "Строка цвета не может быть null");

        try {
            return BarColor.valueOf(colorString.toUpperCase());
        } catch (IllegalArgumentException e) {
            return BarColor.RED;
        }
    }

}