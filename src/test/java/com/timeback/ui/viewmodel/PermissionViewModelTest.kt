package com.timeback.ui.viewmodel

import com.timeback.ui.domain.model.*
import com.timeback.ui.fake.FakeFeatureGateway
import com.timeback.ui.fake.PermissionViewData
import com.timeback.ui.feature.permission.PermissionViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

/**
 * UI-01 PermissionViewModel 단위 테스트
 * 검증: LOADING → CONTENT(권한 있음), LOADING → BLOCKED(권한 없음), refreshAccessState
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PermissionViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var gateway: FakeFeatureGateway
    private lateinit var viewModel: PermissionViewModel

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        gateway = FakeFeatureGateway()
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `permission granted shows CONTENT state`() = runTest {
        gateway.permissionGranted = true
        viewModel = PermissionViewModel(gateway)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state is ScreenState.Content<*>)
        val data = (state as ScreenState.Content<*>).data as PermissionViewData
        assertTrue(data.canEnterMainScreens)
    }

    @Test
    fun `permission denied shows BLOCKED state`() = runTest {
        gateway.permissionGranted = false
        viewModel = PermissionViewModel(gateway)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state is ScreenState.Blocked)
        assertEquals(BlockReason.PERMISSION_REQUIRED, (state as ScreenState.Blocked).reason)
    }

    @Test
    fun `refreshAccessState updates state`() = runTest {
        gateway.permissionGranted = false
        viewModel = PermissionViewModel(gateway)
        advanceUntilIdle()

        // Now grant permission
        gateway.permissionGranted = true
        viewModel.refreshAccessState()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state is ScreenState.Content<*>)
    }
}
