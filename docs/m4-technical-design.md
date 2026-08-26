# M4 백업·통제 트랙 기술 설계서

## 1. 아키텍처 개요

```text
┌─────────────────────────────────────────────────────────┐
│                    Android App (기기)                      │
│                                                           │
│  ┌─────────┐    ┌──────────┐    ┌──────────────────┐    │
│  │  OS-04  │───▶│  APP-02  │───▶│ AnonymousUserId  │    │
│  │ HW 식별원│    │ 변환 경계 │    │   (기기 내부만)   │    │
│  └─────────┘    └──────────┘    └────────┬─────────┘    │
│                                           │              │
│  ┌──────────────────────────────────────┐ │              │
│  │           APP-12 백업 클라이언트       │◀┘              │
│  │  • CommittedChange 수신              │               │
│  │  • BackupChange 생성·상태 관리        │               │
│  │  • BackupBatch 조립·전송·재시도       │               │
│  └──────────────────┬───────────────────┘               │
│                     │                                    │
│  ┌──────────────────┴───────────────────┐               │
│  │           APP-13 데이터 통제           │               │
│  │  • 보관 기간 선택·적용 요청            │               │
│  │  • 전체 삭제 DeletionJob 관리          │               │
│  └──────────────────┬───────────────────┘               │
│                     │                                    │
└─────────────────────┼────────────────────────────────────┘
                      │ CT-05 (HTTPS)
                      ▼
┌─────────────────────────────────────────────────────────┐
│                  백업 서버 (격리 환경)                      │
│                                                           │
│  ┌──────────────────────────────────────────────────┐   │
│  │  SRV-01: 백업 수신                                │   │
│  │  • POST /backup — BackupBatch 수신·멱등 저장      │   │
│  │  • GET  /backup/status — 변경별 상태 조회         │   │
│  └──────────────────────────────────────────────────┘   │
│                                                           │
│  ┌──────────────────────────────────────────────────┐   │
│  │  SRV-02: 보관 기간                                │   │
│  │  • PUT  /retention — 선택 적용                    │   │
│  │  • 만료 데이터 정리 (내부 스케줄)                   │   │
│  └──────────────────────────────────────────────────┘   │
│                                                           │
│  ┌──────────────────────────────────────────────────┐   │
│  │  SRV-03: 전체 삭제                                │   │
│  │  • POST /deletion — 삭제 요청 수락                │   │
│  │  • GET  /deletion/status — 진행 상태 조회         │   │
│  │  • 비동기 처리 후 상태 갱신                        │   │
│  └──────────────────────────────────────────────────┘   │
│                                                           │
│  ┌──────────────────────────────────────────────────┐   │
│  │  공통 저장소                                       │   │
│  │  • anonymousUserId 기준 파티셔닝                   │   │
│  │  • changeId 기반 멱등 키                           │   │
│  └──────────────────────────────────────────────────┘   │
│                                                           │
└─────────────────────────────────────────────────────────┘
```

## 2. 서버 API 설계

### SRV-01: 백업 수신

#### `POST /backup`
- 요청: `BackupBatch` (anonymousUserId + BackupChange[] + 레코드 사본)
- 응답: `BackupItemResult[]`
- 멱등성: 같은 `changeId`는 재처리하지 않고 기존 결과 반환
- 부분 성공: 항목별 독립 처리, 성공/실패 개별 응답

#### `GET /backup/status?userId={anonymousUserId}&changeIds={ids}`
- 응답: 변경별 현재 상태 (ACCEPTED, PENDING, RETRYABLE_FAILURE, FAILED)

### SRV-02: 보관 기간

#### `PUT /retention`
- 요청: `{ anonymousUserId, retentionSelection }`
- 응답: `{ serverStatus: "APPLIED" | "FAILED" }`
- 동작: 만료 대상 데이터 마킹 → 내부 정리 스케줄로 실제 삭제

### SRV-03: 전체 삭제

