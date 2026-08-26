package io.timeback.domain;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Pure calculations for MeasurementDay, Baseline, Saved Time, partial coverage, and Recovery Rate. */
public final class MetricsEngine {
    public MeasurementUpsert upsert(Map<MeasurementKey, MeasurementDay> existing, MeasurementDay candidate) {
        Map<MeasurementKey, MeasurementDay> next = new HashMap<>(existing);
        MeasurementDay current = next.get(candidate.key());
        if (current != null && current.contextSourceRevision() == candidate.contextSourceRevision() && current.coverageRevision() == candidate.coverageRevision()) return new MeasurementUpsert(MeasurementUpsertResult.NO_OP, Map.copyOf(next));
        next.put(candidate.key(), candidate);
        return new MeasurementUpsert(MeasurementUpsertResult.APPLIED, Map.copyOf(next));
    }

    public Optional<Baseline> firstCompleteBaseline(List<MeasurementDay> days) {
        List<MeasurementDay> complete = days.stream().filter(day -> day.coverageStatus() == CoverageStatus.COMPLETE).sorted(Comparator.comparing(day -> day.key().localDate())).toList();
        List<MeasurementDay> streak = new ArrayList<>();
        for (MeasurementDay day : complete) {
            if (streak.isEmpty() || day.key().localDate().equals(streak.get(streak.size() - 1).key().localDate().plusDays(1))) streak.add(day); else { streak.clear(); streak.add(day); }
            if (streak.size() == 7) {
                Duration weekly = streak.stream().map(MeasurementDay::wasteDuration).reduce(Duration.ZERO, Duration::plus);
                return Optional.of(new Baseline(streak.get(0).key().localDate(), streak.get(6).key().localDate(), weekly));
            }
        }
        return Optional.empty();
    }

    public SavedMetrics saved(Baseline baseline, Duration currentWaste, BigDecimal dayEquivalent, boolean dataIncomplete) {
        if (dataIncomplete) return new SavedMetrics(MetricStatus.DATA_INCOMPLETE, currentWaste, null, null, null);
        if (baseline == null) return new SavedMetrics(MetricStatus.OBSERVING, currentWaste, null, null, null);
        Ratio expected = multiply(baseline.dailyAverageNanoseconds(), dayEquivalent);
        SignedNanosRatio delta = SignedNanosRatio.subtract(expected, currentWaste);
        return new SavedMetrics(MetricStatus.VALUE, currentWaste, expected, delta, delta.nonNegativeOrZero());
    }

    public PartialSavedMetrics partialSaved(Baseline baseline, Duration currentWaste, BigDecimal elapsedDayEquivalent,
                                            Interval coveredRange, Instant asOf, boolean coverageGap) {
        if (coverageGap) throw new IllegalArgumentException("partial coverage with a gap is DATA_INCOMPLETE, not an in-progress metric");
        return new PartialSavedMetrics(saved(baseline, currentWaste, elapsedDayEquivalent, false), PeriodStatus.IN_PROGRESS,
                ComparisonBasis.PARTIAL_COVERAGE, coveredRange, asOf, elapsedDayEquivalent);
    }

    public RecoveryMetrics recoveryRate(SavedMetrics saved, Duration assignedRecovered, Duration pendingOverlap) {
        RecoveryDecisionState decision = pendingOverlap.isZero() ? RecoveryDecisionState.NONE : RecoveryDecisionState.DECISION_REQUIRED;
        if (saved.status() == MetricStatus.DATA_INCOMPLETE) return new RecoveryMetrics(assignedRecovered, pendingOverlap, RateAvailability.NOT_AVAILABLE, RateUnavailableReason.DATA_INCOMPLETE, decision, null);
        if (saved.status() == MetricStatus.OBSERVING) return new RecoveryMetrics(assignedRecovered, pendingOverlap, RateAvailability.NOT_AVAILABLE, RateUnavailableReason.BASELINE_OBSERVING, decision, null);
        if (saved.savedDuration().numerator().signum() == 0) return new RecoveryMetrics(assignedRecovered, pendingOverlap, RateAvailability.NOT_AVAILABLE, RateUnavailableReason.SAVED_ZERO, decision, null);
        Ratio savedDuration = saved.savedDuration();
        BigInteger[] wholeNanos = savedDuration.numerator().divideAndRemainder(savedDuration.denominator());
        if (wholeNanos[1].signum() == 0) {
            return new RecoveryMetrics(assignedRecovered, pendingOverlap, RateAvailability.AVAILABLE, null, decision,
                    new Ratio(Ratio.nanos(assignedRecovered), wholeNanos[0]));
        }
        return new RecoveryMetrics(assignedRecovered, pendingOverlap, RateAvailability.AVAILABLE, null, decision,
                new Ratio(Ratio.nanos(assignedRecovered).multiply(savedDuration.denominator()), savedDuration.numerator()));
    }

    private static Ratio multiply(Ratio value, BigDecimal multiplier) {
        if (multiplier == null || multiplier.signum() < 0) throw new IllegalArgumentException("day equivalent must be non-negative");
        BigInteger numerator = value.numerator().multiply(multiplier.unscaledValue());
        BigInteger denominator = value.denominator();
        if (multiplier.scale() >= 0) denominator = denominator.multiply(BigInteger.TEN.pow(multiplier.scale()));
        else numerator = numerator.multiply(BigInteger.TEN.pow(-multiplier.scale()));
        return new Ratio(numerator, denominator);
    }
}
