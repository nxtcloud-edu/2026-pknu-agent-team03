package com.timeback.ui.viewmodel;

import com.timeback.ui.domain.model.ReportData;
import com.timeback.ui.domain.model.ReportPeriod;
import com.timeback.ui.domain.model.ScreenState;
import com.timeback.ui.fake.FakeFeatureGateway;
import com.timeback.ui.feature.report.ReportViewModel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ReportViewModel 단위 테스트.
 */
@ExtendWith(LiveDataTestExtension.class)
class ReportViewModelTest {

    private FakeFeatureGateway gateway;
    private ReportViewModel viewModel;

    @BeforeEach
    void setUp() {
        gateway = new FakeFeatureGateway();
    }

    @Test
    void loadReport_withBaselineReady_shouldReturnContent() throws Exception {
        gateway.baselineReady = true;
        viewModel = new ReportViewModel(gateway);
        Thread.sleep(300);

        ScreenState state = viewModel.state.getValue();
        assertNotNull(state);
        assertInstanceOf(ScreenState.Content.class, state);

        Object data = ((ScreenState.Content) state).getData();
        assertInstanceOf(ReportData.class, data);
        ReportData rd = (ReportData) data;
        assertEquals(ReportPeriod.WEEKLY, rd.getPeriod());
        assertTrue(rd.isBaselineComparable());
        assertFalse(rd.getGoalSummaries().isEmpty());
    }

    @Test
    void loadReport_withoutBaseline_shouldReturnEmpty() throws Exception {
        gateway.baselineReady = false;
        viewModel = new ReportViewModel(gateway);
        Thread.sleep(300);

        ScreenState state = viewModel.state.getValue();
        assertNotNull(state);
        assertInstanceOf(ScreenState.Empty.class, state);
    }

    @Test
    void changePeriod_shouldUpdateReport() throws Exception {
        gateway.baselineReady = true;
        viewModel = new ReportViewModel(gateway);
        Thread.sleep(300);

        viewModel.changePeriod(ReportPeriod.DAILY);
        Thread.sleep(300);

        ScreenState state = viewModel.state.getValue();
        assertInstanceOf(ScreenState.Content.class, state);
        ReportData rd = (ReportData) ((ScreenState.Content) state).getData();
        assertEquals(ReportPeriod.DAILY, rd.getPeriod());
    }
}
