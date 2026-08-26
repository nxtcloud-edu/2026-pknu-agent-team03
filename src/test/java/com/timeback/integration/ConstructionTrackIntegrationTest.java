package com.timeback.integration;

import com.timeback.backup.adapter.DeviceDataAuthorityAdapter;
import com.timeback.backup.adapter.InProcessBackupBoundary;
import com.timeback.backup.client.BackupClient;
import com.timeback.backup.client.DataControlClient;
import com.timeback.backup.contracts.DeletionJob;
import com.timeback.backup.server.BackupServer;
import com.timeback.device.contract.AppSession;
import com.timeback.device.contract.CommitResult;
import com.timeback.device.contract.DataOwnerScope;
import com.timeback.device.contract.SessionCompletionCause;
import com.timeback.device.contract.TimeRange;
import com.timeback.device.storage.InMemoryDeviceDataAuthority;
import io.timeback.domain.DomainFacade;
import io.timeback.domain.DomainRecordStore;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

/** CT-01~CT-06 smoke path across the four independently developed tracks. */
public final class ConstructionTrackIntegrationTest {
    private static int passed;

    public static void main(String[] args) {
        DataOwnerScope owner = new DataOwnerScope("anonymous-user-001");
        InMemoryDeviceDataAuthority deviceAuthority = new InMemoryDeviceDataAuthority();

        AppSession session = new AppSession(
                "session-001",
                "logical-session-001",
                owner,
                "example.productive",
                new TimeRange(1_000L, 6_000L),
                SessionCompletionCause.BACKGROUND_EVENT,
                List.of("event-001", "event-002")
        );
        CommitResult sessionCommit = deviceAuthority.replaceSessions(
                owner,
                new TimeRange(1_000L, 6_000L),
                List.of(session),
                null,
                6_000L
        );
        check(sessionCommit instanceof CommitResult.Success, "device session commit");

        DomainFacade domain = new DomainFacade();
        DomainFacade.AnalysisResult analysis = domain.analyzeSessions(
                deviceAuthority.readSessions(owner, new TimeRange(1_000L, 6_000L)),
                Map.of("example.productive", DomainFacade.ClassificationValue.WASTE)
        );
        check(analysis.contexts().size() == 1, "device AppSession maps to one domain Context");
        check(analysis.wasteDurationMillis() == 5_000L, "domain waste duration preserves CT-01 interval");

        DomainRecordStore domainStore = new DomainRecordStore(deviceAuthority, owner);
        CommitResult goalCommit = domainStore.save(new DomainRecordStore.DomainRecord(
                "goal-001",
                DomainRecordStore.DomainEntityType.GOAL,
                null,
                null,
                "name=focus;targetMillis=3600000"
        ), 7_000L);
        check(goalCommit instanceof CommitResult.Success, "domain output persists through APP-11");

        DeviceDataAuthorityAdapter backupData = new DeviceDataAuthorityAdapter(
                deviceAuthority,
                owner,
                Clock.fixed(Instant.ofEpochMilli(8_000L), ZoneOffset.UTC)
        );
        InProcessBackupBoundary backupBoundary = new InProcessBackupBoundary(new BackupServer());
        BackupClient backupClient = new BackupClient(owner.value(), backupBoundary, backupData);
        backupClient.consumeCommittedChanges();
        check(backupClient.getPendingCount() == 2, "APP-12 consumes session and goal committed changes");
        check(backupClient.getPendingChanges().stream()
                        .allMatch(change -> change.getChangeId() != null && !change.getChangeId().isBlank()),
                "device stable changeId reaches backup");
        check(backupClient.submitPendingChanges() == 2, "in-process server accepts integrated backup batch");
        check(backupClient.getPendingCount() == 0, "accepted backup changes leave pending queue");

        DataControlClient dataControl = new DataControlClient(owner.value(), backupBoundary, backupData);
        DeletionJob deletion = dataControl.startFullDeletion("deletion-001");
        check(deletion.isCompleted(), "full deletion completes only after device and server");
        check(domainStore.readPeriod(
                DomainRecordStore.DomainEntityType.GOAL,
                0L,
                10_000L
        ).isEmpty(), "device authority no longer exposes deleted domain data");

        System.out.println("ConstructionTrackIntegrationTest: " + passed + " passed");
    }

    private static void check(boolean condition, String name) {
        if (!condition) {
            throw new AssertionError(name);
        }
        passed++;
        System.out.println("PASS: " + name);
    }
}
