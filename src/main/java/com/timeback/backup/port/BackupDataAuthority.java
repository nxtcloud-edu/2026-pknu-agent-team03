package com.timeback.backup.port;

import com.timeback.backup.contracts.CommittedChange;

import java.util.List;

/** APP-12/APP-13 view of the device-owned CT-03 authority. */
public interface BackupDataAuthority {
    List<CommittedChange> readCommittedChanges();

    boolean deleteAllForUser(String anonymousUserId);
}
