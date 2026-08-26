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
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("demo")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DemoDashboardIntegrationTest {
    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate http;

    @Autowired
    private ObjectMapper json;

    @Autowired
    private DemoDashboardState state;

    @BeforeEach
    void resetState() {
        state.reset();
    }

    @Test
    void staticDashboardAndTimelineAreServedByTheDemoProfile() throws Exception {
        ResponseEntity<String> page = http.getForEntity(url("/demo/index.html"), String.class);
        assertThat(page.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(page.getBody()).contains(
                "TimeBack 합성 데이터 대시보드",
                "const API='/demo-api'",
                "id=\"error-banner\"",
                "data-action=\"reset-demo\"",
                "role=\"tablist\""
        );
        assertThat(page.getBody()).doesNotContain("@import url", "https://cdn.jsdelivr.net");

        JsonNode timeline = getJson("/demo-api/timeline?day=0");
        assertThat(timeline.get("items").size()).isEqualTo(7);

        ResponseEntity<String> updated = http.exchange(
                url("/demo-api/timeline/d1-1/classify"),
                HttpMethod.PUT,
                new HttpEntity<>(Map.of("classification", "PRODUCTIVE", "activity", "검증 활동")),
                String.class
        );
        assertThat(updated.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(json.readTree(updated.getBody()).get("classification").asText())
                .isEqualTo("PRODUCTIVE");
        assertThat(json.readTree(updated.getBody()).get("userConfirmed").asBoolean()).isTrue();

        assertThat(http.getForEntity(url("/demo-api/timeline?day=7"), String.class).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void goalAndRetentionInputsFailClosed() {
        ResponseEntity<String> blankGoal = http.postForEntity(
                url("/demo-api/goals"),
                Map.of("name", " ", "targetMin", 60),
                String.class
        );
        assertThat(blankGoal.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        ResponseEntity<String> created = http.postForEntity(
                url("/demo-api/goals"),
                Map.of("name", "새 목표", "targetMin", 90),
                String.class
        );
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<String> missingGoal = http.exchange(
                url("/demo-api/goals/missing/record"),
                HttpMethod.PUT,
                new HttpEntity<>(Map.of("minutes", 15)),
                String.class
        );
        assertThat(missingGoal.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        ResponseEntity<String> invalidRetention = http.exchange(
                url("/demo-api/retention"),
                HttpMethod.PUT,
                new HttpEntity<>(Map.of("selection", "FOREVER")),
                String.class
        );
        assertThat(invalidRetention.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        ResponseEntity<String> missingRetention = http.exchange(
                url("/demo-api/retention"),
                HttpMethod.PUT,
                new HttpEntity<>(Map.of()),
                String.class
        );
        assertThat(missingRetention.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void demoDeletionCompletesOnlyServerSideSyntheticData() throws Exception {
        ResponseEntity<String> deletion = http.postForEntity(
                url("/demo-api/deletion"),
                null,
                String.class
        );

        assertThat(deletion.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode result = json.readTree(deletion.getBody());
        assertThat(result.get("deviceStatus").asText()).isEqualTo("PENDING");
        assertThat(result.get("serverStatus").asText()).isEqualTo("COMPLETED");
        assertThat(getJson("/demo-api/goals").size()).isZero();
        assertThat(getJson("/demo-api/timeline/all").get("days"))
                .allSatisfy(day -> assertThat(day.size()).isZero());
        assertThat(getJson("/demo-api/backup/status").get("status").asText())
                .isEqualTo("DEMO_DELETED");

        ResponseEntity<String> reset = http.postForEntity(
                url("/demo-api/reset"),
                null,
                String.class
        );
        assertThat(reset.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(json.readTree(reset.getBody()).get("status").asText()).isEqualTo("RESET");
        assertThat(getJson("/demo-api/goals").size()).isEqualTo(4);
        assertThat(getJson("/demo-api/timeline?day=0").get("items").size()).isEqualTo(7);
    }

    private JsonNode getJson(String path) throws Exception {
        ResponseEntity<String> response = http.getForEntity(url(path), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        return json.readTree(response.getBody());
    }

    private String url(String path) {
        return "http://127.0.0.1:" + port + path;
    }
}
