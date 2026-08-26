package com.timeback.ui.integration;

import com.timeback.backup.contracts.BackupBatch;
import com.timeback.backup.contracts.BackupChange;
import com.timeback.backup.contracts.BackupItemStatus;
import com.timeback.backup.contracts.DeletionRequest;
import com.timeback.backup.contracts.DeletionStatus;
import com.timeback.backup.contracts.EntityOperation;
import com.timeback.backup.contracts.EntityType;
import com.timeback.backup.contracts.RetentionRequest;
import com.timeback.backup.contracts.RetentionServerStatus;
import com.timeback.backup.http.RetrofitBackupBoundary;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RetrofitBackupBoundaryTest {
    private MockWebServer server;
    private RetrofitBackupBoundary boundary;

    @BeforeEach
    void startServer() throws IOException {
        server = new MockWebServer();
        server.start();
        boundary = new RetrofitBackupBoundary(server.url("/").toString());
    }

    @AfterEach
    void stopServer() throws IOException {
        server.shutdown();
    }

    @Test
    void failedRequestRemainsRetryableThenAcceptsStableChangeId() throws Exception {
        BackupBatch batch = new BackupBatch("anonymous-test", List.of(new BackupChange(
                "stable-change-1",
                EntityType.APP_SESSION,
                "session-1",
                EntityOperation.CREATE,
                1_000
        )));
        server.enqueue(new MockResponse().setResponseCode(503));
        server.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("{\"results\":[{\"changeId\":\"stable-change-1\",\"status\":\"ACCEPTED\",\"error\":null}]}"));

        var failed = boundary.submitBackup(batch);
        var accepted = boundary.submitBackup(batch);

        assertEquals(BackupItemStatus.RETRYABLE_FAILURE, failed.getResults().get(0).getStatus());
        assertEquals("stable-change-1", accepted.getResults().get(0).getChangeId());
        assertEquals(BackupItemStatus.ACCEPTED, accepted.getResults().get(0).getStatus());
        String firstBody = server.takeRequest().getBody().readUtf8();
        String secondBody = server.takeRequest().getBody().readUtf8();
        assertTrue(firstBody.contains("stable-change-1"));
        assertEquals(firstBody, secondBody);
    }

    @Test
    void deletionAndRetentionFailClosedOnNetworkErrors() {
        server.enqueue(new MockResponse().setResponseCode(500));
        server.enqueue(new MockResponse().setResponseCode(500));

        assertEquals(
                DeletionStatus.FAILED,
                boundary.requestDeletion(new DeletionRequest("anonymous-test", "job-1"))
                        .getServerStatus()
        );
        assertEquals(
                RetentionServerStatus.FAILED,
                boundary.applyRetention(new RetentionRequest("anonymous-test", "DAYS_30"))
                        .getServerStatus()
        );
    }
}
