# `timeback-mvp` track-device-data 비기능 요구와 기술 결정

## 1. 상태와 범위

| 항목 | 값 |
|---|---|
| 단계 | CONSTRUCTION STEP 02 |
| 책임 | OS-01, OS-02, OS-03, OS-05, APP-01, APP-03, APP-04, APP-11 |
| 주 스토리 | US-01–US-03 |
| 상태 | 트랙 독립 개발 기준 확정 |

이 문서는 `track-device-data`가 다른 트랙의 실제 구현을 기다리지 않고 CT-01, CT-03, CT-06과 테스트 대역으로 STEP 05까지 진행하기 위한 품질 기준이다. `timeback-mvp` 전체의 공식 게이트 승인이나 실제 Android 기기 검증 완료를 대신하지 않는다.

## 2. 구체화한 비기능 요구

| ID | 조건 | 검증 |
|---|---|---|
| `NFR-DD-PERF-01` | 권한 확인, 증분 수집, 세션 재구성, 저장 완료를 포함한 새로고침의 기기 데이터 구간은 대표 데이터 10,000건에서 4초 이내다. UI·도메인 조립을 포함한 전체 5초 예산 중 최소 1초를 남긴다. | 고정 사건 부하 테스트와 STEP 06 계측 |
| `NFR-DD-PERF-02` | 기간 조회는 사용자, 엔터티 종류, 시간 구간 인덱스를 사용하며 전체 테이블 순회를 정상 경로로 삼지 않는다. | Room query plan 검토 |
| `NFR-DD-COR-01` | 같은 원본 사건을 포함하는 겹친 재조회는 안정적인 `eventId`로 한 번만 저장한다. | `DD-COLLECT-05` |
| `NFR-DD-COR-02` | 원본 사건과 체크포인트, 파생 세션 교체와 변경 통지는 각각 하나의 트랜잭션으로 성공하거나 모두 이전 상태를 유지한다. | `DD-COLLECT-04`, `DD-DATA-01`, `DD-DATA-04` |
| `NFR-DD-COR-03` | 자정 분할 조각의 기간 합은 분할 전 기간과 같고 현지 하루를 24시간으로 가정하지 않는다. | DST 포함 `DD-SESSION-05` |
| `NFR-DD-SEC-01` | 현재 Usage Access가 허용된 경우에만 OS-02를 호출한다. 권한 조회 실패 때 과거 허용값으로 우회하지 않는다. | `DD-ACCESS-01`–`04` |
| `NFR-DD-SEC-02` | 원본에는 패키지명, 허용 사건 종류, 발생 시각과 앱 생성 메타데이터만 저장한다. 화면 내용, 앱 내부 콘텐츠, 위치는 모델과 저장 스키마에 존재하지 않는다. | API·스키마 검토, `DD-COLLECT-02` |
| `NFR-DD-ISO-01` | 모든 저장·조회·변경 커서에는 `DataOwnerScope`가 필요하며 다른 사용자 범위의 식별자는 노출하지 않는다. | `DD-DATA-02` |
| `NFR-DD-TEST-01` | OS 경계는 실제 구현과 가짜 구현이 같은 포트에 연결되며 권한, 사건, 화면 종료, 시간을 독립 제어할 수 있다. | CT-06 계약 테스트 |
| `NFR-DD-TEST-02` | Android 14 이상 실제 기기에서 허용·거부·회수와 앱 전환 사건을 확인하기 전 NFR-3.3을 완료로 표시하지 않는다. | STEP 06 기기 체크리스트 |
| `NFR-DD-MNT-01` | UsageEvent, AppSession, 열린 후보, 도메인 파생 레코드는 별도 타입·저장 집합으로 유지한다. | 코드·스키마 검토 |

## 3. 기술 스택 결정

| 영역 | 결정 | 조건 |
|---|---|---|
| 앱 언어 | Java | 17 이상, Kotlin 기반 track-ui와 JVM 상호운용 |
| Android 기준 | compile/target SDK 34, min SDK 34 | 요구사항의 Android 14 이상 |
| OS 사건 | `UsageStatsManager.queryEvents()` | `ACTIVITY_RESUMED`/`ACTIVITY_PAUSED`와 호환 foreground/background 사건만 매핑 |
| 권한 경계 | `AppOpsManager` + `Settings.ACTION_USAGE_ACCESS_SETTINGS` | 설정 열기 성공과 권한 허용을 구분 |
| 로컬 저장 | Room 2.6 이상, SQLite WAL | 사용자·종류·기간 인덱스, `withTransaction` 원자성 |
| 비동기 실행 | Java Executor API + WorkManager | 새로고침은 즉시 실행, 후속 증분 수집은 고유 작업으로 직렬화 |
| 시간 | `java.time.Instant`, `ZoneId`, `ZonedDateTime` | epoch millis 저장, OS-05에서만 현지 경계 계산 |
| 직렬화 | Gson | 로컬 변경 사본과 백업 어댑터에만 사용 |
| 단위 테스트 | Java assertions/JUnit 5 | 순수 JVM 업무 규칙과 가짜 포트 |
| 저장 통합 테스트 | Room in-memory + Robolectric/AndroidX Test | 실제 트랜잭션·조회·삭제 검증 |
| 실기기 테스트 | Android instrumentation + 수동 체크리스트 | NFR-3.3 완료 증거 |

### 선택 근거

- Java 17은 팀의 device-data 구현 언어 결정과 일치하며 `record`·`sealed interface`로 기존 계약 구조를 유지한다. Kotlin 기반 `track-ui`와는 JVM 경계에서 상호운용한다.
- Room은 APP-11의 트랜잭션, 사용자 범위 조회, 기간 인덱스, 스키마 마이그레이션을 명시적으로 검증할 수 있다.
- WorkManager는 프로세스 재시작 뒤에도 증분 수집을 재개할 수 있지만, 유일 작업 이름을 사용자 범위별로 두어 동시에 같은 체크포인트를 갱신하지 않는다.
- `java.time` 경계 계산은 DST가 있는 현지 날짜도 절대 시각 구간으로 바꾸므로 고정 24시간 가정을 제거한다.

## 4. 사건 호환성과 안정 ID

- Android 14에서 foreground는 우선 `ACTIVITY_RESUMED`, background는 `ACTIVITY_PAUSED`로 읽는다. 호환 상수 `MOVE_TO_FOREGROUND`, `MOVE_TO_BACKGROUND`가 같은 값이면 중복 분기하지 않는다.
- OS-02가 반환하는 동일 `(packageName, eventType, occurredAt)` 묶음 안의 발생 순번을 포함해 안정 ID 입력을 만든다.
- 안정 ID는 사용자 범위, 패키지명, 정규화 사건 종류, 발생 시각, 동일 묶음 순번의 SHA-256이다. 수집 시각은 ID 입력에 넣지 않는다.
- 제조사별 사건 누락은 원본을 합성해 숨기지 않고 APP-04의 다음 앱·화면 종료·열린 후보 규칙으로 처리한다.

## 5. STEP 02 잔여 위험

| 위험 | 현재 처리 |
|---|---|
| 실제 제조사별 UsageEvent 차이 | 가짜 경계로 코드 진행, STEP 06 실기기 미완료로 유지 |
| UI·백업 트랙의 CT 모델 차이 | 트랙 내부 타입을 고정하고 CP 통합 때 명시적 어댑터 작성 |
| Room/WorkManager 의존성 미병합 | 순수 Java 메모리 구현으로 계약 검증, Android 저장 구현은 통합 빌드에서 연결 |
