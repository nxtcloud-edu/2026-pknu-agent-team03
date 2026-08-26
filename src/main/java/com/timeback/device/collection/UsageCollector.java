package com.timeback.device.collection;

import com.timeback.device.access.AccessGate;
import com.timeback.device.contract.AccessState;
import com.timeback.device.contract.CollectionCheckpoint;
import com.timeback.device.contract.CollectionResult;
import com.timeback.device.contract.CommitResult;
import com.timeback.device.contract.DataOwnerScope;
import com.timeback.device.contract.DeviceDataAuthority;
import com.timeback.device.contract.ObservedUsageEvent;
import com.timeback.device.contract.StableIds;
import com.timeback.device.contract.TimeRange;
import com.timeback.device.contract.TimeSource;
import com.timeback.device.contract.UsageEvent;
import com.timeback.device.contract.UsageEventSource;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class UsageCollector {
    private final AccessGate accessGate;
    private final UsageEventSource source;
    private final DeviceDataAuthority authority;
    private final TimeSource timeSource;
    private final long overlapMillis;

    public UsageCollector(
            AccessGate accessGate,
            UsageEventSource source,
            DeviceDataAuthority authority,
            TimeSource timeSource
    ) {
        this(accessGate, source, authority, timeSource, 1_000);
    }

    public UsageCollector(
            AccessGate accessGate,
            UsageEventSource source,
            DeviceDataAuthority authority,
            TimeSource timeSource,
            long overlapMillis
    ) {
        if (overlapMillis < 0) {
            throw new IllegalArgumentException("overlap must not be negative");
        }
        this.accessGate = accessGate;
        this.source = source;
        this.authority = authority;
        this.timeSource = timeSource;
        this.overlapMillis = overlapMillis;
    }

    public CollectionResult collect(DataOwnerScope owner, TimeRange requestedRange) {
        AccessState access = accessGate.readAccessState();
        if (access instanceof AccessState.Blocked) {
            return CollectionResult.PermissionRequired.INSTANCE;
        }
        if (access instanceof AccessState.Failure failure) {
            return new CollectionResult.Failure(failure.reason());
        }

        long now = timeSource.nowMillis();
        long cappedEnd = Math.min(requestedRange.endAtMillis(), now);
        if (cappedEnd <= requestedRange.startAtMillis()) {
            return new CollectionResult.Failure(
                    "requested range does not include a collectible instant"
            );
        }

        CollectionCheckpoint checkpoint = authority.readCheckpoint(owner);
        long effectiveStart = requestedRange.startAtMillis();
        if (checkpoint != null
                && checkpoint.successfulThroughMillis() > requestedRange.startAtMillis()
                && checkpoint.successfulThroughMillis() <= cappedEnd) {
            effectiveStart = Math.max(
                    requestedRange.startAtMillis(),
                    checkpoint.successfulThroughMillis() - overlapMillis
            );
        }
        TimeRange effectiveRange = new TimeRange(effectiveStart, cappedEnd);

        List<ObservedUsageEvent> observed;
        try {
            observed = source.queryEvents(effectiveRange);
        } catch (RuntimeException error) {
            return new CollectionResult.RetryableFailure(
                    error.getMessage() == null ? "usage event source unavailable" : error.getMessage()
            );
        }

        List<ObservedUsageEvent> ordered = observed.stream()
                .filter(event -> effectiveRange.contains(event.occurredAtMillis()))
                .sorted(Comparator
                        .comparingLong(ObservedUsageEvent::occurredAtMillis)
                        .thenComparingInt(ObservedUsageEvent::occurrenceInTimestampGroup)
                        .thenComparing(ObservedUsageEvent::packageName)
                        .thenComparing(event -> event.kind().name()))
                .toList();
        long collectedAt = timeSource.nowMillis();
        List<UsageEvent> records = new ArrayList<>();
        for (int index = 0; index < ordered.size(); index++) {
            ObservedUsageEvent event = ordered.get(index);
            records.add(new UsageEvent(
                    StableIds.usageEvent(owner, event),
                    owner,
                    event.packageName(),
                    event.kind(),
                    event.occurredAtMillis(),
                    collectedAt,
                    index
            ));
        }

        CollectionCheckpoint nextCheckpoint = new CollectionCheckpoint(owner, cappedEnd);
        CommitResult committed = authority.commitCollection(owner, records, nextCheckpoint);
        if (committed instanceof CommitResult.Failure failure) {
            return new CollectionResult.RetryableFailure(failure.reason());
        }
        CommitResult.Success success = (CommitResult.Success) committed;
        return new CollectionResult.Success(
                effectiveRange,
                records.size(),
                success.createdCount(),
                nextCheckpoint
        );
    }
}
