package com.timeback.device.fake

import com.timeback.device.contract.ObservedUsageEvent
import com.timeback.device.contract.ScreenEndEvent
import com.timeback.device.contract.ScreenStateSource
import com.timeback.device.contract.TimeRange
import com.timeback.device.contract.TimeSource
import com.timeback.device.contract.UsageAccessGateway
import com.timeback.device.contract.UsageAccessStatus
import com.timeback.device.contract.UsageEventSource
import com.timeback.device.os.TimeBoundaryCalculator
import java.time.ZoneId

class FakeUsageAccessGateway(
    var status: UsageAccessStatus = UsageAccessStatus.NOT_GRANTED,
) : UsageAccessGateway {
    var readCount: Int = 0
        private set
    var settingsOpenCount: Int = 0
        private set
    var failReadsWith: RuntimeException? = null
    var settingsCanOpen: Boolean = true

    override fun readCurrentStatus(): UsageAccessStatus {
        readCount += 1
        failReadsWith?.let { throw it }
        return status
    }

    override fun openUsageAccessSettings(): Boolean {
        settingsOpenCount += 1
        return settingsCanOpen
    }
}
class FakeUsageEventSource(
    var events: List<ObservedUsageEvent> = emptyList(),
) : UsageEventSource {
    var queryCount: Int = 0
        private set
    val requestedRanges: MutableList<TimeRange> = mutableListOf()
    var failWith: RuntimeException? = null

    override fun queryEvents(range: TimeRange): List<ObservedUsageEvent> {
        queryCount += 1
        requestedRanges += range
        failWith?.let { throw it }
        return events.filter { range.contains(it.occurredAtMillis) }
    }
}

class FakeScreenStateSource(
    var events: List<ScreenEndEvent> = emptyList(),
) : ScreenStateSource {
    var queryCount: Int = 0
        private set
    var failWith: RuntimeException? = null

    override fun queryScreenEndEvents(range: TimeRange): List<ScreenEndEvent> {
        queryCount += 1
        failWith?.let { throw it }
        return events.filter { range.contains(it.occurredAtMillis) }
    }
}

class ControlledTimeSource(
    private var currentTimeMillis: Long,
    private var currentZoneId: ZoneId = ZoneId.of("UTC"),
) : TimeSource {
    override fun nowMillis(): Long = currentTimeMillis

    override fun zoneId(): ZoneId = currentZoneId

    override fun localMidnightBoundaries(range: TimeRange): List<Long> =
        TimeBoundaryCalculator.localMidnightBoundaries(range, currentZoneId)

    fun advanceTo(timestampMillis: Long) {
        require(timestampMillis >= currentTimeMillis) { "controlled time cannot move backwards" }
        currentTimeMillis = timestampMillis
    }

    fun changeZone(zoneId: ZoneId) {
        currentZoneId = zoneId
    }
}
