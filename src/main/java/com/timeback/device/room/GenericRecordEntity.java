package com.timeback.device.room;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;

@Entity(
        tableName = "device_records",
        primaryKeys = {"owner", "entityType", "recordId"},
        indices = @Index(value = {"owner", "entityType", "startAtMillis", "endAtMillis"})
)
public final class GenericRecordEntity {
    @NonNull public final String owner;
    @NonNull public final String entityType;
    @NonNull public final String recordId;
    @Nullable public final Long startAtMillis;
    @Nullable public final Long endAtMillis;
    public final String payload;

    public GenericRecordEntity(
            String owner,
            String entityType,
            String recordId,
            @Nullable Long startAtMillis,
            @Nullable Long endAtMillis,
            String payload
    ) {
        this.owner = owner;
        this.entityType = entityType;
        this.recordId = recordId;
        this.startAtMillis = startAtMillis;
        this.endAtMillis = endAtMillis;
        this.payload = payload;
    }
}
