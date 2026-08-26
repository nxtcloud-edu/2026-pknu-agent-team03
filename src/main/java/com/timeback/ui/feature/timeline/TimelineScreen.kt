package com.timeback.ui.feature.timeline

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
import com.timeback.ui.fake.TimelineViewData
import java.text.SimpleDateFormat
import java.util.*

/**
 * UI-03 Timeline 화면
 * 상태: LOADING, REFRESHING, CONTENT, EMPTY, RETRYABLE_ERROR, ERROR
 */
@Composable
fun TimelineScreen(
    viewModel: TimelineViewModel = hiltViewModel(),
    initialDate: Long = System.currentTimeMillis()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(initialDate) {
        viewModel.loadTimeline(initialDate)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Date header
        DateHeader(
            date = initialDate,
            onRefresh = { viewModel.refreshTimeline() }
        )

        when (val current = state) {
            is ScreenState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is ScreenState.Refreshing<*> -> {
                // Show existing data with refresh indicator
                val data = current.currentData as? TimelineViewData
                if (data != null) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    TimelineList(items = data.items, onConfirmContext = { viewModel.confirmContext(it) })
                }
            }
            is ScreenState.Content<*> -> {
                val data = current.data as? TimelineViewData
                if (data != null) {
                    TimelineList(items = data.items, onConfirmContext = { viewModel.confirmContext(it) })
                }
            }
            is ScreenState.Empty -> {
                EmptyTimelineContent()
            }
            is ScreenState.RetryableError -> {
                RetryContent(onRetry = { viewModel.refreshTimeline() })
            }
            is ScreenState.Error -> {
                ErrorContent(onRetry = { viewModel.loadTimeline(initialDate) })
            }
            else -> {}
        }
    }
}

@Composable
private fun DateHeader(date: Long, onRefresh: () -> Unit) {
    val dateFormat = SimpleDateFormat("yyyy년 M월 d일 (E)", Locale.KOREA)
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(dateFormat.format(Date(date)), style = MaterialTheme.typography.titleMedium)
        IconButton(onClick = onRefresh) {
            Text("새로고침")
        }
    }
}

@Composable
private fun TimelineList(items: List<TimelineItem>, onConfirmContext: (Identifier) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items) { item ->
            TimelineItemCard(item = item, onConfirmContext = onConfirmContext)
        }
    }
}

@Composable
private fun TimelineItemCard(item: TimelineItem, onConfirmContext: (Identifier) -> Unit) {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.KOREA)

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${timeFormat.format(Date(item.timeRange.startAt))} - ${timeFormat.format(Date(item.timeRange.endAt))}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "${item.duration.minutes}분",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = item.activityName ?: item.appName,
                style = MaterialTheme.typography.titleSmall
            )
            if (item.isComplex) {
                Text("복합 활동", style = MaterialTheme.typography.labelSmall)
            }
            Spacer(Modifier.height(4.dp))
            ClassificationChip(classification = item.classification)

            if (item.contextConfirmationRequired) {
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { onConfirmContext(item.id) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Context 확인 필요")
                }
            }
        }
    }
}

@Composable
private fun ClassificationChip(classification: ContextClassification) {
    val label = when (classification) {
        ContextClassification.PRODUCTIVE -> "생산"
        ContextClassification.LEISURE -> "여가"
        ContextClassification.WASTE -> "낭비"
        ContextClassification.MIXED -> "혼합"
        ContextClassification.NEUTRAL -> "중립"
    }
    SuggestionChip(onClick = {}, label = { Text(label) })
}

@Composable
private fun EmptyTimelineContent() {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("이 날의 기록이 없습니다", style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(8.dp))
        Text("앱 사용 데이터가 수집되면 여기에 표시됩니다", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun RetryContent(onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("새로고침 실패", style = MaterialTheme.typography.bodyLarge)
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
