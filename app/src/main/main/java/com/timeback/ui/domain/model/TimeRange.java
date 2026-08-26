package com.timeback.ui.domain.model;

public class TimeRange {
    private final long startAt; // epoch millis
    private final long endAt;   // epoch millis, exclusive

    public TimeRange(long startAt, long endAt) {
        this.startAt = startAt;
        this.endAt = endAt;
    }

    public long getStartAt() { return startAt; }
    public long getEndAt() { return endAt; }
}
