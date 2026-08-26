package com.timeback.device.room;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "open_session_candidates")
public final class OpenSessionCandidateEntity {
    @PrimaryKey
    @NonNull
    public final String owner;
    public final String packageName;
    public final long startedAtMillis;
    public final String sourceEventId;

    public OpenSessionCandidateEntity(
            String owner,
            String packageName,
            long startedAtMillis,
            String sourceEventId
    ) {
        this.owner = owner;
        this.packageName = packageName;
        this.startedAtMillis = startedAtMillis;
        this.sourceEventId = sourceEventId;
    }
}
