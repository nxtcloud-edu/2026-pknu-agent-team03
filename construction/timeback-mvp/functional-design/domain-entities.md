# `timeback-mvp` CP-0 공통 도메인 엔터티와 데이터 계약

## 1. 문서 상태와 역할

- 단계: CONSTRUCTION STEP 01 기능 설계
- 범위: CP-0 공통작업
- 상태: 게이트 2 승인 전 후보
- 기준 계약: CT-01, CT-02의 논리 엔터티와 값 상태
- 지원 계약: CT-03, CT-05가 주고받는 논리 메시지 형태

이 문서는 업무 데이터를 저장 제품의 스키마나 통신 직렬화 형식으로 바꾸지 않는다. 필드는 네 트랙이 같은 의미로 사용하기 위한 논리 계약이다. 시간·중첩·저장·백업 규칙은 `business-rules.md`, 호출 순서는 `business-logic-model.md`가 기준이다.

## 2. 공통 논리 형식

| 논리 형식 | 의미 | 값 없음 처리 |
|---|---|---|
| `Identifier` | 같은 종류의 레코드를 구분하는 값 | 필수 식별자는 값 없음 불가 |
| `AnonymousUserId` | APP-02가 기기 안에서 변환한 불투명 사용자 식별자 | 변환 실패는 별도 차단 상태 |
| `Timestamp` | 실제 사건 또는 상태 변경이 발생한 시각 | 업무상 선택 필드만 값 없음 허용 |
| `Duration` | 시작·종료 사이의 실제 경과시간 | 미계산 상태를 영으로 대신하지 않음 |
| `TimeRange` | `startAt`, `endAt`으로 표현한 시작 포함·종료 미포함 구간 | 종료가 없는 실행 중 타이머는 완성된 구간과 구분 |
| `Classification` | 승인된 앱·Context 분류 | 값 없음과 `NEUTRAL`은 다름 |
| `EntityType` | 저장·변경 대상 레코드 종류 | 알 수 없는 종류를 임의 저장하지 않음 |
| `OperationStatus` | 명령·조회 처리 결과 종류 | 상태와 상세 오류를 분리 |
| `RetentionSelection` | 사용자가 승인된 선택지 중 고른 보관 설정을 나타내는 불투명 값 | 선택 전에는 값 없음 가능; 선택지 목록은 여기서 만들지 않음 |

실제 언어의 자료형, 시각 단위, 식별자 생성법은 후속 기술 단계에서 결정한다.

## 3. 공통 상태 사전

### 3.1 분류와 입력 종류

| 상태 집합 | 허용 값 | 사용 위치 |
|---|---|---|
| `AppClassification` | `PRODUCTIVE`, `LEISURE`, `WASTE`, `NEUTRAL` | App 기본 분류 |
| `ContextClassification` | `PRODUCTIVE`, `LEISURE`, `WASTE`, `MIXED`, `NEUTRAL` | Context 최종 분류 |
| `ActivityType` | `EXERCISE`, `STUDY`, `DEVELOPMENT`, `READING`, `LEISURE`, `CUSTOM` | Activity 종류 |
| `RecoveredMethod` | `TIMER`, `MANUAL` | RecoveredTime 기록 방식 |
| `EntityOperation` | `CREATE`, `UPDATE`, `DELETE` | CommittedChange·BackupChange |

표시 문구와 지역화는 이 상태 값과 별도다.

### 3.2 결과 상태

| 상태 | 의미 | 값 제공 |
|---|---|---|
| `SUCCESS` | 요청 완료 | 명령 또는 조회별 결과 제공 가능 |
| `EMPTY` | 조회 성공, 대상 데이터 없음 | 빈 목록 또는 값 없음 |
| `BLOCKED` | 권한·확인·선행 상태 필요 | 차단 이유 제공 |
| `RETRYABLE_FAILURE` | 동일 의미의 요청 재시도 가능 | 재시도 이유 제공 |
| `PARTIAL_FAILURE` | 여러 대상 중 일부만 완료 | 대상별 상태 제공 |
| `FAILURE` | 완료로 처리할 수 없는 실패 | 오류 분류 제공 |

### 3.3 공통 상태 이유 분류

