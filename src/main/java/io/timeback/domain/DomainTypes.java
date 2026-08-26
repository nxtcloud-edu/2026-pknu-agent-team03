package io.timeback.domain;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/** Package-private model types keep the first pure-domain module intentionally compact. */
enum Classification {
    PRODUCTIVE, LEISURE, WASTE, MIXED, NEUTRAL;
    boolean isNonNeutral() { return this != NEUTRAL && this != MIXED; }
}

enum AppClassificationState { UNCLASSIFIED, CLASSIFIED }
enum ActivityType { EXERCISE, STUDY, DEVELOPMENT, READING, LEISURE, CUSTOM }
enum DecisionStatus { AUTO_CLASSIFIED, CONFIRMATION_REQUIRED, USER_CONFIRMED }
enum EffectiveState { CURRENT, SUPERSEDED }
enum DecisionKind { AUTO, CONFIRMATION, TIMELINE_EDIT }
enum ConfirmationAnswer { PRODUCTIVE_PURPOSE, ASSISTIVE_USE, DISTRACTION, INTENTIONAL_REST, OTHER }
enum CoverageStatus { COMPLETE, INCOMPLETE, PERMISSION_MISSING, PARTIAL_IN_PROGRESS }
enum MeasurementUpsertResult { APPLIED, NO_OP }
enum MetricStatus { VALUE, OBSERVING, DATA_INCOMPLETE }
enum PeriodStatus { COMPLETE, IN_PROGRESS }
enum ComparisonBasis { FULL_DAYS, PARTIAL_COVERAGE }
enum RateAvailability { AVAILABLE, NOT_AVAILABLE }
enum RateUnavailableReason { DATA_INCOMPLETE, BASELINE_OBSERVING, SAVED_ZERO }
enum RecoveryDecisionState { NONE, DECISION_REQUIRED }
enum RecoveryMethod { TIMER, MANUAL }

record AppProfile(String packageName, String displayName, AppClassificationState state,
                  Classification defaultClassification, Instant discoveredAt, Instant classificationUpdatedAt) {
    AppProfile {
        DomainChecks.requireText(packageName, "packageName");
        DomainChecks.requireText(displayName, "displayName");
        Objects.requireNonNull(state, "state");
        Objects.requireNonNull(discoveredAt, "discoveredAt");
        if (state == AppClassificationState.UNCLASSIFIED && defaultClassification != null) throw new IllegalArgumentException("unclassified app cannot have a default");
        if (state == AppClassificationState.CLASSIFIED && (defaultClassification == null || defaultClassification == Classification.MIXED)) throw new IllegalArgumentException("classified app needs a non-MIXED default");
    }
    static AppProfile unclassified(String packageName, String displayName, Instant discoveredAt) { return new AppProfile(packageName, displayName, AppClassificationState.UNCLASSIFIED, null, discoveredAt, null); }
    static AppProfile classified(String packageName, String displayName, Classification classification, Instant discoveredAt, Instant updatedAt) { return new AppProfile(packageName, displayName, AppClassificationState.CLASSIFIED, classification, discoveredAt, updatedAt); }
    Classification effectiveDefault() { return state == AppClassificationState.UNCLASSIFIED ? Classification.NEUTRAL : defaultClassification; }
}

record ActivityRecord(String id, ActivityType type, String customName, Classification intent, Interval interval) {
    ActivityRecord {
        DomainChecks.requireText(id, "activity id"); Objects.requireNonNull(type, "type"); Objects.requireNonNull(intent, "intent"); Objects.requireNonNull(interval, "interval");
        if (intent == Classification.MIXED) throw new IllegalArgumentException("activity intent cannot be MIXED");
        if (type == ActivityType.CUSTOM) DomainChecks.requireText(customName, "customName");
    }
}

record AppSession(String id, String packageName, Interval interval, boolean inferred) {
    AppSession { DomainChecks.requireText(id, "session id"); DomainChecks.requireText(packageName, "packageName"); Objects.requireNonNull(interval, "interval"); }
}

