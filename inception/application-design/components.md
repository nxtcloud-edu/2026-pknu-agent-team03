# Application Design — Components

## 1. 시스템 개요

TimeBack은 Android 클라이언트와 백엔드 API 서버로 구성된다.

```
┌─────────────────────────────────────────────────┐
│                Android Client                    │
│                                                  │
│  ┌──────────┐  ┌──────────┐  ┌──────────────┐  │
│  │ Data     │  │ Domain   │  │ Presentation │  │
│  │ Layer    │  │ Layer    │  │ Layer        │  │
│  └──────────┘  └──────────┘  └──────────────┘  │
│        │              │              │           │
│        └──────────────┼──────────────┘           │
│                       │                          │
│               ┌───────▼───────┐                  │
│               │  Local DB     │                  │
│               └───────────────┘                  │
└───────────────────────┬─────────────────────────┘
                        │ Sync (REST API)
┌───────────────────────▼─────────────────────────┐
│                Backend API                        │
│                                                   │
│  ┌──────────┐  ┌──────────┐  ┌──────────────┐   │
│  │ API      │  │ Service  │  │ Repository   │   │
│  │ Layer    │  │ Layer    │  │ Layer        │   │
│  └──────────┘  └──────────┘  └──────────────┘   │
│                       │                           │
│               ┌───────▼───────┐                   │
│               │  Server DB    │                   │
│               └───────────────┘                   │
└───────────────────────────────────────────────────┘
```

---

## 2. Android Client 컴포넌트

### C1: UsageDataCollector (데이터 수집)
- **역할**: UsageStatsManager를 통해 앱 사용 이벤트를 수집하고 AppSession으로 재구성
- **입력**: Android UsageStatsManager API (queryEvents)
- **출력**: UsageEvent 목록, AppSession 목록
- **관련 FR**: FR-1

### C2: AppClassificationManager (앱 분류 관리)
- **역할**: 설치된 앱 목록을 관리하고 기본 분류(생산/여가/낭비/중립)를 저장
- **입력**: PackageManager, 사용자 분류 입력
- **출력**: 앱별 기본 분류 데이터
- **관련 FR**: FR-2

### C3: ActivityRecorder (활동 기록)
- **역할**: 사용자가 직접 입력한 오프라인 활동을 저장
- **입력**: 사용자 입력 (활동 유형, 시작/종료 시간)
- **출력**: Activity 레코드
- **관련 FR**: FR-3

### C4: ContextAnalyzer (Context 분석)
- **역할**: AppSession과 Activity의 시간 중첩을 분석하여 Context를 생성
- **입력**: AppSession 목록, Activity 목록, 앱 기본 분류
- **출력**: Context 레코드 (분류: PRODUCTIVE/LEISURE/WASTE/MIXED/NEUTRAL)
- **관련 FR**: FR-4, FR-5

### C5: ConflictResolver (충돌 확인)
- **역할**: 활동과 앱 사용이 충돌할 때 사용자에게 선택지를 제시
- **입력**: 충돌이 감지된 세션-활동 쌍
- **출력**: 사용자 확인 결과 → Context 분류 확정
- **관련 FR**: FR-5

### C6: WasteCalculator (낭비시간 계산)
- **역할**: WASTE로 분류된 Context의 시간을 합산
- **입력**: Context 목록
- **출력**: 일별/주별 낭비시간
- **관련 FR**: FR-6

### C7: BaselineManager (Baseline 관리)
- **역할**: 초기 7일 측정 → Baseline 생성 → 확보시간 계산
- **입력**: 주간 낭비시간 데이터
- **출력**: Baseline 값, 확보한 시간(Saved Time)
- **관련 FR**: FR-7

### C8: GoalManager (목표 관리)
- **역할**: 목표 CRUD, 누적시간 관리, 진행률 계산
- **입력**: 사용자 목표 입력, 되찾은 시간 데이터
- **출력**: 목표 목록, 진행률
- **관련 FR**: FR-8

### C9: RecoveryTimer (시간 되찾기 타이머)
- **역할**: 타이머 실행/종료, 수동 기록, 되찾은 시간 저장
- **입력**: 목표 선택, 타이머 시작/종료
- **출력**: RecoveredTime 레코드
- **관련 FR**: FR-9, FR-10

