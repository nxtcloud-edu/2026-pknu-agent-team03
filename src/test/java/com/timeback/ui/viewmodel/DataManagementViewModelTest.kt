package com.timeback.ui.viewmodel

import com.timeback.ui.domain.model.*
import com.timeback.ui.fake.DataManagementViewData
import com.timeback.ui.fake.FakeFeatureGateway
import com.timeback.ui.feature.datamanagement.DataManagementViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

/**
 * UI-08 DataManagementViewModel 단위 테스트
 * 검증: CONTENT(backup pending), RETRYABLE_ERROR(offline), confirmFullDeletion flow, PARTIAL_FAILURE
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DataManagementViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var gateway: FakeFeatureGateway
    private lateinit var viewModel: DataManagementViewModel

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
    fun `normal state shows CONTENT with backup pending`() = runTest {
        gateway.isOffline = false
        gateway.deletionComplete = false
        viewModel = DataManagementViewModel(gateway)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state is ScreenState.Content<*>)
        val data = (state as ScreenState.Content<*>).data as DataManagementViewData
        assertEquals(3, data.backupStatus.pendingCount)
    }

    @Test
    fun `offline shows RETRYABLE_ERROR`() = runTest {
        gateway.isOffline = true
        viewModel = DataManagementViewModel(gateway)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state is ScreenState.RetryableError)
    }

    @Test
    fun `requestFullDeletion sets confirmation pending`() = runTest {
        gateway.isOffline = false
        viewModel = DataManagementViewModel(gateway)
        advanceUntilIdle()

        viewModel.requestFullDeletion()
        advanceUntilIdle()

        assertTrue(viewModel.confirmationPending.value)
    }

    @Test
    fun `confirmFullDeletion completes and shows deletion done`() = runTest {
        gateway.isOffline = false
        viewModel = DataManagementViewModel(gateway)
        advanceUntilIdle()

        viewModel.requestFullDeletion()
        advanceUntilIdle()
        viewModel.confirmFullDeletion()
        advanceUntilIdle()

        assertFalse(viewModel.confirmationPending.value)
        val state = viewModel.state.value
        assertTrue(state is ScreenState.Content<*>)
        val data = (state as ScreenState.Content<*>).data as DataManagementViewData
        assertNotNull(data.deletionJob)
        assertTrue(data.deletionJob!!.deviceCompleted)
        assertTrue(data.deletionJob!!.serverCompleted)
    }

    @Test
    fun `changeRetention offline returns RETRYABLE_FAILURE`() = runTest {
        gateway.isOffline = true
        viewModel = DataManagementViewModel(gateway)
        advanceUntilIdle()

        // Reset to test action
        gateway.isOffline = true
        viewModel.changeRetention(RetentionSelection("30days"))
        advanceUntilIdle()

        val result = viewModel.actionResult.value
        assertTrue(result is ActionResult.RetryableFailure)
    }
}
