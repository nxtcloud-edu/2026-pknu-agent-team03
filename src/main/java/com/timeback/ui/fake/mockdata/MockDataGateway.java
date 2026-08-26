package com.timeback.ui.fake.mockdata;

import com.timeback.ui.domain.gateway.FeatureGateway;
import com.timeback.ui.domain.model.*;
import com.timeback.ui.fake.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * MockScreenTimeData를 사용하여 실제 사용 패턴에 가까운 결과를 반환하는 FeatureGateway 구현.
 */
public class MockDataGateway implements FeatureGateway {

    private static final long BASE_DAY = 1_733_670_000_000L;
    private static final long ONE_DAY = 86_400_000L;

    private <T> CompletableFuture<T> delayed(T value) {
        return CompletableFuture.supplyAsync(() -> {
            try { Thread.sleep(80); } catch (InterruptedException ignored) {}
            return value;
        });
    }

    // --- UI-01: 권한 ---
    @Override
    public CompletableFuture<ScreenState> readAccessState() {
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
        MockScreenTimeData.WeeklySummary summary = MockScreenTimeData.computeWeeklySummary();
        List<GoalProgress> goals = Arrays.asList(
                new GoalProgress(new Goal(new Identifier("goal-1"), "코딩 연습", new Duration(3_600_000)), new Duration(2_700_000), 75.0),
                new GoalProgress(new Goal(new Identifier("goal-2"), "독서", new Duration(1_800_000)), new Duration(600_000), 33.3),
                new GoalProgress(new Goal(new Identifier("goal-3"), "포트폴리오 정리", new Duration(5_400_000)), new Duration(1_800_000), 33.3)
        );
        HomeViewData data = new HomeViewData(
                new Duration(10_500_000),  // 오늘 낭비 175분
                new BaselineState(false, null, summary.getTotalWaste()),
                new Duration(3_600_000),
                new Duration(2_700_000),
                0.42,
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
        int dayIndex = (int) ((date - BASE_DAY) / ONE_DAY);
        List<List<TimelineItem>> allDays = MockScreenTimeData.allDays();
        List<TimelineItem> items;
        if (dayIndex >= 0 && dayIndex < allDays.size()) {
            items = allDays.get(dayIndex);
        } else {
            items = allDays.get(0); // fallback to day 1
        }
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
                new AppInfo("com.google.android.youtube", "YouTube", AppClassification.NEUTRAL),
                new AppInfo("notion.id", "Notion", AppClassification.PRODUCTIVE),
                new AppInfo("com.google.android.apps.docs", "Google Docs", AppClassification.PRODUCTIVE),
                new AppInfo("com.visualstudio.code", "VS Code", AppClassification.PRODUCTIVE),
                new AppInfo("com.netflix.mediaclient", "Netflix", AppClassification.LEISURE)
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
        List<Goal> goals = Arrays.asList(
                new Goal(new Identifier("goal-1"), "코딩 연습", new Duration(3_600_000)),
                new Goal(new Identifier("goal-2"), "독서", new Duration(1_800_000)),
                new Goal(new Identifier("goal-3"), "포트폴리오 정리", new Duration(5_400_000))
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
        List<GoalProgress> goals = Arrays.asList(
                new GoalProgress(new Goal(new Identifier("goal-1"), "코딩 연습", new Duration(3_600_000)), new Duration(2_700_000), 75.0),
                new GoalProgress(new Goal(new Identifier("goal-2"), "독서", new Duration(1_800_000)), new Duration(600_000), 33.3),
                new GoalProgress(new Goal(new Identifier("goal-3"), "포트폴리오 정리", new Duration(5_400_000)), new Duration(1_800_000), 33.3)
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
        MockScreenTimeData.WeeklySummary ws = MockScreenTimeData.computeWeeklySummary();
        TimeMetrics metrics = new TimeMetrics(
                ws.getTotalWaste(),
                ws.getTotalWaste(),
                new Duration(7_200_000),
                new Duration(5_400_000),
                0.42
        );
        List<GoalProgress> goalSummaries = Arrays.asList(
                new GoalProgress(new Goal(new Identifier("goal-1"), "코딩 연습", new Duration(3_600_000)), new Duration(2_700_000), 75.0),
                new GoalProgress(new Goal(new Identifier("goal-2"), "독서", new Duration(1_800_000)), new Duration(600_000), 33.3)
        );
        long now = System.currentTimeMillis();
        ReportData data = new ReportData(period, new TimeRange(now - 604_800_000, now), metrics, goalSummaries, true);
        return delayed(new ScreenState.Content(data));
    }

    // --- UI-08: 데이터 관리 ---
    @Override
    public CompletableFuture<ScreenState> readDataManagementState() {
        BackupStatus backupStatus = new BackupStatus(0, System.currentTimeMillis() - 1_800_000, false);
        DataManagementViewData data = new DataManagementViewData(true, backupStatus, new RetentionSelection("30days"), null);
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
        return delayed(new ActionResult.Success());
    }

    @Override
    public CompletableFuture<ScreenState> refreshDeletionStatus(Identifier jobId) {
        BackupStatus backupStatus = new BackupStatus(0, System.currentTimeMillis(), false);
        DeletionJobStatus status = new DeletionJobStatus(jobId, true, true);
        DataManagementViewData data = new DataManagementViewData(true, backupStatus, new RetentionSelection("30days"), status);
        return delayed(new ScreenState.Content(data));
    }
}