### C10: DashboardPresenter (대시보드)
- **역할**: 홈 화면의 핵심 수치(낭비, 확보, 되찾음, 회수율, 목표 진행도) 집계 및 표시
- **입력**: WasteCalculator, BaselineManager, GoalManager 데이터
- **출력**: 대시보드 UI 데이터
- **관련 FR**: FR-11

### C11: TimelinePresenter (Timeline)
- **역할**: 하루의 시간대별 앱 사용/활동/Context를 시각적으로 구성
- **입력**: AppSession, Activity, Context 데이터
- **출력**: Timeline UI 데이터
- **관련 FR**: FR-12

### C12: ReportGenerator (리포트)
- **역할**: 주간 리포트 생성 및 알림 트리거
- **입력**: 주간 집계 데이터
- **출력**: 리포트 데이터, 푸시 알림 트리거
- **관련 FR**: FR-13

### C13: NotificationManager (알림)
- **역할**: 실시간 낭비 경고, 리포트 알림, 타이머 알림 관리
- **입력**: 낭비시간 임계치 초과 이벤트, 리포트 생성 이벤트
- **출력**: 푸시 알림
- **관련 FR**: FR-14

### C14: AuthManager (인증)
- **역할**: 로컬 PIN 설정/검증
- **입력**: 사용자 PIN 입력
- **출력**: 인증 성공/실패
- **관련 FR**: FR-15

### C15: SyncManager (동기화)
- **역할**: 로컬 데이터를 백엔드 API와 동기화
- **입력**: 로컬 DB 변경 사항
- **출력**: 서버 동기화 상태
- **관련 FR**: 전체 (NFR-4 암호화)

---

## 3. Backend API 컴포넌트

### S1: AuthService
- **역할**: 사용자 식별 및 토큰 관리
- **관련 FR**: FR-15

### S2: UsageDataService
- **역할**: 클라이언트에서 동기화된 AppSession/Activity 저장 및 조회
- **관련 FR**: FR-1, FR-3

### S3: ContextService
- **역할**: Context 데이터 저장 및 조회, 통계 집계
- **관련 FR**: FR-4, FR-5, FR-6

### S4: BaselineService
- **역할**: Baseline 계산 및 확보시간 조회
- **관련 FR**: FR-7

### S5: GoalService
- **역할**: 목표 CRUD, RecoveredTime 저장, 진행률 조회
- **관련 FR**: FR-8, FR-9, FR-10

### S6: ReportService
- **역할**: 주간/월간 리포트 데이터 집계
- **관련 FR**: FR-13

### S7: NotificationService
- **역할**: 푸시 알림 발송 (FCM 등)
- **관련 FR**: FR-14

---

## 4. 컴포넌트 간 의존 관계

```
UsageDataCollector ──→ ContextAnalyzer ──→ WasteCalculator ──→ BaselineManager
                              ↑                                       │
ActivityRecorder ─────────────┘                                       ↓
                                                              DashboardPresenter
AppClassificationManager ──→ ContextAnalyzer
                                    │
ConflictResolver ←──────────────────┘ (충돌 감지 시)

GoalManager ←── RecoveryTimer
     │
     ↓
DashboardPresenter

WasteCalculator ──→ NotificationManager (임계치 초과 시)
ReportGenerator ←── WasteCalculator + BaselineManager + GoalManager
ReportGenerator ──→ NotificationManager

SyncManager ←→ 모든 데이터 컴포넌트 (C1~C9)
```

---

## 5. 데이터 흐름

```
[Android OS] 
    │ UsageStatsManager.queryEvents()
    ▼
[C1: UsageDataCollector] → UsageEvent → AppSession
    │
    ▼
[C4: ContextAnalyzer] ← [C3: ActivityRecorder] (Activity)
    │                  ← [C2: AppClassificationManager] (기본 분류)
    │
    ├─ 충돌 감지 → [C5: ConflictResolver] → 사용자 응답 → Context 확정
    │
    ▼
Context (PRODUCTIVE/LEISURE/WASTE/MIXED/NEUTRAL)
    │
    ▼
[C6: WasteCalculator] → 일별/주별 낭비시간
    │
    ├─ 임계치 초과 → [C13: NotificationManager] → 푸시 알림
    │
    ▼
[C7: BaselineManager] → Baseline, 확보한 시간
    │
    ▼
[C10: DashboardPresenter] ← [C8: GoalManager] ← [C9: RecoveryTimer]
    │
    ▼
[UI: 홈 대시보드]

[C11: TimelinePresenter] ← AppSession + Activity + Context → [UI: Timeline]
[C12: ReportGenerator] ← 주간 집계 → [UI: 리포트] + [C13: 푸시 알림]
```

