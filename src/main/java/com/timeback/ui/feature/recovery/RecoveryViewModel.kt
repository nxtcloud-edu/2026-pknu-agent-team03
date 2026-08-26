package com.timeback.ui.feature.recovery

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
 * UI-05 시간 되찾기 ViewModel
 * CT-04 §8: ReadRecoveryEntry, StartGoalTimer, CompleteGoalTimer, CreateManualRecoveredTime, SelectRepresentativeGoal
 */
@HiltViewModel
class RecoveryViewModel @Inject constructor(
    private val gateway: FeatureGateway
) : ViewModel() {

    private val _state = MutableStateFlow<ScreenState>(ScreenState.Initial)
    val state: StateFlow<ScreenState> = _state.asStateFlow()

    private val _actionResult = MutableStateFlow<ActionResult?>(null)
    val actionResult: StateFlow<ActionResult?> = _actionResult.asStateFlow()

    init {
        loadRecoveryEntry()
    }

    fun loadRecoveryEntry() {
        viewModelScope.launch {
            _state.value = ScreenState.Loading
            _state.value = gateway.readRecoveryEntry()
        }
    }

    fun startTimer(goalId: Identifier) {
        viewModelScope.launch {
            val result = gateway.startGoalTimer(goalId)
            _actionResult.value = result
            if (result is ActionResult.Success) {
                _state.value = gateway.readRecoveryEntry()
            }
        }
    }

    fun completeTimer() {
        viewModelScope.launch {
            _state.value = ScreenState.Loading
            val result = gateway.completeGoalTimer()
            _actionResult.value = result
            if (result is ActionResult.Success) {
                _state.value = gateway.readRecoveryEntry()
            }
        }
    }

    fun createManualRecoveredTime(goalId: Identifier, timeRange: TimeRange) {
        viewModelScope.launch {
            _state.value = ScreenState.Loading
            val result = gateway.createManualRecoveredTime(goalId, timeRange)
            _actionResult.value = result
            if (result is ActionResult.Success) {
                _state.value = gateway.readRecoveryEntry()
            }
        }
    }

    fun selectRepresentativeGoal(overlapGroupId: Identifier, goalId: Identifier) {
        viewModelScope.launch {
            val result = gateway.selectRepresentativeGoal(overlapGroupId, goalId)
            _actionResult.value = result
            if (result is ActionResult.Success) {
                _state.value = gateway.readRecoveryEntry()
            }
        }
    }

    fun clearActionResult() {
        _actionResult.value = null
    }
}
