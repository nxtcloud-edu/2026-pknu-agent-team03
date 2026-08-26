package com.timeback.ui.viewmodel

import com.timeback.ui.domain.model.*
import com.timeback.ui.fake.FakeFeatureGateway
import com.timeback.ui.feature.report.ReportViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

/**
 * UI-07 ReportViewModel 단위 테스트
 * 검증: loadReport → CONTENT, changePeriod 전환
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ReportViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var gateway: FakeFeatureGateway
    private lateinit var viewModel: ReportViewModel

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        gateway = FakeFeatureGateway()
        viewModel = ReportViewModel(gateway)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial load shows CONTENT with report data`() = runTest {
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state is ScreenState.Content<*>)
        val data = (state as ScreenState.Content<*>).data as ReportData
        assertEquals(ReportPeriod.WEEKLY, data.period)
        assertTrue(data.baselineComparable)
    }

    @Test
    fun `changePeriod reloads report`() = runTest {
        advanceUntilIdle()

        viewModel.changePeriod(ReportPeriod.DAILY)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state is ScreenState.Content<*>)
    }
}
