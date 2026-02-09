package org.rofu.echeatcheck.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.jetbrains.annotations.NotNull;
import org.rofu.echeatcheck.core.CheatCheck;
import org.rofu.echeatcheck.core.CheatCheckManager;
import org.rofu.echeatcheck.util.ColorUtil;

import java.util.Objects;

public final class PlayerListener implements Listener {

    private final CheatCheckManager checkManager;

    public PlayerListener(@NotNull CheatCheckManager checkManager) {
        this.checkManager = Objects.requireNonNull(checkManager, "Менеджер проверок не может быть null");
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerQuit(@NotNull PlayerQuitEvent event) {
        Player player = event.getPlayer();
        CheatCheck cheatCheck = checkManager.getCheckInvolvingPlayer(player);

        if (cheatCheck != null) {
            cheatCheck.handlePlayerQuit(player);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerMove(@NotNull PlayerMoveEvent event) {
        Player player = event.getPlayer();
        CheatCheck cheatCheck = checkManager.getCheckInvolvingPlayer(player);

        if (cheatCheck != null && cheatCheck.isSuspect(player)) {
            if (hasPlayerMoved(event.getFrom(), event.getTo())) {
                cheatCheck.handlePlayerMove(player);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerChat(@NotNull AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        CheatCheck cheatCheck = checkManager.getCheckInvolvingPlayer(player);
        if (cheatCheck != null) {
            event.setCancelled(true);
            String message = event.getMessage();
            cheatCheck.handleMessage(player, message);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerCommandPreprocess(@NotNull PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        CheatCheck cheatCheck = checkManager.getCheckInvolvingPlayer(player);
        if (cheatCheck != null && cheatCheck.isSuspect(player)) {
            String command = event.getMessage().toLowerCase();
            if (isDangerousCommand(command)) {
                event.setCancelled(true);
                player.sendMessage(ColorUtil.colorize("&cЭта команда заблокирована во время проверки."));
                if (cheatCheck.getSoundManager().isEnabled()) {
                    cheatCheck.getSoundManager().playCheckEndSound(player);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerTeleport(@NotNull PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        CheatCheck cheatCheck = checkManager.getCheckInvolvingPlayer(player);

        if (cheatCheck != null && cheatCheck.isSuspect(player)) {
            if (event.getCause() != PlayerTeleportEvent.TeleportCause.PLUGIN &&
                    event.getCause() != PlayerTeleportEvent.TeleportCause.COMMAND) {

                event.setCancelled(true);
                player.sendMessage(ColorUtil.colorize("&cТелепортация заблокирована во время проверки."));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(@NotNull PlayerInteractEvent event) {
        Player player = event.getPlayer();
        CheatCheck cheatCheck = checkManager.getCheckInvolvingPlayer(player);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDamage(@NotNull PlayerDamageEvent event) {
        Player player = event.getPlayer();
        CheatCheck cheatCheck = checkManager.getCheckInvolvingPlayer(player);

        if (cheatCheck != null && cheatCheck.isSuspect(player)) {
            event.setCancelled(true);
            Player damager = event.getDamager();
            if (damager != null) {
                damager.sendMessage(ColorUtil.colorize("&cНельзя атаковать игрока на проверке."));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDamageByEntity(@NotNull PlayerDamageByEntityEvent event) {
        Player player = event.getPlayer();
        CheatCheck cheatCheck = checkManager.getCheckInvolvingPlayer(player);

        if (cheatCheck != null && cheatCheck.isSuspect(player)) {
            event.setCancelled(true);
        }
    }

    private boolean hasPlayerMoved(@NotNull org.bukkit.Location from, @NotNull org.bukkit.Location to) {
        Objects.requireNonNull(from, "Начальная позиция не может быть null");
        Objects.requireNonNull(to, "Конечная позиция не может быть null");

        if (!from.getWorld().equals(to.getWorld())) {
            return true;
        }

        return from.getX() != to.getX() ||
                from.getY() != to.getY() ||
                from.getZ() != to.getZ() ||
                from.getYaw() != to.getYaw() ||
                from.getPitch() != to.getPitch();
    }

    private boolean isDangerousCommand(@NotNull String command) {
        Objects.requireNonNull(command, "Команда не может быть null");

        String lowerCommand = command.toLowerCase();
        return lowerCommand.startsWith("/tp") ||
                lowerCommand.startsWith("/teleport") ||
                lowerCommand.startsWith("/spawn") ||
                lowerCommand.startsWith("/home") ||
                lowerCommand.startsWith("/warp") ||
                lowerCommand.startsWith("/back") ||
                lowerCommand.startsWith("/kill") ||
                lowerCommand.startsWith("/suicide") ||
                lowerCommand.startsWith("/gamemode") ||
                lowerCommand.startsWith("/fly") ||
                lowerCommand.startsWith("/speed") ||
                lowerCommand.contains("cheat") ||
                lowerCommand.contains("hack");
    }

    private static class PlayerDamageEvent extends org.bukkit.event.player.PlayerEvent {
        private final Player damager;
        private boolean cancelled;

        public PlayerDamageEvent(@NotNull Player player, Player damager) {
            super(player);
            this.damager = damager;
        }

        public Player getDamager() {
            return damager;
        }

        public boolean isCancelled() {
            return cancelled;
        }

        public void setCancelled(boolean cancelled) {
            this.cancelled = cancelled;
        }

        @Override
        public @NotNull HandlerList getHandlers() {
            return null;
        }
    }

    private static class PlayerDamageByEntityEvent extends PlayerDamageEvent {
        private final org.bukkit.entity.Entity damager;

        public PlayerDamageByEntityEvent(@NotNull Player player, org.bukkit.entity.Entity damager) {
            super(player, null);
            this.damager = damager;
        }

        public org.bukkit.entity.Entity getDamagerEntity() {
            return damager;
        }
    }
}