package com.timeback.ui.feature.report;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.timeback.ui.domain.gateway.FeatureGateway;
import com.timeback.ui.domain.model.ReportPeriod;
import com.timeback.ui.domain.model.ScreenState;

import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;

/**
 * UI-07 리포트 ViewModel.
 * 일간/주간/월간 리포트를 조회한다.
 */
@HiltViewModel
public class ReportViewModel extends ViewModel {

    private final FeatureGateway gateway;
    private final MutableLiveData<ScreenState> _state = new MutableLiveData<>(new ScreenState.Initial());
    public final LiveData<ScreenState> state = _state;

    private ReportPeriod currentPeriod = ReportPeriod.WEEKLY;

    @Inject
    public ReportViewModel(FeatureGateway gateway) {
        this.gateway = gateway;
        loadReport(currentPeriod);
    }

    public void loadReport(ReportPeriod period) {
        this.currentPeriod = period;
        _state.setValue(new ScreenState.Loading());
        gateway.readReport(period).thenAccept(_state::postValue);
    }

    public void changePeriod(ReportPeriod period) {
        loadReport(period);
    }
}
