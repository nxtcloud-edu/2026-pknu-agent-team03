package com.timeback.ui.feature.report

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
 * UI-07 리포트 ViewModel
 * CT-04 §10: ReadReport, ChangeReportPeriod
 */
@HiltViewModel
class ReportViewModel @Inject constructor(
    private val gateway: FeatureGateway
) : ViewModel() {

    private val _state = MutableStateFlow<ScreenState>(ScreenState.Initial)
    val state: StateFlow<ScreenState> = _state.asStateFlow()

    private var currentPeriod = ReportPeriod.WEEKLY

    init {
        loadReport(currentPeriod)
    }

    fun loadReport(period: ReportPeriod) {
        currentPeriod = period
        viewModelScope.launch {
            _state.value = ScreenState.Loading
            _state.value = gateway.readReport(period)
        }
    }

    fun changePeriod(period: ReportPeriod) {
        if (period != currentPeriod) {
            loadReport(period)
        }
    }
}
