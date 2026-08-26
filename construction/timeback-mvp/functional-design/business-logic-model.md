# `timeback-mvp` 공통 업무 흐름과 계약

## 1. 문서 상태

- 단계: CONSTRUCTION STEP 01 기능 설계
- 범위: CP-0 공통작업
- 상태: 게이트 2 승인 전 후보
- 공식 작업 단위: `timeback-mvp` 하나
- 다음에 남는 작업: 네 책임 트랙의 상세 기능 설계

이 문서는 네 책임 트랙이 병렬 작업 전에 함께 사용할 흐름, CT-03 저장 계약, CT-05 백업·삭제 계약, CT-06 테스트 대역 계약의 기준이다. 트랙 내부 알고리즘, 기술 제품, API 경로, 저장 스키마, 코드는 정의하지 않는다.

## 2. 분야별 기준 정의 위치

| 계약 | 기준 정의 파일 | 이 문서의 역할 |
|---|---|---|
| CT-01 시간 구간·Context | `domain-entities.md` | 흐름에서 입력·출력으로 사용 |
| CT-02 Baseline·목표·시간 지표 | `domain-entities.md` | 흐름에서 조회 결과로 사용 |
| CT-03 저장·조회·변경 통지 | 이 문서 | 명령·조회·성공·실패 흐름 정의 |
| CT-04 사용자 작업·화면 상태 | `frontend-components.md` | 화면 작업이 호출하는 흐름 연결 |
| CT-05 익명 백업·보관·삭제 | 이 문서 | 기기·서버 사이 논리 동작 정의 |
| CT-06 테스트 대역 | 이 문서 | 모든 트랙이 교체 가능한 대역 정의 |
| 공통 불변조건 | `business-rules.md` | 이 문서의 흐름이 지켜야 할 규칙 참조 |

같은 이름이 여러 문서에 보이면 위 기준 파일의 정의를 적용한다.

## 3. 공통 호출 원칙

### 3.1 명령과 조회

- 명령은 업무 상태를 바꾼다. 성공 결과에는 변경된 대상과 후속 재조회가 필요한 범위를 식별할 수 있는 정보가 포함된다.
- 조회는 업무 상태를 바꾸지 않는다. 조회 결과는 `content`, `empty`, `blocked`, `failure`를 구분한다.
- 화면은 명령 성공 뒤 계산을 직접 수행하지 않고 APP-10 조회를 다시 요청한다.
- 값이 계산되지 않은 상태와 계산된 값 없음 또는 계산 결과가 영인 상태를 구분한다.

### 3.2 공통 결과 종류

논리 결과의 필드와 값 상태는 `domain-entities.md`의 `OperationResult`와 `QueryResult`를 사용한다.

| 결과 | 의미 | 소비자 처리 |
|---|---|---|
| `SUCCESS` | 요청한 명령 또는 조회가 정상 완료됨 | 반환된 결과를 사용하거나 다시 조회 |
| `EMPTY` | 조회는 성공했지만 해당 범위에 데이터가 없음 | 빈 상태 표시, 기존 데이터 삭제로 해석하지 않음 |
| `BLOCKED` | 권한·확인·선행 상태가 충족되지 않음 | 필요한 사용자 동작 또는 상태를 표시 |
| `RETRYABLE_FAILURE` | 동일 의미의 요청을 다시 시도할 수 있음 | 기존 로컬 성공을 유지하고 재시도 상태 표시 |
| `PARTIAL_FAILURE` | 여러 경계 중 일부만 성공함 | 성공·실패 대상을 각각 유지 |
| `FAILURE` | 자동 재시도로 완료를 가정할 수 없음 | 오류를 표시하고 완료 상태로 바꾸지 않음 |

오류 이름은 구현 예외명이 아니라 협업용 논리 분류다.

## 4. CP-0 공통 흐름

### FL-01. 권한 확인과 익명 사용자 준비

