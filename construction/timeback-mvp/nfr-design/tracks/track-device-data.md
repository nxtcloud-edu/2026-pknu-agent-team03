# `timeback-mvp` track-device-data 비기능 설계

## 1. 상태

- 단계: CONSTRUCTION STEP 03
- 상태: 트랙 논리 설계 완료
- 입력: STEP 01 device-data 상세와 STEP 02 품질·기술 결정

## 2. 논리 구성

```text
UI/새로고침
    │
    ▼
AccessGate ───────▶ UsageAccessGateway(OS-01)
    │ 허용
    ▼
UsageCollector ───▶ UsageEventSource(OS-02)
    │                    │
    │ 원본+체크포인트     ▼
    └──────────────▶ DeviceDataAuthority(APP-11)
                          ▲
SessionReconstructor ─────┤
    │          │           │ 기간 조회·세션 원자 교체
    │          ├──────────▶ ScreenStateSource(OS-03)
    └─────────────────────▶ TimeSource(OS-05)

DeviceDataAuthority ── CommittedChangePage(CT-03) ──▶ 백업 어댑터
DeviceDataAuthority ── UsageEvent/AppSession ───────▶ 도메인 어댑터
```

업무 규칙은 Android·Room 클래스에 의존하지 않는다. 실제 OS/저장 구현과 CT-06 가짜 구현은 같은 포트를 구현한다.

## 3. 품질 패턴

### 3.1 권한과 개인정보

- `AccessGate`는 매 보호 작업마다 OS-01의 현재 값을 읽는다.
- 권한이 `GRANTED`가 아니면 수집 소스와 저장소를 호출하지 않는 fail-closed 방식이다.
- OS-02 모델에는 승인된 세 필드 외 콘텐츠를 표현할 속성이 없게 하여 금지 데이터를 구조적으로 차단한다.

### 3.2 증분 수집과 멱등성

- 마지막 성공 체크포인트와 요청 구간으로 유효 구간을 만들고 경계 1초를 겹쳐 다시 읽는다.
- 겹친 사건은 안정 ID의 고유 제약으로 제거한다.
- 사건과 새 체크포인트는 한 저장 트랜잭션에서 확정한다. 소스·저장 실패 시 체크포인트를 진행하지 않는다.
- 같은 사용자 범위의 수집 작업은 mutex/WorkManager unique work로 직렬화한다.

### 3.3 세션 재구성과 원자 교체

- 사건은 `(occurredAt, sourceOrder)`로 정렬하고 같은 시각에는 UsageEvent를 화면 종료보다 먼저 처리한다.
- 정상 background, 다음 다른 앱 foreground, 화면 종료 순으로 관찰되는 가장 빠른 유효 근거에서 열린 세션을 닫는다.
- 근거가 없으면 완성 세션이 아니라 `OpenSessionCandidate`를 저장한다.
- OS-05가 제공하는 모든 현지 자정 경계에서 닫힌 구간을 분할한다.
- 영향 구간의 세션과 열린 후보는 `ReplacePeriodRecords` 한 트랜잭션으로 교체한다.

### 3.4 저장·조회·변경 통지

- Room 실제 구현은 레코드 테이블, `collection_checkpoint`, `open_session_candidate`, `committed_change`를 분리한다.
- 모든 주 테이블의 첫 인덱스 열은 `owner_id`; 기간 레코드는 `(owner_id, entity_type, start_at, end_at)`를 사용한다.
- `CommittedChange.sequence`는 사용자별 단조 증가 위치이며 `changeId`는 재조회에도 안정적이다.
- 소비자는 처리 성공 뒤에만 커서를 보관한다. 빈 페이지는 입력 커서를 그대로 반환한다.
- 원본 UsageEvent는 upsert만 허용하고 기간 교체 대상에서 제외한다.

## 4. 오류 분류

| 경계 | 분류 | 행동 |
|---|---|---|
| 권한 미허용 | `BLOCKED(PERMISSION_REQUIRED)` | 수집·저장 미호출 |
| 권한 조회 실패 | `FAILURE(ACCESS_UNAVAILABLE)` | 과거 허용값 사용 금지 |
| UsageStats 조회 일시 실패 | `RETRYABLE_FAILURE(SOURCE_UNAVAILABLE)` | 체크포인트 유지 |
| 저장 트랜잭션 실패 | `RETRYABLE_FAILURE(STORAGE_UNAVAILABLE)` | 원본·체크포인트·세션 모두 이전 상태 |
| 사용자 범위 불일치 | `FAILURE(OWNER_SCOPE_VIOLATION)` | 데이터 미노출, 재시도 금지 |
| 잘못된 구간 | `FAILURE(INVALID_RANGE)` | OS·저장 호출 전 거부 |

## 5. 테스트 설계

| 층 | 대역 | 핵심 사례 |
|---|---|---|
| AccessGate | FakeUsageAccessGateway, ControlledTimeSource | 허용·미허용·조회 실패·설정 복귀 |
| UsageCollector | FakeUsageEventSource, InMemoryDeviceDataAuthority | 허용 필드, 중복, 빈 결과, 소스·저장 실패 |
| SessionReconstructor | FakeScreenStateSource, ControlledTimeSource | 정상 쌍, 다음 앱, 화면 종료, 열린 후보, 자정·DST 분할 |
| APP-11 계약 | 실제 Room과 메모리 구현에 같은 suite | 원자성, 사용자 격리, 커서, 기간 교체, 삭제 |
| Android 어댑터 | instrumentation/수동 | Android 14 권한 회수와 실제 사건 종류 |

## 6. 다른 트랙 교체 경계

- UI는 `FeatureGateway` 어댑터를 통해 `AccessState`와 새로고침 결과만 소비한다.
- 도메인은 완성된 `AppSession`과 사용자 범위 기간 조회만 소비하며 열린 후보를 계산 입력으로 보지 않는다.
- 백업은 `CommittedChangePage`를 자기 `BackupChange`로 변환한다. 백업 트랙의 현재 단순 큐 구현은 CP-4 통합 때 커서 기반 어댑터로 교체한다.
