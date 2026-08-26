package io.timeback.domain;

import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Covers APP-05~APP-09 command paths that are separate from the calculation engine. */
final class DomainCommandRegression {
    private DomainCommandRegression() { }

    static void run() {
        Instant now = Instant.parse("2026-02-01T00:00:00Z");
        AppProfile app = AppProfile.unclassified("example.app.command", "Example Command", now);
        Goal goal = new Goal("goal", "Example Goal", Duration.ofHours(1), now);
        DomainState initial = new DomainState(Map.of(app.packageName(), app), List.of(), List.of(), List.of(goal), Optional.empty(), List.of(), Map.of());
        DomainCommandService commands = new DomainCommandService(new MetricsEngine());

        DomainState classified = commands.setAppDefault(initial, app.packageName(), Classification.PRODUCTIVE, now.plusSeconds(1));
        check(classified.apps().get(app.packageName()).state() == AppClassificationState.CLASSIFIED, "app command saves default");
        expectIllegal(() -> commands.setAppDefault(classified, app.packageName(), Classification.MIXED, now));

        ActivityRecord activity = new ActivityRecord("activity", ActivityType.CUSTOM, "Example Activity", Classification.PRODUCTIVE,
                new Interval(now, now.plusSeconds(60)));
        DomainState withActivity = commands.createActivity(classified, activity);
        check(withActivity.activities().size() == 1, "activity command creates validated activity");
        expectIllegal(() -> new ActivityRecord("invalid", ActivityType.CUSTOM, " ", Classification.PRODUCTIVE, activity.interval()));

        ContextRevision mixed = new ContextRevision("context", "logical", 1, activity.interval(), Classification.MIXED,
                DecisionStatus.CONFIRMATION_REQUIRED, EffectiveState.CURRENT, DecisionKind.AUTO, null, null, now);
        DomainState withMixed = new DomainState(withActivity.apps(), withActivity.activities(), List.of(mixed), withActivity.goals(),
                withActivity.runningTimer(), withActivity.recoveredTimes(), withActivity.measurements());
        DomainState confirmed = commands.confirmContext(withMixed, "context", Classification.WASTE, ConfirmationAnswer.DISTRACTION, now.plusSeconds(2));
        ContextRevision confirmation = confirmed.contexts().get(1);
        check(confirmed.contexts().stream().filter(value -> value.effectiveState() == EffectiveState.CURRENT).count() == 1
                        && confirmation.kind() == DecisionKind.CONFIRMATION && confirmation.confirmationAnswer() == ConfirmationAnswer.DISTRACTION,
                "context confirmation atomically replaces current revision with provenance");

        MeasurementDay day = new MeasurementDay(new MeasurementKey("anonymous", LocalDate.of(2026, 2, 1), ZoneId.of("UTC")),
                CoverageStatus.COMPLETE, Duration.ZERO, 1, 1);
        check(commands.recordMeasurement(confirmed, day).measurements().containsKey(day.key()), "measurement command records complete zero day");

        DomainState running = commands.startTimer(confirmed, "timer", goal.id(), now);
        expectState(() -> commands.startTimer(running, "timer2", goal.id(), now.plusSeconds(1)));
        DomainState manual = commands.recordManualRecovered(running, "manual", goal.id(), new Interval(now, now.plusSeconds(60)), now.plusSeconds(60));
        check(manual.recoveredTimes().size() == 1 && manual.recoveredTimes().get(0).method() == RecoveryMethod.MANUAL,
                "manual recovery command records valid goal interval");

        RecoveryEngine recovery = new RecoveryEngine();
        RecoveredTime one = new RecoveredTime("one", goal.id(), RecoveryMethod.MANUAL, new Interval(now, now.plusSeconds(60)), now);
        RecoveredTime two = new RecoveredTime("two", goal.id(), RecoveryMethod.MANUAL, new Interval(now.plusSeconds(30), now.plusSeconds(90)), now);
        List<RecoverySegment> sameGoal = recovery.segments(List.of(one, two), List.of());
        check(recovery.pendingRecovered(sameGoal, null).isZero() && recovery.assignedRecovered(sameGoal, null).equals(Duration.ofSeconds(90)),
                "same-goal overlap is unioned without representative decision");

        SavedMetrics nanos = new SavedMetrics(MetricStatus.VALUE, Duration.ZERO, new Ratio(BigInteger.valueOf(500), BigInteger.ONE),
                new SignedNanosRatio(BigInteger.valueOf(500), BigInteger.ONE), new Ratio(BigInteger.valueOf(500), BigInteger.ONE));
        Ratio exact = new MetricsEngine().recoveryRate(nanos, Duration.ofNanos(250), Duration.ZERO).ratio();
        check(exact.numerator().equals(BigInteger.valueOf(250)) && exact.denominator().equals(BigInteger.valueOf(500)),
                "sub-millisecond rate keeps nanosecond components");
    }

