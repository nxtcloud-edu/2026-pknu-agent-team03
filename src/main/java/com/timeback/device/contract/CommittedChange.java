package com.timeback.device.contract;

public record CommittedChange(
        long sequence,
        String changeId,
        DataOwnerScope owner,
        DeviceEntityType entityType,
        String entityId,
        ChangeOperation operation,
        long occurredAtMillis
) {}
