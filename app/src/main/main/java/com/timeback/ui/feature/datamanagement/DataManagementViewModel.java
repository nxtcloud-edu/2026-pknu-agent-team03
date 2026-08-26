package com.timeback.ui.feature.datamanagement;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.timeback.ui.domain.gateway.FeatureGateway;
import com.timeback.ui.domain.model.*;

import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;

/**
 * UI-08 데이터 관리 ViewModel.
 * 보존 기간 변경, 전체 삭제 요청/확인, 삭제 상태 확인을 처리한다.
 */
@HiltViewModel
public class DataManagementViewModel extends ViewModel {

    private final FeatureGateway gateway;
    private final MutableLiveData<ScreenState> _state = new MutableLiveData<>(new ScreenState.Initial());
    public final LiveData<ScreenState> state = _state;

    private final MutableLiveData<ActionResult> _actionResult = new MutableLiveData<>();
    public final LiveData<ActionResult> actionResult = _actionResult;

    @Inject
    public DataManagementViewModel(FeatureGateway gateway) {
        this.gateway = gateway;
        loadState();
    }

    public void loadState() {
        _state.setValue(new ScreenState.Loading());
        gateway.readDataManagementState().thenAccept(_state::postValue);
    }

    public void changeRetention(RetentionSelection selection) {
        gateway.changeRetentionSelection(selection).thenAccept(_actionResult::postValue);
    }

    public void requestDeletion() {
        gateway.requestFullDeletion().thenAccept(_actionResult::postValue);
    }

    public void confirmDeletion() {
        gateway.confirmFullDeletion().thenAccept(result -> {
            _actionResult.postValue(result);
            if (result instanceof ActionResult.Success) {
                loadState();
            }
        });
    }

    public void refreshDeletionStatus(Identifier jobId) {
        gateway.refreshDeletionStatus(jobId).thenAccept(_state::postValue);
    }
}
