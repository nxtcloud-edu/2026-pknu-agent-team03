package com.timeback.ui.viewmodel

import com.timeback.ui.domain.model.*
import com.timeback.ui.fake.AppManagementViewData
import com.timeback.ui.fake.FakeFeatureGateway
import com.timeback.ui.feature.apps.AppManagementViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

/**
 * UI-04 AppManagementViewModel 단위 테스트
 * 검증: loadApps → CONTENT, changeClassification → SUCCESS → 재조회
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AppManagementViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var gateway: FakeFeatureGateway
    private lateinit var viewModel: AppManagementViewModel

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        gateway = FakeFeatureGateway()
        viewModel = AppManagementViewModel(gateway)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadApps shows CONTENT with app list`() = runTest {
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state is ScreenState.Content<*>)
        val data = (state as ScreenState.Content<*>).data as AppManagementViewData
        assertEquals(4, data.apps.size)
    }

    @Test
    fun `changeClassification returns SUCCESS`() = runTest {
        advanceUntilIdle()

        viewModel.changeClassification("com.google.android.youtube", AppClassification.WASTE)
        advanceUntilIdle()

        val result = viewModel.actionResult.value
        assertTrue(result is ActionResult.Success)
    }
}
