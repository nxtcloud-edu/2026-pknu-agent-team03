package com.timeback.backup;

import com.timeback.backup.client.DataControlClient;
import com.timeback.backup.contracts.*;
import com.timeback.backup.fakes.FakeBackupBoundary;
import com.timeback.backup.fakes.FakeDeviceDataAuthority;

/**
 * APP-13 데이터 통제 클라이언트 단위 테스트.
 */
public class DataControlClientTest {

    public static void main(String[] args) {
        testRetentionApplySuccess();
        testRetentionApplyServerFail();
        testFullDeletionSuccess();
        testFullDeletionServerFail();
        testDeletionRetry();

        System.out.println("=== DataControlClientTest: ALL PASSED ===");
    }

    static void testRetentionApplySuccess() {
        FakeBackupBoundary boundary = new FakeBackupBoundary();
        FakeDeviceDataAuthority data = new FakeDeviceDataAuthority();
        DataControlClient client = new DataControlClient("user-001", boundary, data);

        boolean result = client.applyRetention("30_DAYS");
        assert result : "Should succeed";
        assert "30_DAYS".equals(client.getCurrentRetentionSelection());

        System.out.println("  ✓ testRetentionApplySuccess");
    }

    static void testRetentionApplyServerFail() {
        FakeBackupBoundary boundary = new FakeBackupBoundary();
        boundary.setRetentionMode(FakeBackupBoundary.RetentionMode.FAILED);
        FakeDeviceDataAuthority data = new FakeDeviceDataAuthority();
        DataControlClient client = new DataControlClient("user-002", boundary, data);

        boolean result = client.applyRetention("7_DAYS");
        assert !result : "Should fail when server fails";
        // 로컬은 저장됨
        assert "7_DAYS".equals(client.getCurrentRetentionSelection());

        System.out.println("  ✓ testRetentionApplyServerFail");
    }

    static void testFullDeletionSuccess() {
        FakeBackupBoundary boundary = new FakeBackupBoundary();
        FakeDeviceDataAuthority data = new FakeDeviceDataAuthority();
        DataControlClient client = new DataControlClient("user-003", boundary, data);

        DeletionJob job = client.startFullDeletion("job-001");
        assert job.getDeviceStatus() == DeletionStatus.COMPLETED;
        assert job.getServerStatus() == DeletionStatus.COMPLETED;
        assert job.isCompleted() : "Both sides completed → job completed (BR-DELETE-02)";

        System.out.println("  ✓ testFullDeletionSuccess");
    }

    static void testFullDeletionServerFail() {
        FakeBackupBoundary boundary = new FakeBackupBoundary();
        boundary.setDeletionMode(FakeBackupBoundary.DeletionMode.FAILED);
        FakeDeviceDataAuthority data = new FakeDeviceDataAuthority();
        DataControlClient client = new DataControlClient("user-004", boundary, data);

        DeletionJob job = client.startFullDeletion("job-002");
        assert job.getDeviceStatus() == DeletionStatus.COMPLETED : "Device should succeed";
        assert job.getServerStatus() == DeletionStatus.FAILED : "Server should fail";
        assert !job.isCompleted() : "Not completed — partial failure (BR-DELETE-02)";

        System.out.println("  ✓ testFullDeletionServerFail");
    }

    static void testDeletionRetry() {
        FakeBackupBoundary boundary = new FakeBackupBoundary();
        boundary.setDeletionMode(FakeBackupBoundary.DeletionMode.FAILED);
        FakeDeviceDataAuthority data = new FakeDeviceDataAuthority();
        DataControlClient client = new DataControlClient("user-005", boundary, data);

        // 첫 시도 — 서버 실패
        DeletionJob job = client.startFullDeletion("job-003");
        assert !job.isCompleted();

        // 재시도 — 서버 복구
        boundary.setDeletionMode(FakeBackupBoundary.DeletionMode.COMPLETE);
        DeletionJob retried = client.retryDeletion("job-003");
        assert retried.getServerStatus() == DeletionStatus.COMPLETED;
        assert retried.isCompleted() : "After retry, both completed (BR-DELETE-03)";

        System.out.println("  ✓ testDeletionRetry");
    }
}
