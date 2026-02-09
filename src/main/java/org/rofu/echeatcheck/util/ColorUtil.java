package org.rofu.echeatcheck.util;

import net.md_5.bungee.api.ChatColor;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ColorUtil {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final Pattern RGB_PATTERN = Pattern.compile("<rgb:([0-9]{1,3}),([0-9]{1,3}),([0-9]{1,3})>");
    private static final Pattern LEGACY_PATTERN = Pattern.compile("&([0-9a-fk-or])");

    private ColorUtil() {
        throw new AssertionError("Нельзя создать экземпляр ColorUtil");
    }

    @NotNull
    public static String colorize(@NotNull String text) {
        if (text == null) {
            return "";
        }

        String result = text;
        result = processRGBFormat(result);
        result = processHEXFormat(result);
        result = processLegacyFormat(result);
        return result;
    }

    @NotNull
    private static String processRGBFormat(@NotNull String text) {
        String result = text;
        Matcher matcher = RGB_PATTERN.matcher(result);

        while (matcher.find()) {
            try {
                int r = Integer.parseInt(matcher.group(1));
                int g = Integer.parseInt(matcher.group(2));
                int b = Integer.parseInt(matcher.group(3));

                r = Math.max(0, Math.min(255, r));
                g = Math.max(0, Math.min(255, g));
                b = Math.max(0, Math.min(255, b));

                ChatColor color = ChatColor.of(new java.awt.Color(r, g, b));
                result = result.replace(matcher.group(), color.toString());
            } catch (NumberFormatException e) {
            }
        }

        return result;
    }

    @NotNull
    private static String processHEXFormat(@NotNull String text) {
        String result = text;
        Matcher matcher = HEX_PATTERN.matcher(result);

        while (matcher.find()) {
            try {
                ChatColor color = ChatColor.of("#" + matcher.group(1));
                result = result.replace(matcher.group(), color.toString());
            } catch (IllegalArgumentException e) {
            }
        }

        return result;
    }

    @NotNull
    private static String processLegacyFormat(@NotNull String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    @NotNull
    public static String stripColor(@NotNull String text) {
        return ChatColor.stripColor(colorize(text));
    }

    public static boolean containsColor(@NotNull String text) {
        return HEX_PATTERN.matcher(text).find() ||
                RGB_PATTERN.matcher(text).find() ||
                LEGACY_PATTERN.matcher(text).find();
    }
}