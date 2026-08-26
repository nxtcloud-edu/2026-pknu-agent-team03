package io.timeback.domain;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** APP-05~APP-09 pure-domain command paths; adapters persist returned snapshots atomically. */
public final class DomainCommandService {
    private final MetricsEngine metrics;

    public DomainCommandService(MetricsEngine metrics) {
        this.metrics = Objects.requireNonNull(metrics, "metrics");
    }

    public DomainState setAppDefault(DomainState state, String packageName, Classification classification, Instant changedAt) {
        if (classification == Classification.MIXED) throw new IllegalArgumentException("MIXED is not an app default");
        AppProfile current = requireApp(state, packageName);
        Map<String, AppProfile> apps = new HashMap<>(state.apps());
        apps.put(packageName, AppProfile.classified(current.packageName(), current.displayName(), classification, current.discoveredAt(), changedAt));
        return state.withApps(apps);
    }

    public DomainState createActivity(DomainState state, ActivityRecord activity) {
        List<ActivityRecord> next = new ArrayList<>(state.activities());
        next.add(activity);
        return state.withActivities(next);
    }

    public DomainState updateActivity(DomainState state, ActivityRecord replacement) {
        List<ActivityRecord> next = new ArrayList<>(state.activities());
        int index = indexOfActivity(next, replacement.id());
        if (index < 0) throw new IllegalArgumentException("activity not found");
        next.set(index, replacement);
        return state.withActivities(next);
    }

    public DomainState confirmContext(DomainState state, String contextId, Classification finalClassification,
                                      ConfirmationAnswer answer, Instant decidedAt) {
        Objects.requireNonNull(answer, "answer");
        if (answer == ConfirmationAnswer.OTHER && finalClassification == null) throw new IllegalArgumentException("OTHER needs final classification");
        return reviseContext(state, contextId, finalClassification, decidedAt, DecisionKind.CONFIRMATION, answer,
                answer == ConfirmationAnswer.OTHER ? finalClassification : null);
    }

    /** Produces a direct Timeline-edit revision without publishing it to a store. */
    public DomainState editContext(DomainState state, String contextId, Classification finalClassification, Instant decidedAt) {
        return reviseContext(state, contextId, finalClassification, decidedAt, DecisionKind.TIMELINE_EDIT, null, null);
    }

    /** Publishes a direct Timeline edit only if the Context snapshot replacement succeeds atomically. */
    public DomainState editContext(DomainState state, String contextId, Classification finalClassification, Instant decidedAt,
                                   ContextSnapshotStore store) {
        Objects.requireNonNull(store, "store");
        DomainState replacement = editContext(state, contextId, finalClassification, decidedAt);
        store.replaceAtomically(state.contexts(), replacement.contexts());
        return replacement;
    }

    private static DomainState reviseContext(DomainState state, String contextId, Classification finalClassification,
                                             Instant decidedAt, DecisionKind kind, ConfirmationAnswer answer,
                                             Classification otherFinalClassification) {
        Objects.requireNonNull(state, "state"); Objects.requireNonNull(finalClassification, "finalClassification"); Objects.requireNonNull(decidedAt, "decidedAt");
        if (finalClassification == Classification.MIXED) throw new IllegalArgumentException("MIXED cannot be final");
        List<ContextRevision> next = new ArrayList<>(state.contexts());
        int index = indexOfContext(next, contextId);
        if (index < 0) throw new IllegalArgumentException("context not found");
        ContextRevision current = next.get(index);
        if (current.effectiveState() != EffectiveState.CURRENT) throw new IllegalStateException("context is not current");
        ContextRevision superseded = new ContextRevision(current.id(), current.logicalId(), current.revision(), current.interval(), current.classification(), current.status(), EffectiveState.SUPERSEDED, current.kind(), current.confirmationAnswer(), current.otherFinalClassification(), current.decidedAt());
        ContextRevision replacement = new ContextRevision(current.id() + ":r" + (current.revision() + 1), current.logicalId(), current.revision() + 1,
                current.interval(), finalClassification, DecisionStatus.USER_CONFIRMED, EffectiveState.CURRENT, kind, answer, otherFinalClassification, decidedAt);
        next.set(index, superseded);
        next.add(replacement);
        return state.withContexts(next);
    }

    public DomainState recordMeasurement(DomainState state, MeasurementDay measurement) {
        return state.withMeasurements(metrics.upsert(state.measurements(), measurement).snapshot());
    }

    public DomainState startTimer(DomainState state, String timerId, String goalId, Instant startedAt) {
        requireGoal(state, goalId);
        if (state.runningTimer().isPresent()) throw new IllegalStateException("timer already running");
        return state.withRunningTimer(Optional.of(new RunningTimer(timerId, goalId, startedAt)));
    }

    public DomainState recordManualRecovered(DomainState state, String recoveredId, String goalId, Interval interval, Instant createdAt) {
        requireGoal(state, goalId);
        List<RecoveredTime> next = new ArrayList<>(state.recoveredTimes());
        next.add(new RecoveredTime(recoveredId, goalId, RecoveryMethod.MANUAL, interval, createdAt));
        return state.withRecoveredTimes(next);
    }

    public Goal createGoal(String id, String name, Duration targetDuration, Instant createdAt) {
        return new Goal(id, name, targetDuration, createdAt);
    }

    private static AppProfile requireApp(DomainState state, String packageName) {
        AppProfile app = state.apps().get(packageName);
        if (app == null) throw new IllegalArgumentException("app not found");
        return app;
    }
    private static Goal requireGoal(DomainState state, String goalId) {
        return state.goals().stream().filter(goal -> goal.id().equals(goalId)).findFirst().orElseThrow(() -> new IllegalArgumentException("goal not found"));
    }
    private static int indexOfActivity(List<ActivityRecord> activities, String id) { for (int i = 0; i < activities.size(); i++) if (activities.get(i).id().equals(id)) return i; return -1; }
    private static int indexOfContext(List<ContextRevision> contexts, String id) { for (int i = 0; i < contexts.size(); i++) if (contexts.get(i).id().equals(id)) return i; return -1; }
}

record DomainState(Map<String, AppProfile> apps, List<ActivityRecord> activities, List<ContextRevision> contexts,
                   List<Goal> goals, Optional<RunningTimer> runningTimer, List<RecoveredTime> recoveredTimes,
                   Map<MeasurementKey, MeasurementDay> measurements) {
    DomainState {
        apps = Map.copyOf(apps); activities = List.copyOf(activities); contexts = List.copyOf(contexts); goals = List.copyOf(goals);
        runningTimer = Objects.requireNonNull(runningTimer); recoveredTimes = List.copyOf(recoveredTimes); measurements = Map.copyOf(measurements);
    }
    DomainState withApps(Map<String, AppProfile> value) { return new DomainState(value, activities, contexts, goals, runningTimer, recoveredTimes, measurements); }
    DomainState withActivities(List<ActivityRecord> value) { return new DomainState(apps, value, contexts, goals, runningTimer, recoveredTimes, measurements); }
    DomainState withContexts(List<ContextRevision> value) { return new DomainState(apps, activities, value, goals, runningTimer, recoveredTimes, measurements); }
    DomainState withRunningTimer(Optional<RunningTimer> value) { return new DomainState(apps, activities, contexts, goals, value, recoveredTimes, measurements); }
    DomainState withRecoveredTimes(List<RecoveredTime> value) { return new DomainState(apps, activities, contexts, goals, runningTimer, value, measurements); }
    DomainState withMeasurements(Map<MeasurementKey, MeasurementDay> value) { return new DomainState(apps, activities, contexts, goals, runningTimer, recoveredTimes, value); }
}
