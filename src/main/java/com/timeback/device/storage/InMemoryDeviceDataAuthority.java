package com.timeback.device.storage;

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
import com.timeback.device.contract.StableIds;
import com.timeback.device.contract.TimeRange;
import com.timeback.device.contract.UsageEvent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class InMemoryDeviceDataAuthority implements DeviceDataAuthority {
    private final Map<
            DataOwnerScope,
            Map<DeviceEntityType, LinkedHashMap<String, DeviceRecord>>
            > genericRecords = new HashMap<>();
    private final Map<DataOwnerScope, LinkedHashMap<String, UsageEvent>> usageEvents = new HashMap<>();
    private final Map<DataOwnerScope, LinkedHashMap<String, AppSession>> sessions = new HashMap<>();
    private final Map<DataOwnerScope, CollectionCheckpoint> checkpoints = new HashMap<>();
    private final Map<DataOwnerScope, OpenSessionCandidate> openCandidates = new HashMap<>();
    private final Map<DataOwnerScope, List<CommittedChange>> changes = new HashMap<>();
    private final Map<DataOwnerScope, Long> nextSequences = new HashMap<>();
    private String failNextReason;

    public synchronized void failNextCommit() {
        failNextCommit("injected storage failure");
    }

    public synchronized void failNextCommit(String reason) {
        failNextReason = reason;
    }

    @Override
    public synchronized CommitResult saveRecords(
            DataOwnerScope owner,
            List<DeviceRecord> records,
            long occurredAtMillis
    ) {
        String failure = consumeFailure();
        if (failure != null) {
            return new CommitResult.Failure(failure);
        }
        failure = validateGenericRecords(owner, records, null);
        if (failure != null) {
            return new CommitResult.Failure(failure);
        }
        Set<String> uniqueIds = new HashSet<>();
        for (DeviceRecord record : records) {
            if (!uniqueIds.add(record.entityType() + "|" + record.recordId())) {
                return new CommitResult.Failure("duplicate record id in one save request");
            }
        }

        List<CommittedChange> committed = new ArrayList<>();
        for (DeviceRecord record : records) {
            Map<String, DeviceRecord> entityRecords = recordsFor(owner, record.entityType());
            DeviceRecord old = entityRecords.get(record.recordId());
            ChangeOperation operation = old == null
                    ? ChangeOperation.CREATE
                    : old.equals(record) ? null : ChangeOperation.UPDATE;
            if (operation != null) {
                committed.add(newChange(
                        owner,
                        record.entityType(),
                        record.recordId(),
                        operation,
                        occurredAtMillis
                ));
            }
        }
        for (DeviceRecord record : records) {
            recordsFor(owner, record.entityType()).put(record.recordId(), record);
        }
        appendChanges(owner, committed);
        return new CommitResult.Success(
                committed,
                currentCursor(owner),
                countCreates(committed)
        );
    }

    @Override
    public synchronized DeviceRecord readRecord(
            DataOwnerScope owner,
            DeviceEntityType entityType,
            String recordId
    ) {
        Map<DeviceEntityType, LinkedHashMap<String, DeviceRecord>> byType = genericRecords.get(owner);
        return byType == null || byType.get(entityType) == null
                ? null
                : byType.get(entityType).get(recordId);
    }

    @Override
    public synchronized List<DeviceRecord> readPeriodRecords(
            DataOwnerScope owner,
            DeviceEntityType entityType,
            TimeRange range
    ) {
        Map<DeviceEntityType, LinkedHashMap<String, DeviceRecord>> byType = genericRecords.get(owner);
        if (byType == null || byType.get(entityType) == null) {
            return List.of();
        }
        return byType.get(entityType).values().stream()
                .filter(record -> record.range() != null && record.range().overlaps(range))
                .sorted(Comparator
                        .comparingLong((DeviceRecord record) -> record.range().startAtMillis())
                        .thenComparingLong(record -> record.range().endAtMillis())
                        .thenComparing(DeviceRecord::recordId))
                .toList();
    }

    @Override
    public synchronized CommitResult replacePeriodRecords(
            DataOwnerScope owner,
            DeviceEntityType entityType,
            TimeRange impactRange,
            List<DeviceRecord> records,
            long occurredAtMillis
    ) {
        String failure = consumeFailure();
        if (failure != null) {
            return new CommitResult.Failure(failure);
        }
        if (entityType == DeviceEntityType.USAGE_EVENT) {
            return new CommitResult.Failure("raw usage events cannot be replaced by period");
        }
        failure = validateGenericRecords(owner, records, entityType);
        if (failure != null) {
            return new CommitResult.Failure(failure);
        }
        if (records.stream().anyMatch(record -> record.range() == null
                || !record.range().overlaps(impactRange))) {
            return new CommitResult.Failure("replacement record is outside the impact range");
        }
        Set<String> ids = new HashSet<>();
        for (DeviceRecord record : records) {
            if (!ids.add(record.recordId())) {
                return new CommitResult.Failure("duplicate record id in replacement");
            }
        }

        LinkedHashMap<String, DeviceRecord> entityRecords = recordsFor(owner, entityType);
        Map<String, DeviceRecord> previous = new LinkedHashMap<>();
        entityRecords.values().stream()
                .filter(record -> record.range() != null && record.range().overlaps(impactRange))
                .forEach(record -> previous.put(record.recordId(), record));
        Map<String, DeviceRecord> replacement = new LinkedHashMap<>();
        records.forEach(record -> replacement.put(record.recordId(), record));
        List<CommittedChange> committed = new ArrayList<>();

        previous.values().stream()
                .filter(old -> !replacement.containsKey(old.recordId()))
                .forEach(old -> committed.add(newChange(
                        owner,
                        entityType,
                        old.recordId(),
                        ChangeOperation.DELETE,
                        occurredAtMillis
                )));
        for (DeviceRecord fresh : replacement.values()) {
            DeviceRecord old = previous.get(fresh.recordId());
            ChangeOperation operation = old == null
                    ? ChangeOperation.CREATE
                    : old.equals(fresh) ? null : ChangeOperation.UPDATE;
            if (operation != null) {
                committed.add(newChange(
                        owner,
                        entityType,
                        fresh.recordId(),
                        operation,
                        occurredAtMillis
                ));
            }
        }

        previous.keySet().forEach(entityRecords::remove);
        replacement.values().forEach(record -> entityRecords.put(record.recordId(), record));
        appendChanges(owner, committed);
        return new CommitResult.Success(
                committed,
                currentCursor(owner),
                countCreates(committed)
        );
    }

    @Override
    public synchronized CollectionCheckpoint readCheckpoint(DataOwnerScope owner) {
        return checkpoints.get(owner);
    }

    @Override
    public synchronized CommitResult commitCollection(
            DataOwnerScope owner,
            List<UsageEvent> events,
            CollectionCheckpoint checkpoint
    ) {
        String failure = consumeFailure();
        if (failure != null) {
            return new CommitResult.Failure(failure);
        }
        if (!checkpoint.owner().equals(owner)
                || events.stream().anyMatch(event -> !event.owner().equals(owner))) {
            return new CommitResult.Failure("owner scope violation");
        }

        LinkedHashMap<String, UsageEvent> records = usageEvents.computeIfAbsent(
                owner,
                ignored -> new LinkedHashMap<>()
        );
        List<UsageEvent> newEvents = events.stream()
                .filter(event -> !records.containsKey(event.eventId()))
                .toList();
        List<CommittedChange> committed = new ArrayList<>();
        for (UsageEvent event : newEvents) {
            committed.add(newChange(
                    owner,
                    DeviceEntityType.USAGE_EVENT,
                    event.eventId(),
                    ChangeOperation.CREATE,
                    event.collectedAtMillis()
            ));
        }

        newEvents.forEach(event -> records.put(event.eventId(), event));
        checkpoints.put(owner, checkpoint);
        appendChanges(owner, committed);
        return new CommitResult.Success(committed, currentCursor(owner), newEvents.size());
    }

    @Override
    public synchronized List<UsageEvent> readUsageEvents(
            DataOwnerScope owner,
            TimeRange range
    ) {
        return usageEvents.getOrDefault(owner, new LinkedHashMap<>()).values().stream()
                .filter(event -> range.contains(event.occurredAtMillis()))
                .sorted(Comparator
                        .comparingLong(UsageEvent::occurredAtMillis)
                        .thenComparingInt(UsageEvent::sourceOrder)
                        .thenComparing(UsageEvent::eventId))
                .toList();
    }

    @Override
    public synchronized OpenSessionCandidate readOpenSessionCandidate(DataOwnerScope owner) {
        return openCandidates.get(owner);
    }

    @Override
    public synchronized CommitResult replaceSessions(
            DataOwnerScope owner,
            TimeRange impactRange,
            List<AppSession> sessions,
            OpenSessionCandidate openCandidate,
            long occurredAtMillis
    ) {
        String failure = consumeFailure();
        if (failure != null) {
            return new CommitResult.Failure(failure);
        }
        if (sessions.stream().anyMatch(session -> !session.owner().equals(owner))
                || openCandidate != null && !openCandidate.owner().equals(owner)) {
            return new CommitResult.Failure("owner scope violation");
        }
        if (sessions.stream().anyMatch(session -> !session.range().overlaps(impactRange))) {
            return new CommitResult.Failure("replacement session is outside the impact range");
        }

        LinkedHashMap<String, AppSession> records = this.sessions.computeIfAbsent(
                owner,
                ignored -> new LinkedHashMap<>()
        );
        Map<String, AppSession> previous = new LinkedHashMap<>();
        records.values().stream()
                .filter(session -> session.range().overlaps(impactRange))
                .forEach(session -> previous.put(session.sessionId(), session));
        Map<String, AppSession> replacement = new LinkedHashMap<>();
        sessions.forEach(session -> replacement.put(session.sessionId(), session));
        List<CommittedChange> committed = new ArrayList<>();

        previous.values().stream()
                .filter(old -> !replacement.containsKey(old.sessionId()))
                .forEach(old -> committed.add(newChange(
                        owner,
                        DeviceEntityType.APP_SESSION,
                        old.sessionId(),
                        ChangeOperation.DELETE,
                        occurredAtMillis
                )));
        for (AppSession fresh : replacement.values()) {
            AppSession old = previous.get(fresh.sessionId());
            ChangeOperation operation = old == null
                    ? ChangeOperation.CREATE
                    : old.equals(fresh) ? null : ChangeOperation.UPDATE;
            if (operation != null) {
                committed.add(newChange(
                        owner,
                        DeviceEntityType.APP_SESSION,
                        fresh.sessionId(),
                        operation,
                        occurredAtMillis
                ));
            }
        }

        previous.keySet().forEach(records::remove);
        replacement.values().forEach(session -> records.put(session.sessionId(), session));
        if (openCandidate == null) {
            openCandidates.remove(owner);
        } else {
            openCandidates.put(owner, openCandidate);
        }
        appendChanges(owner, committed);
        return new CommitResult.Success(
                committed,
                currentCursor(owner),
                countCreates(committed)
        );
    }

    @Override
    public synchronized List<AppSession> readSessions(DataOwnerScope owner, TimeRange range) {
        return sessions.getOrDefault(owner, new LinkedHashMap<>()).values().stream()
                .filter(session -> session.range().overlaps(range))
                .sorted(Comparator
                        .comparingLong((AppSession session) -> session.range().startAtMillis())
                        .thenComparingLong(session -> session.range().endAtMillis())
                        .thenComparing(AppSession::sessionId))
                .toList();
    }

    @Override
    public synchronized CommittedChangePage readCommittedChanges(
            DataOwnerScope owner,
            ChangeCursor after,
            int limit
    ) {
        if (limit <= 0) {
            throw new IllegalArgumentException("change page limit must be positive");
        }
        List<CommittedChange> page = changes.getOrDefault(owner, List.of()).stream()
                .filter(change -> change.sequence() > after.sequence())
                .limit(limit)
                .toList();
        ChangeCursor nextCursor = page.isEmpty()
                ? after
                : new ChangeCursor(page.get(page.size() - 1).sequence());
        return new CommittedChangePage(page, nextCursor);
    }

    @Override
    public synchronized CommitResult deleteScope(
            DataOwnerScope owner,
            Set<DeviceEntityType> entityTypes,
            long occurredAtMillis
    ) {
        String failure = consumeFailure();
        if (failure != null) {
            return new CommitResult.Failure(failure);
        }
        List<CommittedChange> committed = new ArrayList<>();

        if (entityTypes.contains(DeviceEntityType.USAGE_EVENT)) {
            Map<String, UsageEvent> removed = usageEvents.remove(owner);
            if (removed != null) {
                removed.values().forEach(event -> committed.add(newChange(
                        owner,
                        DeviceEntityType.USAGE_EVENT,
                        event.eventId(),
                        ChangeOperation.DELETE,
                        occurredAtMillis
                )));
            }
            checkpoints.remove(owner);
        }
        if (entityTypes.contains(DeviceEntityType.APP_SESSION)) {
            Map<String, AppSession> removed = sessions.remove(owner);
            if (removed != null) {
                removed.values().forEach(session -> committed.add(newChange(
                        owner,
                        DeviceEntityType.APP_SESSION,
                        session.sessionId(),
                        ChangeOperation.DELETE,
                        occurredAtMillis
                )));
            }
            openCandidates.remove(owner);
        }
        Map<DeviceEntityType, LinkedHashMap<String, DeviceRecord>> byType = genericRecords.get(owner);
        if (byType != null) {
            for (DeviceEntityType entityType : entityTypes) {
                Map<String, DeviceRecord> removed = byType.remove(entityType);
                if (removed != null) {
                    removed.values().forEach(record -> committed.add(newChange(
                            owner,
                            entityType,
                            record.recordId(),
                            ChangeOperation.DELETE,
                            occurredAtMillis
                    )));
                }
            }
            if (byType.isEmpty()) {
                genericRecords.remove(owner);
            }
        }

        appendChanges(owner, committed);
        return new CommitResult.Success(committed, currentCursor(owner));
    }

    private String consumeFailure() {
        String reason = failNextReason;
        failNextReason = null;
        return reason;
    }

    private CommittedChange newChange(
            DataOwnerScope owner,
            DeviceEntityType entityType,
            String entityId,
            ChangeOperation operation,
            long occurredAtMillis
    ) {
        long sequence = nextSequences.getOrDefault(owner, 0L) + 1;
        nextSequences.put(owner, sequence);
        return new CommittedChange(
                sequence,
                StableIds.change(owner, sequence, entityType, entityId, operation),
                owner,
                entityType,
                entityId,
                operation,
                occurredAtMillis
        );
    }

    private void appendChanges(DataOwnerScope owner, List<CommittedChange> committed) {
        if (!committed.isEmpty()) {
            changes.computeIfAbsent(owner, ignored -> new ArrayList<>()).addAll(committed);
        }
    }

    private ChangeCursor currentCursor(DataOwnerScope owner) {
        return new ChangeCursor(nextSequences.getOrDefault(owner, 0L));
    }

    private LinkedHashMap<String, DeviceRecord> recordsFor(
            DataOwnerScope owner,
            DeviceEntityType entityType
    ) {
        return genericRecords
                .computeIfAbsent(owner, ignored -> new HashMap<>())
                .computeIfAbsent(entityType, ignored -> new LinkedHashMap<>());
    }

    private String validateGenericRecords(
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

    private int countCreates(List<CommittedChange> committed) {
        return (int) committed.stream()
                .filter(change -> change.operation() == ChangeOperation.CREATE)
                .count();
    }
}
