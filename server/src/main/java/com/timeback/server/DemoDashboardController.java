package com.timeback.server;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@Profile("demo")
@RequestMapping("/demo-api")
final class DemoDashboardController {
    private static final int MAX_MINUTES = 10_080;
    private static final int MAX_ACTIVITY_LENGTH = 200;
    private static final int MAX_GOAL_NAME_LENGTH = 100;
    private final DemoDashboardState state;

    DemoDashboardController(DemoDashboardState state) {
        this.state = state;
    }

    @GetMapping("/timeline")
    Map<String, Object> timeline(@RequestParam(defaultValue = "6") int day) {
        if (day < 0 || day > 6) {
            throw badRequest("day must be 0-6");
        }
        return Map.of("day", day, "items", state.timeline(day));
    }

    @GetMapping("/timeline/all")
    Map<String, List<List<DemoDashboardState.TimelineItem>>> allTimeline() {
        return Map.of("days", state.allTimeline());
    }

    @PutMapping("/timeline/{itemId}/classify")
    DemoDashboardState.TimelineItem classify(
            @PathVariable String itemId,
            @RequestBody ClassificationUpdateRequest request
    ) {
        if (request == null || (request.classification() == null && request.activity() == null)) {
            throw badRequest("classification or activity is required");
        }
        if (request.classification() != null
                && !DemoDashboardState.CLASSIFICATIONS.contains(request.classification())) {
            throw badRequest("invalid classification");
        }
        if (request.activity() != null && request.activity().length() > MAX_ACTIVITY_LENGTH) {
            throw badRequest("activity is too long");
        }
        DemoDashboardState.TimelineItem updated = state.classify(
                itemId,
                request.classification(),
                request.activity()
        );
        if (updated == null) {
            throw notFound("timeline item not found");
        }
        return updated;
    }

    @GetMapping("/goals")
    List<DemoDashboardState.Goal> goals() {
        return state.goals();
    }

    @PostMapping("/goals")
    @ResponseStatus(HttpStatus.CREATED)
    DemoDashboardState.Goal addGoal(@RequestBody GoalCreateRequest request) {
        if (request == null || request.name() == null || request.name().isBlank()) {
            throw badRequest("goal name is required");
        }
        if (request.name().length() > MAX_GOAL_NAME_LENGTH) {
            throw badRequest("goal name is too long");
        }
        int targetMin = request.targetMin() == null ? 60 : request.targetMin();
        if (targetMin <= 0 || targetMin > MAX_MINUTES) {
            throw badRequest("target minutes must be 1-10080");
        }
        return state.addGoal(request.name().trim(), targetMin);
    }

    @PutMapping("/goals/{goalId}/record")
    DemoDashboardState.Goal recordGoal(
            @PathVariable String goalId,
            @RequestBody GoalRecordRequest request
    ) {
        if (request == null || request.minutes() == null
                || Math.abs((long) request.minutes()) > MAX_MINUTES) {
            throw badRequest("record minutes must be between -10080 and 10080");
        }
        DemoDashboardState.Goal updated = state.recordGoal(goalId, request.minutes());
        if (updated == null) {
            throw notFound("goal not found");
        }
        return updated;
    }

    @DeleteMapping("/goals/{goalId}")
    Map<String, String> deleteGoal(@PathVariable String goalId) {
        if (!state.deleteGoal(goalId)) {
            throw notFound("goal not found");
        }
        return Map.of("deleted", goalId);
    }

    @GetMapping("/metrics/weekly")
    DemoDashboardState.WeeklyMetrics weeklyMetrics() {
        return state.weeklyMetrics();
    }

    @GetMapping("/backup/status")
    DemoDashboardState.BackupState backupStatus() {
        return state.backupState();
    }

    @PostMapping("/backup/sync")
    DemoDashboardState.BackupState triggerSyntheticSync() {
        return state.triggerSyntheticSync();
    }

    @GetMapping("/retention")
    DemoDashboardState.RetentionState retention() {
        return state.retention();
    }

    @PutMapping("/retention")
    DemoDashboardState.RetentionState updateRetention(@RequestBody RetentionUpdateRequest request) {
        if (request == null || request.selection() == null
                || !DemoDashboardState.RETENTION_SELECTIONS.contains(request.selection())) {
            throw badRequest("invalid retention selection");
        }
        return state.updateRetention(request.selection());
    }

    @PostMapping("/deletion")
    DemoDashboardState.DeletionResult requestDeletion() {
        return state.deleteSyntheticServerData();
    }

    @PostMapping("/reset")
    Map<String, String> resetSyntheticData() {
        state.reset();
        return Map.of("status", "RESET");
    }

    private static ResponseStatusException badRequest(String reason) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, reason);
    }

    private static ResponseStatusException notFound(String reason) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, reason);
    }

    record ClassificationUpdateRequest(String classification, String activity) {}

    record GoalCreateRequest(String name, Integer targetMin) {}

    record GoalRecordRequest(Integer minutes) {}

    record RetentionUpdateRequest(String selection) {}
}
