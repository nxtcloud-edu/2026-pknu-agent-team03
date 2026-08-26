package com.timeback.server;

import com.timeback.backup.contracts.BackupBatch;
import com.timeback.backup.contracts.BackupBatchResponse;
import com.timeback.backup.contracts.BackupChange;
import com.timeback.backup.contracts.BackupChangeState;
import com.timeback.backup.contracts.DeletionRequest;
import com.timeback.backup.contracts.DeletionStatusResponse;
import com.timeback.backup.contracts.EntityOperation;
import com.timeback.backup.contracts.EntityType;
import com.timeback.backup.contracts.RetentionApplyResult;
import com.timeback.backup.contracts.RetentionRequest;
import com.timeback.backup.server.BackupServer;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class BackupHttpController {
    private static final int MAX_BATCH_SIZE = 1_000;
    private final BackupServer server;

    public BackupHttpController(BackupServer server) {
        this.server = server;
    }

    @PostMapping("/backup")
    public BackupBatchResponse submitBackup(@RequestBody BackupSubmitRequest request) {
        requireUser(request.anonymousUserId());
        if (request.changes() == null || request.changes().size() > MAX_BATCH_SIZE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid backup batch size");
        }
        List<BackupChange> changes = request.changes().stream()
                .map(BackupChangeRequest::toContract)
                .toList();
        return server.handleBackupSubmit(new BackupBatch(request.anonymousUserId(), changes));
    }

    @GetMapping("/backup/status")
    public Map<String, BackupChangeState> readBackupStatus(
            @RequestParam String anonymousUserId,
            @RequestParam List<String> changeId
    ) {
        requireUser(anonymousUserId);
        return server.handleBackupStatusQuery(anonymousUserId, changeId);
    }

    @PutMapping("/retention")
    public RetentionApplyResult applyRetention(@RequestBody RetentionHttpRequest request) {
        requireUser(request.anonymousUserId());
        if (request.retentionSelection() == null || request.retentionSelection().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "retention selection is required");
        }
        return server.handleRetentionApply(new RetentionRequest(
                request.anonymousUserId(),
                request.retentionSelection()
        ));
    }

    @PostMapping("/deletion")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public DeletionStatusResponse requestDeletion(@RequestBody DeletionHttpRequest request) {
        requireUser(request.anonymousUserId());
        if (request.jobId() == null || request.jobId().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "job id is required");
        }
        return server.handleDeletionRequest(new DeletionRequest(
                request.anonymousUserId(),
                request.jobId()
        ));
    }

    @GetMapping("/deletion/status")
    public DeletionStatusResponse readDeletionStatus(
            @RequestParam String anonymousUserId,
            @RequestParam String jobId
    ) {
        requireUser(anonymousUserId);
        return server.handleDeletionStatusQuery(anonymousUserId, jobId);
    }

    private static void requireUser(String anonymousUserId) {
        if (anonymousUserId == null || anonymousUserId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "anonymous user id is required");
        }
    }

    public record BackupSubmitRequest(String anonymousUserId, List<BackupChangeRequest> changes) {}

    public record BackupChangeRequest(
            String changeId,
            EntityType entityType,
            String entityId,
            EntityOperation operation,
            long occurredAt
    ) {
        BackupChange toContract() {
            if (changeId == null || changeId.isBlank() || entityType == null
                    || entityId == null || entityId.isBlank() || operation == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid backup change");
            }
            return new BackupChange(changeId, entityType, entityId, operation, occurredAt);
        }
    }

    public record RetentionHttpRequest(String anonymousUserId, String retentionSelection) {}

    public record DeletionHttpRequest(String anonymousUserId, String jobId) {}
}
