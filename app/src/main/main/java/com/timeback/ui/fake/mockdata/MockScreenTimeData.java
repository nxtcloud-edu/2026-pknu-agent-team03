package com.timeback.ui.fake.mockdata;

import com.timeback.ui.domain.model.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 취업 준비 중인 대학교 4학년생의 7일간 스크린타임 목 데이터.
 * SNS(Instagram, TikTok, X) 과다 사용 패턴 시뮬레이션.
 */
public final class MockScreenTimeData {

    private MockScreenTimeData() {}

    // --- 기준 시각: 2024-12-09 월요일 00:00 KST (epoch millis) ---
    private static final long BASE_DAY = 1_733_670_000_000L;
    private static final long ONE_DAY = 86_400_000L;
    private static final long ONE_HOUR = 3_600_000L;
    private static final long ONE_MIN = 60_000L;

    // --- Helper ---
    public static TimelineItem item(String id, String appName, String packageName,
                                     String activity, long dayOffset, long startHour, long startMin,
                                     long durationMin, ContextClassification classification,
                                     boolean isComplex, boolean confirmRequired) {
        long dayStart = BASE_DAY + dayOffset * ONE_DAY;
        long start = dayStart + startHour * ONE_HOUR + startMin * ONE_MIN;
        long end = start + durationMin * ONE_MIN;
        return new TimelineItem(
                new Identifier(id), appName, packageName, activity,
                new TimeRange(start, end), new Duration(durationMin * ONE_MIN),
                classification, isComplex, confirmRequired
        );
    }

    // --- Day 1: 월요일 (SNS 폭탄 시작) ---
    public static List<TimelineItem> day1() {
        return Arrays.asList(
                item("d1-1", "Instagram", "com.instagram.android", "피드 스크롤", 0, 7, 30, 45, ContextClassification.WASTE, false, false),
                item("d1-2", "Chrome", "com.android.chrome", "채용공고 검색", 0, 9, 0, 90, ContextClassification.PRODUCTIVE, false, false),
                item("d1-3", "TikTok", "com.zhiliaoapp.musically", "숏폼 시청", 0, 11, 0, 60, ContextClassification.WASTE, false, true),
                item("d1-4", "Google Docs", "com.google.android.apps.docs", "자기소개서 작성", 0, 13, 0, 120, ContextClassification.PRODUCTIVE, false, false),
                item("d1-5", "X", "com.twitter.android", "트윗 브라우징", 0, 15, 30, 40, ContextClassification.LEISURE, false, false),
                item("d1-6", "Instagram", "com.instagram.android", "릴스 시청", 0, 18, 0, 90, ContextClassification.WASTE, false, false),
                item("d1-7", "YouTube", "com.google.android.youtube", "면접 팁 영상", 0, 20, 0, 60, ContextClassification.MIXED, true, true)
        );
    }

    // --- Day 2: 화요일 (면접 준비 + SNS 유혹) ---
    public static List<TimelineItem> day2() {
        return Arrays.asList(
                item("d2-1", "Instagram", "com.instagram.android", "스토리 확인", 1, 8, 0, 30, ContextClassification.WASTE, false, false),
                item("d2-2", "Notion", "notion.id", "면접 질문 정리", 1, 9, 0, 120, ContextClassification.PRODUCTIVE, false, false),
                item("d2-3", "TikTok", "com.zhiliaoapp.musically", "취준생 밈 시청", 1, 12, 0, 50, ContextClassification.WASTE, false, false),
                item("d2-4", "Chrome", "com.android.chrome", "포트폴리오 참고", 1, 14, 0, 90, ContextClassification.PRODUCTIVE, false, false),
                item("d2-5", "X", "com.twitter.android", "IT 뉴스 읽기", 1, 16, 0, 35, ContextClassification.LEISURE, false, true),
                item("d2-6", "Instagram", "com.instagram.android", "릴스 무한 스크롤", 1, 19, 0, 80, ContextClassification.WASTE, false, false),
                item("d2-7", "TikTok", "com.zhiliaoapp.musically", "야간 숏폼", 1, 22, 0, 45, ContextClassification.WASTE, false, false)
        );
    }

    // --- Day 3: 수요일 (의지력 고갈) ---
    public static List<TimelineItem> day3() {
        return Arrays.asList(
                item("d3-1", "TikTok", "com.zhiliaoapp.musically", "아침 숏폼", 2, 7, 0, 60, ContextClassification.WASTE, false, false),
                item("d3-2", "Instagram", "com.instagram.android", "탐색 탭 브라우징", 2, 9, 0, 40, ContextClassification.WASTE, false, false),
                item("d3-3", "VS Code", "com.visualstudio.code", "사이드 프로젝트 코딩", 2, 10, 0, 150, ContextClassification.PRODUCTIVE, false, false),
                item("d3-4", "X", "com.twitter.android", "개발자 트위터 탐색", 2, 13, 0, 45, ContextClassification.MIXED, true, true),
                item("d3-5", "Instagram", "com.instagram.android", "DM 확인 + 피드", 2, 15, 0, 55, ContextClassification.WASTE, false, false),
                item("d3-6", "Netflix", "com.netflix.mediaclient", "드라마 시청", 2, 20, 0, 120, ContextClassification.LEISURE, false, false)
        );
    }

