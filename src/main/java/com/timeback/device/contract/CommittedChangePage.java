package com.timeback.device.contract;

import java.util.List;

public record CommittedChangePage(List<CommittedChange> changes, ChangeCursor nextCursor) {
    public CommittedChangePage {
        changes = List.copyOf(changes);
    }
}