```text
UI-01 권한 상태 조회
  → APP-01 → OS-01 현재 상태
  ├─ 미허용: BLOCKED(PERMISSION_REQUIRED), 주요 화면 접근 제한
  └─ 허용: APP-02 → OS-04 식별원 읽기·기기 내 변환
       ├─ 성공: User 저장 요청 → CT-03
       └─ 실패: BLOCKED(IDENTITY_UNAVAILABLE)
```

- 원본 하드웨어 식별원은 APP-02 경계를 벗어나지 않는다.
- 권한 상태와 식별자 준비 상태는 서로 다른 상태다.
- 추적: FR-1.1–FR-1.2, FR-10.1, US-01, US-22, APP-01, APP-02, OS-01, OS-04, UI-01.

### FL-02. 사용 이벤트부터 Timeline·홈까지

```text
새로고침 또는 앱 갱신
  → APP-01 권한 재확인
  → APP-03 UsageEvent 증분 수집
  → CT-03 원본 이벤트 저장
  → APP-04 AppSession 재구성
  → CT-03 세션 저장
  → APP-07 Context 생성·갱신
  → CT-03 Context 저장
  → APP-08 시간 지표 계산
  → APP-10 Timeline·홈 조회 조립
  → CT-04 화면 결과
```

- 수집할 새 사건이 없으면 `EMPTY` 또는 최신 상태를 반환하고 기존 데이터를 삭제하지 않는다.
- 백업 상태는 이 흐름의 로컬 성공 여부를 막지 않는다.
- 추적: FR-1–FR-3, FR-5–FR-7, FR-9.1, US-01–US-04, US-06, US-08, US-10, US-14.

### FL-03. Activity·Context 수정과 연쇄 재조회

```text
UI-03 Activity 생성·수정 또는 Context 확인·수정
  → APP-06 또는 APP-07 명령
  → CT-03 기준 데이터 저장
  → APP-07 영향 구간 판정
  → APP-08 영향 기간 지표 재계산
  → APP-09 영향 기간 회수 지표 재계산
  → APP-10 Timeline·홈·리포트 재조회
  → CT-04 갱신 결과
```

- 수정 성공은 화면 한 곳의 표시만 바꾸는 결과가 아니다.
- 영향 결과는 같은 최종 Context에서 재현한다.
- 추적: FR-4.1–FR-4.2, FR-5, FR-6, NFR-2.3, US-05–US-10.

### FL-04. Baseline과 리포트

```text
APP-08 최종 Context 기간 조회
  → Baseline 상태 판정
  ├─ 관찰 중: CT-02 OBSERVING 결과
  ├─ 최초 확정: CT-03 Baseline 저장
  └─ 재산정 후보: 사용자 승인 전 기존 Baseline 유지
       → 승인 후 CT-03 새 Baseline 저장
  → APP-10 기간별 조회 조립
  → CT-04 UI-02·UI-07 상태
```

- 정확한 관찰 기간과 화면 응답 기준은 승인된 요구사항을 참조한다.
- 관찰 중에는 확보한 시간과 회수율을 값으로 꾸며 반환하지 않는다.
- 추적: FR-7.2–FR-7.5, FR-9, US-11–US-15, APP-08, APP-10, UI-02, UI-07.

### FL-05. 목표와 시간 회수

```text
UI-05·UI-06 목표 또는 기록 명령
  → APP-09 Goal·RecoveredTime 처리
  → CT-03 기준 데이터 저장
  → 겹침 발견 시 BLOCKED(REPRESENTATIVE_GOAL_REQUIRED)
       → 사용자 대표 Goal 선택
       → APP-09 누적·회수 지표 재계산
  → APP-10 목표·홈·리포트 조회
  → CT-04 화면 결과
```

- 타이머와 직접 입력은 같은 RecoveredTime 계약으로 합류한다.
- 겹친 실제 경과 구간은 대표 목표 확인 뒤 한 번만 반영한다.
- 추적: FR-4.3–FR-4.4, FR-8, US-16–US-21, APP-09, APP-10, UI-05–UI-07.

