package com.timeback.device.room;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;

@Entity(
        tableName = "usage_events",
        primaryKeys = {"owner", "eventId"},
        indices = @Index(value = {"owner", "occurredAtMillis", "sourceOrder"})
)
public final class UsageEventEntity {
    @NonNull public final String owner;
    @NonNull public final String eventId;
    public final String packageName;
    public final String kind;
    public final long occurredAtMillis;
    public final long collectedAtMillis;
    public final int sourceOrder;

    public UsageEventEntity(
            String owner,
            String eventId,
            String packageName,
            String kind,
            long occurredAtMillis,
            long collectedAtMillis,
            int sourceOrder
    ) {
        this.owner = owner;
        this.eventId = eventId;
        this.packageName = packageName;
        this.kind = kind;
        this.occurredAtMillis = occurredAtMillis;
        this.collectedAtMillis = collectedAtMillis;
        this.sourceOrder = sourceOrder;
    }
}
