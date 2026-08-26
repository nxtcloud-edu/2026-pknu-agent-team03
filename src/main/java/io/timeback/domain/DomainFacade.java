package io.timeback.domain;

import com.timeback.device.contract.SessionCompletionCause;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Public APP-05~APP-10 boundary. It keeps package-private domain types internal while
 * accepting the device-owned CT-01 session contract and returning stable DTOs.
 */
public final class DomainFacade {
    private final ContextEngine contextEngine;

    public DomainFacade() {
        this(new ContextEngine());
    }

    DomainFacade(ContextEngine contextEngine) {
        this.contextEngine = Objects.requireNonNull(contextEngine, "contextEngine");
    }

    public AnalysisResult analyzeSessions(
            List<com.timeback.device.contract.AppSession> sourceSessions,
            Map<String, ClassificationValue> appDefaults
    ) {
        Objects.requireNonNull(sourceSessions, "sourceSessions");
        Objects.requireNonNull(appDefaults, "appDefaults");

        List<AppSession> sessions = sourceSessions.stream().map(source -> new AppSession(
                source.sessionId(),
                source.packageName(),
                new Interval(
                        Instant.ofEpochMilli(source.range().startAtMillis()),
                        Instant.ofEpochMilli(source.range().endAtMillis())
                ),
                source.completionCause() != SessionCompletionCause.BACKGROUND_EVENT
        )).toList();

        Map<String, AppProfile> apps = new LinkedHashMap<>();
        for (com.timeback.device.contract.AppSession session : sourceSessions) {
            ClassificationValue selected = appDefaults.getOrDefault(
                    session.packageName(),
                    ClassificationValue.NEUTRAL
            );
            apps.put(session.packageName(), AppProfile.classified(
                    session.packageName(),
                    session.packageName(),
                    Classification.valueOf(selected.name()),
                    Instant.EPOCH,
                    Instant.EPOCH
            ));
        }

        List<EffectiveDecision> decisions = contextEngine.analyze(sessions, apps, List.of(), List.of());
        List<ContextSegment> contexts = decisions.stream().map(decision -> new ContextSegment(
                decision.interval().start().toEpochMilli(),
                decision.interval().end().toEpochMilli(),
                ClassificationValue.valueOf(decision.classification().name()),
                decision.status().name(),
                decision.evidenceIds()
        )).toList();
        long wasteMillis = contextEngine.wasteDuration(decisions).toMillis();
        return new AnalysisResult(contexts, wasteMillis);
    }

    public enum ClassificationValue {
        PRODUCTIVE, LEISURE, WASTE, MIXED, NEUTRAL
    }

    public record ContextSegment(
            long startAtMillis,
            long endAtMillis,
            ClassificationValue classification,
            String decisionStatus,
            List<String> evidenceIds
    ) {
        public ContextSegment {
            evidenceIds = List.copyOf(evidenceIds);
        }
    }

    public record AnalysisResult(List<ContextSegment> contexts, long wasteDurationMillis) {
        public AnalysisResult {
            contexts = List.copyOf(contexts);
        }
    }
}
