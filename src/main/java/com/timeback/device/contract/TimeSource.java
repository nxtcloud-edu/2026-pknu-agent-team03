package com.timeback.device.contract;

import java.time.ZoneId;
import java.util.List;

public interface TimeSource {
    long nowMillis();

    ZoneId zoneId();

    List<Long> localMidnightBoundaries(TimeRange range);
}
