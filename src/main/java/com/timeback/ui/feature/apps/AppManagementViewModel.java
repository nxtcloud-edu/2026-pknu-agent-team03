package com.timeback.ui.feature.apps;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.timeback.ui.domain.gateway.FeatureGateway;
import com.timeback.ui.domain.model.ActionResult;
import com.timeback.ui.domain.model.AppClassification;
import com.timeback.ui.domain.model.ScreenState;

import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;

/**
 * UI-04 앱 관리 ViewModel.
 * 설치된 앱 목록과 기본 분류를 관리한다.
 */
@HiltViewModel
public class AppManagementViewModel extends ViewModel {

    private final FeatureGateway gateway;
    private final MutableLiveData<ScreenState> _state = new MutableLiveData<>(new ScreenState.Initial());
    public final LiveData<ScreenState> state = _state;

    private final MutableLiveData<ActionResult> _actionResult = new MutableLiveData<>();
    public final LiveData<ActionResult> actionResult = _actionResult;

    @Inject
    public AppManagementViewModel(FeatureGateway gateway) {
        this.gateway = gateway;
        loadApps();
    }

    public void loadApps() {
        _state.setValue(new ScreenState.Loading());
        gateway.readApps().thenAccept(_state::postValue);
    }

    public void changeClassification(String packageName, AppClassification classification) {
        gateway.changeDefaultClassification(packageName, classification).thenAccept(_actionResult::postValue);
    }
}