record ContextRevision(String id, String logicalId, int revision, Interval interval, Classification classification,
                       DecisionStatus status, EffectiveState effectiveState, DecisionKind kind,
                       ConfirmationAnswer confirmationAnswer, Classification otherFinalClassification, Instant decidedAt) {
    ContextRevision {
        DomainChecks.requireText(id, "context id"); DomainChecks.requireText(logicalId, "logical context id");
        if (revision < 1) throw new IllegalArgumentException("revision must be positive");
        Objects.requireNonNull(interval, "interval"); Objects.requireNonNull(classification, "classification"); Objects.requireNonNull(status, "status"); Objects.requireNonNull(effectiveState, "effectiveState"); Objects.requireNonNull(kind, "kind"); Objects.requireNonNull(decidedAt, "decidedAt");
        if (status == DecisionStatus.CONFIRMATION_REQUIRED && classification != Classification.MIXED) throw new IllegalArgumentException("confirmation-required context must be MIXED");
        if (status == DecisionStatus.USER_CONFIRMED && classification == Classification.MIXED) throw new IllegalArgumentException("confirmed context cannot be MIXED");
        if (kind == DecisionKind.CONFIRMATION && confirmationAnswer == null) throw new IllegalArgumentException("confirmation provenance is required");
        if (kind == DecisionKind.CONFIRMATION && confirmationAnswer == ConfirmationAnswer.OTHER && otherFinalClassification == null) throw new IllegalArgumentException("OTHER confirmation needs final classification");
        if (kind == DecisionKind.CONFIRMATION && confirmationAnswer != ConfirmationAnswer.OTHER && otherFinalClassification != null) throw new IllegalArgumentException("only OTHER confirmation has alternate final classification");
        if (kind == DecisionKind.TIMELINE_EDIT && status != DecisionStatus.USER_CONFIRMED) throw new IllegalArgumentException("timeline edit must be user-confirmed");
        if (kind == DecisionKind.TIMELINE_EDIT && (confirmationAnswer != null || otherFinalClassification != null)) throw new IllegalArgumentException("timeline edit cannot carry confirmation provenance");
    }
    static ContextRevision confirmed(String id, String logicalId, int revision, Interval interval, Classification classification, Instant decidedAt) {
        return new ContextRevision(id, logicalId, revision, interval, classification, DecisionStatus.USER_CONFIRMED, EffectiveState.CURRENT, DecisionKind.TIMELINE_EDIT, null, null, decidedAt);
    }
}

record EffectiveDecision(Interval interval, Classification classification, DecisionStatus status, List<String> evidenceIds, ContextRevision sourceRevision) {
    EffectiveDecision {
        Objects.requireNonNull(interval, "interval"); Objects.requireNonNull(classification, "classification"); Objects.requireNonNull(status, "status"); evidenceIds = List.copyOf(evidenceIds);
        if (status == DecisionStatus.CONFIRMATION_REQUIRED && classification != Classification.MIXED) throw new IllegalArgumentException("confirmation-required decision must be MIXED");
    }
}

