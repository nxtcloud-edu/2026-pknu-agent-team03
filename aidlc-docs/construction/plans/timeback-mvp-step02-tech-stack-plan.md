# TimeBack UOW-01 — CONSTRUCTION STEP 02 기술 결정 계획

## 1. 목적과 선행 조건

이 계획은 완료된 STEP 01 APP-05~APP-09 기능 설계를 Java로 구현하기 위한 최소 기술 결정을 정한다. 대상은 `timeback-mvp` 내부 `track-domain-engine`이며, 실제 코드·`src/`·빌드 산출물은 Gate 1 승인 전 생성하지 않는다.

확인 사실:

- 사용자 원문은 실제 구현 언어를 Java로 통일하도록 지정한다.
- 현재 환경에는 `java`·`javac` 17.0.12가 있고 Gradle·Maven은 없다.
- 기존 Java/Android 소스, Gradle/Maven 설정, 테스트 기반은 없다.
- 외부 Android·UsageStats·저장소·UI 구현은 다른 책임 트랙의 계약 경계이며, 이번 초기 구현은 fake를 쓰는 순수 업무 규칙이다.

## 2. 최소 기술 결정 범위

Gate 1 승인 후 다음을 `aidlc-docs/construction/timeback-mvp/nfr-requirements/tech-stack-decisions.md`에 확정한다.

1. Java 17 표준 라이브러리만 사용한다.
2. 빌드 도구를 새로 설치하거나 외부 라이브러리를 추가하지 않고 `javac`·`java`와 PowerShell 검증 스크립트를 사용한다.
3. 코드 경로는 `src/main/java`, 합성 검증 경로는 `src/test/java`로 분리한다.
4. 시간은 `java.time.Instant`, `Duration`, `ZoneId`, `LocalDate`로, 기간은 불변 `[start,end)` 값 객체로 표현한다.
5. 비율은 반올림하지 않는 `BigDecimal`, 식별자는 불투명 `String`으로 표현한다.
6. 실제 APP-04/APP-11/OS-05는 Java 포트(interface)로만 두고, 테스트에서 메모리 fake·제어 시간으로 대체한다.
7. 단일 검증 명령은 `powershell.exe -NoLogo -NoProfile -NonInteractive -File .kiro/scripts/verify-domain.ps1`로 제공한다. 이 명령은 JDK 컴파일과 합성 테스트 실행을 수행한다.

이 결정은 Android 앱 전체의 최종 빌드 체계를 확정하지 않는다. Android 모듈, Gradle, UI, UsageStats adapter, 실제 DB·백업·네트워크는 해당 책임 트랙과 이후 단계에서 별도 승인한다.

## 3. 구현 순서와 검증 범위

Gate 1과 STEP 02 Gate 2 후, STEP 03~04의 필요한 설계·Gate를 거친 다음 STEP 05에서 다음 순서로 코드화한다.

1. 시간·분류 value object와 `[start,end)` 연산
2. App/Activity/Session·Context evidence 및 current-effective decision
3. Waste union, MeasurementDay revision, Baseline·Saved·partial coverage
4. Goal/Timer/Recovered·overlap representative·Rate
5. 메모리 fake와 BR-T01~T24, TR-49~62의 핵심 합성 테스트

필수 회귀는 사용자 확정 Context의 superseded Waste 제외, 다중 관계 canonical decision, 7일 Baseline·retry/gap, Saved/Rate 경계, Goal overlap 상속, timer 완료 원자성이다. 실제 사용자 데이터와 Android API는 fixture에 사용하지 않는다.

## 4. Gate 1 검증 질문

### Q1. Java 기준

- A. 설치 확인된 JDK 17을 코드·테스트 기준으로 사용한다. **(추천)**
- B. 다른 Java/JDK 수준을 지정한다.
- Other: 직접 입력

### Q2. 의존성과 빌드 방식

- A. 외부 의존성 없이 JDK 17 `javac`·`java`와 PowerShell 검증 스크립트를 사용한다. **(추천)**
- B. 정확한 버전이 고정된 Gradle 또는 Maven 기반을 추가한다.
- Other: 직접 입력

### Q3. 소스 구조

- A. `src/main/java`와 `src/test/java`를 사용하고 pure domain/port/fake/합성 테스트를 분리한다. **(추천)**
- B. 다른 Java 프로젝트 구조를 지정한다.
- Other: 직접 입력

### Q4. 도메인 값 표현

- A. `Instant`·`Duration`·`ZoneId`·`LocalDate`, 불변 `[start,end)` 값 객체, `BigDecimal` ratio, `String` ID를 사용한다. **(추천)**
- B. 다른 자료형 정책을 지정한다.
- Other: 직접 입력

### Q5. 테스트 실행 방식

- A. 외부 프레임워크 없이 Java `main` 기반 합성 테스트 runner와 fake를 사용하고 `.kiro/scripts/verify-domain.ps1`로 실행한다. **(추천)**
- B. 정확한 버전이 고정된 단위 테스트 프레임워크를 추가한다.
- Other: 직접 입력

### Q6. 초기 구현 경계

- A. APP-05~APP-09 순수 도메인과 fake 포트만 구현하고 Android/실제 DB/UI/백업은 계약으로 남긴다. **(추천)**
- B. 이번 단계에 다른 책임 트랙 구현을 포함한다.
- Other: 직접 입력

## 5. Gate 1 요청

추천안은 Q1=A, Q2=A, Q3=A, Q4=A, Q5=A, Q6=A이다. 선택 후에만 STEP 02 기술결정 산출물을 생성한다.

1) 수정 요청
2) 다음 단계로
