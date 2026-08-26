# TimeBack STEP 01 — 서비스 및 협업 계약

## 1. 목적

이 문서는 APP-05~APP-09가 APP-04, APP-11, OS-05, APP-10과 협업하는 언어 중립 기능 계약을 정의한다. 메서드 문법, Java 인터페이스, 네트워크 프로토콜, 데이터베이스 트랜잭션 제품은 STEP 02 이후에 정한다.

## 2. 계약 원칙

### SC-C01. 명령과 조회 분리

- 명령은 기준 데이터를 생성·수정하고 성공 또는 명시적 오류를 반환한다.
- 조회는 기준 데이터를 바꾸지 않고 값과 상태를 반환한다.
- UI와 APP-10은 조회 결과를 재계산하거나 기준 데이터로 저장하지 않는다.

### SC-C02. 결과 구조

모든 기능 결과는 다음 의미를 구분한다.

| 결과 | 의미 |
|---|---|
| `SUCCESS` | 명령 또는 조회가 완료됨 |
| `VALIDATION_ERROR` | 입력이 업무 규칙을 위반함 |
| `NOT_FOUND` | 참조 엔터티가 없음 |
| `STATE_CONFLICT` | 현재 상태에서 작업을 수행할 수 없음 |
| `DECISION_REQUIRED` | 사용자 Context 확인 또는 대표 Goal 선택 필요 |
| `DATA_INCOMPLETE` | 수집 완전성이 부족해 확정 지표를 만들 수 없음 |
| `DEPENDENCY_FAILURE` | 저장·세션·시간 경계 조회가 실패함 |

오류에는 기계 판정용 `reasonCode`와 사용자 설명에 매핑 가능한 안전한 메시지 키를 포함한다. 실제 package name, 활동명, 시간 패턴을 오류 로그 기본값에 넣지 않는다.

### SC-C03. 변경 원자성

기능적으로 하나의 사용자 작업인 변경은 전부 성공하거나 기준 데이터를 그대로 유지해야 한다.

- Context 확인: 새 확정 Context 저장과 이전 Context 대체 관계
- Baseline 승인: 기존 Baseline 상태 변경과 새 Baseline 활성화
- 타이머 완료: RecoveredTime 생성과 RunningTimer 제거
- 대표 Goal 선택: OverlapResolution 생성·교체

구체 트랜잭션 기술은 STEP 02에서 결정한다.

### SC-C04. 재시도와 중복

- 같은 값의 App Default 저장과 같은 Context 최종 분류 재적용은 의미상 멱등이다.
- 타이머 시작·완료, Activity·Goal·RecoveredTime 생성은 중복 생성 위험이 있으므로 호출 경계에서 재시도 식별 전략이 필요하다.
- 재시도 식별자의 자료형과 보존 방식은 STEP 02에서 정하지만, 실제 구현은 중복 명령으로 실제 시간을 두 번 누적해서는 안 된다.

### SC-C05. 기간 입력

기간 조회는 유효한 `[periodStart, periodEnd)`와 현지 시간대 문맥을 요구한다. `periodEnd <= periodStart`이면 의존 대상을 호출하지 않고 `INVALID_INTERVAL`을 반환한다.

## 3. 의존 방향

```text
APP-04 Session Source ───────┐
APP-11 Data Authority ───────┼─→ APP-05~APP-09 ─→ APP-10 Query Assembly ─→ UI-02~UI-07
OS-05 Time Boundary ─────────┘
CT-06 Fakes ──────────────── 테스트에서 같은 계약 대체
```

- APP-05~APP-09는 Android API 객체를 직접 받지 않는다.
- APP-11은 저장·조회만 담당하며 Context·Baseline·Recovery Rate를 판정하지 않는다.
- APP-10과 UI는 도메인 계산을 복제하지 않는다.
- 서버는 APP-05~APP-09 기능 계약을 호출해 기기 값을 재판정하지 않는다.

