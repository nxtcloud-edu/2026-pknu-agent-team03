package com.timeback.device;

import com.timeback.device.access.AccessGate;
import com.timeback.device.collection.UsageCollector;
import com.timeback.device.contract.AppSession;
import com.timeback.device.contract.ChangeCursor;
import com.timeback.device.contract.ChangeOperation;
import com.timeback.device.contract.CollectionCheckpoint;
import com.timeback.device.contract.CollectionResult;
import com.timeback.device.contract.CommitResult;
import com.timeback.device.contract.DataOwnerScope;
import com.timeback.device.contract.DeviceEntityType;
import com.timeback.device.contract.DeviceRecord;
import com.timeback.device.contract.ObservedUsageEvent;
import com.timeback.device.contract.ReconstructionResult;
import com.timeback.device.contract.ScreenEndEvent;
import com.timeback.device.contract.SessionCompletionCause;
import com.timeback.device.contract.StableIds;
import com.timeback.device.contract.TimeRange;
import com.timeback.device.contract.UsageAccessStatus;
import com.timeback.device.contract.UsageEvent;
import com.timeback.device.contract.UsageEventKind;
import com.timeback.device.fake.ControlledTimeSource;
import com.timeback.device.fake.FakeScreenStateSource;
import com.timeback.device.fake.FakeUsageAccessGateway;
import com.timeback.device.fake.FakeUsageEventSource;
import com.timeback.device.session.SessionReconstructor;
import com.timeback.device.storage.InMemoryDeviceDataAuthority;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public final class DeviceDataTrackTest {
    private DeviceDataTrackTest() {}

    public static void main(String[] args) {
        List<TestCase> tests = List.of(
                new TestCase("permission blocks collection", DeviceDataTrackTest::permissionBlocksCollection),
                new TestCase("access failure is fail-closed", DeviceDataTrackTest::accessFailureIsFailClosed),
                new TestCase("overlapping collection is idempotent", DeviceDataTrackTest::overlappingCollectionIsIdempotent),
                new TestCase("storage failure preserves checkpoint", DeviceDataTrackTest::storageFailurePreservesCheckpoint),
                new TestCase("foreground and background create a session", DeviceDataTrackTest::pairedEventsCreateSession),
                new TestCase("next app closes previous and keeps open candidate", DeviceDataTrackTest::nextAppClosesPrevious),
                new TestCase("screen end closes open session", DeviceDataTrackTest::screenEndClosesOpenSession),
                new TestCase("zero duration pair is discarded", DeviceDataTrackTest::zeroDurationPairIsDiscarded),
                new TestCase("DST midnight split preserves duration", DeviceDataTrackTest::dstMidnightSplitPreservesDuration),
                new TestCase("committed change cursor is ordered", DeviceDataTrackTest::committedChangeCursorIsOrdered),
                new TestCase("owner scope is isolated", DeviceDataTrackTest::ownerScopeIsIsolated),
                new TestCase("generic period records replace completely", DeviceDataTrackTest::genericPeriodRecordsReplaceCompletely),
                new TestCase("failed replacement is atomic", DeviceDataTrackTest::failedReplacementIsAtomic)
        );

        for (TestCase test : tests) {
            test.body().run();
            System.out.println("PASS: " + test.name());
        }
        System.out.println("DeviceDataTrackTest: " + tests.size() + " passed");
    }

    private static void permissionBlocksCollection() {
        CollectionFixture fixture = collectionFixture(UsageAccessStatus.NOT_GRANTED);
        CollectionResult result = fixture.collector().collect(fixture.owner(), new TimeRange(1_000, 5_000));
        check(result == CollectionResult.PermissionRequired.INSTANCE);
        check(fixture.eventSource().getQueryCount() == 0);
        check(fixture.authority().readCheckpoint(fixture.owner()) == null);
    }

    private static void accessFailureIsFailClosed() {
        CollectionFixture fixture = collectionFixture(UsageAccessStatus.GRANTED);
        fixture.accessGateway().setFailReadsWith(new IllegalStateException("app ops unavailable"));
        CollectionResult result = fixture.collector().collect(fixture.owner(), new TimeRange(1_000, 5_000));
        check(result instanceof CollectionResult.Failure);
        check(fixture.eventSource().getQueryCount() == 0);
    }

    private static void overlappingCollectionIsIdempotent() {
        CollectionFixture fixture = collectionFixture(UsageAccessStatus.GRANTED);
        fixture.eventSource().setEvents(List.of(
                new ObservedUsageEvent("example.app", UsageEventKind.FOREGROUND, 4_500),
                new ObservedUsageEvent("example.app", UsageEventKind.BACKGROUND, 4_900)
        ));

        CollectionResult first = fixture.collector().collect(fixture.owner(), new TimeRange(1_000, 5_000));
        check(first instanceof CollectionResult.Success success && success.createdCount() == 2);
        fixture.time().advanceTo(110_000);
        CollectionResult second = fixture.collector().collect(fixture.owner(), new TimeRange(1_000, 6_000));
        check(second instanceof CollectionResult.Success success && success.createdCount() == 0);
        check(fixture.authority().readUsageEvents(fixture.owner(), new TimeRange(1_000, 6_000)).size() == 2);
        List<TimeRange> ranges = fixture.eventSource().getRequestedRanges();
        check(ranges.get(ranges.size() - 1).startAtMillis() == 4_000L);
    }

    private static void storageFailurePreservesCheckpoint() {
        CollectionFixture fixture = collectionFixture(UsageAccessStatus.GRANTED);
        fixture.eventSource().setEvents(List.of(
                new ObservedUsageEvent("example.app", UsageEventKind.FOREGROUND, 2_000)
        ));
        fixture.authority().failNextCommit();

        CollectionResult result = fixture.collector().collect(fixture.owner(), new TimeRange(1_000, 5_000));
        check(result instanceof CollectionResult.RetryableFailure);
        check(fixture.authority().readCheckpoint(fixture.owner()) == null);
        check(fixture.authority().readUsageEvents(fixture.owner(), new TimeRange(1_000, 5_000)).isEmpty());
    }

    private static void pairedEventsCreateSession() {
        DataOwnerScope owner = new DataOwnerScope("owner-paired");
        InMemoryDeviceDataAuthority authority = new InMemoryDeviceDataAuthority();
        seedEvents(
                authority,
                owner,
                event(owner, "fg", "example.app", UsageEventKind.FOREGROUND, 1_000, 0),
                event(owner, "bg", "example.app", UsageEventKind.BACKGROUND, 3_000, 1)
        );
        ReconstructionResult result = reconstructor(authority, 5_000).reconstruct(owner, new TimeRange(500, 4_000));
        check(result instanceof ReconstructionResult.Success);
        ReconstructionResult.Success success = (ReconstructionResult.Success) result;
        check(success.sessions().get(0).range().equals(new TimeRange(1_000, 3_000)));
        check(success.sessions().get(0).completionCause() == SessionCompletionCause.BACKGROUND_EVENT);
        check(success.openCandidate() == null);
    }

    private static void nextAppClosesPrevious() {
        DataOwnerScope owner = new DataOwnerScope("owner-switch");
        InMemoryDeviceDataAuthority authority = new InMemoryDeviceDataAuthority();
        seedEvents(
                authority,
                owner,
                event(owner, "fg-a", "app.a", UsageEventKind.FOREGROUND, 1_000, 0),
                event(owner, "fg-b", "app.b", UsageEventKind.FOREGROUND, 2_000, 1)
        );
        ReconstructionResult result = reconstructor(authority, 5_000).reconstruct(owner, new TimeRange(500, 4_000));
        check(result instanceof ReconstructionResult.Success);
        ReconstructionResult.Success success = (ReconstructionResult.Success) result;
        check(success.sessions().get(0).packageName().equals("app.a"));
        check(success.sessions().get(0).range().equals(new TimeRange(1_000, 2_000)));
        check(success.sessions().get(0).completionCause() == SessionCompletionCause.NEXT_APP_FOREGROUND);
        check(success.openCandidate() != null && success.openCandidate().packageName().equals("app.b"));
    }

    private static void screenEndClosesOpenSession() {
        DataOwnerScope owner = new DataOwnerScope("owner-screen");
        InMemoryDeviceDataAuthority authority = new InMemoryDeviceDataAuthority();
        seedEvents(
                authority,
                owner,
                event(owner, "fg", "example.app", UsageEventKind.FOREGROUND, 1_000, 0)
        );
        ReconstructionResult result = reconstructor(
                authority,
                5_000,
                List.of(new ScreenEndEvent(2_500))
        ).reconstruct(owner, new TimeRange(500, 4_000));
        check(result instanceof ReconstructionResult.Success);
        ReconstructionResult.Success success = (ReconstructionResult.Success) result;
        check(success.sessions().get(0).range().equals(new TimeRange(1_000, 2_500)));
        check(success.sessions().get(0).completionCause() == SessionCompletionCause.SCREEN_ENDED);
    }

    private static void zeroDurationPairIsDiscarded() {
        DataOwnerScope owner = new DataOwnerScope("owner-zero");
        InMemoryDeviceDataAuthority authority = new InMemoryDeviceDataAuthority();
        seedEvents(
                authority,
                owner,
                event(owner, "fg", "example.app", UsageEventKind.FOREGROUND, 1_000, 0),
                event(owner, "bg", "example.app", UsageEventKind.BACKGROUND, 1_000, 1)
        );
        ReconstructionResult result = reconstructor(authority, 5_000).reconstruct(owner, new TimeRange(500, 4_000));
        check(result instanceof ReconstructionResult.Success);
        ReconstructionResult.Success success = (ReconstructionResult.Success) result;
        check(success.sessions().isEmpty());
        check(success.openCandidate() == null);
    }

    private static void dstMidnightSplitPreservesDuration() {
        ZoneId zone = ZoneId.of("America/New_York");
        long start = ZonedDateTime.of(2024, 3, 9, 23, 30, 0, 0, zone).toInstant().toEpochMilli();
        long end = ZonedDateTime.of(2024, 3, 10, 3, 30, 0, 0, zone).toInstant().toEpochMilli();
        DataOwnerScope owner = new DataOwnerScope("owner-dst");
        InMemoryDeviceDataAuthority authority = new InMemoryDeviceDataAuthority();
        seedEvents(
                authority,
                owner,
                event(owner, "fg", "example.app", UsageEventKind.FOREGROUND, start, 0),
                event(owner, "bg", "example.app", UsageEventKind.BACKGROUND, end, 1)
        );
        ControlledTimeSource time = new ControlledTimeSource(end + 10_000, zone);
        ReconstructionResult result = new SessionReconstructor(authority, new FakeScreenStateSource(), time)
                .reconstruct(owner, new TimeRange(start - 1_000, end + 1_000));
        check(result instanceof ReconstructionResult.Success);
        ReconstructionResult.Success success = (ReconstructionResult.Success) result;
        check(success.sessions().size() == 2);
        long total = success.sessions().stream().mapToLong(AppSession::durationMillis).sum();
        check(total == end - start);
        check(end - start == 3L * 60L * 60L * 1_000L);
    }

    private static void committedChangeCursorIsOrdered() {
        DataOwnerScope owner = new DataOwnerScope("owner-cursor");
        InMemoryDeviceDataAuthority authority = new InMemoryDeviceDataAuthority();
        seedEvents(
                authority,
                owner,
                event(owner, "e1", "app", UsageEventKind.FOREGROUND, 1_000, 0),
                event(owner, "e2", "app", UsageEventKind.BACKGROUND, 2_000, 1),
                event(owner, "e3", "app", UsageEventKind.FOREGROUND, 3_000, 2)
        );
        var first = authority.readCommittedChanges(owner, new ChangeCursor(), 2);
        check(first.changes().stream().map(change -> change.sequence()).toList().equals(List.of(1L, 2L)));
        var second = authority.readCommittedChanges(owner, first.nextCursor(), 2);
        check(second.changes().size() == 1 && second.changes().get(0).sequence() == 3L);
        check(second.nextCursor().equals(new ChangeCursor(3)));
    }

    private static void ownerScopeIsIsolated() {
        DataOwnerScope ownerA = new DataOwnerScope("owner-a");
        DataOwnerScope ownerB = new DataOwnerScope("owner-b");
        InMemoryDeviceDataAuthority authority = new InMemoryDeviceDataAuthority();
        seedEvents(
                authority,
                ownerA,
                event(ownerA, "a-event", "app.a", UsageEventKind.FOREGROUND, 1_000, 0)
        );
        check(authority.readUsageEvents(ownerB, new TimeRange(500, 2_000)).isEmpty());
        check(authority.readCommittedChanges(ownerB, new ChangeCursor()).changes().isEmpty());
    }

    private static void genericPeriodRecordsReplaceCompletely() {
        DataOwnerScope owner = new DataOwnerScope("owner-domain-records");
        InMemoryDeviceDataAuthority authority = new InMemoryDeviceDataAuthority();
        DeviceRecord first = new DeviceRecord(
                "context-1",
                owner,
                DeviceEntityType.CONTEXT,
                new TimeRange(1_000, 2_000),
                "first"
        );
        DeviceRecord stale = new DeviceRecord(
                "context-stale",
                owner,
                DeviceEntityType.CONTEXT,
                new TimeRange(2_000, 3_000),
                "stale"
        );
        check(authority.saveRecords(owner, List.of(first, stale), 3_500) instanceof CommitResult.Success);

        DeviceRecord updated = first.withPayload("updated");
        CommitResult result = authority.replacePeriodRecords(
                owner,
                DeviceEntityType.CONTEXT,
                new TimeRange(500, 3_500),
                List.of(updated),
                4_000
        );
        check(result instanceof CommitResult.Success);
        Set<ChangeOperation> operations = ((CommitResult.Success) result).changes().stream()
                .map(change -> change.operation())
                .collect(java.util.stream.Collectors.toSet());
        check(operations.size() == 2);
        check(authority.readPeriodRecords(
                owner,
                DeviceEntityType.CONTEXT,
                new TimeRange(500, 3_500)
        ).equals(List.of(updated)));
    }

    private static void failedReplacementIsAtomic() {
        DataOwnerScope owner = new DataOwnerScope("owner-atomic");
        InMemoryDeviceDataAuthority authority = new InMemoryDeviceDataAuthority();
        TimeRange range = new TimeRange(1_000, 2_000);
        String logicalId = StableIds.logicalSession(owner, "app", range.startAtMillis(), range.endAtMillis());
        AppSession session = new AppSession(
                StableIds.sessionPart(logicalId, range),
                logicalId,
                owner,
                "app",
                range,
                SessionCompletionCause.BACKGROUND_EVENT,
                List.of("fg", "bg")
        );
        check(authority.replaceSessions(
                owner,
                new TimeRange(500, 2_500),
                List.of(session),
                null,
                3_000
        ) instanceof CommitResult.Success);
        authority.failNextCommit();
        check(authority.replaceSessions(
                owner,
                new TimeRange(500, 2_500),
                List.of(),
                null,
                4_000
        ) instanceof CommitResult.Failure);
        check(authority.readSessions(owner, new TimeRange(500, 2_500)).equals(List.of(session)));
    }

    private static CollectionFixture collectionFixture(UsageAccessStatus status) {
        DataOwnerScope owner = new DataOwnerScope("owner-collection");
        FakeUsageAccessGateway access = new FakeUsageAccessGateway(status);
        FakeUsageEventSource source = new FakeUsageEventSource();
        InMemoryDeviceDataAuthority authority = new InMemoryDeviceDataAuthority();
        ControlledTimeSource time = new ControlledTimeSource(100_000);
        UsageCollector collector = new UsageCollector(new AccessGate(access, time), source, authority, time);
        return new CollectionFixture(owner, access, source, authority, time, collector);
    }

    private static SessionReconstructor reconstructor(
            InMemoryDeviceDataAuthority authority,
            long now
    ) {
        return reconstructor(authority, now, List.of());
    }

    private static SessionReconstructor reconstructor(
            InMemoryDeviceDataAuthority authority,
            long now,
            List<ScreenEndEvent> screenEvents
    ) {
        return new SessionReconstructor(
                authority,
                new FakeScreenStateSource(screenEvents),
                new ControlledTimeSource(now)
        );
    }

    private static void seedEvents(
            InMemoryDeviceDataAuthority authority,
            DataOwnerScope owner,
            UsageEvent... events
    ) {
        long end = Arrays.stream(events).mapToLong(UsageEvent::occurredAtMillis).max().orElseThrow() + 1;
        CommitResult result = authority.commitCollection(
                owner,
                List.of(events),
                new CollectionCheckpoint(owner, end)
        );
        check(result instanceof CommitResult.Success);
    }

    private static UsageEvent event(
            DataOwnerScope owner,
            String id,
            String packageName,
            UsageEventKind kind,
            long occurredAt,
            int order
    ) {
        return new UsageEvent(
                id,
                owner,
                packageName,
                kind,
                occurredAt,
                occurredAt,
                order
        );
    }

    private static void check(boolean condition) {
        if (!condition) {
            throw new AssertionError("check failed");
        }
    }

    private record TestCase(String name, Runnable body) {}

    private record CollectionFixture(
            DataOwnerScope owner,
            FakeUsageAccessGateway accessGateway,
            FakeUsageEventSource eventSource,
            InMemoryDeviceDataAuthority authority,
            ControlledTimeSource time,
            UsageCollector collector
    ) {}
}