### FL-06. 로컬 변경과 익명 백업

```text
CT-03 로컬 변경 성공
  → CommittedChange 제공
  → APP-12 BackupChange 기록
  → CT-05 SubmitBackupChanges
     ├─ 전부 성공: 수락된 변경 완료
     ├─ 일부 성공: 성공만 완료, 실패는 대기
     └─ 오프라인·재시도 가능 실패: 전부 또는 해당 변경 대기
  → UI-08 백업 상태 조회
```

- CT-05 실패는 CT-03 성공을 되돌리지 않는다.
- 앱 시작 시 APP-12는 대기 변경을 다시 읽어 같은 `changeId`로 재시도한다.
- 추적: FR-10.2, US-23, APP-11, APP-12, SRV-01–SRV-02, UI-08.

### FL-07. 보관 선택과 전체 삭제

```text
UI-08 보관 선택 변경
  → APP-13 로컬 선택 저장
  → CT-05 ApplyRetentionSelection
  → 로컬 상태와 서버 적용 상태를 각각 표시

UI-08 전체 삭제 확정
  → APP-13 DeletionJob 생성
  ├─ CT-03 기기 데이터 삭제
  └─ CT-05 RequestFullDeletion
       → ReadDeletionStatus
       → 기기·서버 모두 완료일 때만 전체 완료
```

- 부분 실패는 `PARTIAL_FAILURE`로 유지한다.
- 삭제 재시도는 기존 `DeletionJob`을 이어간다.
- 추적: FR-10.3–FR-10.4, US-24–US-25, APP-13, SRV-03, UI-08.

## 5. CT-03 — 저장·조회·변경 통지 계약

### 5.1 목적과 책임

| 항목 | 내용 |
|---|---|
| 목적 | 저장 제품 없이 APP-03–APP-09와 APP-12가 같은 기기 기준 데이터 계약으로 작업하게 함 |
| 기준 정의 위치 | 이 문서 §5, 데이터 형태는 `domain-entities.md` §7 참조 |
| 제공자 | `track-device-data`의 APP-11 |
| 소비자 | `track-domain-engine`의 APP-05–APP-09, `track-backup-server`의 APP-12–APP-13 |
| 변경 영향 | CT-01, CT-02, CT-05, CT-06 및 UI 재조회 범위 |

### 5.2 논리 명령·조회

| 동작 | 종류 | 입력 | 결과 |
|---|---|---|---|
| `SaveRecord` | 명령 | 엔터티 종류, 논리 레코드 | `CommitResult`와 `CommittedChange` |
| `SaveRecords` | 명령 | 같은 업무 동작으로 저장할 레코드 묶음 | 전체 성공 또는 성공하지 않은 묶음 결과 |
| `ReadRecord` | 조회 | 엔터티 종류, 식별자 | 레코드, `EMPTY`, `FAILURE` |
| `ReadPeriod` | 조회 | 엔터티 종류, `PeriodQuery` | 기간 안의 레코드 목록, `EMPTY`, `FAILURE` |
| `DeleteScope` | 명령 | 사용자·엔터티·기간으로 표현한 승인된 삭제 범위 | `CommitResult`와 삭제 변경 목록 |
| `ReadCommittedChanges` | 조회 | 마지막으로 소비한 변경 위치 | 이후 `CommittedChange` 목록 |

`SaveRecords`의 묶음 경계는 한 사용자 동작의 일관성을 지켜야 할 때만 사용한다. 어떤 레코드를 한 묶음으로 저장할지는 트랙 상세 설계에서 업무 규칙을 근거로 정한다.

### 5.3 성공·실패와 불변조건

