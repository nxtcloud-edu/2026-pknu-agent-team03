# `feature/track-device-data` 진행 문서

## 1. 문서 목적과 현재 상태

이 문서는 `timeback-mvp` CONSTRUCTION에서 기기 데이터 트랙을 담당하는 팀원의 진행 기준이다. 별도 작업 단위나 별도 AI-DLC 단계가 아니다.

| 항목 | 값 |
|---|---|
| 브랜치 | `feature/track-device-data` |
| 책임 트랙 | `track-device-data` |
| 담당자 | 1번 팀원 |
| 문서 상태 | STEP 01–05 트랙 산출물 완료 후보 |
| 실제 트랙 진행 | 상세·NFR·기술 설계와 독립 코드·JVM 검증 완료 · 실제 통합 대기 |
| 공통 선행 조건 | CP-0의 CT-01–CT-06과 이 문서가 들어간 공통 기준 커밋 |
| 현재 Git 경계 | `feature/track-device-data`에서 PR 제출 대상이며 `main` 병합은 대기 |
| 종료 판정 | 트랙 독립 체크리스트 충족, CT 변경 합의·Room·다른 트랙 실제 연결은 통합 검토에서 완료 |

## 2. 소유 범위

### 2.1 주 소유 구성 요소

- OS-01, OS-02, OS-03, OS-05
- APP-01, APP-03, APP-04, APP-11

### 2.2 스토리 배정

| 구분 | 스토리 | 이 트랙의 결과 |
|---|---|---|
| 주 구현 | US-01–US-03 | 권한 상태와 접근 제한, 원본 이벤트 수집·보존, 세션 연결·보정·자정 분할 |
| 지원 | US-04–US-09, US-12, US-16–US-19, US-22–US-25 | 실제 입력, 시간 경계, 저장·조회·삭제·변경 통지를 주 구현 트랙에 연결 |

지원 스토리의 주 구현 책임은 가져오지 않는다.

## 3. 포함·금지 범위

### 포함

- 현재 Usage Access 상태, 설정 복귀 후 재조회, 미허용 시 수집 차단 흐름을 구체화한다.
- 승인된 필드만 가진 UsageEvent의 수집 범위와 조회 경계를 정의한다.
- Foreground·Background 사건 쌍, 종료 쌍이 없는 세션, 다음 앱 전환, 화면 종료, 자정 분할 결과를 구체화한다.
- CT-03을 따르는 로컬 저장·조회·삭제와 `CommittedChange` 통지 책임을 구체화한다.
- 기기 현지 시간대와 시작 포함·종료 미포함 구간 규칙을 모든 시간 경계에 적용한다.

### 금지

- APP-05–APP-09의 Context·Baseline·목표·회수 계산을 이 트랙에서 재구현하지 않는다.
- UI 표시 규칙과 백업 서버 정책을 소유하지 않는다.
- 화면 내용, 앱 내부 콘텐츠, 위치, 자동 오프라인 활동을 수집 항목으로 추가하지 않는다.
- 기술 스택, OS API 사용법, DB 스키마를 STEP 01에서 확정하지 않는다.

## 4. 공통 계약 입력·출력

| CT | 역할 | 이 트랙이 하는 일 |
|---|---|---|
| CT-01 | 주 제공자·소비자 | UsageEvent와 AppSession을 제공하고 Activity·Context 저장 시 같은 구간·식별 규칙을 사용 |
| CT-02 | 저장 지원 | 도메인이 제공한 지표·목표·회수 값의 저장·조회 경계만 제공 |
| CT-03 | 주 제공자 | 로컬 명령 결과, 조회 결과, 삭제 범위, `CommittedChange`를 제공 |
| CT-04 | 상태 제공 | 권한·세션·Timeline에 필요한 상태를 UI의 기능 경계에 제공 |
| CT-05 | 통합 소비 | 백업·삭제 트랙이 반환한 상태와 기기 삭제 범위를 연결 |
| CT-06 | 검증 사용자 | 대역과 공통 시나리오로 실제 다른 브랜치 없이 상세 설계를 검증 |

