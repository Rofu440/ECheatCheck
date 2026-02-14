package org.rofu.echeatcheck.util;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Predicate;

public final class Validator {

    private Validator() {
        throw new AssertionError("Нельзя создать экземпляр Validator");
    }

    public static <T> T requireNonNull(@Nullable T obj, @NotNull String message) {
        return Objects.requireNonNull(obj, message);
    }

    @NotNull
    public static String requireNonEmpty(@Nullable String str, @NotNull String message) {
        if (str == null || str.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return str;
    }

    public static long requirePositive(long number, @NotNull String message) {
        if (number <= 0) {
            throw new IllegalArgumentException(message);
        }
        return number;
    }

    public static long requireNonNegative(long number, @NotNull String message) {
        if (number < 0) {
            throw new IllegalArgumentException(message);
        }
        return number;
    }

    public static <T> T requireCondition(@Nullable T obj, @NotNull Predicate<T> condition, @NotNull String message) {
        requireNonNull(obj, "Объект не может быть null");
        if (!condition.test(obj)) {
            throw new IllegalArgumentException(message);
        }
        return obj;
    }

    @NotNull
    public static Player requirePlayer(@NotNull CommandSender sender) {
        if (!(sender instanceof Player)) {
            throw new IllegalStateException("Эта команда доступна только игрокам");
        }
        return (Player) sender;
    }

    public static void requirePermission(@NotNull CommandSender sender, @NotNull String permission) {
        requireNonNull(sender, "Отправитель не может быть null");
        requireNonEmpty(permission, "Разрешение не может быть пустым");

        if (!sender.hasPermission(permission)) {
            throw new IllegalStateException("У вас нет разрешения: " + permission);
        }
    }

    @Nullable
    public static Player findPlayer(@NotNull String playerName) {
        requireNonEmpty(playerName, "Имя игрока не может быть пустым");
        Player player = Bukkit.getPlayerExact(playerName);
        if (player != null && player.isOnline()) {
            return player;
        }

        return null;
    }

    @NotNull
    public static Player requirePlayer(@NotNull String playerName) {
        Player player = findPlayer(playerName);
        if (player == null) {
            throw new IllegalArgumentException("Игрок не найден или не в сети: " + playerName);
        }
        return player;
    }
}