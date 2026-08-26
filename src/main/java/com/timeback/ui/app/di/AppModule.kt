package com.timeback.ui.app.di

import com.timeback.ui.domain.gateway.FeatureGateway
import com.timeback.ui.fake.FakeFeatureGateway
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt DI 모듈
 * 개발 중에는 FakeFeatureGateway를 제공하고,
 * 실제 구현이 완료되면 @TestInstallIn 또는 Flavor로 교체한다.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFeatureGateway(): FeatureGateway {
        return FakeFeatureGateway()
    }
}
