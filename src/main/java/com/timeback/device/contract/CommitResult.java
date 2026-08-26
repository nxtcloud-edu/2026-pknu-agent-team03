package com.timeback.device.contract;

import java.util.List;

public sealed interface CommitResult permits CommitResult.Success, CommitResult.Failure {
    record Success(
            List<CommittedChange> changes,
            ChangeCursor nextCursor,
            int createdCount
    ) implements CommitResult {
        public Success(List<CommittedChange> changes, ChangeCursor nextCursor) {
            this(changes, nextCursor, 0);
        }

        public Success {
            changes = List.copyOf(changes);
        }
    }

    record Failure(String reason) implements CommitResult {}
}
