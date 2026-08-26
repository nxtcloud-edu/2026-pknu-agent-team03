package com.timeback.ui.feature.datamanagement

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
 * UI-08 데이터 관리 ViewModel
 * CT-04 §11: ReadDataManagementState, ChangeRetentionSelection, RequestFullDeletion, ConfirmFullDeletion, RefreshDeletionStatus
 */
@HiltViewModel
class DataManagementViewModel @Inject constructor(
    private val gateway: FeatureGateway
) : ViewModel() {

    private val _state = MutableStateFlow<ScreenState>(ScreenState.Initial)
    val state: StateFlow<ScreenState> = _state.asStateFlow()

    private val _actionResult = MutableStateFlow<ActionResult?>(null)
    val actionResult: StateFlow<ActionResult?> = _actionResult.asStateFlow()

    private val _confirmationPending = MutableStateFlow(false)
    val confirmationPending: StateFlow<Boolean> = _confirmationPending.asStateFlow()

    init {
        loadDataManagement()
    }

    fun loadDataManagement() {
        viewModelScope.launch {
            _state.value = ScreenState.Loading
            _state.value = gateway.readDataManagementState()
        }
    }

    fun changeRetention(selection: RetentionSelection) {
        viewModelScope.launch {
            _state.value = ScreenState.Loading
            val result = gateway.changeRetentionSelection(selection)
            _actionResult.value = result
            if (result is ActionResult.Success) {
                _state.value = gateway.readDataManagementState()
            } else {
                _state.value = gateway.readDataManagementState()
            }
        }
    }

    fun requestFullDeletion() {
        viewModelScope.launch {
            val result = gateway.requestFullDeletion()
            if (result is ActionResult.Success) {
                _confirmationPending.value = true
            }
            _actionResult.value = result
        }
    }

    fun confirmFullDeletion() {
        viewModelScope.launch {
            _confirmationPending.value = false
            _state.value = ScreenState.Loading
            val result = gateway.confirmFullDeletion()
            _actionResult.value = result
            if (result is ActionResult.Success) {
                _state.value = gateway.readDataManagementState()
            }
        }
    }

    fun cancelDeletion() {
        _confirmationPending.value = false
    }

    fun refreshDeletionStatus(jobId: Identifier) {
        viewModelScope.launch {
            _state.value = gateway.refreshDeletionStatus(jobId)
        }
    }

    fun clearActionResult() {
        _actionResult.value = null
    }
}
