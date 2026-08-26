package com.timeback.device.os

import com.timeback.device.contract.TimeRange
import com.timeback.device.contract.TimeSource
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class SystemTimeSource(
    private val clock: Clock = Clock.systemUTC(),
    private val systemZone: () -> ZoneId = { ZoneId.systemDefault() },
) : TimeSource {
    override fun nowMillis(): Long = clock.millis()

    override fun zoneId(): ZoneId = systemZone()

    override fun localMidnightBoundaries(range: TimeRange): List<Long> =
        TimeBoundaryCalculator.localMidnightBoundaries(range, zoneId())
}

object TimeBoundaryCalculator {
    fun localMidnightBoundaries(range: TimeRange, zoneId: ZoneId): List<Long> {
        var nextDate = Instant.ofEpochMilli(range.startAtMillis)
            .atZone(zoneId)
            .toLocalDate()
            .plusDays(1)
        val boundaries = mutableListOf<Long>()
        while (true) {
            val boundary = nextDate.atStartOfDay(zoneId).toInstant().toEpochMilli()
            if (boundary >= range.endAtMillis) break
            if (boundary > range.startAtMillis) boundaries += boundary
            nextDate = nextDate.plusDays(1)
        }
        return boundaries
    }
}
