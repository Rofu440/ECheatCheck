package org.rofu.echeatcheck.api.state;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.rofu.echeatcheck.core.CheatCheck;
import org.rofu.echeatcheck.util.ColorUtil;

import java.util.Objects;
import java.util.Optional;

public class FinishedState extends CheckState {

    private final String reason;

    public FinishedState(@NotNull CheatCheck cheatCheck, @NotNull String reason) {
        super(Objects.requireNonNull(cheatCheck, "CheatCheck не может быть null"));
        this.reason = Objects.requireNonNull(reason, "Причина не может быть null");
    }

    @Override
    public void onEnter() {
        Player suspect = cheatCheck.getSuspect();

        if (suspect != null && suspect.isOnline()) {
            try {
                cheatCheck.getTeleportManager().teleportToFinish(suspect);
            } catch (Exception e) {
                cheatCheck.getLogger().warning("Не удалось телепортировать игрока " +
                        suspect.getName() + " в точку завершения: " + e.getMessage());
            }

            suspect.sendMessage(ColorUtil.colorize("&cПроверка завершена. Причина: &6" + reason));
            cheatCheck.getBossBarManager().hideBossBar(suspect);
            cheatCheck.getActionBarManager().clearActionBar(suspect);
            cheatCheck.getSoundManager().playCheckEndSound(suspect);
        }

        Player admin = cheatCheck.getAdmin();
        if (admin != null && admin.isOnline()) {
            admin.sendMessage(ColorUtil.colorize("&aПроверка игрока &6" +
                    cheatCheck.getSuspectName() + " &aзавершена. Причина: &6" + reason));
            cheatCheck.getBossBarManager().hideBossBar(admin);
            cheatCheck.getSoundManager().playCheckEndSound(admin);
        }
        executeTemplateCommand(reason);
        cheatCheck.stop();
    }

    @Override
    public void onExit() {
        Player suspect = cheatCheck.getSuspect();
        Player admin = cheatCheck.getAdmin();

        if (suspect != null && suspect.isOnline()) {
            cheatCheck.getBossBarManager().hideBossBar(suspect);
            cheatCheck.getActionBarManager().clearActionBar(suspect);
            cheatCheck.getSoundManager().stopAllSounds(suspect);
        }

        if (admin != null && admin.isOnline()) {
            cheatCheck.getBossBarManager().hideBossBar(admin);
        }
    }

    @Override
    public void onUpdate() {
    }

    @Override
    public void onMessageReceived(@NotNull Player sender, @NotNull String message) {
    }

    @Override
    public void onPlayerMove(@NotNull Player player) {
    }

    @Override
    public void onPlayerQuit(@NotNull Player player) {
    }

    @Override
    public void onTimeAdded(long seconds) {
        Player admin = cheatCheck.getAdmin();
        if (admin != null && admin.isOnline()) {
            admin.sendMessage(ColorUtil.colorize("&cНельзя добавить время в завершенной проверке"));
        }
    }

    @Override
    public void onFreeze() {
        Player admin = cheatCheck.getAdmin();
        if (admin != null && admin.isOnline()) {
            admin.sendMessage(ColorUtil.colorize("&cНельзя заморозить завершенную проверку"));
        }
    }

    @Override
    public void onFinish(@NotNull String reason) {
    }

    @Override
    @NotNull
    public String getName() {
        return "FINISHED";
    }

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public boolean isFrozen() {
        return false;
    }

    @NotNull
    public String getReason() {
        return reason;
    }

    private void executeTemplateCommand(@NotNull String reason) {
        Optional<?> template = cheatCheck.getTemplatesConfig().getAllTemplates().stream()
                .filter(t -> {
                    try {
                        var cheatTemplate = (org.rofu.echeatcheck.config.TemplatesConfig.CheatTemplate) t;
                        return cheatTemplate.getName().equalsIgnoreCase(reason) ||
                                cheatTemplate.getReason().toLowerCase().contains(reason.toLowerCase());
                    } catch (Exception e) {
                        return false;
                    }
                })
                .findFirst();

        if (template.isPresent()) {
            try {
                var cheatTemplate = (org.rofu.echeatcheck.config.TemplatesConfig.CheatTemplate) template.get();
                String command = cheatTemplate.apply(cheatCheck.getSuspectName());
                Bukkit.getScheduler().runTask(cheatCheck.getPlugin(),
                        () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command));

                cheatCheck.getLogger().info("Выполнена команда шаблона для игрока " +
                        cheatCheck.getSuspectName() + ": " + command);

            } catch (Exception e) {
                cheatCheck.getLogger().warning("Не удалось выполнить команду шаблона: " + e.getMessage());
            }
        } else {
            String command = "ban " + cheatCheck.getSuspectName() + " 30d " + reason;
            Bukkit.getScheduler().runTask(cheatCheck.getPlugin(),
                    () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command));

            cheatCheck.getLogger().info("Выполнена стандартная команда бана для игрока " +
                    cheatCheck.getSuspectName() + ": " + command);
        }
    }
}