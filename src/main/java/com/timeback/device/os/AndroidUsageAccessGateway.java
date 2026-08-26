package com.timeback.device.os;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import com.timeback.device.contract.UsageAccessGateway;
import com.timeback.device.contract.UsageAccessStatus;

import java.util.Objects;

public final class AndroidUsageAccessGateway implements UsageAccessGateway {
    private final Context context;

    public AndroidUsageAccessGateway(Context context) {
        this.context = context;
    }

    @Override
    public UsageAccessStatus readCurrentStatus() {
        AppOpsManager appOps = Objects.requireNonNull(
                context.getSystemService(AppOpsManager.class),
                "AppOpsManager is unavailable"
        );
        int mode = appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                context.getApplicationInfo().uid,
                context.getPackageName()
        );
        return mode == AppOpsManager.MODE_ALLOWED
                ? UsageAccessStatus.GRANTED
                : UsageAccessStatus.NOT_GRANTED;
    }

    @Override
    public boolean openUsageAccessSettings() {
        try {
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            return true;
        } catch (RuntimeException error) {
            return false;
        }
    }
}
