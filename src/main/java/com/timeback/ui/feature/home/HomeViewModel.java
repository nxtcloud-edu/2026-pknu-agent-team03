package com.timeback.ui.feature.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.timeback.ui.domain.gateway.FeatureGateway;
import com.timeback.ui.domain.model.ScreenState;

import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;

/**
 * UI-02 홈 대시보드 ViewModel.
 * 오늘의 낭비 시간, 기준선 상태, 목표 진행률을 표시한다.
 */
@HiltViewModel
public class HomeViewModel extends ViewModel {

    private final FeatureGateway gateway;
    private final MutableLiveData<ScreenState> _state = new MutableLiveData<>(new ScreenState.Initial());
    public final LiveData<ScreenState> state = _state;

    @Inject
    public HomeViewModel(FeatureGateway gateway) {
        this.gateway = gateway;
        loadHome();
    }

    public void loadHome() {
        _state.setValue(new ScreenState.Loading());
        gateway.readHome().thenAccept(_state::postValue);
    }

    public void refresh() {
        gateway.refreshHome().thenAccept(_state::postValue);
    }
}
