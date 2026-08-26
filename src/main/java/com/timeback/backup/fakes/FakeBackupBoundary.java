package com.timeback.backup.fakes;

import com.timeback.backup.contracts.*;

import java.util.ArrayList;
import java.util.List;

/**
 * CT-06 테스트 대역 — 서버 응답 시뮬레이션.
 * APP-12, APP-13 단위 테스트에서 실제 서버 없이 사용.
 */
public class FakeBackupBoundary {

    public enum BackupMode { ACCEPT_ALL, PARTIAL_FAILURE, OFFLINE, REJECT_ALL }
    public enum DeletionMode { COMPLETE, IN_PROGRESS, FAILED }
    public enum RetentionMode { APPLIED, FAILED }

    private BackupMode backupMode = BackupMode.ACCEPT_ALL;
    private DeletionMode deletionMode = DeletionMode.COMPLETE;
    private RetentionMode retentionMode = RetentionMode.APPLIED;

    // 관찰용 기록
    private final List<BackupBatch> submittedBatches = new ArrayList<>();
    private final List<DeletionRequest> deletionRequests = new ArrayList<>();
    private final List<RetentionRequest> retentionRequests = new ArrayList<>();

    // ─── 백업 ──────────────────────────────────────────

    public BackupBatchResponse submitBackup(BackupBatch batch) {
        submittedBatches.add(batch);

        if (backupMode == BackupMode.OFFLINE) {
            throw new RuntimeException("OFFLINE");
        }

        List<BackupItemResult> results = new ArrayList<>();
        List<BackupChange> changes = batch.getChanges();

        for (int i = 0; i < changes.size(); i++) {
            BackupChange c = changes.get(i);
            switch (backupMode) {
                case ACCEPT_ALL:
                    results.add(new BackupItemResult(c.getChangeId(), BackupItemStatus.ACCEPTED));
                    break;
                case PARTIAL_FAILURE:
                    if (i % 2 == 0) {
                        results.add(new BackupItemResult(c.getChangeId(), BackupItemStatus.ACCEPTED));
                    } else {
                        results.add(new BackupItemResult(c.getChangeId(),
                                BackupItemStatus.RETRYABLE_FAILURE, "server_temp_error"));
                    }
                    break;
                case REJECT_ALL:
                    results.add(new BackupItemResult(c.getChangeId(),
                            BackupItemStatus.FAILED, "permanently_rejected"));
                    break;
            }
        }
        return new BackupBatchResponse(results);
    }

    // ─── 삭제 ──────────────────────────────────────────

    public DeletionStatusResponse requestDeletion(DeletionRequest request) {
        deletionRequests.add(request);

        switch (deletionMode) {
            case COMPLETE:
                return new DeletionStatusResponse(request.getJobId(), DeletionStatus.COMPLETED);
            case IN_PROGRESS:
                return new DeletionStatusResponse(request.getJobId(), DeletionStatus.IN_PROGRESS);
            case FAILED:
                throw new RuntimeException("deletion_server_error");
            default:
                return new DeletionStatusResponse(request.getJobId(), DeletionStatus.PENDING);
        }
    }

    public DeletionStatusResponse readDeletionStatus(String jobId) {
        switch (deletionMode) {
            case COMPLETE:
                return new DeletionStatusResponse(jobId, DeletionStatus.COMPLETED);
            case IN_PROGRESS:
                return new DeletionStatusResponse(jobId, DeletionStatus.IN_PROGRESS);
            case FAILED:
                return new DeletionStatusResponse(jobId, DeletionStatus.FAILED);
            default:
                return new DeletionStatusResponse(jobId, DeletionStatus.PENDING);
        }
    }

    // ─── 보관 ──────────────────────────────────────────

    public RetentionApplyResult applyRetention(RetentionRequest request) {
        retentionRequests.add(request);

        if (retentionMode == RetentionMode.FAILED) {
            throw new RuntimeException("retention_server_error");
        }
        return new RetentionApplyResult(
                request.getRetentionSelection(), "APPLIED", RetentionServerStatus.APPLIED);
    }

    // ─── 모드 설정 ────────────────────────────────────

    public void setBackupMode(BackupMode mode) { this.backupMode = mode; }
    public void setDeletionMode(DeletionMode mode) { this.deletionMode = mode; }
    public void setRetentionMode(RetentionMode mode) { this.retentionMode = mode; }

    // ─── 관찰 ──────────────────────────────────────────

    public List<BackupBatch> getSubmittedBatches() { return submittedBatches; }
    public List<DeletionRequest> getDeletionRequests() { return deletionRequests; }
    public List<RetentionRequest> getRetentionRequests() { return retentionRequests; }

    public void reset() {
        submittedBatches.clear();
        deletionRequests.clear();
        retentionRequests.clear();
    }
}