## 4. 제공받는 계약

### SC-D01. SessionSource — APP-04 제공

#### `listSessions(period)`

입력:

- 유효 기간

출력:

- `AppSession` 목록
- 기간 내 데이터 완전성 정보
- 세션별 `sessionId`, `packageName`, 구간, `sourceEventIds`, `inferred`

오류:

- `INVALID_INTERVAL`
- `SESSION_SOURCE_UNAVAILABLE`
- `DATA_INCOMPLETE`

목록은 요청 기간과 겹치는 세션만 반환하며 정렬 순서에 의존하지 않아도 된다. APP-07이 안정적으로 시간순 정규화한다.

### SC-D02. DomainDataAuthority — APP-11 제공

엔터티별 기능:

- App 조회·분류 저장
- Activity 생성·수정·기간 조회
- Context 기간 조회·자동 결과 교체·사용자 확정 저장
- BaselineObservation·Baseline·Candidate 저장·조회
- Goal·RunningTimer·RecoveredTime·OverlapResolution 저장·조회
- 변경 성공 후 변경 사실 통지

기능 조건:

- 서로 다른 엔터티 종류를 혼합하지 않는다.
- 저장 실패 시 기존 기준 데이터를 유지한다.
- 사용자 확정 Context와 자동 Context를 구분해 조회할 수 있다.
- 기간 조회는 실제 겹치는 레코드를 반환한다.
- APP-11은 계산 결과의 옳고 그름을 다시 판정하지 않는다.

### SC-D03. TimeBoundary — OS-05 제공

기능:

- 현재 절대 시각 제공
- 지정 시점의 현지 날짜와 시간대 문맥 제공
- 다음 현지 자정 경계 제공
- 지정 기간의 월요일 주간 경계 제공
- 기간을 현지 날짜 slice로 분할

운영과 테스트는 같은 의미의 계약을 사용한다. 테스트에서는 ControlledTimeBoundary로 교체한다.

### SC-D04. ChangeNotifier — APP-11 제공

저장 성공 후 다음 의미를 전달한다.

- 변경 엔터티 종류와 식별자
- 변경된 시간 범위
- 작업 종류 `CREATED|UPDATED|SUPERSEDED`
- 후속 재계산이 필요한 구성 요소

민감한 필드 전체를 변경 통지에 복사하지 않는다.

## 5. APP-05 제공 계약

### SC-05-01. `getAppClassifications`

입력: 설치 앱 목록 또는 설치 앱 조회 경계가 제공한 최소 App 정보.

출력 항목:

- `packageName`, `displayName`
- `classificationState`
- 분류된 경우 `defaultClassification`
- 마지막 저장 상태

상태:

- 정상 목록
- 목록 없음
- 미분류 앱 있음
- APP-11 조회 실패

### SC-05-02. `setAppDefaultClassification`

입력:

- `packageName`
- `PRODUCTIVE|LEISURE|WASTE|NEUTRAL`

성공 출력:

- 갱신된 App
- 재분석 영향 범위 또는 영향 앱 식별자
- 기존 사용자 확정 Context 보존 확인

오류:

- `APP_NOT_FOUND`
- `INVALID_DEFAULT_CLASSIFICATION`
- `DEPENDENCY_FAILURE`

성공 후 APP-07 재분석과 APP-08 지표 재계산을 요청한다. UI 요청이 끝나기 전에 모든 재계산을 동기 완료할지 여부는 STEP 02 성능·일관성 결정으로 넘기되, 조회에는 계산 상태를 명시해야 한다.

## 6. APP-06 제공 계약

### SC-06-01. `createActivity`

입력:

- Activity type
- 조건부 custom name
- intent classification
- `[startAt, endAt)`

성공 출력:

- 생성 Activity
- 현지 날짜별 파생 slice 요약
- Context 재분석 영향 범위

오류:

