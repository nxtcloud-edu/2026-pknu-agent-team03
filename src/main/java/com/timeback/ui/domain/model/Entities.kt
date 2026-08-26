package com.timeback.ui.domain.model

/**
 * CT-01, CT-02 논리 엔터티 — domain-entities.md 기준
 * UI 트랙은 이 데이터를 표시만 하고 계산하지 않는다.
 */

data class Identifier(val value: String)

data class TimeRange(
    val startAt: Long, // epoch millis
    val endAt: Long    // epoch millis, exclusive
)

data class Duration(val millis: Long) {
    val minutes: Long get() = millis / 60_000
    val hours: Double get() = millis / 3_600_000.0
}

// --- 앱 분류 ---

enum class AppClassification {
    PRODUCTIVE, LEISURE, WASTE, NEUTRAL
}

enum class ContextClassification {
    PRODUCTIVE, LEISURE, WASTE, MIXED, NEUTRAL
}

enum class ActivityType {
    EXERCISE, STUDY, DEVELOPMENT, READING, LEISURE, CUSTOM
}

enum class RecoveredMethod {
    TIMER, MANUAL
}

// --- Timeline 항목 ---

data class TimelineItem(
    val id: Identifier,
    val appName: String,
    val packageName: String?,
    val activityName: String?,
    val timeRange: TimeRange,
    val duration: Duration,
    val classification: ContextClassification,
    val isComplex: Boolean,
    val contextConfirmationRequired: Boolean
)

// --- 앱 관리 ---

data class AppInfo(
    val packageName: String,
    val appName: String,
    val classification: AppClassification
)

// --- 목표 ---

data class Goal(
    val id: Identifier,
    val name: String,
    val targetDuration: Duration
)

data class GoalProgress(
    val goal: Goal,
    val accumulatedDuration: Duration,
    val progressPercent: Double
)

// --- 시간 지표 ---

data class TimeMetrics(
    val wasteTime: Duration,
    val baselineWeekly: Duration?,
    val savedTime: Duration?,
    val recoveredTime: Duration?,
    val recoveryRate: Double? // null if baseline not ready
)

data class BaselineState(
    val isObserving: Boolean,
    val remainingDays: Int?,
    val weeklyBaseline: Duration?
)

// --- 백업·데이터 관리 ---

data class BackupStatus(
    val pendingCount: Int,
    val lastSuccessAt: Long?,
    val isOffline: Boolean
)

data class DeletionJobStatus(
    val jobId: Identifier,
    val deviceCompleted: Boolean,
    val serverCompleted: Boolean
)

data class RetentionSelection(val value: String)

// --- 리포트 ---

enum class ReportPeriod {
    DAILY, WEEKLY, MONTHLY
}

data class ReportData(
    val period: ReportPeriod,
    val timeRange: TimeRange,
    val metrics: TimeMetrics,
    val goalSummaries: List<GoalProgress>,
    val baselineComparable: Boolean
)

// --- 타이머 ---

enum class TimerState {
    IDLE, RUNNING, COMPLETING
}

data class RecoveryEntry(
    val selectedGoal: Goal?,
    val timerState: TimerState,
    val availableGoals: List<Goal>,
    val overlapRequiresRepresentative: Boolean
)