| 분류 | 의미 | 대표 소비자 |
|---|---|---|
| `PERMISSION_REQUIRED` | 현재 Usage Access가 허용되지 않음 | UI-01, APP-03 |
| `IDENTITY_UNAVAILABLE` | 익명 식별자 변환을 완료하지 못함 | UI-01, APP-12 |
| `INVALID_TIME_RANGE` | 시작·종료 구간이 유효하지 않음 | APP-04, APP-06, APP-09, UI-03·UI-05 |
| `NOT_FOUND` | 요청 식별자에 해당하는 대상이 없음 | APP-05–APP-10 |
| `CONTEXT_CONFIRMATION_REQUIRED` | 충돌 Context의 사용자 확인이 필요함 | UI-03 |
| `REPRESENTATIVE_GOAL_REQUIRED` | 겹친 목표 구간의 대표 Goal 선택이 필요함 | UI-05 |
| `BASELINE_OBSERVING` | Baseline 관찰이 아직 완료되지 않은 정상 내용 상태 | UI-02, UI-07 |
| `OFFLINE` | 백업 경계에 연결할 수 없음 | UI-08, APP-12 |
| `REMOTE_RETRY_REQUIRED` | 서버 처리를 다시 시도해야 함 | APP-12–APP-13, UI-08 |
| `DELETION_INCOMPLETE` | 기기·서버 중 하나 이상 삭제가 미완료임 | UI-08 |
| `DATA_ACCESS_FAILURE` | 기기 기준 데이터 조회·저장을 완료하지 못함 | 모든 기능 경계 |

오류 분류는 화면 문구나 구현 예외 종류가 아니다.

## 4. CT-01 — 사건·세션·활동·Context 계약

### 4.1 계약 머리표

| 항목 | 내용 |
|---|---|
| 목적 | 기기 데이터와 도메인·UI 트랙이 같은 시간 구간과 Context를 사용하게 함 |
| 기준 정의 위치 | 이 문서 §4 |
| 제공자 | UsageEvent·AppSession은 `track-device-data`, Activity·Context는 `track-domain-engine` |
| 소비자 | `track-domain-engine`, `track-ui`, CT-03 저장 경계 |
| 명령·조회 | 명령과 조회 이름은 `business-logic-model.md` 및 `frontend-components.md` 참조 |
| 테스트 대역 | `FakeUsageEventSource`, `FakeScreenStateSource`, `ControlledTimeSource`, `FakeDeviceDataAuthority` |
| 변경 영향 | CT-02 지표, CT-03 저장, CT-04 Timeline, CT-06 세션·Context 시나리오 |

### 4.2 UsageEvent

| 필드 | 필수 | 의미 |
|---|---|---|
| `eventId` | 예 | 원본 사건 식별자 |
| `anonymousUserId` | 예 | 사건 소유 익명 사용자 |
| `packageName` | 예 | 운영체제가 제공한 앱 패키지명 |
| `eventType` | 예 | Foreground 또는 Background 관련 사건 종류 |
| `occurredAt` | 예 | 사건 발생 시각 |
| `collectedAt` | 예 | 앱이 사건을 읽어온 시각 |

- 기준 생성자: APP-03.
- 원본 사건이며 AppSession·Context와 구분한다.
- 화면 내용·앱 내부 콘텐츠·위치·자동 오프라인 활동 정보 필드는 없다.

### 4.3 AppSession

| 필드 | 필수 | 의미 |
|---|---|---|
| `sessionId` | 예 | 세션 식별자 |
| `packageName` | 예 | 사용 앱 패키지명 |
| `startAt` | 예 | 세션 시작 시각 |
| `endAt` | 예 | 보정·분할을 마친 종료 시각 |
| `duration` | 예 | 실제 경과시간 |
| `sourceEventIds` | 예 | 세션 생성 근거 UsageEvent 식별자 목록 |

- 기준 생성자: APP-04.
- 날짜 경계 분할 뒤 각 레코드는 완성된 `TimeRange`다.
- 근거 사건이 없는 합성 세션을 승인 없이 만들지 않는다.

### 4.4 Activity

| 필드 | 필수 | 의미 |
|---|---|---|
| `activityId` | 예 | 활동 식별자 |
| `type` | 예 | `ActivityType` |
| `customName` | 조건부 | `CUSTOM`일 때 사용자가 입력한 활동 이름 |
| `startAt` | 예 | 활동 시작 시각 |
| `endAt` | 예 | 활동 종료 시각 |

- 기준 생성자: APP-06.
- 사용자 입력이며 UsageEvent·AppSession과 구분한다.

### 4.5 Context

| 필드 | 필수 | 의미 |
|---|---|---|
| `contextId` | 예 | Context 식별자 |
| `sessionId` | 예 | 근거 AppSession 식별자 |
| `activityId` | 아니요 | 겹친 Activity가 있을 때 그 식별자 |
| `startAt` | 예 | 판정 구간 시작 시각 |
| `endAt` | 예 | 판정 구간 종료 시각 |
| `classification` | 예 | `ContextClassification` |
| `userConfirmed` | 예 | 사용자가 최종 의미를 확인·수정했는지 여부 |

- 기준 생성자: APP-07.
- `activityId` 값 없음은 Activity와 겹치지 않은 세션 구간을 뜻할 수 있으며 빈 문자열로 대신하지 않는다.
- 확인되지 않은 충돌 Context는 `classification=MIXED`, `userConfirmed=false`로 표현한다.
- 사용자 확정 Context와 App 기본 분류는 다른 데이터다.