record MeasurementKey(String anonymousUser, LocalDate localDate, ZoneId zoneId) {
    MeasurementKey { DomainChecks.requireText(anonymousUser, "anonymousUser"); Objects.requireNonNull(localDate, "localDate"); Objects.requireNonNull(zoneId, "zoneId"); }
}
record MeasurementDay(MeasurementKey key, CoverageStatus coverageStatus, Duration wasteDuration, long contextSourceRevision, long coverageRevision) {
    MeasurementDay {
        Objects.requireNonNull(key, "key"); Objects.requireNonNull(coverageStatus, "coverageStatus"); Objects.requireNonNull(wasteDuration, "wasteDuration");
        if (wasteDuration.isNegative()) throw new IllegalArgumentException("waste cannot be negative");
        if (coverageStatus != CoverageStatus.COMPLETE && !wasteDuration.isZero()) throw new IllegalArgumentException("incomplete measurement must not claim waste");
    }
}
record Baseline(LocalDate startDate, LocalDate endDate, Duration weeklyWaste) {
    Baseline {
        Objects.requireNonNull(startDate); Objects.requireNonNull(endDate); Objects.requireNonNull(weeklyWaste);
        if (!endDate.equals(startDate.plusDays(6))) throw new IllegalArgumentException("baseline must cover exactly seven dates");
        if (weeklyWaste.isNegative()) throw new IllegalArgumentException("weekly waste cannot be negative");
    }
    Ratio dailyAverageNanoseconds() { return new Ratio(Ratio.nanos(weeklyWaste), BigInteger.valueOf(7)); }
}
record SavedMetrics(MetricStatus status, Duration currentWaste, Ratio expectedBaseline, SignedNanosRatio wasteDelta, Ratio savedDuration) {
    SavedMetrics {
        Objects.requireNonNull(status); Objects.requireNonNull(currentWaste);
        if (status == MetricStatus.VALUE) { Objects.requireNonNull(expectedBaseline); Objects.requireNonNull(wasteDelta); Objects.requireNonNull(savedDuration); }
    }
}
record PartialSavedMetrics(SavedMetrics metrics, PeriodStatus periodStatus, ComparisonBasis comparisonBasis,
                           Interval coveredRange, Instant asOf, BigDecimal elapsedDayEquivalent) {
    PartialSavedMetrics {
        Objects.requireNonNull(metrics); Objects.requireNonNull(periodStatus); Objects.requireNonNull(comparisonBasis); Objects.requireNonNull(coveredRange); Objects.requireNonNull(asOf); Objects.requireNonNull(elapsedDayEquivalent);
        if (periodStatus != PeriodStatus.IN_PROGRESS || comparisonBasis != ComparisonBasis.PARTIAL_COVERAGE) throw new IllegalArgumentException("partial metrics must be in-progress partial coverage");
    }
}
record Goal(String id, String name, Duration targetDuration, Instant createdAt) {
    Goal { DomainChecks.requireText(id, "goal id"); DomainChecks.requireText(name, "goal name"); Objects.requireNonNull(targetDuration); Objects.requireNonNull(createdAt); if (targetDuration.isZero() || targetDuration.isNegative()) throw new IllegalArgumentException("goal target must be positive"); }
}
record RunningTimer(String id, String goalId, Instant startedAt) {
    RunningTimer { DomainChecks.requireText(id, "timer id"); DomainChecks.requireText(goalId, "goalId"); Objects.requireNonNull(startedAt); }
}
record RecoveredTime(String id, String goalId, RecoveryMethod method, Interval interval, Instant createdAt) {
    RecoveredTime { DomainChecks.requireText(id, "recovered id"); DomainChecks.requireText(goalId, "goalId"); Objects.requireNonNull(method); Objects.requireNonNull(interval); Objects.requireNonNull(createdAt); }
}
record OverlapResolution(Set<String> sourceRecoveredIds, Interval effectiveInterval, String representativeGoalId, int revision) {
    OverlapResolution {
        sourceRecoveredIds = Set.copyOf(sourceRecoveredIds); if (sourceRecoveredIds.size() < 2) throw new IllegalArgumentException("resolution needs overlap sources"); Objects.requireNonNull(effectiveInterval); DomainChecks.requireText(representativeGoalId, "representativeGoalId"); if (revision < 1) throw new IllegalArgumentException("revision must be positive");
    }
}
record RecoverySegment(Interval interval, Set<String> sourceRecoveredIds, Set<String> candidateGoalIds, String representativeGoalId) {
    RecoverySegment {
        Objects.requireNonNull(interval); sourceRecoveredIds = Set.copyOf(sourceRecoveredIds); candidateGoalIds = Set.copyOf(candidateGoalIds);
        if (sourceRecoveredIds.isEmpty() || candidateGoalIds.isEmpty()) throw new IllegalArgumentException("recovery segment needs sources and candidates");
        if (representativeGoalId != null && !candidateGoalIds.contains(representativeGoalId)) throw new IllegalArgumentException("representative must be a candidate");
    }
    boolean pending() { return representativeGoalId == null; }
}
record Ratio(BigInteger numerator, BigInteger denominator) {
    Ratio { Objects.requireNonNull(numerator); Objects.requireNonNull(denominator); if (denominator.signum() <= 0 || numerator.signum() < 0) throw new IllegalArgumentException("ratio components must be non-negative with positive denominator"); }
    static Ratio from(Duration numerator, Duration denominator) { return new Ratio(nanos(numerator), nanos(denominator)); }
    BigDecimal asBigDecimal(int scale, RoundingMode roundingMode) { return new BigDecimal(numerator).divide(new BigDecimal(denominator), scale, roundingMode); }
    static BigInteger nanos(Duration duration) { return BigInteger.valueOf(duration.getSeconds()).multiply(BigInteger.valueOf(1_000_000_000L)).add(BigInteger.valueOf(duration.getNano())); }
}
record SignedNanosRatio(BigInteger numerator, BigInteger denominator) {
    SignedNanosRatio { Objects.requireNonNull(numerator); Objects.requireNonNull(denominator); if (denominator.signum() <= 0) throw new IllegalArgumentException("denominator must be positive"); }
    static SignedNanosRatio subtract(Ratio minuend, Duration subtrahend) {
        return new SignedNanosRatio(minuend.numerator().subtract(Ratio.nanos(subtrahend).multiply(minuend.denominator())), minuend.denominator());
    }
    boolean isNegative() { return numerator.signum() < 0; }
    Ratio nonNegativeOrZero() { return numerator.signum() <= 0 ? new Ratio(BigInteger.ZERO, denominator) : new Ratio(numerator, denominator); }
}
record GoalProgress(String goalId, Duration accumulatedDuration, Duration pendingOverlapDuration, Duration targetDuration, Ratio ratio) { }
record RecoveryMetrics(Duration assignedRecovered, Duration pendingOverlap, RateAvailability availability, RateUnavailableReason unavailableReason, RecoveryDecisionState decisionState, Ratio ratio) { }
record TimerCompletion(RecoveredTime recoveredTime, Optional<RunningTimer> remainingTimer) { }
record MeasurementUpsert(MeasurementUpsertResult result, Map<MeasurementKey, MeasurementDay> snapshot) { }

final class DomainChecks {
    private DomainChecks() { }
    static void requireText(String value, String name) { if (value == null || value.trim().isEmpty()) throw new IllegalArgumentException(name + " must not be blank"); }
}


enum BaselineCandidateStatus { PROPOSED, APPROVED, REJECTED }
record BaselineCandidate(String id, Baseline baseline, BaselineCandidateStatus status) {
    BaselineCandidate { DomainChecks.requireText(id, "candidate id"); Objects.requireNonNull(baseline); Objects.requireNonNull(status); }
}
record BaselineDecision(Baseline activeBaseline, BaselineCandidate candidate) { }
