package com.timeback.server;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BackupHttpIntegrationTest {
    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate http;

    @Autowired
    private ObjectMapper json;

    @Autowired
    private JdbcBackupStorage storage;

    @BeforeEach
    void resetStorage() {
        storage.clear();
    }

    @Test
    void backupIsIdempotentAndScopedByAnonymousUser() throws Exception {
        Map<String, Object> request = backupRequest("anonymous-a", "change-1");

        ResponseEntity<String> first = http.postForEntity(url("/api/backup"), request, String.class);
        ResponseEntity<String> retry = http.postForEntity(url("/api/backup"), request, String.class);

        assertThat(first.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(retry.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(first.getBody()).isEqualTo(retry.getBody());
        assertThat(json.readTree(first.getBody()).at("/results/0/status").asText())
                .isEqualTo("ACCEPTED");

        JsonNode ownerStatus = getJson(
                "/api/backup/status?anonymousUserId=anonymous-a&changeId=change-1"
        );
        JsonNode otherStatus = getJson(
                "/api/backup/status?anonymousUserId=anonymous-b&changeId=change-1"
        );
        assertThat(ownerStatus.get("change-1").asText()).isEqualTo("ACCEPTED");
        assertThat(otherStatus.get("change-1").asText()).isEqualTo("PENDING");
    }

    @Test
    void retentionAndDeletionUseTheSamePersistentUserScope() throws Exception {
        http.postForEntity(url("/api/backup"), backupRequest("anonymous-a", "change-delete"), String.class);
        Map<String, String> retention = Map.of(
                "anonymousUserId", "anonymous-a",
                "retentionSelection", "DAYS_30"
        );
        ResponseEntity<String> retentionResponse = http.exchange(
                url("/api/retention"),
                HttpMethod.PUT,
                new HttpEntity<>(retention),
                String.class
        );
        assertThat(retentionResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(storage.getRetention("anonymous-a")).isEqualTo("DAYS_30");

        Map<String, String> deletion = Map.of(
                "anonymousUserId", "anonymous-a",
                "jobId", "job-1"
        );
        ResponseEntity<String> first = http.postForEntity(url("/api/deletion"), deletion, String.class);
        ResponseEntity<String> retry = http.postForEntity(url("/api/deletion"), deletion, String.class);

        assertThat(first.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(retry.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(json.readTree(first.getBody()).get("serverStatus").asText()).isEqualTo("COMPLETED");
        assertThat(storage.getRetention("anonymous-a")).isNull();
        assertThat(getJson(
                "/api/backup/status?anonymousUserId=anonymous-a&changeId=change-delete"
        ).get("change-delete").asText()).isEqualTo("PENDING");
        assertThat(getJson(
                "/api/deletion/status?anonymousUserId=anonymous-a&jobId=job-1"
        ).get("serverStatus").asText()).isEqualTo("COMPLETED");
    }

    @Test
    void invalidIdentityAndOversizedInputFailClosed() {
        ResponseEntity<String> blankIdentity = http.postForEntity(
                url("/api/backup"),
                backupRequest("", "change-invalid"),
                String.class
        );
        assertThat(blankIdentity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    private JsonNode getJson(String path) throws Exception {
        ResponseEntity<String> response = http.getForEntity(url(path), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        return json.readTree(response.getBody());
    }

    private String url(String path) {
        return "http://127.0.0.1:" + port + path;
    }

    private static Map<String, Object> backupRequest(String user, String changeId) {
        return Map.of(
                "anonymousUserId", user,
                "changes", List.of(Map.of(
                        "changeId", changeId,
                        "entityType", "APP_SESSION",
                        "entityId", "session-1",
                        "operation", "CREATE",
                        "occurredAt", 1_000
                ))
        );
    }
}
