package org.rofu.echeatcheck.core.core.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
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
                cheatCheck.getSoundManager().playCheckEndSound(player);
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

        if (cheatCheck != null && cheatCheck.isSuspect(player)) {
            if (event.getAction().name().contains("RIGHT_CLICK")) {
                if (event.getItem() != null &&
                        event.getItem().getType().name().contains("ENDER_PEARL")) {
                    event.setCancelled(true);
                    player.sendMessage(ColorUtil.colorize("&cЭндер-перлы заблокированы во время проверки."));
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamageByEntity(org.bukkit.event.entity.EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();
        CheatCheck cheatCheck = checkManager.getCheckInvolvingPlayer(player);

        if (cheatCheck != null && cheatCheck.isSuspect(player)) {
            event.setCancelled(true);
            if (event.getDamager() instanceof Player damager) {
                damager.sendMessage(ColorUtil.colorize("&cНельзя атаковать игрока на проверке."));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamage(org.bukkit.event.entity.EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();
        CheatCheck cheatCheck = checkManager.getCheckInvolvingPlayer(player);

        if (cheatCheck != null && cheatCheck.isSuspect(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(@NotNull PlayerJoinEvent event) {
        Player player = event.getPlayer();
        CheatCheck cheatCheck = checkManager.getCheckBySuspect(player);

        if (cheatCheck != null) {
            player.sendMessage(ColorUtil.colorize("&cВы были возвращены на проверку читов."));
            if (cheatCheck.getTeleportManager().isReviseLocationAvailable()) {
                cheatCheck.getTeleportManager().teleportToRevise(player);
            }

            player.sendMessage(ColorUtil.colorize("&eУ вас осталось времени: &6" +
                    org.rofu.echeatcheck.util.TimeFormatter.formatSeconds(cheatCheck.getRemainingTime())));
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerKick(@NotNull PlayerKickEvent event) {
        Player player = event.getPlayer();
        CheatCheck cheatCheck = checkManager.getCheckInvolvingPlayer(player);

        if (cheatCheck != null) {
            String reason = event.getReason();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(@NotNull PlayerDeathEvent event) {
        Player player = event.getPlayer();
        CheatCheck cheatCheck = checkManager.getCheckInvolvingPlayer(player);

        if (cheatCheck != null && cheatCheck.isSuspect(player)) {
            event.setKeepInventory(true);
            event.setKeepLevel(true);
            event.getDrops().clear();
            event.setDroppedExp(0);
            player.sendMessage(ColorUtil.colorize("&aВаши вещи сохранены, так как вы находитесь на проверке."));
            Bukkit.getScheduler().runTaskLater(checkManager.getAllActiveChecks().iterator().next().getPlugin(), () -> {
                if (player.isOnline()) {
                    cheatCheck.getTeleportManager().teleportToRevise(player);
                }
            }, 20L);
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
                lowerCommand.contains("hack") ||
                lowerCommand.startsWith("/minecraft:") && (
                        lowerCommand.contains("tp") ||
                                lowerCommand.contains("gamemode")
                );
    }
}