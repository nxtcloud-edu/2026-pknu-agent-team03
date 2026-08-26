# `timeback-mvp` 트랙 통합·작동 보정 계획

## 1. 목적과 작업 방식 변경

- 네 책임 트랙은 공통 CP-0 계약을 기준으로 각자 CONSTRUCTION STEP 02–05 산출물을 먼저 완성할 수 있다.
- 트랙별 선행 완료는 `timeback-mvp` 전체 STEP 02–05의 공식 통합 완료를 뜻하지 않는다.
- 네 트랙을 병합한 뒤 계약 차이, 빌드 구성, 실제 어댑터와 실행 경계를 한 번에 보정한다.
- 최종 Android 설치 빌드와 실제 기기 검증은 사용자가 수행할 수 있도록 재현 가능한 명령과 미완료 체크리스트를 남긴다.
- 통합 수정은 업무 규칙 → 공통 계약·API → 저장·서버 → UI 순서를 유지한다.

## 2. 통합 대상

| 트랙 | 통합 대상 | 현재 독립 결과 |
|---|---|---|
| `track-device-data` | OS-01–OS-03·OS-05, APP-01·03·04·11 | Java 계약, Android OS 어댑터, 메모리 저장, 독립 회귀 |
| `track-domain-engine` | APP-05–APP-09 | Java 순수 도메인, 포트, 독립 회귀 |
| `track-ui` | APP-10, UI-01–UI-08 | Java ViewModel·Fragment 경계, Fake/Mock gateway |
| `track-backup-server` | OS-04, APP-02·12·13, SRV-01–SRV-03 | Java 백업·삭제 로직, 메모리 서버, 독립 회귀 |

통합 기준은 `inception/application-design/unit-of-work.md`와
`construction/timeback-mvp/functional-design/`의 CP-0 CT-01–CT-06이다.

## 3. 보호 대상

- 기존 사용자의 수정 파일과 미추적 파일은 덮어쓰거나 삭제하지 않는다.
- 트랙의 업무 계산과 독립 테스트 의미는 바꾸지 않는다.
- 패키지·모델 변환은 어댑터에서 수행하고 도메인 계산을 UI나 서버에 복제하지 않는다.
- 하드웨어 식별원은 실제 검증 전까지 fake 경계로 유지하며 임의 식별 방식으로 대체하지 않는다.

## 4. 실행 순서

### Phase 1. Git·문서 통합

- [ ] 통합 작업 브랜치를 생성한다.
- [ ] 최신 `main`에 `track-backup-server` 결과를 병합한다.
- [ ] `nfr-requirements.md`, `tech-stack-decisions.md`의 add/add 충돌을 트랙별 섹션을 보존하는 단일 문서로 해결한다.
- [ ] Java 전환 뒤 남은 UI의 Kotlin·Compose·StateFlow 설명을 Java·Fragment·LiveData 구현과 일치시킨다.
- [ ] 트랙 독립 완료와 UOW 통합 완료를 구분하도록 상태·완료 표현을 정리한다.

### Phase 2. CT-01–CT-03 데이터·도메인 통합

- [ ] device `AppSession`을 domain 입력으로 바꾸는 명시적 어댑터를 추가한다.
- [ ] domain 외부 호출에 필요한 타입과 포트를 공개 실행 경계 뒤에 둔다.
- [ ] APP-11의 `DeviceDataAuthority`를 domain 저장 요구와 연결하는 어댑터를 추가한다.
- [ ] device의 `CommittedChange`를 유일한 CT-03 제공 모델로 삼고 backup의 중복 모델을 변환 경계로 축소한다.
- [ ] 변경 순서·소유자·안정적인 `changeId`가 백업 재시도에서 유실되지 않도록 한다.

### Phase 3. CT-04 UI 통합

- [ ] `FeatureGateway`의 실제 구현을 추가해 device, domain, backup 기능을 조립한다.
- [ ] 프로덕션 DI가 `FakeFeatureGateway`를 고정 주입하지 않도록 실제/Fake 바인딩을 분리한다.
- [ ] Java/Fragment Android 모듈에 필요한 Manifest, 리소스, Navigation, Gradle 의존성을 연결한다.
- [ ] TODO 또는 `null` 반환 화면은 최소 실행 가능한 UI로 연결하되 화면 업무 계산은 추가하지 않는다.

### Phase 4. CT-05 백업·삭제 통합

- [ ] `BackupClient`와 `DataControlClient`가 구체 Fake가 아닌 포트에 의존하도록 변경한다.
- [ ] APP-11 변경 페이지를 APP-12가 실제 소비하도록 연결한다.
- [ ] 기기·서버 삭제가 모두 완료되기 전 전체 성공을 반환하지 않는 통합 테스트를 추가한다.
- [ ] 서버 모듈의 실행 가능한 빌드와 격리 로컬 실행 구성을 추가하거나, 현재 환경에서 불가능하면 승인된 검증 경계를 명시한다.
- [ ] OS-04/APP-02 실제 하드웨어 식별원 검증은 별도 차단 항목으로 유지한다.

### Phase 5. 공통 빌드·계약 검증