    // --- Day 4: 목요일 (약간 회복) ---
    public static List<TimelineItem> day4() {
        return Arrays.asList(
                item("d4-1", "Chrome", "com.android.chrome", "알고리즘 문제 풀기", 3, 8, 0, 120, ContextClassification.PRODUCTIVE, false, false),
                item("d4-2", "Instagram", "com.instagram.android", "점심시간 인스타", 3, 12, 0, 25, ContextClassification.WASTE, false, false),
                item("d4-3", "Notion", "notion.id", "프로젝트 문서화", 3, 13, 0, 90, ContextClassification.PRODUCTIVE, false, false),
                item("d4-4", "TikTok", "com.zhiliaoapp.musically", "코딩 팁 영상", 3, 15, 30, 30, ContextClassification.MIXED, true, true),
                item("d4-5", "Chrome", "com.android.chrome", "기업 분석", 3, 16, 30, 60, ContextClassification.PRODUCTIVE, false, false),
                item("d4-6", "X", "com.twitter.android", "저녁 트위터", 3, 20, 0, 40, ContextClassification.LEISURE, false, false),
                item("d4-7", "Instagram", "com.instagram.android", "자기 전 릴스", 3, 23, 0, 35, ContextClassification.WASTE, false, false)
        );
    }

    // --- Day 5: 금요일 (SNS 다시 폭주) ---
    public static List<TimelineItem> day5() {
        return Arrays.asList(
                item("d5-1", "Instagram", "com.instagram.android", "모닝 피드", 4, 8, 0, 50, ContextClassification.WASTE, false, false),
                item("d5-2", "TikTok", "com.zhiliaoapp.musically", "오전 숏폼", 4, 9, 30, 70, ContextClassification.WASTE, false, false),
                item("d5-3", "Chrome", "com.android.chrome", "지원서 제출", 4, 11, 30, 60, ContextClassification.PRODUCTIVE, false, false),
                item("d5-4", "X", "com.twitter.android", "트렌드 확인", 4, 13, 0, 45, ContextClassification.LEISURE, false, false),
                item("d5-5", "Instagram", "com.instagram.android", "친구 스토리 답장", 4, 14, 30, 40, ContextClassification.LEISURE, false, false),
                item("d5-6", "TikTok", "com.zhiliaoapp.musically", "밤 숏폼 루프", 4, 21, 0, 100, ContextClassification.WASTE, false, false)
        );
    }

    // --- Day 6: 토요일 (자유 시간 + SNS 과몰입) ---
    public static List<TimelineItem> day6() {
        return Arrays.asList(
                item("d6-1", "Instagram", "com.instagram.android", "늦잠 후 인스타", 5, 10, 0, 60, ContextClassification.WASTE, false, false),
                item("d6-2", "TikTok", "com.zhiliaoapp.musically", "점심까지 숏폼", 5, 11, 30, 90, ContextClassification.WASTE, false, false),
                item("d6-3", "YouTube", "com.google.android.youtube", "개발 강의 시청", 5, 14, 0, 120, ContextClassification.PRODUCTIVE, false, false),
                item("d6-4", "X", "com.twitter.android", "주말 트위터", 5, 17, 0, 50, ContextClassification.LEISURE, false, false),
                item("d6-5", "Instagram", "com.instagram.android", "저녁 릴스", 5, 19, 30, 75, ContextClassification.WASTE, false, false),
                item("d6-6", "TikTok", "com.zhiliaoapp.musically", "심야 숏폼", 5, 23, 0, 60, ContextClassification.WASTE, false, false)
        );
    }

    // --- Day 7: 일요일 (반성 + 목표 설정) ---
    public static List<TimelineItem> day7() {
        return Arrays.asList(
                item("d7-1", "Instagram", "com.instagram.android", "아침 확인", 6, 9, 0, 20, ContextClassification.WASTE, false, false),
                item("d7-2", "Notion", "notion.id", "주간 회고 작성", 6, 10, 0, 90, ContextClassification.PRODUCTIVE, false, false),
                item("d7-3", "Chrome", "com.android.chrome", "시간관리 앱 리서치", 6, 12, 0, 45, ContextClassification.PRODUCTIVE, false, false),
                item("d7-4", "TikTok", "com.zhiliaoapp.musically", "잠깐 숏폼", 6, 14, 0, 25, ContextClassification.WASTE, false, false),
                item("d7-5", "Chrome", "com.android.chrome", "주간 계획 수립", 6, 15, 0, 60, ContextClassification.PRODUCTIVE, false, false),
                item("d7-6", "X", "com.twitter.android", "개발 커뮤니티", 6, 17, 0, 30, ContextClassification.MIXED, true, true),
                item("d7-7", "Instagram", "com.instagram.android", "자기 전 짧게", 6, 22, 0, 15, ContextClassification.WASTE, false, false)
        );
    }

