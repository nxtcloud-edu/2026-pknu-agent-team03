package com.timeback.device.contract;

public record UsageEvent(
        String eventId,
        DataOwnerScope owner,
        String packageName,
        UsageEventKind kind,
        long occurredAtMillis,
        long collectedAtMillis,
        int sourceOrder
) {}
