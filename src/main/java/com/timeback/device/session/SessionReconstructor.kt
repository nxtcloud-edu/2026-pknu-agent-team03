package com.timeback.device.session

import com.timeback.device.contract.AppSession
import com.timeback.device.contract.CommitResult
import com.timeback.device.contract.DataOwnerScope
import com.timeback.device.contract.DeviceDataAuthority
import com.timeback.device.contract.OpenSessionCandidate
import com.timeback.device.contract.ReconstructionResult
import com.timeback.device.contract.ScreenEndEvent
import com.timeback.device.contract.ScreenStateSource
import com.timeback.device.contract.SessionCompletionCause
import com.timeback.device.contract.StableIds
import com.timeback.device.contract.TimeRange
import com.timeback.device.contract.TimeSource
import com.timeback.device.contract.UsageEvent
import com.timeback.device.contract.UsageEventKind

class SessionReconstructor(
    private val authority: DeviceDataAuthority,
    private val screenStateSource: ScreenStateSource,
    private val timeSource: TimeSource,
) {
    fun reconstruct(owner: DataOwnerScope, impactRange: TimeRange): ReconstructionResult {
        val usageEvents = authority.readUsageEvents(owner, impactRange)
        val screenEvents = try {
            screenStateSource.queryScreenEndEvents(impactRange)
        } catch (error: RuntimeException) {
            return ReconstructionResult.RetryableFailure(
                error.message ?: "screen state source unavailable",
            )
        }

        var open = authority.readOpenSessionCandidate(owner)
        val logicalSessions = mutableListOf<LogicalSession>()
        val tokens = buildTokens(usageEvents, screenEvents)

        tokens.forEach { token ->
            when (token) {
                is Token.Usage -> {
                    val event = token.event
                    when (event.kind) {
                        UsageEventKind.FOREGROUND -> {
                            val current = open
                            if (current == null) {
                                open = event.toCandidate()
                            } else if (current.packageName != event.packageName) {
                                close(
                                    current,
                                    event.occurredAtMillis,
                                    SessionCompletionCause.NEXT_APP_FOREGROUND,
                                    event.eventId,
                                )?.let(logicalSessions::add)
                                open = event.toCandidate()
                            }
                        }

                        UsageEventKind.BACKGROUND -> {
                            val current = open
                            if (current != null && current.packageName == event.packageName) {
                                close(
                                    current,
                                    event.occurredAtMillis,
                                    SessionCompletionCause.BACKGROUND_EVENT,
                                    event.eventId,
                                )?.let(logicalSessions::add)
                                if (event.occurredAtMillis >= current.startedAtMillis) {
                                    open = null
                                }
                            }
                        }
                    }
                }

                is Token.Screen -> {
                    val current = open
                    if (current != null && token.event.occurredAtMillis >= current.startedAtMillis) {
                        close(
                            current,
                            token.event.occurredAtMillis,
                            SessionCompletionCause.SCREEN_ENDED,
                            "screen:${token.event.occurredAtMillis}",
                        )?.let(logicalSessions::add)
                        open = null
                    }
                }
            }
        }

        val sessions = logicalSessions.flatMap(::splitAtLocalMidnights)
        return when (
            val committed = authority.replaceSessions(
                owner = owner,
                impactRange = impactRange,
                sessions = sessions,
                openCandidate = open,
                occurredAtMillis = timeSource.nowMillis(),
            )
        ) {
            is CommitResult.Failure -> ReconstructionResult.RetryableFailure(committed.reason)
            is CommitResult.Success -> ReconstructionResult.Success(
                sessions = sessions,
                openCandidate = open,
                committedChanges = committed.changes,
            )
        }
    }

    private fun buildTokens(
        usageEvents: List<UsageEvent>,
        screenEvents: List<ScreenEndEvent>,
    ): List<Token> = buildList {
        usageEvents.forEach { add(Token.Usage(it)) }
        screenEvents.forEach { add(Token.Screen(it)) }
    }.sortedWith(
        compareBy<Token>(
            { it.occurredAtMillis },
            { if (it is Token.Usage) 0 else 1 },
            { if (it is Token.Usage) it.event.sourceOrder else Int.MAX_VALUE },
        ),
    )

    private fun close(
        candidate: OpenSessionCandidate,
        endedAtMillis: Long,
        cause: SessionCompletionCause,
        closingEvidenceId: String,
    ): LogicalSession? {
        if (endedAtMillis <= candidate.startedAtMillis) return null
        val logicalId = StableIds.logicalSession(
            candidate.owner,
            candidate.packageName,
            candidate.startedAtMillis,
            endedAtMillis,
        )
        return LogicalSession(
            logicalSessionId = logicalId,
            owner = candidate.owner,
            packageName = candidate.packageName,
            range = TimeRange(candidate.startedAtMillis, endedAtMillis),
            completionCause = cause,
            sourceEventIds = listOf(candidate.sourceEventId, closingEvidenceId),
        )
    }

    private fun splitAtLocalMidnights(logical: LogicalSession): List<AppSession> {
        val boundaries = timeSource.localMidnightBoundaries(logical.range)
            .filter { it > logical.range.startAtMillis && it < logical.range.endAtMillis }
            .distinct()
            .sorted()
        val points = listOf(logical.range.startAtMillis) + boundaries + logical.range.endAtMillis
        return points.zipWithNext { start, end ->
            val partRange = TimeRange(start, end)
            AppSession(
                sessionId = StableIds.sessionPart(logical.logicalSessionId, partRange),
                logicalSessionId = logical.logicalSessionId,
                owner = logical.owner,
                packageName = logical.packageName,
                range = partRange,
                completionCause = logical.completionCause,
                sourceEventIds = logical.sourceEventIds,
            )
        }
    }

    private fun UsageEvent.toCandidate(): OpenSessionCandidate = OpenSessionCandidate(
        owner = owner,
        packageName = packageName,
        startedAtMillis = occurredAtMillis,
        sourceEventId = eventId,
    )

    private sealed interface Token {
        val occurredAtMillis: Long

        data class Usage(val event: UsageEvent) : Token {
            override val occurredAtMillis: Long = event.occurredAtMillis
        }

        data class Screen(val event: ScreenEndEvent) : Token {
            override val occurredAtMillis: Long = event.occurredAtMillis
        }
    }

    private data class LogicalSession(
        val logicalSessionId: String,
        val owner: DataOwnerScope,
        val packageName: String,
        val range: TimeRange,
        val completionCause: SessionCompletionCause,
        val sourceEventIds: List<String>,
    )
}
