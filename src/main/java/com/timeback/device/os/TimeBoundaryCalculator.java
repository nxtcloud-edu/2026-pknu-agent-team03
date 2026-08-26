package com.timeback.device.os;

import com.timeback.device.contract.TimeRange;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public final class TimeBoundaryCalculator {
    private TimeBoundaryCalculator() {}

    public static List<Long> localMidnightBoundaries(TimeRange range, ZoneId zoneId) {
        LocalDate nextDate = Instant.ofEpochMilli(range.startAtMillis())
                .atZone(zoneId)
                .toLocalDate()
                .plusDays(1);
        List<Long> boundaries = new ArrayList<>();
        while (true) {
            long boundary = nextDate.atStartOfDay(zoneId).toInstant().toEpochMilli();
            if (boundary >= range.endAtMillis()) {
                break;
            }
            if (boundary > range.startAtMillis()) {
                boundaries.add(boundary);
            }
            nextDate = nextDate.plusDays(1);
        }
        return List.copyOf(boundaries);
    }
}