## 5. 의존성 판정

### 5.1 결론

- 다른 기능 브랜치에 대한 **시작 의존성: 없음**
- CP-0가 포함된 공통 기준 커밋에 대한 **Git 시작 의존성: 있음**
- 도메인·UI·백업의 실제 결과에 대한 **최종 통합 의존성: 있음**

| 종류 | 대상 | 필요 시점 | 없을 때의 판정 |
|---|---|---|---|
| 시작 필수 | 공통 CT-01, CT-03, CT-06과 소유 구성 요소 배정 | 브랜치 작업 전 | 시작 불가 |
| 대역 대체 가능 | `FakeUsageEventSource`, `FakeScreenStateSource`, `ControlledTimeSource`, `FakeDeviceDataAuthority` | 다른 트랙의 실제 결과가 없을 때 | 병렬 진행 가능 |
| 통합 필수 | 도메인의 실제 AppSession 소비·저장 값, UI-01·Timeline 접근, 백업의 `CommittedChange`·`DeleteScope` 소비 | 해당 CP 완료 전 | 트랙 단독 작업은 가능하나 통합 완료 불가 |
| 하위 소비자 | 도메인, UI, 백업 서버 | CT-01·CT-03 결과 변경 시 | 변경 영향 통보 필수 |
| 후속 단계 위험 | 실제 대상 기기의 이벤트·화면 상태·시간 차이, 저장 기술 선택 | STEP 02 이후 | STEP 01에서 임의 확정 금지 |

### 5.2 Git 시작 조건

1. `main`에 CP-0 문서와 네 진행 문서가 포함된 하나의 공통 기준 커밋이 있어야 한다.
2. `feature/track-device-data`는 그 정확한 커밋에서 생성한다.
3. 이 조건은 다른 트랙 작업 완료를 기다리라는 뜻이 아니다.

## 6. STEP 01 상세 기능 설계 체크리스트

아래는 이 문서 생성 작업이 아니라 트랙 담당자가 실제로 수행할 작업이다.

- [x] APP-01의 현재 권한 상태, 설정 이동, 복귀 후 재조회, 차단 결과를 상세화한다.
- [x] APP-03의 수집 조회 구간과 중복·경계 사건 처리 결과를 정의한다.
- [x] 허용된 UsageEvent 필드와 금지 데이터를 명시한다.
- [x] APP-04의 정상 쌍, 종료 미존재, 다음 앱 전환, 화면 종료 결과를 정의한다.
- [x] OS-05 기준의 자정 분할과 분할 전후 합계 보존을 정의한다.
- [x] APP-11의 저장·조회·삭제 명령별 성공·빈 결과·실패 결과를 정의한다.
- [x] 로컬 저장 성공 뒤에만 발행하는 `CommittedChange`를 구체화한다.
- [x] 전체 삭제에서 기기가 소유하는 `DeleteScope` 책임을 구체화한다.
- [x] 로컬 저장 실패가 성공으로 통지되지 않는 오류 흐름을 정의한다.
- [x] CT-06 시나리오와 이 트랙 상세 결과를 연결한다.
- [x] 주·지원 스토리와 FR·NFR·구성 요소 추적을 검증한다.
- [x] 공식 기능 설계 문서의 자기 소유 섹션만 작성한다.

## 7. CT-06 대역과 고정 시나리오