---

## 6. 인터페이스(경계)

| 경계 | 통신 방식 | 데이터 형식 |
|---|---|---|
| Android OS → C1 | UsageStatsManager API | UsageEvents |
| Android Client → Backend API | REST API (HTTPS) | JSON |
| Backend API → Push | FCM (Firebase Cloud Messaging) | Notification payload |
| Client 내부 계층 간 | 함수 호출 / Repository 패턴 | Domain 객체 |
| Client ↔ Local DB | ORM / DAO 패턴 | Entity 객체 |


---

## 7. 화면 구성 요소 상세 (상태·동작·오류)

### 홈 대시보드 (HomeScreen)

| 항목 | 설명 |
|---|---|
| **상태** | 로딩 중 / 데이터 표시 / Baseline 측정 중 / 오프라인 |
| **사용자 동작** | Pull-to-refresh, 카드 탭(상세 이동), 충돌 배지 탭 |
| **오류 상태** | 데이터 로딩 실패 → "새로고침" 버튼 표시 |
| **빈 상태** | 첫 설치 직후 → "데이터 수집 중..." 안내 |

### Timeline (TimelineScreen)

| 항목 | 설명 |
|---|---|
| **상태** | 로딩 / 날짜별 데이터 표시 / 빈 날 |
| **사용자 동작** | 날짜 이동(스와이프/선택), 항목 탭(분류 변경), 스크롤 |
| **오류 상태** | 데이터 로딩 실패 → 재시도 버튼 |
| **빈 상태** | 해당 날짜에 기록 없음 → "이 날은 기록이 없습니다" |

### 시간 되찾기 (RecoveryScreen)

| 항목 | 설명 |
|---|---|
| **상태** | 대기 / 타이머 진행 중 / 기록 완료 |
| **사용자 동작** | 목표별 ▶ 시작, ⏹ 정지, 활동 직접 기록 |
| **오류 상태** | Foreground Service 시작 실패 → 권한 안내 |
| **빈 상태** | 목표 미등록 → "먼저 목표를 등록하세요" + 목표 화면 링크 |

### 목표 (GoalsScreen)

| 항목 | 설명 |
|---|---|
| **상태** | 목표 목록 표시 / 빈 목록 |
| **사용자 동작** | 목표 추가(FAB), 수정(롱프레스), 삭제(스와이프) |
| **오류 상태** | 10개 초과 등록 시도 → "최대 10개까지 등록 가능" 토스트 |
| **빈 상태** | 목표 없음 → "목표를 추가해보세요" + 추가 버튼 |

### 리포트 (ReportScreen)

| 항목 | 설명 |
|---|---|
| **상태** | 리포트 존재 / Baseline 미측정 / 데이터 부족 |
| **사용자 동작** | 주 이동(좌우), 주간/월간 탭 전환 |
| **오류 상태** | 리포트 생성 실패 → "나중에 다시 확인" |
| **빈 상태** | Baseline 미완성 → "측정 완료 후 리포트가 생성됩니다" |

### 앱 분류 (AppClassificationScreen)

| 항목 | 설명 |
|---|---|
| **상태** | 앱 목록 로드 / 필터 적용 중 |
| **사용자 동작** | 검색, 필터 탭(전체/생산/여가/낭비/중립), 분류 변경 |
| **오류 상태** | PackageManager 접근 실패 → "앱 목록을 불러올 수 없습니다" |
| **빈 상태** | 필터 결과 없음 → "해당 분류의 앱이 없습니다" |

### 충돌 확인 (ConflictResolutionDialog)

| 항목 | 설명 |
|---|---|
| **상태** | 선택지 표시 / 처리 중 |
| **사용자 동작** | 5개 선택지 중 택1, "나중에" 버튼 |
| **오류 상태** | 없음 (로컬 처리) |
| **빈 상태** | 미확인 충돌 0건 → 다이얼로그 미표시 |

### 알림 설정 (NotificationSettingsScreen)

| 항목 | 설명 |
|---|---|
| **상태** | 설정값 표시 |
| **사용자 동작** | 토글 ON/OFF, 임계치 슬라이더 조절 |
| **오류 상태** | 알림 권한 미허가 → "알림 권한을 허용해주세요" 안내 |
| **빈 상태** | 없음 (항상 기본값 존재) |
