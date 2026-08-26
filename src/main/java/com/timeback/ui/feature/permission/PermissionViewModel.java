package com.timeback.ui.feature.permission;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.timeback.ui.domain.gateway.FeatureGateway;
import com.timeback.ui.domain.model.ActionResult;
import com.timeback.ui.domain.model.ScreenState;

import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;

/**
 * UI-01 권한 화면 ViewModel.
 * 사용 권한 상태를 읽고 설정 화면을 열 수 있다.
 */
@HiltViewModel
public class PermissionViewModel extends ViewModel {

    private final FeatureGateway gateway;
    private final MutableLiveData<ScreenState> _state = new MutableLiveData<>(new ScreenState.Initial());
    public final LiveData<ScreenState> state = _state;

    private final MutableLiveData<ActionResult> _actionResult = new MutableLiveData<>();
    public final LiveData<ActionResult> actionResult = _actionResult;

    @Inject
    public PermissionViewModel(FeatureGateway gateway) {
        this.gateway = gateway;
        loadAccessState();
    }

    public void loadAccessState() {
        _state.setValue(new ScreenState.Loading());
        gateway.readAccessState().thenAccept(_state::postValue);
    }

    public void openSettings() {
        gateway.openUsageAccessSettings().thenAccept(_actionResult::postValue);
    }

    public void refresh() {
        gateway.refreshAccessState().thenAccept(_state::postValue);
    }
}
