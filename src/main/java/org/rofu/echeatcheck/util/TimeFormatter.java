package org.rofu.echeatcheck.util;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public final class TimeFormatter {

    private TimeFormatter() {
        throw new AssertionError("Нельзя создать экземпляр TimeFormatter");
    }

    @NotNull
    public static String formatSeconds(long seconds) {
        if (seconds <= 0) {
            return "0 сек";
        }

        long days = TimeUnit.SECONDS.toDays(seconds);
        long hours = TimeUnit.SECONDS.toHours(seconds) - TimeUnit.DAYS.toHours(days);
        long minutes = TimeUnit.SECONDS.toMinutes(seconds) - TimeUnit.HOURS.toMinutes(hours) - TimeUnit.DAYS.toMinutes(days);
        long secs = seconds - TimeUnit.MINUTES.toSeconds(minutes) - TimeUnit.HOURS.toSeconds(hours) - TimeUnit.DAYS.toSeconds(days);

        StringBuilder builder = new StringBuilder();

        if (days > 0) {
            builder.append(days).append("д ");
        }
        if (hours > 0) {
            builder.append(hours).append("ч ");
        }
        if (minutes > 0) {
            builder.append(minutes).append("м ");
        }
        if (secs > 0 || builder.length() == 0) {
            builder.append(secs).append("с");
        }

        return builder.toString().trim();
    }

    @NotNull
    public static String formatMMSS(long seconds) {
        long minutes = seconds / 60;
        long secs = seconds % 60;
        return String.format("%02d:%02d", minutes, secs);
    }

    public static long parseTime(@NotNull String timeString) throws IllegalArgumentException {
        if (timeString == null || timeString.trim().isEmpty()) {
            throw new IllegalArgumentException("Строка времени не может быть пустой");
        }

        timeString = timeString.trim().toLowerCase();

        try {
            if (timeString.matches("\\d+")) {
                return Long.parseLong(timeString);
            }

            if (timeString.matches("\\d+[smhd]")) {
                long value = Long.parseLong(timeString.substring(0, timeString.length() - 1));
                char suffix = timeString.charAt(timeString.length() - 1);

                return switch (suffix) {
                    case 's' -> value;
                    case 'm' -> value * 60;
                    case 'h' -> value * 3600;
                    case 'd' -> value * 86400;
                    default -> throw new IllegalArgumentException("Неизвестный суффикс времени: " + suffix);
                };
            }

            throw new IllegalArgumentException("Неверный формат времени: " + timeString);

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Неверный числовой формат: " + timeString, e);
        }
    }
}