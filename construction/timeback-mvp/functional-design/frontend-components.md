# `timeback-mvp` CP-0 공통 화면 계약

## 1. 문서 상태와 역할

- 단계: CONSTRUCTION STEP 01 기능 설계
- 범위: CP-0 공통작업
- 상태: 게이트 2 승인 전 후보
- 기준 계약: CT-04 사용자 작업·조회 결과·화면 상태
- 지원 계약: CT-06의 고정 화면 결과와 기능 경계 대역

이 문서는 UI-01–UI-08의 공통 입력·출력과 상태 의미만 고정한다. 화면 프레임워크, 위젯, 디자인 시스템, 스타일, 네비게이션 제품, 구현 코드는 정하지 않는다.

## 2. 화면 경계 원칙

- UI는 APP-01–APP-13에 사용자 명령을 전달하고 APP-10 또는 해당 기능 경계의 조회 결과를 표시한다.
- UI는 시간 구간, 중첩, Context, Baseline, 확보시간, 되찾은 시간, 회수율을 계산하지 않는다.
- 명령 성공 뒤 영향받는 조회를 다시 요청한다.
- 로딩, 내용 있음, 데이터 없음, 차단, 재시도 가능 오류, 부분 실패를 구분한다.
- 화면 문구와 시각 표현은 논리 상태 값과 별도다.
- UI가 표시하는 엔터티와 지표의 기준 정의는 `domain-entities.md`를 사용한다.

## 3. CT-04 — 사용자 작업·조회·화면 상태 계약

### 3.1 계약 머리표

| 항목 | 내용 |
|---|---|
| 목적 | 실제 기기·도메인·서버 구현 전에 UI가 같은 명령·조회·상태 계약으로 작업하게 함 |
| 기준 정의 위치 | 이 문서 |
| 제공자 | APP-01, APP-05–APP-10, APP-12–APP-13의 기능 경계 |
| 소비자 | `track-ui`의 UI-01–UI-08 |
| 입력 | 사용자 작업, 선택한 식별자·기간·분류·시간 구간 |
| 출력 | `QueryResult<ViewData>`, `OperationResult<ActionData>` |
| 테스트 대역 | `FakeFeatureGateway`와 §12의 고정 결과 |
| 변경 영향 | CT-01–CT-03 데이터·지표, CT-05 상태, CT-06 UI 시나리오 |

### 3.2 공통 화면 상태

| 상태 | 의미 | 데이터 유지 원칙 |
|---|---|---|
| `INITIAL` | 아직 첫 조회를 시작하지 않음 | 표시 결과 없음 |
| `LOADING` | 첫 조회 또는 화면 전체 조회 중 | 첫 조회면 결과 없음 |
| `REFRESHING` | 기존 내용을 유지한 채 재조회 중 | 기존 내용 유지 |
| `CONTENT` | 표시 가능한 조회 결과가 있음 | 반환된 최신 내용 사용 |
| `EMPTY` | 조회는 성공했지만 표시할 데이터가 없음 | 빈 상태 동작 제공 가능 |
| `BLOCKED` | 화면의 주요 사용을 막는 권한·식별자 등 선행 상태 필요 | 차단 이유와 허용된 동작만 제공 |
| `RETRYABLE_ERROR` | 다시 시도할 수 있는 실패 | 안전한 기존 내용 유지 가능 |
| `PARTIAL_FAILURE` | 여러 경계 중 일부만 완료 | 대상별 상태 표시 |
| `ERROR` | 자동 완료를 가정할 수 없는 실패 | 오류와 안전한 재진입 동작 표시 |

### 3.3 사용자 작업 결과

| 결과 | UI 처리 |
|---|---|
| `SUCCESS` | 영향받는 조회를 다시 요청하고 성공 상태 표시 |
| `BLOCKED` | 오류로 합치지 않고 필요한 확인·권한·선행 동작 표시 |
| `RETRYABLE_FAILURE` | 입력과 기존 데이터를 훼손하지 않고 다시 시도 동작 제공 |
| `PARTIAL_FAILURE` | 성공·실패 경계를 각각 표시 |
| `FAILURE` | 완료로 표시하지 않고 오류 상태 유지 |

### 3.4 공통 작업 입력

