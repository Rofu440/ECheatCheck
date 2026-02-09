package org.rofu.echeatcheck.util.sound;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.rofu.echeatcheck.config.PluginConfig;

import java.util.Objects;

public final class SoundManager {

    private final PluginConfig config;

    public SoundManager(@NotNull PluginConfig config) {
        this.config = Objects.requireNonNull(config, "Конфигурация не может быть null");
    }

    public void playChatSound(@NotNull Player player) {
        Objects.requireNonNull(player, "Игрок не может быть null");

        if (!config.isChatSoundEnabled() || !player.isOnline()) {
            return;
        }

        try {
            Sound sound = parseSound(config.getChatSound());
            player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
        } catch (IllegalArgumentException e) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
        }
    }

    public void playCheckStartSound(@NotNull Player player) {
        Objects.requireNonNull(player, "Игрок не может быть null");

        if (!player.isOnline()) {
            return;
        }

        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.5f);
    }

    public void playCheckEndSound(@NotNull Player player) {
        Objects.requireNonNull(player, "Игрок не может быть null");

        if (!player.isOnline()) {
            return;
        }

        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
    }

    public void playFreezeSound(@NotNull Player player) {
        Objects.requireNonNull(player, "Игрок не может быть null");

        if (!player.isOnline()) {
            return;
        }

        player.playSound(player.getLocation(), Sound.BLOCK_GLASS_BREAK, 0.5f, 1.0f);
    }

    public void playUnfreezeSound(@NotNull Player player) {
        Objects.requireNonNull(player, "Игрок не может быть null");

        if (!player.isOnline()) {
            return;
        }

        player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 0.5f, 1.0f);
    }

    public void playTimeWarningSound(@NotNull Player player) {
        Objects.requireNonNull(player, "Игрок не может быть null");

        if (!player.isOnline()) {
            return;
        }

        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1.0f, 2.0f);
    }

    public void playCriticalTimeSound(@NotNull Player player) {
        Objects.requireNonNull(player, "Игрок не может быть null");

        if (!player.isOnline()) {
            return;
        }

        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 0.5f);
    }

    public void playTeleportSound(@NotNull Player player) {
        Objects.requireNonNull(player, "Игрок не может быть null");

        if (!player.isOnline()) {
            return;
        }

        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
    }

    @NotNull
    private Sound parseSound(@NotNull String soundName) {
        Objects.requireNonNull(soundName, "Имя звука не может быть null");

        try {
            return Sound.valueOf(soundName.toUpperCase());
        } catch (IllegalArgumentException e) {
            for (Sound sound : Sound.values()) {
                if (sound.name().equalsIgnoreCase(soundName)) {
                    return sound;
                }
            }

            throw new IllegalArgumentException("Звук не найден: " + soundName);
        }
    }

    public boolean isEnabled() {
        return config.isChatSoundEnabled();
    }

    public void stopAllSounds(@NotNull Player player) {
        Objects.requireNonNull(player, "Игрок не может быть null");

        if (!player.isOnline()) {
            return;
        }

        player.stopAllSounds();
    }
}