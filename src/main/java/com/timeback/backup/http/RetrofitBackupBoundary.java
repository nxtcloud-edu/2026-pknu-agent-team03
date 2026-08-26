package com.timeback.backup.http;

import com.timeback.backup.contracts.BackupBatch;
import com.timeback.backup.contracts.BackupBatchResponse;
import com.timeback.backup.contracts.BackupItemResult;
import com.timeback.backup.contracts.BackupItemStatus;
import com.timeback.backup.contracts.DeletionRequest;
import com.timeback.backup.contracts.DeletionStatus;
import com.timeback.backup.contracts.DeletionStatusResponse;
import com.timeback.backup.contracts.RetentionApplyResult;
import com.timeback.backup.contracts.RetentionRequest;
import com.timeback.backup.contracts.RetentionServerStatus;
import com.timeback.backup.port.BackupBoundary;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.util.Objects;

/** Synchronous CT-05 adapter intended for execution on the app background executor. */
public final class RetrofitBackupBoundary implements BackupBoundary {
    private final BackupHttpApi api;

    public RetrofitBackupBoundary(String baseUrl) {
        this(new Retrofit.Builder()
                .baseUrl(Objects.requireNonNull(baseUrl, "baseUrl"))
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(BackupHttpApi.class));
    }

    RetrofitBackupBoundary(BackupHttpApi api) {
        this.api = Objects.requireNonNull(api, "api");
    }

    @Override
    public BackupBatchResponse submitBackup(BackupBatch batch) {
        try {
            Response<BackupBatchResponse> response = api.submitBackup(batch).execute();
            if (response.isSuccessful() && response.body() != null) {
                return response.body();
            }
            return retryableBatch(batch, "http_" + response.code());
        } catch (IOException error) {
            return retryableBatch(batch, "network_unavailable");
        }
    }

    @Override
    public DeletionStatusResponse requestDeletion(DeletionRequest request) {
        try {
            Response<DeletionStatusResponse> response = api.requestDeletion(request).execute();
            return response.isSuccessful() && response.body() != null
                    ? response.body()
                    : new DeletionStatusResponse(request.getJobId(), DeletionStatus.FAILED);
        } catch (IOException error) {
            return new DeletionStatusResponse(request.getJobId(), DeletionStatus.FAILED);
        }
    }

    @Override
    public DeletionStatusResponse readDeletionStatus(String anonymousUserId, String jobId) {
        try {
            Response<DeletionStatusResponse> response = api
                    .readDeletionStatus(anonymousUserId, jobId)
                    .execute();
            return response.isSuccessful() && response.body() != null
                    ? response.body()
                    : new DeletionStatusResponse(jobId, DeletionStatus.FAILED);
        } catch (IOException error) {
            return new DeletionStatusResponse(jobId, DeletionStatus.FAILED);
        }
    }

    @Override
    public RetentionApplyResult applyRetention(RetentionRequest request) {
        try {
            Response<RetentionApplyResult> response = api.applyRetention(request).execute();
            return response.isSuccessful() && response.body() != null
                    ? response.body()
                    : failedRetention(request);
        } catch (IOException error) {
            return failedRetention(request);
        }
    }

    private static BackupBatchResponse retryableBatch(BackupBatch batch, String error) {
        return new BackupBatchResponse(batch.getChanges().stream()
                .map(change -> new BackupItemResult(
                        change.getChangeId(),
                        BackupItemStatus.RETRYABLE_FAILURE,
                        error
                ))
                .toList());
    }

    private static RetentionApplyResult failedRetention(RetentionRequest request) {
        return new RetentionApplyResult(
                request.getRetentionSelection(),
                "PENDING",
                RetentionServerStatus.FAILED
        );
    }
}
