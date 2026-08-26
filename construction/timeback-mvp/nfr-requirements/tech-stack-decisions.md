# `timeback-mvp` track-ui 기술 스택 결정

## 1. 문서 상태

- 단계: CONSTRUCTION STEP 02 비기능 요구
- 범위: track-ui (APP-10, UI-01–UI-08)
- 상태: 확정

## 2. 기술 스택 결정 요약

| 영역 | 결정 | 버전/조건 |
|---|---|---|
| 언어 | Kotlin | 2.0+ |
| UI 프레임워크 | Jetpack Compose | BOM 2024.06+ |
| 최소 SDK | API 34 (Android 14) | requirements.md 제약 |
| 빌드 도구 | Gradle (Kotlin DSL) | 8.x |
| 상태 관리 | Compose State + ViewModel + StateFlow | AndroidX Lifecycle |
| 의존성 주입 | Hilt (Dagger) | AndroidX Hilt |
| 네비게이션 | Jetpack Navigation Compose | — |
| 단위 테스트 | JUnit 5 + Turbine (Flow 테스트) | — |
| UI 테스트 | Compose UI Test (ComposeTestRule) | — |
| 통합 테스트 | Robolectric + Compose Test | — |
| 코드 스타일 | ktlint | — |

## 3. 결정 근거

### 3.1 Kotlin + Jetpack Compose

- Android 14 이상 타겟이므로 Compose의 최신 기능을 제한 없이 사용 가능
- CT-04의 상태 기반 UI 계약(`LOADING`, `CONTENT`, `EMPTY`, `BLOCKED`, `ERROR` 등)이 Compose의 선언적 상태 모델과 자연스럽게 매핑됨
- `sealed class`/`sealed interface`로 화면 상태를 표현하면 컴파일 타임에 모든 상태 분기를 강제할 수 있음

### 3.2 StateFlow + ViewModel

- CT-04의 조회 결과를 `StateFlow<ViewState>`로 노출하면 Compose가 자동으로 재구성함
- `REFRESHING` 상태에서 기존 내용을 유지하는 요구(frontend-components.md §3.2)를 StateFlow의 현재 값 유지로 구현 가능
- ViewModel의 생명주기가 화면 회전·프로세스 복원에 안전

### 3.3 Hilt (DI)

- `FakeFeatureGateway`와 실제 `FeatureGateway`를 같은 인터페이스로 교체 가능
- 테스트에서 `@TestInstallIn`으로 Fake 주입이 간단
- CT-06의 독립 개발 요구("실제 기능 완료 전 UI 개발")를 DI 교체로 충족

### 3.4 테스트 전략

- **단위 테스트**: ViewModel + FakeFeatureGateway로 모든 화면 상태 전이 검증
- **UI 테스트**: ComposeTestRule로 화면 렌더링과 사용자 동작 검증
- **통합 테스트**: Robolectric으로 Android 프레임워크 없이 빠른 피드백 루프

### 3.5 네비게이션

- UI-01~UI-08 사이 이동(frontend-components.md §14 화면 간 영향 재조회)을 Navigation Compose의 NavGraph로 관리
- 딥링크와 백스택 처리가 선언적

## 4. CT-04 계약과 기술 매핑

| CT-04 개념 | Kotlin/Compose 구현 |
|---|---|
| 공통 화면 상태 | `sealed interface ViewState` |
| 조회 결과 `QueryResult<ViewData>` | `StateFlow<ViewState>` |
| 작업 결과 `OperationResult<ActionData>` | `sealed interface ActionResult` |
| `FakeFeatureGateway` | `interface FeatureGateway` + Hilt `@TestInstallIn` |
| 고정 결과 FX-* | `object FakeFeatureGatewayImpl : FeatureGateway` |

## 5. 프로젝트 구조 방향

```
src/
├── app/                          # Application, Hilt 설정, NavGraph
├── ui/
│   ├── common/                   # 공통 상태 모델, 공통 컴포넌트
│   ├── permission/               # UI-01
│   ├── home/                     # UI-02
│   ├── timeline/                 # UI-03
│   ├── apps/                     # UI-04
│   ├── recovery/                 # UI-05
│   ├── goals/                    # UI-06
│   ├── report/                   # UI-07
│   └── datamanagement/           # UI-08
├── domain/
│   └── gateway/                  # FeatureGateway 인터페이스
├── fake/                         # FakeFeatureGateway 구현
└── test/                         # 단위·UI·통합 테스트
```

## 6. 이 문서에서 정하지 않는 것

- 서버 통신 라이브러리 (Retrofit, Ktor 등) — track-backup-server STEP 02에서 결정
- 로컬 데이터베이스 (Room 등) — track-device-data STEP 02에서 결정
- 디자인 시스템 (Material 3 커스텀 테마) — STEP 05에서 기본 Material 3 사용 후 확장
- CI/CD 파이프라인 — STEP 06 빌드와 테스트에서 결정
