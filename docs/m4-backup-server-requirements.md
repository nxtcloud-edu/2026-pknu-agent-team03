# M4 백업·통제 트랙 요구사항 명세서

## 1. 개요

| 항목 | 내용 |
|---|---|
| 담당 트랙 | `track-backup-server` (4번) |
| 마일스톤 | M4 — CP-4 백업·통제 |
| 관련 스토리 | US-22, US-23, US-24, US-25 |
| 주 구성 요소 | OS-04, APP-02, APP-12, APP-13, SRV-01, SRV-02, SRV-03 |
| 관련 계약 | CT-05 (익명 백업·삭제), CT-06 (테스트 대역) |
| 기능 요구사항 | FR-10 |
| 비기능 요구사항 | NFR-4.1~NFR-4.4 |

## 2. 유저 스토리별 요구사항

### US-22: 로그인 없는 익명 사용자

- 하드웨어 기반 식별원(OS-04)에서 기기 내부에서만 익명 식별자로 변환한다 (APP-02)
- 원본 하드웨어 값은 변환 경계 밖으로 노출·저장·전송하지 않는다 (BR-IDENTITY-01)
- 변환 실패 시 `BLOCKED(IDENTITY_UNAVAILABLE)` 상태를 반환한다
- 실제 안정성이 확인되지 않으면 임의 대체 방식을 사용하지 않는다 (BR-IDENTITY-02)

### US-23: 자동 백업·실패·재시도

- 로컬 저장 성공 시 `CommittedChange`를 기반으로 `BackupChange`를 생성한다
- `BackupBatch`로 묶어 서버(SRV-01)에 전송한다
- 백업 실패·오프라인은 로컬 저장을 되돌리지 않는다 (BR-BACKUP-01)
- 같은 `changeId` 재전송 시 중복 적용하지 않는다 (BR-BACKUP-02)
- 묶음의 일부만 성공하면 성공한 것만 `ACCEPTED`, 실패한 것은 `PENDING` 유지 (BR-BACKUP-03)
- 재시도 횟수를 `retryCount`로 추적한다

### US-24: 보관 기간 적용

- 사용자가 선택한 `RetentionSelection`을 기기·서버에 일관 적용한다 (BR-RETENTION-01)
- 원본 UsageEvent와 정책 대상 파생 데이터 모두 적용 대상이다
- 보관 선택지 목록은 상세 설계에서 결정한다 (후속 과제)
- 적용 결과는 `RetentionApplyResult`로 기기/서버 각각 상태를 반환한다

### US-25: 기기·서버 전체 삭제 일관성

- 전체 삭제는 `DeletionJob`으로 추적한다
- 대상: 원본·파생·활동·Context·목표·회수 기록 + 같은 익명 식별자의 서버 백업 (BR-DELETE-01)
- 기기와 서버 모두 완료 전까지 전체 삭제를 완료로 표시하지 않는다 (BR-DELETE-02)
- 삭제 재시도는 같은 `DeletionJob`의 미완료 경계를 이어간다 (BR-DELETE-03)
- `completedAt`은 양쪽 모두 `COMPLETED`일 때만 존재한다

## 3. 구성 요소 책임

| 구성 요소 | 책임 |
|---|---|
| OS-04 | 하드웨어 식별원 접근 (Android API) |
| APP-02 | 하드웨어 값 → 익명 식별자 변환 (기기 내부) |
| APP-12 | 백업 클라이언트 — BackupBatch 조립·전송·재시도·상태 관리 |
| APP-13 | 보관 기간 적용·전체 삭제 요청·추적 |
| SRV-01 | 서버 — 백업 수신·저장·멱등 처리 |
| SRV-02 | 서버 — 보관 기간 적용 |
| SRV-03 | 서버 — 전체 삭제 처리 |

## 4. 핵심 데이터 모델

| 엔터티 | 핵심 필드 | 상태값 |
|---|---|---|
| `BackupChange` | changeId, entityType, entityId, operation, occurredAt, state, retryCount | PENDING, ACCEPTED, RETRYABLE_FAILURE, FAILED |
| `BackupBatch` | anonymousUserId, changes[] | — |
| `BackupItemResult` | changeId, status, error | ACCEPTED, RETRYABLE_FAILURE, FAILED |
| `RetentionApplyResult` | retentionSelection, deviceStatus, serverStatus | — |
| `DeletionJob` | jobId, requestedAt, deviceStatus, serverStatus, completedAt | PENDING, IN_PROGRESS, COMPLETED, FAILED |

## 5. 의존성 분석

### 독립 개발 가능 (다른 트랙 완성 불필요)

| 범위 | 이유 |
|---|---|
| SRV-01 백업 수신 API | CT-05 계약만으로 서버 로직 구현 가능 |
| SRV-02 보관 기간 적용 API | 서버 독립 로직 |
| SRV-03 전체 삭제 API | 서버 독립 로직 |
| APP-12 백업 클라이언트 핵심 로직 | CT-05·CT-06 가짜 서버 응답으로 개발 가능 |
| APP-13 삭제·보관 클라이언트 로직 | CT-05·CT-06 가짜 응답으로 개발 가능 |
| 단위 테스트 전체 | CT-06 테스트 대역 사용 |

### 의존성 있음 (나중에 개발)

| 범위 | 의존 대상 |
|---|---|
| OS-04 실제 하드웨어 식별원 접근 | Android 기기·API 실제 검증 필요 |
| APP-02 실제 변환 구현 | OS-04 안정성 검증 결과 (위험 게이트) |
| APP-12 ↔ APP-11 실제 CommittedChange 연동 | track-device-data의 CT-03 구현 |
| 통합 테스트 (기기+서버) | 모든 트랙 CP-1~CP-3 완료 필요 |

## 6. 적용 규칙 요약

- BR-IDENTITY-01, BR-IDENTITY-02: 익명 식별자 보안
- BR-BACKUP-01~03: 백업 부분 성공·멱등·로컬 비간섭
- BR-RETENTION-01: 보관 일관 적용
- BR-DELETE-01~03: 전체 삭제 완료 조건·재시도
- BR-DATA-01: 기기가 기준, 서버는 백업 사본
- BR-TIME-06: 서버는 자체 시간 계산 금지

## 7. 검증 시나리오

| 시나리오 | 검증 관찰점 |
|---|---|
| SC-BACKUP-PARTIAL | 성공만 ACCEPTED, 실패는 PENDING 유지 |
| SC-DELETE-PARTIAL | 양쪽 완료 전 전체 미완료 |
| 멱등 재전송 | 같은 changeId 두 번 보내도 중복 없음 |
| 오프라인 → 복구 | PENDING 유지 → 재시도 → ACCEPTED |
| 보관 기간 적용 | 기기·서버 양쪽 상태 일치 |