    /** 7일 전체 데이터를 반환한다. index 0 = Day 1(월). */
    public static List<List<TimelineItem>> allDays() {
        return Arrays.asList(day1(), day2(), day3(), day4(), day5(), day6(), day7());
    }

    // --- Inner Classes ---

    /** 하루치 요약 데이터 */
    public static class DayData {
        private final long dateEpoch;
        private final List<TimelineItem> items;
        private final Duration totalWaste;
        private final Duration totalProductive;

        public DayData(long dateEpoch, List<TimelineItem> items, Duration totalWaste, Duration totalProductive) {
            this.dateEpoch = dateEpoch;
            this.items = items;
            this.totalWaste = totalWaste;
            this.totalProductive = totalProductive;
        }

        public long getDateEpoch() { return dateEpoch; }
        public List<TimelineItem> getItems() { return items; }
        public Duration getTotalWaste() { return totalWaste; }
        public Duration getTotalProductive() { return totalProductive; }
    }

    /** 주간 요약 */
    public static class WeeklySummary {
        private final Duration totalWaste;
        private final Duration totalProductive;
        private final Duration totalLeisure;
        private final String topWasteApp;
        private final Duration topWasteAppDuration;

        public WeeklySummary(Duration totalWaste, Duration totalProductive, Duration totalLeisure,
                             String topWasteApp, Duration topWasteAppDuration) {
            this.totalWaste = totalWaste;
            this.totalProductive = totalProductive;
            this.totalLeisure = totalLeisure;
            this.topWasteApp = topWasteApp;
            this.topWasteAppDuration = topWasteAppDuration;
        }

        public Duration getTotalWaste() { return totalWaste; }
        public Duration getTotalProductive() { return totalProductive; }
        public Duration getTotalLeisure() { return totalLeisure; }
        public String getTopWasteApp() { return topWasteApp; }
        public Duration getTopWasteAppDuration() { return topWasteAppDuration; }
    }

    /** 앱별 사용량 요약 */
    public static class AppUsageSummary {
        private final String appName;
        private final String packageName;
        private final Duration totalDuration;
        private final int sessionCount;
        private final ContextClassification dominantClassification;

        public AppUsageSummary(String appName, String packageName, Duration totalDuration,
                               int sessionCount, ContextClassification dominantClassification) {
            this.appName = appName;
            this.packageName = packageName;
            this.totalDuration = totalDuration;
            this.sessionCount = sessionCount;
            this.dominantClassification = dominantClassification;
        }

        public String getAppName() { return appName; }
        public String getPackageName() { return packageName; }
        public Duration getTotalDuration() { return totalDuration; }
        public int getSessionCount() { return sessionCount; }
        public ContextClassification getDominantClassification() { return dominantClassification; }
    }

    /** 주간 요약 계산 */
    public static WeeklySummary computeWeeklySummary() {
        // Instagram 총 낭비: 약 495분 (가장 많이 사용한 낭비 앱)
        return new WeeklySummary(
                new Duration(55_500_000),    // ~925min 주간 낭비
                new Duration(64_200_000),    // ~1070min 주간 생산
                new Duration(19_800_000),    // ~330min 주간 레저
                "Instagram",
                new Duration(29_700_000)     // ~495min
        );
    }

    /** 앱별 사용량 요약 목록 */
    public static List<AppUsageSummary> computeAppUsageSummaries() {
        return Arrays.asList(
                new AppUsageSummary("Instagram", "com.instagram.android", new Duration(29_700_000), 14, ContextClassification.WASTE),
                new AppUsageSummary("TikTok", "com.zhiliaoapp.musically", new Duration(21_600_000), 10, ContextClassification.WASTE),
                new AppUsageSummary("Chrome", "com.android.chrome", new Duration(27_900_000), 8, ContextClassification.PRODUCTIVE),
                new AppUsageSummary("X", "com.twitter.android", new Duration(17_100_000), 7, ContextClassification.LEISURE),
                new AppUsageSummary("Notion", "notion.id", new Duration(18_000_000), 3, ContextClassification.PRODUCTIVE),
                new AppUsageSummary("YouTube", "com.google.android.youtube", new Duration(10_800_000), 2, ContextClassification.MIXED),
                new AppUsageSummary("VS Code", "com.visualstudio.code", new Duration(9_000_000), 1, ContextClassification.PRODUCTIVE),
                new AppUsageSummary("Google Docs", "com.google.android.apps.docs", new Duration(7_200_000), 1, ContextClassification.PRODUCTIVE),
                new AppUsageSummary("Netflix", "com.netflix.mediaclient", new Duration(7_200_000), 1, ContextClassification.LEISURE)
        );
    }
}
