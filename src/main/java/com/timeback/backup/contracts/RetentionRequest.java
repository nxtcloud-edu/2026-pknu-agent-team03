package com.timeback.backup.contracts;

public class RetentionRequest {
    private final String anonymousUserId;
    private final String retentionSelection;

    public RetentionRequest(String anonymousUserId, String retentionSelection) {
        this.anonymousUserId = anonymousUserId;
        this.retentionSelection = retentionSelection;
    }

    public String getAnonymousUserId() { return anonymousUserId; }
    public String getRetentionSelection() { return retentionSelection; }
}
