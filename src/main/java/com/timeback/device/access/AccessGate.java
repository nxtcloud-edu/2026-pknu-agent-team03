package com.timeback.device.access;

import com.timeback.device.contract.AccessState;
import com.timeback.device.contract.SettingsOpenResult;
import com.timeback.device.contract.TimeSource;
import com.timeback.device.contract.UsageAccessGateway;
import com.timeback.device.contract.UsageAccessStatus;

public final class AccessGate {
    private final UsageAccessGateway gateway;
    private final TimeSource timeSource;

    public AccessGate(UsageAccessGateway gateway, TimeSource timeSource) {
        this.gateway = gateway;
        this.timeSource = timeSource;
    }

    public AccessState readAccessState() {
        long observedAt = timeSource.nowMillis();
        try {
            UsageAccessStatus status = gateway.readCurrentStatus();
            if (status == UsageAccessStatus.GRANTED) {
                return new AccessState.Granted(observedAt);
            }
            return new AccessState.Blocked(observedAt);
        } catch (RuntimeException error) {
            return new AccessState.Failure(
                    observedAt,
                    error.getMessage() == null ? "usage access status unavailable" : error.getMessage()
            );
        }
    }

    public SettingsOpenResult openUsageAccessSettings() {
        try {
            if (gateway.openUsageAccessSettings()) {
                return SettingsOpenResult.Opened.INSTANCE;
            }
            return new SettingsOpenResult.Failure("usage access settings could not be opened");
        } catch (RuntimeException error) {
            return new SettingsOpenResult.Failure(
                    error.getMessage() == null ? "usage access settings unavailable" : error.getMessage()
            );
        }
    }
}
