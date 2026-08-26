package com.timeback.backup.server;

import com.timeback.backup.contracts.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SRV-01 ~ SRV-03 백업 서버 비즈니스 로직.
 */
public class BackupServer {

    private final BackupStorage storage;

    public BackupServer() {
        this.storage = new BackupStorage();
    }

    public BackupServer(BackupStorage storage) {
        this.storage = storage;
    }

    // ═══════════════════════════════════════════════════
    // SRV-01: 백업 수신
    // ═══════════════════════════════════════════════════

    /**
     * POST /backup — BackupBatch를 항목별 독립 처리. 부분 성공 허용.
     */
    public BackupBatchResponse handleBackupSubmit(BackupBatch batch) {
        List<BackupItemResult> results = new ArrayList<>();

        for (BackupChange change : batch.getChanges()) {
            try {
                BackupItemStatus status = storage.storeBackupItem(
                        batch.getAnonymousUserId(), change);
                results.add(new BackupItemResult(change.getChangeId(), status));
            } catch (Exception e) {
                results.add(new BackupItemResult(
                        change.getChangeId(), BackupItemStatus.RETRYABLE_FAILURE, "internal_error"));
            }
        }

        return new BackupBatchResponse(results);
    }

    /**
     * GET /backup/status — 변경별 백업 상태 조회.
     */
    public Map<String, BackupChangeState> handleBackupStatusQuery(
            String anonymousUserId, List<String> changeIds) {
        Map<String, BackupChangeState> statuses = new HashMap<>();

        for (String changeId : changeIds) {
            BackupChangeState state = storage.getBackupStatus(anonymousUserId, changeId);
            statuses.put(changeId, state != null ? state : BackupChangeState.PENDING);
        }

        return statuses;
    }

    // ═══════════════════════════════════════════════════
    // SRV-02: 보관 기간
    // ═══════════════════════════════════════════════════

    /**
     * PUT /retention — 보관 선택 적용.
     */
    public RetentionApplyResult handleRetentionApply(RetentionRequest request) {
        storage.setRetention(request.getAnonymousUserId(), request.getRetentionSelection());
        return new RetentionApplyResult(
                request.getRetentionSelection(),
                "PENDING",  // 서버는 기기 상태를 모름
                RetentionServerStatus.APPLIED
        );
    }

    // ═══════════════════════════════════════════════════
    // SRV-03: 전체 삭제
    // ═══════════════════════════════════════════════════

    /**
     * POST /deletion — 전체 삭제 요청 수락.
     */
    public DeletionStatusResponse handleDeletionRequest(DeletionRequest request) {
        DeletionStatus status = storage.startDeletion(
                request.getAnonymousUserId(), request.getJobId());
        return new DeletionStatusResponse(request.getJobId(), status);
    }

    /**
     * GET /deletion/status — 삭제 진행 상태 조회.
     */
    public DeletionStatusResponse handleDeletionStatusQuery(String anonymousUserId, String jobId) {
        DeletionStatus status = storage.getDeletionStatus(anonymousUserId, jobId);
        return new DeletionStatusResponse(jobId,
                status != null ? status : DeletionStatus.PENDING);
    }

    public void reset() {
        storage.clear();
    }
}
