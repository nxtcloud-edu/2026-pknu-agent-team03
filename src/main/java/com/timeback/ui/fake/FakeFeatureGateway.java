package com.timeback.ui.fake;

import com.timeback.ui.domain.gateway.FeatureGateway;
import com.timeback.ui.domain.model.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * CT-06 고정 결과를 반환하는 FakeFeatureGateway.
 * CompletableFuture.supplyAsync + Thread.sleep(100)으로 시뮬레이션 딜레이 제공.
 */
public class FakeFeatureGateway implements FeatureGateway {

    // --- 동작 제어 필드 ---
    public boolean permissionGranted = true;
    public boolean baselineReady = true;
    public boolean hasGoals = true;
    public boolean isOffline = false;
    public boolean deletionComplete = false;

    private <T> CompletableFuture<T> delayed(T value) {
        return CompletableFuture.supplyAsync(() -> {
            try { Thread.sleep(100); } catch (InterruptedException ignored) {}
            return value;
        });
    }

    // --- UI-01: 권한 ---
    @Override
    public CompletableFuture<ScreenState> readAccessState() {
        if (!permissionGranted) {
            return delayed(new ScreenState.Blocked(BlockReason.PERMISSION_REQUIRED));
        }
        PermissionViewData data = new PermissionViewData(true, true, true);
        return delayed(new ScreenState.Content(data));
    }

    @Override
    public CompletableFuture<ActionResult> openUsageAccessSettings() {
        return delayed(new ActionResult.Success());
    }

    @Override
    public CompletableFuture<ScreenState> refreshAccessState() {
        return readAccessState();
    }

    // --- UI-02: 홈 대시보드 ---
    @Override
    public CompletableFuture<ScreenState> readHome() {
        if (isOffline) {
            return delayed(new ScreenState.RetryableError(ErrorReason.OFFLINE));
        }
        List<GoalProgress> goals = hasGoals
                ? Arrays.asList(
                    new GoalProgress(new Goal(new Identifier("goal-1"), "코딩 연습", new Duration(3_600_000)), new Duration(1_800_000), 50.0),
                    new GoalProgress(new Goal(new Identifier("goal-2"), "독서", new Duration(1_800_000)), new Duration(900_000), 50.0)
                )
                : Collections.emptyList();
        HomeViewData data = new HomeViewData(
                new Duration(7_200_000),  // 2시간 낭비
                new BaselineState(false, null, new Duration(10_800_000)),
                new Duration(3_600_000),  // 1시간 절약
                new Duration(1_800_000),  // 30분 되찾기
                0.5,
                goals
        );
        return delayed(new ScreenState.Content(data));
    }

    @Override
    public CompletableFuture<ScreenState> refreshHome() {
        return readHome();
    }

    // --- UI-03: Timeline ---
    @Override
    public CompletableFuture<ScreenState> readTimeline(long date) {
        List<TimelineItem> items = Arrays.asList(
                new TimelineItem(new Identifier("tl-1"), "Instagram", "com.instagram.android", "피드 탐색",
                        new TimeRange(date + 32_400_000, date + 36_000_000), new Duration(3_600_000),
                        ContextClassification.WASTE, false, false),
                new TimelineItem(new Identifier("tl-2"), "Chrome", "com.android.chrome", "채용 공고 검색",
                        new TimeRange(date + 36_000_000, date + 39_600_000), new Duration(3_600_000),
                        ContextClassification.PRODUCTIVE, false, false),
                new TimelineItem(new Identifier("tl-3"), "TikTok", "com.zhiliaoapp.musically", "숏폼 시청",
                        new TimeRange(date + 43_200_000, date + 46_800_000), new Duration(3_600_000),
                        ContextClassification.WASTE, false, true)
        );
        TimelineViewData data = new TimelineViewData(date, items);
        return delayed(new ScreenState.Content(data));
    }

    @Override
    public CompletableFuture<ScreenState> refreshTimeline(long date) {
        return readTimeline(date);
    }

    @Override
    public CompletableFuture<ActionResult> createActivity(ActivityType type, String name, TimeRange timeRange) {
        return delayed(new ActionResult.Success());
    }

    @Override
    public CompletableFuture<ActionResult> updateActivity(Identifier activityId, String name, TimeRange timeRange) {
        return delayed(new ActionResult.Success());
    }

    @Override
    public CompletableFuture<ActionResult> confirmContext(Identifier contextId) {
        return delayed(new ActionResult.Success());
    }

    @Override
    public CompletableFuture<ActionResult> updateContext(Identifier contextId, ContextClassification classification) {
        return delayed(new ActionResult.Success());
    }

    // --- UI-04: 앱 관리 ---
    @Override
    public CompletableFuture<ScreenState> readApps() {
        List<AppInfo> apps = Arrays.asList(
                new AppInfo("com.instagram.android", "Instagram", AppClassification.WASTE),
                new AppInfo("com.zhiliaoapp.musically", "TikTok", AppClassification.WASTE),
                new AppInfo("com.twitter.android", "X", AppClassification.LEISURE),
                new AppInfo("com.android.chrome", "Chrome", AppClassification.NEUTRAL),
                new AppInfo("com.google.android.apps.docs", "Google Docs", AppClassification.PRODUCTIVE)
        );
        AppManagementViewData data = new AppManagementViewData(apps);
        return delayed(new ScreenState.Content(data));
    }

