package com.timeback.ui.fake.mockdata

import com.timeback.ui.domain.model.*

/**
 * 페르소나: 취업준비생 대학교 4학년 (김민수, 23세)
 * 
 * 특징:
 * - SNS(인스타그램, 트위터/X, 틱톡) 사용이 하루 평균 4~5시간으로 과다
 * - 취업 준비(사람인, 잡코리아, 링크드인)는 하루 30분~1시간 정도만 사용
 * - 개발 공부(VS Code, 인프런, 노션)를 하려고 하지만 SNS에 빠져 시간 분배 실패
 * - YouTube는 취업 면접 영상과 숏츠를 섞어 보므로 MIXED 상태 자주 발생
 * - 평일은 카페에서 공부하다가 SNS에 빠지는 패턴, 주말은 더 심함
 * 
 * 기간: 2024-08-19(월) ~ 2024-08-25(일) 일주일
 * Baseline 관찰용으로 7일 연속 데이터
 */
object MockScreenTimeData {

    // === 앱 분류 기본값 ===
    val appClassifications = listOf(
        AppInfo("com.instagram.android", "Instagram", AppClassification.WASTE),
        AppInfo("com.twitter.android", "X (Twitter)", AppClassification.WASTE),
        AppInfo("com.zhiliaoapp.musically", "TikTok", AppClassification.WASTE),
        AppInfo("com.google.android.youtube", "YouTube", AppClassification.NEUTRAL),
        AppInfo("com.android.chrome", "Chrome", AppClassification.NEUTRAL),
        AppInfo("kr.co.saramin.android", "사람인", AppClassification.PRODUCTIVE),
        AppInfo("net.jobkorea.app", "잡코리아", AppClassification.PRODUCTIVE),
        AppInfo("com.linkedin.android", "LinkedIn", AppClassification.PRODUCTIVE),
        AppInfo("com.microsoft.vscode", "VS Code", AppClassification.PRODUCTIVE),
        AppInfo("com.inflearn.app", "인프런", AppClassification.PRODUCTIVE),
        AppInfo("notion.id", "Notion", AppClassification.PRODUCTIVE),
        AppInfo("com.kakao.talk", "카카오톡", AppClassification.NEUTRAL),
        AppInfo("com.nhn.android.nmap", "네이버 지도", AppClassification.NEUTRAL),
        AppInfo("com.spotify.music", "Spotify", AppClassification.LEISURE),
        AppInfo("com.netflix.mediaclient", "Netflix", AppClassification.LEISURE)
    )

    // === 일주일 치 일별 Timeline 데이터 ===

    /** 8/19 (월) - 카페에서 공부 시도, SNS에 빠짐 */
    val day1_monday = DayData(
        date = 1724025600000, // 2024-08-19 00:00 KST epoch
        timelineItems = listOf(
            // 오전: 일어나서 SNS 확인
            item("d1-1", "Instagram", "com.instagram.android", 8, 0, 8, 45, ContextClassification.WASTE),
            item("d1-2", "X (Twitter)", "com.twitter.android", 8, 45, 9, 10, ContextClassification.WASTE),
            // 카페 이동 후 공부 시작
            item("d1-3", "VS Code", "com.microsoft.vscode", 9, 30, 10, 45, ContextClassification.PRODUCTIVE),
            item("d1-4", "인프런", "com.inflearn.app", 10, 45, 11, 30, ContextClassification.PRODUCTIVE),
            // 점심 후 SNS 폭주
            item("d1-5", "Instagram", "com.instagram.android", 12, 30, 13, 45, ContextClassification.WASTE),
            item("d1-6", "TikTok", "com.zhiliaoapp.musically", 13, 45, 14, 50, ContextClassification.WASTE),
            // 취업 사이트 잠깐
            item("d1-7", "사람인", "kr.co.saramin.android", 15, 0, 15, 25, ContextClassification.PRODUCTIVE),
            item("d1-8", "잡코리아", "net.jobkorea.app", 15, 25, 15, 45, ContextClassification.PRODUCTIVE),
            // YouTube (면접 영상 + 숏츠 혼합)
            item("d1-9", "YouTube", "com.google.android.youtube", 16, 0, 17, 30, ContextClassification.MIXED, contextConfirmRequired = true),
            // 저녁 SNS
            item("d1-10", "Instagram", "com.instagram.android", 19, 0, 20, 15, ContextClassification.WASTE),
            item("d1-11", "X (Twitter)", "com.twitter.android", 20, 15, 20, 50, ContextClassification.WASTE),
            item("d1-12", "TikTok", "com.zhiliaoapp.musically", 21, 0, 22, 30, ContextClassification.WASTE),
            item("d1-13", "카카오톡", "com.kakao.talk", 22, 30, 23, 0, ContextClassification.NEUTRAL)
        ),
        wasteMinutes = 435, // 7시간 15분
        productiveMinutes = 155, // 2시간 35분
        neutralMinutes = 120  // 2시간 (YouTube MIXED + 카카오톡)
    )

