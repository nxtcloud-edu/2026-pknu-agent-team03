package com.timeback.ui.feature.goals

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.timeback.ui.domain.model.*
import com.timeback.ui.fake.GoalsViewData

/**
 * UI-06 목표 화면
 * 상태: LOADING, EMPTY, CONTENT, RETRYABLE_ERROR, ERROR
 */
@Composable
fun GoalsScreen(
    viewModel: GoalsViewModel = hiltViewModel(),
    onGoalDetail: (String) -> Unit = {},
    onRecoveryForGoal: (String) -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

    when (val current = state) {
        is ScreenState.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is ScreenState.Empty -> {
            EmptyGoalsContent(onCreateGoal = { showCreateDialog = true })
        }
        is ScreenState.Content<*> -> {
            val data = current.data as? GoalsViewData
            if (data != null) {
                GoalsListContent(
                    goals = data.goals,
                    onCreateGoal = { showCreateDialog = true },
                    onGoalDetail = onGoalDetail,
                    onRecoveryForGoal = onRecoveryForGoal
                )
            }
        }
        is ScreenState.RetryableError -> {
            RetryContent(onRetry = { viewModel.loadGoals() })
        }
        is ScreenState.Error -> {
            ErrorContent(onRetry = { viewModel.loadGoals() })
        }
        else -> {}
    }

    if (showCreateDialog) {
        CreateGoalDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name, minutes ->
                viewModel.createGoal(name, minutes)
                showCreateDialog = false
            }
        )
    }
}

@Composable
private fun EmptyGoalsContent(onCreateGoal: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("아직 목표가 없습니다", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onCreateGoal) {
            Text("첫 목표 만들기")
        }
    }
}

@Composable
private fun GoalsListContent(
    goals: List<GoalProgress>,
    onCreateGoal: () -> Unit,
    onGoalDetail: (String) -> Unit,
    onRecoveryForGoal: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("목표", style = MaterialTheme.typography.headlineSmall)
            Button(onClick = onCreateGoal) { Text("추가") }
        }
        Spacer(Modifier.height(16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(goals) { goalProgress ->
                GoalCard(
                    goalProgress = goalProgress,
                    onDetail = { onGoalDetail(goalProgress.goal.id.value) },
                    onRecovery = { onRecoveryForGoal(goalProgress.goal.id.value) }
                )
            }
        }
    }
}

@Composable
private fun GoalCard(
    goalProgress: GoalProgress,
    onDetail: () -> Unit,
    onRecovery: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onDetail
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(goalProgress.goal.name, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { (goalProgress.progressPercent / 100.0).toFloat().coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "${goalProgress.accumulatedDuration.minutes}분 / ${goalProgress.goal.targetDuration.minutes}분",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    "${goalProgress.progressPercent.toInt()}%",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = onRecovery) {
                Text("시간 되찾기")
            }
        }
    }
}

@Composable
private fun CreateGoalDialog(
    onDismiss: () -> Unit,
    onCreate: (String, Long) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var minutes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("새 목표") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("목표 이름") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = minutes,
                    onValueChange = { minutes = it },
                    label = { Text("목표 시간 (분)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onCreate(name, minutes.toLongOrNull() ?: 0) },
                enabled = name.isNotBlank() && (minutes.toLongOrNull() ?: 0) > 0
            ) { Text("만들기") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("취소") }
        }
    )
}

@Composable
private fun RetryContent(onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("목표를 불러올 수 없습니다", style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(16.dp))
        OutlinedButton(onClick = onRetry) { Text("다시 시도") }
    }
}

@Composable
private fun ErrorContent(onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("오류가 발생했습니다", style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(16.dp))
        OutlinedButton(onClick = onRetry) { Text("다시 시도") }
    }
}