- `INVALID_INTERVAL`
- `CUSTOM_NAME_REQUIRED`
- `INVALID_ACTIVITY_INTENT`
- `DEPENDENCY_FAILURE`

### SC-06-02. `updateActivity`

입력:

- `activityId`
- 변경할 type/name/intent/interval

성공 출력:

- 수정 Activity
- 이전·신규 구간 합집합인 재분석 범위

오류:

- 생성 검증 오류
- `ACTIVITY_NOT_FOUND`
- `DEPENDENCY_FAILURE`

### SC-06-03. `listActivities(period)`

출력:

- 원본 Activity
- 요청 기간과 현지 날짜에 맞춘 ActivitySlice
- `VALUE|EMPTY|DATA_INCOMPLETE`

## 7. APP-07 제공 계약

### SC-07-01. `reanalyzeContexts`

입력:

- 유효 분석 기간
- 재분석 원인 `SESSION_CHANGED|APP_DEFAULT_CHANGED|ACTIVITY_CHANGED|USER_REQUEST`

의존 조회:

- APP-04 SessionSource
- APP-05 App 분류
- APP-06 Activity
- APP-11 기존 사용자 확정 Context

성공 출력:

- 새·유지·대체된 Context 목록
- 확인 필요 Context 목록
- 보존한 사용자 확정 Context 목록
- 영향 기간
- APP-08 재계산 요청

오류/상태:

- `INVALID_INTERVAL`
- `DATA_INCOMPLETE`
- `DEPENDENCY_FAILURE`

교체 범위 안의 자동 Context만 대체하고 사용자 확정 Context는 유지한다.

### SC-07-02. `confirmMixedContext`

입력:

- `contextId`
- 확인 답변
- `OTHER`이면 final classification

성공 출력:

- `USER_CONFIRMED` Context
- 대체된 이전 Context ID
- APP-08 재계산 범위

오류:

- `CONTEXT_NOT_FOUND`
- `CONTEXT_NOT_CONFIRMABLE`
- `FINAL_CLASSIFICATION_REQUIRED`
- `INVALID_FINAL_CLASSIFICATION`
- `DEPENDENCY_FAILURE`

### SC-07-03. `editContext`

입력:

- `contextId`
- final classification (`MIXED` 제외)

성공 출력:

- `USER_CONFIRMED` Context
- `USER_TIMELINE_EDIT` 근거
- APP-08·APP-09·APP-10에 전달할 영향 범위

### SC-07-04. `listContexts(period)`

출력:

- 시간순 Context
- 사용자 확인 상태와 이유
- AppSession·Activity 참조
- `VALUE|EMPTY|DATA_INCOMPLETE|DECISION_REQUIRED`

## 8. APP-08 제공 계약

### SC-08-01. `recordMeasurementDay`

입력:

- 현지 날짜
- 수집 coverage 상태
- 해당 날짜 최종 Context

출력:

- MeasurementDay
- BaselineObservation 진행 상태
- 남은 유효일 수 또는 무효화 사유

불완전 날짜는 Waste 0으로 기록하지 않는다.

### SC-08-02. `getWasteMetrics(period)`

출력:

- Waste duration
- 집계에 사용한 WASTE 구간 수와 합집합 범위 요약
- `VALUE|DATA_INCOMPLETE`

UI에 원시 Context를 다시 합산시키지 않는다.

### SC-08-03. `getBaselineStatus`

출력 상태:

- `OBSERVING`: 연속 유효일 수, 남은 일수
- `ACTIVE`: 주간 합계, 일평균, 관찰 범위
- `RECALCULATION_PROPOSED`: 현재 Baseline과 후보, 사용자 결정 필요

### SC-08-04. `decideBaselineCandidate`

입력:

- `candidateId`
- `APPROVE|REJECT`

성공 출력:

- 현재 ACTIVE Baseline
- 이전 Baseline 상태
- Saved·Recovery Rate 재계산 범위

오류:

