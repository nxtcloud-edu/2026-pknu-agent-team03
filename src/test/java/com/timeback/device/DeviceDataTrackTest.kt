package com.timeback.device

import com.timeback.device.access.AccessGate
import com.timeback.device.collection.UsageCollector
import com.timeback.device.contract.AppSession
import com.timeback.device.contract.ChangeCursor
import com.timeback.device.contract.CollectionCheckpoint
import com.timeback.device.contract.CollectionResult
import com.timeback.device.contract.CommitResult
import com.timeback.device.contract.DataOwnerScope
import com.timeback.device.contract.DeviceEntityType
import com.timeback.device.contract.DeviceRecord
import com.timeback.device.contract.ObservedUsageEvent
import com.timeback.device.contract.ReconstructionResult
import com.timeback.device.contract.ScreenEndEvent
import com.timeback.device.contract.SessionCompletionCause
import com.timeback.device.contract.StableIds
import com.timeback.device.contract.TimeRange
import com.timeback.device.contract.UsageAccessStatus
import com.timeback.device.contract.UsageEvent
import com.timeback.device.contract.UsageEventKind
import com.timeback.device.fake.ControlledTimeSource
import com.timeback.device.fake.FakeScreenStateSource
import com.timeback.device.fake.FakeUsageAccessGateway
import com.timeback.device.fake.FakeUsageEventSource
import com.timeback.device.session.SessionReconstructor
import com.timeback.device.storage.InMemoryDeviceDataAuthority
import java.time.ZoneId
import java.time.ZonedDateTime

fun main() {
    val tests = listOf(
        "permission blocks collection" to ::permissionBlocksCollection,
        "access failure is fail-closed" to ::accessFailureIsFailClosed,
        "overlapping collection is idempotent" to ::overlappingCollectionIsIdempotent,
        "storage failure preserves checkpoint" to ::storageFailurePreservesCheckpoint,
        "foreground and background create a session" to ::pairedEventsCreateSession,
        "next app closes previous and keeps open candidate" to ::nextAppClosesPrevious,
        "screen end closes open session" to ::screenEndClosesOpenSession,
        "zero duration pair is discarded" to ::zeroDurationPairIsDiscarded,
        "DST midnight split preserves duration" to ::dstMidnightSplitPreservesDuration,
        "committed change cursor is ordered" to ::committedChangeCursorIsOrdered,
        "owner scope is isolated" to ::ownerScopeIsIsolated,
        "generic period records replace completely" to ::genericPeriodRecordsReplaceCompletely,
        "failed replacement is atomic" to ::failedReplacementIsAtomic,
    )

    tests.forEach { (name, test) ->
        test()
        println("PASS: $name")
    }
    println("DeviceDataTrackTest: ${tests.size} passed")
}

private fun permissionBlocksCollection() {
    val fixture = collectionFixture(UsageAccessStatus.NOT_GRANTED)
    val result = fixture.collector.collect(fixture.owner, TimeRange(1_000, 5_000))
    check(result == CollectionResult.PermissionRequired)
    check(fixture.eventSource.queryCount == 0)
    check(fixture.authority.readCheckpoint(fixture.owner) == null)
}

private fun accessFailureIsFailClosed() {
    val fixture = collectionFixture(UsageAccessStatus.GRANTED)
    fixture.accessGateway.failReadsWith = IllegalStateException("app ops unavailable")
    val result = fixture.collector.collect(fixture.owner, TimeRange(1_000, 5_000))
    check(result is CollectionResult.Failure)
    check(fixture.eventSource.queryCount == 0)
}

private fun overlappingCollectionIsIdempotent() {
    val fixture = collectionFixture(UsageAccessStatus.GRANTED)
    fixture.eventSource.events = listOf(
        ObservedUsageEvent("example.app", UsageEventKind.FOREGROUND, 4_500),
        ObservedUsageEvent("example.app", UsageEventKind.BACKGROUND, 4_900),
    )

    val first = fixture.collector.collect(fixture.owner, TimeRange(1_000, 5_000))
    check(first is CollectionResult.Success && first.createdCount == 2)
    fixture.time.advanceTo(110_000)
    val second = fixture.collector.collect(fixture.owner, TimeRange(1_000, 6_000))
    check(second is CollectionResult.Success && second.createdCount == 0)
    check(fixture.authority.readUsageEvents(fixture.owner, TimeRange(1_000, 6_000)).size == 2)
    check(fixture.eventSource.requestedRanges.last().startAtMillis == 4_000L)
}

private fun storageFailurePreservesCheckpoint() {
    val fixture = collectionFixture(UsageAccessStatus.GRANTED)
    fixture.eventSource.events = listOf(
        ObservedUsageEvent("example.app", UsageEventKind.FOREGROUND, 2_000),
    )
    fixture.authority.failNextCommit()

    val result = fixture.collector.collect(fixture.owner, TimeRange(1_000, 5_000))
    check(result is CollectionResult.RetryableFailure)
    check(fixture.authority.readCheckpoint(fixture.owner) == null)
    check(fixture.authority.readUsageEvents(fixture.owner, TimeRange(1_000, 5_000)).isEmpty())
}

