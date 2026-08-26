package com.timeback.ui.viewmodel;

import com.timeback.ui.domain.model.ActionResult;
import com.timeback.ui.domain.model.AppClassification;
import com.timeback.ui.domain.model.ScreenState;
import com.timeback.ui.fake.AppManagementViewData;
import com.timeback.ui.fake.FakeFeatureGateway;
import com.timeback.ui.feature.apps.AppManagementViewModel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AppManagementViewModel 단위 테스트.
 */
class AppManagementViewModelTest {

    private FakeFeatureGateway gateway;
    private AppManagementViewModel viewModel;

    @BeforeEach
    void setUp() {
        gateway = new FakeFeatureGateway();
    }

    @Test
    void loadApps_shouldReturnContentWithAppList() throws Exception {
        viewModel = new AppManagementViewModel(gateway);
        Thread.sleep(300);

        ScreenState state = viewModel.state.getValue();
        assertNotNull(state);
        assertInstanceOf(ScreenState.Content.class, state);

        Object data = ((ScreenState.Content) state).getData();
        assertInstanceOf(AppManagementViewData.class, data);
        AppManagementViewData amd = (AppManagementViewData) data;
        assertEquals(5, amd.getApps().size());
    }

    @Test
    void loadApps_shouldContainInstagramAsWaste() throws Exception {
        viewModel = new AppManagementViewModel(gateway);
        Thread.sleep(300);

        ScreenState state = viewModel.state.getValue();
        AppManagementViewData data = (AppManagementViewData) ((ScreenState.Content) state).getData();
        assertEquals("Instagram", data.getApps().get(0).getAppName());
        assertEquals(AppClassification.WASTE, data.getApps().get(0).getClassification());
    }

    @Test
    void changeClassification_shouldReturnSuccess() throws Exception {
        viewModel = new AppManagementViewModel(gateway);
        Thread.sleep(300);

        viewModel.changeClassification("com.instagram.android", AppClassification.LEISURE);
        Thread.sleep(300);

        ActionResult result = viewModel.actionResult.getValue();
        assertNotNull(result);
        assertInstanceOf(ActionResult.Success.class, result);
    }
}
