package com.timeback.ui.viewmodel;

import com.timeback.ui.domain.model.*;
import com.timeback.ui.fake.FakeFeatureGateway;
import com.timeback.ui.fake.RecoveryViewData;
import com.timeback.ui.feature.recovery.RecoveryViewModel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RecoveryViewModel 단위 테스트.
 */
@ExtendWith(LiveDataTestExtension.class)
class RecoveryViewModelTest {

    private FakeFeatureGateway gateway;
    private RecoveryViewModel viewModel;

    @BeforeEach
    void setUp() {
        gateway = new FakeFeatureGateway();
    }

    @Test
    void loadRecovery_withGoals_shouldReturnContent() throws Exception {
        gateway.hasGoals = true;
        viewModel = new RecoveryViewModel(gateway);
        Thread.sleep(300);

        ScreenState state = viewModel.state.getValue();
        assertNotNull(state);
        assertInstanceOf(ScreenState.Content.class, state);

        Object data = ((ScreenState.Content) state).getData();
        assertInstanceOf(RecoveryViewData.class, data);
        RecoveryViewData rvd = (RecoveryViewData) data;
        assertNotNull(rvd.getEntry().getSelectedGoal());
        assertEquals(TimerState.IDLE, rvd.getEntry().getTimerState());
    }

    @Test
    void loadRecovery_withoutGoals_shouldReturnEmpty() throws Exception {
        gateway.hasGoals = false;
        viewModel = new RecoveryViewModel(gateway);
        Thread.sleep(300);

        ScreenState state = viewModel.state.getValue();
        assertNotNull(state);
        assertInstanceOf(ScreenState.Empty.class, state);
    }

    @Test
    void startTimer_shouldReturnSuccess() throws Exception {
        viewModel = new RecoveryViewModel(gateway);
        Thread.sleep(300);

        viewModel.startTimer(new Identifier("goal-1"));
        Thread.sleep(300);

        ActionResult result = viewModel.actionResult.getValue();
        assertNotNull(result);
        assertInstanceOf(ActionResult.Success.class, result);
    }

    @Test
    void completeTimer_shouldReturnSuccess() throws Exception {
        viewModel = new RecoveryViewModel(gateway);
        Thread.sleep(300);

        viewModel.completeTimer();
        Thread.sleep(300);

        ActionResult result = viewModel.actionResult.getValue();
        assertNotNull(result);
        assertInstanceOf(ActionResult.Success.class, result);
    }
}
