package com.timeback.ui.fake;

import androidx.annotation.Nullable;
import com.timeback.ui.domain.model.*;
import java.util.List;

public class HomeViewData {
    private final Duration wasteTimeToday;
    private final BaselineState baselineState;
    @Nullable private final Duration savedTime;
    @Nullable private final Duration recoveredTime;
    @Nullable private final Double recoveryRate;
    private final List<GoalProgress> goalProgresses;

    public HomeViewData(Duration wasteTimeToday, BaselineState baselineState,
                        @Nullable Duration savedTime, @Nullable Duration recoveredTime,
                        @Nullable Double recoveryRate, List<GoalProgress> goalProgresses) {
        this.wasteTimeToday = wasteTimeToday;
        this.baselineState = baselineState;
        this.savedTime = savedTime;
        this.recoveredTime = recoveredTime;
        this.recoveryRate = recoveryRate;
        this.goalProgresses = goalProgresses;
    }

    public Duration getWasteTimeToday() { return wasteTimeToday; }
    public BaselineState getBaselineState() { return baselineState; }
    @Nullable public Duration getSavedTime() { return savedTime; }
    @Nullable public Duration getRecoveredTime() { return recoveredTime; }
    @Nullable public Double getRecoveryRate() { return recoveryRate; }
    public List<GoalProgress> getGoalProgresses() { return goalProgresses; }
}
