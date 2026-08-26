package com.timeback.ui.feature.datamanagement

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.timeback.ui.domain.model.*
import com.timeback.ui.fake.DataManagementViewData

/**
 * UI-08 데이터 관리 화면
 * 상태: CONTENT, LOADING, RETRYABLE_ERROR(OFFLINE), PARTIAL_FAILURE, ERROR
 */
@Composable
fun DataManagementScreen(
    viewModel: DataManagementViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val confirmationPending by viewModel.confirmationPending.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("데이터 관리", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))

        when (val current = state) {
            is ScreenState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is ScreenState.Content<*> -> {
                val data = current.data as? DataManagementViewData
                if (data != null) {
                    DataManagementContent(
                        data = data,
                        onChangeRetention = { viewModel.changeRetention(it) },
                        onRequestDeletion = { viewModel.requestFullDeletion() },
                        onRefreshDeletion = { jobId -> viewModel.refreshDeletionStatus(jobId) }
                    )
                }
            }
            is ScreenState.RetryableError -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("오프라인 상태입니다", style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(8.dp))
                    Text("로컬 기능은 계속 사용할 수 있습니다")
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(onClick = { viewModel.loadDataManagement() }) { Text("다시 시도") }
                }
            }
            is ScreenState.PartialFailure -> {
                Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                    Text("부분 실패", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(12.dp))
                    current.successes.forEach { Text("성공: $it") }
                    current.failures.forEach { Text("실패: $it") }
                    Spacer(Modifier.height(8.dp))
                    Text("전체 삭제가 완료되지 않았습니다", style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(onClick = { viewModel.loadDataManagement() }) { Text("상태 확인") }
                }
            }
            is ScreenState.Error -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("오류가 발생했습니다")
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(onClick = { viewModel.loadDataManagement() }) { Text("다시 시도") }
                }
            }
            else -> {}
        }

        // Confirmation dialog
        if (confirmationPending) {
            DeletionConfirmationDialog(
                onConfirm = { viewModel.confirmFullDeletion() },
                onCancel = { viewModel.cancelDeletion() }
            )
        }
    }
}

@Composable
private fun DataManagementContent(
    data: DataManagementViewData,
    onChangeRetention: (RetentionSelection) -> Unit,
    onRequestDeletion: () -> Unit,
    onRefreshDeletion: (Identifier) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Backup status
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("백업 상태", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                if (data.backupStatus.isOffline) {
                    Text("오프라인 — 대기 중")
                } else {
                    Text("대기 중: ${data.backupStatus.pendingCount}건")
                    if (data.backupStatus.lastSuccessAt != null) {
                        Text("마지막 성공: 완료", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        // Deletion job status
        if (data.deletionJob != null) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("전체 삭제 상태", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text("기기: ${if (data.deletionJob.deviceCompleted) "완료" else "진행 중"}")
                    Text("서버: ${if (data.deletionJob.serverCompleted) "완료" else "진행 중"}")
                    if (data.deletionJob.deviceCompleted && data.deletionJob.serverCompleted) {
                        Spacer(Modifier.height(8.dp))
                        Text("전체 삭제 완료", style = MaterialTheme.typography.titleSmall)
                    } else {
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(onClick = { onRefreshDeletion(data.deletionJob.jobId) }) {
                            Text("상태 새로고침")
                        }
                    }
                }
            }
        }

        // Danger zone
        if (data.deletionJob == null || !(data.deletionJob.deviceCompleted && data.deletionJob.serverCompleted)) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("전체 데이터 삭제", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "기기와 서버의 모든 데이터를 삭제합니다. 이 작업은 되돌릴 수 없습니다.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = onRequestDeletion,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("전체 삭제 요청")
                    }
                }
            }
        }
    }
}

@Composable
private fun DeletionConfirmationDialog(
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("전체 삭제 확인") },
        text = { Text("정말로 모든 데이터를 삭제하시겠습니까? 기기와 서버의 데이터가 모두 삭제됩니다.") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) { Text("삭제") }
        },
        dismissButton = {
            TextButton(onClick = onCancel) { Text("취소") }
        }
    )
}
