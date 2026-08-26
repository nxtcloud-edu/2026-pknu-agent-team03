package com.timeback.ui.feature.goals;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.timeback.ui.domain.gateway.FeatureGateway;
import com.timeback.ui.domain.model.ActionResult;
import com.timeback.ui.domain.model.Duration;
import com.timeback.ui.domain.model.ScreenState;

import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;

/**
 * UI-06 목표 ViewModel.
 * 목표 목록 조회 및 새 목표 생성을 처리한다.
 */
@HiltViewModel
public class GoalsViewModel extends ViewModel {

    private final FeatureGateway gateway;
    private final MutableLiveData<ScreenState> _state = new MutableLiveData<>(new ScreenState.Initial());
    public final LiveData<ScreenState> state = _state;

    private final MutableLiveData<ActionResult> _actionResult = new MutableLiveData<>();
    public final LiveData<ActionResult> actionResult = _actionResult;

    @Inject
    public GoalsViewModel(FeatureGateway gateway) {
        this.gateway = gateway;
        loadGoals();
    }

    public void loadGoals() {
        _state.setValue(new ScreenState.Loading());
        gateway.readGoals().thenAccept(_state::postValue);
    }

    public void createGoal(String name, Duration targetDuration) {
        gateway.createGoal(name, targetDuration).thenAccept(result -> {
            _actionResult.postValue(result);
            // 목표 생성 성공 시 목록 새로고침
            if (result instanceof ActionResult.Success) {
                loadGoals();
            }
        });
    }
}
