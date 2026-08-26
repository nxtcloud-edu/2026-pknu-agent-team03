package com.timeback.backup.contracts;

import java.util.List;

/**
 * 서버로 전송하는 백업 묶음.
 */
public class BackupBatch {
    private final String anonymousUserId;
    private final List<BackupChange> changes;

    public BackupBatch(String anonymousUserId, List<BackupChange> changes) {
        this.anonymousUserId = anonymousUserId;
        this.changes = changes;
    }

    public String getAnonymousUserId() { return anonymousUserId; }
    public List<BackupChange> getChanges() { return changes; }
}