- `SUCCESS`는 기기 기준 데이터의 저장이 완료됐다는 뜻이다.
- 저장 실패에서는 `CommittedChange`를 성공으로 제공하지 않는다.
- 저장 성공 뒤의 `CommittedChange`는 엔터티 종류, 식별자, 변경 종류, 발생 시각을 구분할 수 있어야 한다.
- 백업 전달 실패는 이미 성공한 로컬 저장을 실패로 바꾸지 않는다.
- `ReadPeriod`의 기간은 CT-01과 같은 시작 포함·종료 미포함 구간을 사용한다.
- 조회 `EMPTY`는 저장 데이터 삭제나 오류를 뜻하지 않는다.
- 실제 저장 제품의 트랜잭션, 인덱스, 직렬화 방식은 여기서 정하지 않는다.

### 5.4 CT-03 테스트 대역

`FakeDeviceDataAuthority`는 다음을 제어할 수 있어야 한다.

- 엔터티별 초기 레코드
- 저장 성공·실패
- 기간 조회 결과와 빈 결과
- 저장 뒤 제공되는 `CommittedChange`
- 삭제 전체 성공·부분 실패·실패

같은 CT-03 계약 검증을 실제 APP-11 경계에도 적용해야 한다.

## 6. CT-05 — 익명 백업·보관·삭제 계약

### 6.1 목적과 책임

| 항목 | 내용 |
|---|---|
| 목적 | 앱과 서버가 도메인 계산을 공유하지 않고 익명 백업 사본의 변경·상태·삭제만 주고받게 함 |
| 기준 정의 위치 | 이 문서 §6, 데이터 형태는 `domain-entities.md` §8 참조 |
| 제공자 | 앱 측 APP-02·APP-12·APP-13과 서버 측 SRV-01–SRV-03 |
| 소비자 | `track-device-data`, `track-backup-server`, `track-ui` |
| 변경 영향 | CT-03 변경 통지, CT-04 UI-08, CT-06 가짜 서버 응답 |

### 6.2 논리 동작

| 동작 | 입력 | 결과 | 완료 의미 |
|---|---|---|---|
| `SubmitBackupChanges` | 변환된 익명 식별자, `BackupChange` 묶음, 대상 레코드 사본 | 변경별 `BackupItemResult` | 수락된 변경만 서버 백업 완료 |
| `ReadBackupStatus` | 변환된 익명 식별자, 변경 식별자 목록 | 변경별 백업 상태 | APP-12의 대기·완료 판단 자료 |
| `ApplyRetentionSelection` | 변환된 익명 식별자, 사용자가 승인한 선택 | 서버 적용 상태 | 서버 사본에 같은 사용자 선택 적용 |
| `RequestFullDeletion` | 변환된 익명 식별자, 삭제 작업 식별자 | 요청 수락 또는 실패 | 삭제 작업 시작이며 전체 완료가 아님 |
| `ReadDeletionStatus` | 변환된 익명 식별자, 삭제 작업 식별자 | 서버 삭제 상태 | 서버 사본의 진행·완료·실패만 의미 |

### 6.3 성공·실패와 불변조건

- 같은 `changeId` 재전송은 같은 논리 변경으로 식별한다.
- 묶음 일부가 성공하면 성공한 항목만 완료 처리한다.
- 오프라인과 재시도 가능 실패는 APP-12의 대기 상태를 유지한다.
- 서버는 Context, Baseline, 목표 중첩, 확보·되찾은 시간, 회수율을 다시 계산하지 않는다.
- 변환 전 하드웨어 값과 로그인 계정은 CT-05 입력에 포함하지 않는다.
- 서버 삭제 완료는 기기 삭제 완료를 대신하지 않는다.
- 보관 선택과 전체 삭제는 원본 및 승인된 파생 데이터 범위에 일관되게 적용한다.

### 6.4 CT-05 테스트 대역

`FakeAnonymousBackupBoundary`는 다음 응답을 선택적으로 제공할 수 있어야 한다.

