# `timeback-mvp` track-ui 비기능 요구사항

## 1. 문서 상태

- 단계: CONSTRUCTION STEP 02 비기능 요구
- 범위: track-ui (APP-10, UI-01–UI-08)
- 상태: 확정

## 2. 성능 (NFR-1.1 구체화)

| 요구 ID | track-ui 구체 조건 | 측정 방법 |
|---|---|---|
| NFR-1.1-UI-01 | 화면 전환 후 ViewModel의 첫 `CONTENT` 상태 도달까지 200ms 이내 (FakeFeatureGateway 기준) | Compose UI Test에서 상태 전이 시간 측정 |
| NFR-1.1-UI-02 | 실제 FeatureGateway 연결 시 새로고침 후 `CONTENT` 상태 도달까지 5초 이내 | 통합 테스트에서 타임아웃 검증 |
| NFR-1.1-UI-03 | `REFRESHING` 상태에서 기존 데이터 표시 유지, 새 데이터 도달 시 한 프레임 내 교체 | UI 테스트에서 기존 내용 노출 확인 |
| NFR-1.1-UI-04 | 화면 간 이동 시 이전 화면의 캐시된 상태를 즉시 표시 (cold start 제외) | Navigation 테스트 |

## 3. 정확성 (NFR-2 구체화)

| 요구 ID | track-ui 구체 조건 | 검증 |
|---|---|---|
| NFR-2-UI-01 | UI는 시간 계산·중첩·분할을 수행하지 않고 도메인 결과만 표시한다 | ViewModel에 계산 로직 없음을 코드 리뷰·테스트로 확인 |
| NFR-2-UI-02 | 도메인이 제공한 `Duration`, `TimeRange` 값을 변환 없이 표시한다 | FakeFeatureGateway 반환값과 화면 표시값 일치 테스트 |
| NFR-2-UI-03 | `EMPTY`와 계산된 0은 구분하여 표시한다 (frontend-components.md §10.3) | UI 테스트에서 빈 상태 vs 0값 상태 분기 확인 |

## 4. 테스트 (NFR-3.2 구체화)

| 요구 ID | track-ui 구체 조건 | 도구 |
|---|---|---|
| NFR-3.2-UI-01 | UI-01~UI-08 각 화면의 모든 상태(LOADING, CONTENT, EMPTY, BLOCKED, ERROR 등)에 대해 최소 1개 UI 테스트 | ComposeTestRule |
| NFR-3.2-UI-02 | 각 화면의 사용자 작업(조회, 생성, 수정, 이동)에 대해 최소 1개 ViewModel 단위 테스트 | JUnit 5 + Turbine |
| NFR-3.2-UI-03 | FakeFeatureGateway의 모든 FX-* 고정 결과(§12)에 대해 UI가 올바르게 반응하는 테스트 | ComposeTestRule |
| NFR-3.2-UI-04 | 화면 간 영향 재조회(frontend-components.md §14)가 발생하는 시나리오 통합 테스트 | Robolectric |

## 5. 유지보수 (NFR-5 구체화)

| 요구 ID | track-ui 구체 조건 | 구현 방향 |
|---|---|---|
| NFR-5.1-UI-01 | 각 화면은 독립된 패키지로 분리하고 공통 의존은 `ui/common`을 통해서만 사용한다 | 패키지 구조 규칙 |
| NFR-5.1-UI-02 | FeatureGateway 인터페이스는 화면별로 분리하거나 화면별 메서드 그룹으로 구분한다 | 인터페이스 분리 원칙 |
| NFR-5.2-UI-01 | ViewState `sealed class`는 CT-04 상태와 1:1 매핑하고 임의 상태를 추가하지 않는다 | 코드 리뷰 규칙 |
| NFR-5.2-UI-02 | FakeFeatureGateway는 실제 FeatureGateway와 같은 인터페이스를 구현하고 계약 검증 테스트를 공유한다 | 인터페이스 기반 테스트 |

## 6. 보안 (NFR-4 구체화)

| 요구 ID | track-ui 구체 조건 |
|---|---|
| NFR-4-UI-01 | UI는 UsageEvent 원본 데이터를 직접 접근하지 않고 도메인 결과만 표시한다 |
| NFR-4-UI-02 | UI는 화면 내용, 앱 내부 콘텐츠, 위치 정보를 수집하거나 표시하지 않는다 |
| NFR-4-UI-03 | UI-01에서 권한 미허용 시 주요 화면 진입을 차단한다 (BLOCKED 상태 강제) |

## 7. 추적

| NFR | 관련 화면 | 관련 스토리 |
|---|---|---|
| NFR-1.1 | UI-01~UI-08 전체 | US-08, US-14, US-15 |
| NFR-2.1~2.4 | UI-02, UI-03, UI-05, UI-07 | US-10~US-13, US-19~US-21 |
| NFR-3.2 | UI-01~UI-08 전체 | 전체 |
| NFR-4.4 | UI-03, UI-04 | US-02 |
| NFR-5.1~5.2 | UI-01~UI-08 전체 | 전체 |