private fun pairedEventsCreateSession() {
    val owner = DataOwnerScope("owner-paired")
    val authority = InMemoryDeviceDataAuthority()
    seedEvents(
        authority,
        owner,
        event(owner, "fg", "example.app", UsageEventKind.FOREGROUND, 1_000, 0),
        event(owner, "bg", "example.app", UsageEventKind.BACKGROUND, 3_000, 1),
    )
    val result = reconstructor(authority, now = 5_000).reconstruct(owner, TimeRange(500, 4_000))
    check(result is ReconstructionResult.Success)
    check(result.sessions.single().range == TimeRange(1_000, 3_000))
    check(result.sessions.single().completionCause == SessionCompletionCause.BACKGROUND_EVENT)
    check(result.openCandidate == null)
}

private fun nextAppClosesPrevious() {
    val owner = DataOwnerScope("owner-switch")
    val authority = InMemoryDeviceDataAuthority()
    seedEvents(
        authority,
        owner,
        event(owner, "fg-a", "app.a", UsageEventKind.FOREGROUND, 1_000, 0),
        event(owner, "fg-b", "app.b", UsageEventKind.FOREGROUND, 2_000, 1),
    )
    val result = reconstructor(authority, now = 5_000).reconstruct(owner, TimeRange(500, 4_000))
    check(result is ReconstructionResult.Success)
    check(result.sessions.single().packageName == "app.a")
    check(result.sessions.single().range == TimeRange(1_000, 2_000))
    check(result.sessions.single().completionCause == SessionCompletionCause.NEXT_APP_FOREGROUND)
    check(result.openCandidate?.packageName == "app.b")
}

private fun screenEndClosesOpenSession() {
    val owner = DataOwnerScope("owner-screen")
    val authority = InMemoryDeviceDataAuthority()
    seedEvents(
        authority,
        owner,
        event(owner, "fg", "example.app", UsageEventKind.FOREGROUND, 1_000, 0),
    )
    val result = reconstructor(
        authority = authority,
        now = 5_000,
        screenEvents = listOf(ScreenEndEvent(2_500)),
    ).reconstruct(owner, TimeRange(500, 4_000))
    check(result is ReconstructionResult.Success)
    check(result.sessions.single().range == TimeRange(1_000, 2_500))
    check(result.sessions.single().completionCause == SessionCompletionCause.SCREEN_ENDED)
}

private fun zeroDurationPairIsDiscarded() {
    val owner = DataOwnerScope("owner-zero")
    val authority = InMemoryDeviceDataAuthority()
    seedEvents(
        authority,
        owner,
        event(owner, "fg", "example.app", UsageEventKind.FOREGROUND, 1_000, 0),
        event(owner, "bg", "example.app", UsageEventKind.BACKGROUND, 1_000, 1),
    )
    val result = reconstructor(authority, now = 5_000).reconstruct(owner, TimeRange(500, 4_000))
    check(result is ReconstructionResult.Success)
    check(result.sessions.isEmpty())
    check(result.openCandidate == null)
}

private fun dstMidnightSplitPreservesDuration() {
    val zone = ZoneId.of("America/New_York")
    val start = ZonedDateTime.of(2024, 3, 9, 23, 30, 0, 0, zone).toInstant().toEpochMilli()
    val end = ZonedDateTime.of(2024, 3, 10, 3, 30, 0, 0, zone).toInstant().toEpochMilli()
    val owner = DataOwnerScope("owner-dst")
    val authority = InMemoryDeviceDataAuthority()
    seedEvents(
        authority,
        owner,
        event(owner, "fg", "example.app", UsageEventKind.FOREGROUND, start, 0),
        event(owner, "bg", "example.app", UsageEventKind.BACKGROUND, end, 1),
    )
    val time = ControlledTimeSource(end + 10_000, zone)
    val result = SessionReconstructor(authority, FakeScreenStateSource(), time)
        .reconstruct(owner, TimeRange(start - 1_000, end + 1_000))
    check(result is ReconstructionResult.Success)
    check(result.sessions.size == 2)
    check(result.sessions.sumOf(AppSession::durationMillis) == end - start)
    check(end - start == 3L * 60L * 60L * 1_000L)
}

private fun committedChangeCursorIsOrdered() {
    val owner = DataOwnerScope("owner-cursor")
    val authority = InMemoryDeviceDataAuthority()
    seedEvents(
        authority,
        owner,
        event(owner, "e1", "app", UsageEventKind.FOREGROUND, 1_000, 0),
        event(owner, "e2", "app", UsageEventKind.BACKGROUND, 2_000, 1),
        event(owner, "e3", "app", UsageEventKind.FOREGROUND, 3_000, 2),
    )
    val first = authority.readCommittedChanges(owner, ChangeCursor(), limit = 2)
    check(first.changes.map { it.sequence } == listOf(1L, 2L))
    val second = authority.readCommittedChanges(owner, first.nextCursor, limit = 2)
    check(second.changes.single().sequence == 3L)
    check(second.nextCursor == ChangeCursor(3))
}

