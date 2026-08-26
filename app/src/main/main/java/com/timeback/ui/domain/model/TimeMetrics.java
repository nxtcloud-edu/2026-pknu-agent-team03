package com.timeback.ui.domain.model;

import androidx.annotation.Nullable;

public class TimeMetrics {
    private final Duration wasteTime;
    @Nullable private final Duration baselineWeekly;
    @Nullable private final Duration savedTime;
    @Nullable private final Duration recoveredTime;
    @Nullable private final Double recoveryRate;

    public TimeMetrics(Duration wasteTime, @Nullable Duration baselineWeekly,
                       @Nullable Duration savedTime, @Nullable Duration recoveredTime,
                       @Nullable Double recoveryRate) {
        this.wasteTime = wasteTime;
        this.baselineWeekly = baselineWeekly;
        this.savedTime = savedTime;
        this.recoveredTime = recoveredTime;
        this.recoveryRate = recoveryRate;
    }

    public Duration getWasteTime() { return wasteTime; }
    @Nullable public Duration getBaselineWeekly() { return baselineWeekly; }
    @Nullable public Duration getSavedTime() { return savedTime; }
    @Nullable public Duration getRecoveredTime() { return recoveredTime; }
    @Nullable public Double getRecoveryRate() { return recoveryRate; }
}
