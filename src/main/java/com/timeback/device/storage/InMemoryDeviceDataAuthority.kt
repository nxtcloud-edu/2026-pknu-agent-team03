package com.timeback.device.storage

import com.timeback.device.contract.AppSession
import com.timeback.device.contract.ChangeCursor
import com.timeback.device.contract.ChangeOperation
import com.timeback.device.contract.CollectionCheckpoint
import com.timeback.device.contract.CommitResult
import com.timeback.device.contract.CommittedChange
import com.timeback.device.contract.CommittedChangePage
import com.timeback.device.contract.DataOwnerScope
import com.timeback.device.contract.DeviceDataAuthority
import com.timeback.device.contract.DeviceEntityType
import com.timeback.device.contract.DeviceRecord
import com.timeback.device.contract.OpenSessionCandidate
import com.timeback.device.contract.StableIds
import com.timeback.device.contract.TimeRange
import com.timeback.device.contract.UsageEvent

class InMemoryDeviceDataAuthority : DeviceDataAuthority {
    private val genericRecords = mutableMapOf<
        DataOwnerScope,
        MutableMap<DeviceEntityType, LinkedHashMap<String, DeviceRecord>>,
        >()
    private val usageEvents = mutableMapOf<DataOwnerScope, LinkedHashMap<String, UsageEvent>>()
    private val sessions = mutableMapOf<DataOwnerScope, LinkedHashMap<String, AppSession>>()
    private val checkpoints = mutableMapOf<DataOwnerScope, CollectionCheckpoint>()
    private val openCandidates = mutableMapOf<DataOwnerScope, OpenSessionCandidate>()
    private val changes = mutableMapOf<DataOwnerScope, MutableList<CommittedChange>>()
    private val nextSequences = mutableMapOf<DataOwnerScope, Long>()
    private var failNextReason: String? = null

    @Synchronized
    fun failNextCommit(reason: String = "injected storage failure") {
        failNextReason = reason
    }

    @Synchronized
    override fun saveRecords(
        owner: DataOwnerScope,
        records: List<DeviceRecord>,
        occurredAtMillis: Long,
    ): CommitResult {
        consumeFailure()?.let { return CommitResult.Failure(it) }
        validateGenericRecords(owner, records)?.let { return CommitResult.Failure(it) }
        if (records.map { it.entityType to it.recordId }.toSet().size != records.size) {
            return CommitResult.Failure("duplicate record id in one save request")
        }

        val committed = records.mapNotNull { record ->
            val entityRecords = recordsFor(owner, record.entityType)
            val operation = when (entityRecords[record.recordId]) {
                null -> ChangeOperation.CREATE
                record -> null
                else -> ChangeOperation.UPDATE
            }
            operation?.let {
                newChange(owner, record.entityType, record.recordId, it, occurredAtMillis)
            }
        }
        records.forEach { record -> recordsFor(owner, record.entityType)[record.recordId] = record }
        appendChanges(owner, committed)
        return CommitResult.Success(
            committed,
            currentCursor(owner),
            committed.count { it.operation == ChangeOperation.CREATE },
        )
    }

    @Synchronized
    override fun readRecord(
        owner: DataOwnerScope,
        entityType: DeviceEntityType,
        recordId: String,
    ): DeviceRecord? = genericRecords[owner]?.get(entityType)?.get(recordId)

    @Synchronized
    override fun readPeriodRecords(
        owner: DataOwnerScope,
        entityType: DeviceEntityType,
        range: TimeRange,
    ): List<DeviceRecord> = genericRecords[owner]
        ?.get(entityType)
        .orEmpty()
        .values
        .filter { it.range?.overlaps(range) == true }
        .sortedWith(compareBy({ it.range?.startAtMillis }, { it.range?.endAtMillis }, { it.recordId }))

