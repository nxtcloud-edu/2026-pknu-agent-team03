package com.timeback.device.contract

import java.time.Instant
import java.time.ZoneId

@JvmInline
value class DataOwnerScope(val value: String) {
    init {
        require(value.isNotBlank()) { "owner scope must not be blank" }
    }
}

data class TimeRange(val startAtMillis: Long, val endAtMillis: Long) {
    init {
        require(startAtMillis < endAtMillis) { "time range must be start-inclusive and end-exclusive" }
    }

    val durationMillis: Long get() = endAtMillis - startAtMillis

    fun contains(timestampMillis: Long): Boolean =
        timestampMillis >= startAtMillis && timestampMillis < endAtMillis

    fun overlaps(other: TimeRange): Boolean =
        startAtMillis < other.endAtMillis && other.startAtMillis < endAtMillis
}

enum class UsageEventKind {
    FOREGROUND,
    BACKGROUND,
}

data class ObservedUsageEvent(
    val packageName: String,
    val kind: UsageEventKind,
    val occurredAtMillis: Long,
    val occurrenceInTimestampGroup: Int = 0,
) {
    init {
        require(packageName.isNotBlank()) { "package name must not be blank" }
        require(occurrenceInTimestampGroup >= 0) { "occurrence must not be negative" }
    }
}

data class UsageEvent(
    val eventId: String,
    val owner: DataOwnerScope,
    val packageName: String,
    val kind: UsageEventKind,
    val occurredAtMillis: Long,
    val collectedAtMillis: Long,
    val sourceOrder: Int,
)

data class CollectionCheckpoint(
    val owner: DataOwnerScope,
    val successfulThroughMillis: Long,
)

enum class SessionCompletionCause {
    BACKGROUND_EVENT,
    NEXT_APP_FOREGROUND,
    SCREEN_ENDED,
}

data class OpenSessionCandidate(
    val owner: DataOwnerScope,
    val packageName: String,
    val startedAtMillis: Long,
    val sourceEventId: String,
)

data class AppSession(
    val sessionId: String,
    val logicalSessionId: String,
    val owner: DataOwnerScope,
    val packageName: String,
    val range: TimeRange,
    val completionCause: SessionCompletionCause,
    val sourceEventIds: List<String>,
) {
    val durationMillis: Long get() = range.durationMillis
}

/**
 * APP-05–APP-09가 APP-11에 맡기는 논리 레코드의 저장 표현이다.
 * payload는 트랙 통합 어댑터가 직렬화하며 APP-11은 도메인 의미를 재계산하지 않는다.
 */
data class DeviceRecord(
    val recordId: String,
    val owner: DataOwnerScope,
    val entityType: DeviceEntityType,
    val range: TimeRange?,
    val payload: String,
) {
    init {
        require(recordId.isNotBlank()) { "record id must not be blank" }
    }
}

data class ScreenEndEvent(val occurredAtMillis: Long)

enum class DeviceEntityType {
    USER,
    APP,
    USAGE_EVENT,
    APP_SESSION,
    ACTIVITY,
    CONTEXT,
    BASELINE,
    GOAL,
    RECOVERED_TIME,
}

enum class ChangeOperation {
    CREATE,
    UPDATE,
    DELETE,
}

data class ChangeCursor(val sequence: Long = 0) {
    init {
        require(sequence >= 0) { "cursor sequence must not be negative" }
    }
}

data class CommittedChange(
    val sequence: Long,
    val changeId: String,
    val owner: DataOwnerScope,
    val entityType: DeviceEntityType,
    val entityId: String,
    val operation: ChangeOperation,
    val occurredAtMillis: Long,
)

data class CommittedChangePage(
    val changes: List<CommittedChange>,
    val nextCursor: ChangeCursor,
)

sealed interface CommitResult {
    data class Success(
        val changes: List<CommittedChange>,
        val nextCursor: ChangeCursor,
        val createdCount: Int = 0,
    ) : CommitResult

