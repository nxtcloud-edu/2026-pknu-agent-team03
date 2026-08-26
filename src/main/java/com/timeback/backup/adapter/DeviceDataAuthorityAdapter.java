package com.timeback.backup.adapter;

import com.timeback.backup.contracts.CommittedChange;
import com.timeback.backup.contracts.EntityOperation;
import com.timeback.backup.contracts.EntityType;
import com.timeback.backup.port.BackupDataAuthority;
import com.timeback.device.contract.ChangeCursor;
import com.timeback.device.contract.CommitResult;
import com.timeback.device.contract.CommittedChangePage;
import com.timeback.device.contract.DataOwnerScope;
import com.timeback.device.contract.DeviceDataAuthority;
import com.timeback.device.contract.DeviceEntityType;

import java.time.Clock;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

/** Maps the device-owned CT-03 contract to the APP-12/APP-13 backup ports. */
public final class DeviceDataAuthorityAdapter implements BackupDataAuthority {
    private final DeviceDataAuthority authority;
    private final DataOwnerScope owner;
    private final Clock clock;
    private ChangeCursor cursor = new ChangeCursor();

    public DeviceDataAuthorityAdapter(DeviceDataAuthority authority, DataOwnerScope owner) {
        this(authority, owner, Clock.systemUTC());
    }

    public DeviceDataAuthorityAdapter(DeviceDataAuthority authority, DataOwnerScope owner, Clock clock) {
        this.authority = Objects.requireNonNull(authority, "authority");
        this.owner = Objects.requireNonNull(owner, "owner");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    @Override
    public synchronized List<CommittedChange> readCommittedChanges() {
        CommittedChangePage page = authority.readCommittedChanges(owner, cursor);
        cursor = page.nextCursor();
        return page.changes().stream().map(DeviceDataAuthorityAdapter::map).toList();
    }

    @Override
    public synchronized boolean deleteAllForUser(String anonymousUserId) {
        if (!owner.value().equals(anonymousUserId)) {
            return false;
        }
        CommitResult result = authority.deleteScope(
                owner,
                EnumSet.allOf(DeviceEntityType.class),
                clock.millis()
        );
        return result instanceof CommitResult.Success;
    }

    public ChangeCursor cursor() {
        return cursor;
    }

    private static CommittedChange map(com.timeback.device.contract.CommittedChange source) {
        return new CommittedChange(
                source.sequence(),
                source.changeId(),
                source.owner().value(),
                EntityType.valueOf(source.entityType().name()),
                source.entityId(),
                EntityOperation.valueOf(source.operation().name()),
                source.occurredAtMillis()
        );
    }
}
