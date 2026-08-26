package com.timeback.device.fake;

import com.timeback.device.contract.UsageAccessGateway;
import com.timeback.device.contract.UsageAccessStatus;

public final class FakeUsageAccessGateway implements UsageAccessGateway {
    private UsageAccessStatus status;
    private int readCount;
    private int settingsOpenCount;
    private RuntimeException failReadsWith;
    private boolean settingsCanOpen = true;

    public FakeUsageAccessGateway() {
        this(UsageAccessStatus.NOT_GRANTED);
    }

    public FakeUsageAccessGateway(UsageAccessStatus status) {
        this.status = status;
    }

    @Override
    public UsageAccessStatus readCurrentStatus() {
        readCount++;
        if (failReadsWith != null) {
            throw failReadsWith;
        }
        return status;
    }

    @Override
    public boolean openUsageAccessSettings() {
        settingsOpenCount++;
        return settingsCanOpen;
    }

    public UsageAccessStatus getStatus() {
        return status;
    }

    public void setStatus(UsageAccessStatus status) {
        this.status = status;
    }

    public int getReadCount() {
        return readCount;
    }

    public int getSettingsOpenCount() {
        return settingsOpenCount;
    }

    public void setFailReadsWith(RuntimeException failReadsWith) {
        this.failReadsWith = failReadsWith;
    }

    public void setSettingsCanOpen(boolean settingsCanOpen) {
        this.settingsCanOpen = settingsCanOpen;
    }
}