### 4.6 App

| 필드 | 필수 | 의미 |
|---|---|---|
| `packageName` | 예 | 앱 식별 패키지명 |
| `displayName` | 예 | 사용자에게 표시할 앱 이름 |
| `defaultClassification` | 예 | `AppClassification` 기본값 |
| `updatedAt` | 예 | 기본 분류 마지막 변경 시각 |

App은 CT-01 Context 판정 입력이지만 최종 Context를 소유하지 않는다.

### 4.7 CT-01 결과 상태

| 상황 | 결과 표현 |
|---|---|
| 기간에 UsageEvent가 없음 | `QueryResult.status=EMPTY` |
| 열린 세션을 닫을 근거가 아직 없음 | 완성 AppSession으로 반환하지 않고 상세 처리 상태로 유지 |
| 유효하지 않은 Activity 구간 | `OperationResult.status=BLOCKED`, `INVALID_TIME_RANGE` |
| Context 충돌 | Context의 `MIXED`·미확정 상태와 `CONTEXT_CONFIRMATION_REQUIRED` |
| 저장 실패 | `FAILURE`, `DATA_ACCESS_FAILURE` |

### 4.8 CT-01 추적

- 요구사항: FR-1.3–FR-6.4, FR-7.1, NFR-2.1–NFR-2.3, NFR-5.1–NFR-5.2.
- 스토리: US-02–US-10.
- 구성 요소: OS-02–OS-03, OS-05, APP-03–APP-07, APP-10–APP-11, UI-03–UI-04.

## 5. CT-02 — Baseline·Goal·시간 지표 계약

### 5.1 계약 머리표

| 항목 | 내용 |
|---|---|
| 목적 | 도메인 계산 결과를 UI와 기기 데이터 트랙이 같은 값·미완료 상태로 사용하게 함 |
| 기준 정의 위치 | 이 문서 §5 |
| 제공자 | `track-domain-engine`의 APP-08–APP-09 |
| 소비자 | `track-ui`의 APP-10·UI-02·UI-05–UI-07, CT-03 저장 경계 |
| 명령·조회 | `ReadBaselineObservation`, `ReadTimeMetrics`, `ReadGoalProgress`; 변경 명령은 CT-04 작업 계약을 통해 APP-09에 전달 |
| 테스트 대역 | 고정 Context·Goal·RecoveredTime, `ControlledTimeSource`, `FakeDeviceDataAuthority` |
| 변경 영향 | CT-03 저장, CT-04 홈·목표·리포트, CT-06 지표 시나리오 |

### 5.2 Baseline

| 필드 | 필수 | 의미 |
|---|---|---|
| `baselineId` | 예 | Baseline 식별자 |
| `observationStart` | 예 | 관찰 시작 시각 |
| `observationEnd` | 조건부 | 관찰 완료 전에는 값 없음 |
| `baselineWasteDuration` | 조건부 | 관찰 완료·확정 전에는 값 없음 |
| `status` | 예 | `OBSERVING`, `CONFIRMED`, `RECALCULATION_PROPOSED` 중 현재 상태 |

`RECALCULATION_PROPOSED`에서도 기존 확정 Baseline 값은 자동 교체되지 않는다.

### 5.3 Goal

| 필드 | 필수 | 의미 |
|---|---|---|
| `goalId` | 예 | Goal 식별자 |
| `name` | 예 | 사용자 목표 이름 |
| `targetDuration` | 예 | 사용자가 정한 목표시간 |
| `createdAt` | 예 | 생성 시각 |

Goal 자체에는 계산된 누적시간을 기준 데이터로 중복 저장한다고 가정하지 않는다. 누적·진행 결과는 RecoveredTime에서 조회할 수 있어야 한다.

### 5.4 RecoveredTime

| 필드 | 필수 | 의미 |
|---|---|---|
| `recoveredId` | 예 | 회수 기록 식별자 |
| `goalId` | 예 | 연결된 Goal 식별자 |
| `method` | 예 | `TIMER` 또는 `MANUAL` |
| `startAt` | 예 | 기록 시작 시각 |
| `endAt` | 예 | 기록 종료 시각 |
| `duration` | 예 | 실제 경과시간 |

실행 중 타이머 상태는 완성된 RecoveredTime과 구분하며, 완료 뒤에만 이 레코드가 된다.

### 5.5 시간 지표 조회 형태

#### `TimeMetrics`

