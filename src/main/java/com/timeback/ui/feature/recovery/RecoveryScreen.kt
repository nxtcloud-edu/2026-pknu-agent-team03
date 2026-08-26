package com.timeback.ui.feature.recovery

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.timeback.ui.domain.model.*
import com.timeback.ui.fake.RecoveryViewData

/**
 * UI-05 시간 되찾기 화면
 * 상태: LOADING, EMPTY (Goal 없음), CONTENT, BLOCKED(REPRESENTATIVE_GOAL_REQUIRED), RETRYABLE_ERROR, ERROR
 */
@Composable
fun RecoveryScreen(
    viewModel: RecoveryViewModel = hiltViewModel(),
    onNavigateToGoals: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()

    when (val current = state) {
        is ScreenState.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is ScreenState.Empty -> {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("목표가 없습니다", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(16.dp))
                Button(onClick = onNavigateToGoals) {
                    Text("첫 목표 만들기")
                }
            }
        }
        is ScreenState.Content<*> -> {
            val data = current.data as? RecoveryViewData
            if (data != null) {
                RecoveryContent(
                    entry = data.entry,
                    onStartTimer = { viewModel.startTimer(it) },
                    onCompleteTimer = { viewModel.completeTimer() },
                    onSelectRepresentative = { groupId, goalId ->
                        viewModel.selectRepresentativeGoal(groupId, goalId)
                    }
                )
            }
        }
        is ScreenState.RetryableError -> {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("저장 실패", style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(12.dp))
                OutlinedButton(onClick = { viewModel.loadRecoveryEntry() }) { Text("다시 시도") }
            }
        }
        is ScreenState.Error -> {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("오류가 발생했습니다", style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(12.dp))
                OutlinedButton(onClick = { viewModel.loadRecoveryEntry() }) { Text("다시 시도") }
            }
        }
        else -> {}
    }
}

@Composable
private fun RecoveryContent(
    entry: RecoveryEntry,
    onStartTimer: (Identifier) -> Unit,
    onCompleteTimer: () -> Unit,
    onSelectRepresentative: (Identifier, Identifier) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("시간 되찾기", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))

        // Goal selector
        if (entry.availableGoals.isNotEmpty()) {
            Text("목표 선택", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            entry.availableGoals.forEach { goal ->
                val isSelected = entry.selectedGoal?.id == goal.id
                FilterChip(
                    selected = isSelected,
                    onClick = { /* goal selection handled by gateway */ },
                    label = { Text(goal.name) },
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // Timer controls
        when (entry.timerState) {
            TimerState.IDLE -> {
                if (entry.selectedGoal != null) {
                    Button(
                        onClick = { onStartTimer(entry.selectedGoal.id) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("타이머 시작")
                    }
                }
            }
            TimerState.RUNNING -> {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("타이머 실행 중", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = onCompleteTimer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("완료")
                        }
                    }
                }
            }
            TimerState.COMPLETING -> {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }

        // Overlap warning
        if (entry.overlapRequiresRepresentative) {
            Spacer(Modifier.height(24.dp))
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("겹침 확인 필요", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(8.dp))
                    Text("기록된 시간이 다른 목표와 겹칩니다. 대표 목표를 선택해 주세요.")
                    Spacer(Modifier.height(8.dp))
                    entry.availableGoals.forEach { goal ->
                        OutlinedButton(
                            onClick = { onSelectRepresentative(Identifier("overlap-group"), goal.id) },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Text(goal.name)
                        }
                    }
                }
            }
        }
    }
}
