package com.timeback.ui.feature.permission

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.timeback.ui.domain.model.*
import com.timeback.ui.fake.PermissionViewData

/**
 * UI-01 권한·초기 진입 화면
 * 상태: LOADING, BLOCKED(PERMISSION_REQUIRED), BLOCKED(IDENTITY_UNAVAILABLE), CONTENT, ERROR
 */
@Composable
fun PermissionScreen(
    viewModel: PermissionViewModel = hiltViewModel(),
    onPermissionReady: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    when (val current = state) {
        is ScreenState.Initial,
        is ScreenState.Loading -> {
            LoadingContent()
        }
        is ScreenState.Blocked -> {
            when (current.reason) {
                BlockReason.PERMISSION_REQUIRED -> {
                    PermissionRequiredContent(
                        onOpenSettings = { viewModel.openSettings() },
                        onRefresh = { viewModel.refreshAccessState() }
                    )
                }
                BlockReason.IDENTITY_UNAVAILABLE -> {
                    IdentityUnavailableContent(
                        onRefresh = { viewModel.refreshAccessState() }
                    )
                }
                else -> {
                    ErrorContent(onRetry = { viewModel.loadAccessState() })
                }
            }
        }
        is ScreenState.Content<*> -> {
            val data = current.data as? PermissionViewData
            if (data?.canEnterMainScreens == true) {
                onPermissionReady()
            } else {
                LoadingContent()
            }
        }
        is ScreenState.Error -> {
            ErrorContent(onRetry = { viewModel.loadAccessState() })
        }
        else -> {
            ErrorContent(onRetry = { viewModel.loadAccessState() })
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun PermissionRequiredContent(
    onOpenSettings: () -> Unit,
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Usage Access 권한이 필요합니다",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "앱 사용 시간을 분석하려면 Usage Access 권한을 허용해 주세요.",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onOpenSettings) {
            Text("설정 열기")
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(onClick = onRefresh) {
            Text("상태 다시 확인")
        }
    }
}

@Composable
private fun IdentityUnavailableContent(onRefresh: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "익명 식별자를 준비할 수 없습니다",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "기기 식별자 변환에 실패했습니다. 다시 시도해 주세요.",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedButton(onClick = onRefresh) {
            Text("다시 확인")
        }
    }
}

@Composable
private fun ErrorContent(onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "오류가 발생했습니다",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedButton(onClick = onRetry) {
            Text("다시 시도")
        }
    }
}
