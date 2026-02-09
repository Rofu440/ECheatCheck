package org.rofu.echeatcheck.api.state;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.rofu.echeatcheck.core.CheatCheck;
import org.rofu.echeatcheck.core.core.task.MovementCheckTask;
import org.rofu.echeatcheck.util.ColorUtil;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class WaitingState extends CheckState {

    private final MovementCheckTask movementCheckTask;
    private long startTime;
    private boolean movementDetected;

    public WaitingState(@NotNull CheatCheck cheatCheck) {
        super(Objects.requireNonNull(cheatCheck, "CheatCheck не может быть null"));
        this.movementCheckTask = new MovementCheckTask(cheatCheck);
        this.movementDetected = false;
    }

    @Override
    public void onEnter() {
        this.startTime = System.currentTimeMillis();
        this.movementDetected = false;

        Player suspect = cheatCheck.getSuspect();
        if (suspect != null && suspect.isOnline()) {
            String message = ColorUtil.colorize("&cВы были вызваны на проверку читов. " +
                    "Пожалуйста, не двигайтесь в течение нескольких секунд.");
            suspect.sendMessage(message);
            cheatCheck.getBossBarManager().showWaitingBossBar(suspect);
        }

        movementCheckTask.start();

        cheatCheck.getLogger().info("Проверка игрока " +
                cheatCheck.getSuspectName() + " перешла в состояние ожидания");
    }

    @Override
    public void onExit() {
        movementCheckTask.stop();
        Player suspect = cheatCheck.getSuspect();
        if (suspect != null && suspect.isOnline()) {
            cheatCheck.getBossBarManager().hideBossBar(suspect);
        }

        cheatCheck.getLogger().info("Проверка игрока " +
                cheatCheck.getSuspectName() + " вышла из состояния ожидания");
    }

    @Override
    public void onUpdate() {
        long elapsedTime = System.currentTimeMillis() - startTime;
        long maxWaitTime = TimeUnit.SECONDS.toMillis(cheatCheck.getConfig().getMovementCheckTime());

        if (elapsedTime >= maxWaitTime && !movementDetected) {
            cheatCheck.transitionTo(new ActiveState(cheatCheck));
        }
    }

    @Override
    public void onMessageReceived(@NotNull Player sender, @NotNull String message) {
        if (sender.equals(cheatCheck.getSuspect())) {
            cheatCheck.getChatManager().sendSuspectToAdmin(sender, message);
        } else if (sender.equals(cheatCheck.getAdmin())) {
            cheatCheck.getChatManager().sendAdminToSuspect(sender, message);
        }
    }

    @Override
    public void onPlayerMove(@NotNull Player player) {
        Objects.requireNonNull(player, "Игрок не может быть null");

        if (player.equals(cheatCheck.getSuspect())) {
            movementDetected = true;
            cheatCheck.transitionTo(new ActiveState(cheatCheck));
        }
    }

    @Override
    public void onPlayerQuit(@NotNull Player player) {
        Objects.requireNonNull(player, "Игрок не может быть null");

        if (player.equals(cheatCheck.getSuspect())) {
            String command = cheatCheck.getConfig().getOnQuitCommand()
                    .replace("%player%", player.getName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            cheatCheck.stop();
        }
    }

    @Override
    public void onTimeAdded(long seconds) {
        Player admin = cheatCheck.getAdmin();
        if (admin != null && admin.isOnline()) {
            admin.sendMessage(ColorUtil.colorize("&cНельзя добавить время в состоянии ожидания"));
        }
    }

    @Override
    public void onFreeze() {
        Player admin = cheatCheck.getAdmin();
        if (admin != null && admin.isOnline()) {
            admin.sendMessage(ColorUtil.colorize("&cНельзя заморозить время в состоянии ожидания"));
        }
    }

    @Override
    public void onFinish(@NotNull String reason) {
        Objects.requireNonNull(reason, "Причина не может быть null");
        cheatCheck.transitionTo(new FinishedState(cheatCheck, reason));
    }

    @Override
    @NotNull
    public String getName() {
        return "WAITING";
    }

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public boolean isFrozen() {
        return false;
    }

    public boolean isMovementDetected() {
        return movementDetected;
    }

    public long getElapsedTime() {
        return System.currentTimeMillis() - startTime;
    }
}