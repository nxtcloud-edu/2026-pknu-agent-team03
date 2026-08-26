# TimeBack UOW-01 — CONSTRUCTION STEP 03 품질·검증 설계 계획

## 1. 목적

이 계획은 Java 17로 결정된 APP-05~APP-09 순수 도메인 구현이 시간 정확성, 결정 우선순위, 원자성, 개인정보 fixture 경계를 검증하도록 하는 최소 비기능 설계를 정한다. 코드·`src/`·검증 스크립트는 Gate 1 승인 전 생성하지 않는다.

## 2. 설계 범위

- 반개구간 `[start,end)`과 `end > start` 검증
- 중첩·union·자정·DST의 절대 지속시간 보존
- current-effective Context와 superseded revision 제외
- MeasurementDay retry·coverage gap·Baseline/Saved/Rate 상태
- Goal overlap·timer 완료의 원자성
- synthetic fixture·비로그·외부 의존성 없음
- `javac` 컴파일과 main 테스트 runner의 실패 전파

실제 Android 기기, UsageStats, DB·백업·UI 성능, NFR-1.1의 수치화되지 않은 대표 데이터 규모는 이 책임 트랙의 이번 코드 범위 밖이다.

## 3. Gate 1 검증 질문

### Q1. 시간 테스트 기준

- A. 모든 테스트에서 고정 `Instant`와 명시적 `ZoneId`를 사용하고 UTC·DST 전환 zone 사례를 함께 검증한다. **(추천)**
- B. 시스템 현재 시간·기본 timezone을 사용한다.
- Other: 직접 입력

### Q2. 품질 게이트

- A. `verify-domain.ps1`가 Java 17 컴파일, 합성 테스트 runner 실행, 실패 시 non-zero 반환을 모두 보장한다. **(추천)**
- B. 컴파일 또는 테스트 일부만 실행한다.
- Other: 직접 입력

### Q3. 메모리 fake 원자성

- A. 변경 전 검증하고 성공한 전체 snapshot만 교체하며, 실패 fake는 변경 전 snapshot을 보존한다. **(추천)**
- B. 부분 변경을 허용한다.
- Other: 직접 입력

### Q4. 개인정보·fixture 경계

- A. `example.app.*`, 가명 Activity·Goal, 고정 시각만 사용하고 production/test 로그에 행동 상세값을 출력하지 않는다. **(추천)**
- B. 실제 앱·행동 데이터를 fixture로 사용한다.
- Other: 직접 입력

### Q5. 실행성 검증 범위

- A. BR-T01~T24, TR-49~TR-62 중 순수 도메인이 소유하는 핵심 회귀를 runner로 실행하고 미구현 Android/저장 통합은 명시적으로 제외한다. **(추천)**
- B. 도메인 이외 책임 트랙까지 stub 없이 구현한다.
- Other: 직접 입력

## 4. Gate 1 요청

추천안은 `Q1=A, Q2=A, Q3=A, Q4=A, Q5=A`이다.

1) 수정 요청
2) 다음 단계로