    data class Failure(val reason: String) : CommitResult
}

enum class UsageAccessStatus {
    GRANTED,
    NOT_GRANTED,
}

sealed interface AccessState {
    val observedAtMillis: Long

    data class Granted(override val observedAtMillis: Long) : AccessState
    data class Blocked(override val observedAtMillis: Long) : AccessState
    data class Failure(
        override val observedAtMillis: Long,
        val reason: String,
    ) : AccessState
}

sealed interface SettingsOpenResult {
    data object Opened : SettingsOpenResult
    data class Failure(val reason: String) : SettingsOpenResult
}

sealed interface CollectionResult {
    data class Success(
        val effectiveRange: TimeRange,
        val observedCount: Int,
        val createdCount: Int,
        val checkpoint: CollectionCheckpoint,
    ) : CollectionResult

    data object PermissionRequired : CollectionResult
    data class RetryableFailure(val reason: String) : CollectionResult
    data class Failure(val reason: String) : CollectionResult
}

sealed interface ReconstructionResult {
    data class Success(
        val sessions: List<AppSession>,
        val openCandidate: OpenSessionCandidate?,
        val committedChanges: List<CommittedChange>,
    ) : ReconstructionResult

    data class RetryableFailure(val reason: String) : ReconstructionResult
    data class Failure(val reason: String) : ReconstructionResult
}

interface UsageAccessGateway {
    fun readCurrentStatus(): UsageAccessStatus
    fun openUsageAccessSettings(): Boolean
}

interface UsageEventSource {
    fun queryEvents(range: TimeRange): List<ObservedUsageEvent>
}

interface ScreenStateSource {
    fun queryScreenEndEvents(range: TimeRange): List<ScreenEndEvent>
}

interface TimeSource {
    fun nowMillis(): Long
    fun zoneId(): ZoneId
    fun localMidnightBoundaries(range: TimeRange): List<Long>
}

interface DeviceDataAuthority {
    fun saveRecords(
        owner: DataOwnerScope,
        records: List<DeviceRecord>,
        occurredAtMillis: Long,
    ): CommitResult

    fun readRecord(
        owner: DataOwnerScope,
        entityType: DeviceEntityType,
        recordId: String,
    ): DeviceRecord?

    fun readPeriodRecords(
        owner: DataOwnerScope,
        entityType: DeviceEntityType,
        range: TimeRange,
    ): List<DeviceRecord>

    fun replacePeriodRecords(
        owner: DataOwnerScope,
        entityType: DeviceEntityType,
        impactRange: TimeRange,
        records: List<DeviceRecord>,
        occurredAtMillis: Long,
    ): CommitResult

    fun readCheckpoint(owner: DataOwnerScope): CollectionCheckpoint?

    fun commitCollection(
        owner: DataOwnerScope,
        events: List<UsageEvent>,
        checkpoint: CollectionCheckpoint,
    ): CommitResult

    fun readUsageEvents(owner: DataOwnerScope, range: TimeRange): List<UsageEvent>

    fun readOpenSessionCandidate(owner: DataOwnerScope): OpenSessionCandidate?

    fun replaceSessions(
        owner: DataOwnerScope,
        impactRange: TimeRange,
        sessions: List<AppSession>,
        openCandidate: OpenSessionCandidate?,
        occurredAtMillis: Long,
    ): CommitResult

    fun readSessions(owner: DataOwnerScope, range: TimeRange): List<AppSession>

    fun readCommittedChanges(
        owner: DataOwnerScope,
        after: ChangeCursor,
        limit: Int = 100,
    ): CommittedChangePage

    fun deleteScope(
        owner: DataOwnerScope,
        entityTypes: Set<DeviceEntityType>,
        occurredAtMillis: Long,
    ): CommitResult
}

internal fun Long.asInstant(): Instant = Instant.ofEpochMilli(this)
