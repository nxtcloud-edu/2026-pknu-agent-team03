package com.timeback.device.os;

import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import com.timeback.device.contract.ObservedUsageEvent;
import com.timeback.device.contract.TimeRange;
import com.timeback.device.contract.UsageEventKind;
import com.timeback.device.contract.UsageEventSource;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class AndroidUsageEventSource implements UsageEventSource {
    private final UsageStatsManager usageStatsManager;

    public AndroidUsageEventSource(Context context) {
        this.usageStatsManager = Objects.requireNonNull(
                context.getSystemService(UsageStatsManager.class),
                "UsageStatsManager is unavailable"
        );
    }

    @Override
    public List<ObservedUsageEvent> queryEvents(TimeRange range) {
        UsageEvents stream = usageStatsManager.queryEvents(range.startAtMillis(), range.endAtMillis());
        UsageEvents.Event cursor = new UsageEvents.Event();
        Map<String, Integer> occurrences = new HashMap<>();
        List<ObservedUsageEvent> result = new ArrayList<>();

        while (stream.hasNextEvent()) {
            stream.getNextEvent(cursor);
            UsageEventKind kind;
            if (cursor.getEventType() == UsageEvents.Event.ACTIVITY_RESUMED) {
                kind = UsageEventKind.FOREGROUND;
            } else if (cursor.getEventType() == UsageEvents.Event.ACTIVITY_PAUSED) {
                kind = UsageEventKind.BACKGROUND;
            } else {
                continue;
            }

            String packageName = cursor.getPackageName();
            if (packageName == null || packageName.isBlank() || !range.contains(cursor.getTimeStamp())) {
                continue;
            }
            String groupKey = packageName + "|" + kind.name() + "|" + cursor.getTimeStamp();
            int occurrence = occurrences.getOrDefault(groupKey, 0);
            occurrences.put(groupKey, occurrence + 1);
            result.add(new ObservedUsageEvent(
                    packageName,
                    kind,
                    cursor.getTimeStamp(),
                    occurrence
            ));
        }

        result.sort(Comparator
                .comparingLong(ObservedUsageEvent::occurredAtMillis)
                .thenComparingInt(ObservedUsageEvent::occurrenceInTimestampGroup)
                .thenComparing(ObservedUsageEvent::packageName)
                .thenComparing(event -> event.kind().name()));
        return List.copyOf(result);
    }
}
