package com.timeback.device.contract;

import java.util.List;

public interface ScreenStateSource {
    List<ScreenEndEvent> queryScreenEndEvents(TimeRange range);
}