- 식별자는 화면 표시 순서나 배열 위치가 아니라 도메인 `Identifier`를 전달한다.
- 시간 입력은 완성된 `TimeRange`로 전달하고 UI가 기간을 계산하지 않는다.
- 분류 입력은 `domain-entities.md`의 허용 상태를 사용한다.
- 기간 선택은 일·주·월 의미를 기능 경계에 전달하고, 실제 현지 시간 구간 해석은 OS-05 계약을 사용하는 제공자가 수행한다.

## 4. UI-01 권한·초기 진입 계약

### 4.1 조회와 작업

| 종류 | 이름 | 입력 | 결과 |
|---|---|---|---|
| 조회 | `ReadAccessState` | 없음 | `PermissionViewData` |
| 작업 | `OpenUsageAccessSettings` | 없음 | 설정 이동 요청 결과 |
| 작업 | `RefreshAccessState` | 없음 | 현재 권한·식별자 준비 상태 재조회 |

### 4.2 표시 데이터

`PermissionViewData`:

- 현재 Usage Access 상태
- 익명 식별자 준비 상태
- 주요 화면 진입 가능 여부
- 차단 또는 실패 분류

### 4.3 상태

| 상태 | 조건 | 허용 동작 |
|---|---|---|
| `LOADING` | 현재 권한 확인 중 | 대기 |
| `BLOCKED(PERMISSION_REQUIRED)` | 권한 미허용·회수 | 설정 열기, 상태 다시 확인 |
| `LOADING` | 권한 허용 후 식별자 준비 중 | 대기 |
| `CONTENT` | 권한·식별자 준비 완료 | 주요 화면 진입 |
| `BLOCKED(IDENTITY_UNAVAILABLE)` | 식별자 변환 실패 | 상태 다시 확인; 임의 대체 없음 |
| `ERROR` | 권한 상태 조회 실패 | 다시 확인 |

- 의존: CT-04, CT-05의 익명 식별자 상태, CT-06.
- 추적: FR-1.1–FR-1.2, FR-10.1, US-01, US-22, APP-01–APP-02, OS-01, OS-04.

## 5. UI-02 홈 대시보드 계약

### 5.1 조회와 작업

| 종류 | 이름 | 입력 | 결과 |
|---|---|---|---|
| 조회 | `ReadHome` | 현재 기간 의미 | `HomeViewData` |
| 작업 | `RefreshHome` | 없음 | 새 사건 반영 흐름 시작 결과 |
| 이동 | `OpenTimeline`, `OpenGoals`, `OpenReport` | 선택 목적 | 대상 화면 이동 |

### 5.2 표시 데이터

`HomeViewData`:

- 오늘 낭비시간
- Baseline 현재 상태와 비교 결과
- 확보한 시간, 되찾은 시간, 회수율의 제공 가능 상태와 값
- 현재 Goal별 진행 결과
- 데이터 기준 시각 또는 최신 상태

### 5.3 상태

| 상태 | 조건 | 표시 원칙 |
|---|---|---|
| `LOADING` | 첫 조회 | 전체 로딩 |
| `REFRESHING` | 새 사건 반영 중 | 기존 지표 유지 |
| `CONTENT` | 지표 조회 성공 | 반환된 도메인 값 표시 |
| `CONTENT` 내 `BASELINE_OBSERVING` | 관찰 중 | 확보시간·회수율 대신 관찰 상태 표시 |
| `EMPTY` | 수집된 기간 데이터 없음 | 수집 조건과 Timeline 이동 제공 |
| `RETRYABLE_ERROR` | 새로고침 실패 | 기존 내용 유지, 다시 시도 |
| `ERROR` | 지표 조회 실패 | 계산 오류 표시, 임의 값 금지 |

- 의존: CT-02, CT-04, CT-06.
- 추적: FR-7, FR-9.1, US-10–US-14, APP-08–APP-10.

## 6. UI-03 Timeline 계약

### 6.1 조회와 작업

| 종류 | 이름 | 입력 | 결과 |
|---|---|---|---|
| 조회 | `ReadTimeline` | 선택 날짜 의미 | `TimelineViewData` |
| 작업 | `RefreshTimeline` | 선택 날짜 의미 | 새 사건 반영 흐름 시작 결과 |
| 작업 | `CreateActivity` | Activity 종류·이름·TimeRange | 생성 결과 |
| 작업 | `UpdateActivity` | Activity 식별자와 수정 입력 | 수정 결과 |
| 작업 | `ConfirmContext` | Context 식별자와 확인 의미 | 확정 결과 |
| 작업 | `UpdateContext` | Context 식별자와 최종 분류 | 수정 결과 |

