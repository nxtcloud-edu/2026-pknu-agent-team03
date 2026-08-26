package com.timeback.ui.domain.model;

public class DeletionJobStatus {
    private final Identifier jobId;
    private final boolean deviceCompleted;
    private final boolean serverCompleted;

    public DeletionJobStatus(Identifier jobId, boolean deviceCompleted, boolean serverCompleted) {
        this.jobId = jobId;
        this.deviceCompleted = deviceCompleted;
        this.serverCompleted = serverCompleted;
    }

    public Identifier getJobId() { return jobId; }
    public boolean isDeviceCompleted() { return deviceCompleted; }
    public boolean isServerCompleted() { return serverCompleted; }
}