#### `POST /deletion`
- 요청: `{ anonymousUserId, jobId }`
- 응답: `{ status: "ACCEPTED" }` 또는 오류
- 비동기: 수락 후 백그라운드에서 전체 사용자 데이터 삭제

#### `GET /deletion/status?userId={anonymousUserId}&jobId={jobId}`
- 응답: `{ serverStatus: "PENDING" | "IN_PROGRESS" | "COMPLETED" | "FAILED" }`

## 3. 클라이언트 로직 흐름

### APP-12 백업 클라이언트

```text
1. CommittedChange 수신 (CT-03 변경 통지 구독)
2. BackupChange 레코드 생성 (state=PENDING, retryCount=0)
3. 대기 중인 BackupChange 수집 → BackupBatch 조립
4. 서버 전송 시도
   ├─ 전체 성공: 해당 변경 state=ACCEPTED
   ├─ 부분 성공: 개별 처리
   │   ├─ ACCEPTED → state=ACCEPTED
   │   ├─ RETRYABLE_FAILURE → retryCount++, state 유지
   │   └─ FAILED → state=FAILED
   └─ 네트워크 오류: 전체 PENDING 유지
5. 재시도 스케줄 (지수 백오프 또는 앱 재시작 시)
```

### APP-13 데이터 통제

```text
보관 기간 변경:
1. 사용자 선택 → 로컬 User.retentionSelection 저장
2. 서버 ApplyRetentionSelection 호출
3. 기기·서버 각각 상태 UI에 반영

전체 삭제:
1. 사용자 확정 → DeletionJob 생성 (deviceStatus=PENDING, serverStatus=PENDING)
2. 기기 삭제 실행 (CT-03 DeleteScope)
   ├─ 성공: deviceStatus=COMPLETED
   └─ 실패: deviceStatus=FAILED
3. 서버 삭제 요청 (CT-05 RequestFullDeletion)
   ├─ 수락: serverStatus=IN_PROGRESS
   └─ 실패: serverStatus=FAILED
4. 상태 폴링 (ReadDeletionStatus)
   ├─ COMPLETED: serverStatus=COMPLETED
   └─ FAILED: 재시도 가능 → 같은 jobId로 재요청
5. 양쪽 모두 COMPLETED → completedAt 기록
```

## 4. 핵심 설계 원칙

| 원칙 | 적용 |
|---|---|
| 멱등성 | changeId 기반 — 같은 요청 재전송해도 부작용 없음 |
| 부분 성공 | 묶음 내 항목별 독립 처리, 전체 실패로 롤백하지 않음 |
| 로컬 우선 | 백업 실패가 로컬 기능을 방해하지 않음 |
| 익명성 | 원본 HW 값은 기기 내부 변환 경계 밖으로 나가지 않음 |
| 최종 일관성 | 기기·서버 양쪽 완료까지 진행 상태 추적 |
| 시간 계산 금지 | 서버는 도메인 계산(Context, Baseline 등) 일절 안 함 |

## 5. 오류 처리 전략

| 상황 | 클라이언트 행동 | 서버 행동 |
|---|---|---|
| 네트워크 끊김 | PENDING 유지, 복구 시 재시도 | — |
| 서버 5xx | RETRYABLE_FAILURE, 지수 백오프 | 로그 기록 |
| 중복 changeId | 재전송 | 기존 결과 반환 (멱등) |
| 잘못된 요청 | FAILED 마킹 | 400 + 오류 분류 |
| 삭제 부분 실패 | 같은 jobId로 재시도 | 미완료 대상만 이어서 처리 |

## 6. 테스트 대역 활용

| 대역 | 용도 |
|---|---|
| `FakeAnonymousBackupBoundary` | 서버 응답 시뮬레이션 (성공/부분실패/오프라인) |
| `FakeDeviceIdentitySource` | 식별자 변환 성공/실패 시뮬레이션 |
| `FakeDeviceDataAuthority` | CommittedChange 제공·삭제 결과 시뮬레이션 |
