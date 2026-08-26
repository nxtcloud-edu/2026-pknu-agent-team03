package com.timeback.ui.domain.model;

public class TimelineItem {
    private final Identifier id;
    private final String appName;
    private final String packageName;
    private final String activityName;
    private final TimeRange timeRange;
    private final Duration duration;
    private final ContextClassification classification;
    private final boolean isComplex;
    private final boolean contextConfirmationRequired;

    public TimelineItem(Identifier id, String appName, String packageName, String activityName,
                        TimeRange timeRange, Duration duration, ContextClassification classification,
                        boolean isComplex, boolean contextConfirmationRequired) {
        this.id = id;
        this.appName = appName;
        this.packageName = packageName;
        this.activityName = activityName;
        this.timeRange = timeRange;
        this.duration = duration;
        this.classification = classification;
        this.isComplex = isComplex;
        this.contextConfirmationRequired = contextConfirmationRequired;
    }

    public Identifier getId() { return id; }
    public String getAppName() { return appName; }
    public String getPackageName() { return packageName; }
    public String getActivityName() { return activityName; }
    public TimeRange getTimeRange() { return timeRange; }
    public Duration getDuration() { return duration; }
    public ContextClassification getClassification() { return classification; }
    public boolean isComplex() { return isComplex; }
    public boolean isContextConfirmationRequired() { return contextConfirmationRequired; }
}
