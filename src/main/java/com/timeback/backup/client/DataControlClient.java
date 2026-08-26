package com.timeback.backup.client;

import com.timeback.backup.contracts.*;
import com.timeback.backup.port.BackupBoundary;
import com.timeback.backup.port.BackupDataAuthority;

import java.util.HashMap;
import java.util.Map;

/**
 * APP-13 데이터 통제 클라이언트.
 * 보관 기간 변경, 전체 삭제 (DeletionJob) 관리.
 *
 * 규칙:
 * - 기기·서버 양쪽 완료 전 전체 삭제를 완료 표시 안 함 (BR-DELETE-02)
 * - 삭제 재시도는 같은 DeletionJob 이어감 (BR-DELETE-03)
 * - 보관은 기기·서버 일관 적용 (BR-RETENTION-01)
 */
public class DataControlClient {

    private final String anonymousUserId;
    private final BackupBoundary boundary;
    private final BackupDataAuthority dataAuthority;
    private final Map<String, DeletionJob> deletionJobs = new HashMap<>();
    private String currentRetentionSelection;

    public DataControlClient(String anonymousUserId,
                             BackupBoundary boundary,
                             BackupDataAuthority dataAuthority) {
        this.anonymousUserId = anonymousUserId;
        this.boundary = boundary;
        this.dataAuthority = dataAuthority;
    }

    // ═══════════════════════════════════════════════════
    // 보관 기간
    // ═══════════════════════════════════════════════════

    /**
     * 보관 기간 선택 변경.
     * @return 서버 적용 성공 여부
     */
    public boolean applyRetention(String retentionSelection) {
        // 로컬 저장
        this.currentRetentionSelection = retentionSelection;

        // 서버 적용
        try {
            RetentionRequest request = new RetentionRequest(anonymousUserId, retentionSelection);
            RetentionApplyResult result = boundary.applyRetention(request);
            return result.getServerStatus() == RetentionServerStatus.APPLIED;
        } catch (RuntimeException e) {
            return false;
        }
    }

    public String getCurrentRetentionSelection() {
        return currentRetentionSelection;
    }

    // ═══════════════════════════════════════════════════
    // 전체 삭제
    // ═══════════════════════════════════════════════════

    /**
     * 전체 삭제 시작. DeletionJob 생성.
     */
    public DeletionJob startFullDeletion(String jobId) {
        DeletionJob job = new DeletionJob(jobId, anonymousUserId, System.currentTimeMillis());
        deletionJobs.put(jobId, job);

        // 1. 기기 삭제 실행
        boolean deviceSuccess = dataAuthority.deleteAllForUser(anonymousUserId);
        if (deviceSuccess) {
            job.setDeviceStatus(DeletionStatus.COMPLETED);
        } else {
            job.setDeviceStatus(DeletionStatus.FAILED);
        }

        // 2. 서버 삭제 요청
        try {
            DeletionRequest request = new DeletionRequest(anonymousUserId, jobId);
            DeletionStatusResponse response = boundary.requestDeletion(request);
            job.setServerStatus(response.getServerStatus());
        } catch (RuntimeException e) {
            job.setServerStatus(DeletionStatus.FAILED);
        }

        // 3. 양쪽 완료 확인
        job.checkCompletion();
        return job;
    }

    /**
     * 삭제 상태 폴링 — 서버 상태를 다시 확인.
     */
    public DeletionJob pollDeletionStatus(String jobId) {
        DeletionJob job = deletionJobs.get(jobId);
        if (job == null) return null;
        if (job.isCompleted()) return job;

        // 서버 상태 재확인
        try {
            DeletionStatusResponse response = boundary.readDeletionStatus(anonymousUserId, jobId);
            job.setServerStatus(response.getServerStatus());
        } catch (RuntimeException e) {
            job.setServerStatus(DeletionStatus.FAILED);
        }

        job.checkCompletion();
        return job;
    }

    /**
     * 실패한 삭제 재시도 — 같은 jobId 이어감 (BR-DELETE-03).
     */
    public DeletionJob retryDeletion(String jobId) {
        DeletionJob job = deletionJobs.get(jobId);
        if (job == null) return null;
        if (job.isCompleted()) return job;

        // 기기 미완료면 재시도
        if (job.getDeviceStatus() != DeletionStatus.COMPLETED) {
            boolean deviceSuccess = dataAuthority.deleteAllForUser(anonymousUserId);
            job.setDeviceStatus(deviceSuccess ? DeletionStatus.COMPLETED : DeletionStatus.FAILED);
        }

        // 서버 미완료면 재시도
        if (job.getServerStatus() != DeletionStatus.COMPLETED) {
            try {
                DeletionRequest request = new DeletionRequest(anonymousUserId, jobId);
                DeletionStatusResponse response = boundary.requestDeletion(request);
                job.setServerStatus(response.getServerStatus());
            } catch (RuntimeException e) {
                job.setServerStatus(DeletionStatus.FAILED);
            }
        }

        job.checkCompletion();
        return job;
    }

    public DeletionJob getDeletionJob(String jobId) {
        return deletionJobs.get(jobId);
    }
}
