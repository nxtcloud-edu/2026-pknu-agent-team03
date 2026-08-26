# `timeback-mvp` STEP 02 통합 기술 스택 결정

## 1. 문서 상태

- 단계: CONSTRUCTION STEP 02 비기능 요구
- 범위: 네 책임 트랙의 통합 기술 기준
- 상태: 트랙별 결정 통합본
- 애플리케이션 구현 언어: **Java 17**
- 병합 방식: 트랙별 STEP 02–05 선행 완료 후 공통 빌드와 어댑터를 통합

## 2. 공통 기준

| 영역 | 결정 | 조건 |
|---|---|---|
| 언어·JDK | Java 17 | production/test source 공통 |
| 빌드 | Gradle 8.x, Kotlin DSL | 빌드 스크립트 언어이며 앱 Kotlin 사용을 뜻하지 않음 |
| Android 최소 SDK | API 34(Android 14) | 실제 기기 위험 검증은 별도 |
| 단위 테스트 | JUnit 5 | 순수 JVM 영역 |
| Android 테스트 | JUnit, Robolectric, AndroidX Test | UI·Room·Android adapter |
| 의존성 주입 | Hilt(Dagger) | 실제/Fake 바인딩 분리 |

## 3. Android 앱·UI

| 항목 | 결정 | 근거 |
|---|---|---|
| UI 언어 | Java 17 | 현재 Java 전환 결과와 일치 |
| UI 프레임워크 | AndroidX Fragment + XML View | 현재 Fragment 소스와 일치 |
| 상태 관리 | AndroidX ViewModel + LiveData | 현재 ViewModel 계약과 일치 |
| 내비게이션 | AndroidX Navigation Component | Fragment 기반 화면 전환 |
| 로컬 저장 | Room 2.6 이상, SQLite WAL | APP-11 원자 저장·조회 |
| 비동기 작업 | Java Executor + WorkManager | 증분 수집·백업 재시도 |
| HTTP | OkHttp + Retrofit | APP-12·APP-13 서버 경계 |
| 클라이언트 직렬화 | Gson | Retrofit 변환기 |

Kotlin, Jetpack Compose, StateFlow, Navigation Compose, Compose UI Test는 현재 Java/Fragment 구현의 확정 스택으로 사용하지 않는다.

## 4. 순수 도메인

| 항목 | 결정 | 조건 |
|---|---|---|
| 런타임 | Java 17 표준 라이브러리 | Android·DB·네트워크 의존 금지 |
| 시간·수치 | `java.time`, `BigDecimal`·정확 분수 | 제어 시간과 DST 회귀 유지 |
| 외부 경계 | Java interface·DTO·adapter | APP-11·APP-10과 연결 |

## 5. 백업 서버

| 항목 | 결정 | 조건 |
|---|---|---|
| 서버 프레임워크 | Spring Boot 3.x | SRV-01–SRV-03 HTTP 경계 |
| MVP DB | H2 | 격리 로컬 통합 검증 |
| 운영 전환 DB | PostgreSQL 15 이상 | 배포 전 별도 검증 |
| 서버 직렬화 | Jackson | Spring Boot 기본 |
| 컨테이너 | Docker Compose | 격리 서버·DB 실행 |

## 6. 계약·조립 기준

- CT-03 기준 제공자는 device의 APP-11 `DeviceDataAuthority`다.
- domain과 backup은 APP-11을 직접 복제하지 않고 포트·어댑터로 소비한다.
- UI는 `FeatureGateway`만 소비하며 저장소나 서버를 직접 호출하지 않는다.
- 프로덕션 조립은 실제 gateway·adapter를 사용하고 Fake는 테스트 조립에서만 사용한다.
- backup의 재시도·멱등성은 APP-11이 제공한 안정적인 `changeId`를 보존한다.

## 7. 미확정 위험

| 항목 | 현재 상태 | 처리 |
|---|---|---|
| OS-04 하드웨어 식별원 접근·안정성·개인정보 영향 | 실제 기기 미검증 | 임의 대체 금지, 사용자 기기 검증 전 미완료 |
| 서버 배포 주소·인증서 | 미정 | 로컬 격리 검증 뒤 배포 단계에서 결정 |
| PostgreSQL 운영 전환 | 미실행 | H2 계약 테스트 통과 뒤 별도 migration 검증 |
| 실제 Android 제조사별 UsageEvent 차이 | 미실행 | 사용자 실기기 체크리스트로 검증 |