- `CANDIDATE_NOT_FOUND`
- `CANDIDATE_ALREADY_DECIDED`
- `DEPENDENCY_FAILURE`

### SC-08-05. `getSavedMetrics(period)`

출력:

- period status
- current Waste
- expected Baseline
- signed waste delta
- non-negative Saved
- `VALUE|OBSERVING|DATA_INCOMPLETE`

`OBSERVING`과 `DATA_INCOMPLETE`에는 Saved 숫자를 넣지 않는다.

## 9. APP-09 제공 계약

### SC-09-01. `createGoal`

입력: 이름, 양수 target duration.

출력: Goal.

오류: `INVALID_GOAL_NAME`, `INVALID_TARGET_DURATION`, `DEPENDENCY_FAILURE`.

### SC-09-02. `startGoalTimer`

입력: `goalId`.

의존: Goal 조회, OS-05 current time, RunningTimer 조회·저장.

출력: RunningTimer.

오류: `GOAL_NOT_FOUND`, `TIMER_ALREADY_RUNNING`, `DEPENDENCY_FAILURE`.

### SC-09-03. `completeGoalTimer`

입력: 실행 중 timer 식별자 또는 현재 사용자 타이머.

성공 출력:

- TIMER RecoveredTime
- 타이머 종료 상태
- 새 overlap 결정 상태
- 갱신 대상 GoalProgress

오류:

- `TIMER_NOT_RUNNING`
- `INVALID_INTERVAL`
- `DEPENDENCY_FAILURE`

RecoveredTime 저장과 RunningTimer 제거는 하나의 기능 작업이다.

### SC-09-05. `recordManualRecoveredTime`

입력: `goalId`, 유효 구간.

출력:

- MANUAL RecoveredTime
- overlap 분석 결과
- 대표 Goal 선택 필요 구간

오류: `GOAL_NOT_FOUND`, `INVALID_INTERVAL`, `DEPENDENCY_FAILURE`.

### SC-09-06. `resolveGoalOverlap`

입력:

- 미결정 overlap segment
- 후보 중 representative goal ID

출력:

- OverlapResolution
- 목표별 누적 변화
- 전체 Recovered 변화

오류:

- `OVERLAP_NOT_FOUND`
- `OVERLAP_ALREADY_CHANGED`
- `INVALID_REPRESENTATIVE_GOAL`
- `DEPENDENCY_FAILURE`

### SC-09-07. `getGoalProgress(period)`

출력:

- Goal별 target, accumulated, unbounded progress ratio
- pending overlap duration
- `VALUE|EMPTY|DECISION_REQUIRED|DATA_INCOMPLETE`

### SC-09-08. `getRecoveryMetrics(period)`

출력:

- assigned Recovered
- pending overlap duration
- same-period Saved
- rate status와 조건부 exact ratio
- `AVAILABLE|BASELINE_OBSERVING|SAVED_ZERO|DATA_INCOMPLETE|DECISION_REQUIRED`

## 10. APP-10에 제공하는 조회 모델

### SC-Q01. TimelineDomainView

- ActivitySlice
- AppSession 참조
- Context와 classification
- `userConfirmed`, decision reason
- 확인 필요 작업
- 데이터 완전성

### SC-Q02. HomeMetricsView

- 오늘 Waste와 period status
- Baseline observation 또는 active summary
- signed waste delta와 조건부 Saved
- Recovered, pending overlap
- Recovery Rate status와 조건부 ratio
- GoalProgress 요약

### SC-Q03. GoalRecoveryView

- Goal 목록·진행률
- 실행 중 타이머
- TIMER/MANUAL 기록
- overlap representative decision 목록
- 오류·빈 상태

### SC-Q04. ReportDomainView

- 선택 기간과 완전성
- Waste, expected Baseline, Saved
- Goal별 Recovered와 전체 Recovered
- rate status와 조건부 ratio
- 진행 중 기간 여부