### 6.2 표시 데이터

`TimelineViewData`:

- 선택한 현지 날짜와 실제 조회 구간
- 시간순 `TimelineItem` 목록
- 각 항목의 AppSession·Activity·Context 참조
- 시작·종료·기간, 표시 이름, 최종 분류
- 독립 앱 사용 또는 복합 활동 구분
- Context 확인 필요 여부

### 6.3 상태

| 상태 | 조건 | 허용 동작 |
|---|---|---|
| `LOADING` | 날짜 첫 조회 | 대기 |
| `REFRESHING` | 선택 날짜 재조회 | 기존 Timeline 유지 |
| `CONTENT` | 항목 있음 | 날짜 변경, Activity·Context 작업 |
| `EMPTY` | 항목 없음 | Activity 추가, 새로고침, 수집 조건 확인 |
| `CONTENT` + 작업 `BLOCKED(CONTEXT_CONFIRMATION_REQUIRED)` | 특정 MIXED Context | 해당 Context 확인 |
| `CONTENT` + 작업 `BLOCKED(INVALID_TIME_RANGE)` | Activity 입력 구간 오류 | 입력 수정 |
| `RETRYABLE_ERROR` | 새로고침 실패 | 기존 항목 유지, 재시도 |
| `ERROR` | 저장·조회 실패 | 완료 표시 금지 |

명령 성공 뒤 APP-10 Timeline과 관련 홈·리포트 조회를 다시 요청한다.

- 의존: CT-01–CT-04, CT-06.
- 추적: FR-4.1–FR-6.4, US-05–US-09, APP-04, APP-06–APP-07, APP-10.

## 7. UI-04 앱 관리 계약

### 7.1 조회와 작업

| 종류 | 이름 | 입력 | 결과 |
|---|---|---|---|
| 조회 | `ReadApps` | 없음 | `AppManagementViewData` |
| 작업 | `ChangeDefaultClassification` | packageName, AppClassification | 변경 결과 |

### 7.2 표시 데이터와 상태

`AppManagementViewData`는 앱 이름·패키지명·현재 기본 분류 목록을 제공한다.

| 상태 | 조건 | 표시 원칙 |
|---|---|---|
| `LOADING` | 목록 조회 중 | 대기 |
| `CONTENT` | 앱 목록 있음 | 현재 기본 분류와 변경 동작 |
| `EMPTY` | 목록 없음 | 목록 없음 상태 |
| `CONTENT` 내 미분류 표시 | 기본 분류 확인 필요 | 해당 앱 선택 강조 가능 |
| `RETRYABLE_ERROR` | 목록 조회 실패 | 기존 저장 분류 유지 |
| `ERROR` | 분류 저장 실패 | 변경 완료 표시 금지 |

기본 분류 변경은 사용자 확정 Context를 화면에서 덮어쓰지 않는다.

- 의존: CT-01, CT-03–CT-04, CT-06.
- 추적: FR-3, US-04, APP-05, APP-11.

## 8. UI-05 시간 되찾기 계약

### 8.1 조회와 작업

| 종류 | 이름 | 입력 | 결과 |
|---|---|---|---|
| 조회 | `ReadRecoveryEntry` | 없음 | `RecoveryViewData` |
| 작업 | `StartGoalTimer` | Goal 식별자, 시작 요청 | 실행 중 타이머 상태 |
| 작업 | `CompleteGoalTimer` | 실행 중 타이머 식별 | RecoveredTime 생성 결과 |
| 작업 | `CreateManualRecoveredTime` | Goal 식별자, TimeRange | RecoveredTime 생성 결과 |
| 작업 | `SelectRepresentativeGoal` | 겹침 묶음 식별, 대표 Goal 식별자 | 겹침 확정 결과 |

### 8.2 표시 데이터

`RecoveryViewData`:

- 선택 가능한 Goal 목록
- 선택한 Goal
- 타이머 상태: `IDLE`, `RUNNING`, `COMPLETING`
- 직접 기록 입력 상태
- 대표 Goal 확인이 필요한 겹침 정보
- 마지막 명령 결과

