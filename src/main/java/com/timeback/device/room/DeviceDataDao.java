package com.timeback.device.room;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface DeviceDataDao {
    @Query("SELECT * FROM device_records WHERE owner = :owner AND entityType = :entityType AND recordId = :recordId")
    GenericRecordEntity readRecord(String owner, String entityType, String recordId);

    @Query("SELECT * FROM device_records WHERE owner = :owner AND entityType = :entityType "
            + "AND startAtMillis < :endAt AND endAtMillis > :startAt "
            + "ORDER BY startAtMillis, endAtMillis, recordId")
    List<GenericRecordEntity> readPeriodRecords(
            String owner,
            String entityType,
            long startAt,
            long endAt
    );

    @Query("SELECT * FROM device_records WHERE owner = :owner AND entityType = :entityType")
    List<GenericRecordEntity> readAllRecords(String owner, String entityType);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertRecords(List<GenericRecordEntity> records);

    @Query("DELETE FROM device_records WHERE owner = :owner AND entityType = :entityType "
            + "AND startAtMillis < :endAt AND endAtMillis > :startAt")
    void deletePeriodRecords(String owner, String entityType, long startAt, long endAt);

    @Query("DELETE FROM device_records WHERE owner = :owner AND entityType = :entityType")
    void deleteAllRecords(String owner, String entityType);

    @Query("SELECT * FROM usage_events WHERE owner = :owner AND eventId = :eventId")
    UsageEventEntity readUsageEvent(String owner, String eventId);

    @Query("SELECT * FROM usage_events WHERE owner = :owner "
            + "AND occurredAtMillis >= :startAt AND occurredAtMillis < :endAt "
            + "ORDER BY occurredAtMillis, sourceOrder, eventId")
    List<UsageEventEntity> readUsageEvents(String owner, long startAt, long endAt);

    @Query("SELECT * FROM usage_events WHERE owner = :owner")
    List<UsageEventEntity> readAllUsageEvents(String owner);

    @Query("SELECT eventId FROM usage_events WHERE owner = :owner")
    List<String> readAllUsageEventIds(String owner);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertUsageEvents(List<UsageEventEntity> events);

    @Query("DELETE FROM usage_events WHERE owner = :owner")
    void deleteUsageEvents(String owner);

    @Query("SELECT * FROM collection_checkpoints WHERE owner = :owner")
    CollectionCheckpointEntity readCheckpoint(String owner);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertCheckpoint(CollectionCheckpointEntity checkpoint);

    @Query("DELETE FROM collection_checkpoints WHERE owner = :owner")
    void deleteCheckpoint(String owner);

    @Query("SELECT * FROM app_sessions WHERE owner = :owner "
            + "AND startAtMillis < :endAt AND endAtMillis > :startAt "
            + "ORDER BY startAtMillis, endAtMillis, sessionId")
    List<AppSessionEntity> readSessions(String owner, long startAt, long endAt);

    @Query("SELECT * FROM app_sessions WHERE owner = :owner")
    List<AppSessionEntity> readAllSessions(String owner);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertSessions(List<AppSessionEntity> sessions);

    @Query("DELETE FROM app_sessions WHERE owner = :owner "
            + "AND startAtMillis < :endAt AND endAtMillis > :startAt")
    void deletePeriodSessions(String owner, long startAt, long endAt);

    @Query("DELETE FROM app_sessions WHERE owner = :owner")
    void deleteSessions(String owner);

    @Query("SELECT * FROM open_session_candidates WHERE owner = :owner")
    OpenSessionCandidateEntity readOpenCandidate(String owner);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertOpenCandidate(OpenSessionCandidateEntity candidate);

    @Query("DELETE FROM open_session_candidates WHERE owner = :owner")
    void deleteOpenCandidate(String owner);

    @Query("SELECT * FROM owner_sequences WHERE owner = :owner")
    OwnerSequenceEntity readSequence(String owner);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertSequence(OwnerSequenceEntity sequence);

    @Insert
    void insertChanges(List<CommittedChangeEntity> changes);

    @Query("SELECT * FROM committed_changes WHERE owner = :owner AND sequence > :afterSequence "
            + "ORDER BY sequence LIMIT :limit")
    List<CommittedChangeEntity> readChanges(String owner, long afterSequence, int limit);
}
