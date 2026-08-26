package com.timeback.ui.domain.model;

public class GoalProgress {
    private final Goal goal;
    private final Duration accumulatedDuration;
    private final double progressPercent;

    public GoalProgress(Goal goal, Duration accumulatedDuration, double progressPercent) {
        this.goal = goal;
        this.accumulatedDuration = accumulatedDuration;
        this.progressPercent = progressPercent;
    }

    public Goal getGoal() { return goal; }
    public Duration getAccumulatedDuration() { return accumulatedDuration; }
    public double getProgressPercent() { return progressPercent; }
}
