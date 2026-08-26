package com.timeback.ui.fake

import com.timeback.ui.domain.model.*

/**
 * CT-06 §12 고정 화면 결과 — frontend-components.md FX-* 정의
 * FakeFeatureGateway가 반환하는 고정 데이터.
 */
object FixedResults {

    // --- UI-01 ---
    val permissionRequired = ScreenState.Blocked(BlockReason.PERMISSION_REQUIRED)
    val permissionReady = ScreenState.Content(
        PermissionViewData(
            usageAccessGranted = true,
            anonymousIdReady = true,
            canEnterMainScreens = true
        )
    )
    val identityUnavailable = ScreenState.Blocked(BlockReason.IDENTITY_UNAVAILABLE)

    // --- UI-02 ---
    val homeBaselineObserving = ScreenState.Content(
        HomeViewData(
            wasteTimeToday = Duration(7_200_000), // 2h
            baselineState = BaselineState(isObserving = true, remainingDays = 4, weeklyBaseline = null),
            savedTime = null,
            recoveredTime = null,
            recoveryRate = null,
            goalProgresses = emptyList()
        )
    )
    val homeContent = ScreenState.Content(
        HomeViewData(
            wasteTimeToday = Duration(3_600_000), // 1h
            baselineState = BaselineState(isObserving = false, remainingDays = null, weeklyBaseline = Duration(14_400_000)),
            savedTime = Duration(1_800_000), // 30min
            recoveredTime = Duration(2_400_000), // 40min
            recoveryRate = 133.3,
            goalProgresses = listOf(
                GoalProgress(
                    goal = Goal(Identifier("goal-1"), "독서", Duration(3_600_000)),
                    accumulatedDuration = Duration(2_400_000),
                    progressPercent = 66.7
                )
            )
        )
    )

    // --- UI-03 ---
    val timelineEmpty = ScreenState.Empty
    val timelineWithMixed = ScreenState.Content(
        TimelineViewData(
            date = 1724630400000, // sample date
            items = listOf(
                TimelineItem(
                    id = Identifier("item-1"),
                    appName = "YouTube",
                    packageName = "com.google.android.youtube",
                    activityName = null,
                    timeRange = TimeRange(1724630400000, 1724634000000),
                    duration = Duration(3_600_000),
                    classification = ContextClassification.MIXED,
                    isComplex = false,
                    contextConfirmationRequired = true
                ),
                TimelineItem(
                    id = Identifier("item-2"),
                    appName = "VS Code",
                    packageName = "com.microsoft.vscode",
                    activityName = "개발",
                    timeRange = TimeRange(1724634000000, 1724641200000),
                    duration = Duration(7_200_000),
                    classification = ContextClassification.PRODUCTIVE,
                    isComplex = true,
                    contextConfirmationRequired = false
                )
            )
        )
    )

    // --- UI-04 ---
    val appsContent = ScreenState.Content(
        AppManagementViewData(
            apps = listOf(
                AppInfo("com.google.android.youtube", "YouTube", AppClassification.LEISURE),
                AppInfo("com.microsoft.vscode", "VS Code", AppClassification.PRODUCTIVE),
                AppInfo("com.instagram.android", "Instagram", AppClassification.WASTE),
                AppInfo("com.android.chrome", "Chrome", AppClassification.NEUTRAL)
            )
        )
    )
    val appsReadFailure = ScreenState.RetryableError(ErrorReason.DATA_ACCESS_FAILURE)

    // --- UI-05 ---
    val recoveryNoGoal = ScreenState.Empty
    val recoveryGoalOverlap = ScreenState.Content(
        RecoveryViewData(
            entry = RecoveryEntry(
                selectedGoal = Goal(Identifier("goal-1"), "독서", Duration(3_600_000)),
                timerState = TimerState.IDLE,
                availableGoals = listOf(
                    Goal(Identifier("goal-1"), "독서", Duration(3_600_000)),
                    Goal(Identifier("goal-2"), "운동", Duration(1_800_000))
                ),
                overlapRequiresRepresentative = true
            )
        )
    )

    // --- UI-06 ---
    val goalsContent = ScreenState.Content(
        GoalsViewData(
            goals = listOf(
                GoalProgress(
                    goal = Goal(Identifier("goal-1"), "독서", Duration(3_600_000)),
                    accumulatedDuration = Duration(2_400_000),
                    progressPercent = 66.7
                ),
                GoalProgress(
                    goal = Goal(Identifier("goal-2"), "운동", Duration(1_800_000)),
                    accumulatedDuration = Duration(900_000),
                    progressPercent = 50.0
                )
            )
        )
    )

    // --- UI-07 ---
    val reportNoData = ScreenState.Empty
    val reportContent = ScreenState.Content(
        ReportData(
            period = ReportPeriod.WEEKLY,
            timeRange = TimeRange(1724025600000, 1724630400000),
            metrics = TimeMetrics(
                wasteTime = Duration(25_200_000), // 7h
                baselineWeekly = Duration(36_000_000), // 10h
                savedTime = Duration(10_800_000), // 3h
                recoveredTime = Duration(7_200_000), // 2h
                recoveryRate = 66.7
            ),
            goalSummaries = listOf(
                GoalProgress(
                    goal = Goal(Identifier("goal-1"), "독서", Duration(3_600_000)),
                    accumulatedDuration = Duration(5_400_000),
                    progressPercent = 150.0
                )
            ),
            baselineComparable = true
        )
    )

    // --- UI-08 ---
    val dataManagementBackupPending = ScreenState.Content(
        DataManagementViewData(
            anonymousIdReady = true,
            backupStatus = BackupStatus(pendingCount = 3, lastSuccessAt = 1724620000000, isOffline = false),
            retentionSelection = null,
            deletionJob = null
        )
    )
    val dataManagementOffline = ScreenState.RetryableError(ErrorReason.OFFLINE)
    val dataManagementDeletePartial = ScreenState.PartialFailure(
        successes = listOf("device"),
        failures = listOf("server")
    )
    val dataManagementDeleteComplete = ScreenState.Content(
        DataManagementViewData(
            anonymousIdReady = true,
            backupStatus = BackupStatus(pendingCount = 0, lastSuccessAt = null, isOffline = false),
            retentionSelection = null,
            deletionJob = DeletionJobStatus(
                jobId = Identifier("del-1"),
                deviceCompleted = true,
                serverCompleted = true
            )
        )
    )
}

// --- View Data classes for each screen ---

data class PermissionViewData(
    val usageAccessGranted: Boolean,
    val anonymousIdReady: Boolean,
    val canEnterMainScreens: Boolean
)

data class HomeViewData(
    val wasteTimeToday: Duration,
    val baselineState: BaselineState,
    val savedTime: Duration?,
    val recoveredTime: Duration?,
    val recoveryRate: Double?,
    val goalProgresses: List<GoalProgress>
)

data class TimelineViewData(
    val date: Long,
    val items: List<TimelineItem>
)

data class AppManagementViewData(
    val apps: List<AppInfo>
)

data class RecoveryViewData(
    val entry: RecoveryEntry
)

data class GoalsViewData(
    val goals: List<GoalProgress>
)

data class DataManagementViewData(
    val anonymousIdReady: Boolean,
    val backupStatus: BackupStatus,
    val retentionSelection: RetentionSelection?,
    val deletionJob: DeletionJobStatus?
)
