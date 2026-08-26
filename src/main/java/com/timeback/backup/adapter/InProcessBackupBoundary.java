package com.timeback.backup.adapter;

import com.timeback.backup.contracts.BackupBatch;
import com.timeback.backup.contracts.BackupBatchResponse;
import com.timeback.backup.contracts.DeletionRequest;
import com.timeback.backup.contracts.DeletionStatusResponse;
import com.timeback.backup.contracts.RetentionApplyResult;
import com.timeback.backup.contracts.RetentionRequest;
import com.timeback.backup.port.BackupBoundary;
import com.timeback.backup.server.BackupServer;

import java.util.Objects;

/** Executable local boundary used before the HTTP/Spring adapter is connected. */
public final class InProcessBackupBoundary implements BackupBoundary {
    private final BackupServer server;

    public InProcessBackupBoundary(BackupServer server) {
        this.server = Objects.requireNonNull(server, "server");
    }

    @Override
    public BackupBatchResponse submitBackup(BackupBatch batch) {
        return server.handleBackupSubmit(batch);
    }

    @Override
    public DeletionStatusResponse requestDeletion(DeletionRequest request) {
        return server.handleDeletionRequest(request);
    }

    @Override
    public DeletionStatusResponse readDeletionStatus(String anonymousUserId, String jobId) {
        return server.handleDeletionStatusQuery(anonymousUserId, jobId);
    }

    @Override
    public RetentionApplyResult applyRetention(RetentionRequest request) {
        return server.handleRetentionApply(request);
    }
}
