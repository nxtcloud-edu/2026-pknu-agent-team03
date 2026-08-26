package com.timeback.ui.feature.apps

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
import com.timeback.ui.fake.AppManagementViewData

/**
 * UI-04 앱 관리 화면
 * 상태: LOADING, CONTENT, EMPTY, RETRYABLE_ERROR, ERROR
 */
@Composable
fun AppManagementScreen(
    viewModel: AppManagementViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("앱 분류 관리", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))

        when (val current = state) {
            is ScreenState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is ScreenState.Content<*> -> {
                val data = current.data as? AppManagementViewData
                if (data != null) {
                    AppList(
                        apps = data.apps,
                        onClassificationChange = { pkg, cls ->
                            viewModel.changeClassification(pkg, cls)
                        }
                    )
                }
            }
            is ScreenState.Empty -> {
                Text("설치된 앱이 없습니다", style = MaterialTheme.typography.bodyMedium)
            }
            is ScreenState.RetryableError -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("앱 목록을 불러올 수 없습니다")
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(onClick = { viewModel.loadApps() }) { Text("다시 시도") }
                }
            }
            is ScreenState.Error -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("오류가 발생했습니다")
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(onClick = { viewModel.loadApps() }) { Text("다시 시도") }
                }
            }
            else -> {}
        }
    }
}

@Composable
private fun AppList(
    apps: List<AppInfo>,
    onClassificationChange: (String, AppClassification) -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(apps) { app ->
            AppCard(app = app, onClassificationChange = onClassificationChange)
        }
    }
}

@Composable
private fun AppCard(
    app: AppInfo,
    onClassificationChange: (String, AppClassification) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(app.appName, style = MaterialTheme.typography.titleSmall)
                Text(app.packageName, style = MaterialTheme.typography.bodySmall)
            }
            Box {
                FilterChip(
                    selected = true,
                    onClick = { expanded = true },
                    label = { Text(app.classification.name) }
                )
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    AppClassification.entries.forEach { cls ->
                        DropdownMenuItem(
                            text = { Text(cls.name) },
                            onClick = {
                                expanded = false
                                onClassificationChange(app.packageName, cls)
                            }
                        )
                    }
                }
            }
        }
    }
}