- [ ] Java 17 기준의 공통 Gradle 설정과 `verify-domain`, `verify-data`, `verify-backup`, `verify-ui`, `verify-all` 진입점을 만든다.
- [ ] 기존 domain 14개, device 13개, backup 15개 회귀를 그대로 통과시킨다.
- [ ] CT-01–CT-06의 Fake와 실제 어댑터가 같은 계약 테스트를 통과하게 한다.
- [ ] CP-1 → CP-2 → CP-3 → CP-4 순서의 대표 통합 시나리오를 실행한다.
- [ ] `build-debug`가 가능한 환경이면 디버그 APK를 만들고, 불가능하면 사용자 실행 명령과 원인을 남긴다.
- [ ] Android 14 이상 실제 기기 검증은 사용자 수행 체크리스트로 남기고 성공으로 가장하지 않는다.

## 5. 우선 해결할 충돌

| 우선순위 | 항목 | 해결 방향 |
|---:|---|---|
| P0 | PR #1과 `main`의 NFR 문서 2개 충돌 | 트랙별 요구와 결정을 한 문서에 병합 |
| P0 | CT-03 `CommittedChange` 중복 | device 모델을 기준으로 backup 변환 어댑터 적용 |
| P0 | OS-04 하드웨어 식별원 미검증 | 실제 기능으로 표시하지 않고 위험 게이트 유지 |
| P1 | domain 타입·포트의 외부 접근 불가 | 공개 facade/DTO 또는 전용 어댑터 경계 추가 |
| P1 | UI의 Fake 고정 주입 | 실제 조립 gateway와 test binding 분리 |
| P1 | 루트 빌드·Android 리소스 부재 | 공통 Gradle/Android 모듈과 최소 리소스 구성 |
| P2 | 문서 경로·상태 중복 | 공식 경로와 UOW/트랙 상태를 후속 정리 |

## 6. 완료 조건

- PR #1 결과가 코드·문서 유실 없이 통합되어야 한다.
- 네 트랙 사이에 직접 호환되지 않는 동일 책임 타입은 기준 타입 또는 명시적 어댑터를 가져야 한다.
- 프로덕션 경로가 테스트 Fake를 직접 생성하지 않아야 한다.
- 전체 소스가 한 빌드 정의에서 컴파일되고 자동 테스트가 재현 가능해야 한다.
- 독립 회귀와 새 계약·통합 테스트가 모두 통과해야 한다.
- 실제 기기나 외부 환경이 필요한 항목은 미완료 상태와 사용자 실행 절차가 남아야 한다.
- 위 조건을 충족한 뒤에만 트랙 STEP 02–05 결과를 UOW 통합 완료 후보로 판정한다.

## 7. 결과 기록

각 Phase가 끝날 때 다음을 기록한다.

- 변경 파일과 해결한 CT ID
- 실행 명령과 통과·실패 수
- 남은 실제 기기·서버 위험
- 사용자 빌드 테스트에 필요한 명령과 준비 조건

## 8. 2026-08-26 실행 결과

### 완료

- [x] PR #1이 최신 `main`에 병합된 결과를 기준으로 통합했다.
- [x] 병합 과정에서 비어 버린 UI·backup NFR 및 기술 결정 문서를 Java 통합본으로 복구했다.
- [x] device `AppSession`을 domain 공개 facade로 변환하고 APP-11 기반 domain 저장 어댑터를 추가했다.
- [x] device `CommittedChange.sequence/changeId/owner`를 backup까지 보존하는 CT-03 어댑터를 추가했다.
- [x] `BackupClient`·`DataControlClient`의 구체 Fake 의존을 포트로 교체했다.
- [x] 메모리 서버를 사용하는 실행 가능한 `InProcessBackupBoundary`를 추가했다.
- [x] 프로덕션 UI DI에서 `FakeFeatureGateway` 고정 주입을 제거했다.
- [x] 실제 Usage Access를 읽되 미검증 OS-04 식별원은 `IDENTITY_UNAVAILABLE`로 차단하는 production gateway를 추가했다.
- [x] Java/Fragment 앱의 Manifest, Navigation, 최소 레이아웃, Hilt, 공통 Gradle 모듈을 연결했다.
- [x] 기존 UI 테스트에 동기 LiveData 실행 경계를 추가했다.
- [x] device 13개, domain 14개, backup 15개, UI 26개, 트랙 통합 10개를 공통 `verifyAll`에서 통과시켰다.
- [x] `app-debug.apk` 생성을 확인했다.

### 남은 실제 환경 항목

- [ ] Android 14 이상 기기의 Usage Access·UsageEvent·화면 종료 시나리오
- [ ] OS-04 하드웨어 식별원 접근 가능성·안정성·개인정보 영향 검증
- [ ] Room 기반 APP-11 실제 영속 저장과 프로세스 재시작 회귀
- [ ] Spring Boot/H2 HTTP 경계와 Docker 격리 서버 통합
- [ ] 검증된 익명 식별원을 production gateway에 연결한 뒤 UI-02–UI-08 실제 기능 활성화

위 항목은 현재 코드가 임의 구현으로 성공을 가장하지 않는다. production 앱은 Usage Access를 실제 조회하고,
OS-04 검증 전에는 `IDENTITY_UNAVAILABLE`로 안전하게 차단한다.
