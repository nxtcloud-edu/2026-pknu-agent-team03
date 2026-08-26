package com.timeback.backup.http;

import com.timeback.backup.contracts.BackupBatch;
import com.timeback.backup.contracts.BackupBatchResponse;
import com.timeback.backup.contracts.DeletionRequest;
import com.timeback.backup.contracts.DeletionStatusResponse;
import com.timeback.backup.contracts.RetentionApplyResult;
import com.timeback.backup.contracts.RetentionRequest;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;

interface BackupHttpApi {
    @POST("api/backup")
    Call<BackupBatchResponse> submitBackup(@Body BackupBatch batch);

    @POST("api/deletion")
    Call<DeletionStatusResponse> requestDeletion(@Body DeletionRequest request);

    @GET("api/deletion/status")
    Call<DeletionStatusResponse> readDeletionStatus(
            @Query("anonymousUserId") String anonymousUserId,
            @Query("jobId") String jobId
    );

    @PUT("api/retention")
    Call<RetentionApplyResult> applyRetention(@Body RetentionRequest request);
}
