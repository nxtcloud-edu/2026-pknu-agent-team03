package com.timeback.device.contract;

import java.util.List;
import java.util.Set;

public interface DeviceDataAuthority {
    CommitResult saveRecords(DataOwnerScope owner, List<DeviceRecord> records, long occurredAtMillis);

    DeviceRecord readRecord(DataOwnerScope owner, DeviceEntityType entityType, String recordId);

    List<DeviceRecord> readPeriodRecords(
            DataOwnerScope owner,
            DeviceEntityType entityType,
            TimeRange range
    );

    CommitResult replacePeriodRecords(
            DataOwnerScope owner,
            DeviceEntityType entityType,
            TimeRange impactRange,
            List<DeviceRecord> records,
            long occurredAtMillis
    );

    CollectionCheckpoint readCheckpoint(DataOwnerScope owner);

    CommitResult commitCollection(
            DataOwnerScope owner,
            List<UsageEvent> events,
            CollectionCheckpoint checkpoint
    );

    List<UsageEvent> readUsageEvents(DataOwnerScope owner, TimeRange range);

    OpenSessionCandidate readOpenSessionCandidate(DataOwnerScope owner);

    CommitResult replaceSessions(
            DataOwnerScope owner,
            TimeRange impactRange,
            List<AppSession> sessions,
            OpenSessionCandidate openCandidate,
            long occurredAtMillis
    );

    List<AppSession> readSessions(DataOwnerScope owner, TimeRange range);

    default CommittedChangePage readCommittedChanges(DataOwnerScope owner, ChangeCursor after) {
        return readCommittedChanges(owner, after, 100);
    }

    CommittedChangePage readCommittedChanges(DataOwnerScope owner, ChangeCursor after, int limit);

    CommitResult deleteScope(
            DataOwnerScope owner,
            Set<DeviceEntityType> entityTypes,
            long occurredAtMillis
    );
}
