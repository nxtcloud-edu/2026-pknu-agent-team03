package com.timeback.device.room;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "owner_sequences")
public final class OwnerSequenceEntity {
    @PrimaryKey
    @NonNull
    public final String owner;
    public final long currentSequence;

    public OwnerSequenceEntity(String owner, long currentSequence) {
        this.owner = owner;
        this.currentSequence = currentSequence;
    }
}