### 8.3 상태

| 상태 | 조건 | 허용 동작 |
|---|---|---|
| `EMPTY` | Goal 없음 | UI-06 첫 Goal 생성 이동 |
| `CONTENT` | Goal 있고 타이머 대기 | Goal 선택, 시작, 직접 기록 |
| `CONTENT` | 타이머 실행 중 | 완료 |
| `LOADING` | 완료 또는 저장 중 | 중복 제출 방지 |
| `CONTENT` + 작업 `BLOCKED(REPRESENTATIVE_GOAL_REQUIRED)` | 목표 기록 겹침 | 대표 Goal 선택 |
| `CONTENT` + 작업 `BLOCKED(INVALID_TIME_RANGE)` | 직접 기록 구간 오류 | 입력 수정 |
| `RETRYABLE_ERROR` | 저장 실패 | 안전한 입력 유지, 재시도 |
| `ERROR` | 상태 복구 불가 | 완료로 표시하지 않음 |

- 의존: CT-02–CT-04, CT-06.
- 추적: FR-8, US-17–US-21, APP-09, UI-05.

## 9. UI-06 목표 계약

### 9.1 조회와 작업

| 종류 | 이름 | 입력 | 결과 |
|---|---|---|---|
| 조회 | `ReadGoals` | 없음 | `GoalsViewData` |
| 작업 | `CreateGoal` | 이름, 목표시간 | Goal 생성 결과 |
| 이동 | `OpenGoalDetail` | Goal 식별자 | 선택 Goal 상세 |
| 이동 | `OpenRecoveryForGoal` | Goal 식별자 | UI-05 목표 선택 상태 |

### 9.2 표시 데이터와 상태

`GoalsViewData`는 Goal과 GoalProgress 목록을 제공한다.

| 상태 | 조건 | 표시 원칙 |
|---|---|---|
| `LOADING` | 목록 조회 중 | 대기 |
| `EMPTY` | Goal 없음 | 첫 Goal 생성 동작 |
| `CONTENT` | Goal 있음 | 목표시간·누적시간·진행 결과 |
| `LOADING` | Goal 저장 중 | 중복 제출 방지 |
| `RETRYABLE_ERROR` | 저장·조회 실패 | 입력 또는 기존 목록 유지 |
| `ERROR` | 완료할 수 없는 실패 | 완료 표시 금지 |

- 의존: CT-02–CT-04, CT-06.
- 추적: FR-4.3–FR-4.4, US-16, US-19, APP-09–APP-10.

## 10. UI-07 리포트 계약

### 10.1 조회와 작업

| 종류 | 이름 | 입력 | 결과 |
|---|---|---|---|
| 조회 | `ReadReport` | 일·주·월 기간 의미 | `ReportViewData` |
| 작업 | `ChangeReportPeriod` | 새 기간 의미 | 새 리포트 조회 |
| 이동 | `OpenGoalReportDetail` | Goal 식별자, 선택 기간 | 목표별 상세 |

### 10.2 표시 데이터

`ReportViewData`:

- 선택 기간과 실제 조회 구간
- TimeMetrics
- 목표별 RecoveredTime 집계 결과
- Baseline 비교 가능 상태
- 데이터 수집 여부

### 10.3 상태

| 상태 | 조건 | 표시 원칙 |
|---|---|---|
| `LOADING` | 첫 기간 조회 | 대기 |
| `CONTENT` | 계산된 리포트 있음 | 도메인 결과 표시 |
| `CONTENT` 내 `BASELINE_OBSERVING` | Baseline 미완료 | 비교 불가와 관찰 상태 표시 |
| `EMPTY` | 선택 기간 데이터 없음 | 미수집 상태, 계산된 영과 구분 |
| `REFRESHING` | 기간 변경 | 기존 결과와 새 기간 혼합 금지 |
| `RETRYABLE_ERROR` | 조회 실패 | 기간 선택 유지, 재시도 |
| `ERROR` | 계산 결과 제공 불가 | 임의 값 금지 |

- 의존: CT-02, CT-04, CT-06.
- 추적: FR-9.2–FR-9.5, US-15, APP-08–APP-10.

## 11. UI-08 데이터 관리 계약

### 11.1 조회와 작업

