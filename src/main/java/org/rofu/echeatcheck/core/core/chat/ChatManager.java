package org.rofu.echeatcheck.core.core.chat;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.rofu.echeatcheck.config.PluginConfig;
import org.rofu.echeatcheck.util.sound.SoundManager;
import org.rofu.echeatcheck.util.ColorUtil;

import java.util.Objects;

public final class ChatManager {

    private final PluginConfig config;
    private SoundManager soundManager;

    public ChatManager(@NotNull PluginConfig config) {
        this.config = Objects.requireNonNull(config, "Конфигурация не может быть null");
        this.soundManager = Objects.requireNonNull(soundManager, "Менеджер звуков не может быть null");
    }

    public void sendAdminToPlayer(@NotNull Player admin, @NotNull Player player, @NotNull String message) {
        Objects.requireNonNull(admin, "Администратор не может быть null");
        Objects.requireNonNull(player, "Игрок не может быть null");
        Objects.requireNonNull(message, "Сообщение не может быть null");

        if (!admin.isOnline() || !player.isOnline()) {
            return;
        }

        String formattedMessage = formatAdminToPlayer(admin, player, message);
        player.sendMessage(formattedMessage);
        if (config.isChatSoundEnabled()) {
            soundManager.playChatSound(player);
        }
    }

    public void sendPlayerToAdmin(@NotNull Player player, @NotNull Player admin, @NotNull String message) {
        Objects.requireNonNull(player, "Игрок не может быть null");
        Objects.requireNonNull(admin, "Администратор не может быть null");
        Objects.requireNonNull(message, "Сообщение не может быть null");

        if (!player.isOnline() || !admin.isOnline()) {
            return;
        }

        String formattedMessage = formatPlayerToAdmin(player, admin, message);
        admin.sendMessage(formattedMessage);
        if (config.isChatSoundEnabled()) {
            soundManager.playChatSound(admin);
        }
    }

    public void sendSuspectToAdmin(@NotNull Player suspect, @NotNull String message) {
        Objects.requireNonNull(suspect, "Проверяемый не может быть null");
        Objects.requireNonNull(message, "Сообщение не может быть null");
        String formattedMessage = formatSuspectToAdmin(suspect, message);
        for (Player admin : Bukkit.getOnlinePlayers()) {
            if (admin.hasPermission("ecc.admin") && admin.isOnline()) {
                admin.sendMessage(formattedMessage);

                if (config.isChatSoundEnabled()) {
                    soundManager.playChatSound(admin);
                }
            }
        }
    }

    public void sendAdminToSuspect(@NotNull Player admin, @NotNull String message) {
        Objects.requireNonNull(admin, "Администратор не может быть null");
        Objects.requireNonNull(message, "Сообщение не может быть null");
        String formattedMessage = formatAdminToSuspect(admin, message);
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.hasPermission("ecc.admin") && player.isOnline()) {
                player.sendMessage(formattedMessage);

                if (config.isChatSoundEnabled()) {
                    soundManager.playChatSound(player);
                }
            }
        }
    }

    public void sendSystemMessage(@NotNull Player admin, @NotNull Player suspect, @NotNull String message) {
        Objects.requireNonNull(admin, "Администратор не может быть null");
        Objects.requireNonNull(suspect, "Проверяемый не может быть null");
        Objects.requireNonNull(message, "Сообщение не может быть null");

        if (admin.isOnline()) {
            admin.sendMessage(ColorUtil.colorize("&8[&cСистема&8] &7" + message));
        }

        if (suspect.isOnline()) {
            suspect.sendMessage(ColorUtil.colorize("&8[&cСистема&8] &7" + message));
        }
    }

    public void sendAutoMessages(@NotNull Player suspect) {
        Objects.requireNonNull(suspect, "Проверяемый не может быть null");

        if (!config.isAutoMessagesEnabled() || !suspect.isOnline()) {
            return;
        }

        for (String message : config.getAutoMessages()) {
            suspect.sendMessage(ColorUtil.colorize(message));
        }

        if (config.isChatSoundEnabled()) {
            soundManager.playChatSound(suspect);
        }
    }

    @NotNull
    private String formatAdminToPlayer(@NotNull Player admin, @NotNull Player player, @NotNull String message) {
        String format = config.getAdminToPlayerFormat();
        format = format.replace("%admin_name%", admin.getName())
                .replace("%player_name%", player.getName())
                .replace("%message%", message);

        return ColorUtil.colorize(format);
    }

    @NotNull
    private String formatPlayerToAdmin(@NotNull Player player, @NotNull Player admin, @NotNull String message) {
        String format = config.getPlayerToAdminFormat();
        format = format.replace("%player_name%", player.getName())
                .replace("%admin_name%", admin.getName())
                .replace("%message%", message);

        return ColorUtil.colorize(format);
    }

    @NotNull
    private String formatSuspectToAdmin(@NotNull Player suspect, @NotNull String message) {
        String format = config.getSuspectToAdminFormat();
        format = format.replace("%suspect_name%", suspect.getName())
                .replace("%message%", message);

        return ColorUtil.colorize(format);
    }

    @NotNull
    private String formatAdminToSuspect(@NotNull Player admin, @NotNull String message) {
        String format = config.getAdminToSuspectFormat();
        format = format.replace("%admin_name%", admin.getName())
                .replace("%message%", message);

        return ColorUtil.colorize(format);
    }

    public boolean isCommand(@NotNull String message) {
        Objects.requireNonNull(message, "Сообщение не может быть null");
        return message.startsWith("/");
    }

    public boolean isConfession(@NotNull String message) {
        Objects.requireNonNull(message, "Сообщение не может быть null");

        String lowerMessage = message.toLowerCase();
        return lowerMessage.contains("у меня чит") ||
                lowerMessage.contains("использую чит") ||
                lowerMessage.contains("признаюсь") ||
                lowerMessage.contains("читерю");
    }

    @NotNull
    public String processMessage(@NotNull String message) {
        Objects.requireNonNull(message, "Сообщение не может быть null");
        String processed = message.trim();
        if (processed.length() > 256) {
            processed = processed.substring(0, 256) + "...";
        }

        return processed;
    }
}