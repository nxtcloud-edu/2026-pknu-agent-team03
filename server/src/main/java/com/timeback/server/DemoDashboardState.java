package com.timeback.server;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
@Profile("demo")
final class DemoDashboardState {
    private static final long BASE_DAY = 1_733_670_000_000L;
    private static final long ONE_DAY = 86_400_000L;
    private static final long ONE_HOUR = 3_600_000L;
    private static final long ONE_MINUTE = 60_000L;
    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
    static final Set<String> CLASSIFICATIONS = Set.of(
            "WASTE", "PRODUCTIVE", "LEISURE", "MIXED", "NEUTRAL"
    );
    static final Set<String> RETENTION_SELECTIONS = Set.of(
            "7_DAYS", "30_DAYS", "90_DAYS", "UNLIMITED"
    );

    private final List<List<TimelineItem>> timeline = new ArrayList<>();
    private final List<Goal> goals = new ArrayList<>();
    private String backupStatus;
    private long lastSyncAt;
    private int pendingCount;
    private String retentionSelection;

    DemoDashboardState() {
        reset();
    }

    synchronized void reset() {
        timeline.clear();
        timeline.add(new ArrayList<>(List.of(
                item("d1-1", "Instagram", "com.instagram.android", "피드 스크롤", 0, 7, 30, 45, "WASTE"),
                item("d1-2", "Chrome", "com.android.chrome", "채용공고 검색", 0, 9, 0, 90, "PRODUCTIVE"),
                item("d1-3", "TikTok", "com.zhiliaoapp.musically", "숏폼 시청", 0, 11, 0, 60, "WASTE"),
                item("d1-4", "Google Docs", "com.google.android.apps.docs", "자기소개서 작성", 0, 13, 0, 120, "PRODUCTIVE"),
                item("d1-5", "X", "com.twitter.android", "트윗 브라우징", 0, 15, 30, 40, "LEISURE"),
                item("d1-6", "Instagram", "com.instagram.android", "릴스 시청", 0, 18, 0, 90, "WASTE"),
                item("d1-7", "YouTube", "com.google.android.youtube", "면접 팁 영상", 0, 20, 0, 60, "MIXED")
        )));
        timeline.add(new ArrayList<>(List.of(
                item("d2-1", "Instagram", "com.instagram.android", "스토리 확인", 1, 8, 0, 30, "WASTE"),
                item("d2-2", "Notion", "notion.id", "면접 질문 정리", 1, 9, 0, 120, "PRODUCTIVE"),
                item("d2-3", "TikTok", "com.zhiliaoapp.musically", "취준생 밈 시청", 1, 12, 0, 50, "WASTE"),
                item("d2-4", "Chrome", "com.android.chrome", "포트폴리오 참고", 1, 14, 0, 90, "PRODUCTIVE"),
                item("d2-5", "X", "com.twitter.android", "IT 뉴스 읽기", 1, 16, 0, 35, "LEISURE"),
                item("d2-6", "Instagram", "com.instagram.android", "릴스 무한 스크롤", 1, 19, 0, 80, "WASTE"),
                item("d2-7", "TikTok", "com.zhiliaoapp.musically", "야간 숏폼", 1, 22, 0, 45, "WASTE")
        )));
        timeline.add(new ArrayList<>(List.of(
                item("d3-1", "TikTok", "com.zhiliaoapp.musically", "아침 숏폼", 2, 7, 0, 60, "WASTE"),
                item("d3-2", "Instagram", "com.instagram.android", "탐색 탭", 2, 9, 0, 40, "WASTE"),
                item("d3-3", "VS Code", "com.visualstudio.code", "사이드 프로젝트", 2, 10, 0, 150, "PRODUCTIVE"),
                item("d3-4", "X", "com.twitter.android", "개발자 트위터", 2, 13, 0, 45, "MIXED"),
                item("d3-5", "Instagram", "com.instagram.android", "DM 확인+피드", 2, 15, 0, 55, "WASTE"),
                item("d3-6", "Netflix", "com.netflix.mediaclient", "드라마 시청", 2, 20, 0, 120, "LEISURE")
        )));
        timeline.add(new ArrayList<>(List.of(
                item("d4-1", "Chrome", "com.android.chrome", "알고리즘 문제", 3, 8, 0, 120, "PRODUCTIVE"),
                item("d4-2", "Instagram", "com.instagram.android", "점심 인스타", 3, 12, 0, 25, "WASTE"),
                item("d4-3", "Notion", "notion.id", "프로젝트 문서화", 3, 13, 0, 90, "PRODUCTIVE"),
                item("d4-4", "TikTok", "com.zhiliaoapp.musically", "코딩 팁 영상", 3, 15, 30, 30, "MIXED"),
                item("d4-5", "Chrome", "com.android.chrome", "기업 분석", 3, 16, 30, 60, "PRODUCTIVE"),
                item("d4-6", "X", "com.twitter.android", "저녁 트위터", 3, 20, 0, 40, "LEISURE"),
                item("d4-7", "Instagram", "com.instagram.android", "자기 전 릴스", 3, 23, 0, 35, "WASTE")
        )));
        timeline.add(new ArrayList<>(List.of(
                item("d5-1", "Instagram", "com.instagram.android", "모닝 피드", 4, 8, 0, 50, "WASTE"),
                item("d5-2", "TikTok", "com.zhiliaoapp.musically", "오전 숏폼", 4, 9, 30, 70, "WASTE"),
                item("d5-3", "Chrome", "com.android.chrome", "지원서 제출", 4, 11, 30, 60, "PRODUCTIVE"),
                item("d5-4", "X", "com.twitter.android", "트렌드 확인", 4, 13, 0, 45, "LEISURE"),
                item("d5-5", "Instagram", "com.instagram.android", "친구 스토리", 4, 14, 30, 40, "LEISURE"),
                item("d5-6", "TikTok", "com.zhiliaoapp.musically", "밤 숏폼 루프", 4, 21, 0, 100, "WASTE")
        )));
        timeline.add(new ArrayList<>(List.of(
                item("d6-1", "Instagram", "com.instagram.android", "늦잠 후 인스타", 5, 10, 0, 60, "WASTE"),
                item("d6-2", "TikTok", "com.zhiliaoapp.musically", "점심까지 숏폼", 5, 11, 30, 90, "WASTE"),
                item("d6-3", "YouTube", "com.google.android.youtube", "개발 강의", 5, 14, 0, 120, "PRODUCTIVE"),
                item("d6-4", "X", "com.twitter.android", "주말 트위터", 5, 17, 0, 50, "LEISURE"),
                item("d6-5", "Instagram", "com.instagram.android", "저녁 릴스", 5, 19, 30, 75, "WASTE"),
                item("d6-6", "TikTok", "com.zhiliaoapp.musically", "심야 숏폼", 5, 23, 0, 60, "WASTE")
        )));
        timeline.add(new ArrayList<>(List.of(
                item("d7-1", "Instagram", "com.instagram.android", "아침 확인", 6, 9, 0, 20, "WASTE"),
                item("d7-2", "Notion", "notion.id", "주간 회고", 6, 10, 0, 90, "PRODUCTIVE"),
                item("d7-3", "Chrome", "com.android.chrome", "시간관리 앱 리서치", 6, 12, 0, 45, "PRODUCTIVE"),
                item("d7-4", "TikTok", "com.zhiliaoapp.musically", "잠깐 숏폼", 6, 14, 0, 25, "WASTE"),
                item("d7-5", "Chrome", "com.android.chrome", "주간 계획 수립", 6, 15, 0, 60, "PRODUCTIVE"),
                item("d7-6", "X", "com.twitter.android", "개발 커뮤니티", 6, 17, 0, 30, "MIXED"),
                item("d7-7", "Instagram", "com.instagram.android", "자기 전 짧게", 6, 22, 0, 15, "WASTE")
        )));

        goals.clear();
        goals.add(new Goal("g1", "코딩 공부", 300, 210));
        goals.add(new Goal("g2", "포트폴리오 정리", 180, 120));
        goals.add(new Goal("g3", "운동", 150, 45));
        goals.add(new Goal("g4", "독서", 120, 30));

        backupStatus = "DEMO_SYNCED";
        lastSyncAt = System.currentTimeMillis();
        pendingCount = 0;
        retentionSelection = "30_DAYS";
    }

