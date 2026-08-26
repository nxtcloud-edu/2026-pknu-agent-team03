package com.timeback.backup;

import com.timeback.backup.client.BackupClient;
import com.timeback.backup.contracts.*;
import com.timeback.backup.fakes.FakeBackupBoundary;
import com.timeback.backup.fakes.FakeDeviceDataAuthority;

/**
 * APP-12 백업 클라이언트 단위 테스트.
 */
public class BackupClientTest {

    public static void main(String[] args) {
        testConsumeAndSubmitAllAccepted();
        testPartialFailureAndRetry();
        testOfflineKeepsPending();
        testEmptyNoop();

        System.out.println("=== BackupClientTest: ALL PASSED ===");
    }

    static void testConsumeAndSubmitAllAccepted() {
        FakeBackupBoundary boundary = new FakeBackupBoundary();
        FakeDeviceDataAuthority data = new FakeDeviceDataAuthority();
        BackupClient client = new BackupClient("user-001", boundary, data);

        // 변경 통지 2개
        data.notifyChange(new CommittedChange(EntityType.USAGE_EVENT, "e1",
                EntityOperation.CREATE, 1000L));
        data.notifyChange(new CommittedChange(EntityType.CONTEXT, "ctx1",
                EntityOperation.UPDATE, 2000L));

        client.consumeCommittedChanges();
        assert client.getPendingCount() == 2 : "Should have 2 pending";

        int accepted = client.submitPendingChanges();
        assert accepted == 2 : "All should be accepted";
        assert client.getPendingCount() == 0 : "No pending after success";

        System.out.println("  ✓ testConsumeAndSubmitAllAccepted");
    }

    static void testPartialFailureAndRetry() {
        FakeBackupBoundary boundary = new FakeBackupBoundary();
        boundary.setBackupMode(FakeBackupBoundary.BackupMode.PARTIAL_FAILURE);
        FakeDeviceDataAuthority data = new FakeDeviceDataAuthority();
        BackupClient client = new BackupClient("user-002", boundary, data);

        data.notifyChange(new CommittedChange(EntityType.GOAL, "g1",
                EntityOperation.CREATE, 3000L));
        data.notifyChange(new CommittedChange(EntityType.ACTIVITY, "a1",
                EntityOperation.CREATE, 4000L));

        client.consumeCommittedChanges();
        int firstAttempt = client.submitPendingChanges();
        assert firstAttempt == 1 : "Only even index accepted";
        assert client.getPendingCount() == 1 : "1 still pending (RETRYABLE)";

        // 재시도 — 모드를 ACCEPT_ALL로 변경
        boundary.setBackupMode(FakeBackupBoundary.BackupMode.ACCEPT_ALL);
        int retryResult = client.retry();
        assert retryResult == 1 : "Retry should accept remaining";
        assert client.getPendingCount() == 0 : "All done after retry";

        System.out.println("  ✓ testPartialFailureAndRetry");
    }

    static void testOfflineKeepsPending() {
        FakeBackupBoundary boundary = new FakeBackupBoundary();
        boundary.setBackupMode(FakeBackupBoundary.BackupMode.OFFLINE);
        FakeDeviceDataAuthority data = new FakeDeviceDataAuthority();
        BackupClient client = new BackupClient("user-003", boundary, data);

        data.notifyChange(new CommittedChange(EntityType.BASELINE, "b1",
                EntityOperation.CREATE, 5000L));
        client.consumeCommittedChanges();

        int result = client.submitPendingChanges();
        assert result == 0 : "Offline → 0 accepted";
        assert client.getPendingCount() == 1 : "Still pending (BR-BACKUP-01)";

        System.out.println("  ✓ testOfflineKeepsPending");
    }

    static void testEmptyNoop() {
        FakeBackupBoundary boundary = new FakeBackupBoundary();
        FakeDeviceDataAuthority data = new FakeDeviceDataAuthority();
        BackupClient client = new BackupClient("user-004", boundary, data);

        int result = client.submitPendingChanges();
        assert result == 0;
        assert boundary.getSubmittedBatches().isEmpty() : "No batch should be sent";

        System.out.println("  ✓ testEmptyNoop");
    }
}
