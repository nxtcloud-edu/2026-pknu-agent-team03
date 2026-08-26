package com.timeback.device.room;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(
        entities = {
                GenericRecordEntity.class,
                UsageEventEntity.class,
                AppSessionEntity.class,
                CollectionCheckpointEntity.class,
                OpenSessionCandidateEntity.class,
                CommittedChangeEntity.class,
                OwnerSequenceEntity.class
        },
        version = 1,
        exportSchema = true
)
public abstract class TimeBackRoomDatabase extends RoomDatabase {
    public abstract DeviceDataDao deviceDataDao();

    public static TimeBackRoomDatabase create(Context context) {
        return Room.databaseBuilder(
                        context.getApplicationContext(),
                        TimeBackRoomDatabase.class,
                        "timeback.db"
                )
                .setJournalMode(JournalMode.WRITE_AHEAD_LOGGING)
                .build();
    }
}
