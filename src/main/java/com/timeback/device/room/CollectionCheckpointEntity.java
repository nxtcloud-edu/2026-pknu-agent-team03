package com.timeback.device.room;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "collection_checkpoints")
public final class CollectionCheckpointEntity {
    @PrimaryKey
    @NonNull
    public final String owner;
    public final long successfulThroughMillis;

    public CollectionCheckpointEntity(String owner, long successfulThroughMillis) {
        this.owner = owner;
        this.successfulThroughMillis = successfulThroughMillis;
    }
}
