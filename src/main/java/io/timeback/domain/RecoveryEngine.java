package io.timeback.domain;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

/** Assigns recovered time once per actual interval and preserves stable overlap choices. */
public final class RecoveryEngine {
    public TimerCompletion completeTimer(RunningTimer timer, Instant completedAt, String recoveredId) {
        if (!completedAt.isAfter(timer.startedAt())) throw new IllegalArgumentException("completedAt must be after timer start");
        return new TimerCompletion(new RecoveredTime(recoveredId, timer.goalId(), RecoveryMethod.TIMER, new Interval(timer.startedAt(), completedAt), completedAt), Optional.empty());
    }

    public List<RecoverySegment> segments(Collection<RecoveredTime> records, Collection<OverlapResolution> resolutions) {
        TreeSet<Instant> boundaries = new TreeSet<>(); records.forEach(record -> { boundaries.add(record.interval().start()); boundaries.add(record.interval().end()); });
        List<Instant> points = new ArrayList<>(boundaries); List<RecoverySegment> result = new ArrayList<>();
        for (int index = 0; index < points.size() - 1; index++) {
            Interval segment = new Interval(points.get(index), points.get(index + 1));
            List<RecoveredTime> active = records.stream().filter(record -> record.interval().contains(segment)).toList();
            if (active.isEmpty()) continue;
            Set<String> sourceIds = active.stream().map(RecoveredTime::id).collect(java.util.stream.Collectors.toSet());
            Set<String> goalIds = active.stream().map(RecoveredTime::goalId).collect(java.util.stream.Collectors.toSet());
            String representative = goalIds.size() == 1 ? goalIds.iterator().next() : matchingResolution(sourceIds, segment, resolutions).map(OverlapResolution::representativeGoalId).orElse(null);
            result.add(new RecoverySegment(segment, sourceIds, goalIds, representative));
        }
        return List.copyOf(result);
    }

    public GoalProgress lifetimeProgress(Goal goal, Collection<RecoverySegment> segments) { return progress(goal, segments, null); }
    public GoalProgress periodSummary(Goal goal, Collection<RecoverySegment> segments, Interval period) { return progress(goal, segments, period); }
    public Duration assignedRecovered(Collection<RecoverySegment> segments, Interval period) { return segments.stream().filter(segment -> !segment.pending()).map(segment -> clippedDuration(segment.interval(), period)).reduce(Duration.ZERO, Duration::plus); }
    public Duration pendingRecovered(Collection<RecoverySegment> segments, Interval period) { return pendingFor(segments, period); }

    public OverlapResolution resolveOverlap(Collection<RecoverySegment> segments, Set<String> sourceRecoveredIds,
                                              Interval effectiveInterval, String representativeGoalId, int revision) {
        boolean matchingPending = segments.stream().anyMatch(segment -> segment.pending()
                && segment.sourceRecoveredIds().equals(sourceRecoveredIds)
                && effectiveInterval.contains(segment.interval())
                && segment.candidateGoalIds().contains(representativeGoalId));
        if (!matchingPending) throw new IllegalArgumentException("representative is not valid for pending overlap");
        return new OverlapResolution(sourceRecoveredIds, effectiveInterval, representativeGoalId, revision);
    }

    private static GoalProgress progress(Goal goal, Collection<RecoverySegment> segments, Interval period) {
        Duration accumulated = assignedFor(goal.id(), segments, period); Duration pending = pendingFor(segments, period);
        return new GoalProgress(goal.id(), accumulated, pending, goal.targetDuration(), Ratio.from(accumulated, goal.targetDuration()));
    }
    private static Optional<OverlapResolution> matchingResolution(Set<String> sourceIds, Interval segment, Collection<OverlapResolution> resolutions) {
        return resolutions.stream().filter(resolution -> resolution.sourceRecoveredIds().equals(sourceIds)).filter(resolution -> resolution.effectiveInterval().contains(segment)).max(Comparator.comparingInt(OverlapResolution::revision));
    }
    private static Duration assignedFor(String goalId, Collection<RecoverySegment> segments, Interval period) { return segments.stream().filter(segment -> goalId.equals(segment.representativeGoalId())).map(segment -> clippedDuration(segment.interval(), period)).reduce(Duration.ZERO, Duration::plus); }
    private static Duration pendingFor(Collection<RecoverySegment> segments, Interval period) { return segments.stream().filter(RecoverySegment::pending).map(segment -> clippedDuration(segment.interval(), period)).reduce(Duration.ZERO, Duration::plus); }
    private static Duration clippedDuration(Interval interval, Interval period) { return period == null ? interval.duration() : interval.intersection(period).map(Interval::duration).orElse(Duration.ZERO); }
}
