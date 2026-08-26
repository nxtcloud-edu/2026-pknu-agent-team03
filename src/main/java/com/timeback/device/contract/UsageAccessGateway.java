package com.timeback.device.contract;

public interface UsageAccessGateway {
    UsageAccessStatus readCurrentStatus();

    boolean openUsageAccessSettings();
}