- 전부 수락
- 일부 수락과 일부 재시도 가능 실패
- 오프라인
- 보관 선택 적용 성공·실패
- 삭제 진행·완료·실패

가짜 경계도 원본 하드웨어 값이나 사용자 계정을 요구하지 않는다.

## 7. CT-06 — 공통 테스트 대역 계약

### 7.1 목적과 책임

| 항목 | 내용 |
|---|---|
| 목적 | 실제 기기·저장소·도메인·서버·화면 구현이 없어도 각 트랙이 같은 계약으로 검증을 시작하게 함 |
| 기준 정의 위치 | 이 문서 §7, UI 고정 결과는 `frontend-components.md` §12 참조 |
| 제공자·소비자 | 네 책임 트랙 모두 |
| 입력·출력 | 고정 사건·시각·레코드·서버·기능 결과를 입력하고, 실제 경계와 같은 논리 결과·호출 기록을 관찰 |
| 성공·실패 | CT-01–CT-05의 성공·빈 결과·차단·재시도·부분 실패·실패를 선택적으로 재현 |
| 불변조건 | 대역과 실제 구현이 같은 계약 이름·필드·상태·검증 기대를 사용 |
| 테스트 대역 | §7.2의 일곱 대역과 §7.3 공통 시나리오 |
| 추적 | NFR-3.1–NFR-3.3, US-01–US-25 인수 조건 |
| 변경 영향 | CT-01–CT-05, 관련 스토리 인수 사례와 계약 검증 |

### 7.2 교체 가능한 대역

| 대역 | 제어 입력 | 관찰 결과 | 주 사용 트랙 |
|---|---|---|---|
| `FakeUsageEventSource` | 고정 UsageEvent와 조회 기간 | 요청 기간, 반환 사건 | 기기 데이터·도메인 |
| `FakeScreenStateSource` | 화면 종료 사건 | 열린 세션 종료 근거 | 기기 데이터 |
| `ControlledTimeSource` | 현재 시각, 현지 시간대, 기간 경계 | 요청된 시각과 경계 | 기기 데이터·도메인 |
| `FakeDeviceIdentitySource` | 변환 가능한 식별원 또는 실패 | 원본 노출 없이 변환 경계 호출 | 백업 서버·기기 데이터 |
| `FakeDeviceDataAuthority` | 초기 레코드와 성공·실패 | 저장·조회·삭제·변경 통지 | 모든 트랙 |
| `FakeAnonymousBackupBoundary` | 백업·보관·삭제 응답 | 전송 요청과 상태 전이 | 백업 서버·UI |
| `FakeFeatureGateway` | CT-04 고정 조회·명령 결과 | UI 작업과 재조회 요청 | UI |

### 7.3 공통 시나리오 묶음

| 시나리오 | 고정 입력 | 확인할 공통 결과 | 관련 계약 |
|---|---|---|---|
| `SC-PERMISSION-BLOCKED` | 미허용 권한 | 수집 미호출, 주요 화면 차단 | CT-04, CT-06 |
| `SC-SESSION-PAIRED` | 짝이 있는 사건 | 한 AppSession과 근거 사건 연결 | CT-01, CT-03, CT-06 |
| `SC-SESSION-OPEN` | 종료 짝 없는 사건과 종료 근거 | 보정된 종료 시각의 세션 | CT-01, CT-06 |
| `SC-MIDNIGHT-SPLIT` | 날짜 경계를 지나는 구간 | 분할 구간 합계 보존 | CT-01, CT-06 |
| `SC-CONTEXT-CONFLICT` | 충돌하는 활동·앱 분류 | 확인 전 MIXED | CT-01, CT-04, CT-06 |
| `SC-CONTEXT-CORRECTED` | 사용자 Context 수정 | 관련 조회 결과 재계산 | CT-01–CT-04, CT-06 |
| `SC-BASELINE-OBSERVING` | 관찰 미완료 상태 | 확보시간·회수율 미제공 | CT-02, CT-04, CT-06 |
| `SC-GOAL-OVERLAP` | 겹친 RecoveredTime | 대표 목표 확인과 한 번 누적 | CT-02, CT-04, CT-06 |
| `SC-BACKUP-PARTIAL` | 변경별 서로 다른 응답 | 성공만 완료, 나머지 대기 | CT-03, CT-05, CT-06 |
| `SC-DELETE-PARTIAL` | 기기 또는 서버만 완료 | 전체 삭제 미완료 | CT-03–CT-06 |

