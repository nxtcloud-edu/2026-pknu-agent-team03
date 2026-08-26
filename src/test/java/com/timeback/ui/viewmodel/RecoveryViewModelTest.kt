package com.timeback.ui.viewmodel

import com.timeback.ui.domain.model.*
import com.timeback.ui.fake.FakeFeatureGateway
import com.timeback.ui.fake.RecoveryViewData
import com.timeback.ui.feature.recovery.RecoveryViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

/**
 * UI-05 RecoveryViewModel 단위 테스트
 * 검증: hasGoals → CONTENT, !hasGoals → EMPTY, startTimer/completeTimer, INVALID_TIME_RANGE
 */
@OptIn(ExperimentalCoroutinesApi::class)
class RecoveryViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var gateway: FakeFeatureGateway
    private lateinit var viewModel: RecoveryViewModel

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
    fun `goals exist shows CONTENT with recovery entry`() = runTest {
        gateway.hasGoals = true
        viewModel = RecoveryViewModel(gateway)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state is ScreenState.Content<*>)
        val data = (state as ScreenState.Content<*>).data as RecoveryViewData
        assertTrue(data.entry.overlapRequiresRepresentative)
    }

    @Test
    fun `no goals shows EMPTY`() = runTest {
        gateway.hasGoals = false
        viewModel = RecoveryViewModel(gateway)
        advanceUntilIdle()

        assertTrue(viewModel.state.value is ScreenState.Empty)
    }

    @Test
    fun `startTimer returns SUCCESS`() = runTest {
        gateway.hasGoals = true
        viewModel = RecoveryViewModel(gateway)
        advanceUntilIdle()

        viewModel.startTimer(Identifier("goal-1"))
        advanceUntilIdle()

        assertTrue(viewModel.actionResult.value is ActionResult.Success)
    }

    @Test
    fun `manual recovered time with invalid range returns BLOCKED`() = runTest {
        gateway.hasGoals = true
        viewModel = RecoveryViewModel(gateway)
        advanceUntilIdle()

        viewModel.createManualRecoveredTime(
            Identifier("goal-1"),
            TimeRange(startAt = 1000, endAt = 500) // invalid
        )
        advanceUntilIdle()

        val result = viewModel.actionResult.value
        assertTrue(result is ActionResult.Blocked)
    }
}