| 종류 | 이름 | 입력 | 결과 |
|---|---|---|---|
| 조회 | `ReadDataManagementState` | 없음 | `DataManagementViewData` |
| 작업 | `ChangeRetentionSelection` | 사용자가 고른 RetentionSelection | 로컬·서버 적용 결과 |
| 작업 | `RequestFullDeletion` | 삭제 요청 | 확인 필요 상태 |
| 작업 | `ConfirmFullDeletion` | 확인된 삭제 요청 | DeletionJob 시작 결과 |
| 조회 | `RefreshDeletionStatus` | DeletionJob 식별자 | 기기·서버 상태 |

### 11.2 표시 데이터

`DataManagementViewData`:

- 익명 식별자 준비 상태
- 백업 변경별 또는 요약 상태
- 오프라인 여부
- 현재 보관 선택과 기기·서버 적용 상태
- DeletionJob의 기기·서버 상태

### 11.3 상태

| 상태 | 조건 | 허용 동작·표시 |
|---|---|---|
| `CONTENT` | 백업 완료 또는 대기 상태 조회 성공 | 상태 확인, 보관 변경, 삭제 요청 |
| `CONTENT` + 확인 대기 | 전체 삭제 확인 전 | 취소 또는 확정 |
| `LOADING` | 보관·삭제 요청 처리 중 | 중복 제출 방지 |
| `RETRYABLE_ERROR(OFFLINE)` | 백업·서버 적용 연결 불가 | 로컬 기능 유지, 재시도 상태 |
| `PARTIAL_FAILURE` | 보관 또는 삭제 일부 경계 실패 | 기기·서버 상태 각각 표시 |
| `CONTENT` 내 삭제 완료 | 기기·서버 모두 완료 | 전체 완료 표시 |
| `ERROR` | 상태 조회·처리 실패 | 전체 완료 표시 금지 |

- 의존: CT-03–CT-06.
- 추적: FR-10, US-22–US-25, APP-02, APP-12–APP-13, SRV-01–SRV-03.

## 12. CT-06 고정 화면 결과

`FakeFeatureGateway`는 아래 결과를 화면 구현에 제공할 수 있어야 한다. 각 결과는 실제 기능 경계와 같은 CT-04 형태를 사용한다.

| 고정 결과 ID | 대상 | 결과 상태 | 공통 확인점 |
|---|---|---|---|
| `FX-UI01-PERMISSION-REQUIRED` | UI-01 | `BLOCKED` | 설정 열기·재확인만 제공 |
| `FX-UI01-READY` | UI-01 | `CONTENT` | 주요 화면 진입 가능 |
| `FX-UI02-BASELINE-OBSERVING` | UI-02 | `CONTENT` 내 관찰 상태 | Baseline 기반 값 숨김 |
| `FX-UI02-CONTENT` | UI-02 | `CONTENT` | 모든 값은 CT-02 결과 사용 |
| `FX-UI03-EMPTY` | UI-03 | `EMPTY` | 수집 조건과 Activity 추가 제공 |
| `FX-UI03-MIXED` | UI-03 | `CONTENT` + 확인 필요 항목 | 해당 Context에만 확인 동작 |
| `FX-UI04-READ-FAILURE` | UI-04 | `RETRYABLE_ERROR` | 기존 분류 훼손 금지 |
| `FX-UI05-NO-GOAL` | UI-05 | `EMPTY` | UI-06 생성 이동 |
| `FX-UI05-GOAL-OVERLAP` | UI-05 | `CONTENT` + 작업 `BLOCKED` | 대표 Goal 선택 |
| `FX-UI06-CONTENT` | UI-06 | `CONTENT` | GoalProgress 표시 |
| `FX-UI07-NO-DATA` | UI-07 | `EMPTY` | 계산된 영과 미수집 구분 |
| `FX-UI07-REPORT` | UI-07 | `CONTENT` | 선택 기간 결과만 표시 |
| `FX-UI08-BACKUP-PENDING` | UI-08 | `CONTENT` | 로컬 기능 성공과 백업 대기 구분 |
| `FX-UI08-OFFLINE` | UI-08 | `RETRYABLE_ERROR` | 대기 유지 |
| `FX-UI08-DELETE-PARTIAL` | UI-08 | `PARTIAL_FAILURE` | 전체 완료 표시 금지 |
| `FX-UI08-DELETE-COMPLETE` | UI-08 | `CONTENT` | 양쪽 완료 표시 |