    synchronized List<TimelineItem> timeline(int day) {
        return List.copyOf(timeline.get(day));
    }

    synchronized List<List<TimelineItem>> allTimeline() {
        return timeline.stream().map(List::copyOf).toList();
    }

    synchronized TimelineItem classify(String itemId, String classification, String activity) {
        for (List<TimelineItem> day : timeline) {
            for (int index = 0; index < day.size(); index++) {
                TimelineItem current = day.get(index);
                if (current.id().equals(itemId)) {
                    TimelineItem updated = new TimelineItem(
                            current.id(),
                            current.app(),
                            current.packageName(),
                            activity == null ? current.activity() : activity,
                            current.startAt(),
                            current.endAt(),
                            current.durationMin(),
                            classification == null ? current.classification() : classification,
                            true
                    );
                    day.set(index, updated);
                    return updated;
                }
            }
        }
        return null;
    }

    synchronized List<Goal> goals() {
        return List.copyOf(goals);
    }

    synchronized Goal addGoal(String name, int targetMin) {
        Goal goal = new Goal(UUID.randomUUID().toString().substring(0, 8), name, targetMin, 0);
        goals.add(goal);
        return goal;
    }

    synchronized Goal recordGoal(String goalId, int minutes) {
        for (int index = 0; index < goals.size(); index++) {
            Goal current = goals.get(index);
            if (current.id().equals(goalId)) {
                Goal updated = new Goal(
                        current.id(),
                        current.name(),
                        current.targetMin(),
                        (int) Math.min(Integer.MAX_VALUE, Math.max(0L, (long) current.doneMin() + minutes))
                );
                goals.set(index, updated);
                return updated;
            }
        }
        return null;
    }

