package com.timeback.device.room;

import com.timeback.device.contract.AppSession;
import com.timeback.device.contract.ChangeCursor;
import com.timeback.device.contract.ChangeOperation;
import com.timeback.device.contract.CollectionCheckpoint;
import com.timeback.device.contract.CommitResult;
import com.timeback.device.contract.CommittedChange;
import com.timeback.device.contract.CommittedChangePage;
import com.timeback.device.contract.DataOwnerScope;
import com.timeback.device.contract.DeviceDataAuthority;
import com.timeback.device.contract.DeviceEntityType;
import com.timeback.device.contract.DeviceRecord;
import com.timeback.device.contract.OpenSessionCandidate;
import com.timeback.device.contract.SessionCompletionCause;
import com.timeback.device.contract.StableIds;
import com.timeback.device.contract.TimeRange;
import com.timeback.device.contract.UsageEvent;
import com.timeback.device.contract.UsageEventKind;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;

/** Room/WAL APP-11 adapter with the same owner, atomicity, and change-cursor contract as memory storage. */
public final class RoomDeviceDataAuthority implements DeviceDataAuthority {
    private final TimeBackRoomDatabase database;
    private final DeviceDataDao dao;

    public RoomDeviceDataAuthority(TimeBackRoomDatabase database) {
        this.database = Objects.requireNonNull(database, "database");
        this.dao = database.deviceDataDao();
    }

    @Override
    public CommitResult saveRecords(
            DataOwnerScope owner,
            List<DeviceRecord> records,
            long occurredAtMillis
    ) {
        String validation = validateGenericRecords(owner, records, null);
        if (validation != null) {
            return new CommitResult.Failure(validation);
        }
        Set<String> uniqueIds = new HashSet<>();
        for (DeviceRecord record : records) {
            if (!uniqueIds.add(record.entityType() + "|" + record.recordId())) {
                return new CommitResult.Failure("duplicate record id in one save request");
            }
        }

        return commit(() -> {
            List<CommittedChange> changes = new ArrayList<>();
            for (DeviceRecord record : records) {
                GenericRecordEntity previous = dao.readRecord(
                        owner.value(), record.entityType().name(), record.recordId());
                ChangeOperation operation = previous == null
                        ? ChangeOperation.CREATE
                        : toRecord(previous).equals(record) ? null : ChangeOperation.UPDATE;
                if (operation != null) {
                    changes.add(newChange(
                            owner, record.entityType(), record.recordId(), operation, occurredAtMillis));
                }
            }
            dao.upsertRecords(records.stream().map(RoomDeviceDataAuthority::toEntity).toList());
            appendChanges(changes);
            return success(owner, changes);
        });
    }

    @Override
    public DeviceRecord readRecord(
            DataOwnerScope owner,
            DeviceEntityType entityType,
            String recordId
    ) {
        GenericRecordEntity entity = dao.readRecord(owner.value(), entityType.name(), recordId);
        return entity == null ? null : toRecord(entity);
    }

    @Override
    public List<DeviceRecord> readPeriodRecords(
            DataOwnerScope owner,
            DeviceEntityType entityType,
            TimeRange range
    ) {
        return dao.readPeriodRecords(
                        owner.value(), entityType.name(), range.startAtMillis(), range.endAtMillis())
                .stream()
                .map(RoomDeviceDataAuthority::toRecord)
                .toList();
    }

    @Override
    public CommitResult replacePeriodRecords(
            DataOwnerScope owner,
            DeviceEntityType entityType,
            TimeRange impactRange,
            List<DeviceRecord> records,
            long occurredAtMillis
    ) {
        if (entityType == DeviceEntityType.USAGE_EVENT) {
            return new CommitResult.Failure("raw usage events cannot be replaced by period");
        }
        String validation = validateGenericRecords(owner, records, entityType);
        if (validation != null) {
            return new CommitResult.Failure(validation);
        }
        if (records.stream().anyMatch(record -> record.range() == null
                || !record.range().overlaps(impactRange))) {
            return new CommitResult.Failure("replacement record is outside the impact range");
        }
        if (records.stream().map(DeviceRecord::recordId).distinct().count() != records.size()) {
            return new CommitResult.Failure("duplicate record id in replacement");
        }

        return commit(() -> {
            Map<String, DeviceRecord> previous = dao.readPeriodRecords(
                            owner.value(),
                            entityType.name(),
                            impactRange.startAtMillis(),
                            impactRange.endAtMillis())
                    .stream()
                    .map(RoomDeviceDataAuthority::toRecord)
                    .collect(java.util.stream.Collectors.toMap(
                            DeviceRecord::recordId,
                            record -> record,
                            (first, ignored) -> first,
                            LinkedHashMap::new));
            Map<String, DeviceRecord> replacement = new LinkedHashMap<>();
            records.forEach(record -> replacement.put(record.recordId(), record));
            List<CommittedChange> changes = new ArrayList<>();

            previous.values().stream()
                    .filter(old -> !replacement.containsKey(old.recordId()))
                    .forEach(old -> changes.add(newChange(
                            owner, entityType, old.recordId(), ChangeOperation.DELETE, occurredAtMillis)));
            for (DeviceRecord fresh : replacement.values()) {
                DeviceRecord old = previous.get(fresh.recordId());
                ChangeOperation operation = old == null
                        ? ChangeOperation.CREATE
                        : old.equals(fresh) ? null : ChangeOperation.UPDATE;
                if (operation != null) {
                    changes.add(newChange(
                            owner, entityType, fresh.recordId(), operation, occurredAtMillis));
                }
            }

            dao.deletePeriodRecords(
                    owner.value(), entityType.name(), impactRange.startAtMillis(), impactRange.endAtMillis());
            dao.upsertRecords(records.stream().map(RoomDeviceDataAuthority::toEntity).toList());
            appendChanges(changes);
            return success(owner, changes);
        });
    }

