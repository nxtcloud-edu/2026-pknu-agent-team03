package io.timeback.domain;

import com.timeback.device.contract.CommitResult;
import com.timeback.device.contract.DataOwnerScope;
import com.timeback.device.contract.DeviceDataAuthority;
import com.timeback.device.contract.DeviceEntityType;
import com.timeback.device.contract.DeviceRecord;
import com.timeback.device.contract.TimeRange;

import java.util.List;
import java.util.Objects;

/** Public APP-05~APP-09 persistence adapter over the device-owned APP-11 authority. */
public final class DomainRecordStore {
    private final DeviceDataAuthority authority;
    private final DataOwnerScope owner;

    public DomainRecordStore(DeviceDataAuthority authority, DataOwnerScope owner) {
        this.authority = Objects.requireNonNull(authority, "authority");
        this.owner = Objects.requireNonNull(owner, "owner");
    }

    public CommitResult save(DomainRecord record, long occurredAtMillis) {
        Objects.requireNonNull(record, "record");
        return authority.saveRecords(owner, List.of(toDeviceRecord(record)), occurredAtMillis);
    }

    public List<DomainRecord> readPeriod(
            DomainEntityType entityType,
            long startAtMillis,
            long endAtMillis
    ) {
        return authority.readPeriodRecords(
                owner,
                DeviceEntityType.valueOf(entityType.name()),
                new TimeRange(startAtMillis, endAtMillis)
        ).stream().map(DomainRecordStore::fromDeviceRecord).toList();
    }

    private DeviceRecord toDeviceRecord(DomainRecord record) {
        TimeRange range = record.startAtMillis() == null || record.endAtMillis() == null
                ? null
                : new TimeRange(record.startAtMillis(), record.endAtMillis());
        return new DeviceRecord(
                record.id(),
                owner,
                DeviceEntityType.valueOf(record.entityType().name()),
                range,
                record.payload()
        );
    }

    private static DomainRecord fromDeviceRecord(DeviceRecord record) {
        return new DomainRecord(
                record.recordId(),
                DomainEntityType.valueOf(record.entityType().name()),
                record.range() == null ? null : record.range().startAtMillis(),
                record.range() == null ? null : record.range().endAtMillis(),
                record.payload()
        );
    }

    public enum DomainEntityType {
        APP, ACTIVITY, CONTEXT, BASELINE, GOAL, RECOVERED_TIME
    }

    public record DomainRecord(
            String id,
            DomainEntityType entityType,
            Long startAtMillis,
            Long endAtMillis,
            String payload
    ) {
        public DomainRecord {
            if (id == null || id.isBlank()) {
                throw new IllegalArgumentException("record id must not be blank");
            }
            Objects.requireNonNull(entityType, "entityType");
            if ((startAtMillis == null) != (endAtMillis == null)) {
                throw new IllegalArgumentException("record range must provide both start and end");
            }
        }
    }
}
