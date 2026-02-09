package org.rofu.echeatcheck.api.event;

import org.jetbrains.annotations.NotNull;
import org.rofu.echeatcheck.core.CheatCheck;

public class CheckStartEvent extends CheatCheckEvent {

    public CheckStartEvent(@NotNull CheatCheck cheatCheck) {
        super(cheatCheck);
    }
}