| 필드 | 필수 | 의미 |
|---|---|---|
| `period` | 예 | 조회한 `PeriodQuery` |
| `wasteDuration` | 조건부 | 최종 Context에서 계산된 낭비시간 |
| `baselineStatus` | 예 | Baseline 현재 상태 |
| `baselineWasteDuration` | 조건부 | Baseline 확정 뒤 제공 |
| `securedDuration` | 조건부 | Baseline 기반 계산 가능할 때 제공 |
| `recoveredDuration` | 예 | 조회 기간의 중복 제거된 회수시간 |
| `recoveryRate` | 조건부 | 확보한 시간이 존재해 계산 가능할 때 제공 |

#### `GoalProgress`

| 필드 | 필수 | 의미 |
|---|---|---|
| `goalId` | 예 | 대상 Goal |
| `targetDuration` | 예 | 목표시간 |
| `accumulatedDuration` | 예 | 대표 목표 규칙을 적용한 누적시간 |
| `progress` | 예 | 목표시간 대비 누적 진행 결과 |

#### `BaselineObservation`

| 필드 | 필수 | 의미 |
|---|---|---|
| `status` | 예 | Baseline 상태 |
| `observationStart` | 예 | 관찰 시작 시각 |
| `observationEnd` | 조건부 | 관찰이 완료되면 제공 |
| `remainingObservation` | 조건부 | 관찰 중일 때 남은 기간 표현 |

### 5.6 CT-02 결과 상태

| 상황 | 결과 표현 |
|---|---|
| Baseline 관찰 중 | `BLOCKED`가 아니라 정상 `content` 안의 `baselineStatus=OBSERVING`; Baseline 기반 값은 없음 |
| 기간 데이터 미수집 | `QueryResult.status=EMPTY` |
| 낭비시간 계산 결과가 영 | `SUCCESS`와 계산된 Duration 값 |
| 대표 Goal 미선택 | `BLOCKED`, `REPRESENTATIVE_GOAL_REQUIRED` |
| 확보한 시간이 없어 회수율 계산 불가 | `SUCCESS`, `recoveryRate` 값 없음과 이유 상태 |

### 5.7 CT-02 추적

- 요구사항: FR-4.3–FR-4.4, FR-7–FR-9, NFR-2.3–NFR-2.4.
- 스토리: US-10–US-21.
- 구성 요소: OS-05, APP-08–APP-11, UI-02, UI-05–UI-07.

## 6. User와 데이터 소유 연결

### 6.1 User

| 필드 | 필수 | 의미 |
|---|---|---|
| `anonymousUserId` | 예 | 변환된 익명 사용자 식별자 |
| `retentionSelection` | 아니요 | 사용자가 선택한 보관 설정; 선택 전 값 없음 가능 |
| `createdAt` | 예 | 익명 사용자 영역 생성 시각 |

### 6.2 소유 관계

| 관계 | 의미 |
|---|---|
| User → App·UsageEvent·AppSession·Activity·Context·Baseline·Goal·RecoveredTime | 모두 같은 익명 사용자 영역의 기기 기준 데이터 |
| UsageEvent → AppSession | `sourceEventIds`로 재구성 근거 연결 |
| AppSession·Activity → Context | 세션은 필수, 활동은 겹친 경우 연결 |
| Goal → RecoveredTime | 회수 기록은 하나의 Goal에 연결 |
| 기준 엔터티 → BackupChange | 저장된 엔터티 변경을 백업 조정 상태가 참조 |
| User → DeletionJob | 같은 사용자 영역의 기기·서버 삭제를 하나로 추적 |

서버 사본은 이 관계의 새 소유자가 아니다.

## 7. CT-03 지원 메시지 — 저장·조회·변경 통지

CT-03의 동작 기준은 `business-logic-model.md` §5다.

### 7.1 `EntityType`

`USER`, `APP`, `USAGE_EVENT`, `APP_SESSION`, `ACTIVITY`, `CONTEXT`, `BASELINE`, `GOAL`, `RECOVERED_TIME`를 구분한다. 백업·삭제 조정 상태는 업무 엔터티와 별도다.

### 7.2 `CommittedChange`

| 필드 | 필수 | 의미 |
|---|---|---|
| `entityType` | 예 | 변경된 엔터티 종류 |
| `entityId` | 예 | 변경된 엔터티 식별자 |
| `operation` | 예 | `CREATE`, `UPDATE`, `DELETE` |
| `occurredAt` | 예 | 로컬 변경 완료 시각 |

### 7.3 `CommitResult`

| 필드 | 필수 | 의미 |
|---|---|---|
| `status` | 예 | `SUCCESS` 또는 실패 종류 |
| `committedChanges` | 성공 시 | 완료된 변경 목록 |
| `error` | 실패 시 | 논리 오류 분류와 설명 자료 |

### 7.4 `PeriodQuery`

| 필드 | 필수 | 의미 |
|---|---|---|
| `startAt` | 예 | 조회 시작 포함 시각 |
| `endAt` | 예 | 조회 종료 미포함 시각 |

