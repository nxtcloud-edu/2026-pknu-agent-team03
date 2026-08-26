package com.timeback.device.fake;

import com.timeback.device.contract.TimeRange;
import com.timeback.device.contract.TimeSource;
import com.timeback.device.os.TimeBoundaryCalculator;

import java.time.ZoneId;
import java.util.List;

public final class ControlledTimeSource implements TimeSource {
    private long currentTimeMillis;
    private ZoneId currentZoneId;

    public ControlledTimeSource(long currentTimeMillis) {
        this(currentTimeMillis, ZoneId.of("UTC"));
    }

    public ControlledTimeSource(long currentTimeMillis, ZoneId currentZoneId) {
        this.currentTimeMillis = currentTimeMillis;
        this.currentZoneId = currentZoneId;
    }

    @Override
    public long nowMillis() {
        return currentTimeMillis;
    }

    @Override
    public ZoneId zoneId() {
        return currentZoneId;
    }

    @Override
    public List<Long> localMidnightBoundaries(TimeRange range) {
        return TimeBoundaryCalculator.localMidnightBoundaries(range, currentZoneId);
    }

    public void advanceTo(long timestampMillis) {
        if (timestampMillis < currentTimeMillis) {
            throw new IllegalArgumentException("controlled time cannot move backwards");
        }
        currentTimeMillis = timestampMillis;
    }

    public void changeZone(ZoneId zoneId) {
        currentZoneId = zoneId;
    }
}
