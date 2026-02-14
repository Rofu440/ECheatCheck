package org.rofu.echeatcheck.api.state;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.rofu.echeatcheck.core.CheatCheck;
import org.rofu.echeatcheck.util.ColorUtil;

import java.util.Objects;

public class FrozenState extends ActiveState {

    public FrozenState(@NotNull CheatCheck cheatCheck) {
        super(Objects.requireNonNull(cheatCheck, "CheatCheck не может быть null"));
    }

    @Override
    public void onEnter() {
        super.onEnter();

        Player admin = cheatCheck.getAdmin();
        Player suspect = cheatCheck.getSuspect();

        if (admin != null && admin.isOnline()) {
            cheatCheck.getBossBarManager().showFrozenAdminBossBar(admin);
        }

        if (suspect != null && suspect.isOnline()) {
            cheatCheck.getBossBarManager().showFrozenPlayerBossBar(suspect);
        }
    }

    @Override
    public void onUpdate() {
        Player admin = cheatCheck.getAdmin();
        Player suspect = cheatCheck.getSuspect();

        if (admin != null && admin.isOnline()) {
            cheatCheck.getBossBarManager().updateFrozenAdminBossBar(admin);
        }

        if (suspect != null && suspect.isOnline()) {
            cheatCheck.getBossBarManager().updateFrozenPlayerBossBar(suspect);
        }
    }

    @Override
    public void onFreeze() {
        cheatCheck.transitionTo(new ActiveState(cheatCheck));

        Player admin = cheatCheck.getAdmin();
        if (admin != null && admin.isOnline()) {
            admin.sendMessage(ColorUtil.colorize("&aПроверка возобновлена"));
        }
    }

    @Override
    @NotNull
    public String getName() {
        return "FROZEN";
    }

    @Override
    public boolean isFrozen() {
        return true;
    }
}