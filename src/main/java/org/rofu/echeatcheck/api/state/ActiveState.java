package org.rofu.echeatcheck.api.state;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.rofu.echeatcheck.core.CheatCheck;
import org.rofu.echeatcheck.core.core.task.AutoMessageTask;
import org.rofu.echeatcheck.core.core.task.TimerTask;
import org.rofu.echeatcheck.util.ColorUtil;
import org.rofu.echeatcheck.util.TimeFormatter;

import java.util.Objects;

public class ActiveState extends CheckState {

    private final TimerTask timerTask;
    private final AutoMessageTask autoMessageTask;
    private long remainingTime;
    private boolean frozen;

    public ActiveState(@NotNull CheatCheck cheatCheck) {
        super(Objects.requireNonNull(cheatCheck, "CheatCheck не может быть null"));
        this.timerTask = new TimerTask(cheatCheck);
        this.autoMessageTask = new AutoMessageTask(cheatCheck);
        this.remainingTime = cheatCheck.getRemainingTime();
        this.frozen = false;
    }

    @Override
    public void onEnter() {
        this.frozen = false;

        Player suspect = cheatCheck.getSuspect();
        Player admin = cheatCheck.getAdmin();

        if (suspect != null && suspect.isOnline()) {
            String timeFormatted = TimeFormatter.formatSeconds(remainingTime);
            String message = ColorUtil.colorize("&cПроверка началась! У вас есть &6" +
                    timeFormatted + " &cна прохождение проверки.");
            suspect.sendMessage(message);
            cheatCheck.getBossBarManager().showPlayerTimerBossBar(suspect, remainingTime);
        }

        if (admin != null && admin.isOnline()) {
            cheatCheck.getBossBarManager().showAdminTimerBossBar(admin, remainingTime);
        }

        timerTask.start();
        if (cheatCheck.getConfig().isAutoMessagesEnabled()) {
            autoMessageTask.start();
        }
    }

    @Override
    public void onExit() {
        timerTask.stop();
        autoMessageTask.stop();

        Player suspect = cheatCheck.getSuspect();
        Player admin = cheatCheck.getAdmin();

        if (suspect != null && suspect.isOnline()) {
            cheatCheck.getBossBarManager().hideBossBar(suspect);
        }

        if (admin != null && admin.isOnline()) {
            cheatCheck.getBossBarManager().hideBossBar(admin);
        }
    }

    @Override
    public void onUpdate() {
        Player suspect = cheatCheck.getSuspect();
        Player admin = cheatCheck.getAdmin();

        if (!frozen) {
            if (remainingTime <= 0) {
                onTimeExpired();
                return;
            }

            remainingTime--;
            cheatCheck.setRemainingTime(remainingTime);
        }

        if (suspect != null && suspect.isOnline()) {
            cheatCheck.getBossBarManager().updatePlayerTimerBossBar(suspect, remainingTime);
        }

        if (admin != null && admin.isOnline()) {
            cheatCheck.getBossBarManager().updateAdminTimerBossBar(admin, remainingTime);
        }
    }

    private void onTimeExpired() {
        Player suspect = cheatCheck.getSuspect();
        if (suspect != null) {
            String command = cheatCheck.getConfig().getOnTimeoutCommand()
                    .replace("%player%", suspect.getName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        }

        cheatCheck.stop();
    }

    @Override
    public void onMessageReceived(@NotNull Player sender, @NotNull String message) {
        Objects.requireNonNull(sender, "Отправитель не может быть null");
        Objects.requireNonNull(message, "Сообщение не может быть null");
        if (sender.equals(cheatCheck.getSuspect()) &&
                message.toLowerCase().contains("у меня чит")) {
            Player admin = cheatCheck.getAdmin();
            if (admin != null && admin.isOnline()) {
                admin.sendMessage(ColorUtil.colorize("&aИгрок &6" + sender.getName() +
                        " &aпризнался в использовании читов. Срок бана будет уменьшен."));
            }

            onFinish("Признание в использовании читов");
            return;
        }

        if (sender.equals(cheatCheck.getSuspect())) {
            cheatCheck.getChatManager().sendSuspectToAdmin(sender, message);
        } else if (sender.equals(cheatCheck.getAdmin())) {
            cheatCheck.getChatManager().sendAdminToSuspect(sender, message);
        }
    }

    @Override
    public void onPlayerMove(@NotNull Player player) {
        if (player.equals(cheatCheck.getSuspect())) {
            cheatCheck.getActionBarManager().showCheatCheckActionBar(player);
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
        Objects.requireNonNull(seconds, "Секунды не могут быть null");

        if (frozen) {
            Player admin = cheatCheck.getAdmin();
            if (admin != null && admin.isOnline()) {
                admin.sendMessage(ColorUtil.colorize("&cНельзя добавить время, когда проверка заморожена"));
            }
            return;
        }

        long newTime = remainingTime + seconds;
        long maxTime = cheatCheck.getConfig().getMaxTime();

        if (newTime > maxTime) {
            Player admin = cheatCheck.getAdmin();
            if (admin != null && admin.isOnline()) {
                admin.sendMessage(ColorUtil.colorize("&cНельзя превысить максимальное время проверки: " +
                        maxTime + " секунд"));
            }
            return;
        }

        remainingTime = newTime;
        cheatCheck.setRemainingTime(remainingTime);

        Player admin = cheatCheck.getAdmin();
        Player suspect = cheatCheck.getSuspect();

        if (admin != null && admin.isOnline()) {
            String timeFormatted = TimeFormatter.formatSeconds(seconds);
            admin.sendMessage(ColorUtil.colorize("&aВремя проверки увеличено на &6" + timeFormatted));
        }

        if (suspect != null && suspect.isOnline()) {
            String timeFormatted = TimeFormatter.formatSeconds(seconds);
            suspect.sendMessage(ColorUtil.colorize("&aВремя вашей проверки увеличено на &6" + timeFormatted));
        }

    }

    @Override
    public void onFreeze() {
        if (frozen) {
            frozen = false;
            timerTask.resume();

            Player admin = cheatCheck.getAdmin();
            Player suspect = cheatCheck.getSuspect();

            if (admin != null && admin.isOnline()) {
                admin.sendMessage(ColorUtil.colorize("&aПроверка возобновлена"));
                cheatCheck.getBossBarManager().showAdminTimerBossBar(admin, remainingTime);
            }

            if (suspect != null && suspect.isOnline()) {
                suspect.sendMessage(ColorUtil.colorize("&aПроверка возобновлена"));
                cheatCheck.getBossBarManager().showPlayerTimerBossBar(suspect, remainingTime);
            }

        } else {
            frozen = true;
            timerTask.pause();

            Player admin = cheatCheck.getAdmin();
            Player suspect = cheatCheck.getSuspect();

            if (admin != null && admin.isOnline()) {
                admin.sendMessage(ColorUtil.colorize("&aПроверка заморожена"));
                cheatCheck.getBossBarManager().showFrozenAdminBossBar(admin);
            }

            if (suspect != null && suspect.isOnline()) {
                suspect.sendMessage(ColorUtil.colorize("&aПроверка заморожена"));
                cheatCheck.getBossBarManager().showFrozenPlayerBossBar(suspect);
            }
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
        return frozen ? "FROZEN" : "ACTIVE";
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public boolean isFrozen() {
        return frozen;
    }

    public long getRemainingTime() {
        return remainingTime;
    }
}