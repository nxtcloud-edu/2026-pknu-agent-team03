package com.timeback.device.contract;

public record ObservedUsageEvent(
        String packageName,
        UsageEventKind kind,
        long occurredAtMillis,
        int occurrenceInTimestampGroup
) {
    public ObservedUsageEvent(String packageName, UsageEventKind kind, long occurredAtMillis) {
        this(packageName, kind, occurredAtMillis, 0);
    }

    public ObservedUsageEvent {
        if (packageName == null || packageName.isBlank()) {
            throw new IllegalArgumentException("package name must not be blank");
        }
        if (kind == null) {
            throw new IllegalArgumentException("usage event kind must not be null");
        }
        if (occurrenceInTimestampGroup < 0) {
            throw new IllegalArgumentException("occurrence must not be negative");
        }
    }
}
