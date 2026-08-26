package com.timeback.ui.domain.model;

import androidx.annotation.Nullable;

public class BackupStatus {
    private final int pendingCount;
    @Nullable private final Long lastSuccessAt;
    private final boolean isOffline;

    public BackupStatus(int pendingCount, @Nullable Long lastSuccessAt, boolean isOffline) {
        this.pendingCount = pendingCount;
        this.lastSuccessAt = lastSuccessAt;
        this.isOffline = isOffline;
    }

    public int getPendingCount() { return pendingCount; }
    @Nullable public Long getLastSuccessAt() { return lastSuccessAt; }
    public boolean isOffline() { return isOffline; }
}