기기 현지 날짜·주·월을 실제 시각 구간으로 바꾸는 책임은 OS-05 계약을 사용하는 제공자에게 있다.

### 7.5 공통 결과 봉투

#### `OperationResult<T>`

| 필드 | 필수 | 의미 |
|---|---|---|
| `status` | 예 | §3.2 결과 상태 |
| `value` | 조건부 | 성공 또는 부분 성공의 논리 결과 |
| `error` | 조건부 | 차단·실패 분류와 표시 가능한 설명 자료 |

#### `QueryResult<T>`

| 필드 | 필수 | 의미 |
|---|---|---|
| `status` | 예 | `SUCCESS`, `EMPTY`, `BLOCKED`, 실패 종류 |
| `content` | 조건부 | 조회 데이터 또는 목록 |
| `error` | 조건부 | 차단·실패 분류와 설명 자료 |

`EMPTY`와 `SUCCESS`의 계산된 값은 서로 바꿔 쓰지 않는다.

## 8. CT-05 지원 메시지 — 익명 백업·보관·삭제

CT-05의 동작 기준은 `business-logic-model.md` §6이다.

### 8.1 BackupChange

| 필드 | 필수 | 의미 |
|---|---|---|
| `changeId` | 예 | 백업 변경 식별자 |
| `entityType` | 예 | 대상 엔터티 종류 |
| `entityId` | 예 | 대상 엔터티 식별자 |
| `operation` | 예 | `CREATE`, `UPDATE`, `DELETE` |
| `occurredAt` | 예 | 로컬 변경 시각 |
| `state` | 예 | `PENDING`, `ACCEPTED`, `RETRYABLE_FAILURE`, `FAILED` |
| `retryCount` | 예 | 같은 변경의 시도 횟수 기록 |

### 8.2 `BackupBatch`

| 필드 | 필수 | 의미 |
|---|---|---|
| `anonymousUserId` | 예 | 백업 묶음 소유자 |
| `changes` | 예 | 하나 이상의 BackupChange와 필요한 레코드 사본 |

### 8.3 `BackupItemResult`

| 필드 | 필수 | 의미 |
|---|---|---|
| `changeId` | 예 | 요청 항목과 대응하는 식별자 |
| `status` | 예 | `ACCEPTED`, `RETRYABLE_FAILURE`, `FAILED` |
| `error` | 실패 시 | 논리 실패 분류와 설명 자료 |

### 8.4 `RetentionApplyResult`

| 필드 | 필수 | 의미 |
|---|---|---|
| `retentionSelection` | 예 | 사용자가 승인한 선택값 |
| `deviceStatus` | 예 | 기기 적용 상태 |
| `serverStatus` | 예 | 서버 사본 적용 상태 |

### 8.5 DeletionJob

| 필드 | 필수 | 의미 |
|---|---|---|
| `jobId` | 예 | 전체 삭제 작업 식별자 |
| `requestedAt` | 예 | 사용자가 확정한 요청 시각 |
| `deviceStatus` | 예 | `PENDING`, `IN_PROGRESS`, `COMPLETED`, `FAILED` |
| `serverStatus` | 예 | `PENDING`, `IN_PROGRESS`, `COMPLETED`, `FAILED` |
| `completedAt` | 조건부 | 두 경계가 모두 완료일 때만 존재 |

### 8.6 CT-05 결과 상태

| 상황 | 결과 표현 |
|---|---|
| 모든 변경 수락 | 항목별 `ACCEPTED` |
| 일부 변경만 수락 | 묶음 `PARTIAL_FAILURE`와 항목별 상태 |
| 오프라인 | `RETRYABLE_FAILURE`, `OFFLINE` |
| 서버 삭제 진행 중 | `DeletionJob.serverStatus=IN_PROGRESS` |
| 기기 또는 서버 삭제 실패 | 전체 `PARTIAL_FAILURE`, `completedAt` 없음 |

## 9. CT-04에서 참조할 조회 모델

기준 화면 계약은 `frontend-components.md`다. UI는 다음 논리 모델을 참조한다.

| 조회 모델 | 포함 데이터 |
|---|---|
| `PermissionViewData` | 현재 권한 상태, 익명 식별자 준비 상태 |
| `HomeViewData` | TimeMetrics, GoalProgress 목록, BaselineObservation |
| `TimelineViewData` | 선택 기간, AppSession·Activity·Context 표시 항목 |
| `AppManagementViewData` | App 목록과 기본 분류 |
| `RecoveryViewData` | Goal 목록, 실행 중 타이머 상태, 중첩 확인 상태 |
| `GoalsViewData` | Goal과 GoalProgress 목록 |
| `ReportViewData` | 선택 기간, TimeMetrics, 목표별 회수 결과 |
| `DataManagementViewData` | 백업 상태, 보관 선택, DeletionJob 상태 |

