package com.timeback.ui.domain.model;

import androidx.annotation.Nullable;
import java.util.List;

public class RecoveryEntry {
    @Nullable private final Goal selectedGoal;
    private final TimerState timerState;
    private final List<Goal> availableGoals;
    private final boolean overlapRequiresRepresentative;

    public RecoveryEntry(@Nullable Goal selectedGoal, TimerState timerState,
                         List<Goal> availableGoals, boolean overlapRequiresRepresentative) {
        this.selectedGoal = selectedGoal;
        this.timerState = timerState;
        this.availableGoals = availableGoals;
        this.overlapRequiresRepresentative = overlapRequiresRepresentative;
    }

    @Nullable public Goal getSelectedGoal() { return selectedGoal; }
    public TimerState getTimerState() { return timerState; }
    public List<Goal> getAvailableGoals() { return availableGoals; }
    public boolean isOverlapRequiresRepresentative() { return overlapRequiresRepresentative; }
}
