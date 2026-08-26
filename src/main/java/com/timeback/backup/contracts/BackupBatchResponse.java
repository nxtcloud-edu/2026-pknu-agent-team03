package com.timeback.backup.contracts;

import java.util.List;

/**
 * 서버 응답: BackupBatch 전체에 대한 항목별 결과.
 */
public class BackupBatchResponse {
    private final List<BackupItemResult> results;

    public BackupBatchResponse(List<BackupItemResult> results) {
        this.results = results;
    }

    public List<BackupItemResult> getResults() { return results; }
}
