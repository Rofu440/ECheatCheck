package org.rofu.echeatcheck.api.event;

import org.jetbrains.annotations.NotNull;
import org.rofu.echeatcheck.core.CheatCheck;

import java.util.Objects;

public class CheckFinishEvent extends CheatCheckEvent {

    private final String reason;

    public CheckFinishEvent(@NotNull CheatCheck cheatCheck, @NotNull String reason) {
        super(cheatCheck);
        this.reason = Objects.requireNonNull(reason, "Причина не может быть null");
    }

    @NotNull
    public String getReason() {
        return reason;
    }
}