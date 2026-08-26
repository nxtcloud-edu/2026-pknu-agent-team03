package com.timeback.ui.fake

import com.timeback.ui.domain.gateway.FeatureGateway
import com.timeback.ui.domain.model.*
import kotlinx.coroutines.delay

/**
 * CT-06 FakeFeatureGateway — 실제 기능 완료 전 UI 독립 개발용 테스트 대역.
 * CT-04 인터페이스를 구현하며 FixedResults의 고정 데이터를 반환한다.
 * 시나리오 전환은 currentScenario 필드로 제어한다.
 */
class FakeFeatureGateway : FeatureGateway {

    // 시나리오 제어용 상태 — 테스트에서 변경 가능
    var permissionGranted: Boolean = true
    var baselineReady: Boolean = true
    var hasGoals: Boolean = true
    var isOffline: Boolean = false
    var deletionComplete: Boolean = false

    private val simulatedDelay = 100L // ms

    // --- UI-01 ---
    override suspend fun readAccessState(): ScreenState {
        delay(simulatedDelay)
        return if (permissionGranted) FixedResults.permissionReady
        else FixedResults.permissionRequired
    }

    override suspend fun openUsageAccessSettings(): ActionResult {
        delay(simulatedDelay)
        return ActionResult.Success
    }

    override suspend fun refreshAccessState(): ScreenState {
        delay(simulatedDelay)
        return if (permissionGranted) FixedResults.permissionReady
        else FixedResults.permissionRequired
    }

    // --- UI-02 ---
    override suspend fun readHome(): ScreenState {
        delay(simulatedDelay)
        return if (baselineReady) FixedResults.homeContent
        else FixedResults.homeBaselineObserving
    }

    override suspend fun refreshHome(): ScreenState {
        delay(simulatedDelay)
        return if (baselineReady) FixedResults.homeContent
        else FixedResults.homeBaselineObserving
    }

    // --- UI-03 ---
    override suspend fun readTimeline(date: Long): ScreenState {
        delay(simulatedDelay)
        return FixedResults.timelineWithMixed
    }

    override suspend fun refreshTimeline(date: Long): ScreenState {
        delay(simulatedDelay)
        return FixedResults.timelineWithMixed
    }

    override suspend fun createActivity(type: ActivityType, name: String, timeRange: TimeRange): ActionResult {
        delay(simulatedDelay)
        return if (timeRange.endAt > timeRange.startAt) ActionResult.Success
        else ActionResult.Blocked(BlockReason.INVALID_TIME_RANGE)
    }

    override suspend fun updateActivity(activityId: Identifier, name: String?, timeRange: TimeRange?): ActionResult {
        delay(simulatedDelay)
        return ActionResult.Success
    }

    override suspend fun confirmContext(contextId: Identifier): ActionResult {
        delay(simulatedDelay)
        return ActionResult.Success
    }

    override suspend fun updateContext(contextId: Identifier, classification: ContextClassification): ActionResult {
        delay(simulatedDelay)
        return ActionResult.Success
    }

    // --- UI-04 ---
    override suspend fun readApps(): ScreenState {
        delay(simulatedDelay)
        return FixedResults.appsContent
    }

    override suspend fun changeDefaultClassification(packageName: String, classification: AppClassification): ActionResult {
        delay(simulatedDelay)
        return ActionResult.Success
    }

    // --- UI-05 ---
    override suspend fun readRecoveryEntry(): ScreenState {
        delay(simulatedDelay)
        return if (hasGoals) FixedResults.recoveryGoalOverlap
        else FixedResults.recoveryNoGoal
    }

    override suspend fun startGoalTimer(goalId: Identifier): ActionResult {
        delay(simulatedDelay)
        return ActionResult.Success
    }

    override suspend fun completeGoalTimer(): ActionResult {
        delay(simulatedDelay)
        return ActionResult.Success
    }

    override suspend fun createManualRecoveredTime(goalId: Identifier, timeRange: TimeRange): ActionResult {
        delay(simulatedDelay)
        return if (timeRange.endAt > timeRange.startAt) ActionResult.Success
        else ActionResult.Blocked(BlockReason.INVALID_TIME_RANGE)
    }

    override suspend fun selectRepresentativeGoal(overlapGroupId: Identifier, goalId: Identifier): ActionResult {
        delay(simulatedDelay)
        return ActionResult.Success
    }

    // --- UI-06 ---
    override suspend fun readGoals(): ScreenState {
        delay(simulatedDelay)
        return if (hasGoals) FixedResults.goalsContent
        else ScreenState.Empty
    }

    override suspend fun createGoal(name: String, targetDuration: Duration): ActionResult {
        delay(simulatedDelay)
        return ActionResult.Success
    }

    // --- UI-07 ---
    override suspend fun readReport(period: ReportPeriod): ScreenState {
        delay(simulatedDelay)
        return FixedResults.reportContent
    }

    // --- UI-08 ---
    override suspend fun readDataManagementState(): ScreenState {
        delay(simulatedDelay)
        return when {
            isOffline -> FixedResults.dataManagementOffline
            deletionComplete -> FixedResults.dataManagementDeleteComplete
            else -> FixedResults.dataManagementBackupPending
        }
    }

    override suspend fun changeRetentionSelection(selection: RetentionSelection): ActionResult {
        delay(simulatedDelay)
        return if (isOffline) ActionResult.RetryableFailure(ErrorReason.OFFLINE)
        else ActionResult.Success
    }

    override suspend fun requestFullDeletion(): ActionResult {
        delay(simulatedDelay)
        return ActionResult.Success // returns confirmation-needed state
    }

    override suspend fun confirmFullDeletion(): ActionResult {
        delay(simulatedDelay)
        deletionComplete = true
        return ActionResult.Success
    }

    override suspend fun refreshDeletionStatus(jobId: Identifier): ScreenState {
        delay(simulatedDelay)
        return if (deletionComplete) FixedResults.dataManagementDeleteComplete
        else FixedResults.dataManagementDeletePartial
    }
}
