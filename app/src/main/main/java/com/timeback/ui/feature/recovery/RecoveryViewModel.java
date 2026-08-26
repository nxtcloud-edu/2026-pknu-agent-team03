package com.timeback.ui.feature.recovery;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.timeback.ui.domain.gateway.FeatureGateway;
import com.timeback.ui.domain.model.*;

import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;

/**
 * UI-05 시간 되찾기 ViewModel.
 * 목표 타이머 시작/완료, 수동 시간 기록, 대표 목표 선택을 처리한다.
 */
@HiltViewModel
public class RecoveryViewModel extends ViewModel {

    private final FeatureGateway gateway;
    private final MutableLiveData<ScreenState> _state = new MutableLiveData<>(new ScreenState.Initial());
    public final LiveData<ScreenState> state = _state;

    private final MutableLiveData<ActionResult> _actionResult = new MutableLiveData<>();
    public final LiveData<ActionResult> actionResult = _actionResult;

    @Inject
    public RecoveryViewModel(FeatureGateway gateway) {
        this.gateway = gateway;
        loadRecovery();
    }

    public void loadRecovery() {
        _state.setValue(new ScreenState.Loading());
        gateway.readRecoveryEntry().thenAccept(_state::postValue);
    }

    public void startTimer(Identifier goalId) {
        gateway.startGoalTimer(goalId).thenAccept(_actionResult::postValue);
    }

    public void completeTimer() {
        gateway.completeGoalTimer().thenAccept(_actionResult::postValue);
    }

    public void createManualTime(Identifier goalId, TimeRange timeRange) {
        gateway.createManualRecoveredTime(goalId, timeRange).thenAccept(_actionResult::postValue);
    }

    public void selectRepresentativeGoal(Identifier overlapGroupId, Identifier goalId) {
        gateway.selectRepresentativeGoal(overlapGroupId, goalId).thenAccept(_actionResult::postValue);
    }
}
