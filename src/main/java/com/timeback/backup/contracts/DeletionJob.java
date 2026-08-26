package com.timeback.backup.contracts;

/**
 * 전체 삭제 작업 추적.
 * 기기·서버 양쪽 COMPLETED일 때만 completedAt이 존재한다.
 */
public class DeletionJob {
    private final String jobId;
    private final String anonymousUserId;
    private final long requestedAt;
    private DeletionStatus deviceStatus;
    private DeletionStatus serverStatus;
    private Long completedAt; // nullable — 양쪽 완료 시만

    public DeletionJob(String jobId, String anonymousUserId, long requestedAt) {
        this.jobId = jobId;
        this.anonymousUserId = anonymousUserId;
        this.requestedAt = requestedAt;
        this.deviceStatus = DeletionStatus.PENDING;
        this.serverStatus = DeletionStatus.PENDING;
        this.completedAt = null;
    }

    public String getJobId() { return jobId; }
    public String getAnonymousUserId() { return anonymousUserId; }
    public long getRequestedAt() { return requestedAt; }
    public DeletionStatus getDeviceStatus() { return deviceStatus; }
    public DeletionStatus getServerStatus() { return serverStatus; }
    public Long getCompletedAt() { return completedAt; }

    public void setDeviceStatus(DeletionStatus status) { this.deviceStatus = status; }
    public void setServerStatus(DeletionStatus status) { this.serverStatus = status; }

    public void checkCompletion() {
        if (deviceStatus == DeletionStatus.COMPLETED && serverStatus == DeletionStatus.COMPLETED) {
            this.completedAt = System.currentTimeMillis();
        }
    }

    public boolean isCompleted() {
        return completedAt != null;
    }
}
