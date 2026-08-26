package com.timeback.ui.fake.mockdata

import com.timeback.ui.domain.gateway.FeatureGateway
import com.timeback.ui.domain.model.*
import com.timeback.ui.fake.*
import kotlinx.coroutines.delay

/**
 * MockDataGateway — MockScreenTimeData의 일주일치 데이터를 사용하는 FeatureGateway.
 * FakeFeatureGateway를 대체하여 실제적인 목업 시나리오를 UI에 제공한다.
 * 
 * 페르소나: 취업준비생 대학교 4학년, SNS 과다 사용
 */
class MockDataGateway : FeatureGateway {

    private val data = MockScreenTimeData
    private val simulatedDelay = 80L

    // --- UI-01: 권한 (항상 허용) ---
    override suspend fun readAccessState(): ScreenState {
        delay(simulatedDelay)
        return ScreenState.Content(
            PermissionViewData(usageAccessGranted = true, anonymousIdReady = true, canEnterMainScreens = true)
        )
    }

    override suspend fun openUsageAccessSettings(): ActionResult = ActionResult.Success
    override suspend fun refreshAccessState(): ScreenState = readAccessState()

    // --- UI-02: 홈 대시보드 (오늘 = 일요일 기준) ---
    override suspend fun readHome(): ScreenState {
        delay(simulatedDelay)
        val today = data.day7_sunday
        val summary = data.weeklySummary

        return ScreenState.Content(
            HomeViewData(
                wasteTimeToday = Duration(today.wasteMinutes * 60_000L),
                baselineState = BaselineState(
                    isObserving = false,
                    remainingDays = null,
                    weeklyBaseline = Duration(summary.averageDailyWasteMinutes * 7L * 60_000)
                ),
                savedTime = null, // Baseline 방금 완료, 비교 시작
                recoveredTime = Duration(0),
                recoveryRate = null,
                goalProgresses = listOf(
                    GoalProgress(
                        goal = Goal(Identifier("goal-coding"), "코딩 공부", Duration(120 * 60_000L)),
                        accumulatedDuration = Duration(90 * 60_000L), // 150분 중 누적 90분
                        progressPercent = 75.0
                    ),
                    GoalProgress(
                        goal = Goal(Identifier("goal-interview"), "면접 준비", Duration(60 * 60_000L)),
                        accumulatedDuration = Duration(25 * 60_000L),
                        progressPercent = 41.7
                    )
                )
            )
        )
    }

    override suspend fun refreshHome(): ScreenState = readHome()

    // --- UI-03: Timeline (날짜별) ---
    override suspend fun readTimeline(date: Long): ScreenState {
        delay(simulatedDelay)
        val dayData = findDayByDate(date) ?: return ScreenState.Empty
        return ScreenState.Content(
            TimelineViewData(date = dayData.date, items = dayData.timelineItems)
        )
    }

    override suspend fun refreshTimeline(date: Long): ScreenState = readTimeline(date)

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

    // --- UI-04: 앱 관리 ---
    override suspend fun readApps(): ScreenState {
        delay(simulatedDelay)
        return ScreenState.Content(AppManagementViewData(apps = data.appClassifications))
    }

    override suspend fun changeDefaultClassification(packageName: String, classification: AppClassification): ActionResult {
        delay(simulatedDelay)
        return ActionResult.Success
    }

