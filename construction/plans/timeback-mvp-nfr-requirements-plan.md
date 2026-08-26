# CONSTRUCTION STEP 02 — `timeback-mvp` 비기능 요구 계획 (track-ui 관점)

## 1. 현재 위치와 목적

- 공식 작업 단위: `timeback-mvp`
- 책임 트랙: `track-ui` (APP-10, UI-01–UI-08)
- STEP 01 기능 설계의 CP-0 공통작업 완료 상태에서, track-ui에 필요한 비기능 요구와 기술 스택을 결정한다.
- 이 계획은 track-ui의 독립 개발에 필요한 최소한의 기술 결정을 포함한다.

## 2. 범위

### 이번에 진행하는 범위

- track-ui에 필요한 NFR 조건 (성능 NFR-1.1, 테스트 NFR-3.2, 유지보수 NFR-5.1–5.2)
- UI 프레임워크·언어·상태관리·테스트 도구 결정
- FakeFeatureGateway 기반 독립 개발 가능한 기술 스택 구성
- CT-04, CT-06 계약을 구현할 수 있는 인터페이스 설계 방향

### 이번에 진행하지 않는 범위

- 다른 트랙(device-data, domain-engine, backup-server)의 기술 스택
- 서버 인프라, 데이터베이스, 네트워크 통신 제품
- 실제 Android UsageStatsManager 연동 코드
- 백업 서버 API 구현

## 3. 고정 입력

| 순위 | 입력 | 이번 단계에서 참조하는 것 |
|---:|---|---|
| 1 | `requirements.md` | NFR-1.1 (5초), NFR-3.2 (통합 테스트), NFR-5.1–5.2 (데이터 분리) |
| 2 | `frontend-components.md` | CT-04 화면 상태, UI-01–UI-08 계약 |
| 3 | `domain-entities.md` | 공통 논리 형식, 상태 사전 |
| 4 | `unit-of-work.md` | track-ui 독립 시작 수단: 합의된 기능 호출 계약과 고정 화면 상태 |
| 5 | Technical Constraints | Android 14 이상 |

## 4. 생성할 공식 산출물

| 파일 | 위치 | 내용 |
|---|---|---|
| `nfr-requirements.md` | `construction/timeback-mvp/nfr-requirements/` | track-ui NFR 조건 |
| `tech-stack-decisions.md` | `construction/timeback-mvp/nfr-requirements/` | 기술 스택 결정 |

## 5. 작성 체크리스트

- [ ] NFR-1.1 성능 요건을 track-ui 관점에서 구체화한다.
- [ ] NFR-3.2 통합 테스트 요건을 track-ui 화면 테스트 관점에서 구체화한다.
- [ ] NFR-5.1–5.2 유지보수 요건을 UI 컴포넌트 설계 관점에서 구체화한다.
- [ ] Android UI 프레임워크를 결정한다 (Jetpack Compose vs XML Views).
- [ ] 프로그래밍 언어를 결정한다 (Kotlin).
- [ ] 상태 관리 방식을 결정한다.
- [ ] 테스트 프레임워크를 결정한다.
- [ ] FakeFeatureGateway 구현에 사용할 DI 방식을 결정한다.
- [ ] 결정 근거를 기술 스택 문서에 명시한다.

## 6. 확인 질문

추가 질문 없음. 기술 제약(Android 14 이상)과 CT-04 계약 형태가 충분히 정의되어 있어 track-ui 기술 스택을 결정할 수 있다.

## 7. 완료 조건

- `nfr-requirements.md`와 `tech-stack-decisions.md`가 작성되어 있다.
- 기술 스택이 CT-04 계약과 CT-06 고정 결과를 구현할 수 있음을 설명한다.
- track-ui 독립 개발에 필요한 모든 도구가 명시되어 있다.
