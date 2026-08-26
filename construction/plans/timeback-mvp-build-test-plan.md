# `timeback-mvp` CONSTRUCTION STEP 06 빌드·테스트 계획

## 1. 목적

- 네 Construction 트랙의 STEP 02–05 통합 결과를 하나의 재현 가능한 빌드로 검증한다.
- CP-1 → CP-2 → CP-3 → CP-4 → CP-5 순서의 자동 검증 증거를 남긴다.
- 앱 debug APK와 격리 로컬 백업 서버의 실행·통합 증거를 만든다.
- Android 14 이상 실기기가 없으면 NFR-3.3을 완료로 표시하지 않고 사용자 체크리스트로 남긴다.
- 검증 중 발견한 계약·빌드·실행 오류는 책임을 중복시키지 않는 어댑터 또는 구성 수정으로 보정한다.

## 2. 입력과 보호 대상

- 기준: `inception/application-design/unit-of-work.md`, CP-0 CT-01–CT-06, 통합 PR #6의 소스.
- 기존 사용자의 수정 파일과 미추적 기능 설계 트랙 문서는 stage·수정·삭제하지 않는다.
- Java 17, Android SDK 34, 기존 트랙별 업무 규칙과 테스트 의미를 유지한다.
- OS-04 식별원은 실제 검증 없이 임의 UUID나 설치 ID로 대체하지 않는다.

## 3. 실행 순서

### Phase 1. 사전 상태와 계약 확인

- [x] PR #6의 head와 로컬 커밋이 같은지 확인한다.
- [x] 미해결 충돌 표식, 빈 통합 문서, Gradle 모듈 의존 순환을 확인한다.
- [x] 기존 사용자 작업이 검증·커밋 대상에 섞이지 않았는지 확인한다.

### Phase 2. 전체 자동 검증

- [x] `verifyData`의 기기 데이터 13개 회귀를 실행한다.
- [x] `verifyDomain`의 도메인 14개 회귀를 실행한다.
- [x] `verifyBackup`의 백업·삭제 15개 회귀를 실행한다.
- [x] UI ViewModel 26개 회귀를 실행한다.
- [x] device → domain → APP-11 → backup → 전체 삭제 통합 회귀를 실행한다.
- [x] `verifyAll` 한 명령으로 위 검증이 재현되는지 확인한다.

### Phase 3. 격리 백업 서버 통합

- [x] SRV-01–SRV-03을 HTTP 경계에서 실행할 수 있는 서버 구성을 확인한다.
- [x] 실제 HTTP 클라이언트와 서버가 CT-05의 동일한 요청·응답 의미를 사용하는지 검증한다.
- [x] 백업 성공, 같은 `changeId` 재시도, 실패 후 재시도, 보관 기간, 전체 삭제를 격리 데이터로 검증한다.
- [x] 서버 저장은 사용자 범위로 격리되고 전체 삭제 전 성공을 반환하지 않는지 확인한다.
- [x] Docker 또는 동등한 반복 초기화 명령과 실행 방법을 남긴다.

### Phase 4. Android 빌드와 설치 경계

- [x] `:app:assembleDebug`로 설치 가능한 APK를 생성한다.
- [x] Manifest, Hilt, Navigation, Java 소스가 하나의 debug variant에서 컴파일되는지 확인한다.
- [ ] 연결 기기가 있으면 설치와 권한 허용·거부·회수·UsageEvent 시나리오를 실행한다.
- [x] 연결 기기가 없으면 기기 검증을 통과로 표시하지 않고 정확한 실행 명령과 체크리스트를 남긴다.

### Phase 5. 성능·보안·잔존 위험

- [x] 대표 합성 데이터로 순수 계산 및 저장 경계의 반복 실행 시간을 기록한다.
- [x] 로그·HTTP·테스트 결과에 원본 하드웨어 값이나 사용자 식별 정보가 노출되지 않는지 확인한다.
- [x] Room 영속 저장을 검증하고 OS-04 식별원·실제 제조사 UsageEvent 경계를 분리한다.

### Phase 6. STEP 06 결과 기록

- [x] 빌드 명령, 테스트 수, APK 경로, 서버 실행법을 단일 검증 보고서에 기록한다.
- [x] FR/NFR·US·CP 체크포인트의 통과·부분 통과·미실행을 구분한다.
- [x] `aidlc-state.md`와 append-only audit에 검증된 사실만 반영한다.
- [x] 로컬 STEP 06과 UOW 실제 기기 미완료 상태를 구분해 유지한다.

실제 기기 실행 항목은 연결 기기가 없어 미실행이다. Docker Compose 정의는 해석되었고 실행 JAR의
HTTP/H2 흐름은 통과했으나 Docker 데몬이 꺼져 있어 컨테이너 실행은 보고서에 미실행으로 기록했다.

## 4. 완료 조건

- `./gradlew --no-daemon verifyAll`과 `./gradlew --no-daemon :app:assembleDebug`가 성공한다.
- CP-1–CP-4의 계산·저장·백업·삭제 대표 경로가 자동 검증을 통과한다.
- 격리 HTTP 서버에서 백업·멱등 재시도·보관·전체 삭제가 통과한다.
- debug APK와 전체 소스, 서버·앱 실행 절차, 테스트 요약이 제공된다.
- 실제 기기가 없으면 NFR-3.3 미완료와 사용자 실행 체크리스트가 명시된다.
- 실제 구현이 없는 경계를 fake 성공으로 대체하지 않는다.

## 5. 예상 산출물

- `construction/timeback-mvp/build-test/build-test-report.md`
- 공통 `verifyAll` 및 `assembleDebug` 결과
- 격리 서버 실행·테스트 구성과 필요한 통합 보정
- 갱신된 `aidlc-state.md`와 `aidlc-docs/audit.md`
