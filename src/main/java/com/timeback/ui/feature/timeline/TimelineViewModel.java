package com.timeback.ui.feature.timeline;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.timeback.ui.domain.gateway.FeatureGateway;
import com.timeback.ui.domain.model.*;

import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;

/**
 * UI-03 타임라인 ViewModel.
 * 날짜별 앱 사용 타임라인을 로드하고, 활동 생성/수정/맥락 확인을 처리한다.
 */
@HiltViewModel
public class TimelineViewModel extends ViewModel {

    private final FeatureGateway gateway;
    private final MutableLiveData<ScreenState> _state = new MutableLiveData<>(new ScreenState.Initial());
    public final LiveData<ScreenState> state = _state;

    private final MutableLiveData<ActionResult> _actionResult = new MutableLiveData<>();
    public final LiveData<ActionResult> actionResult = _actionResult;

    private long currentDate = System.currentTimeMillis();

    @Inject
    public TimelineViewModel(FeatureGateway gateway) {
        this.gateway = gateway;
        loadTimeline(currentDate);
    }

    public void loadTimeline(long date) {
        this.currentDate = date;
        _state.setValue(new ScreenState.Loading());
        gateway.readTimeline(date).thenAccept(_state::postValue);
    }

    public void refresh() {
        gateway.refreshTimeline(currentDate).thenAccept(_state::postValue);
    }

    public void createActivity(ActivityType type, String name, TimeRange timeRange) {
        gateway.createActivity(type, name, timeRange).thenAccept(_actionResult::postValue);
    }

    public void updateActivity(Identifier activityId, String name, TimeRange timeRange) {
        gateway.updateActivity(activityId, name, timeRange).thenAccept(_actionResult::postValue);
    }

    public void confirmContext(Identifier contextId) {
        gateway.confirmContext(contextId).thenAccept(_actionResult::postValue);
    }

    public void updateContext(Identifier contextId, ContextClassification classification) {
        gateway.updateContext(contextId, classification).thenAccept(_actionResult::postValue);
    }
}
