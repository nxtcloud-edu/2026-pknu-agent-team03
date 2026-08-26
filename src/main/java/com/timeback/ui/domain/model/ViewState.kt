package com.timeback.ui.domain.model

/**
 * CT-04 §3.2 공통 화면 상태 — frontend-components.md 매핑
 * 모든 화면의 ViewState는 이 sealed interface를 기반으로 화면별로 확장한다.
 */

sealed interface ScreenState {
    data object Initial : ScreenState
    data object Loading : ScreenState
    data class Refreshing<T>(val currentData: T) : ScreenState
    data class Content<T>(val data: T) : ScreenState
    data object Empty : ScreenState
    data class Blocked(val reason: BlockReason) : ScreenState
    data class RetryableError(val reason: ErrorReason, val cachedData: Any? = null) : ScreenState
    data class PartialFailure(val successes: List<String>, val failures: List<String>) : ScreenState
    data class Error(val reason: ErrorReason) : ScreenState
}

enum class BlockReason {
    PERMISSION_REQUIRED,
    IDENTITY_UNAVAILABLE,
    CONTEXT_CONFIRMATION_REQUIRED,
    REPRESENTATIVE_GOAL_REQUIRED,
    INVALID_TIME_RANGE
}

enum class ErrorReason {
    PERMISSION_CHECK_FAILED,
    DATA_ACCESS_FAILURE,
    OFFLINE,
    REMOTE_RETRY_REQUIRED,
    DELETION_INCOMPLETE,
    UNKNOWN
}
