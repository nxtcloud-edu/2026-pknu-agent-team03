package com.timeback.device.collection

import com.timeback.device.access.AccessGate
import com.timeback.device.contract.AccessState
import com.timeback.device.contract.CollectionCheckpoint
import com.timeback.device.contract.CollectionResult
import com.timeback.device.contract.CommitResult
import com.timeback.device.contract.DataOwnerScope
import com.timeback.device.contract.DeviceDataAuthority
import com.timeback.device.contract.StableIds
import com.timeback.device.contract.TimeRange
import com.timeback.device.contract.TimeSource
import com.timeback.device.contract.UsageEvent
import com.timeback.device.contract.UsageEventSource
import kotlin.math.max
import kotlin.math.min

class UsageCollector(
    private val accessGate: AccessGate,
    private val source: UsageEventSource,
    private val authority: DeviceDataAuthority,
    private val timeSource: TimeSource,
    private val overlapMillis: Long = 1_000,
) {
    init {
        require(overlapMillis >= 0) { "overlap must not be negative" }
    }

    fun collect(owner: DataOwnerScope, requestedRange: TimeRange): CollectionResult {
        when (val access = accessGate.readAccessState()) {
            is AccessState.Blocked -> return CollectionResult.PermissionRequired
            is AccessState.Failure -> return CollectionResult.Failure(access.reason)
            is AccessState.Granted -> Unit
        }

        val now = timeSource.nowMillis()
        val cappedEnd = min(requestedRange.endAtMillis, now)
        if (cappedEnd <= requestedRange.startAtMillis) {
            return CollectionResult.Failure("requested range does not include a collectible instant")
        }

        val checkpoint = authority.readCheckpoint(owner)
        val effectiveStart = checkpoint
            ?.takeIf { it.successfulThroughMillis > requestedRange.startAtMillis && it.successfulThroughMillis <= cappedEnd }
            ?.let { max(requestedRange.startAtMillis, it.successfulThroughMillis - overlapMillis) }
            ?: requestedRange.startAtMillis
        val effectiveRange = TimeRange(effectiveStart, cappedEnd)

        val observed = try {
            source.queryEvents(effectiveRange)
        } catch (error: RuntimeException) {
            return CollectionResult.RetryableFailure(error.message ?: "usage event source unavailable")
        }

        val collectedAt = timeSource.nowMillis()
        val records = observed
            .asSequence()
            .filter { effectiveRange.contains(it.occurredAtMillis) }
            .sortedWith(
                compareBy(
                    { it.occurredAtMillis },
                    { it.occurrenceInTimestampGroup },
                    { it.packageName },
                    { it.kind.name },
                ),
            )
            .mapIndexed { index, event ->
                UsageEvent(
                    eventId = StableIds.usageEvent(owner, event),
                    owner = owner,
                    packageName = event.packageName,
                    kind = event.kind,
                    occurredAtMillis = event.occurredAtMillis,
                    collectedAtMillis = collectedAt,
                    sourceOrder = index,
                )
            }
            .toList()

        val nextCheckpoint = CollectionCheckpoint(owner, cappedEnd)
        return when (val committed = authority.commitCollection(owner, records, nextCheckpoint)) {
            is CommitResult.Failure -> CollectionResult.RetryableFailure(committed.reason)
            is CommitResult.Success -> CollectionResult.Success(
                effectiveRange = effectiveRange,
                observedCount = records.size,
                createdCount = committed.createdCount,
                checkpoint = nextCheckpoint,
            )
        }
    }
}
