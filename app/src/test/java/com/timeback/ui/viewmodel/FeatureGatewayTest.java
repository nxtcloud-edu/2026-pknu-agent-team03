package com.timeback.ui.viewmodel;

import com.timeback.ui.domain.model.*;
import com.timeback.ui.fake.FakeFeatureGateway;
import com.timeback.ui.fake.PermissionViewData;
import com.timeback.ui.fake.HomeViewData;
import com.timeback.ui.fake.GoalsViewData;
import com.timeback.ui.fake.TimelineViewData;
import com.timeback.ui.fake.mockdata.MockDataGateway;
import com.timeback.ui.fake.mockdata.MockScreenTimeData;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 순수 Java 단위 테스트 — Android 의존성 없이 FeatureGateway 상태 전이를 검증한다.
 * FakeFeatureGateway + MockDataGateway 모두 테스트.
 */
class FeatureGatewayTest {

    private FakeFeatureGateway fakeGateway;
    private MockDataGateway mockGateway;

    @BeforeEach
    void setUp() {
        fakeGateway = new FakeFeatureGateway();
        mockGateway = new MockDataGateway();
    }

    // === FakeFeatureGateway Tests ===

    @Test
    void fake_permissionGranted_returnsContent() throws Exception {
        fakeGateway.permissionGranted = true;
        ScreenState result = fakeGateway.readAccessState().get();
        assertInstanceOf(ScreenState.Content.class, result);
        PermissionViewData data = (PermissionViewData) ((ScreenState.Content) result).getData();
        assertTrue(data.canEnterMainScreens());
    }

    @Test
    void fake_permissionDenied_returnsBlocked() throws Exception {
        fakeGateway.permissionGranted = false;
        ScreenState result = fakeGateway.readAccessState().get();
        assertInstanceOf(ScreenState.Blocked.class, result);
        assertEquals(BlockReason.PERMISSION_REQUIRED, ((ScreenState.Blocked) result).getReason());
    }

    @Test
    void fake_readHome_returnsContentWithMetrics() throws Exception {
        fakeGateway.baselineReady = true;
        ScreenState result = fakeGateway.readHome().get();
        assertInstanceOf(ScreenState.Content.class, result);
        HomeViewData data = (HomeViewData) ((ScreenState.Content) result).getData();
        assertNotNull(data.getWasteTimeToday());
        assertNotNull(data.getRecoveryRate());
    }

    @Test
    void fake_readGoals_whenHasGoals_returnsContent() throws Exception {
        fakeGateway.hasGoals = true;
        ScreenState result = fakeGateway.readGoals().get();
        assertInstanceOf(ScreenState.Content.class, result);
        GoalsViewData data = (GoalsViewData) ((ScreenState.Content) result).getData();
        assertFalse(data.getGoals().isEmpty());
    }

    @Test
    void fake_readGoals_whenNoGoals_returnsEmpty() throws Exception {
        fakeGateway.hasGoals = false;
        ScreenState result = fakeGateway.readGoals().get();
        assertInstanceOf(ScreenState.Empty.class, result);
    }

    @Test
    void fake_readTimeline_returnsContentWithItems() throws Exception {
        long date = System.currentTimeMillis();
        ScreenState result = fakeGateway.readTimeline(date).get();
        assertInstanceOf(ScreenState.Content.class, result);
        TimelineViewData data = (TimelineViewData) ((ScreenState.Content) result).getData();
        assertFalse(data.getItems().isEmpty());
    }

    @Test
    void fake_confirmDeletion_setsDeletionComplete() throws Exception {
        ActionResult result = fakeGateway.confirmFullDeletion().get();
        assertInstanceOf(ActionResult.Success.class, result);
        assertTrue(fakeGateway.deletionComplete);
    }

    // === MockDataGateway Tests ===

    @Test
    void mock_readAccessState_alwaysContent() throws Exception {
        ScreenState result = mockGateway.readAccessState().get();
        assertInstanceOf(ScreenState.Content.class, result);
    }

    @Test
    void mock_readHome_returnsGoalProgress() throws Exception {
        ScreenState result = mockGateway.readHome().get();
        assertInstanceOf(ScreenState.Content.class, result);
        HomeViewData data = (HomeViewData) ((ScreenState.Content) result).getData();
        assertFalse(data.getGoalProgresses().isEmpty());
    }

    @Test
    void mock_readTimeline_returnsDay1Items() throws Exception {
        long day1Date = 1_733_670_000_000L; // BASE_DAY
        ScreenState result = mockGateway.readTimeline(day1Date).get();
        assertInstanceOf(ScreenState.Content.class, result);
        TimelineViewData data = (TimelineViewData) ((ScreenState.Content) result).getData();
        assertEquals(7, data.getItems().size()); // day1 has 7 items
    }

    // === MockScreenTimeData Tests ===

    @Test
    void mockData_allDays_returns7Days() {
        List<List<TimelineItem>> days = MockScreenTimeData.allDays();
        assertEquals(7, days.size());
    }

    @Test
    void mockData_day1_hasCorrectItems() {
        List<TimelineItem> day1 = MockScreenTimeData.day1();
        assertFalse(day1.isEmpty());
        // First item should be Instagram waste
        assertEquals("Instagram", day1.get(0).getAppName());
        assertEquals(ContextClassification.WASTE, day1.get(0).getClassification());
    }

    @Test
    void mockData_weeklySummary_hasValues() {
        MockScreenTimeData.WeeklySummary summary = MockScreenTimeData.computeWeeklySummary();
        assertTrue(summary.getTotalWaste().getMillis() > 0);
        assertTrue(summary.getTotalProductive().getMillis() > 0);
        assertEquals("Instagram", summary.getTopWasteApp());
    }

    @Test
    void mockData_appUsageSummaries_containsInstagram() {
        List<MockScreenTimeData.AppUsageSummary> summaries = MockScreenTimeData.computeAppUsageSummaries();
        boolean hasInstagram = summaries.stream()
                .anyMatch(s -> s.getAppName().equals("Instagram"));
        assertTrue(hasInstagram);
    }

    @Test
    void mock_createActivity_success() throws Exception {
        TimeRange validRange = new TimeRange(1000, 2000);
        ActionResult result = mockGateway.createActivity(ActivityType.STUDY, "공부", validRange).get();
        assertInstanceOf(ActionResult.Success.class, result);
    }

    @Test
    void mock_readReport_returnsData() throws Exception {
        ScreenState result = mockGateway.readReport(ReportPeriod.WEEKLY).get();
        assertInstanceOf(ScreenState.Content.class, result);
        ReportData data = (ReportData) ((ScreenState.Content) result).getData();
        assertEquals(ReportPeriod.WEEKLY, data.getPeriod());
        assertNotNull(data.getMetrics());
    }
}