APP-10은 이 값을 화면별로 조립할 뿐 계산식을 다시 수행하지 않는다.

## 11. CT-06 테스트 대역 계약

### SC-F01. FakeSessionSource

- 합성 package name과 고정 AppSession 목록 주입
- 중첩·경계 접촉·불완전 coverage 주입
- 입력 순서 변형 가능
- 실제 기기 UsageEvent 사용 금지

### SC-F02. FakeDomainDataAuthority

- 엔터티 종류별 독립 저장
- 저장 성공·실패 선택 가능
- 명령 전후 snapshot 비교 가능
- 사용자 확정 Context와 자동 Context 구분
- 호출 기록과 영향 범위 검증 가능

### SC-F03. ControlledTimeBoundary

- 현재 시각 고정·전진
- 현지 자정·월요일 경계 제공
- DST·시간대 전환 합성 사례 제공
- 시스템 실제 시계에 의존하지 않음

### SC-F04. ContractFixtureFactory

- `example.app.a`, `example.app.b` 같은 합성 앱
- 개인을 나타내지 않는 Activity·Goal 이름
- 정상·누락·중첩·자정·7일 관찰 fixture
- 결과 예상치를 원시 duration 단순 합계가 아닌 구간 합집합으로 정의

## 12. 계약 검증 체크리스트

- SC-V01: 모든 명령의 실패가 기존 기준 데이터를 보존한다.
- SC-V02: 타이머 완료의 생성·제거가 부분 성공하지 않는다.
- SC-V03: Context 재분석이 USER_CONFIRMED를 보존한다.
- SC-V04: APP-10과 UI 계약에 계산식이 중복되지 않는다.
- SC-V05: `OBSERVING`, `DATA_INCOMPLETE`, 숫자 0이 구분된다.
- SC-V06: 같은 기간의 Saved와 Recovered만 Recovery Rate에 사용된다.
- SC-V07: fake와 실제 의존 구현이 같은 기능 계약을 만족해야 한다.
- SC-V08: 계약 오류와 로그에 상세 행동 데이터가 기본 포함되지 않는다.
- SC-V09: Java 또는 저장 제품의 구체 타입이 STEP 01 계약에 유입되지 않는다.

## 13. 리뷰 보완 — 우선 적용 계약 정정

이 절은 SC-D/SC-07~SC-09/SC-Q/SC-V의 이전 서술과 충돌할 때 우선한다.

### SC-D05. InstalledAppSource — Android 경계 제공

`listInstalledApps()`는 최소 `packageName`, 사용자 표시 `displayName`, `snapshotAvailability=COMPLETE|UNAVAILABLE`, source-observed time만 반환한다. 화면 내용·콘텐츠·위치는 반환하지 않는다. APP-05는 COMPLETE snapshot에서만 새 App을 `UNCLASSIFIED + discoveredAt`으로 관찰하며 `classificationUpdatedAt`은 사용자 분류 저장 때만 설정한다. UNAVAILABLE이면 저장 App 분류를 삭제·초기화하지 않고 `DEPENDENCY_FAILURE`를 반환한다.

### SC-D02A. DomainDataAuthority 확장

APP-11은 Context revision/provenance, current-effective Context 조회, MeasurementDay logical-key upsert/source revision, BaselineObservation/Candidate, RunningTimer, OverlapResolution을 기준 엔터티로 저장·조회한다. `listCurrentEffectiveContexts(period)`는 지표용 current EffectiveSegmentDecision만, 감사 조회는 revision history를 반환한다. 기준·영속 파생 cache entity registry는 APP-12 backup, 보관, APP-13 전체 삭제 대상이며 backup/device 중 하나라도 실패하면 삭제 완료 통지를 발행하지 않는다.

### SC-D04A. ChangeNotifier 확장

