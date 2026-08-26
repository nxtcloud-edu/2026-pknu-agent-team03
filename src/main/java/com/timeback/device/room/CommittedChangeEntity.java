package com.timeback.device.room;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;

@Entity(
        tableName = "committed_changes",
        primaryKeys = {"owner", "sequence"},
        indices = @Index(value = {"changeId"}, unique = true)
)
public final class CommittedChangeEntity {
    @NonNull public final String owner;
    public final long sequence;
    public final String changeId;
    public final String entityType;
    public final String entityId;
    public final String operation;
    public final long occurredAtMillis;

    public CommittedChangeEntity(
            String owner,
            long sequence,
            String changeId,
            String entityType,
            String entityId,
            String operation,
            long occurredAtMillis
    ) {
        this.owner = owner;
        this.sequence = sequence;
        this.changeId = changeId;
        this.entityType = entityType;
        this.entityId = entityId;
        this.operation = operation;
        this.occurredAtMillis = occurredAtMillis;
    }
}
