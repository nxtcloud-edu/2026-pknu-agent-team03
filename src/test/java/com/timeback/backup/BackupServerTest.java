package com.timeback.backup;

import com.timeback.backup.contracts.*;
import com.timeback.backup.server.BackupServer;

import java.util.*;

/**
 * SRV-01~03 서버 단위 테스트.
 * JUnit 없이 main()에서 직접 assert — 빌드 도구 없는 MVP용.
 */
public class BackupServerTest {

    private static final BackupServer server = new BackupServer();

    public static void main(String[] args) {
        server.reset();

        testBackupSubmitAcceptsAll();
        testBackupIdempotent();
        testBackupStatusQuery();
        testRetentionApply();
        testDeletionComplete();
        testDeletionIdempotent();

        System.out.println("=== BackupServerTest: ALL PASSED ===");
    }

    static void testBackupSubmitAcceptsAll() {
        server.reset();
        BackupChange c1 = new BackupChange("c1", EntityType.USAGE_EVENT, "e1",
                EntityOperation.CREATE, 1000L);
        BackupChange c2 = new BackupChange("c2", EntityType.CONTEXT, "ctx1",
                EntityOperation.UPDATE, 2000L);

        BackupBatch batch = new BackupBatch("user-001", Arrays.asList(c1, c2));
        BackupBatchResponse resp = server.handleBackupSubmit(batch);

        assert resp.getResults().size() == 2 : "Expected 2 results";
        assert resp.getResults().get(0).getStatus() == BackupItemStatus.ACCEPTED;
        assert resp.getResults().get(1).getStatus() == BackupItemStatus.ACCEPTED;
        System.out.println("  ✓ testBackupSubmitAcceptsAll");
    }

    static void testBackupIdempotent() {
        server.reset();
        BackupChange c1 = new BackupChange("c1", EntityType.GOAL, "g1",
                EntityOperation.CREATE, 3000L);

        BackupBatch batch = new BackupBatch("user-001", Collections.singletonList(c1));
        server.handleBackupSubmit(batch);
        // 같은 changeId 재전송
        BackupBatchResponse resp2 = server.handleBackupSubmit(batch);

        assert resp2.getResults().get(0).getStatus() == BackupItemStatus.ACCEPTED
                : "Idempotent: should still be ACCEPTED";
        System.out.println("  ✓ testBackupIdempotent");
    }

    static void testBackupStatusQuery() {
        server.reset();
        BackupChange c1 = new BackupChange("c1", EntityType.ACTIVITY, "a1",
                EntityOperation.CREATE, 4000L);
        server.handleBackupSubmit(new BackupBatch("user-002", Collections.singletonList(c1)));

        Map<String, BackupChangeState> statuses =
                server.handleBackupStatusQuery("user-002", Arrays.asList("c1", "unknown"));

        assert statuses.get("c1") == BackupChangeState.ACCEPTED;
        assert statuses.get("unknown") == BackupChangeState.PENDING : "Unknown should be PENDING";
        System.out.println("  ✓ testBackupStatusQuery");
    }

    static void testRetentionApply() {
        server.reset();
        RetentionRequest req = new RetentionRequest("user-003", "30_DAYS");
        RetentionApplyResult result = server.handleRetentionApply(req);

        assert result.getServerStatus() == RetentionServerStatus.APPLIED;
        assert result.getRetentionSelection().equals("30_DAYS");
        System.out.println("  ✓ testRetentionApply");
    }

    static void testDeletionComplete() {
        server.reset();
        // 먼저 백업 데이터 넣기
        BackupChange c1 = new BackupChange("c1", EntityType.USER, "u1",
                EntityOperation.CREATE, 5000L);
        server.handleBackupSubmit(new BackupBatch("user-004", Collections.singletonList(c1)));

        // 삭제 요청
        DeletionRequest delReq = new DeletionRequest("user-004", "job-001");
        DeletionStatusResponse resp = server.handleDeletionRequest(delReq);

        assert resp.getServerStatus() == DeletionStatus.COMPLETED;
        assert resp.getJobId().equals("job-001");

        // 삭제 후 백업 상태 확인 — 데이터 없어야 함
        Map<String, BackupChangeState> statuses =
                server.handleBackupStatusQuery("user-004", Collections.singletonList("c1"));
        assert statuses.get("c1") == BackupChangeState.PENDING : "Deleted data should not exist";
        System.out.println("  ✓ testDeletionComplete");
    }

    static void testDeletionIdempotent() {
        server.reset();
        DeletionRequest req = new DeletionRequest("user-005", "job-002");
        server.handleDeletionRequest(req);
        // 같은 jobId 재요청
        DeletionStatusResponse resp2 = server.handleDeletionRequest(req);

        assert resp2.getServerStatus() == DeletionStatus.COMPLETED : "Idempotent deletion";
        System.out.println("  ✓ testDeletionIdempotent");
    }
}