구체적인 화면 상태와 사용자 작업은 `frontend-components.md`에서 정의한다.

## 10. 값 없음과 빈 결과 규약

| 표현 | 사용 예 | 사용하지 않는 대체 표현 |
|---|---|---|
| 선택 필드 값 없음 | Activity 없는 Context, 관찰 중 Baseline 값, 미계산 회수율 | 빈 문자열, 임의의 영 값 |
| `QueryResult.EMPTY` | 선택 기간에 레코드 없음 | 저장 오류, 계산된 영 값 |
| `BLOCKED` | 권한 또는 사용자 확인 필요 | 일반 `FAILURE` |
| `PARTIAL_FAILURE` | 백업·삭제 일부 완료 | 전부 성공 또는 전부 실패 |
| `DeletionJob.completedAt` 값 없음 | 전체 삭제 미완료 | 요청 시각이나 한쪽 완료 시각 |

## 11. 엔터티 변경 영향

| 변경 엔터티 | 다시 판정·조회할 공통 범위 | 관련 계약 |
|---|---|---|
| App 기본 분류 | 미확정 Context 후보와 관련 화면 | CT-01, CT-04 |
| UsageEvent | AppSession, Context, 시간 지표, Timeline·홈·리포트 | CT-01–CT-04 |
| AppSession | Context, 시간 지표, Timeline·홈·리포트 | CT-01–CT-04 |
| Activity | Context, 시간 지표, Timeline·홈·리포트 | CT-01–CT-04 |
| Context | 낭비·확보·회수 지표, Timeline·홈·리포트 | CT-01–CT-04 |
| Baseline | 확보시간·회수율, 홈·리포트 | CT-02–CT-04 |
| Goal | 목표·타이머 입력과 목표 조회 | CT-02–CT-04 |
| RecoveredTime | 목표 누적·회수 지표, 홈·목표·리포트 | CT-02–CT-04 |
| 모든 기준 엔터티 | BackupChange와 UI-08 상태 | CT-03, CT-05 |

이 표는 재계산 알고리즘을 정하지 않고 트랙 사이의 영향 범위만 고정한다.

## 12. 공통 변경 검토표

다음 변경은 한 트랙만의 내부 수정이 아니다.

| 변경 종류 | 함께 검토할 대상 |
|---|---|
| CT-01 필드·상태 변경 | 기기 데이터, 도메인, UI, CT-03, CT-06 |
| CT-02 값·미완료 상태 변경 | 도메인, UI, CT-03, CT-06 |
| CT-03 결과·변경 통지 변경 | 기기 데이터, 도메인, 백업 서버, CT-05–CT-06 |
| CT-05 백업·삭제 상태 변경 | 기기 데이터, 백업 서버, UI, CT-03–CT-04, CT-06 |
| 오류 분류 변경 | 모든 소비자 화면과 계약 시나리오 |

## 13. 이번 범위에서 정하지 않은 것

- 실제 자료형 크기, 날짜·시간 라이브러리, 식별자 생성 알고리즘
- 테이블·컬렉션·인덱스·관계 매핑·직렬화 필드명
- API 요청·응답 문법과 네트워크 경로
- Context 확인 의미의 내부 분류 매핑
- Baseline 재산정 제안 생성 기준과 보관 기간 선택지 목록
- 트랙별 코드 구조와 구현 클래스

## Track Data — device-data

이 섹션은 `track-device-data`의 논리 모델과 CT-03 변경 제안을 소유한다. 제안 항목은 `track-domain-engine`·`track-backup-server` 검토와 단일 STEP 01 게이트 전에는 CP-0 공통 계약을 대체하지 않는다.

### 트랙 전용 논리 형식과 상태

| 이름 | 의미 | 허용 값·경계 |
|---|---|---|
| `DataOwnerScope` | 한 APP-11 명령·조회가 접근할 변환된 익명 사용자 범위 | 값 없음 또는 다른 사용자 범위로 암묵 전환 불가 |
| `ChangeCursor` | 사용자별 확정 변경 위치를 잇는 불투명 값 | 최초 읽기만 값 없음 허용; 소비자가 내부 구조를 해석하지 않음 |
| `UsageAccessStatus` | APP-01의 현재 OS-01 관측 결과 | `GRANTED`, `NOT_GRANTED` |
| `LogicalUsageEventType` | APP-03이 승인된 Android 사건을 정규화한 종류 | `FOREGROUND`, `BACKGROUND` |

`TimeBoundaryContext`는 OS-05가 제공하는 `observedAt`, 불투명한 `localTimeZone`, 요청한 기간 의미, 그 의미를 변환한 절대 `PeriodQuery`를 가진다. 소비자는 현지 날짜를 고정 24시간으로 바꾸지 않고 이 절대 경계를 사용한다.