### 7.3.1 공통 고정 입력

네 트랙은 실제 시각값 대신 아래 기호와 순서를 먼저 공유한다. 구현 테스트는 기호를 실제 시각으로 바꾸되 관계를 바꾸지 않는다.

```text
DAY_A_START
  < SESSION_START
  < ACTIVITY_START
  < OVERLAP_START
  < OVERLAP_END
  < DAY_B_START
  < SESSION_END

GOAL_A_START
  < GOAL_B_START
  < GOAL_A_END
  < GOAL_B_END
```

| 고정 ID | 데이터 | 사용 시나리오 |
|---|---|---|
| `USER-CP0` | 변환된 가짜 익명 식별자 사용자 | 모든 저장·백업 시나리오 |
| `APP-PRODUCTIVE` | 기본 분류 `PRODUCTIVE`인 앱 | 충돌 없는 Context |
| `APP-WASTE` | 기본 분류 `WASTE`인 앱 | 충돌 Context |
| `EVENT-PAIR-START`, `EVENT-PAIR-END` | 같은 앱의 짝이 되는 Foreground·Background 사건 | `SC-SESSION-PAIRED` |
| `EVENT-OPEN`, `EVENT-NEXT-APP` | 종료 짝 없는 사건과 다음 앱 전환 근거 | `SC-SESSION-OPEN` |
| `ACTIVITY-STUDY` | `STUDY`, `ACTIVITY_START`부터 경계 뒤까지 이어지는 활동 | 자정 분할·Context 겹침 |
| `CONTEXT-MIXED` | `APP-WASTE`와 `ACTIVITY-STUDY`가 겹친 미확정 Context | `SC-CONTEXT-CONFLICT` |
| `GOAL-A`, `GOAL-B` | 서로 다른 목표 | `SC-GOAL-OVERLAP` |
| `RECOVERED-A`, `RECOVERED-B` | 위 기호 순서로 일부가 겹치는 회수 기록 | `SC-GOAL-OVERLAP` |
| `CHANGE-ACCEPT`, `CHANGE-RETRY` | 같은 사용자의 서로 다른 BackupChange | `SC-BACKUP-PARTIAL` |
| `DELETE-CP0` | 한 경계만 완료된 DeletionJob | `SC-DELETE-PARTIAL` |

- `SC-MIDNIGHT-SPLIT`은 `DAY_B_START`에서 구간을 나누고 두 구간의 합이 원래 구간과 같음을 확인한다.
- `SC-CONTEXT-CONFLICT`는 사용자 확인 전 `CONTEXT-MIXED`를 유지하고 확인 뒤 관련 조회 재계산을 관찰한다.
- `SC-GOAL-OVERLAP`은 `OVERLAP` 관계가 아니라 `GOAL_A_START`–`GOAL_B_END` 관계를 사용해 대표 Goal 선택을 확인한다.
- `SC-BACKUP-PARTIAL`은 `CHANGE-ACCEPT`만 완료하고 `CHANGE-RETRY`는 같은 식별자로 대기한다.

고정 ID와 기호는 테스트 협업용이며 제품 데이터 값이나 새로운 시간 임계값이 아니다.

### 7.4 대역과 실제 구현의 동등성

