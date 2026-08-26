package com.timeback.ui.viewmodel

import com.timeback.ui.domain.model.*
import com.timeback.ui.fake.FakeFeatureGateway
import com.timeback.ui.fake.HomeViewData
import com.timeback.ui.feature.home.HomeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

/**
 * UI-02 HomeViewModel 단위 테스트
 * 검증: LOADING → CONTENT, BASELINE_OBSERVING 상태, refreshHome
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var gateway: FakeFeatureGateway
    private lateinit var viewModel: HomeViewModel

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
    fun `baseline ready shows full CONTENT`() = runTest {
        gateway.baselineReady = true
        viewModel = HomeViewModel(gateway)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state is ScreenState.Content<*>)
        val data = (state as ScreenState.Content<*>).data as HomeViewData
        assertNotNull(data.recoveryRate)
        assertFalse(data.baselineState.isObserving)
    }

    @Test
    fun `baseline observing shows observing state`() = runTest {
        gateway.baselineReady = false
        viewModel = HomeViewModel(gateway)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state is ScreenState.Content<*>)
        val data = (state as ScreenState.Content<*>).data as HomeViewData
        assertTrue(data.baselineState.isObserving)
        assertNull(data.recoveryRate)
    }

    @Test
    fun `refreshHome reloads data`() = runTest {
        gateway.baselineReady = false
        viewModel = HomeViewModel(gateway)
        advanceUntilIdle()

        gateway.baselineReady = true
        viewModel.refreshHome()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state is ScreenState.Content<*>)
        val data = (state as ScreenState.Content<*>).data as HomeViewData
        assertFalse(data.baselineState.isObserving)
    }
}
