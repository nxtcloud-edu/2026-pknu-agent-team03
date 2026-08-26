package com.timeback.device.contract;

public record TimeRange(long startAtMillis, long endAtMillis) {
    public TimeRange {
        if (startAtMillis >= endAtMillis) {
            throw new IllegalArgumentException("time range must be start-inclusive and end-exclusive");
        }
    }

    public long durationMillis() {
        return endAtMillis - startAtMillis;
    }

    public boolean contains(long timestampMillis) {
        return timestampMillis >= startAtMillis && timestampMillis < endAtMillis;
    }

    public boolean overlaps(TimeRange other) {
        return startAtMillis < other.endAtMillis && other.startAtMillis < endAtMillis;
    }
}
