package com.timeback.ui.domain.model;

import androidx.annotation.Nullable;

public class BaselineState {
    private final boolean isObserving;
    @Nullable private final Integer remainingDays;
    @Nullable private final Duration weeklyBaseline;

    public BaselineState(boolean isObserving, @Nullable Integer remainingDays, @Nullable Duration weeklyBaseline) {
        this.isObserving = isObserving;
        this.remainingDays = remainingDays;
        this.weeklyBaseline = weeklyBaseline;
    }

    public boolean isObserving() { return isObserving; }
    @Nullable public Integer getRemainingDays() { return remainingDays; }
    @Nullable public Duration getWeeklyBaseline() { return weeklyBaseline; }
}
