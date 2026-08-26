# TimeBack UOW-01 — CONSTRUCTION STEP 02 기술 결정

## 1. 승인 근거와 범위

- Gate 1 승인: 2026-08-26, 사용자 응답 `2`
- 채택 답변: `Q1=A, Q2=A, Q3=A, Q4=A, Q5=A, Q6=A`
- 대상: `timeback-mvp` 내부 `track-domain-engine`의 APP-05~APP-09 순수 도메인 규칙
- 제외: Android UI/UsageStats adapter, 실제 DB, 백업·네트워크, 다른 책임 트랙 구현

이 문서는 STEP 01의 언어 중립 설계를 구현 가능한 Java 경계로 변환한다. 코드 생성은 STEP 03~04 Gate 승인 전까지 계속 차단한다.

## 2. 확정 기술 선택

| 결정 | 채택 내용 | 근거 |
|---|---|---|
| 언어·JDK | Java 17.0.12 LTS, Java SE 표준 라이브러리 | 현재 환경에서 `java`·`javac` 17.0.12 확인 |
| 외부 의존성 | 없음 | Gradle·Maven이 없고 도메인 규칙은 표준 라이브러리로 구현 가능 |
| 컴파일·실행 | `javac`·`java`를 호출하는 PowerShell 검증 스크립트 | 설치·다운로드 없이 재현 가능 |
| 소스 구조 | `src/main/java`, `src/test/java` | production 규칙과 합성 검증 분리 |
| 기본 패키지 | `io.timeback.domain` | Android·UI·저장 제품에 종속되지 않는 도메인 경계 |
| 시간 | `Instant`, `Duration`, `ZoneId`, `LocalDate` | 절대 시간·현지 날짜 경계 분리 |
| 기간 | 불변 `[start,end)` 값 객체 | DM-02, BR-C02의 유효성·중첩·union 요구 |
| 비율 | `BigDecimal`, 반올림 없는 원본 값 | Recovery Rate 100% 초과·도메인 반올림 금지 |
| 식별자 | 불투명 `String` | ID 의미 추론 금지 |
| 외부 경계 | Java interface 포트와 메모리 fake | APP-04/APP-11/OS-05 직접 구현 금지, CT-06 |
| 테스트 | 외부 프레임워크 없는 `main` 기반 합성 테스트 runner | 의존성 없이 BR-T/TR 회귀 검증 |

## 3. 코드 경계

향후 STEP 05에서만 다음 구조를 생성한다.

```text
src/
  main/java/io/timeback/domain/
    model/       # 시간·분류·기준 엔터티 값
    interval/    # [start,end) 연산과 분할·union
    context/     # evidence와 current-effective decision
    metrics/     # Waste·Baseline·Saved·Rate
    recovery/    # Goal·Timer·Recovered·overlap
    port/        # Session/Data/Time/Notifier 계약
  test/java/io/timeback/domain/
    support/     # memory fake·controlled time·fixture
    DomainEngineTestRunner.java
```

- production 패키지는 Android SDK, 데이터베이스, UI, 네트워크 객체를 import하지 않는다.
- fake는 합성 package name·활동·목표와 고정 시각만 사용한다.
- 실제 persistence/transaction은 포트의 원자성 계약만 표현하며 이번 구현 범위에 포함하지 않는다.

## 4. 검증 명령 결정

STEP 05에서 `.kiro/scripts/verify-domain.ps1`를 생성한다. 해당 스크립트는 다음을 수행한다.

1. `src/main/java`과 `src/test/java`의 Java 17 소스를 임시 `build/domain-classes`로 컴파일한다.
2. `io.timeback.domain.DomainEngineTestRunner`를 실행한다.
3. 컴파일·합성 테스트 중 하나라도 실패하면 0 이외의 종료 코드를 반환한다.

필수 합성 검증은 BR-T01~T24와 TR-49~TR-62의 도메인 소유 사례를 기준으로 한다. 실제 Android 빌드·기기 검증 명령은 현재 책임 트랙의 범위 밖이다.

## 5. 위험과 완화

| 위험 | 완화 |
|---|---|
| Gradle/Maven 부재로 외부 test framework 사용 불가 | JDK 표준 컴파일과 main runner로 즉시 검증 |
| 이후 Android 모듈 도입 시 빌드 체계 변경 필요 | 도메인 코드를 SDK 독립적으로 두고 후속 트랙 Gate에서 별도 결정 |
| in-memory fake가 실제 저장 원자성을 증명하지 못함 | command 전후 snapshot·실패 fake로 도메인 원자성 계약만 검증; 실제 APP-11 통합은 별도 트랙에서 검증 |
| 임의 기술 추가 | 명시된 표준 라이브러리 외 의존성·Android·DB·네트워크 도입 금지 |

## 6. STEP 02 Gate 2 완료 검사

- [x] Java 17 기준이 현재 환경 확인 결과와 일치한다.
- [x] 외부 의존성·다운로드 없이 실행 가능한 검증 경계를 정했다.
- [x] APP-05~APP-09 pure domain과 외부 책임 트랙을 분리했다.
- [x] 시간·비율·식별자·port/fake·source/test 경계를 결정했다.
- [x] 승인되지 않은 Android·DB·백업·UI 구현을 추가하지 않았다.
- [x] STEP 05의 실제 코드·검증 명령과 필수 회귀 범위를 추적했다.

## 7. Gate 2 요청

1) 수정 요청
2) 다음 단계로
