package com.timeback.ui.viewmodel

import com.timeback.ui.domain.model.*
import com.timeback.ui.fake.FakeFeatureGateway
import com.timeback.ui.fake.TimelineViewData
import com.timeback.ui.feature.timeline.TimelineViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

/**
 * UI-03 TimelineViewModel 단위 테스트
 * 검증: loadTimeline → CONTENT, confirmContext → SUCCESS → 재조회, createActivity INVALID_TIME_RANGE
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TimelineViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var gateway: FakeFeatureGateway
    private lateinit var viewModel: TimelineViewModel

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        gateway = FakeFeatureGateway()
        viewModel = TimelineViewModel(gateway)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadTimeline shows CONTENT with items`() = runTest {
        viewModel.loadTimeline(1724630400000)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state is ScreenState.Content<*>)
        val data = (state as ScreenState.Content<*>).data as TimelineViewData
        assertTrue(data.items.isNotEmpty())
    }

    @Test
    fun `confirmContext returns SUCCESS and reloads`() = runTest {
        viewModel.loadTimeline(1724630400000)
        advanceUntilIdle()

        viewModel.confirmContext(Identifier("item-1"))
        advanceUntilIdle()

        val result = viewModel.actionResult.value
        assertTrue(result is ActionResult.Success)
    }

    @Test
    fun `createActivity with invalid time range returns BLOCKED`() = runTest {
        viewModel.loadTimeline(1724630400000)
        advanceUntilIdle()

        // endAt before startAt
        viewModel.createActivity(
            ActivityType.STUDY,
            "공부",
            TimeRange(startAt = 1724641200000, endAt = 1724630400000)
        )
        advanceUntilIdle()

        val result = viewModel.actionResult.value
        assertTrue(result is ActionResult.Blocked)
        assertEquals(BlockReason.INVALID_TIME_RANGE, (result as ActionResult.Blocked).reason)
    }
}
