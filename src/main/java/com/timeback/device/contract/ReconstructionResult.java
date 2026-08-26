package com.timeback.device.contract;

import java.util.List;

public sealed interface ReconstructionResult permits
        ReconstructionResult.Success,
        ReconstructionResult.RetryableFailure,
        ReconstructionResult.Failure {
    record Success(
            List<AppSession> sessions,
            OpenSessionCandidate openCandidate,
            List<CommittedChange> committedChanges
    ) implements ReconstructionResult {
        public Success {
            sessions = List.copyOf(sessions);
            committedChanges = List.copyOf(committedChanges);
        }
    }

    record RetryableFailure(String reason) implements ReconstructionResult {}

    record Failure(String reason) implements ReconstructionResult {}
}