    @Override
    public CollectionCheckpoint readCheckpoint(DataOwnerScope owner) {
        CollectionCheckpointEntity entity = dao.readCheckpoint(owner.value());
        return entity == null
                ? null
                : new CollectionCheckpoint(owner, entity.successfulThroughMillis);
    }

    @Override
    public CommitResult commitCollection(
            DataOwnerScope owner,
            List<UsageEvent> events,
            CollectionCheckpoint checkpoint
    ) {
        if (!checkpoint.owner().equals(owner)
                || events.stream().anyMatch(event -> !event.owner().equals(owner))) {
            return new CommitResult.Failure("owner scope violation");
        }

        return commit(() -> {
            Set<String> existingIds = new HashSet<>(dao.readAllUsageEventIds(owner.value()));
            Map<String, UsageEvent> uniqueNewEvents = new LinkedHashMap<>();
            for (UsageEvent event : events) {
                if (!existingIds.contains(event.eventId())) {
                    uniqueNewEvents.putIfAbsent(event.eventId(), event);
                }
            }
            List<CommittedChange> changes = new ArrayList<>();
            for (UsageEvent event : uniqueNewEvents.values()) {
                changes.add(newChange(
                        owner,
                        DeviceEntityType.USAGE_EVENT,
                        event.eventId(),
                        ChangeOperation.CREATE,
                        event.collectedAtMillis()));
            }
            dao.insertUsageEvents(uniqueNewEvents.values().stream()
                    .map(RoomDeviceDataAuthority::toEntity)
                    .toList());
            dao.upsertCheckpoint(new CollectionCheckpointEntity(
                    owner.value(), checkpoint.successfulThroughMillis()));
            appendChanges(changes);
            return new CommitResult.Success(
                    changes,
                    currentCursor(owner),
                    uniqueNewEvents.size());
        });
    }

    @Override
    public List<UsageEvent> readUsageEvents(DataOwnerScope owner, TimeRange range) {
        return dao.readUsageEvents(owner.value(), range.startAtMillis(), range.endAtMillis())
                .stream()
                .map(RoomDeviceDataAuthority::toUsageEvent)
                .toList();
    }

    @Override
    public OpenSessionCandidate readOpenSessionCandidate(DataOwnerScope owner) {
        OpenSessionCandidateEntity entity = dao.readOpenCandidate(owner.value());
        return entity == null
                ? null
                : new OpenSessionCandidate(
                        owner, entity.packageName, entity.startedAtMillis, entity.sourceEventId);
    }

