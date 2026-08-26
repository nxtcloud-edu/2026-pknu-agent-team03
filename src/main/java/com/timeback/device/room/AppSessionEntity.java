package com.timeback.device.room;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;

@Entity(
        tableName = "app_sessions",
        primaryKeys = {"owner", "sessionId"},
        indices = @Index(value = {"owner", "startAtMillis", "endAtMillis"})
)
public final class AppSessionEntity {
    @NonNull public final String owner;
    @NonNull public final String sessionId;
    public final String logicalSessionId;
    public final String packageName;
    public final long startAtMillis;
    public final long endAtMillis;
    public final String completionCause;
    public final String sourceEventIds;

    public AppSessionEntity(
            String owner,
            String sessionId,
            String logicalSessionId,
            String packageName,
            long startAtMillis,
            long endAtMillis,
            String completionCause,
            String sourceEventIds
    ) {
        this.owner = owner;
        this.sessionId = sessionId;
        this.logicalSessionId = logicalSessionId;
        this.packageName = packageName;
        this.startAtMillis = startAtMillis;
        this.endAtMillis = endAtMillis;
        this.completionCause = completionCause;
        this.sourceEventIds = sourceEventIds;
    }
}
