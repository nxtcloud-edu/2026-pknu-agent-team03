package com.timeback.ui.feature.timeline

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
 * UI-03 Timeline ViewModel
 * CT-04 §6: ReadTimeline, RefreshTimeline, CreateActivity, UpdateActivity, ConfirmContext, UpdateContext
 */
@HiltViewModel
class TimelineViewModel @Inject constructor(
    private val gateway: FeatureGateway
) : ViewModel() {

    private val _state = MutableStateFlow<ScreenState>(ScreenState.Initial)
    val state: StateFlow<ScreenState> = _state.asStateFlow()

    private val _actionResult = MutableStateFlow<ActionResult?>(null)
    val actionResult: StateFlow<ActionResult?> = _actionResult.asStateFlow()

    private var currentDate: Long = System.currentTimeMillis()

    fun loadTimeline(date: Long) {
        currentDate = date
        viewModelScope.launch {
            _state.value = ScreenState.Loading
            _state.value = gateway.readTimeline(date)
        }
    }

    fun refreshTimeline() {
        viewModelScope.launch {
            val current = _state.value
            if (current is ScreenState.Content<*>) {
                _state.value = ScreenState.Refreshing(current.data)
            }
            _state.value = gateway.refreshTimeline(currentDate)
        }
    }

    fun createActivity(type: ActivityType, name: String, timeRange: TimeRange) {
        viewModelScope.launch {
            val result = gateway.createActivity(type, name, timeRange)
            _actionResult.value = result
            if (result is ActionResult.Success) {
                _state.value = gateway.readTimeline(currentDate)
            }
        }
    }

    fun updateActivity(activityId: Identifier, name: String?, timeRange: TimeRange?) {
        viewModelScope.launch {
            val result = gateway.updateActivity(activityId, name, timeRange)
            _actionResult.value = result
            if (result is ActionResult.Success) {
                _state.value = gateway.readTimeline(currentDate)
            }
        }
    }

    fun confirmContext(contextId: Identifier) {
        viewModelScope.launch {
            val result = gateway.confirmContext(contextId)
            _actionResult.value = result
            if (result is ActionResult.Success) {
                _state.value = gateway.readTimeline(currentDate)
            }
        }
    }

    fun updateContext(contextId: Identifier, classification: ContextClassification) {
        viewModelScope.launch {
            val result = gateway.updateContext(contextId, classification)
            _actionResult.value = result
            if (result is ActionResult.Success) {
                _state.value = gateway.readTimeline(currentDate)
            }
        }
    }

    fun clearActionResult() {
        _actionResult.value = null
    }
}
