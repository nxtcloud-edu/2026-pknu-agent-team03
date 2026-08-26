package com.timeback.ui.integration;

import android.content.Context;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import com.timeback.device.contract.AppSession;
import com.timeback.device.contract.ChangeCursor;
import com.timeback.device.contract.ChangeOperation;
import com.timeback.device.contract.CollectionCheckpoint;
import com.timeback.device.contract.CommitResult;
import com.timeback.device.contract.DataOwnerScope;
import com.timeback.device.contract.DeviceEntityType;
import com.timeback.device.contract.DeviceRecord;
import com.timeback.device.contract.OpenSessionCandidate;
import com.timeback.device.contract.SessionCompletionCause;
import com.timeback.device.contract.TimeRange;
import com.timeback.device.contract.UsageEvent;
import com.timeback.device.contract.UsageEventKind;
import com.timeback.device.room.RoomDeviceDataAuthority;
import com.timeback.device.room.TimeBackRoomDatabase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.EnumSet;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class RoomDeviceDataAuthorityTest {
    private static final String DATABASE_NAME = "room-device-contract-test";

    private Context context;
    private TimeBackRoomDatabase database;
    private RoomDeviceDataAuthority authority;

    @Before
    public void openDatabase() {
        context = ApplicationProvider.getApplicationContext();
        context.deleteDatabase(DATABASE_NAME);
        reopenDatabase();
    }

    @After
    public void closeDatabase() {
        if (database != null) {
            database.close();
        }
        context.deleteDatabase(DATABASE_NAME);
    }

    @Test
    public void genericRecordAndChangeCursorSurviveDatabaseReopen() {
        DataOwnerScope owner = new DataOwnerScope("room-owner");
        DeviceRecord record = new DeviceRecord(
                "context-1",
                owner,
                DeviceEntityType.CONTEXT,
                new TimeRange(1_000, 2_000),
                "FOCUS"
        );
        CommitResult result = authority.saveRecords(owner, List.of(record), 2_500);
        assertTrue(result instanceof CommitResult.Success);
        String changeId = ((CommitResult.Success) result).changes().get(0).changeId();

        database.close();
        reopenDatabase();

        assertEquals(record, authority.readRecord(owner, DeviceEntityType.CONTEXT, "context-1"));
        var page = authority.readCommittedChanges(owner, new ChangeCursor(), 10);
        assertEquals(1, page.changes().size());
        assertEquals(changeId, page.changes().get(0).changeId());
        assertEquals(new ChangeCursor(1), page.nextCursor());
    }

    @Test
    public void collectionIsIdempotentAndOwnerScoped() {
        DataOwnerScope owner = new DataOwnerScope("room-owner-a");
        DataOwnerScope other = new DataOwnerScope("room-owner-b");
        UsageEvent event = new UsageEvent(
                "event-1",
                owner,
                "example.app",
                UsageEventKind.FOREGROUND,
                1_000,
                2_000,
                0
        );
        CollectionCheckpoint checkpoint = new CollectionCheckpoint(owner, 3_000);

        CommitResult first = authority.commitCollection(owner, List.of(event), checkpoint);
        CommitResult retry = authority.commitCollection(owner, List.of(event), checkpoint);

        assertEquals(1, ((CommitResult.Success) first).createdCount());
        assertEquals(0, ((CommitResult.Success) retry).createdCount());
        assertEquals(List.of(event), authority.readUsageEvents(owner, new TimeRange(500, 1_500)));
        assertTrue(authority.readUsageEvents(other, new TimeRange(500, 1_500)).isEmpty());
        assertEquals(checkpoint, authority.readCheckpoint(owner));
    }

    @Test
    public void sessionsAndOpenCandidateReplaceAtomically() {
        DataOwnerScope owner = new DataOwnerScope("room-session-owner");
        AppSession session = new AppSession(
                "session-1",
                "logical-1",
                owner,
                "example.app",
                new TimeRange(1_000, 2_000),
                SessionCompletionCause.BACKGROUND_EVENT,
                List.of("fg-1", "bg-1")
        );
        OpenSessionCandidate candidate = new OpenSessionCandidate(
                owner, "next.app", 2_100, "fg-2");

        assertTrue(authority.replaceSessions(
                owner,
                new TimeRange(500, 3_000),
                List.of(session),
                candidate,
                3_500
        ) instanceof CommitResult.Success);
        assertEquals(List.of(session), authority.readSessions(owner, new TimeRange(500, 3_000)));
        assertEquals(candidate, authority.readOpenSessionCandidate(owner));

        assertTrue(authority.replaceSessions(
                owner,
                new TimeRange(500, 3_000),
                List.of(),
                null,
                4_000
        ) instanceof CommitResult.Success);
        assertTrue(authority.readSessions(owner, new TimeRange(500, 3_000)).isEmpty());
        assertNull(authority.readOpenSessionCandidate(owner));
    }

    @Test
    public void fullScopeDeletionRemovesDataAndEmitsDeleteChange() {
        DataOwnerScope owner = new DataOwnerScope("room-delete-owner");
        DeviceRecord goal = new DeviceRecord(
                "goal-1",
                owner,
                DeviceEntityType.GOAL,
                new TimeRange(1_000, 2_000),
                "goal"
        );
        assertTrue(authority.saveRecords(owner, List.of(goal), 2_500)
                instanceof CommitResult.Success);

        CommitResult deletion = authority.deleteScope(
                owner,
                EnumSet.allOf(DeviceEntityType.class),
                3_000
        );

        assertTrue(deletion instanceof CommitResult.Success);
        assertNull(authority.readRecord(owner, DeviceEntityType.GOAL, "goal-1"));
        assertEquals(
                List.of(ChangeOperation.CREATE, ChangeOperation.DELETE),
                authority.readCommittedChanges(owner, new ChangeCursor(), 10)
                        .changes().stream().map(change -> change.operation()).toList()
        );
    }

    @Test
    public void tenThousandEventsCommitWithinStorageBudget() {
        DataOwnerScope owner = new DataOwnerScope("room-performance-owner");
        List<UsageEvent> events = new ArrayList<>(10_000);
        for (int index = 0; index < 10_000; index++) {
            events.add(new UsageEvent(
                    "event-" + index,
                    owner,
                    "example.app",
                    index % 2 == 0 ? UsageEventKind.FOREGROUND : UsageEventKind.BACKGROUND,
                    1_000L + index,
                    20_000L,
                    index
            ));
        }

        long startedAt = System.nanoTime();
        CommitResult result = authority.commitCollection(
                owner,
                events,
                new CollectionCheckpoint(owner, 20_000L)
        );
        long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt);

        assertTrue(result instanceof CommitResult.Success);
        assertEquals(10_000, ((CommitResult.Success) result).createdCount());
        assertTrue("10,000 event storage took " + elapsedMillis + "ms", elapsedMillis <= 4_000L);
    }

    private void reopenDatabase() {
        database = Room.databaseBuilder(context, TimeBackRoomDatabase.class, DATABASE_NAME)
                .allowMainThreadQueries()
                .setJournalMode(androidx.room.RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)
                .build();
        authority = new RoomDeviceDataAuthority(database);
    }
}
