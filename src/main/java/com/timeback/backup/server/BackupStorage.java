package com.timeback.backup.server;

import com.timeback.backup.contracts.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 서버 측 인메모리 저장소 (MVP).
 * 인터페이스만 맞추면 나중에 DB로 교체 가능.
 */
public class BackupStorage {

    // anonymousUserId → (changeId → status)
    private final Map<String, Map<String, BackupItemStatus>> backupStore = new ConcurrentHashMap<>();

    // anonymousUserId → retentionSelection
    private final Map<String, String> retentionStore = new ConcurrentHashMap<>();

    // anonymousUserId → (jobId → DeletionStatus)
    private final Map<String, Map<String, DeletionStatus>> deletionStore = new ConcurrentHashMap<>();

    // ─── 백업 ──────────────────────────────────────────

    /**
     * 백업 항목 저장. changeId 기반 멱등.
     */
    public BackupItemStatus storeBackupItem(String anonymousUserId, BackupChange change) {
        Map<String, BackupItemStatus> userStore =
                backupStore.computeIfAbsent(anonymousUserId, k -> new ConcurrentHashMap<>());

        // 이미 ACCEPTED면 중복 저장 안 함
        BackupItemStatus existing = userStore.get(change.getChangeId());
        if (existing == BackupItemStatus.ACCEPTED) {
            return BackupItemStatus.ACCEPTED;
        }

        userStore.put(change.getChangeId(), BackupItemStatus.ACCEPTED);
        return BackupItemStatus.ACCEPTED;
    }

    public BackupChangeState getBackupStatus(String anonymousUserId, String changeId) {
        Map<String, BackupItemStatus> userStore = backupStore.get(anonymousUserId);
        if (userStore == null) return null;

        BackupItemStatus status = userStore.get(changeId);
        if (status == null) return null;

        switch (status) {
            case ACCEPTED: return BackupChangeState.ACCEPTED;
            case RETRYABLE_FAILURE: return BackupChangeState.RETRYABLE_FAILURE;
            case FAILED: return BackupChangeState.FAILED;
            default: return null;
        }
    }

    // ─── 보관 ──────────────────────────────────────────

    public void setRetention(String anonymousUserId, String selection) {
        retentionStore.put(anonymousUserId, selection);
    }

    public String getRetention(String anonymousUserId) {
        return retentionStore.get(anonymousUserId);
    }

    // ─── 삭제 ──────────────────────────────────────────

    /**
     * 전체 삭제 시작. 멱등 — 이미 존재하는 jobId면 기존 상태 반환.
     * MVP에서는 즉시 완료 처리.
     */
    public DeletionStatus startDeletion(String anonymousUserId, String jobId) {
        Map<String, DeletionStatus> userJobs =
                deletionStore.computeIfAbsent(anonymousUserId, k -> new ConcurrentHashMap<>());

        if (userJobs.containsKey(jobId)) {
            return userJobs.get(jobId);
        }

        // 해당 사용자 데이터 전체 삭제
        backupStore.remove(anonymousUserId);
        retentionStore.remove(anonymousUserId);

        userJobs.put(jobId, DeletionStatus.COMPLETED);
        return DeletionStatus.COMPLETED;
    }

    public DeletionStatus getDeletionStatus(String anonymousUserId, String jobId) {
        Map<String, DeletionStatus> userJobs = deletionStore.get(anonymousUserId);
        if (userJobs == null) return null;
        return userJobs.get(jobId);
    }

    public void clear() {
        backupStore.clear();
        retentionStore.clear();
        deletionStore.clear();
    }
}
