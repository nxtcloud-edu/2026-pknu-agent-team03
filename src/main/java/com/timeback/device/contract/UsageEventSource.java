package com.timeback.device.contract;

import java.util.List;

public interface UsageEventSource {
    List<ObservedUsageEvent> queryEvents(TimeRange range);
}
