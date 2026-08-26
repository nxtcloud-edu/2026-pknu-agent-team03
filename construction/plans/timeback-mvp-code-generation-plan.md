# CONSTRUCTION STEP 05 — `timeback-mvp` 코드 생성 계획 (track-ui)

## 1. 현재 위치와 목적

- 공식 작업 단위: `timeback-mvp`
- 책임 트랙: `track-ui` (APP-10, UI-01–UI-08)
- STEP 02 기술 스택 결정 완료 (Kotlin + Jetpack Compose + Hilt + JUnit 5)
- STEP 03, 04 건너뜀 (track-ui는 순수 클라이언트 화면, 인프라 없음)
- 이번 단계에서 FakeFeatureGateway 기반으로 UI-01~UI-08을 독립 구현한다.

## 2. 코드 생성 순서

AIDLC 규칙: **업무 규칙 → API → 저장 → 화면**

track-ui 적용:
1. **업무 규칙 층** — FeatureGateway 인터페이스 + 공통 상태 모델 (CT-04 매핑)
2. **API 층** — FakeFeatureGateway 구현 (CT-06 고정 결과)
3. **저장 층** — track-ui에 저장 없음 (도메인 결과만 표시), 건너뜀
4. **화면 층** — ViewModel + Compose UI (UI-01~UI-08)

각 층의 테스트를 같은 단계에서 함께 만든다.

## 3. 독립 개발 전략

- `unit-of-work.md` §3: track-ui 독립 시작 수단은 "합의된 기능 호출 계약과 고정 화면 상태·가짜 조회 결과"
- `frontend-components.md` §12 CT-06의 FX-* 고정 결과를 FakeFeatureGateway가 반환
- 다른 트랙의 실제 구현을 기다리지 않고 인터페이스 기반으로 개발

## 4. 생성할 파일 목록

### 4.1 공통 (업무 규칙 층)

| 파일 | 역할 |
|---|---|
| `src/domain/gateway/FeatureGateway.kt` | 모든 화면이 사용하는 기능 호출 인터페이스 |
| `src/domain/model/ViewState.kt` | 공통 화면 상태 sealed interface |
| `src/domain/model/ActionResult.kt` | 사용자 작업 결과 sealed interface |
| `src/domain/model/Entities.kt` | CT-01, CT-02 논리 엔터티 데이터 클래스 |

### 4.2 FakeFeatureGateway (API 층 — 테스트 대역)

| 파일 | 역할 |
|---|---|
| `src/fake/FakeFeatureGateway.kt` | CT-06 고정 결과를 반환하는 Fake 구현 |
| `src/fake/FixedResults.kt` | FX-UI01~FX-UI08 고정 데이터 정의 |

### 4.3 화면 (UI 층)

| 화면 | ViewModel | Screen | 의존성 순서 |
|---|---|---|---|
| UI-01 권한·초기 진입 | `PermissionViewModel` | `PermissionScreen` | 없음 (독립) |
| UI-02 홈 대시보드 | `HomeViewModel` | `HomeScreen` | UI-01 CONTENT 후 진입 |
| UI-03 Timeline | `TimelineViewModel` | `TimelineScreen` | 독립 |
| UI-04 앱 관리 | `AppManagementViewModel` | `AppManagementScreen` | 독립 |
| UI-05 시간 되찾기 | `RecoveryViewModel` | `RecoveryScreen` | UI-06 Goal 존재 필요 |
| UI-06 목표 | `GoalsViewModel` | `GoalsScreen` | 독립 |
| UI-07 리포트 | `ReportViewModel` | `ReportScreen` | 독립 |
| UI-08 데이터 관리 | `DataManagementViewModel` | `DataManagementScreen` | 독립 |

### 4.4 앱 진입점

| 파일 | 역할 |
|---|---|
| `src/app/TimeBackApp.kt` | Hilt Application |
| `src/app/MainActivity.kt` | 단일 Activity |
| `src/app/NavGraph.kt` | Navigation Compose 라우팅 |
| `src/app/di/AppModule.kt` | Hilt DI 모듈 |

### 4.5 테스트

| 파일 | 대상 |
|---|---|
| `src/test/viewmodel/PermissionViewModelTest.kt` | UI-01 상태 전이 |
| `src/test/viewmodel/HomeViewModelTest.kt` | UI-02 상태 전이 |
| `src/test/viewmodel/TimelineViewModelTest.kt` | UI-03 상태 전이 |
| `src/test/viewmodel/AppManagementViewModelTest.kt` | UI-04 상태 전이 |
| `src/test/viewmodel/RecoveryViewModelTest.kt` | UI-05 상태 전이 |
| `src/test/viewmodel/GoalsViewModelTest.kt` | UI-06 상태 전이 |
| `src/test/viewmodel/ReportViewModelTest.kt` | UI-07 상태 전이 |
| `src/test/viewmodel/DataManagementViewModelTest.kt` | UI-08 상태 전이 |

## 5. 작성 체크리스트

### 5.1 업무 규칙 층
- [ ] FeatureGateway 인터페이스 작성 (CT-04 §3~§11의 조회·작업 매핑)
- [ ] ViewState sealed interface 작성 (CT-04 §3.2 상태 매핑)
- [ ] ActionResult sealed interface 작성 (CT-04 §3.3 결과 매핑)
- [ ] 도메인 엔터티 데이터 클래스 작성

### 5.2 API 층 (FakeFeatureGateway)
- [ ] FakeFeatureGateway 구현 (FX-UI01~FX-UI08 고정 결과 반환)
- [ ] FixedResults 데이터 정의

### 5.3 화면 층
- [ ] UI-01 PermissionViewModel + PermissionScreen
- [ ] UI-02 HomeViewModel + HomeScreen
- [ ] UI-03 TimelineViewModel + TimelineScreen
- [ ] UI-04 AppManagementViewModel + AppManagementScreen
- [ ] UI-05 RecoveryViewModel + RecoveryScreen
- [ ] UI-06 GoalsViewModel + GoalsScreen
- [ ] UI-07 ReportViewModel + ReportScreen
- [ ] UI-08 DataManagementViewModel + DataManagementScreen
- [ ] NavGraph 및 화면 간 이동
- [ ] MainActivity + Application 설정

### 5.4 테스트
- [ ] 각 ViewModel 단위 테스트 (8개)
- [ ] 각 화면 상태 분기 테스트

### 5.5 빌드 확인
- [ ] Gradle 프로젝트 설정 (build.gradle.kts)
- [ ] 컴파일 확인

## 6. 구현 우선순위 (의존성 없는 것부터)

1. 공통 모델 + FeatureGateway 인터페이스
2. FakeFeatureGateway
3. UI-01 (권한 — 앱 진입 흐름의 시작점)
4. UI-06 (목표 — UI-05가 의존하므로 먼저)
5. UI-03 (Timeline — 핵심 화면)
6. UI-04 (앱 관리 — 독립)
7. UI-02 (홈 대시보드 — 여러 데이터 조합)
8. UI-07 (리포트)
9. UI-05 (시간 되찾기 — UI-06 이후)
10. UI-08 (데이터 관리)
11. NavGraph + MainActivity 통합
12. 테스트

## 7. 완료 조건

- FeatureGateway 인터페이스가 CT-04의 모든 조회·작업을 포함한다.
- FakeFeatureGateway가 CT-06의 FX-* 고정 결과를 모두 반환할 수 있다.
- UI-01~UI-08 각 화면이 모든 CT-04 상태를 처리한다.
- 각 ViewModel에 최소 1개 단위 테스트가 있다.
- Gradle 빌드가 컴파일을 통과한다.
