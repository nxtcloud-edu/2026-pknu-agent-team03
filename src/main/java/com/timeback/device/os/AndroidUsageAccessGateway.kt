package com.timeback.device.os

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.timeback.device.contract.UsageAccessGateway
import com.timeback.device.contract.UsageAccessStatus

class AndroidUsageAccessGateway(
    private val context: Context,
) : UsageAccessGateway {
    override fun readCurrentStatus(): UsageAccessStatus {
        val appOps = context.getSystemService(AppOpsManager::class.java)
        val mode = appOps.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            context.applicationInfo.uid,
            context.packageName,
        )
        return if (mode == AppOpsManager.MODE_ALLOWED) {
            UsageAccessStatus.GRANTED
        } else {
            UsageAccessStatus.NOT_GRANTED
        }
    }

    override fun openUsageAccessSettings(): Boolean = try {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        true
    } catch (_: RuntimeException) {
        false
    }
}
