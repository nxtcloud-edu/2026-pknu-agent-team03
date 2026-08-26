package com.timeback.ui.viewmodel;

import com.timeback.ui.domain.model.ErrorReason;
import com.timeback.ui.domain.model.ScreenState;
import com.timeback.ui.fake.FakeFeatureGateway;
import com.timeback.ui.fake.HomeViewData;
import com.timeback.ui.feature.home.HomeViewModel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

/**
 * HomeViewModel 단위 테스트.
 */
@ExtendWith(LiveDataTestExtension.class)
class HomeViewModelTest {

    private FakeFeatureGateway gateway;
    private HomeViewModel viewModel;

    @BeforeEach
    void setUp() {
        gateway = new FakeFeatureGateway();
    }

    @Test
    void loadHome_shouldReturnContentWithHomeViewData() throws Exception {
        viewModel = new HomeViewModel(gateway);
        Thread.sleep(300);

        ScreenState state = viewModel.state.getValue();
        assertNotNull(state);
        assertInstanceOf(ScreenState.Content.class, state);

        Object data = ((ScreenState.Content) state).getData();
        assertInstanceOf(HomeViewData.class, data);
        HomeViewData hvd = (HomeViewData) data;
        assertNotNull(hvd.getWasteTimeToday());
        assertNotNull(hvd.getBaselineState());
        assertFalse(hvd.getGoalProgresses().isEmpty());
    }

    @Test
    void whenOffline_shouldReturnRetryableError() throws Exception {
        gateway.isOffline = true;
        viewModel = new HomeViewModel(gateway);
        Thread.sleep(300);

        ScreenState state = viewModel.state.getValue();
        assertNotNull(state);
        assertInstanceOf(ScreenState.RetryableError.class, state);
        assertEquals(ErrorReason.OFFLINE, ((ScreenState.RetryableError) state).getReason());
    }

    @Test
    void refresh_shouldReloadHome() throws Exception {
        gateway.isOffline = true;
        viewModel = new HomeViewModel(gateway);
        Thread.sleep(300);

        gateway.isOffline = false;
        viewModel.refresh();
        Thread.sleep(300);

        ScreenState state = viewModel.state.getValue();
        assertInstanceOf(ScreenState.Content.class, state);
    }
}