    /** 8/20 (화) - 비슷한 패턴, 조금 더 공부 */
    val day2_tuesday = DayData(
        date = 1724112000000,
        timelineItems = listOf(
            item("d2-1", "Instagram", "com.instagram.android", 7, 30, 8, 15, ContextClassification.WASTE),
            item("d2-2", "X (Twitter)", "com.twitter.android", 8, 15, 8, 35, ContextClassification.WASTE),
            item("d2-3", "VS Code", "com.microsoft.vscode", 9, 0, 11, 0, ContextClassification.PRODUCTIVE),
            item("d2-4", "Notion", "notion.id", 11, 0, 11, 30, ContextClassification.PRODUCTIVE),
            item("d2-5", "Instagram", "com.instagram.android", 12, 0, 13, 0, ContextClassification.WASTE),
            item("d2-6", "TikTok", "com.zhiliaoapp.musically", 13, 0, 13, 45, ContextClassification.WASTE),
            item("d2-7", "인프런", "com.inflearn.app", 14, 0, 15, 30, ContextClassification.PRODUCTIVE),
            item("d2-8", "LinkedIn", "com.linkedin.android", 15, 30, 16, 0, ContextClassification.PRODUCTIVE),
            item("d2-9", "YouTube", "com.google.android.youtube", 16, 30, 18, 0, ContextClassification.MIXED, contextConfirmRequired = true),
            item("d2-10", "Instagram", "com.instagram.android", 19, 30, 20, 30, ContextClassification.WASTE),
            item("d2-11", "TikTok", "com.zhiliaoapp.musically", 20, 30, 22, 0, ContextClassification.WASTE),
            item("d2-12", "카카오톡", "com.kakao.talk", 22, 0, 22, 30, ContextClassification.NEUTRAL)
        ),
        wasteMinutes = 375, // 6시간 15분
        productiveMinutes = 210, // 3시간 30분
        neutralMinutes = 120
    )