| 대역·시나리오 | 이 트랙의 확인 대상 |
|---|---|
| `FakeUsageAccessGateway` | 허용·미허용·조회 실패·설정 이동과 복귀 재조회 |
| `FakeUsageEventSource` | 요청 기간과 반환 사건, 미허용 시 미호출 |
| `FakeScreenStateSource` | 화면 종료가 열린 세션의 종료 근거로 사용됨 |
| `ControlledTimeSource` | 현재 시각, 현지 시간대, 자정 경계 재현 |
| `FakeDeviceDataAuthority` | 저장 성공·실패, 조회, 삭제, 변경 통지 관찰 |
| `SC-PERMISSION-BLOCKED` | 수집 미호출과 접근 차단 |
| `SC-SESSION-PAIRED`, `SC-SESSION-OPEN` | 세션 연결·보정과 근거 사건 |
| `SC-MIDNIGHT-SPLIT` | 분할 구간 합계 보존 |
| `SC-BACKUP-PARTIAL`, `SC-DELETE-PARTIAL` | 변경 통지·기기 삭제 경계가 실패에도 유지됨 |
| `DD-ACCESS-01`–`DD-ACCESS-04` | 미허용·설정 복귀·회수·권한 조회 실패 |
| `DD-COLLECT-01`–`DD-COLLECT-06` | 허용 필드, 빈 결과, 저장·소스 실패, 경계 중복 방지 |
| `DD-SESSION-01`–`DD-SESSION-05` | 정상 쌍, 다음 앱, 화면 종료, 열린 후보, 자정 분할 |
| `DD-DATA-01`–`DD-DATA-04` | 원자 저장, 사용자 격리, 변경 커서, 파생 레코드 교체 제안 |

## 8. M1–M4 기여와 통합 시점

| 마일스톤 | 기여 | 실제 통합 상대 |
|---|---|---|
| M1 | US-01–US-03 주 결과와 US-04–US-10의 실제 입력·저장 | 도메인, UI |
| M2 | Baseline·리포트에 필요한 제어 시간·기간 조회·저장 | 도메인, UI |
| M3 | Goal·RecoveredTime 저장·조회와 시간 경계 | 도메인, UI |
| M4 | `CommittedChange`, 기기 삭제, 백업·삭제 상태 연결 | 백업 서버, UI |

이 트랙은 M2–M4의 상위 업무 규칙을 소유하지 않지만 실제 저장·시간 경계를 통합해야 한다.

## 9. 공식 기능 설계 문서 소유 섹션

| 문서 | 고유 섹션 제목 |
|---|---|
| `business-logic-model.md` | `Track Detail — device-data` |
| `business-rules.md` | `Track Rules — device-data` |
| `domain-entities.md` | `Track Data — device-data` |
| `frontend-components.md` | `Track UI Support — device-data` |

CP-0 공통 섹션과 다른 트랙의 고유 섹션은 직접 수정하지 않는다.

## 10. 완료 증거와 병합 전 확인

- [x] 모든 주 소유 구성 요소가 상세 흐름·상태·오류·추적에 연결됐다.
- [x] US-01–US-03의 인수 결과와 CT-06 검증 결과가 연결됐다.
- [x] 다른 트랙이 소비할 CT-01·CT-03 결과와 실패 상태가 명확하다.
- [x] 도메인 계산, UI 표시 규칙, 서버 정책이 승인 없이 들어가지 않았다.
- [x] CP-0 공통 섹션과 다른 트랙 소유 섹션을 임의 수정하지 않았다.
- [x] 통합 필수 항목의 실제 연결 결과 또는 미해결 사유가 기록됐다.

## 11. 계약 변경·차단 절차

- CT 변경이 필요하면 CT ID, 요구사항 근거, 영향 트랙, CT-06 기대 결과 변경을 먼저 기록한다.
- 합의 전에 공통 계약을 조용히 바꾸지 않는다.
- 시작 필수 입력이 없거나 승인된 수집 범위를 넘어야만 진행할 수 있으면 차단으로 기록한다.
- 기기·OS·저장 기술 결정이 필요하면 STEP 02 위험으로 이관하고 STEP 01 사실로 단정하지 않는다.

## 12. 상세 설계 결과와 통합 대기 항목

