package com.timeback.device.session;

import com.timeback.device.contract.AppSession;
import com.timeback.device.contract.CommitResult;
import com.timeback.device.contract.DataOwnerScope;
import com.timeback.device.contract.DeviceDataAuthority;
import com.timeback.device.contract.OpenSessionCandidate;
import com.timeback.device.contract.ReconstructionResult;
import com.timeback.device.contract.ScreenEndEvent;
import com.timeback.device.contract.ScreenStateSource;
import com.timeback.device.contract.SessionCompletionCause;
import com.timeback.device.contract.StableIds;
import com.timeback.device.contract.TimeRange;
import com.timeback.device.contract.TimeSource;
import com.timeback.device.contract.UsageEvent;
import com.timeback.device.contract.UsageEventKind;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class SessionReconstructor {
    private final DeviceDataAuthority authority;
    private final ScreenStateSource screenStateSource;
    private final TimeSource timeSource;

    public SessionReconstructor(
            DeviceDataAuthority authority,
            ScreenStateSource screenStateSource,
            TimeSource timeSource
    ) {
        this.authority = authority;
        this.screenStateSource = screenStateSource;
        this.timeSource = timeSource;
    }

    public ReconstructionResult reconstruct(DataOwnerScope owner, TimeRange impactRange) {
        List<UsageEvent> usageEvents = authority.readUsageEvents(owner, impactRange);
        List<ScreenEndEvent> screenEvents;
        try {
            screenEvents = screenStateSource.queryScreenEndEvents(impactRange);
        } catch (RuntimeException error) {
            return new ReconstructionResult.RetryableFailure(
                    error.getMessage() == null ? "screen state source unavailable" : error.getMessage()
            );
        }

        OpenSessionCandidate open = authority.readOpenSessionCandidate(owner);
        List<LogicalSession> logicalSessions = new ArrayList<>();
        for (Token token : buildTokens(usageEvents, screenEvents)) {
            if (token instanceof UsageToken usageToken) {
                UsageEvent event = usageToken.event();
                if (event.kind() == UsageEventKind.FOREGROUND) {
                    if (open == null) {
                        open = toCandidate(event);
                    } else if (!open.packageName().equals(event.packageName())) {
                        LogicalSession completed = close(
                                open,
                                event.occurredAtMillis(),
                                SessionCompletionCause.NEXT_APP_FOREGROUND,
                                event.eventId()
                        );
                        if (completed != null) {
                            logicalSessions.add(completed);
                        }
                        open = toCandidate(event);
                    }
                } else if (open != null && open.packageName().equals(event.packageName())) {
                    LogicalSession completed = close(
                            open,
                            event.occurredAtMillis(),
                            SessionCompletionCause.BACKGROUND_EVENT,
                            event.eventId()
                    );
                    if (completed != null) {
                        logicalSessions.add(completed);
                    }
                    if (event.occurredAtMillis() >= open.startedAtMillis()) {
                        open = null;
                    }
                }
            } else if (token instanceof ScreenToken screenToken && open != null) {
                ScreenEndEvent event = screenToken.event();
                if (event.occurredAtMillis() >= open.startedAtMillis()) {
                    LogicalSession completed = close(
                            open,
                            event.occurredAtMillis(),
                            SessionCompletionCause.SCREEN_ENDED,
                            "screen:" + event.occurredAtMillis()
                    );
                    if (completed != null) {
                        logicalSessions.add(completed);
                    }
                    open = null;
                }
            }
        }

        List<AppSession> sessions = logicalSessions.stream()
                .flatMap(session -> splitAtLocalMidnights(session).stream())
                .toList();
        CommitResult committed = authority.replaceSessions(
                owner,
                impactRange,
                sessions,
                open,
                timeSource.nowMillis()
        );
        if (committed instanceof CommitResult.Failure failure) {
            return new ReconstructionResult.RetryableFailure(failure.reason());
        }
        CommitResult.Success success = (CommitResult.Success) committed;
        return new ReconstructionResult.Success(sessions, open, success.changes());
    }

    private List<Token> buildTokens(
            List<UsageEvent> usageEvents,
            List<ScreenEndEvent> screenEvents
    ) {
        List<Token> tokens = new ArrayList<>();
        usageEvents.forEach(event -> tokens.add(new UsageToken(event)));
        screenEvents.forEach(event -> tokens.add(new ScreenToken(event)));
        tokens.sort(Comparator
                .comparingLong(Token::occurredAtMillis)
                .thenComparingInt(token -> token instanceof UsageToken ? 0 : 1)
                .thenComparingInt(token -> token instanceof UsageToken usage
                        ? usage.event().sourceOrder()
                        : Integer.MAX_VALUE));
        return tokens;
    }

    private LogicalSession close(
            OpenSessionCandidate candidate,
            long endedAtMillis,
            SessionCompletionCause cause,
            String closingEvidenceId
    ) {
        if (endedAtMillis <= candidate.startedAtMillis()) {
            return null;
        }
        String logicalId = StableIds.logicalSession(
                candidate.owner(),
                candidate.packageName(),
                candidate.startedAtMillis(),
                endedAtMillis
        );
        return new LogicalSession(
                logicalId,
                candidate.owner(),
                candidate.packageName(),
                new TimeRange(candidate.startedAtMillis(), endedAtMillis),
                cause,
                List.of(candidate.sourceEventId(), closingEvidenceId)
        );
    }

    private List<AppSession> splitAtLocalMidnights(LogicalSession logical) {
        List<Long> points = new ArrayList<>();
        points.add(logical.range().startAtMillis());
        timeSource.localMidnightBoundaries(logical.range()).stream()
                .filter(boundary -> boundary > logical.range().startAtMillis())
                .filter(boundary -> boundary < logical.range().endAtMillis())
                .distinct()
                .sorted()
                .forEach(points::add);
        points.add(logical.range().endAtMillis());

        List<AppSession> result = new ArrayList<>();
        for (int index = 0; index < points.size() - 1; index++) {
            TimeRange partRange = new TimeRange(points.get(index), points.get(index + 1));
            result.add(new AppSession(
                    StableIds.sessionPart(logical.logicalSessionId(), partRange),
                    logical.logicalSessionId(),
                    logical.owner(),
                    logical.packageName(),
                    partRange,
                    logical.completionCause(),
                    logical.sourceEventIds()
            ));
        }
        return List.copyOf(result);
    }

    private OpenSessionCandidate toCandidate(UsageEvent event) {
        return new OpenSessionCandidate(
                event.owner(),
                event.packageName(),
                event.occurredAtMillis(),
                event.eventId()
        );
    }

    private sealed interface Token permits UsageToken, ScreenToken {
        long occurredAtMillis();
    }

    private record UsageToken(UsageEvent event) implements Token {
        @Override
        public long occurredAtMillis() {
            return event.occurredAtMillis();
        }
    }

    private record ScreenToken(ScreenEndEvent event) implements Token {
        @Override
        public long occurredAtMillis() {
            return event.occurredAtMillis();
        }
    }

    private record LogicalSession(
            String logicalSessionId,
            DataOwnerScope owner,
            String packageName,
            TimeRange range,
            SessionCompletionCause completionCause,
            List<String> sourceEventIds
    ) {}
}
