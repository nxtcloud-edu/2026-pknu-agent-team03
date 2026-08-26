# TimeBack UOW-01 — CONSTRUCTION STEP 03 품질·검증 설계

## 1. 승인 근거와 범위

- Gate 1 승인: 2026-08-26, 사용자 응답 `2`
- 채택 답변: `Q1=A, Q2=A, Q3=A, Q4=A, Q5=A`
- 대상: APP-05~APP-09 Java 순수 도메인과 CT-06 fake

이 설계는 STEP 05 구현이 만족해야 하는 테스트 가능성·시간 정확성·원자성·개인정보 경계를 정의한다. 실제 Android/DB/UI/backup 통합은 제외한다.

## 2. 품질 설계

### 2.1 결정론적 시간

- 모든 테스트 입력은 고정 `Instant`와 명시 `ZoneId`를 사용한다.
- 시스템 시계·기본 timezone에 의존하지 않는다.
- UTC와 DST 전환이 있는 zone을 각각 fixture로 사용한다.
- 지속시간은 항상 `Duration.between(start, end)`의 절대 시간선 값을 기준으로 하며, 현지 날짜는 분할·표시·Baseline 유효일 판단에만 사용한다.

### 2.2 검증 실행

`verify-domain.ps1`는 다음 조건을 모두 만족해야 한다.

1. `src/main/java`과 `src/test/java`를 Java 17로 컴파일한다.
2. main 기반 `DomainEngineTestRunner`를 실행한다.
3. assertion·예외·컴파일 실패는 non-zero 종료 코드로 전파한다.
4. 실행 전 결과 디렉터리를 정리하지 않고, 해당 실행의 컴파일 출력만 결정적으로 교체한다.

외부 다운로드·실행 중 네트워크 연결·실제 사용자 행동 데이터 접근은 금지한다.

### 2.3 메모리 fake 원자성

- 변경 명령은 모든 입력·상태 검증 후에만 snapshot을 교체한다.
- fake가 실패하도록 설정된 경우 저장 전 snapshot과 current timer·Context revision·Goal 배정은 유지된다.
- timer 완료는 Recovered 생성과 timer 제거가 함께 성공한 경우에만 관찰 가능하다.
- Context 확정은 새 `CURRENT` revision과 기존 `SUPERSEDED` 전환이 함께 성공한 경우에만 관찰 가능하다.

### 2.4 합성 fixture와 로그

- fixture는 `example.app.*`, 가명 Activity/Goal 이름, 고정 시각만 사용한다.
- package name·활동명·시각 패턴을 production/test 기본 로그에 출력하지 않는다.
- 화면 내용, 위치, YouTube 내부 콘텐츠, 실제 UsageEvent를 수집·fixture화·출력하지 않는다.

### 2.5 필수 실행 회귀

| 범주 | 최소 검증 |
|---|---|
| 시간 | invalid interval 거부, 경계 접촉, 부분 중첩, union, 자정·DST duration 보존 |
| Context | UNCLASSIFIED fallback, Activity/App conflict, current-effective revision, superseded WASTE 제외, 다중 evidence 단일 decision |
| Metrics | COMPLETE Waste 0, 7일 관찰, coverage gap reset, MeasurementDay idempotent upsert, partial IN_PROGRESS, Saved clamp, Rate reason precedence |
| Recovery | goal validation, timer 단일 실행·완료 원자성, manual record, overlap pending·대표 선택·재분할 상속, lifetime/period 분리 |
| 안전 | fake 실패 시 snapshot 보존, synthetic fixture·비로그 경계 |

BR-T01~T24 및 TR-49~TR-62 중 위 범주에 속하는 순수 도메인 소유 사례는 runner의 명명된 테스트로 연결한다. Android 저장·UI·백업의 실제 통합 사례는 이 구현에서 성공으로 가장하지 않는다.

## 3. 완료 기준

- [x] 시간·timezone·DST의 결정론적 기준을 정했다.
- [x] 컴파일·runner 실패 전파 기준을 정했다.
- [x] fake snapshot 원자성과 핵심 변경 묶음을 정했다.
- [x] 합성 fixture·민감 데이터 비로그 경계를 정했다.
- [x] 기능 설계의 핵심 회귀를 실행 범위에 연결했다.
- [x] 승인되지 않은 통합 구현·외부 의존성을 추가하지 않았다.

## 4. Gate 2 요청

1) 수정 요청
2) 다음 단계로