| 오류 제안 | 의미 | 대표 소비자 |
|---|---|---|
| `OS_ACCESS_FAILURE` | 현재 Usage Access 상태를 읽거나 설정 화면을 열지 못함 | APP-01, UI-01 |
| `USAGE_SOURCE_FAILURE` | 승인된 수집 창의 UsageEvent 조회를 완료하지 못함 | APP-03, UI-02·UI-03 새로고침 |
| `OWNER_SCOPE_VIOLATION` | 요청 사용자 범위와 대상 레코드의 소유 범위가 일치하지 않음 | APP-11 소비자 |

UsageEvent의 `eventType`은 OS-02의 Android 사건을 `LogicalUsageEventType`으로 정규화한 값이다. 구체 Android 상수 매핑은 후속 기술 설계가 소유한다.

### CT-03 변경 제안

| 제안 | 근거 | 영향받는 소비자·검증 |
|---|---|---|
| 모든 CT-03 요청의 `DataOwnerScope` | 사용자 필드가 없는 AppSession 등도 다른 익명 사용자와 섞이지 않아야 함 | 도메인·백업 트랙, `DD-DATA-02` |
| `ReplacePeriodRecords` 원자 명령 | 재구성·재계산 뒤 사라진 파생 레코드가 잔류하지 않아야 함 | 도메인 트랙 APP-07–APP-09, `DD-DATA-04` |
| `CommittedChange`의 `anonymousUserId`, `changePosition` | APP-12가 사용자별 변경을 누락 없이 이어 읽어야 함 | 백업 트랙 APP-12, `DD-DATA-03` |
| `CommitResult.nextChangeCursor`, `CommittedChangePage` | 성공한 변경과 소비 위치를 분리해 재시도를 안전하게 해야 함 | 백업 트랙 APP-12, `DD-DATA-03` |

`CommittedChangePage`는 `anonymousUserId`, 확정 순서의 `changes`, 마지막 반환 변경 뒤 `nextCursor`를 가진다. 같은 입력 커서 재조회는 아직 인계하지 못한 변경을 건너뛰지 않는다. `CollectionCheckpoint`와 `OpenSessionCandidate`는 사용자 범위 로컬 제어 상태이며 일반 `EntityType`이나 백업 대상이 아니지만 기기 전체 삭제에는 포함한다.

### 권한·수집 모델

#### `AccessState`

| 필드 | 필수 | 의미 |
|---|---|---|
| `usageAccessStatus` | 예 | OS-01에서 방금 관측한 `UsageAccessStatus` |
| `observedAt` | 예 | OS-05 기준 관측 시각 |
| `canCollect` | 예 | 현재 관측에서 OS-02 호출이 허용되는지 여부 |

OS-01 조회 자체의 실패는 임의의 `UsageAccessStatus` 값이 아니라 `OperationResult` 또는 `QueryResult`의 `OS_ACCESS_FAILURE`로 표현한다.

#### `SourceUsageEvent`

OS-02가 APP-03에 제공하는 경계 값이며 아직 기준 UsageEvent가 아니다.

| 필드 | 필수 | 의미 |
|---|---|---|
| `packageName` | 예 | Android가 제공한 패키지명 |
| `eventType` | 예 | 승인된 Foreground·Background 관련 원본 사건 종류 |
| `occurredAt` | 예 | 사건 발생 시각 |
| `sourceOrder` | 예 | 같은 조회 결과 안에서 Android 경계가 제공한 원본 순서 |

`sourceOrder`는 결정적 처리용 조회 메타데이터이며 화면·콘텐츠 정보가 아니다. APP-03은 이 값과 사건 필드로 안정적인 `eventId`를 만들 수 있어야 하지만 생성 알고리즘은 여기서 정하지 않는다.

#### `CollectionWindow`와 `CollectionCheckpoint`

| 모델 | 필드 | 의미 |
|---|---|---|
| `CollectionWindow` | `requestedRange`, `effectiveRange` | 호출자가 요청한 범위와 체크포인트를 적용해 실제 조회한 시작 포함·종료 미포함 범위 |
| `CollectionCheckpoint` | `anonymousUserId`, `lastSuccessfulEnd` | 원본 사건 저장까지 성공한 마지막 수집 경계 |

체크포인트는 사용자 범위의 로컬 제어 상태다. 서버 백업 사본이나 시간 지표 입력이 아니며 원본 사건 저장과 같은 원자 경계에서만 진행한다.

#### `CollectionResult`

| 필드 | 필수 | 의미 |
|---|---|---|
| `window` | 예 | 요청·실제 수집 창 |
| `newEventCount` | 예 | 이번 확정에서 새로 저장된 UsageEvent 수 |
| `duplicateEventCount` | 예 | 겹친 재조회에서 이미 존재해 새로 만들지 않은 사건 수 |
| `checkpoint` | 예 | 성공 뒤의 체크포인트 |
| `completedAt` | 예 | 수집과 로컬 확정 완료 시각 |