    /** 8/21 (수) - 면접 준비 집중, 그래도 SNS 많음 */
    val day3_wednesday = DayData(
        date = 1724198400000,
        timelineItems = listOf(
            item("d3-1", "Instagram", "com.instagram.android", 8, 0, 8, 30, ContextClassification.WASTE),
            item("d3-2", "사람인", "kr.co.saramin.android", 9, 0, 9, 45, ContextClassification.PRODUCTIVE),
            item("d3-3", "잡코리아", "net.jobkorea.app", 9, 45, 10, 15, ContextClassification.PRODUCTIVE),
            item("d3-4", "VS Code", "com.microsoft.vscode", 10, 15, 12, 0, ContextClassification.PRODUCTIVE),
            item("d3-5", "Notion", "notion.id", 12, 0, 12, 30, ContextClassification.PRODUCTIVE),
            item("d3-6", "Instagram", "com.instagram.android", 13, 0, 14, 0, ContextClassification.WASTE),
            item("d3-7", "TikTok", "com.zhiliaoapp.musically", 14, 0, 14, 40, ContextClassification.WASTE),
            item("d3-8", "YouTube", "com.google.android.youtube", 15, 0, 16, 0, ContextClassification.MIXED, contextConfirmRequired = true),
            item("d3-9", "인프런", "com.inflearn.app", 16, 0, 17, 0, ContextClassification.PRODUCTIVE),
            item("d3-10", "Instagram", "com.instagram.android", 18, 30, 19, 30, ContextClassification.WASTE),
            item("d3-11", "X (Twitter)", "com.twitter.android", 19, 30, 20, 0, ContextClassification.WASTE),
            item("d3-12", "Netflix", "com.netflix.mediaclient", 21, 0, 23, 0, ContextClassification.LEISURE),
            item("d3-13", "TikTok", "com.zhiliaoapp.musically", 23, 0, 23, 45, ContextClassification.WASTE)
        ),
        wasteMinutes = 295, // 4시간 55분
        productiveMinutes = 240, // 4시간
        neutralMinutes = 60,
        leisureMinutes = 120
    )

    /** 8/22 (목) - 의지 약해진 날, SNS 폭주 */
    val day4_thursday = DayData(
        date = 1724284800000,
        timelineItems = listOf(
            item("d4-1", "Instagram", "com.instagram.android", 8, 0, 9, 30, ContextClassification.WASTE),
            item("d4-2", "TikTok", "com.zhiliaoapp.musically", 9, 30, 10, 30, ContextClassification.WASTE),
            item("d4-3", "X (Twitter)", "com.twitter.android", 10, 30, 11, 0, ContextClassification.WASTE),
            item("d4-4", "VS Code", "com.microsoft.vscode", 11, 30, 12, 30, ContextClassification.PRODUCTIVE),
            item("d4-5", "Instagram", "com.instagram.android", 13, 0, 14, 30, ContextClassification.WASTE),
            item("d4-6", "TikTok", "com.zhiliaoapp.musically", 14, 30, 15, 30, ContextClassification.WASTE),
            item("d4-7", "YouTube", "com.google.android.youtube", 15, 30, 17, 0, ContextClassification.MIXED, contextConfirmRequired = true),
            item("d4-8", "사람인", "kr.co.saramin.android", 17, 0, 17, 20, ContextClassification.PRODUCTIVE),
            item("d4-9", "Instagram", "com.instagram.android", 18, 0, 19, 30, ContextClassification.WASTE),
            item("d4-10", "TikTok", "com.zhiliaoapp.musically", 19, 30, 21, 0, ContextClassification.WASTE),
            item("d4-11", "카카오톡", "com.kakao.talk", 21, 0, 21, 30, ContextClassification.NEUTRAL),
            item("d4-12", "Instagram", "com.instagram.android", 21, 30, 23, 0, ContextClassification.WASTE)
        ),
        wasteMinutes = 510, // 8시간 30분
        productiveMinutes = 80, // 1시간 20분
        neutralMinutes = 120
    )

    /** 8/23 (금) - 오전 취업 면접, 오후 보상심리로 SNS */
    val day5_friday = DayData(
        date = 1724371200000,
        timelineItems = listOf(
            item("d5-1", "Notion", "notion.id", 7, 0, 7, 45, ContextClassification.PRODUCTIVE),
            item("d5-2", "사람인", "kr.co.saramin.android", 7, 45, 8, 15, ContextClassification.PRODUCTIVE),
            // 면접 (앱 사용 없음 9:00~11:00)
            item("d5-3", "카카오톡", "com.kakao.talk", 11, 30, 12, 0, ContextClassification.NEUTRAL),
            item("d5-4", "Instagram", "com.instagram.android", 12, 30, 14, 0, ContextClassification.WASTE),
            item("d5-5", "TikTok", "com.zhiliaoapp.musically", 14, 0, 15, 30, ContextClassification.WASTE),
            item("d5-6", "X (Twitter)", "com.twitter.android", 15, 30, 16, 0, ContextClassification.WASTE),
            item("d5-7", "YouTube", "com.google.android.youtube", 16, 0, 17, 30, ContextClassification.LEISURE),
            item("d5-8", "Instagram", "com.instagram.android", 18, 0, 19, 0, ContextClassification.WASTE),
            item("d5-9", "Netflix", "com.netflix.mediaclient", 20, 0, 22, 0, ContextClassification.LEISURE),
            item("d5-10", "TikTok", "com.zhiliaoapp.musically", 22, 0, 23, 30, ContextClassification.WASTE)
        ),
        wasteMinutes = 360, // 6시간
        productiveMinutes = 75, // 1시간 15분
        neutralMinutes = 30,
        leisureMinutes = 210
    )

