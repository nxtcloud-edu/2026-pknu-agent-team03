package com.timeback.device.fake;

import com.timeback.device.contract.ScreenEndEvent;
import com.timeback.device.contract.ScreenStateSource;
import com.timeback.device.contract.TimeRange;

import java.util.List;

public final class FakeScreenStateSource implements ScreenStateSource {
    private List<ScreenEndEvent> events;
    private int queryCount;
    private RuntimeException failWith;

    public FakeScreenStateSource() {
        this(List.of());
    }

    public FakeScreenStateSource(List<ScreenEndEvent> events) {
        this.events = List.copyOf(events);
    }

    @Override
    public List<ScreenEndEvent> queryScreenEndEvents(TimeRange range) {
        queryCount++;
        if (failWith != null) {
            throw failWith;
        }
        return events.stream().filter(event -> range.contains(event.occurredAtMillis())).toList();
    }

    public void setEvents(List<ScreenEndEvent> events) {
        this.events = List.copyOf(events);
    }

    public int getQueryCount() {
        return queryCount;
    }

    public void setFailWith(RuntimeException failWith) {
        this.failWith = failWith;
    }
}
