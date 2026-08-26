package com.timeback.backup.client;

import com.timeback.backup.contracts.*;
import com.timeback.backup.port.BackupBoundary;
import com.timeback.backup.port.BackupDataAuthority;

import java.util.ArrayList;
import java.util.List;

/**
 * APP-12 백업 클라이언트.
 * CommittedChange를 소비하여 BackupChange를 생성하고,
 * 서버(또는 FakeBackupBoundary)에 전송·재시도한다.
 *
 * 규칙:
 * - 백업 실패는 로컬 저장을 되돌리지 않음 (BR-BACKUP-01)
 * - 같은 changeId 재전송은 중복 적용 안 됨 (BR-BACKUP-02)
 * - 부분 성공: 성공만 ACCEPTED, 나머지 PENDING 유지 (BR-BACKUP-03)
 */
public class BackupClient {

    private final String anonymousUserId;
    private final BackupBoundary boundary;
    private final BackupDataAuthority dataAuthority;
    private final List<BackupChange> pendingChanges = new ArrayList<>();

    public BackupClient(String anonymousUserId,
                        BackupBoundary boundary,
                        BackupDataAuthority dataAuthority) {
        this.anonymousUserId = anonymousUserId;
        this.boundary = boundary;
        this.dataAuthority = dataAuthority;
    }

    /**
     * CT-03 CommittedChange를 수신하여 BackupChange로 변환.
     */
    public void consumeCommittedChanges() {
        List<CommittedChange> changes = dataAuthority.readCommittedChanges();
        for (CommittedChange c : changes) {
            BackupChange bc = new BackupChange(
                    c.getChangeId(),
                    c.getEntityType(),
                    c.getEntityId(),
                    c.getOperation(),
                    c.getOccurredAt()
            );
            pendingChanges.add(bc);
        }
    }

    /**
     * 대기 중인 변경을 서버로 전송 시도.
     * @return 이번 전송에서 ACCEPTED된 변경 수
     */
    public int submitPendingChanges() {
        if (pendingChanges.isEmpty()) return 0;

        // PENDING 상태인 것만 전송
        List<BackupChange> toSend = new ArrayList<>();
        for (BackupChange bc : pendingChanges) {
            if (bc.getState() == BackupChangeState.PENDING ||
                bc.getState() == BackupChangeState.RETRYABLE_FAILURE) {
                toSend.add(bc);
            }
        }

        if (toSend.isEmpty()) return 0;

        BackupBatch batch = new BackupBatch(anonymousUserId, toSend);

        try {
            BackupBatchResponse response = boundary.submitBackup(batch);
            int accepted = 0;

            for (BackupItemResult result : response.getResults()) {
                BackupChange target = findByChangeId(result.getChangeId());
                if (target == null) continue;

                switch (result.getStatus()) {
                    case ACCEPTED:
                        target.setState(BackupChangeState.ACCEPTED);
                        accepted++;
                        break;
                    case RETRYABLE_FAILURE:
                        target.setState(BackupChangeState.RETRYABLE_FAILURE);
                        target.incrementRetry();
                        break;
                    case FAILED:
                        target.setState(BackupChangeState.FAILED);
                        break;
                }
            }

            // ACCEPTED된 것은 대기 목록에서 제거
            pendingChanges.removeIf(bc -> bc.getState() == BackupChangeState.ACCEPTED);
            return accepted;

        } catch (RuntimeException e) {
            // 오프라인 — 모든 변경 PENDING 유지 (BR-BACKUP-01)
            return 0;
        }
    }

    /**
     * 재시도: RETRYABLE_FAILURE 상태인 것들을 다시 전송.
     */
    public int retry() {
        return submitPendingChanges();
    }

    public List<BackupChange> getPendingChanges() {
        return new ArrayList<>(pendingChanges);
    }

    public int getPendingCount() {
        return (int) pendingChanges.stream()
                .filter(bc -> bc.getState() != BackupChangeState.ACCEPTED)
                .count();
    }

    private BackupChange findByChangeId(String changeId) {
        for (BackupChange bc : pendingChanges) {
            if (bc.getChangeId().equals(changeId)) return bc;
        }
        return null;
    }
}
