package com.timeback.device.os;

import com.timeback.device.contract.TimeRange;
import com.timeback.device.contract.TimeSource;

import java.time.Clock;
import java.time.ZoneId;
import java.util.List;
import java.util.function.Supplier;

public final class SystemTimeSource implements TimeSource {
    private final Clock clock;
    private final Supplier<ZoneId> systemZone;

    public SystemTimeSource() {
        this(Clock.systemUTC(), ZoneId::systemDefault);
    }

    public SystemTimeSource(Clock clock, Supplier<ZoneId> systemZone) {
        this.clock = clock;
        this.systemZone = systemZone;
    }

    @Override
    public long nowMillis() {
        return clock.millis();
    }

    @Override
    public ZoneId zoneId() {
        return systemZone.get();
    }

    @Override
    public List<Long> localMidnightBoundaries(TimeRange range) {
        return TimeBoundaryCalculator.localMidnightBoundaries(range, zoneId());
    }
}
