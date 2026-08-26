package com.timeback.server;

import com.timeback.backup.contracts.BackupChange;
import com.timeback.backup.contracts.BackupChangeState;
import com.timeback.backup.contracts.BackupItemStatus;
import com.timeback.backup.contracts.DeletionStatus;
import com.timeback.backup.server.BackupStorage;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Objects;

/** H2/PostgreSQL-compatible persistence adapter for the SRV-01~SRV-03 rules. */
public final class JdbcBackupStorage extends BackupStorage {
    private final JdbcTemplate jdbc;
    private final TransactionTemplate transactions;

    public JdbcBackupStorage(JdbcTemplate jdbc, TransactionTemplate transactions) {
        this.jdbc = Objects.requireNonNull(jdbc, "jdbc");
        this.transactions = Objects.requireNonNull(transactions, "transactions");
    }

    @Override
    public BackupItemStatus storeBackupItem(String anonymousUserId, BackupChange change) {
        List<String> existing = jdbc.query(
                "SELECT status FROM backup_changes WHERE anonymous_user_id = ? AND change_id = ?",
                (result, row) -> result.getString("status"),
                anonymousUserId,
                change.getChangeId()
        );
        if (!existing.isEmpty()) {
            return BackupItemStatus.valueOf(existing.get(0));
        }
        try {
            jdbc.update(
                    "INSERT INTO backup_changes "
                            + "(change_id, anonymous_user_id, entity_type, entity_id, operation, occurred_at, status) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?)",
                    change.getChangeId(),
                    anonymousUserId,
                    change.getEntityType().name(),
                    change.getEntityId(),
                    change.getOperation().name(),
                    change.getOccurredAt(),
                    BackupItemStatus.ACCEPTED.name()
            );
            return BackupItemStatus.ACCEPTED;
        } catch (DuplicateKeyException collision) {
            return BackupItemStatus.FAILED;
        }
    }

    @Override
    public BackupChangeState getBackupStatus(String anonymousUserId, String changeId) {
        List<String> statuses = jdbc.query(
                "SELECT status FROM backup_changes WHERE anonymous_user_id = ? AND change_id = ?",
                (result, row) -> result.getString("status"),
                anonymousUserId,
                changeId
        );
        return statuses.isEmpty() ? null : BackupChangeState.valueOf(statuses.get(0));
    }

    @Override
    public void setRetention(String anonymousUserId, String selection) {
        jdbc.update(
                "MERGE INTO retention_settings (anonymous_user_id, selection, updated_at) "
                        + "KEY (anonymous_user_id) VALUES (?, ?, CURRENT_TIMESTAMP)",
                anonymousUserId,
                selection
        );
    }

    @Override
    public String getRetention(String anonymousUserId) {
        List<String> selections = jdbc.query(
                "SELECT selection FROM retention_settings WHERE anonymous_user_id = ?",
                (result, row) -> result.getString("selection"),
                anonymousUserId
        );
        return selections.isEmpty() ? null : selections.get(0);
    }

    @Override
    public DeletionStatus startDeletion(String anonymousUserId, String jobId) {
        return transactions.execute(status -> {
            List<String> existing = jdbc.query(
                    "SELECT server_status FROM deletion_jobs "
                            + "WHERE anonymous_user_id = ? AND job_id = ?",
                    (result, row) -> result.getString("server_status"),
                    anonymousUserId,
                    jobId
            );
            if (!existing.isEmpty()) {
                return DeletionStatus.valueOf(existing.get(0));
            }

            jdbc.update("DELETE FROM backup_changes WHERE anonymous_user_id = ?", anonymousUserId);
            jdbc.update("DELETE FROM retention_settings WHERE anonymous_user_id = ?", anonymousUserId);
            jdbc.update(
                    "INSERT INTO deletion_jobs "
                            + "(job_id, anonymous_user_id, requested_at, server_status, completed_at) "
                            + "VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)",
                    jobId,
                    anonymousUserId,
                    System.currentTimeMillis(),
                    DeletionStatus.COMPLETED.name()
            );
            return DeletionStatus.COMPLETED;
        });
    }

    @Override
    public DeletionStatus getDeletionStatus(String anonymousUserId, String jobId) {
        List<String> statuses = jdbc.query(
                "SELECT server_status FROM deletion_jobs WHERE anonymous_user_id = ? AND job_id = ?",
                (result, row) -> result.getString("server_status"),
                anonymousUserId,
                jobId
        );
        return statuses.isEmpty() ? null : DeletionStatus.valueOf(statuses.get(0));
    }

    @Override
    public void clear() {
        transactions.executeWithoutResult(status -> {
            jdbc.update("DELETE FROM deletion_jobs");
            jdbc.update("DELETE FROM backup_changes");
            jdbc.update("DELETE FROM retention_settings");
        });
    }
}