    @Override
    public CommitResult replaceSessions(
            DataOwnerScope owner,
            TimeRange impactRange,
            List<AppSession> sessions,
            OpenSessionCandidate openCandidate,
            long occurredAtMillis
    ) {
        if (sessions.stream().anyMatch(session -> !session.owner().equals(owner))
                || openCandidate != null && !openCandidate.owner().equals(owner)) {
            return new CommitResult.Failure("owner scope violation");
        }
        if (sessions.stream().anyMatch(session -> !session.range().overlaps(impactRange))) {
            return new CommitResult.Failure("replacement session is outside the impact range");
        }

        return commit(() -> {
            Map<String, AppSession> previous = dao.readSessions(
                            owner.value(), impactRange.startAtMillis(), impactRange.endAtMillis())
                    .stream()
                    .map(RoomDeviceDataAuthority::toSession)
                    .collect(java.util.stream.Collectors.toMap(
                            AppSession::sessionId,
                            session -> session,
                            (first, ignored) -> first,
                            LinkedHashMap::new));
            Map<String, AppSession> replacement = new LinkedHashMap<>();
            sessions.forEach(session -> replacement.put(session.sessionId(), session));
            List<CommittedChange> changes = new ArrayList<>();

            previous.values().stream()
                    .filter(old -> !replacement.containsKey(old.sessionId()))
                    .forEach(old -> changes.add(newChange(
                            owner,
                            DeviceEntityType.APP_SESSION,
                            old.sessionId(),
                            ChangeOperation.DELETE,
                            occurredAtMillis)));
            for (AppSession fresh : replacement.values()) {
                AppSession old = previous.get(fresh.sessionId());
                ChangeOperation operation = old == null
                        ? ChangeOperation.CREATE
                        : old.equals(fresh) ? null : ChangeOperation.UPDATE;
                if (operation != null) {
                    changes.add(newChange(
                            owner,
                            DeviceEntityType.APP_SESSION,
                            fresh.sessionId(),
                            operation,
                            occurredAtMillis));
                }
            }

            dao.deletePeriodSessions(
                    owner.value(), impactRange.startAtMillis(), impactRange.endAtMillis());
            dao.upsertSessions(sessions.stream().map(RoomDeviceDataAuthority::toEntity).toList());
            dao.deleteOpenCandidate(owner.value());
            if (openCandidate != null) {
                dao.upsertOpenCandidate(new OpenSessionCandidateEntity(
                        owner.value(),
                        openCandidate.packageName(),
                        openCandidate.startedAtMillis(),
                        openCandidate.sourceEventId()));
            }
            appendChanges(changes);
            return success(owner, changes);
        });
    }

    @Override
    public List<AppSession> readSessions(DataOwnerScope owner, TimeRange range) {
        return dao.readSessions(owner.value(), range.startAtMillis(), range.endAtMillis())
                .stream()
                .map(RoomDeviceDataAuthority::toSession)
                .toList();
    }

    @Override
    public CommittedChangePage readCommittedChanges(
            DataOwnerScope owner,
            ChangeCursor after,
            int limit
    ) {
        if (limit <= 0) {
            throw new IllegalArgumentException("change page limit must be positive");
        }
        List<CommittedChange> changes = dao.readChanges(owner.value(), after.sequence(), limit)
                .stream()
                .map(RoomDeviceDataAuthority::toChange)
                .toList();
        ChangeCursor next = changes.isEmpty()
                ? after
                : new ChangeCursor(changes.get(changes.size() - 1).sequence());
        return new CommittedChangePage(changes, next);
    }

    @Override
    public CommitResult deleteScope(
            DataOwnerScope owner,
            Set<DeviceEntityType> entityTypes,
            long occurredAtMillis
    ) {
        return commit(() -> {
            List<CommittedChange> changes = new ArrayList<>();
            if (entityTypes.contains(DeviceEntityType.USAGE_EVENT)) {
                dao.readAllUsageEvents(owner.value()).stream()
                        .sorted(Comparator.comparing(event -> event.eventId))
                        .forEach(event -> changes.add(newChange(
                                owner,
                                DeviceEntityType.USAGE_EVENT,
                                event.eventId,
                                ChangeOperation.DELETE,
                                occurredAtMillis)));
                dao.deleteUsageEvents(owner.value());
                dao.deleteCheckpoint(owner.value());
            }
            if (entityTypes.contains(DeviceEntityType.APP_SESSION)) {
                dao.readAllSessions(owner.value()).stream()
                        .sorted(Comparator.comparing(session -> session.sessionId))
                        .forEach(session -> changes.add(newChange(
                                owner,
                                DeviceEntityType.APP_SESSION,
                                session.sessionId,
                                ChangeOperation.DELETE,
                                occurredAtMillis)));
                dao.deleteSessions(owner.value());
                dao.deleteOpenCandidate(owner.value());
            }
            for (DeviceEntityType entityType : entityTypes) {
                dao.readAllRecords(owner.value(), entityType.name()).stream()
                        .sorted(Comparator.comparing(record -> record.recordId))
                        .forEach(record -> changes.add(newChange(
                                owner,
                                entityType,
                                record.recordId,
                                ChangeOperation.DELETE,
                                occurredAtMillis)));
                dao.deleteAllRecords(owner.value(), entityType.name());
            }
            appendChanges(changes);
            return success(owner, changes);
        });
    }

    private CommitResult commit(Callable<CommitResult> work) {
        try {
            return database.runInTransaction(work);
        } catch (Exception error) {
            return new CommitResult.Failure("room transaction failed");
        }
    }

    private CommittedChange newChange(
            DataOwnerScope owner,
            DeviceEntityType entityType,
            String entityId,
            ChangeOperation operation,
            long occurredAtMillis
    ) {
        OwnerSequenceEntity current = dao.readSequence(owner.value());
        long sequence = current == null ? 1L : current.currentSequence + 1L;
        dao.upsertSequence(new OwnerSequenceEntity(owner.value(), sequence));
        return new CommittedChange(
                sequence,
                StableIds.change(owner, sequence, entityType, entityId, operation),
                owner,
                entityType,
                entityId,
                operation,
                occurredAtMillis);
    }

