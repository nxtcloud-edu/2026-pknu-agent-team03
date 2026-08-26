package com.timeback.backup.contracts;

/**
 * 백업 변경 항목. 로컬 CommittedChange에서 파생.
 * changeId 기반 멱등 처리의 단위.
 */
public class BackupChange {
    private final String changeId;
    private final EntityType entityType;
    private final String entityId;
    private final EntityOperation operation;
    private final long occurredAt;
    private BackupChangeState state;
    private int retryCount;

    public BackupChange(String changeId, EntityType entityType, String entityId,
                        EntityOperation operation, long occurredAt) {
        this.changeId = changeId;
        this.entityType = entityType;
        this.entityId = entityId;
        this.operation = operation;
        this.occurredAt = occurredAt;
        this.state = BackupChangeState.PENDING;
        this.retryCount = 0;
    }

    public String getChangeId() { return changeId; }
    public EntityType getEntityType() { return entityType; }
    public String getEntityId() { return entityId; }
    public EntityOperation getOperation() { return operation; }
    public long getOccurredAt() { return occurredAt; }
    public BackupChangeState getState() { return state; }
    public int getRetryCount() { return retryCount; }

    public void setState(BackupChangeState state) { this.state = state; }
    public void incrementRetry() { this.retryCount++; }
}