고정 결과의 구체 값은 승인된 요구사항과 스토리 인수 사례만 사용한다.

## 13. 공통 상태 전이 원칙

### 13.1 조회

```text
INITIAL → LOADING
  ├─ SUCCESS(content) → CONTENT
  ├─ EMPTY → EMPTY
  ├─ BLOCKED → BLOCKED
  ├─ RETRYABLE_FAILURE → RETRYABLE_ERROR
  └─ FAILURE → ERROR

CONTENT·EMPTY·BLOCKED → REFRESHING
  └─ 결과에 따라 새 상태, 기존 내용은 성공 전까지 안전하게 유지 가능
```

### 13.2 명령

```text
입력 상태 → 명령 처리 중
  ├─ SUCCESS → 영향 조회 재실행
  ├─ BLOCKED → 필요한 확인 상태
  ├─ RETRYABLE_FAILURE → 입력 유지·재시도
  ├─ PARTIAL_FAILURE → 경계별 상태 표시
  └─ FAILURE → 완료 표시 없이 오류
```

구현의 비동기 처리 방식은 여기서 정하지 않는다.

## 14. 화면 간 영향 재조회

| 성공한 작업 | 다시 읽을 최소 화면 데이터 |
|---|---|
| 앱 기본 분류 변경 | UI-04, 영향받는 UI-03·UI-02·UI-07 결과 |
| Activity 생성·수정 | UI-03, UI-02, UI-07 |
| Context 확인·수정 | UI-03, UI-02, UI-07, 관련 UI-05·UI-06 지표 |
| Goal 생성 | UI-05, UI-06, UI-02 |
| RecoveredTime 생성·대표 Goal 선택 | UI-05, UI-06, UI-02, UI-07 |
| 보관 선택 변경 | UI-08 |
| 전체 삭제 | UI-08; 완료 뒤 다른 화면은 삭제된 기준 데이터 재조회 |

구체적인 캐시 무효화와 화면 생명주기는 트랙 상세 설계에서 정한다.

## 15. CT-04 추적 요약

| 화면 | 스토리 | 제공 구성 요소 | 참조 계약 |
|---|---|---|---|
| UI-01 | US-01, US-22 | APP-01–APP-02 | CT-04–CT-06 |
| UI-02 | US-10–US-14 | APP-08–APP-10 | CT-02, CT-04, CT-06 |
| UI-03 | US-05–US-09 | APP-04, APP-06–APP-07, APP-10 | CT-01–CT-04, CT-06 |
| UI-04 | US-04 | APP-05 | CT-01, CT-03–CT-04, CT-06 |
| UI-05 | US-17–US-21 | APP-09 | CT-02–CT-04, CT-06 |
| UI-06 | US-16, US-19 | APP-09–APP-10 | CT-02–CT-04, CT-06 |
| UI-07 | US-15 | APP-08–APP-10 | CT-02, CT-04, CT-06 |
| UI-08 | US-23–US-25 | APP-12–APP-13 | CT-03–CT-06 |

## 16. UI 트랙 병렬 시작 준비

| 필요한 공통 입력 | 기준 위치 | 준비 상태 |
|---|---|---|
| 논리 엔터티·지표 | `domain-entities.md` | 준비됨 |
| 공통 업무 규칙 | `business-rules.md` | 준비됨 |
| 저장·백업 흐름 | `business-logic-model.md` | 준비됨 |
| UI-01–UI-08 작업·상태 | 이 문서 §4–§11 | 준비됨 |
| 고정 기능 결과 | 이 문서 §12 | 준비됨 |

이 판정은 UI 상세 작업의 시작 승인이 아니다. 사용자가 지정한 중지 경계에 따라 실제 병렬 작업은 시작하지 않는다.

## 17. 이번 범위에서 정하지 않은 것

- 화면별 실제 레이아웃, 색상, 문구, 접근성 구현, 애니메이션
- 화면·상태 관리 프레임워크와 네비게이션 구현
- 네트워크·저장 호출 코드와 오류 예외 변환
- 비동기 실행, 캐시, 성능 최적화 방식
- 트랙 내부 파일·컴포넌트 구조와 테스트 코드
