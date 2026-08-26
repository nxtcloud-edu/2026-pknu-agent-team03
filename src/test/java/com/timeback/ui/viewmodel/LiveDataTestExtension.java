package com.timeback.ui.viewmodel;

import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.arch.core.executor.TaskExecutor;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/** Executes LiveData work synchronously without requiring an Android main Looper. */
public final class LiveDataTestExtension implements BeforeEachCallback, AfterEachCallback {
    private static final TaskExecutor SYNCHRONOUS = new TaskExecutor() {
        @Override
        public void executeOnDiskIO(Runnable runnable) {
            runnable.run();
        }

        @Override
        public void postToMainThread(Runnable runnable) {
            runnable.run();
        }

        @Override
        public boolean isMainThread() {
            return true;
        }
    };

    @Override
    public void beforeEach(ExtensionContext context) {
        ArchTaskExecutor.getInstance().setDelegate(SYNCHRONOUS);
    }

    @Override
    public void afterEach(ExtensionContext context) {
        ArchTaskExecutor.getInstance().setDelegate(null);
    }
}