빈 조회 성공은 `newEventCount=0`인 성공 결과다. 조회·저장 실패에서는 새 체크포인트를 제공하지 않는다.

### 세션 재구성 모델

#### `ScreenEndEvent`

| 필드 | 필수 | 의미 |
|---|---|---|
| `occurredAt` | 예 | OS-03이 관측한 화면 종료 시각 |
| `sourceOrder` | 예 | 같은 시각 사건 사이의 경계 순서 |

화면 내용이나 종료 전 표시 앱 정보는 포함하지 않는다.

#### `OpenSessionCandidate`

| 필드 | 필수 | 의미 |
|---|---|---|
| `anonymousUserId` | 예 | 후보 소유 사용자 범위 |
| `packageName` | 예 | 열린 앱 패키지명 |
| `startAt` | 예 | Foreground 시작 시각 |
| `sourceEventIds` | 예 | 현재까지 후보가 참조한 원본 사건 식별자 |

종료 근거가 없는 처리 상태이며 완성 AppSession, Timeline 항목 또는 시간 지표 입력이 아니다. 다음 재구성 성공에서 닫히거나 다른 후보로 교체될 때까지 사용자 범위 로컬 상태로 유지한다.

#### `ReconstructionResult`

| 필드 | 필수 | 의미 |
|---|---|---|
| `affectedWindow` | 예 | 다시 읽고 교체한 원본·세션 영향 범위 |
| `completedSessions` | 예 | 자정 분할까지 끝나 APP-11에 확정된 AppSession 목록 |
| `openCandidate` | 아니요 | 종료 근거가 없어 다음 실행으로 넘긴 후보 |
| `ignoredEventIds` | 예 | 근거 없는 Background·중복 Foreground 등 세션을 만들지 않은 원본 사건 |
| `completedAt` | 예 | 교체 성공 시각 |

`ignoredEventIds`는 원본 사건 삭제를 뜻하지 않는다. 저장 실패에서는 새 결과를 성공으로 제공하지 않는다.

### APP-11 사용자 범위와 기간 조회 모델

#### `DataOwnerScope`

| 필드 | 필수 | 의미 |
|---|---|---|
| `anonymousUserId` | 예 | APP-02가 제공한 변환된 익명 식별자 |

원본 하드웨어 값은 이 범위에 포함하지 않는다. 모든 레코드는 명령 인자의 `DataOwnerScope` 안에서 소유 관계를 갖고, 레코드에 사용자 필드가 없더라도 저장 경계가 소유 범위를 보존한다.

#### 기간 조회 포함 기준

| 엔터티 종류 | `ReadPeriod` 포함 기준 |
|---|---|
| UsageEvent | `occurredAt`이 조회 구간 안에 있음 |
| AppSession·Activity·Context·RecoveredTime | 레코드의 TimeRange가 조회 구간과 겹침 |
| Baseline | 관찰 범위가 조회 구간과 겹치거나 해당 기간 계산에 사용되는 확정 기준 |
| User·App·Goal | 기간 조회 대신 식별자·목록 조회 사용 |

기본 정렬은 사건·구간 시작 시각 오름차순이며 같은 시각의 안정적인 순서는 소비자 계약이 요구할 때 보존한다. 물리 정렬 키는 후속 단계에서 결정한다.

### 기기 데이터 새로고침 결과

`DeviceDataRefreshResult`는 UI가 계산하지 않고 APP-03·APP-04의 로컬 갱신 결과를 구분해 표시하기 위한 기능 결과다.

| 필드 | 필수 | 의미 |
|---|---|---|
| `accessState` | 예 | 갱신 직전 현재 권한 관측 |
| `collectionResult` | 성공 시 | 수집 창과 신규 사건 수 |
| `reconstructionResult` | 성공 시 | 확정 세션과 열린 후보 상태 |
| `dataFreshnessAt` | 성공 시 | 로컬 수집·재구성이 완료된 시각 |
| `status` | 예 | `SUCCESS`, `BLOCKED`, `RETRYABLE_FAILURE`, `FAILURE` |
| `error` | 실패·차단 시 | 공통 상태 이유 분류 |

새 사건 수가 영이어도 갱신 성공이면 `SUCCESS`다. UI는 이 결과의 수를 시간 지표로 해석하지 않고 APP-10 조회를 다시 요청한다.

### 후속 기술 단계로 넘기는 항목

- Android 사건 상수와 `LogicalUsageEventType`의 구체 매핑
- 안정적인 `eventId`·`sessionId`·`ChangeCursor` 생성·표현 방식
- 최초 수집 과거 범위, 조회 페이지 크기와 물리 정렬·동시성 방식