    static void directTimelineEditAtomicity() {
        Instant now = Instant.parse("2026-02-02T00:00:00Z");
        Interval interval = new Interval(now, now.plusSeconds(60));
        ContextRevision automaticWaste = new ContextRevision("context", "logical", 1, interval, Classification.WASTE,
                DecisionStatus.AUTO_CLASSIFIED, EffectiveState.CURRENT, DecisionKind.AUTO, null, null, now);
        DomainState original = new DomainState(Map.of(), List.of(), List.of(automaticWaste), List.of(), Optional.empty(), List.of(), Map.of());
        DomainCommandService commands = new DomainCommandService(new MetricsEngine());

        InMemoryContextStore success = new InMemoryContextStore(original.contexts(), false);
        DomainState edited = commands.editContext(original, "context", Classification.PRODUCTIVE, now.plusSeconds(1), success);
        ContextRevision superseded = edited.contexts().get(0);
        ContextRevision replacement = edited.contexts().get(1);
        check(success.snapshot.equals(edited.contexts()) && superseded.effectiveState() == EffectiveState.SUPERSEDED
                        && replacement.effectiveState() == EffectiveState.CURRENT && replacement.revision() == 2
                        && replacement.logicalId().equals(automaticWaste.logicalId()) && replacement.interval().equals(interval)
                        && replacement.kind() == DecisionKind.TIMELINE_EDIT && replacement.confirmationAnswer() == null
                        && replacement.otherFinalClassification() == null,
                "direct edit creates one provenance-safe replacement revision atomically");
        AppProfile app = AppProfile.classified("example.app.direct-edit", "Example Direct Edit", Classification.WASTE, now, now);
        List<EffectiveDecision> decisions = new ContextEngine().analyze(List.of(new AppSession("session", app.packageName(), interval, false)),
                Map.of(app.packageName(), app), List.of(), edited.contexts());
        check(decisions.get(0).classification() == Classification.PRODUCTIVE && new ContextEngine().wasteDuration(decisions).isZero(),
                "direct edit overrides automatic waste in the timeline");
        expectIllegal(() -> commands.editContext(original, "context", Classification.MIXED, now));

        InMemoryContextStore failure = new InMemoryContextStore(original.contexts(), true);
        expectState(() -> commands.editContext(original, "context", Classification.PRODUCTIVE, now.plusSeconds(1), failure));
        check(failure.snapshot.equals(original.contexts()) && failure.snapshot.size() == 1
                        && failure.snapshot.get(0).effectiveState() == EffectiveState.CURRENT && original.contexts().equals(List.of(automaticWaste)),
                "failed Context persistence preserves the prior revision snapshot");
    }

    private static void check(boolean value, String message) { if (!value) throw new AssertionError(message); }
    private static void expectIllegal(Runnable command) { try { command.run(); throw new AssertionError("expected illegal argument"); } catch (IllegalArgumentException expected) { } }
    private static void expectState(Runnable command) { try { command.run(); throw new AssertionError("expected state conflict"); } catch (IllegalStateException expected) { } }

    private static final class InMemoryContextStore implements ContextSnapshotStore {
        private List<ContextRevision> snapshot;
        private final boolean fail;
        private InMemoryContextStore(List<ContextRevision> initial, boolean fail) { snapshot = List.copyOf(initial); this.fail = fail; }
        @Override public void replaceAtomically(List<ContextRevision> expectedSnapshot, List<ContextRevision> replacementSnapshot) {
            if (!snapshot.equals(expectedSnapshot)) throw new IllegalStateException("context snapshot changed");
            if (fail) throw new IllegalStateException("forced context persistence failure");
            snapshot = List.copyOf(replacementSnapshot);
        }
    }
}
