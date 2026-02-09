package org.rofu.echeatcheck.api.event;

import org.jetbrains.annotations.NotNull;
import org.rofu.echeatcheck.core.CheatCheck;

public class CheckStopEvent extends CheatCheckEvent {

    public CheckStopEvent(@NotNull CheatCheck cheatCheck) {
        super(cheatCheck);
    }
}