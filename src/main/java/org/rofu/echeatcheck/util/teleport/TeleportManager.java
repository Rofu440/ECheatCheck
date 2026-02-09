package org.rofu.echeatcheck.util.teleport;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.rofu.echeatcheck.config.PluginConfig;
import org.rofu.echeatcheck.util.sound.SoundManager;
import org.rofu.echeatcheck.util.ColorUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class TeleportManager {

    private final PluginConfig config;
    private final SoundManager soundManager;
    private final Map<UUID, Location> playerOriginalLocations;

    public TeleportManager(@NotNull PluginConfig config) {
        this.config = Objects.requireNonNull(config, "Конфигурация не может быть null");
        this.soundManager = new SoundManager(config);
        this.playerOriginalLocations = new HashMap<>();
    }

    public void teleportToRevise(@NotNull Player player) {
        Objects.requireNonNull(player, "Игрок не может быть null");

        if (!player.isOnline()) {
            return;
        }

        Location reviseLocation = config.getReviseLocation();
        if (reviseLocation == null) {
            player.sendMessage(ColorUtil.colorize("&cТочка телепортации для проверки не настроена."));
            return;
        }

        saveOriginalLocation(player);
        player.teleport(reviseLocation);
        soundManager.playTeleportSound(player);
        player.sendMessage(ColorUtil.colorize("&aВы были телепортированы на точку проверки."));
    }

    public void teleportToFinish(@NotNull Player player) {
        Objects.requireNonNull(player, "Игрок не может быть null");

        if (!player.isOnline()) {
            return;
        }

        Location finishLocation = config.getFinishLocation();
        if (finishLocation == null) {
            returnToOriginalLocation(player);
            return;
        }

        player.teleport(finishLocation);
        soundManager.playTeleportSound(player);
    }

    public void returnToOriginalLocation(@NotNull Player player) {
        Objects.requireNonNull(player, "Игрок не может быть null");

        if (!player.isOnline()) {
            return;
        }

        Location originalLocation = playerOriginalLocations.remove(player.getUniqueId());
        if (originalLocation != null && originalLocation.getWorld() != null) {
            player.teleport(originalLocation);
            soundManager.playTeleportSound(player);
            player.sendMessage(ColorUtil.colorize("&aВы были возвращены на вашу оригинальную позицию."));
        }
    }

    public void saveOriginalLocation(@NotNull Player player) {
        Objects.requireNonNull(player, "Игрок не может быть null");

        if (!player.isOnline()) {
            return;
        }

        playerOriginalLocations.put(player.getUniqueId(), player.getLocation().clone());
    }

    public boolean hasOriginalLocation(@NotNull Player player) {
        Objects.requireNonNull(player, "Игрок не может быть null");
        return playerOriginalLocations.containsKey(player.getUniqueId());
    }

    public Location getOriginalLocation(@NotNull Player player) {
        Objects.requireNonNull(player, "Игрок не может быть null");
        return playerOriginalLocations.get(player.getUniqueId());
    }

    public void clearSavedLocations() {
        playerOriginalLocations.clear();
    }

    public boolean isReviseLocationAvailable() {
        return config.isReviseLocationAvailable();
    }

    public boolean isFinishLocationAvailable() {
        return config.isFinishLocationAvailable();
    }

    public Location getReviseLocation() {
        Location location = config.getReviseLocation();
        return location != null ? location.clone() : null;
    }

    public Location getFinishLocation() {
        Location location = config.getFinishLocation();
        return location != null ? location.clone() : null;
    }

    public boolean isInSafeZone(@NotNull Player player) {
        Objects.requireNonNull(player, "Игрок не может быть null");

        Location playerLocation = player.getLocation();
        Location reviseLocation = config.getReviseLocation();
        Location finishLocation = config.getFinishLocation();

        if (reviseLocation != null && reviseLocation.getWorld() != null &&
                reviseLocation.getWorld().equals(playerLocation.getWorld())) {

            double distance = playerLocation.distance(reviseLocation);
            if (distance < 10.0) {
                return true;
            }
        }

        if (finishLocation != null && finishLocation.getWorld() != null &&
                finishLocation.getWorld().equals(playerLocation.getWorld())) {

            double distance = playerLocation.distance(finishLocation);
            if (distance < 10.0) {
                return true;
            }
        }

        return false;
    }
}