package io.timeback.domain;

import java.time.Instant;
import java.util.Objects;

/** Commits timer removal and recovered creation through one authority operation. */
public final class TimerCommandService {
    private final RecoveryEngine recoveryEngine;

    public TimerCommandService(RecoveryEngine recoveryEngine) {
        this.recoveryEngine = Objects.requireNonNull(recoveryEngine, "recoveryEngine");
    }

    public TimerCompletion complete(RunningTimer timer, Instant completedAt, String recoveredId, TimerRecoveryStore store) {
        TimerCompletion completion = recoveryEngine.completeTimer(timer, completedAt, recoveredId);
        store.completeAtomically(timer, completion.recoveredTime());
        return completion;
    }
}

interface TimerRecoveryStore {
    void completeAtomically(RunningTimer timer, RecoveredTime recoveredTime);
}
