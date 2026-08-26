package io.timeback.domain;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** Produces exactly one current effective decision for every session-backed atomic interval. */
public final class ContextEngine {
    public List<EffectiveDecision> analyze(Collection<AppSession> sessions, Map<String, AppProfile> apps,
                                           Collection<ActivityRecord> activities, Collection<ContextRevision> revisions) {
        Objects.requireNonNull(sessions, "sessions");
        List<Instant> boundaries = new ArrayList<>();
        addBoundaries(boundaries, sessions.stream().map(AppSession::interval).toList());
        addBoundaries(boundaries, activities.stream().map(ActivityRecord::interval).toList());
        addBoundaries(boundaries, revisions.stream().map(ContextRevision::interval).toList());
        List<EffectiveDecision> decisions = new ArrayList<>();
        for (int index = 0; index < boundaries.size() - 1; index++) {
            Interval segment = new Interval(boundaries.get(index), boundaries.get(index + 1));
            List<AppSession> activeSessions = sessions.stream().filter(session -> session.interval().contains(segment)).toList();
            if (activeSessions.isEmpty()) continue;
            List<ActivityRecord> activeActivities = activities.stream().filter(activity -> activity.interval().contains(segment)).toList();
            decisions.add(decide(segment, activeSessions, apps, activeActivities, revisions));
        }
        return List.copyOf(decisions);
    }

    public Duration wasteDuration(Collection<EffectiveDecision> decisions) {
        return Interval.unionDuration(decisions.stream().filter(decision -> decision.classification() == Classification.WASTE)
                .filter(decision -> decision.status() != DecisionStatus.CONFIRMATION_REQUIRED).map(EffectiveDecision::interval).toList());
    }

    private EffectiveDecision decide(Interval segment, List<AppSession> sessions, Map<String, AppProfile> apps,
                                     List<ActivityRecord> activities, Collection<ContextRevision> revisions) {
        List<ContextRevision> currentConfirmed = revisions.stream().filter(revision -> revision.effectiveState() == EffectiveState.CURRENT)
                .filter(revision -> revision.status() == DecisionStatus.USER_CONFIRMED).filter(revision -> revision.interval().contains(segment)).toList();
        if (currentConfirmed.size() > 1) {
            throw new IllegalStateException("conflicting current confirmed contexts for canonical segment");
        }
        if (currentConfirmed.size() == 1) {
            ContextRevision revision = currentConfirmed.get(0);
            return new EffectiveDecision(segment, revision.classification(), DecisionStatus.USER_CONFIRMED, evidenceIds(sessions, activities, revision), revision);
        }
        Set<Classification> nonNeutral = new HashSet<>();
        for (AppSession session : sessions) {
            AppProfile app = apps.get(session.packageName());
            Classification classification = app == null ? Classification.NEUTRAL : app.effectiveDefault();
            if (classification.isNonNeutral()) nonNeutral.add(classification);
        }
        for (ActivityRecord activity : activities) if (activity.intent().isNonNeutral()) nonNeutral.add(activity.intent());
        Classification classification = nonNeutral.size() > 1 ? Classification.MIXED : nonNeutral.isEmpty() ? Classification.NEUTRAL : nonNeutral.iterator().next();
        DecisionStatus status = classification == Classification.MIXED ? DecisionStatus.CONFIRMATION_REQUIRED : DecisionStatus.AUTO_CLASSIFIED;
        return new EffectiveDecision(segment, classification, status, evidenceIds(sessions, activities, null), null);
    }

    private static List<String> evidenceIds(List<AppSession> sessions, List<ActivityRecord> activities, ContextRevision revision) {
        List<String> ids = new ArrayList<>(); sessions.forEach(session -> ids.add("session:" + session.id())); activities.forEach(activity -> ids.add("activity:" + activity.id())); if (revision != null) ids.add("context:" + revision.id()); return ids;
    }
    private static void addBoundaries(List<Instant> target, Collection<Interval> intervals) {
        for (Interval interval : intervals) { target.add(interval.start()); target.add(interval.end()); }
        target.sort(Comparator.naturalOrder());
        for (int index = target.size() - 1; index > 0; index--) if (target.get(index).equals(target.get(index - 1))) target.remove(index);
    }
}