작업 종류는 `CREATED|UPDATED|SUPERSEDED|DELETED`다. `DELETED`에는 entity type/id, 삭제 원인, 영향 범위만 전달하며 민감 상세값은 복사하지 않는다. timer 완료는 RecoveredTime `CREATED`와 RunningTimer `DELETED(reason=COMPLETED)`를 하나의 성공 change set으로 통지한다.

### SC-07A. Context revision 계약

`reanalyzeContexts`는 `sourceRevision`, 새/유지/대체된 revision 및 current EffectiveSegmentDecision을 반환한다. `confirmMixedContext`는 원래 confirmation answer, OTHER final enum, decidedAt, previous auto classification을 보존한다. `editContext`는 `TIMELINE_EDIT` provenance를 보존한다. `listContexts`는 canonical interval, duration, current effective classification, evidence summary, confirmation provenance, freshness를 반환하며 지표 조회와 history 조회를 혼합하지 않는다.

### SC-08A. Measurement·Saved 계약

`recordMeasurementDay` 입력에는 logical key, coverage revision, context source revision이 포함된다. 동일 key·동일 revision은 no-op, 더 새 revision은 atomic upsert다. 출력에는 applied/no-op, measurement revision, 관찰 재구성 결과를 포함한다. `getSavedMetrics`는 full/partial comparison basis, as-of, covered range, `sourceRevision`, `computedThroughRevision`, `freshness`, 마지막 성공 시각을 포함한다. `DATA_INCOMPLETE`는 coverage gap에만 사용하며 current partial coverage는 `IN_PROGRESS` 잠정 결과다.

### SC-09A. Goal·Rate 계약 정정

`cancelGoalTimer`(SC-09-04)은 제공 계약에서 제거한다. `resolveGoalOverlap` 입력은 임시 segment 좌표가 아니라 `canonicalSourceRecoveredIds`, `effectiveInterval`, representative Goal이다. `getGoalLifetimeProgress()`는 기간 입력 없이 lifetime assigned duration·ratio·pending을 반환한다. `getGoalRecoveredSummary(period)`는 period-clipped Goal별 assigned/pending을 반환한다. 기존 `getGoalProgress(period)`는 이 두 계약으로 대체한다.

`getRecoveryMetrics(period)`은 assigned Recovered, pending overlap, Saved와 함께 `rateAvailability`, 조건부 `unavailabilityReason`, `recoveryDecisionState`, 조건부 ratio를 반환한다. pending은 Rate availability를 대체하지 않는다.

### SC-QA. canonical view·freshness 계약

모든 APP-10 view는 `sourceRevision`, `computedThroughRevision`, `freshness=FRESH|RECALCULATING|STALE|FAILED`, `lastSuccessfulAt`, 조건부 retryable failure key를 제공한다. FRESH는 두 revision이 같음을 뜻하고, stale/failed snapshot은 계산된 revision을 함께 표시한다.

`TimelineDomainView`는 raw Context 조합 대신 `CanonicalTimelineRow` 목록을 제공한다. 각 row는 `rowId`, 날짜-clipped canonical `[startAt,endAt)`, 이미 계산한 `duration`, final classification/status/revision, app/activity relation summary, `independent|composite`, confirmation provenance, coverage/freshness를 가진다. UI는 row를 재분할·union·계산하지 않는다.

### SC-V10~SC-V17. 보완 검증

- SC-V10: superseded Context는 current metrics input에 없다.
- SC-V11: MeasurementDay 재시도는 idempotent이고 source revision 변경만 upsert한다.
- SC-V12: partial coverage와 incomplete coverage, DST elapsed day가 구분된다.
- SC-V13: lifetime GoalProgress와 period summary가 분리된다.
- SC-V14: resolution은 source recovered ID 집합·effective interval에 안정적으로 연결된다.
- SC-V15: entity registry 전체가 retention/backup/full deletion 범위에 있다.
- SC-V16: canonical Timeline row의 duration과 UI 비계산 원칙이 보장된다.
- SC-V17: stale/failed view는 revision과 마지막 성공 snapshot을 구분한다.
