package io.timeback.domain;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Remaining approved pure-domain boundary regressions. */
final class AdditionalDomainRegression {
    private AdditionalDomainRegression() { }

    static void run() {
        Instant now = Instant.parse("2026-03-01T00:00:00Z");
        MetricsEngine metrics = new MetricsEngine();
        ZoneId utc = ZoneId.of("UTC");
        List<MeasurementDay> zeroDays = new ArrayList<>();
        for (int index = 0; index < 7; index++) {
            zeroDays.add(new MeasurementDay(new MeasurementKey("anonymous", LocalDate.of(2026, 3, 1).plusDays(index), utc), CoverageStatus.COMPLETE, Duration.ZERO, 1, 1));
        }
        check(metrics.firstCompleteBaseline(zeroDays).orElseThrow().weeklyWaste().isZero(), "complete zero-waste days are valid baseline inputs");
        List<MeasurementDay> gapped = new ArrayList<>(zeroDays.subList(0, 4));
        gapped.addAll(zeroDays.subList(5, 7));
        check(metrics.firstCompleteBaseline(gapped).isEmpty(), "coverage gap resets seven-day observation");

        List<MeasurementDay> nonDivisible = new ArrayList<>();
        for (int index = 0; index < 7; index++) {
            nonDivisible.add(new MeasurementDay(new MeasurementKey("anonymous", LocalDate.of(2026, 3, 8).plusDays(index), utc), CoverageStatus.COMPLETE,
                    Duration.ofNanos(index == 0 ? 501 : 500), 1, 1));
        }
        Baseline exactBaseline = metrics.firstCompleteBaseline(nonDivisible).orElseThrow();
        Ratio dailyAverage = exactBaseline.dailyAverageNanoseconds();
        check(dailyAverage.numerator().equals(BigInteger.valueOf(3501)) && dailyAverage.denominator().equals(BigInteger.valueOf(7)),
                "3,501ns weekly waste retains its exact one-seventh daily average");
        SavedMetrics oneDay = metrics.saved(exactBaseline, Duration.ZERO, BigDecimal.ONE, false);
        check(oneDay.expectedBaseline().numerator().equals(BigInteger.valueOf(3501)) && oneDay.expectedBaseline().denominator().equals(BigInteger.valueOf(7))
                        && oneDay.savedDuration().equals(oneDay.expectedBaseline()),
                "one-day saved metric retains an exact non-integral nanosecond expectation");
        SavedMetrics halfDay = metrics.saved(exactBaseline, Duration.ZERO, new BigDecimal("0.5"), false);
        check(halfDay.expectedBaseline().numerator().equals(BigInteger.valueOf(17505)) && halfDay.expectedBaseline().denominator().equals(BigInteger.valueOf(70)),
                "partial-day saved metric retains the exact baseline fraction");
        SavedMetrics sevenDays = metrics.saved(exactBaseline, Duration.ZERO, BigDecimal.valueOf(7), false);
        check(sevenDays.expectedBaseline().numerator().equals(BigInteger.valueOf(24507)) && sevenDays.expectedBaseline().denominator().equals(BigInteger.valueOf(7)),
                "seven-day expected baseline remains exactly equal to weekly waste");
        SavedMetrics negative = metrics.saved(exactBaseline, Duration.ofNanos(501), BigDecimal.ONE, false);
        check(negative.wasteDelta().numerator().equals(BigInteger.valueOf(-6)) && negative.wasteDelta().denominator().equals(BigInteger.valueOf(7))
                        && negative.savedDuration().numerator().signum() == 0,
                "negative exact delta clamps saved time without rounding");

        AppProfile app = AppProfile.classified("example.app.multi", "Example Multi", Classification.WASTE, now, now);
        Interval interval = new Interval(now, now.plusSeconds(60));
        List<EffectiveDecision> decisions = new ContextEngine().analyze(
                List.of(new AppSession("session", app.packageName(), interval, false)), Map.of(app.packageName(), app),
                List.of(new ActivityRecord("activity", ActivityType.CUSTOM, "Example", Classification.WASTE, interval)), List.of());
        check(decisions.size() == 1 && decisions.get(0).evidenceIds().size() == 2, "multi-evidence produces one canonical decision");

        DomainCommandService commands = new DomainCommandService(metrics);
        Goal created = commands.createGoal("goal", "Example Goal", Duration.ofMinutes(10), now);
        expectIllegal(() -> commands.createGoal("bad", " ", Duration.ofMinutes(10), now));
        BaselineLifecycle lifecycle = new BaselineLifecycle();
        Baseline candidateValue = new Baseline(LocalDate.of(2026, 3, 8), LocalDate.of(2026, 3, 14), Duration.ofMinutes(600));
        BaselineCandidate candidate = lifecycle.propose("candidate", new Baseline(LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 7), Duration.ofMinutes(700)), candidateValue);
        check(lifecycle.decide(candidateValue, candidate, false).activeBaseline().equals(candidateValue), "rejected candidate preserves active baseline");

        RecoveryEngine recovery = new RecoveryEngine();
        RecoveredTime a = new RecoveredTime("a", created.id(), RecoveryMethod.MANUAL, new Interval(now, now.plusSeconds(60)), now);
        RecoveredTime b = new RecoveredTime("b", "other", RecoveryMethod.MANUAL, new Interval(now.plusSeconds(30), now.plusSeconds(90)), now);
        List<RecoverySegment> pending = recovery.segments(List.of(a, b), List.of());
        OverlapResolution resolution = recovery.resolveOverlap(pending, Set.of("a", "b"), new Interval(now.plusSeconds(30), now.plusSeconds(60)), created.id(), 1);
        List<RecoverySegment> resolved = recovery.segments(List.of(a, b), List.of(resolution));
        check(recovery.periodSummary(created, resolved, new Interval(now.plusSeconds(35), now.plusSeconds(55))).accumulatedDuration().equals(Duration.ofSeconds(20)), "clipped child inherits representative resolution");
    }

    private static void check(boolean value, String message) { if (!value) throw new AssertionError(message); }
    private static void expectIllegal(Runnable runnable) { try { runnable.run(); throw new AssertionError("expected illegal input"); } catch (IllegalArgumentException expected) { } }
}
