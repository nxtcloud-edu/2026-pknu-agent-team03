package com.timeback.ui.feature.goals

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
 * UI-06 목표 ViewModel
 * CT-04 §9: ReadGoals, CreateGoal
 */
@HiltViewModel
class GoalsViewModel @Inject constructor(
    private val gateway: FeatureGateway
) : ViewModel() {

    private val _state = MutableStateFlow<ScreenState>(ScreenState.Initial)
    val state: StateFlow<ScreenState> = _state.asStateFlow()

    private val _actionResult = MutableStateFlow<ActionResult?>(null)
    val actionResult: StateFlow<ActionResult?> = _actionResult.asStateFlow()

    init {
        loadGoals()
    }

    fun loadGoals() {
        viewModelScope.launch {
            _state.value = ScreenState.Loading
            _state.value = gateway.readGoals()
        }
    }

    fun createGoal(name: String, targetMinutes: Long) {
        viewModelScope.launch {
            _state.value = ScreenState.Loading
            val result = gateway.createGoal(name, Duration(targetMinutes * 60_000))
            _actionResult.value = result
            if (result is ActionResult.Success) {
                _state.value = gateway.readGoals()
            }
        }
    }

    fun clearActionResult() {
        _actionResult.value = null
    }
}
