package com.timeback.backup.contracts;

public class DeletionRequest {
    private final String anonymousUserId;
    private final String jobId;

    public DeletionRequest(String anonymousUserId, String jobId) {
        this.anonymousUserId = anonymousUserId;
        this.jobId = jobId;
    }

    public String getAnonymousUserId() { return anonymousUserId; }
    public String getJobId() { return jobId; }
}
