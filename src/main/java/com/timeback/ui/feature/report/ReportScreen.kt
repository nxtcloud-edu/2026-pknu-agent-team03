package com.timeback.ui.feature.report

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

/**
 * UI-07 리포트 화면
 * 상태: LOADING, CONTENT (BASELINE_OBSERVING 포함), EMPTY, REFRESHING, RETRYABLE_ERROR, ERROR
 */
@Composable
fun ReportScreen(
    viewModel: ReportViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var selectedPeriod by remember { mutableStateOf(ReportPeriod.WEEKLY) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("리포트", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))

        // Period selector
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ReportPeriod.entries.forEach { period ->
                FilterChip(
                    selected = selectedPeriod == period,
                    onClick = {
                        selectedPeriod = period
                        viewModel.changePeriod(period)
                    },
                    label = {
                        Text(
                            when (period) {
                                ReportPeriod.DAILY -> "일간"
                                ReportPeriod.WEEKLY -> "주간"
                                ReportPeriod.MONTHLY -> "월간"
                            }
                        )
                    }
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        when (val current = state) {
            is ScreenState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is ScreenState.Content<*> -> {
                val data = current.data as? ReportData
                if (data != null) {
                    ReportContent(data)
                }
            }
            is ScreenState.Empty -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("선택한 기간에 데이터가 없습니다")
                    Spacer(Modifier.height(8.dp))
                    Text("데이터가 수집되면 리포트가 표시됩니다", style = MaterialTheme.typography.bodySmall)
                }
            }
            is ScreenState.RetryableError -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("리포트를 불러올 수 없습니다")
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(onClick = { viewModel.loadReport(selectedPeriod) }) { Text("다시 시도") }
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
                    OutlinedButton(onClick = { viewModel.loadReport(selectedPeriod) }) { Text("다시 시도") }
                }
            }
            else -> {}
        }
    }
}

@Composable
private fun ReportContent(data: ReportData) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Metrics summary
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("낭비시간", style = MaterialTheme.typography.labelMedium)
                    Text("${data.metrics.wasteTime.minutes}분", style = MaterialTheme.typography.headlineSmall)
                }
            }
        }

        if (data.baselineComparable && data.metrics.baselineWeekly != null) {
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Card(Modifier.weight(1f)) {
                        Column(Modifier.padding(12.dp)) {
                            Text("Baseline", style = MaterialTheme.typography.labelSmall)
                            Text("${data.metrics.baselineWeekly.minutes}분", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                    Card(Modifier.weight(1f)) {
                        Column(Modifier.padding(12.dp)) {
                            Text("확보", style = MaterialTheme.typography.labelSmall)
                            Text("${data.metrics.savedTime?.minutes ?: 0}분", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                    Card(Modifier.weight(1f)) {
                        Column(Modifier.padding(12.dp)) {
                            Text("회수율", style = MaterialTheme.typography.labelSmall)
                            Text("${data.metrics.recoveryRate?.toInt() ?: 0}%", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
        }

        if (!data.baselineComparable) {
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Baseline 관찰 중", style = MaterialTheme.typography.labelMedium)
                        Text("Baseline 완료 후 비교 가능", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        // Goal summaries
        if (data.goalSummaries.isNotEmpty()) {
            item {
                Text("목표별 되찾은 시간", style = MaterialTheme.typography.titleMedium)
            }
            items(data.goalSummaries) { goalProgress ->
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(12.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(goalProgress.goal.name, style = MaterialTheme.typography.bodyMedium)
                        Text("${goalProgress.accumulatedDuration.minutes}분", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
