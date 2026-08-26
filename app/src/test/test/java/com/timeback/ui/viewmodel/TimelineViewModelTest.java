package com.timeback.ui.viewmodel;

import com.timeback.ui.domain.model.ScreenState;
import com.timeback.ui.fake.FakeFeatureGateway;
import com.timeback.ui.fake.TimelineViewData;
import com.timeback.ui.feature.timeline.TimelineViewModel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TimelineViewModel 단위 테스트.
 */
class TimelineViewModelTest {

    private FakeFeatureGateway gateway;
    private TimelineViewModel viewModel;

    @BeforeEach
    void setUp() {
        gateway = new FakeFeatureGateway();
    }

    @Test
    void loadTimeline_shouldReturnContentWithTimelineItems() throws Exception {
        viewModel = new TimelineViewModel(gateway);
        Thread.sleep(300);

        ScreenState state = viewModel.state.getValue();
        assertNotNull(state);
        assertInstanceOf(ScreenState.Content.class, state);

        Object data = ((ScreenState.Content) state).getData();
        assertInstanceOf(TimelineViewData.class, data);
        TimelineViewData tvd = (TimelineViewData) data;
        assertFalse(tvd.getItems().isEmpty());
        assertEquals(3, tvd.getItems().size());
    }

    @Test
    void loadTimeline_shouldContainExpectedApps() throws Exception {
        viewModel = new TimelineViewModel(gateway);
        Thread.sleep(300);

        ScreenState state = viewModel.state.getValue();
        TimelineViewData data = (TimelineViewData) ((ScreenState.Content) state).getData();
        assertEquals("Instagram", data.getItems().get(0).getAppName());
        assertEquals("Chrome", data.getItems().get(1).getAppName());
        assertEquals("TikTok", data.getItems().get(2).getAppName());
    }

    @Test
    void refresh_shouldReturnSameContent() throws Exception {
        viewModel = new TimelineViewModel(gateway);
        Thread.sleep(300);

        viewModel.refresh();
        Thread.sleep(300);

        ScreenState state = viewModel.state.getValue();
        assertInstanceOf(ScreenState.Content.class, state);
    }
}
