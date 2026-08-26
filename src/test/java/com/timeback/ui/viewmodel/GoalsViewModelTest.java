package com.timeback.ui.viewmodel;

import com.timeback.ui.domain.model.ActionResult;
import com.timeback.ui.domain.model.Duration;
import com.timeback.ui.domain.model.ScreenState;
import com.timeback.ui.fake.FakeFeatureGateway;
import com.timeback.ui.fake.GoalsViewData;
import com.timeback.ui.feature.goals.GoalsViewModel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

/**
 * GoalsViewModel 단위 테스트.
 */
@ExtendWith(LiveDataTestExtension.class)
class GoalsViewModelTest {

    private FakeFeatureGateway gateway;
    private GoalsViewModel viewModel;

    @BeforeEach
    void setUp() {
        gateway = new FakeFeatureGateway();
    }

    @Test
    void loadGoals_withGoals_shouldReturnContent() throws Exception {
        gateway.hasGoals = true;
        viewModel = new GoalsViewModel(gateway);
        Thread.sleep(300);

        ScreenState state = viewModel.state.getValue();
        assertNotNull(state);
        assertInstanceOf(ScreenState.Content.class, state);

        Object data = ((ScreenState.Content) state).getData();
        assertInstanceOf(GoalsViewData.class, data);
        GoalsViewData gvd = (GoalsViewData) data;
        assertEquals(2, gvd.getGoals().size());
    }

    @Test
    void loadGoals_withoutGoals_shouldReturnEmpty() throws Exception {
        gateway.hasGoals = false;
        viewModel = new GoalsViewModel(gateway);
        Thread.sleep(300);

        ScreenState state = viewModel.state.getValue();
        assertNotNull(state);
        assertInstanceOf(ScreenState.Empty.class, state);
    }

    @Test
    void createGoal_shouldReturnSuccess() throws Exception {
        viewModel = new GoalsViewModel(gateway);
        Thread.sleep(300);

        viewModel.createGoal("운동", new Duration(1_800_000));
        Thread.sleep(300);

        ActionResult result = viewModel.actionResult.getValue();
        assertNotNull(result);
        assertInstanceOf(ActionResult.Success.class, result);
    }
}
