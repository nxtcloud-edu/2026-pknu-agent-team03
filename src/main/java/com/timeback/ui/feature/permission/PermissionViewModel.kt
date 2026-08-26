package com.timeback.ui.feature.permission

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timeback.ui.domain.gateway.FeatureGateway
import com.timeback.ui.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI-01 권한·초기 진입 ViewModel
 * CT-04 §4: ReadAccessState, OpenUsageAccessSettings, RefreshAccessState
 */
@HiltViewModel
class PermissionViewModel @Inject constructor(
    private val gateway: FeatureGateway
) : ViewModel() {

    private val _state = MutableStateFlow<ScreenState>(ScreenState.Initial)
    val state: StateFlow<ScreenState> = _state.asStateFlow()

    init {
        loadAccessState()
    }

    fun loadAccessState() {
        viewModelScope.launch {
            _state.value = ScreenState.Loading
            _state.value = gateway.readAccessState()
        }
    }

    fun openSettings() {
        viewModelScope.launch {
            gateway.openUsageAccessSettings()
        }
    }

    fun refreshAccessState() {
        viewModelScope.launch {
            _state.value = ScreenState.Loading
            _state.value = gateway.refreshAccessState()
        }
    }
}
