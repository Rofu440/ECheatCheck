package org.rofu.echeatcheck.core.core.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.rofu.echeatcheck.core.CheatCheck;
import org.rofu.echeatcheck.core.CheatCheckManager;

public class MovementListener implements Listener {

    private final CheatCheckManager checkManager;

    public MovementListener(CheatCheckManager checkManager) {
        this.checkManager = checkManager;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        CheatCheck check = checkManager.getCheckBySuspect(player);

        if (check != null && check.isActive()) {
            event.setCancelled(true);
        }
    }
}