    @Synchronized
    override fun replacePeriodRecords(
        owner: DataOwnerScope,
        entityType: DeviceEntityType,
        impactRange: TimeRange,
        records: List<DeviceRecord>,
        occurredAtMillis: Long,
    ): CommitResult {
        consumeFailure()?.let { return CommitResult.Failure(it) }
        if (entityType == DeviceEntityType.USAGE_EVENT) {
            return CommitResult.Failure("raw usage events cannot be replaced by period")
        }
        validateGenericRecords(owner, records, entityType)?.let { return CommitResult.Failure(it) }
        if (records.any { it.range?.overlaps(impactRange) != true }) {
            return CommitResult.Failure("replacement record is outside the impact range")
        }
        if (records.map(DeviceRecord::recordId).toSet().size != records.size) {
            return CommitResult.Failure("duplicate record id in replacement")
        }

        val entityRecords = recordsFor(owner, entityType)
        val previous = entityRecords.values
            .filter { it.range?.overlaps(impactRange) == true }
            .associateBy(DeviceRecord::recordId)
        val replacement = records.associateBy(DeviceRecord::recordId)
        val committed = mutableListOf<CommittedChange>()

        previous.values.filterNot { replacement.containsKey(it.recordId) }.forEach { old ->
            committed += newChange(
                owner,
                entityType,
                old.recordId,
                ChangeOperation.DELETE,
                occurredAtMillis,
            )
        }
        replacement.values.forEach { fresh ->
            val operation = when (previous[fresh.recordId]) {
                null -> ChangeOperation.CREATE
                fresh -> null
                else -> ChangeOperation.UPDATE
            }
            if (operation != null) {
                committed += newChange(owner, entityType, fresh.recordId, operation, occurredAtMillis)
            }
        }

        previous.keys.forEach(entityRecords::remove)
        replacement.values.forEach { entityRecords[it.recordId] = it }
        appendChanges(owner, committed)
        return CommitResult.Success(
            committed,
            currentCursor(owner),
            committed.count { it.operation == ChangeOperation.CREATE },
        )
    }

    @Synchronized
    override fun readCheckpoint(owner: DataOwnerScope): CollectionCheckpoint? = checkpoints[owner]

    @Synchronized
    override fun commitCollection(
        owner: DataOwnerScope,
        events: List<UsageEvent>,
        checkpoint: CollectionCheckpoint,
    ): CommitResult {
        consumeFailure()?.let { return CommitResult.Failure(it) }
        if (checkpoint.owner != owner || events.any { it.owner != owner }) {
            return CommitResult.Failure("owner scope violation")
        }

        val records = usageEvents.getOrPut(owner) { linkedMapOf() }
        val newEvents = events.filterNot { records.containsKey(it.eventId) }
        val committed = newEvents.map { event ->
            newChange(
                owner = owner,
                entityType = DeviceEntityType.USAGE_EVENT,
                entityId = event.eventId,
                operation = ChangeOperation.CREATE,
                occurredAtMillis = event.collectedAtMillis,
            )
        }

        newEvents.forEach { records[it.eventId] = it }
        checkpoints[owner] = checkpoint
        appendChanges(owner, committed)
        return CommitResult.Success(
            changes = committed,
            nextCursor = currentCursor(owner),
            createdCount = newEvents.size,
        )
    }

    @Synchronized
    override fun readUsageEvents(owner: DataOwnerScope, range: TimeRange): List<UsageEvent> =
        usageEvents[owner]
            .orEmpty()
            .values
            .filter { range.contains(it.occurredAtMillis) }
            .sortedWith(compareBy({ it.occurredAtMillis }, { it.sourceOrder }, { it.eventId }))

    @Synchronized
    override fun readOpenSessionCandidate(owner: DataOwnerScope): OpenSessionCandidate? =
        openCandidates[owner]

    @Synchronized
    override fun replaceSessions(
        owner: DataOwnerScope,
        impactRange: TimeRange,
        sessions: List<AppSession>,
        openCandidate: OpenSessionCandidate?,
        occurredAtMillis: Long,
    ): CommitResult {
        consumeFailure()?.let { return CommitResult.Failure(it) }
        if (sessions.any { it.owner != owner } || (openCandidate != null && openCandidate.owner != owner)) {
            return CommitResult.Failure("owner scope violation")
        }
        if (sessions.any { !it.range.overlaps(impactRange) }) {
            return CommitResult.Failure("replacement session is outside the impact range")
        }

        val records = this.sessions.getOrPut(owner) { linkedMapOf() }
        val previous = records.values.filter { it.range.overlaps(impactRange) }.associateBy { it.sessionId }
        val replacement = sessions.associateBy { it.sessionId }
        val committed = mutableListOf<CommittedChange>()

        previous.values
            .filterNot { replacement.containsKey(it.sessionId) }
            .forEach { old ->
                committed += newChange(
                    owner,
                    DeviceEntityType.APP_SESSION,
                    old.sessionId,
                    ChangeOperation.DELETE,
                    occurredAtMillis,
                )
            }

        replacement.values.forEach { fresh ->
            val operation = when (previous[fresh.sessionId]) {
                null -> ChangeOperation.CREATE
                fresh -> null
                else -> ChangeOperation.UPDATE
            }
            if (operation != null) {
                committed += newChange(
                    owner,
                    DeviceEntityType.APP_SESSION,
                    fresh.sessionId,
                    operation,
                    occurredAtMillis,
                )
            }
        }

        previous.keys.forEach(records::remove)
        replacement.values.forEach { records[it.sessionId] = it }
        if (openCandidate == null) {
            openCandidates.remove(owner)
        } else {
            openCandidates[owner] = openCandidate
        }
        appendChanges(owner, committed)
        return CommitResult.Success(committed, currentCursor(owner), committed.count {
            it.operation == ChangeOperation.CREATE
        })
    }

