package com.timeback.ui.fake;

public class PermissionViewData {
    private final boolean usageAccessGranted;
    private final boolean anonymousIdReady;
    private final boolean canEnterMainScreens;

    public PermissionViewData(boolean usageAccessGranted, boolean anonymousIdReady, boolean canEnterMainScreens) {
        this.usageAccessGranted = usageAccessGranted;
        this.anonymousIdReady = anonymousIdReady;
        this.canEnterMainScreens = canEnterMainScreens;
    }

    public boolean isUsageAccessGranted() { return usageAccessGranted; }
    public boolean isAnonymousIdReady() { return anonymousIdReady; }
    public boolean canEnterMainScreens() { return canEnterMainScreens; }
}