### 12.1 공식 산출물 반영

| 문서 | 반영 섹션 | 결과 |
|---|---|---|
| `business-logic-model.md` | `Track Detail — device-data` | APP-01·APP-03·APP-04·APP-11 흐름과 DD 검증 사례 |
| `business-rules.md` | `Track Rules — device-data` | 권한·수집·세션·저장 불변조건 |
| `domain-entities.md` | `Track Data — device-data` | 권한·수집·열린 세션·새로고침 모델과 CT-03 변경 제안 |
| `frontend-components.md` | `Track UI Support — device-data` | UI-01 및 UI-02·UI-03 새로고침 제공 경계 |

### 12.2 계약 변경 제안

| 대상 계약 | 제안 | 검토 트랙 | 현재 상태 |
|---|---|---|---|
| CT-03 | 모든 명령·조회에 `DataOwnerScope` 명시 | 도메인·백업 서버 | 합의 대기 |
| CT-03 | 파생 기간의 원자 교체 `ReplacePeriodRecords` | 도메인 | 합의 대기 |
| CT-03 | 사용자별 순서형 변경 커서와 `CommittedChangePage` | 백업 서버 | 합의 대기 |
| CT-04·CT-06 | 권한·수집 실패와 신규 사건 0의 고정 기능 결과 | UI | 합의 대기 |

제안은 공통 CP-0 섹션을 직접 수정하지 않고 각 device-data 소유 섹션에만 기록했다.

### 12.3 후속 통합·검증

| 항목 | 현재 상태 | 완료 시점 |
|---|---|---|
| 도메인의 실제 AppSession 소비와 파생 저장 연결 | 대기 | CP-1–CP-3 통합 |
| UI의 실제 권한·새로고침 상태 소비 | 대기 | CP-1 통합 |
| 백업의 실제 변경 통지·기기 삭제 소비 | 대기 | CP-4 통합 |
| Android 14 이상 권한·UsageEvent 실기기 검증 | 미실행·통과로 기록하지 않음 | STEP 06 또는 승인된 기기 검증 시점 |
| `feature/track-device-data` PR 제출과 `main` 병합 | PR 제출 대상·병합 대기 | 팀 Git 절차에 따라 수행 |

## 13. STEP 02–05 병렬 진행 갱신

다른 책임 트랙이 공통 계약과 가짜 구현을 사용해 자기 STEP 02–05 기여를 독립 완성한 진행 방식에 맞춰, `track-device-data`도 공식 UOW 게이트와 트랙 작업을 구분해 후속 단계를 진행했다. 앞 절의 “STEP 02 시작 대기”는 STEP 01 작성 당시의 판단이며 이 절이 현재 상태를 대체한다.

| 단계 | 결과 | 산출물·증거 |
|---|---|---|
| STEP 02 | 완료 후보 | `nfr-requirements/tracks/track-device-data.md` — Kotlin, Android 14, Room, Coroutines, 품질 조건 |
| STEP 03 | 완료 후보 | `nfr-design/tracks/track-device-data.md` — OS 포트, 원자 수집·교체, 오류·테스트 설계 |
| STEP 04 | 적용 조건상 건너뜀 | `infrastructure-design/tracks/track-device-data.md` — 순수 Android 내부 트랙 근거와 통합 검토 |
| STEP 05 | 독립 완료 후보 | `src/main/java/com/timeback/device`, `src/test/java/com/timeback/device` — Android SDK 34 컴파일과 자동 검증 |

현재 판정은 `track-device-data STEP 01–05 독립 완료 후보`다. 이는 `timeback-mvp`의 단계별 공식 게이트를 네 번 통과했다는 뜻이 아니다. CT-03 확정, Room 실제 저장, UI·도메인·백업 어댑터, 실제 Android 기기 NFR-3.3과 전체 Gradle 빌드는 네 트랙 병합 후 통합해야 한다.