    /** 8/24 (토) - 주말, 완전 이완, SNS 폭발 */
    val day6_saturday = DayData(
        date = 1724457600000,
        timelineItems = listOf(
            item("d6-1", "Instagram", "com.instagram.android", 9, 30, 11, 0, ContextClassification.WASTE),
            item("d6-2", "TikTok", "com.zhiliaoapp.musically", 11, 0, 12, 30, ContextClassification.WASTE),
            item("d6-3", "YouTube", "com.google.android.youtube", 12, 30, 14, 0, ContextClassification.LEISURE),
            item("d6-4", "Instagram", "com.instagram.android", 14, 0, 15, 0, ContextClassification.WASTE),
            item("d6-5", "X (Twitter)", "com.twitter.android", 15, 0, 15, 45, ContextClassification.WASTE),
            item("d6-6", "Spotify", "com.spotify.music", 16, 0, 17, 0, ContextClassification.LEISURE),
            item("d6-7", "TikTok", "com.zhiliaoapp.musically", 17, 0, 18, 30, ContextClassification.WASTE),
            item("d6-8", "카카오톡", "com.kakao.talk", 18, 30, 19, 0, ContextClassification.NEUTRAL),
            item("d6-9", "Instagram", "com.instagram.android", 19, 0, 20, 30, ContextClassification.WASTE),
            item("d6-10", "Netflix", "com.netflix.mediaclient", 20, 30, 23, 0, ContextClassification.LEISURE),
            item("d6-11", "TikTok", "com.zhiliaoapp.musically", 23, 0, 0, 30, ContextClassification.WASTE)
        ),
        wasteMinutes = 525, // 8시간 45분
        productiveMinutes = 0,
        neutralMinutes = 30,
        leisureMinutes = 210
    )

    /** 8/25 (일) - 약간의 죄책감, 오후에 조금 공부 */
    val day7_sunday = DayData(
        date = 1724544000000,
        timelineItems = listOf(
            item("d7-1", "Instagram", "com.instagram.android", 10, 0, 11, 30, ContextClassification.WASTE),
            item("d7-2", "TikTok", "com.zhiliaoapp.musically", 11, 30, 12, 30, ContextClassification.WASTE),
            item("d7-3", "YouTube", "com.google.android.youtube", 13, 0, 14, 0, ContextClassification.MIXED, contextConfirmRequired = true),
            item("d7-4", "VS Code", "com.microsoft.vscode", 14, 30, 16, 0, ContextClassification.PRODUCTIVE),
            item("d7-5", "Notion", "notion.id", 16, 0, 16, 30, ContextClassification.PRODUCTIVE),
            item("d7-6", "인프런", "com.inflearn.app", 16, 30, 17, 30, ContextClassification.PRODUCTIVE),
            item("d7-7", "Instagram", "com.instagram.android", 18, 0, 19, 0, ContextClassification.WASTE),
            item("d7-8", "X (Twitter)", "com.twitter.android", 19, 0, 19, 30, ContextClassification.WASTE),
            item("d7-9", "TikTok", "com.zhiliaoapp.musically", 19, 30, 21, 0, ContextClassification.WASTE),
            item("d7-10", "카카오톡", "com.kakao.talk", 21, 0, 21, 30, ContextClassification.NEUTRAL),
            item("d7-11", "Instagram", "com.instagram.android", 21, 30, 23, 0, ContextClassification.WASTE)
        ),
        wasteMinutes = 420, // 7시간
        productiveMinutes = 150, // 2시간 30분
        neutralMinutes = 90,
        leisureMinutes = 0
    )