    private void appendChanges(List<CommittedChange> changes) {
        if (!changes.isEmpty()) {
            dao.insertChanges(changes.stream().map(RoomDeviceDataAuthority::toEntity).toList());
        }
    }

    private CommitResult.Success success(DataOwnerScope owner, List<CommittedChange> changes) {
        ChangeCursor cursor = currentCursor(owner);
        int createdCount = (int) changes.stream()
                .filter(change -> change.operation() == ChangeOperation.CREATE)
                .count();
        return new CommitResult.Success(changes, cursor, createdCount);
    }

    private ChangeCursor currentCursor(DataOwnerScope owner) {
        OwnerSequenceEntity sequence = dao.readSequence(owner.value());
        return new ChangeCursor(sequence == null ? 0L : sequence.currentSequence);
    }

    private static String validateGenericRecords(
            DataOwnerScope owner,
            List<DeviceRecord> records,
            DeviceEntityType requiredType
    ) {
        if (records.stream().anyMatch(record -> !record.owner().equals(owner))) {
            return "owner scope violation";
        }
        if (requiredType != null
                && records.stream().anyMatch(record -> record.entityType() != requiredType)) {
            return "record entity type does not match replacement type";
        }
        return null;
    }

    private static GenericRecordEntity toEntity(DeviceRecord record) {
        return new GenericRecordEntity(
                record.owner().value(),
                record.entityType().name(),
                record.recordId(),
                record.range() == null ? null : record.range().startAtMillis(),
                record.range() == null ? null : record.range().endAtMillis(),
                record.payload());
    }

    private static DeviceRecord toRecord(GenericRecordEntity entity) {
        TimeRange range = entity.startAtMillis == null || entity.endAtMillis == null
                ? null
                : new TimeRange(entity.startAtMillis, entity.endAtMillis);
        return new DeviceRecord(
                entity.recordId,
                new DataOwnerScope(entity.owner),
                DeviceEntityType.valueOf(entity.entityType),
                range,
                entity.payload);
    }

    private static UsageEventEntity toEntity(UsageEvent event) {
        return new UsageEventEntity(
                event.owner().value(),
                event.eventId(),
                event.packageName(),
                event.kind().name(),
                event.occurredAtMillis(),
                event.collectedAtMillis(),
                event.sourceOrder());
    }

    private static UsageEvent toUsageEvent(UsageEventEntity entity) {
        return new UsageEvent(
                entity.eventId,
                new DataOwnerScope(entity.owner),
                entity.packageName,
                UsageEventKind.valueOf(entity.kind),
                entity.occurredAtMillis,
                entity.collectedAtMillis,
                entity.sourceOrder);
    }

    private static AppSessionEntity toEntity(AppSession session) {
        return new AppSessionEntity(
                session.owner().value(),
                session.sessionId(),
                session.logicalSessionId(),
                session.packageName(),
                session.range().startAtMillis(),
                session.range().endAtMillis(),
                session.completionCause().name(),
                encodeIds(session.sourceEventIds()));
    }

    private static AppSession toSession(AppSessionEntity entity) {
        return new AppSession(
                entity.sessionId,
                entity.logicalSessionId,
                new DataOwnerScope(entity.owner),
                entity.packageName,
                new TimeRange(entity.startAtMillis, entity.endAtMillis),
                SessionCompletionCause.valueOf(entity.completionCause),
                decodeIds(entity.sourceEventIds));
    }

    private static CommittedChangeEntity toEntity(CommittedChange change) {
        return new CommittedChangeEntity(
                change.owner().value(),
                change.sequence(),
                change.changeId(),
                change.entityType().name(),
                change.entityId(),
                change.operation().name(),
                change.occurredAtMillis());
    }

    private static CommittedChange toChange(CommittedChangeEntity entity) {
        return new CommittedChange(
                entity.sequence,
                entity.changeId,
                new DataOwnerScope(entity.owner),
                DeviceEntityType.valueOf(entity.entityType),
                entity.entityId,
                ChangeOperation.valueOf(entity.operation),
                entity.occurredAtMillis);
    }

    private static String encodeIds(List<String> ids) {
        Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
        return ids.stream()
                .map(id -> encoder.encodeToString(id.getBytes(StandardCharsets.UTF_8)))
                .collect(java.util.stream.Collectors.joining(","));
    }

    private static List<String> decodeIds(String encoded) {
        if (encoded == null || encoded.isEmpty()) {
            return List.of();
        }
        Base64.Decoder decoder = Base64.getUrlDecoder();
        return List.of(encoded.split(",")).stream()
                .map(value -> new String(decoder.decode(value), StandardCharsets.UTF_8))
                .toList();
    }
}
