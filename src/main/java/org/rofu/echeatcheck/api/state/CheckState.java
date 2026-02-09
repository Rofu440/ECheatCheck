package org.rofu.echeatcheck.api.state;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.rofu.echeatcheck.core.CheatCheck;

public abstract class CheckState {

    protected final CheatCheck cheatCheck;

    protected CheckState(@NotNull CheatCheck cheatCheck) {
        this.cheatCheck = cheatCheck;
    }

    public abstract void onEnter();

    public abstract void onExit();

    public abstract void onUpdate();

    public abstract void onMessageReceived(@NotNull Player sender, @NotNull String message);

    public abstract void onPlayerMove(@NotNull Player player);

    public abstract void onPlayerQuit(@NotNull Player player);

    public abstract void onTimeAdded(long seconds);

    public abstract void onFreeze();

    public abstract void onFinish(@NotNull String reason);

    @NotNull
    public abstract String getName();

    public abstract boolean isActive();

    public abstract boolean isFrozen();
}