package com.timeback.ui.domain.model

/**
 * CT-04 §3.3 사용자 작업 결과 — frontend-components.md 매핑
 */

sealed interface ActionResult {
    data object Success : ActionResult
    data class Blocked(val reason: BlockReason) : ActionResult
    data class RetryableFailure(val reason: ErrorReason) : ActionResult
    data class PartialFailure(val details: Map<String, Boolean>) : ActionResult
    data class Failure(val reason: ErrorReason) : ActionResult
}
