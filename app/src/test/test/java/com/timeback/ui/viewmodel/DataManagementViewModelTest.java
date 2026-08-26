package com.timeback.ui.viewmodel;

import com.timeback.ui.domain.model.*;
import com.timeback.ui.fake.DataManagementViewData;
import com.timeback.ui.fake.FakeFeatureGateway;
import com.timeback.ui.feature.datamanagement.DataManagementViewModel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DataManagementViewModel 단위 테스트.
 */
class DataManagementViewModelTest {

    private FakeFeatureGateway gateway;
    private DataManagementViewModel viewModel;

    @BeforeEach
    void setUp() {
        gateway = new FakeFeatureGateway();
    }

    @Test
    void loadState_shouldReturnContentWithDataManagementViewData() throws Exception {
        viewModel = new DataManagementViewModel(gateway);
        Thread.sleep(300);

        ScreenState state = viewModel.state.getValue();
        assertNotNull(state);
        assertInstanceOf(ScreenState.Content.class, state);

        Object data = ((ScreenState.Content) state).getData();
        assertInstanceOf(DataManagementViewData.class, data);
        DataManagementViewData dmvd = (DataManagementViewData) data;
        assertTrue(dmvd.isAnonymousIdReady());
        assertNotNull(dmvd.getRetentionSelection());
    }

    @Test
    void changeRetention_shouldReturnSuccess() throws Exception {
        viewModel = new DataManagementViewModel(gateway);
        Thread.sleep(300);

        viewModel.changeRetention(new RetentionSelection("7days"));
        Thread.sleep(300);

        ActionResult result = viewModel.actionResult.getValue();
        assertNotNull(result);
        assertInstanceOf(ActionResult.Success.class, result);
    }

    @Test
    void confirmDeletion_shouldSetDeletionComplete() throws Exception {
        viewModel = new DataManagementViewModel(gateway);
        Thread.sleep(300);

        viewModel.confirmDeletion();
        Thread.sleep(500);  // 삭제 확인 + 재로드

        ActionResult result = viewModel.actionResult.getValue();
        assertInstanceOf(ActionResult.Success.class, result);

        // 상태 재로드됨 - deletionComplete = true
        ScreenState state = viewModel.state.getValue();
        assertInstanceOf(ScreenState.Content.class, state);
        DataManagementViewData data = (DataManagementViewData) ((ScreenState.Content) state).getData();
        assertNotNull(data.getDeletionJob());
        assertTrue(data.getDeletionJob().isDeviceCompleted());
        assertTrue(data.getDeletionJob().isServerCompleted());
    }

    @Test
    void refreshDeletionStatus_shouldReturnUpdatedStatus() throws Exception {
        gateway.deletionComplete = true;
        viewModel = new DataManagementViewModel(gateway);
        Thread.sleep(300);

        viewModel.refreshDeletionStatus(new Identifier("job-1"));
        Thread.sleep(300);

        ScreenState state = viewModel.state.getValue();
        assertInstanceOf(ScreenState.Content.class, state);
        DataManagementViewData data = (DataManagementViewData) ((ScreenState.Content) state).getData();
        assertNotNull(data.getDeletionJob());
    }
}