    synchronized boolean deleteGoal(String goalId) {
        return goals.removeIf(goal -> goal.id().equals(goalId));
    }

    synchronized WeeklyMetrics weeklyMetrics() {
        int todayIndex = DayOfWeek.from(LocalDate.now(SEOUL)).getValue() - 1;
        List<Integer> dailyWaste = timeline.stream()
                .map(day -> sumDuration(day, "WASTE"))
                .toList();
        int totalWaste = dailyWaste.stream().mapToInt(Integer::intValue).sum();
        int totalProductive = timeline.stream().mapToInt(day -> sumDuration(day, "PRODUCTIVE")).sum();
        int totalLeisure = timeline.stream().mapToInt(day -> sumDuration(day, "LEISURE")).sum();
        int baseline = totalWaste / 7;
        int todayWaste = dailyWaste.get(todayIndex);
        int todayTotal = timeline.get(todayIndex).stream().mapToInt(TimelineItem::durationMin).sum();
        int saved = Math.max(0, baseline - todayWaste);
        int recovered = goals.stream().mapToInt(Goal::doneMin).sum();
        int recoveryRate = saved == 0 ? 0 : Math.round((float) recovered / saved * 100);
        return new WeeklyMetrics(
                totalWaste,
                totalProductive,
                totalLeisure,
                baseline,
                todayWaste,
                todayTotal,
                saved,
                recovered,
                recoveryRate,
                dailyWaste,
                todayIndex
        );
    }

    synchronized BackupState backupState() {
        return new BackupState(backupStatus, lastSyncAt, pendingCount);
    }

    synchronized BackupState triggerSyntheticSync() {
        backupStatus = "DEMO_SYNCED";
        lastSyncAt = System.currentTimeMillis();
        pendingCount = 0;
        return backupState();
    }

    synchronized RetentionState retention() {
        return new RetentionState(retentionSelection);
    }

    synchronized RetentionState updateRetention(String selection) {
        retentionSelection = selection;
        return retention();
    }

    synchronized DeletionResult deleteSyntheticServerData() {
        timeline.forEach(List::clear);
        goals.clear();
        pendingCount = 0;
        backupStatus = "DEMO_DELETED";
        lastSyncAt = System.currentTimeMillis();
        return new DeletionResult(
                UUID.randomUUID().toString().substring(0, 8),
                "PENDING",
                "COMPLETED",
                lastSyncAt
        );
    }

    private static int sumDuration(List<TimelineItem> items, String classification) {
        return items.stream()
                .filter(item -> item.classification().equals(classification))
                .mapToInt(TimelineItem::durationMin)
                .sum();
    }

    private static TimelineItem item(
            String id,
            String app,
            String packageName,
            String activity,
            int day,
            int hour,
            int minute,
            int durationMin,
            String classification
    ) {
        long startAt = BASE_DAY + day * ONE_DAY + hour * ONE_HOUR + minute * ONE_MINUTE;
        return new TimelineItem(
                id,
                app,
                packageName,
                activity,
                startAt,
                startAt + durationMin * ONE_MINUTE,
                durationMin,
                classification,
                false
        );
    }

    record TimelineItem(
            String id,
            String app,
            @JsonProperty("package") String packageName,
            String activity,
            long startAt,
            long endAt,
            int durationMin,
            String classification,
            boolean userConfirmed
    ) {}

    record Goal(String id, String name, int targetMin, int doneMin) {}

    record WeeklyMetrics(
            int totalWasteMin,
            int totalProductiveMin,
            int totalLeisureMin,
            int baselineMin,
            int todayWasteMin,
            int todayTotalMin,
            int savedMin,
            int recoveredMin,
            int recoveryRate,
            List<Integer> dailyWaste,
            int todayIndex
    ) {}

    record BackupState(String status, long lastSyncAt, int pendingCount) {}

    record RetentionState(String selection) {}

    record DeletionResult(
            String jobId,
            String deviceStatus,
            String serverStatus,
            long completedAt
    ) {}
}