    // === 주간 요약 ===
    val weeklySummary = WeeklySummary(
        days = listOf(day1_monday, day2_tuesday, day3_wednesday, day4_thursday, day5_friday, day6_saturday, day7_sunday),
        totalWasteMinutes = 2920,   // 약 48시간 40분 (하루 평균 약 7시간)
        totalProductiveMinutes = 910, // 약 15시간 10분 (하루 평균 약 2시간 10분)
        totalNeutralMinutes = 570,
        totalLeisureMinutes = 540,
        averageDailyWasteMinutes = 417, // 약 6시간 57분
        averageDailyProductiveMinutes = 130, // 약 2시간 10분
        topWasteApps = listOf(
            AppUsageSummary("Instagram", 1050),  // 17.5시간/주
            AppUsageSummary("TikTok", 990),      // 16.5시간/주
            AppUsageSummary("X (Twitter)", 310), // 약 5시간/주
        ),
        topProductiveApps = listOf(
            AppUsageSummary("VS Code", 390),     // 6.5시간/주
            AppUsageSummary("인프런", 195),       // 3.25시간/주
            AppUsageSummary("Notion", 135),      // 2.25시간/주
        )
    )

    // === Helper function ===
    private fun item(
        id: String,
        appName: String,
        packageName: String,
        startHour: Int,
        startMin: Int,
        endHour: Int,
        endMin: Int,
        classification: ContextClassification,
        activityName: String? = null,
        isComplex: Boolean = false,
        contextConfirmRequired: Boolean = false
    ): TimelineItem {
        val baseDate = 1724025600000L // 2024-08-19 00:00 KST
        val dayOffset = id.substringAfter("d").substringBefore("-").toLong() - 1
        val dayBase = baseDate + (dayOffset * 86_400_000)
        val startAt = dayBase + (startHour * 3_600_000L) + (startMin * 60_000L)
        // endHour < startHour means next day (midnight crossing)
        val endAt = if (endHour < startHour) {
            dayBase + 86_400_000 + (endHour * 3_600_000L) + (endMin * 60_000L)
        } else {
            dayBase + (endHour * 3_600_000L) + (endMin * 60_000L)
        }
        val durationMs = endAt - startAt

        return TimelineItem(
            id = Identifier(id),
            appName = appName,
            packageName = packageName,
            activityName = activityName,
            timeRange = TimeRange(startAt, endAt),
            duration = Duration(durationMs),
            classification = classification,
            isComplex = isComplex,
            contextConfirmationRequired = contextConfirmRequired
        )
    }
}

// === Data classes ===

data class DayData(
    val date: Long,
    val timelineItems: List<TimelineItem>,
    val wasteMinutes: Int,
    val productiveMinutes: Int,
    val neutralMinutes: Int,
    val leisureMinutes: Int = 0
) {
    val totalScreenTimeMinutes: Int get() = wasteMinutes + productiveMinutes + neutralMinutes + leisureMinutes
}

data class WeeklySummary(
    val days: List<DayData>,
    val totalWasteMinutes: Int,
    val totalProductiveMinutes: Int,
    val totalNeutralMinutes: Int,
    val totalLeisureMinutes: Int,
    val averageDailyWasteMinutes: Int,
    val averageDailyProductiveMinutes: Int,
    val topWasteApps: List<AppUsageSummary>,
    val topProductiveApps: List<AppUsageSummary>
)

data class AppUsageSummary(
    val appName: String,
    val totalMinutes: Int
)
