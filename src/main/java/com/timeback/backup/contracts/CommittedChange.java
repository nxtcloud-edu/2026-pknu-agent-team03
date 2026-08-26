package com.timeback.backup.contracts;

/**
 * CT-03에서 제공하는 로컬 저장 완료 통지.
 * APP-12가 이것을 소비하여 BackupChange를 생성한다.
 */
public class CommittedChange {
    private final EntityType entityType;
    private final String entityId;
    private final EntityOperation operation;
    private final long occurredAt; // epoch millis

    public CommittedChange(EntityType entityType, String entityId,
                           EntityOperation operation, long occurredAt) {
        this.entityType = entityType;
        this.entityId = entityId;
        this.operation = operation;
        this.occurredAt = occurredAt;
    }

    public EntityType getEntityType() { return entityType; }
    public String getEntityId() { return entityId; }
    public EntityOperation getOperation() { return operation; }
    public long getOccurredAt() { return occurredAt; }
}