- 같은 입력에 대해 같은 논리 결과 종류와 상태 이름을 사용한다.
- 대역 전용 성공 경로를 제품 동작으로 가정하지 않는다.
- 실제 경계에서만 확인 가능한 기기·네트워크 품질은 후속 단계의 검증 위험으로 남긴다.
- 계약 검증은 대역과 실제 구현에 같은 입력·예상 결과를 적용할 수 있어야 한다.

## 8. 병렬 시작 준비 판정

이 표는 공통 계약의 사용 준비만 뜻하며 각 트랙의 상세 작업이 시작됐다는 의미가 아니다.

| 책임 트랙 | 사용할 공통 입력 | 제공해야 할 결과 | 대역 | CP-0 판정 |
|---|---|---|---|---|
| `track-device-data` | CT-01 사건·구간, CT-03 저장 결과 | AppSession, CommittedChange | 사건·화면 종료·시간·저장 대역 | 계약 준비됨 |
| `track-domain-engine` | CT-01 세션·활동, CT-03 조회, 제어 시간 | Context와 CT-02 지표 | 세션·활동·저장·시간 대역 | 계약 준비됨 |
| `track-ui` | CT-04 조회·명령 결과, CT-01·CT-02 참조 값 | 사용자 작업과 상태 표현 | 기능 경계·고정 화면 결과 | 계약 준비됨 |
| `track-backup-server` | CT-03 CommittedChange, CT-05 익명 경계 | BackupChange·DeletionJob 상태 | 식별원·저장·서버 대역 | 계약 준비됨, 실제 식별원 위험은 STEP 02 검증 |

## 9. 계약 변경 절차

1. 변경하려는 CT ID와 기준 파일을 표시한다.
2. 변경 이유를 승인된 FR/NFR 또는 US에 연결한다.
3. 영향받는 제공자·소비자 트랙과 다른 CT를 기록한다.
4. 관련 CT-06 시나리오와 UI 상태의 예상 결과를 함께 수정한다.
5. 네 트랙의 기준 용어가 일치하는지 검토한 뒤 현재 활성 게이트의 승인을 받는다.

공통 계약은 트랙 하나가 조용히 바꾸지 않는다.

## 10. CT 추적 요약

| 계약 | 주요 요구사항 | 스토리 | 구성 요소 |
|---|---|---|---|
| CT-01 | FR-1–FR-6, NFR-2.1–NFR-2.2, NFR-5 | US-02–US-10 | OS-02–OS-03, OS-05, APP-03–APP-07, APP-10–APP-11 |
| CT-02 | FR-4.3–FR-4.4, FR-7–FR-9, NFR-2.3–NFR-2.4 | US-10–US-21 | OS-05, APP-08–APP-11, UI-02, UI-05–UI-07 |
| CT-03 | FR-1.4, FR-10.4–FR-10.5, NFR-3.2, NFR-5 | US-02–US-05, US-09, US-12, US-16–US-19, US-22–US-25 | APP-03–APP-13 |
| CT-04 | FR-1.1–FR-1.2, FR-3–FR-10, NFR-1.1, NFR-3.2 | US-01, US-04–US-25 | APP-01, APP-05–APP-10, APP-12–APP-13, UI-01–UI-08 |
| CT-05 | FR-10, NFR-4.2–NFR-4.3 | US-22–US-25 | OS-04, APP-02, APP-12–APP-13, SRV-01–SRV-03, UI-08 |
| CT-06 | NFR-3.1–NFR-3.3 | US-01–US-25 인수 검증 | 모든 경계 구성 요소 |

## 11. 이번 범위에서 하지 않은 것

- 네 책임 트랙 내부 처리 알고리즘과 파일 구조
- 특정 언어·프레임워크·런타임·저장·통신·배포 제품
- 실제 네트워크 주소, API 경로, 직렬화 형식, 저장 스키마
- 화면 스타일, 위젯 구조, 서버·앱 구현 코드
- 실제 병렬 작업과 CP-1 이후 통합

따라서 CP-0 승인 뒤에도 CONSTRUCTION STEP 01 전체는 `진행 중`이다.
