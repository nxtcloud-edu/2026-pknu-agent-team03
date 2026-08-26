package com.timeback.device.contract;

public sealed interface CollectionResult permits
        CollectionResult.Success,
        CollectionResult.PermissionRequired,
        CollectionResult.RetryableFailure,
        CollectionResult.Failure {
    record Success(
            TimeRange effectiveRange,
            int observedCount,
            int createdCount,
            CollectionCheckpoint checkpoint
    ) implements CollectionResult {}

    enum PermissionRequired implements CollectionResult {
        INSTANCE
    }

    record RetryableFailure(String reason) implements CollectionResult {}

    record Failure(String reason) implements CollectionResult {}
}
