package org.rofu.echeatcheck.api.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.rofu.echeatcheck.core.CheatCheck;

import java.util.Objects;

public abstract class CheatCheckEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final CheatCheck cheatCheck;

    protected CheatCheckEvent(@NotNull CheatCheck cheatCheck) {
        this.cheatCheck = Objects.requireNonNull(cheatCheck, "Проверка не может быть null");
    }

    @NotNull
    public CheatCheck getCheatCheck() {
        return cheatCheck;
    }

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}