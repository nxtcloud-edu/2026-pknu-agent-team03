package com.timeback.ui.feature.home

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
 * UI-02 홈 대시보드 ViewModel
 * CT-04 §5: ReadHome, RefreshHome
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val gateway: FeatureGateway
) : ViewModel() {

    private val _state = MutableStateFlow<ScreenState>(ScreenState.Initial)
    val state: StateFlow<ScreenState> = _state.asStateFlow()

    init {
        loadHome()
    }

    fun loadHome() {
        viewModelScope.launch {
            _state.value = ScreenState.Loading
            _state.value = gateway.readHome()
        }
    }

    fun refreshHome() {
        viewModelScope.launch {
            val current = _state.value
            if (current is ScreenState.Content<*>) {
                _state.value = ScreenState.Refreshing(current.data)
            }
            _state.value = gateway.refreshHome()
        }
    }
}
