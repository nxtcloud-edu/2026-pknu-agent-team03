# `timeback-mvp` STEP 02 통합 비기능 요구사항

## 1. 문서 상태

- 단계: CONSTRUCTION STEP 02 비기능 요구
- 범위: `track-ui`, `track-backup-server` 통합 및 공통 CT-04·CT-05 경계
- 상태: 트랙별 확정 결과 통합본
- 실행 원칙: 트랙별 STEP 02–05 선행 완료 후 UOW 통합 빌드·검증

`track-device-data`의 세부 요구는 `tracks/track-device-data.md`,
`track-domain-engine`의 세부 요구는 `aidlc-docs/construction/timeback-mvp/nfr-requirements/`를 함께 적용한다.

## 2. UI 성능

| 요구 ID | 조건 | 측정 방법 |
|---|---|---|
| NFR-1.1-UI-01 | 화면 전환 뒤 ViewModel의 첫 `CONTENT` 상태 도달까지 200ms 이내(Fake 기준) | ViewModel 상태 전이 테스트 |
| NFR-1.1-UI-02 | 실제 `FeatureGateway` 연결 시 새로고침 뒤 `CONTENT` 상태 도달까지 5초 이내 | 통합 테스트 타임아웃 |
| NFR-1.1-UI-03 | `REFRESHING` 중 기존 데이터를 유지하고 새 결과 도착 뒤 교체한다 | UI 상태 테스트 |
| NFR-1.1-UI-04 | cold start가 아닌 화면 재진입은 캐시된 상태를 우선 표시한다 | Navigation/Robolectric 테스트 |

## 3. UI 정확성·보안

| 요구 ID | 조건 | 검증 |
|---|---|---|
| NFR-2-UI-01 | UI는 시간 계산·중첩·분할을 수행하지 않고 기능 경계 결과만 표시한다 | ViewModel·gateway 코드 검토 |
| NFR-2-UI-02 | 기능 경계가 제공한 `Duration`, `TimeRange`를 다시 계산하지 않는다 | gateway 결과와 화면 모델 비교 |
| NFR-2-UI-03 | `EMPTY`와 계산된 0을 구분한다 | 상태 분기 테스트 |
| NFR-4-UI-01 | UI는 원본 UsageEvent 저장소를 직접 호출하지 않는다 | 의존성 검사 |
| NFR-4-UI-02 | 화면 내용·앱 내부 콘텐츠·위치 정보를 수집하거나 표시하지 않는다 | fixture·로그 검사 |
| NFR-4-UI-03 | 권한 미허용 시 주요 화면 진입을 `BLOCKED`로 제한한다 | UI-01 상태 테스트 |

## 4. UI 테스트·유지보수

| 요구 ID | 조건 | 검증 도구 |
|---|---|---|
| NFR-3.2-UI-01 | UI-01–UI-08의 주요 `LOADING`, `CONTENT`, `EMPTY`, `BLOCKED`, `ERROR` 상태를 검증한다 | JUnit 5, Robolectric |
| NFR-3.2-UI-02 | 각 화면의 대표 조회·생성·수정 동작을 ViewModel 테스트로 검증한다 | JUnit 5 |
| NFR-3.2-UI-03 | CT-06 고정 결과에 대한 UI 반응을 검증한다 | `FakeFeatureGateway` 계약 테스트 |
| NFR-3.2-UI-04 | 화면 간 영향 재조회를 통합 테스트로 검증한다 | Robolectric |
| NFR-5.1-UI-01 | 화면별 패키지를 분리하고 공통 의존은 gateway·공통 모델로 제한한다 | 패키지 의존성 검사 |
| NFR-5.2-UI-01 | Java `ScreenState` 계층은 CT-04 상태와 일치해야 한다 | 계약 테스트 |
| NFR-5.2-UI-02 | Fake와 실제 gateway는 같은 `FeatureGateway` 계약을 구현한다 | 공유 계약 테스트 |

## 5. 백업 성능·가용성

| 요구 ID | 조건 | 측정 기준 |
|---|---|---|
| NFR-PERF-01 | 변경 10건의 `BackupBatch` 응답 | 2초 이내 |
| NFR-PERF-02 | 전체 삭제 요청 수락 | 500ms 이내, 완료와 구분 |
| NFR-PERF-03 | 재시도 간격 | 1→2→4→8→최대 60초 지수 백오프 |
| NFR-AVAIL-01 | 백업 실패는 성공한 로컬 저장을 되돌리지 않는다 | 오프라인 계약 테스트 |
| NFR-AVAIL-02 | 서버 장애 시 변경을 `PENDING`으로 보존하고 복구 후 재시도한다 | 재시작·재시도 테스트 |
| NFR-AVAIL-03 | 재시도 5회 초과 상태를 UI-08에 제공한다 | CT-04·CT-05 통합 테스트 |

## 6. 백업 정확성·보안

| 요구 ID | 조건 | 검증 |
|---|---|---|
| NFR-ACC-01 | 같은 `changeId` 재전송은 서버에 한 번만 적용한다 | 동일 배치 재전송 테스트 |
| NFR-ACC-02 | 부분 성공 시 성공 항목만 `ACCEPTED`로 전이한다 | 항목별 응답 테스트 |
| NFR-ACC-03 | 기기와 서버가 모두 완료되기 전 삭제 `completedAt`을 만들지 않는다 | 삭제 부분 실패 테스트 |
| NFR-SEC-01 | 원본 하드웨어 식별원은 APP-02 밖으로 노출·저장·전송하지 않는다 | 경계·로그 검사 |
| NFR-SEC-02 | 실제 네트워크 전송은 TLS 1.2 이상을 사용한다 | 로컬 통합 설정 검사 |
| NFR-SEC-03 | 서버는 계정·원본 하드웨어 값 없이 변환된 익명 식별자만 사용한다 | 요청·저장 모델 검사 |
| NFR-SEC-04 | 삭제 완료 데이터는 정상 API로 복구할 수 없어야 한다 | 삭제 후 조회 테스트 |

## 7. 통합 완료 조건

- UI는 실제 `FeatureGateway`와 Fake 양쪽에서 같은 계약 테스트를 통과한다.
- APP-12는 APP-11이 제공한 안정적인 변경 식별자를 보존한다.
- 전체 삭제는 기기·서버 결과를 개별 추적하고 양쪽 완료 뒤에만 완료된다.
- OS-04 하드웨어 식별원은 Android 14 이상 대상 기기 검증 전까지 미완료 위험으로 남긴다.
- 공통 빌드에서 UI·device·domain·backup 회귀와 CT-01–CT-06 통합 검증을 재현할 수 있어야 한다.
