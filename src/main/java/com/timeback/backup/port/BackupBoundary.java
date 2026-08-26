package com.timeback.backup.port;

import com.timeback.backup.contracts.BackupBatch;
import com.timeback.backup.contracts.BackupBatchResponse;
import com.timeback.backup.contracts.DeletionRequest;
import com.timeback.backup.contracts.DeletionStatusResponse;
import com.timeback.backup.contracts.RetentionApplyResult;
import com.timeback.backup.contracts.RetentionRequest;

/** CT-05 client-to-server boundary shared by fake, in-process, and HTTP adapters. */
public interface BackupBoundary {
    BackupBatchResponse submitBackup(BackupBatch batch);

    DeletionStatusResponse requestDeletion(DeletionRequest request);

    DeletionStatusResponse readDeletionStatus(String anonymousUserId, String jobId);

    RetentionApplyResult applyRetention(RetentionRequest request);
}
