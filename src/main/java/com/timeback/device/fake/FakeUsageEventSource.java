package com.timeback.device.fake;

import com.timeback.device.contract.ObservedUsageEvent;
import com.timeback.device.contract.TimeRange;
import com.timeback.device.contract.UsageEventSource;

import java.util.ArrayList;
import java.util.List;

public final class FakeUsageEventSource implements UsageEventSource {
    private List<ObservedUsageEvent> events;
    private int queryCount;
    private final List<TimeRange> requestedRanges = new ArrayList<>();
    private RuntimeException failWith;

    public FakeUsageEventSource() {
        this(List.of());
    }

    public FakeUsageEventSource(List<ObservedUsageEvent> events) {
        this.events = List.copyOf(events);
    }

    @Override
    public List<ObservedUsageEvent> queryEvents(TimeRange range) {
        queryCount++;
        requestedRanges.add(range);
        if (failWith != null) {
            throw failWith;
        }
        return events.stream().filter(event -> range.contains(event.occurredAtMillis())).toList();
    }

    public void setEvents(List<ObservedUsageEvent> events) {
        this.events = List.copyOf(events);
    }

    public int getQueryCount() {
        return queryCount;
    }

    public List<TimeRange> getRequestedRanges() {
        return List.copyOf(requestedRanges);
    }

    public void setFailWith(RuntimeException failWith) {
        this.failWith = failWith;
    }
}
