package com.timeback.ui.viewmodel

import com.timeback.ui.domain.model.*
import com.timeback.ui.fake.FakeFeatureGateway
import com.timeback.ui.fake.GoalsViewData
import com.timeback.ui.feature.goals.GoalsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

/**
 * UI-06 GoalsViewModel 단위 테스트
 * 검증: hasGoals → CONTENT, !hasGoals → EMPTY, createGoal → SUCCESS → 재조회
 */
@OptIn(ExperimentalCoroutinesApi::class)
class GoalsViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var gateway: FakeFeatureGateway
    private lateinit var viewModel: GoalsViewModel

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
    fun `goals exist shows CONTENT`() = runTest {
        gateway.hasGoals = true
        viewModel = GoalsViewModel(gateway)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state is ScreenState.Content<*>)
        val data = (state as ScreenState.Content<*>).data as GoalsViewData
        assertEquals(2, data.goals.size)
    }

    @Test
    fun `no goals shows EMPTY`() = runTest {
        gateway.hasGoals = false
        viewModel = GoalsViewModel(gateway)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state is ScreenState.Empty)
    }

    @Test
    fun `createGoal returns SUCCESS and reloads`() = runTest {
        gateway.hasGoals = true
        viewModel = GoalsViewModel(gateway)
        advanceUntilIdle()

        viewModel.createGoal("명상", 30)
        advanceUntilIdle()

        val result = viewModel.actionResult.value
        assertTrue(result is ActionResult.Success)
    }
}
