package com.timeback.ui.domain.gateway

import com.timeback.ui.domain.model.*

/**
 * CT-04 기능 호출 계약 — frontend-components.md §4~§11의 조회·작업 매핑
 * track-ui는 이 인터페이스만 사용하고 실제 구현을 알지 못한다.
 * FakeFeatureGateway와 실제 FeatureGateway 모두 이 인터페이스를 구현한다.
 */
interface FeatureGateway {

    // --- UI-01: 권한·초기 진입 ---
    suspend fun readAccessState(): ScreenState
    suspend fun openUsageAccessSettings(): ActionResult
    suspend fun refreshAccessState(): ScreenState

    // --- UI-02: 홈 대시보드 ---
    suspend fun readHome(): ScreenState
    suspend fun refreshHome(): ScreenState

    // --- UI-03: Timeline ---
    suspend fun readTimeline(date: Long): ScreenState // date = epoch millis of local midnight
    suspend fun refreshTimeline(date: Long): ScreenState
    suspend fun createActivity(type: ActivityType, name: String, timeRange: TimeRange): ActionResult
    suspend fun updateActivity(activityId: Identifier, name: String?, timeRange: TimeRange?): ActionResult
    suspend fun confirmContext(contextId: Identifier): ActionResult
    suspend fun updateContext(contextId: Identifier, classification: ContextClassification): ActionResult

    // --- UI-04: 앱 관리 ---
    suspend fun readApps(): ScreenState
    suspend fun changeDefaultClassification(packageName: String, classification: AppClassification): ActionResult

    // --- UI-05: 시간 되찾기 ---
    suspend fun readRecoveryEntry(): ScreenState
    suspend fun startGoalTimer(goalId: Identifier): ActionResult
    suspend fun completeGoalTimer(): ActionResult
    suspend fun createManualRecoveredTime(goalId: Identifier, timeRange: TimeRange): ActionResult
    suspend fun selectRepresentativeGoal(overlapGroupId: Identifier, goalId: Identifier): ActionResult

    // --- UI-06: 목표 ---
    suspend fun readGoals(): ScreenState
    suspend fun createGoal(name: String, targetDuration: Duration): ActionResult

    // --- UI-07: 리포트 ---
    suspend fun readReport(period: ReportPeriod): ScreenState

    // --- UI-08: 데이터 관리 ---
    suspend fun readDataManagementState(): ScreenState
    suspend fun changeRetentionSelection(selection: RetentionSelection): ActionResult
    suspend fun requestFullDeletion(): ActionResult
    suspend fun confirmFullDeletion(): ActionResult
    suspend fun refreshDeletionStatus(jobId: Identifier): ScreenState
}
