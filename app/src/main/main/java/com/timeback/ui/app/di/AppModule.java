package com.timeback.ui.app.di;

import com.timeback.ui.domain.gateway.FeatureGateway;
import com.timeback.ui.fake.FakeFeatureGateway;

import javax.inject.Singleton;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

/**
 * Hilt DI 모듈 — FakeFeatureGateway를 FeatureGateway로 제공.
 * 프로덕션에서는 실제 구현으로 교체한다.
 */
@Module
@InstallIn(SingletonComponent.class)
public class AppModule {

    @Provides
    @Singleton
    public FeatureGateway provideFeatureGateway() {
        return new MockDataGateway();
    }
}
