package com.timeback.ui.feature.apps

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
 * UI-04 앱 관리 ViewModel
 * CT-04 §7: ReadApps, ChangeDefaultClassification
 */
@HiltViewModel
class AppManagementViewModel @Inject constructor(
    private val gateway: FeatureGateway
) : ViewModel() {

    private val _state = MutableStateFlow<ScreenState>(ScreenState.Initial)
    val state: StateFlow<ScreenState> = _state.asStateFlow()

    private val _actionResult = MutableStateFlow<ActionResult?>(null)
    val actionResult: StateFlow<ActionResult?> = _actionResult.asStateFlow()

    init {
        loadApps()
    }

    fun loadApps() {
        viewModelScope.launch {
            _state.value = ScreenState.Loading
            _state.value = gateway.readApps()
        }
    }

    fun changeClassification(packageName: String, classification: AppClassification) {
        viewModelScope.launch {
            val result = gateway.changeDefaultClassification(packageName, classification)
            _actionResult.value = result
            if (result is ActionResult.Success) {
                _state.value = gateway.readApps()
            }
        }
    }

    fun clearActionResult() {
        _actionResult.value = null
    }
}