private fun ownerScopeIsIsolated() {
    val ownerA = DataOwnerScope("owner-a")
    val ownerB = DataOwnerScope("owner-b")
    val authority = InMemoryDeviceDataAuthority()
    seedEvents(
        authority,
        ownerA,
        event(ownerA, "a-event", "app.a", UsageEventKind.FOREGROUND, 1_000, 0),
    )
    check(authority.readUsageEvents(ownerB, TimeRange(500, 2_000)).isEmpty())
    check(authority.readCommittedChanges(ownerB, ChangeCursor()).changes.isEmpty())
}

private fun genericPeriodRecordsReplaceCompletely() {
    val owner = DataOwnerScope("owner-domain-records")
    val authority = InMemoryDeviceDataAuthority()
    val first = DeviceRecord(
        recordId = "context-1",
        owner = owner,
        entityType = DeviceEntityType.CONTEXT,
        range = TimeRange(1_000, 2_000),
        payload = "first",
    )
    val stale = DeviceRecord(
        recordId = "context-stale",
        owner = owner,
        entityType = DeviceEntityType.CONTEXT,
        range = TimeRange(2_000, 3_000),
        payload = "stale",
    )
    check(authority.saveRecords(owner, listOf(first, stale), 3_500) is CommitResult.Success)

    val updated = first.copy(payload = "updated")
    val result = authority.replacePeriodRecords(
        owner,
        DeviceEntityType.CONTEXT,
        TimeRange(500, 3_500),
        listOf(updated),
        4_000,
    )
    check(result is CommitResult.Success)
    check(result.changes.map { it.operation }.toSet().size == 2)
    check(authority.readPeriodRecords(owner, DeviceEntityType.CONTEXT, TimeRange(500, 3_500)) == listOf(updated))
}

private fun failedReplacementIsAtomic() {
    val owner = DataOwnerScope("owner-atomic")
    val authority = InMemoryDeviceDataAuthority()
    val range = TimeRange(1_000, 2_000)
    val logicalId = StableIds.logicalSession(owner, "app", range.startAtMillis, range.endAtMillis)
    val session = AppSession(
        sessionId = StableIds.sessionPart(logicalId, range),
        logicalSessionId = logicalId,
        owner = owner,
        packageName = "app",
        range = range,
        completionCause = SessionCompletionCause.BACKGROUND_EVENT,
        sourceEventIds = listOf("fg", "bg"),
    )
    check(
        authority.replaceSessions(owner, TimeRange(500, 2_500), listOf(session), null, 3_000) is
            CommitResult.Success,
    )
    authority.failNextCommit()
    check(
        authority.replaceSessions(owner, TimeRange(500, 2_500), emptyList(), null, 4_000) is
            CommitResult.Failure,
    )
    check(authority.readSessions(owner, TimeRange(500, 2_500)) == listOf(session))
}

private data class CollectionFixture(
    val owner: DataOwnerScope,
    val accessGateway: FakeUsageAccessGateway,
    val eventSource: FakeUsageEventSource,
    val authority: InMemoryDeviceDataAuthority,
    val time: ControlledTimeSource,
    val collector: UsageCollector,
)

private fun collectionFixture(status: UsageAccessStatus): CollectionFixture {
    val owner = DataOwnerScope("owner-collection")
    val access = FakeUsageAccessGateway(status)
    val source = FakeUsageEventSource()
    val authority = InMemoryDeviceDataAuthority()
    val time = ControlledTimeSource(100_000)
    val collector = UsageCollector(AccessGate(access, time), source, authority, time)
    return CollectionFixture(owner, access, source, authority, time, collector)
}

private fun reconstructor(
    authority: InMemoryDeviceDataAuthority,
    now: Long,
    screenEvents: List<ScreenEndEvent> = emptyList(),
): SessionReconstructor = SessionReconstructor(
    authority = authority,
    screenStateSource = FakeScreenStateSource(screenEvents),
    timeSource = ControlledTimeSource(now),
)

private fun seedEvents(
    authority: InMemoryDeviceDataAuthority,
    owner: DataOwnerScope,
    vararg events: UsageEvent,
) {
    val end = events.maxOf { it.occurredAtMillis } + 1
    val result = authority.commitCollection(owner, events.toList(), CollectionCheckpoint(owner, end))
    check(result is CommitResult.Success)
}

private fun event(
    owner: DataOwnerScope,
    id: String,
    packageName: String,
    kind: UsageEventKind,
    occurredAt: Long,
    order: Int,
): UsageEvent = UsageEvent(
    eventId = id,
    owner = owner,
    packageName = packageName,
    kind = kind,
    occurredAtMillis = occurredAt,
    collectedAtMillis = occurredAt,
    sourceOrder = order,
)
