package com.timeback.ui.fake;

import com.timeback.ui.domain.model.RecoveryEntry;

public class RecoveryViewData {
    private final RecoveryEntry entry;

    public RecoveryViewData(RecoveryEntry entry) {
        this.entry = entry;
    }

    public RecoveryEntry getEntry() { return entry; }
}
