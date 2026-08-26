package com.timeback.device.access

import com.timeback.device.contract.AccessState
import com.timeback.device.contract.SettingsOpenResult
import com.timeback.device.contract.TimeSource
import com.timeback.device.contract.UsageAccessGateway
import com.timeback.device.contract.UsageAccessStatus

class AccessGate(
    private val gateway: UsageAccessGateway,
    private val timeSource: TimeSource,
) {
    fun readAccessState(): AccessState {
        val observedAt = timeSource.nowMillis()
        return try {
            when (gateway.readCurrentStatus()) {
                UsageAccessStatus.GRANTED -> AccessState.Granted(observedAt)
                UsageAccessStatus.NOT_GRANTED -> AccessState.Blocked(observedAt)
            }
        } catch (error: RuntimeException) {
            AccessState.Failure(observedAt, error.message ?: "usage access status unavailable")
        }
    }

    fun openUsageAccessSettings(): SettingsOpenResult = try {
        if (gateway.openUsageAccessSettings()) {
            SettingsOpenResult.Opened
        } else {
            SettingsOpenResult.Failure("usage access settings could not be opened")
        }
    } catch (error: RuntimeException) {
        SettingsOpenResult.Failure(error.message ?: "usage access settings unavailable")
    }
}
