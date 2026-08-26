package com.timeback.ui.domain.model;

import java.util.Map;

/**
 * CT-04 §3.3 사용자 작업 결과
 */
public abstract class ActionResult {

    private ActionResult() {}

    public static final class Success extends ActionResult {}

    public static final class Blocked extends ActionResult {
        private final BlockReason reason;
        public Blocked(BlockReason reason) { this.reason = reason; }
        public BlockReason getReason() { return reason; }
    }

    public static final class RetryableFailure extends ActionResult {
        private final ErrorReason reason;
        public RetryableFailure(ErrorReason reason) { this.reason = reason; }
        public ErrorReason getReason() { return reason; }
    }

    public static final class PartialFailure extends ActionResult {
        private final Map<String, Boolean> details;
        public PartialFailure(Map<String, Boolean> details) { this.details = details; }
        public Map<String, Boolean> getDetails() { return details; }
    }

    public static final class Failure extends ActionResult {
        private final ErrorReason reason;
        public Failure(ErrorReason reason) { this.reason = reason; }
        public ErrorReason getReason() { return reason; }
    }
}
