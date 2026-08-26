package com.timeback.device.os

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import com.timeback.device.contract.ObservedUsageEvent
import com.timeback.device.contract.TimeRange
import com.timeback.device.contract.UsageEventKind
import com.timeback.device.contract.UsageEventSource

class AndroidUsageEventSource(
    context: Context,
) : UsageEventSource {
    private val usageStatsManager = context.getSystemService(UsageStatsManager::class.java)

    override fun queryEvents(range: TimeRange): List<ObservedUsageEvent> {
        val stream = usageStatsManager.queryEvents(range.startAtMillis, range.endAtMillis)
        val cursor = UsageEvents.Event()
        val occurrences = mutableMapOf<String, Int>()
        val result = mutableListOf<ObservedUsageEvent>()

        while (stream.hasNextEvent()) {
            stream.getNextEvent(cursor)
            val kind = when (cursor.eventType) {
                UsageEvents.Event.ACTIVITY_RESUMED -> UsageEventKind.FOREGROUND
                UsageEvents.Event.ACTIVITY_PAUSED -> UsageEventKind.BACKGROUND
                else -> null
            } ?: continue
            val packageName = cursor.packageName?.takeIf(String::isNotBlank) ?: continue
            if (!range.contains(cursor.timeStamp)) continue

            val groupKey = "$packageName|${kind.name}|${cursor.timeStamp}"
            val occurrence = occurrences[groupKey] ?: 0
            occurrences[groupKey] = occurrence + 1
            result += ObservedUsageEvent(
                packageName = packageName,
                kind = kind,
                occurredAtMillis = cursor.timeStamp,
                occurrenceInTimestampGroup = occurrence,
            )
        }

        return result.sortedWith(
            compareBy(
                { it.occurredAtMillis },
                { it.occurrenceInTimestampGroup },
                { it.packageName },
                { it.kind.name },
            ),
        )
    }
}
