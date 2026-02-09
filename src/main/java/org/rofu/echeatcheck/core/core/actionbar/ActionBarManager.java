package org.rofu.echeatcheck.core.core.actionbar;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.rofu.echeatcheck.config.PluginConfig;
import org.rofu.echeatcheck.util.ColorUtil;
import org.rofu.echeatcheck.util.TimeFormatter;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class ActionBarManager {

    private final PluginConfig config;
    private final Map<UUID, Long> playerActionBarCooldowns;

    public ActionBarManager(@NotNull PluginConfig config) {
        this.config = Objects.requireNonNull(config, "Конфигурация не может быть null");
        this.playerActionBarCooldowns = new HashMap<>();
    }

    public void showCheatCheckActionBar(@NotNull Player player) {
        Objects.requireNonNull(player, "Игрок не может быть null");

        if (!player.isOnline()) {
            return;
        }

        Long lastShown = playerActionBarCooldowns.get(player.getUniqueId());
        long currentTime = System.currentTimeMillis();

        if (lastShown != null && currentTime - lastShown < 1000) {
            return;
        }

        String message = config.getActionBarCheatCheck();
        sendActionBar(player, message);
        playerActionBarCooldowns.put(player.getUniqueId(), currentTime);
    }

    public void showTimeActionBar(@NotNull Player player, long remainingTime) {
        Objects.requireNonNull(player, "Игрок не может быть null");

        if (!player.isOnline()) {
            return;
        }

        String timeFormatted = TimeFormatter.formatMMSS(remainingTime);
        String message = ColorUtil.colorize("&eОсталось времени: &6" + timeFormatted);
        sendActionBar(player, message);
    }

    public void showFreezeActionBar(@NotNull Player player) {
        Objects.requireNonNull(player, "Игрок не может быть null");

        if (!player.isOnline()) {
            return;
        }

        String message = ColorUtil.colorize("&cПроверка заморожена");
        sendActionBar(player, message);
    }

    public void showTimeWarningActionBar(@NotNull Player player, long remainingTime) {
        Objects.requireNonNull(player, "Игрок не может быть null");

        if (!player.isOnline()) {
            return;
        }

        if (remainingTime <= 60) {
            String timeFormatted = TimeFormatter.formatMMSS(remainingTime);
            String message = ColorUtil.colorize("&cВнимание! Осталось: &6" + timeFormatted);
            sendActionBar(player, message);
        } else if (remainingTime <= 300) {
            String timeFormatted = TimeFormatter.formatMMSS(remainingTime);
            String message = ColorUtil.colorize("&eВремя истекает: &6" + timeFormatted);
            sendActionBar(player, message);
        }
    }

    public void clearActionBar(@NotNull Player player) {
        Objects.requireNonNull(player, "Игрок не может быть null");

        if (!player.isOnline()) {
            return;
        }

        sendActionBar(player, "");
        playerActionBarCooldowns.remove(player.getUniqueId());
    }

    private void sendActionBar(@NotNull Player player, @NotNull String message) {
        Objects.requireNonNull(player, "Игрок не может быть null");
        Objects.requireNonNull(message, "Сообщение не может быть null");

        if (!player.isOnline()) {
            return;
        }

        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                new TextComponent(ColorUtil.colorize(message)));
    }

    public boolean hasCooldown(@NotNull Player player) {
        Objects.requireNonNull(player, "Игрок не может быть null");

        Long lastShown = playerActionBarCooldowns.get(player.getUniqueId());
        if (lastShown == null) {
            return false;
        }

        return System.currentTimeMillis() - lastShown < 1000;
    }

    public void clearAllCooldowns() {
        playerActionBarCooldowns.clear();
    }
}