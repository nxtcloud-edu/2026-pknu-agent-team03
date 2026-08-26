package com.timeback.ui.feature.home

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
import com.timeback.ui.fake.HomeViewData

/**
 * UI-02 홈 대시보드 화면
 * 상태: LOADING, REFRESHING, CONTENT (including BASELINE_OBSERVING), EMPTY, RETRYABLE_ERROR, ERROR
 */
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateTimeline: () -> Unit = {},
    onNavigateGoals: () -> Unit = {},
    onNavigateReport: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()

    when (val current = state) {
        is ScreenState.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is ScreenState.Refreshing<*> -> {
            val data = current.currentData as? HomeViewData
            if (data != null) {
                Column {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    HomeContent(data, onNavigateTimeline, onNavigateGoals, onNavigateReport)
                }
            }
        }
        is ScreenState.Content<*> -> {
            val data = current.data as? HomeViewData
            if (data != null) {
                HomeContent(data, onNavigateTimeline, onNavigateGoals, onNavigateReport,
                    onRefresh = { viewModel.refreshHome() })
            }
        }
        is ScreenState.Empty -> {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("아직 수집된 데이터가 없습니다", style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(12.dp))
                OutlinedButton(onClick = onNavigateTimeline) { Text("Timeline 확인") }
            }
        }
        is ScreenState.RetryableError -> {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("새로고침 실패", style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(12.dp))
                OutlinedButton(onClick = { viewModel.refreshHome() }) { Text("다시 시도") }
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
                OutlinedButton(onClick = { viewModel.loadHome() }) { Text("다시 시도") }
            }
        }
        else -> {}
    }
}

@Composable
private fun HomeContent(
    data: HomeViewData,
    onNavigateTimeline: () -> Unit,
    onNavigateGoals: () -> Unit,
    onNavigateReport: () -> Unit,
    onRefresh: (() -> Unit)? = null
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Waste time today
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("오늘 낭비시간", style = MaterialTheme.typography.labelMedium)
                    Text("${data.wasteTimeToday.minutes}분", style = MaterialTheme.typography.headlineMedium)
                }
            }
        }

        // Baseline state
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    if (data.baselineState.isObserving) {
                        Text("Baseline 관찰 중", style = MaterialTheme.typography.labelMedium)
                        Text("남은 기간: ${data.baselineState.remainingDays}일", style = MaterialTheme.typography.bodyMedium)
                    } else {
                        Text("주간 Baseline", style = MaterialTheme.typography.labelMedium)
                        Text("${data.baselineState.weeklyBaseline?.minutes ?: 0}분", style = MaterialTheme.typography.headlineSmall)
                    }
                }
            }
        }

        // Saved time & Recovery (only if baseline ready)
        if (!data.baselineState.isObserving) {
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Card(Modifier.weight(1f)) {
                        Column(Modifier.padding(12.dp)) {
                            Text("확보한 시간", style = MaterialTheme.typography.labelSmall)
                            Text("${data.savedTime?.minutes ?: 0}분", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                    Card(Modifier.weight(1f)) {
                        Column(Modifier.padding(12.dp)) {
                            Text("되찾은 시간", style = MaterialTheme.typography.labelSmall)
                            Text("${data.recoveredTime?.minutes ?: 0}분", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                    Card(Modifier.weight(1f)) {
                        Column(Modifier.padding(12.dp)) {
                            Text("회수율", style = MaterialTheme.typography.labelSmall)
                            Text("${data.recoveryRate?.toInt() ?: 0}%", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
        }

        // Goal progress
        if (data.goalProgresses.isNotEmpty()) {
            item {
                Text("목표 진행", style = MaterialTheme.typography.titleMedium)
            }
            items(data.goalProgresses) { goalProgress ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text(goalProgress.goal.name, style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { (goalProgress.progressPercent / 100.0).toFloat().coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text("${goalProgress.progressPercent.toInt()}%", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        // Navigation
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onNavigateTimeline, modifier = Modifier.weight(1f)) {
                    Text("Timeline")
                }
                OutlinedButton(onClick = onNavigateGoals, modifier = Modifier.weight(1f)) {
                    Text("목표")
                }
                OutlinedButton(onClick = onNavigateReport, modifier = Modifier.weight(1f)) {
                    Text("리포트")
                }
            }
        }

        if (onRefresh != null) {
            item {
                OutlinedButton(onClick = onRefresh, modifier = Modifier.fillMaxWidth()) {
                    Text("새로고침")
                }
            }
        }
    }
}