    // --- UI-05: 시간 되찾기 ---
    override suspend fun readRecoveryEntry(): ScreenState {
        delay(simulatedDelay)
        return ScreenState.Content(
            RecoveryViewData(
                entry = RecoveryEntry(
                    selectedGoal = Goal(Identifier("goal-coding"), "코딩 공부", Duration(120 * 60_000L)),
                    timerState = TimerState.IDLE,
                    availableGoals = listOf(
                        Goal(Identifier("goal-coding"), "코딩 공부", Duration(120 * 60_000L)),
                        Goal(Identifier("goal-interview"), "면접 준비", Duration(60 * 60_000L)),
                        Goal(Identifier("goal-exercise"), "운동", Duration(30 * 60_000L))
                    ),
                    overlapRequiresRepresentative = false
                )
            )
        )
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

    // --- UI-06: 목표 ---
    override suspend fun readGoals(): ScreenState {
        delay(simulatedDelay)
        return ScreenState.Content(
            GoalsViewData(
                goals = listOf(
                    GoalProgress(
                        goal = Goal(Identifier("goal-coding"), "코딩 공부", Duration(120 * 60_000L)),
                        accumulatedDuration = Duration(90 * 60_000L),
                        progressPercent = 75.0
                    ),
                    GoalProgress(
                        goal = Goal(Identifier("goal-interview"), "면접 준비", Duration(60 * 60_000L)),
                        accumulatedDuration = Duration(25 * 60_000L),
                        progressPercent = 41.7
                    ),
                    GoalProgress(
                        goal = Goal(Identifier("goal-exercise"), "운동", Duration(30 * 60_000L)),
                        accumulatedDuration = Duration(0),
                        progressPercent = 0.0
                    )
                )
            )
        )
    }

    override suspend fun createGoal(name: String, targetDuration: Duration): ActionResult {
        delay(simulatedDelay)
        return ActionResult.Success
    }

    // --- UI-07: 리포트 ---
    override suspend fun readReport(period: ReportPeriod): ScreenState {
        delay(simulatedDelay)
        val summary = data.weeklySummary
        return ScreenState.Content(
            ReportData(
                period = period,
                timeRange = TimeRange(data.day1_monday.date, data.day7_sunday.date + 86_400_000),
                metrics = TimeMetrics(
                    wasteTime = Duration(summary.totalWasteMinutes * 60_000L),
                    baselineWeekly = Duration(summary.averageDailyWasteMinutes * 7L * 60_000),
                    savedTime = null,
                    recoveredTime = Duration(90 * 60_000L), // 코딩 공부 90분
                    recoveryRate = null
                ),
                goalSummaries = listOf(
                    GoalProgress(
                        goal = Goal(Identifier("goal-coding"), "코딩 공부", Duration(120 * 60_000L)),
                        accumulatedDuration = Duration(90 * 60_000L),
                        progressPercent = 75.0
                    ),
                    GoalProgress(
                        goal = Goal(Identifier("goal-interview"), "면접 준비", Duration(60 * 60_000L)),
                        accumulatedDuration = Duration(25 * 60_000L),
                        progressPercent = 41.7
                    )
                ),
                baselineComparable = false // 첫 주 관찰이므로 아직 비교 불가
            )
        )
    }

    // --- UI-08: 데이터 관리 ---
    override suspend fun readDataManagementState(): ScreenState {
        delay(simulatedDelay)
        return ScreenState.Content(
            DataManagementViewData(
                anonymousIdReady = true,
                backupStatus = BackupStatus(pendingCount = 0, lastSuccessAt = System.currentTimeMillis(), isOffline = false),
                retentionSelection = null,
                deletionJob = null
            )
        )
    }

    override suspend fun changeRetentionSelection(selection: RetentionSelection): ActionResult {
        delay(simulatedDelay)
        return ActionResult.Success
    }

    override suspend fun requestFullDeletion(): ActionResult {
        delay(simulatedDelay)
        return ActionResult.Success
    }

    override suspend fun confirmFullDeletion(): ActionResult {
        delay(simulatedDelay)
        return ActionResult.Success
    }

    override suspend fun refreshDeletionStatus(jobId: Identifier): ScreenState {
        delay(simulatedDelay)
        return ScreenState.Content(
            DataManagementViewData(
                anonymousIdReady = true,
                backupStatus = BackupStatus(pendingCount = 0, lastSuccessAt = null, isOffline = false),
                retentionSelection = null,
                deletionJob = DeletionJobStatus(jobId, deviceCompleted = true, serverCompleted = true)
            )
        )
    }

    // --- Helper ---
    private fun findDayByDate(date: Long): DayData? {
        return data.weeklySummary.days.minByOrNull { kotlin.math.abs(it.date - date) }
    }
}