    @Override
    public CompletableFuture<ActionResult> changeDefaultClassification(String packageName, AppClassification classification) {
        return delayed(new ActionResult.Success());
    }

    // --- UI-05: 시간 되찾기 ---
    @Override
    public CompletableFuture<ScreenState> readRecoveryEntry() {
        if (!hasGoals) {
            return delayed(new ScreenState.Empty());
        }
        List<Goal> goals = Arrays.asList(
                new Goal(new Identifier("goal-1"), "코딩 연습", new Duration(3_600_000)),
                new Goal(new Identifier("goal-2"), "독서", new Duration(1_800_000))
        );
        RecoveryEntry entry = new RecoveryEntry(goals.get(0), TimerState.IDLE, goals, false);
        RecoveryViewData data = new RecoveryViewData(entry);
        return delayed(new ScreenState.Content(data));
    }

    @Override
    public CompletableFuture<ActionResult> startGoalTimer(Identifier goalId) {
        return delayed(new ActionResult.Success());
    }

    @Override
    public CompletableFuture<ActionResult> completeGoalTimer() {
        return delayed(new ActionResult.Success());
    }

    @Override
    public CompletableFuture<ActionResult> createManualRecoveredTime(Identifier goalId, TimeRange timeRange) {
        return delayed(new ActionResult.Success());
    }

    @Override
    public CompletableFuture<ActionResult> selectRepresentativeGoal(Identifier overlapGroupId, Identifier goalId) {
        return delayed(new ActionResult.Success());
    }

    // --- UI-06: 목표 ---
    @Override
    public CompletableFuture<ScreenState> readGoals() {
        if (!hasGoals) {
            return delayed(new ScreenState.Empty());
        }
        List<GoalProgress> goals = Arrays.asList(
                new GoalProgress(new Goal(new Identifier("goal-1"), "코딩 연습", new Duration(3_600_000)), new Duration(1_800_000), 50.0),
                new GoalProgress(new Goal(new Identifier("goal-2"), "독서", new Duration(1_800_000)), new Duration(900_000), 50.0)
        );
        GoalsViewData data = new GoalsViewData(goals);
        return delayed(new ScreenState.Content(data));
    }

    @Override
    public CompletableFuture<ActionResult> createGoal(String name, Duration targetDuration) {
        return delayed(new ActionResult.Success());
    }

    // --- UI-07: 리포트 ---
    @Override
    public CompletableFuture<ScreenState> readReport(ReportPeriod period) {
        if (!baselineReady) {
            return delayed(new ScreenState.Empty());
        }
        TimeMetrics metrics = new TimeMetrics(
                new Duration(14_400_000),  // 4h 주간 낭비
                new Duration(10_800_000),  // 3h 기준선
                new Duration(3_600_000),   // 1h 절약
                new Duration(1_800_000),   // 30min 되찾기
                0.5
        );
        List<GoalProgress> goalSummaries = Arrays.asList(
                new GoalProgress(new Goal(new Identifier("goal-1"), "코딩 연습", new Duration(3_600_000)), new Duration(2_700_000), 75.0),
                new GoalProgress(new Goal(new Identifier("goal-2"), "독서", new Duration(1_800_000)), new Duration(1_200_000), 66.7)
        );
        long now = System.currentTimeMillis();
        ReportData data = new ReportData(period, new TimeRange(now - 604_800_000, now), metrics, goalSummaries, true);
        return delayed(new ScreenState.Content(data));
    }

    // --- UI-08: 데이터 관리 ---
    @Override
    public CompletableFuture<ScreenState> readDataManagementState() {
        BackupStatus backupStatus = new BackupStatus(0, System.currentTimeMillis() - 3_600_000, isOffline);
        DeletionJobStatus deletionJob = deletionComplete
                ? new DeletionJobStatus(new Identifier("job-1"), true, true)
                : null;
        DataManagementViewData data = new DataManagementViewData(
                true, backupStatus, new RetentionSelection("30days"), deletionJob
        );
        return delayed(new ScreenState.Content(data));
    }

    @Override
    public CompletableFuture<ActionResult> changeRetentionSelection(RetentionSelection selection) {
        return delayed(new ActionResult.Success());
    }

    @Override
    public CompletableFuture<ActionResult> requestFullDeletion() {
        return delayed(new ActionResult.Success());
    }

    @Override
    public CompletableFuture<ActionResult> confirmFullDeletion() {
        deletionComplete = true;
        return delayed(new ActionResult.Success());
    }

    @Override
    public CompletableFuture<ScreenState> refreshDeletionStatus(Identifier jobId) {
        DeletionJobStatus status = new DeletionJobStatus(jobId, deletionComplete, deletionComplete);
        BackupStatus backupStatus = new BackupStatus(0, System.currentTimeMillis(), isOffline);
        DataManagementViewData data = new DataManagementViewData(true, backupStatus, new RetentionSelection("30days"), status);
        return delayed(new ScreenState.Content(data));
    }
}
