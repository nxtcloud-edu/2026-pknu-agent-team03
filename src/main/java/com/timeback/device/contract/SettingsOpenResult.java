package com.timeback.device.contract;

public sealed interface SettingsOpenResult permits SettingsOpenResult.Opened, SettingsOpenResult.Failure {
    enum Opened implements SettingsOpenResult {
        INSTANCE
    }

    record Failure(String reason) implements SettingsOpenResult {}
}
