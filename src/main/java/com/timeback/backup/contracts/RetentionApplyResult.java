package com.timeback.backup.contracts;

public class RetentionApplyResult {
    private final String retentionSelection;
    private final String deviceStatus;
    private final RetentionServerStatus serverStatus;

    public RetentionApplyResult(String retentionSelection, String deviceStatus,
                                RetentionServerStatus serverStatus) {
        this.retentionSelection = retentionSelection;
        this.deviceStatus = deviceStatus;
        this.serverStatus = serverStatus;
    }

    public String getRetentionSelection() { return retentionSelection; }
    public String getDeviceStatus() { return deviceStatus; }
    public RetentionServerStatus getServerStatus() { return serverStatus; }
}
