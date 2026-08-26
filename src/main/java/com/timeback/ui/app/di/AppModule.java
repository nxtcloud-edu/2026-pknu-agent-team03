package com.timeback.ui.app.di;

import android.content.Context;
import com.timeback.BuildConfig;
import com.timeback.backup.http.RetrofitBackupBoundary;
import com.timeback.backup.port.BackupBoundary;
import com.timeback.device.contract.UsageAccessGateway;
import com.timeback.device.contract.DeviceDataAuthority;
import com.timeback.device.os.AndroidUsageAccessGateway;
import com.timeback.device.room.RoomDeviceDataAuthority;
import com.timeback.device.room.TimeBackRoomDatabase;
import com.timeback.ui.domain.gateway.FeatureGateway;
import com.timeback.ui.integration.ProductionFeatureGateway;

import javax.inject.Singleton;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

/**
 * Production Hilt composition. Fake gateways are instantiated only by tests.
 */
@Module
@InstallIn(SingletonComponent.class)
public class AppModule {

    @Provides
    @Singleton
    public UsageAccessGateway provideUsageAccessGateway(@ApplicationContext Context context) {
        return new AndroidUsageAccessGateway(context);
    }

    @Provides
    @Singleton
    public TimeBackRoomDatabase provideRoomDatabase(@ApplicationContext Context context) {
        return TimeBackRoomDatabase.create(context);
    }

    @Provides
    @Singleton
    public DeviceDataAuthority provideDeviceDataAuthority(TimeBackRoomDatabase database) {
        return new RoomDeviceDataAuthority(database);
    }

    @Provides
    @Singleton
    public BackupBoundary provideBackupBoundary() {
        return new RetrofitBackupBoundary(BuildConfig.TIMEBACK_BACKUP_BASE_URL);
    }

    @Provides
    @Singleton
    public FeatureGateway provideFeatureGateway(UsageAccessGateway usageAccessGateway) {
        // OS-04 identity verification is still pending; do not invent a fallback identity.
        return new ProductionFeatureGateway(usageAccessGateway, false);
    }
}
