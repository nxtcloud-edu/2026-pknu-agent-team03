package com.timeback.ui.domain.gateway;

import com.timeback.ui.domain.model.*;

import java.util.concurrent.CompletableFuture;

/**
 * CT-04 기능 호출 계약 — frontend-components.md §4~§11 매핑
 * Java에서는 CompletableFuture로 비동기 호출을 표현한다.
 */
public interface FeatureGateway {

    // --- UI-01: 권한·초기 진입 ---
    CompletableFuture<ScreenState> readAccessState();
    CompletableFuture<ActionResult> openUsageAccessSettings();
    CompletableFuture<ScreenState> refreshAccessState();

    // --- UI-02: 홈 대시보드 ---
    CompletableFuture<ScreenState> readHome();
    CompletableFuture<ScreenState> refreshHome();

    // --- UI-03: Timeline ---
    CompletableFuture<ScreenState> readTimeline(long date);
    CompletableFuture<ScreenState> refreshTimeline(long date);
    CompletableFuture<ActionResult> createActivity(ActivityType type, String name, TimeRange timeRange);
    CompletableFuture<ActionResult> updateActivity(Identifier activityId, String name, TimeRange timeRange);
    CompletableFuture<ActionResult> confirmContext(Identifier contextId);
    CompletableFuture<ActionResult> updateContext(Identifier contextId, ContextClassification classification);

    // --- UI-04: 앱 관리 ---
    CompletableFuture<ScreenState> readApps();
    CompletableFuture<ActionResult> changeDefaultClassification(String packageName, AppClassification classification);

    // --- UI-05: 시간 되찾기 ---
    CompletableFuture<ScreenState> readRecoveryEntry();
    CompletableFuture<ActionResult> startGoalTimer(Identifier goalId);
    CompletableFuture<ActionResult> completeGoalTimer();
    CompletableFuture<ActionResult> createManualRecoveredTime(Identifier goalId, TimeRange timeRange);
    CompletableFuture<ActionResult> selectRepresentativeGoal(Identifier overlapGroupId, Identifier goalId);

    // --- UI-06: 목표 ---
    CompletableFuture<ScreenState> readGoals();
    CompletableFuture<ActionResult> createGoal(String name, Duration targetDuration);

    // --- UI-07: 리포트 ---
    CompletableFuture<ScreenState> readReport(ReportPeriod period);

    // --- UI-08: 데이터 관리 ---
    CompletableFuture<ScreenState> readDataManagementState();
    CompletableFuture<ActionResult> changeRetentionSelection(RetentionSelection selection);
    CompletableFuture<ActionResult> requestFullDeletion();
    CompletableFuture<ActionResult> confirmFullDeletion();
    CompletableFuture<ScreenState> refreshDeletionStatus(Identifier jobId);
}
