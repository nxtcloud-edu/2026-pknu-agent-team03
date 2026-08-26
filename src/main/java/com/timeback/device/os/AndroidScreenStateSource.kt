package com.timeback.device.os

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import com.timeback.device.contract.ScreenEndEvent
import com.timeback.device.contract.ScreenStateSource
import com.timeback.device.contract.TimeRange

class AndroidScreenStateSource(
    context: Context,
) : ScreenStateSource {
    private val usageStatsManager = context.getSystemService(UsageStatsManager::class.java)

    override fun queryScreenEndEvents(range: TimeRange): List<ScreenEndEvent> {
        val stream = usageStatsManager.queryEvents(range.startAtMillis, range.endAtMillis)
        val cursor = UsageEvents.Event()
        val result = mutableListOf<ScreenEndEvent>()
        while (stream.hasNextEvent()) {
            stream.getNextEvent(cursor)
            if (
                cursor.eventType == UsageEvents.Event.SCREEN_NON_INTERACTIVE &&
                range.contains(cursor.timeStamp)
            ) {
                result += ScreenEndEvent(cursor.timeStamp)
            }
        }
        return result.distinctBy { it.occurredAtMillis }.sortedBy { it.occurredAtMillis }
    }
}
