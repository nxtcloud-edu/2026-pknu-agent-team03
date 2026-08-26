package com.timeback.ui.domain.model;

import java.util.List;

public class ReportData {
    private final ReportPeriod period;
    private final TimeRange timeRange;
    private final TimeMetrics metrics;
    private final List<GoalProgress> goalSummaries;
    private final boolean baselineComparable;

    public ReportData(ReportPeriod period, TimeRange timeRange, TimeMetrics metrics,
                      List<GoalProgress> goalSummaries, boolean baselineComparable) {
        this.period = period;
        this.timeRange = timeRange;
        this.metrics = metrics;
        this.goalSummaries = goalSummaries;
        this.baselineComparable = baselineComparable;
    }

    public ReportPeriod getPeriod() { return period; }
    public TimeRange getTimeRange() { return timeRange; }
    public TimeMetrics getMetrics() { return metrics; }
    public List<GoalProgress> getGoalSummaries() { return goalSummaries; }
    public boolean isBaselineComparable() { return baselineComparable; }
}
