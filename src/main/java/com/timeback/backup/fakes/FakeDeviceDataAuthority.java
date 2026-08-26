package com.timeback.backup.fakes;

import com.timeback.backup.contracts.*;
import com.timeback.backup.port.BackupDataAuthority;

import java.util.ArrayList;
import java.util.List;

/**
 * CT-06 테스트 대역 — CT-03 저장 통지 중 백업 트랙이 사용하는 부분.
 */
public class FakeDeviceDataAuthority implements BackupDataAuthority {

    public enum DeleteMode { SUCCESS, PARTIAL_FAILURE, FAILURE }

    private DeleteMode deleteMode = DeleteMode.SUCCESS;
    private final List<CommittedChange> pendingChanges = new ArrayList<>();
    private final List<String> deleteRequests = new ArrayList<>();

    /**
     * CommittedChange 큐에 추가 (다른 트랙이 저장 완료 시 통지).
     */
    public void notifyChange(CommittedChange change) {
        pendingChanges.add(change);
    }

    /**
     * 대기 중인 CommittedChange 읽기 (APP-12가 호출).
     */
    public List<CommittedChange> readCommittedChanges() {
        List<CommittedChange> result = new ArrayList<>(pendingChanges);
        pendingChanges.clear();
        return result;
    }

    /**
     * 전체 삭제 실행 (APP-13이 기기 삭제 시 호출).
     * @return true = 성공, false = 실패
     */
    public boolean deleteAllForUser(String anonymousUserId) {
        deleteRequests.add(anonymousUserId);
        return deleteMode != DeleteMode.FAILURE;
    }

    public void setDeleteMode(DeleteMode mode) { this.deleteMode = mode; }
    public List<String> getDeleteRequests() { return deleteRequests; }

    public void reset() {
        pendingChanges.clear();
        deleteRequests.clear();
        deleteMode = DeleteMode.SUCCESS;
    }
}
