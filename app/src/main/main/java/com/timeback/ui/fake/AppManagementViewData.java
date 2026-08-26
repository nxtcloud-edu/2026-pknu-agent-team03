package com.timeback.ui.fake;

import com.timeback.ui.domain.model.AppInfo;
import java.util.List;

public class AppManagementViewData {
    private final List<AppInfo> apps;

    public AppManagementViewData(List<AppInfo> apps) {
        this.apps = apps;
    }

    public List<AppInfo> getApps() { return apps; }
}