    @Synchronized
    override fun readSessions(owner: DataOwnerScope, range: TimeRange): List<AppSession> =
        sessions[owner]
            .orEmpty()
            .values
            .filter { it.range.overlaps(range) }
            .sortedWith(compareBy({ it.range.startAtMillis }, { it.range.endAtMillis }, { it.sessionId }))

    @Synchronized
    override fun readCommittedChanges(
        owner: DataOwnerScope,
        after: ChangeCursor,
        limit: Int,
    ): CommittedChangePage {
        require(limit > 0) { "change page limit must be positive" }
        val page = changes[owner]
            .orEmpty()
            .asSequence()
            .filter { it.sequence > after.sequence }
            .take(limit)
            .toList()
        return CommittedChangePage(
            changes = page,
            nextCursor = page.lastOrNull()?.let { ChangeCursor(it.sequence) } ?: after,
        )
    }

    @Synchronized
    override fun deleteScope(
        owner: DataOwnerScope,
        entityTypes: Set<DeviceEntityType>,
        occurredAtMillis: Long,
    ): CommitResult {
        consumeFailure()?.let { return CommitResult.Failure(it) }
        val committed = mutableListOf<CommittedChange>()

        if (DeviceEntityType.USAGE_EVENT in entityTypes) {
            usageEvents.remove(owner).orEmpty().values.forEach { event ->
                committed += newChange(
                    owner,
                    DeviceEntityType.USAGE_EVENT,
                    event.eventId,
                    ChangeOperation.DELETE,
                    occurredAtMillis,
                )
            }
            checkpoints.remove(owner)
        }
        if (DeviceEntityType.APP_SESSION in entityTypes) {
            sessions.remove(owner).orEmpty().values.forEach { session ->
                committed += newChange(
                    owner,
                    DeviceEntityType.APP_SESSION,
                    session.sessionId,
                    ChangeOperation.DELETE,
                    occurredAtMillis,
                )
            }
            openCandidates.remove(owner)
        }
        entityTypes.forEach { entityType ->
            genericRecords[owner]?.remove(entityType).orEmpty().values.forEach { record ->
                committed += newChange(
                    owner,
                    entityType,
                    record.recordId,
                    ChangeOperation.DELETE,
                    occurredAtMillis,
                )
            }
        }
        if (genericRecords[owner].isNullOrEmpty()) genericRecords.remove(owner)

        appendChanges(owner, committed)
        return CommitResult.Success(committed, currentCursor(owner))
    }

    private fun consumeFailure(): String? = failNextReason.also { failNextReason = null }

    private fun newChange(
        owner: DataOwnerScope,
        entityType: DeviceEntityType,
        entityId: String,
        operation: ChangeOperation,
        occurredAtMillis: Long,
    ): CommittedChange {
        val sequence = (nextSequences[owner] ?: 0L) + 1L
        nextSequences[owner] = sequence
        return CommittedChange(
            sequence = sequence,
            changeId = StableIds.change(owner, sequence, entityType, entityId, operation),
            owner = owner,
            entityType = entityType,
            entityId = entityId,
            operation = operation,
            occurredAtMillis = occurredAtMillis,
        )
    }

    private fun appendChanges(owner: DataOwnerScope, committed: List<CommittedChange>) {
        if (committed.isNotEmpty()) {
            changes.getOrPut(owner) { mutableListOf() }.addAll(committed)
        }
    }

    private fun currentCursor(owner: DataOwnerScope): ChangeCursor =
        ChangeCursor(nextSequences[owner] ?: 0L)

    private fun recordsFor(
        owner: DataOwnerScope,
        entityType: DeviceEntityType,
    ): LinkedHashMap<String, DeviceRecord> = genericRecords
        .getOrPut(owner) { mutableMapOf() }
        .getOrPut(entityType) { linkedMapOf() }

    private fun validateGenericRecords(
        owner: DataOwnerScope,
        records: List<DeviceRecord>,
        requiredType: DeviceEntityType? = null,
    ): String? = when {
        records.any { it.owner != owner } -> "owner scope violation"
        requiredType != null && records.any { it.entityType != requiredType } ->
            "record entity type does not match replacement type"
        else -> null
    }
}
