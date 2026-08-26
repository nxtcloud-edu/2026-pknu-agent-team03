package com.timeback.device.contract;

public sealed interface AccessState permits AccessState.Granted, AccessState.Blocked, AccessState.Failure {
    long observedAtMillis();

    record Granted(long observedAtMillis) implements AccessState {}

    record Blocked(long observedAtMillis) implements AccessState {}

    record Failure(long observedAtMillis, String reason) implements AccessState {}
}
