package com.timeback.ui.domain.model;

import androidx.annotation.Nullable;
import java.util.List;

/**
 * CT-04 §3.2 공통 화면 상태 — sealed hierarchy를 Java abstract class + static subclasses로 구현
 */
public abstract class ScreenState {

    private ScreenState() {}

    public static final class Initial extends ScreenState {}

    public static final class Loading extends ScreenState {}

    public static final class Refreshing extends ScreenState {
        private final Object currentData;
        public Refreshing(Object currentData) { this.currentData = currentData; }
        public Object getCurrentData() { return currentData; }
    }

    public static final class Content extends ScreenState {
        private final Object data;
        public Content(Object data) { this.data = data; }
        public Object getData() { return data; }
    }

    public static final class Empty extends ScreenState {}

    public static final class Blocked extends ScreenState {
        private final BlockReason reason;
        public Blocked(BlockReason reason) { this.reason = reason; }
        public BlockReason getReason() { return reason; }
    }

    public static final class RetryableError extends ScreenState {
        private final ErrorReason reason;
        @Nullable private final Object cachedData;
        public RetryableError(ErrorReason reason) { this.reason = reason; this.cachedData = null; }
        public RetryableError(ErrorReason reason, @Nullable Object cachedData) { this.reason = reason; this.cachedData = cachedData; }
        public ErrorReason getReason() { return reason; }
        @Nullable public Object getCachedData() { return cachedData; }
    }

    public static final class PartialFailure extends ScreenState {
        private final List<String> successes;
        private final List<String> failures;
        public PartialFailure(List<String> successes, List<String> failures) {
            this.successes = successes;
            this.failures = failures;
        }
        public List<String> getSuccesses() { return successes; }
        public List<String> getFailures() { return failures; }
    }

    public static final class Error extends ScreenState {
        private final ErrorReason reason;
        public Error(ErrorReason reason) { this.reason = reason; }
        public ErrorReason getReason() { return reason; }
    }
}
