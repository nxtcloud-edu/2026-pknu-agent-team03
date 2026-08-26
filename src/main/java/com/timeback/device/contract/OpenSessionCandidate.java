package com.timeback.device.contract;

public record OpenSessionCandidate(
        DataOwnerScope owner,
        String packageName,
        long startedAtMillis,
        String sourceEventId
) {}
