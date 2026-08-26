package com.timeback.device.os;

import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import com.timeback.device.contract.ScreenEndEvent;
import com.timeback.device.contract.ScreenStateSource;
import com.timeback.device.contract.TimeRange;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class AndroidScreenStateSource implements ScreenStateSource {
    private final UsageStatsManager usageStatsManager;

    public AndroidScreenStateSource(Context context) {
        this.usageStatsManager = Objects.requireNonNull(
                context.getSystemService(UsageStatsManager.class),
                "UsageStatsManager is unavailable"
        );
    }

    @Override
    public List<ScreenEndEvent> queryScreenEndEvents(TimeRange range) {
        UsageEvents stream = usageStatsManager.queryEvents(range.startAtMillis(), range.endAtMillis());
        UsageEvents.Event cursor = new UsageEvents.Event();
        List<ScreenEndEvent> result = new ArrayList<>();
        while (stream.hasNextEvent()) {
            stream.getNextEvent(cursor);
            if (cursor.getEventType() == UsageEvents.Event.SCREEN_NON_INTERACTIVE
                    && range.contains(cursor.getTimeStamp())) {
                result.add(new ScreenEndEvent(cursor.getTimeStamp()));
            }
        }

        result.sort(Comparator.comparingLong(ScreenEndEvent::occurredAtMillis));
        Map<Long, ScreenEndEvent> unique = new LinkedHashMap<>();
        for (ScreenEndEvent event : result) {
            unique.putIfAbsent(event.occurredAtMillis(), event);
        }
        return List.copyOf(unique.values());
    }
}
