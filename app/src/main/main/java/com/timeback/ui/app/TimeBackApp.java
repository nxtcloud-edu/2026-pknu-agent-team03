package com.timeback.ui.app;

import android.app.Application;
import dagger.hilt.android.HiltAndroidApp;

/**
 * TimeBack Application entry point.
 * Hilt가 컴포넌트 트리를 생성하는 시작점.
 */
@HiltAndroidApp
public class TimeBackApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
