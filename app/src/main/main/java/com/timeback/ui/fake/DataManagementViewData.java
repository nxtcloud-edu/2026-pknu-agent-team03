package com.timeback.ui.fake;

import androidx.annotation.Nullable;
import com.timeback.ui.domain.model.*;

public class DataManagementViewData {
    private final boolean anonymousIdReady;
    private final BackupStatus backupStatus;
    @Nullable private final RetentionSelection retentionSelection;
    @Nullable private final DeletionJobStatus deletionJob;

    public DataManagementViewData(boolean anonymousIdReady, BackupStatus backupStatus,
                                   @Nullable RetentionSelection retentionSelection,
                                   @Nullable DeletionJobStatus deletionJob) {
        this.anonymousIdReady = anonymousIdReady;
        this.backupStatus = backupStatus;
        this.retentionSelection = retentionSelection;
        this.deletionJob = deletionJob;
    }

    public boolean isAnonymousIdReady() { return anonymousIdReady; }
    public BackupStatus getBackupStatus() { return backupStatus; }
    @Nullable public RetentionSelection getRetentionSelection() { return retentionSelection; }
    @Nullable public DeletionJobStatus getDeletionJob() { return deletionJob; }
}
