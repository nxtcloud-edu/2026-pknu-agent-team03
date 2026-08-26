package io.timeback.domain;

import java.util.Objects;

/** Explicit APP-08 candidate proposal and approval path; active Baseline never changes implicitly. */
public final class BaselineLifecycle {
    public BaselineCandidate propose(String candidateId, Baseline active, Baseline observed) {
        Objects.requireNonNull(observed, "observed");
        if (active != null && active.weeklyWaste().equals(observed.weeklyWaste())) {
            throw new IllegalArgumentException("unchanged baseline does not need a candidate");
        }
        return new BaselineCandidate(candidateId, observed, BaselineCandidateStatus.PROPOSED);
    }

    public BaselineDecision decide(Baseline active, BaselineCandidate candidate, boolean approve) {
        if (candidate.status() != BaselineCandidateStatus.PROPOSED) {
            throw new IllegalStateException("candidate already decided");
        }
        BaselineCandidate decided = new BaselineCandidate(candidate.id(), candidate.baseline(),
                approve ? BaselineCandidateStatus.APPROVED : BaselineCandidateStatus.REJECTED);
        return new BaselineDecision(approve ? candidate.baseline() : active, decided);
    }
}
