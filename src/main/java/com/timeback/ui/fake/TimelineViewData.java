package com.timeback.ui.fake;

import com.timeback.ui.domain.model.TimelineItem;
import java.util.List;

public class TimelineViewData {
    private final long date;
    private final List<TimelineItem> items;

    public TimelineViewData(long date, List<TimelineItem> items) {
        this.date = date;
        this.items = items;
    }

    public long getDate() { return date; }
    public List<TimelineItem> getItems() { return items; }
}
