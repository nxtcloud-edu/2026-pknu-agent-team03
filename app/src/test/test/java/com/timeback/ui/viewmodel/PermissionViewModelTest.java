package com.timeback.ui.viewmodel;

import com.timeback.ui.domain.model.BlockReason;
import com.timeback.ui.domain.model.ScreenState;
import com.timeback.ui.fake.FakeFeatureGateway;
import com.timeback.ui.fake.PermissionViewData;
import com.timeback.ui.feature.permission.PermissionViewModel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PermissionViewModel 단위 테스트.
 * FakeFeatureGateway를 주입하여 상태 전이를 검증한다.
 */
class PermissionViewModelTest {

    private FakeFeatureGateway gateway;
    private PermissionViewModel viewModel;

    @BeforeEach
    void setUp() {
        gateway = new FakeFeatureGateway();
    }

    @Test
    void whenPermissionGranted_stateShouldBeContent() throws Exception {
        gateway.permissionGranted = true;
        viewModel = new PermissionViewModel(gateway);

        // CompletableFuture 완료 대기
        Thread.sleep(300);

        ScreenState state = viewModel.state.getValue();
        assertNotNull(state);
        assertInstanceOf(ScreenState.Content.class, state);

        Object data = ((ScreenState.Content) state).getData();
        assertInstanceOf(PermissionViewData.class, data);
        PermissionViewData pvd = (PermissionViewData) data;
        assertTrue(pvd.isUsageAccessGranted());
        assertTrue(pvd.canEnterMainScreens());
    }

    @Test
    void whenPermissionDenied_stateShouldBeBlocked() throws Exception {
        gateway.permissionGranted = false;
        viewModel = new PermissionViewModel(gateway);

        Thread.sleep(300);

        ScreenState state = viewModel.state.getValue();
        assertNotNull(state);
        assertInstanceOf(ScreenState.Blocked.class, state);
        assertEquals(BlockReason.PERMISSION_REQUIRED, ((ScreenState.Blocked) state).getReason());
    }

    @Test
    void refresh_shouldUpdateState() throws Exception {
        gateway.permissionGranted = false;
        viewModel = new PermissionViewModel(gateway);
        Thread.sleep(300);

        // 권한 부여 후 새로고침
        gateway.permissionGranted = true;
        viewModel.refresh();
        Thread.sleep(300);

        ScreenState state = viewModel.state.getValue();
        assertInstanceOf(ScreenState.Content.class, state);
    }
}
