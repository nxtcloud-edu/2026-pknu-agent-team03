package com.timeback.backup.contracts;

public class DeletionStatusResponse {
    private final String jobId;
    private final DeletionStatus serverStatus;

    public DeletionStatusResponse(String jobId, DeletionStatus serverStatus) {
        this.jobId = jobId;
        this.serverStatus = serverStatus;
    }

    public String getJobId() { return jobId; }
    public DeletionStatus getServerStatus() { return serverStatus; }
}
