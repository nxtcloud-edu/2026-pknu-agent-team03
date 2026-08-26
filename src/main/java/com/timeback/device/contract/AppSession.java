package com.timeback.device.contract;

import java.util.List;

public record AppSession(
        String sessionId,
        String logicalSessionId,
        DataOwnerScope owner,
        String packageName,
        TimeRange range,
        SessionCompletionCause completionCause,
        List<String> sourceEventIds
) {
    public AppSession {
        sourceEventIds = List.copyOf(sourceEventIds);
    }

    public long durationMillis() {
        return range.durationMillis();
    }
}
