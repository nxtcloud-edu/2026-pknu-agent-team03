package com.timeback.backup.contracts;

/**
 * 서버 응답: 개별 변경 항목의 처리 결과.
 */
public class BackupItemResult {
    private final String changeId;
    private final BackupItemStatus status;
    private final String error; // nullable

    public BackupItemResult(String changeId, BackupItemStatus status, String error) {
        this.changeId = changeId;
        this.status = status;
        this.error = error;
    }

    public BackupItemResult(String changeId, BackupItemStatus status) {
        this(changeId, status, null);
    }

    public String getChangeId() { return changeId; }
    public BackupItemStatus getStatus() { return status; }
    public String getError() { return error; }
}
