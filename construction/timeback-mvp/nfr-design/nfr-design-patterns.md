# STEP 03 비기능 설계 패턴 — track-backup-server (M4)

## 1. 멱등성 패턴

- **changeId 기반 중복 제거**: 서버가 changeId를 키로 저장, 이미 ACCEPTED면 재저장 안 함
- 구현: `BackupStorage.storeBackupItem()` — ConcurrentHashMap + putIfAbsent 논리
- 테스트: 같은 배치 2회 전송 → 데이터 1건 확인

## 2. 부분 성공 패턴

- **항목별 독립 처리**: 배치 내 각 항목을 개별 try-catch로 감싸서 하나 실패해도 나머지 진행
- 구현: `BackupServer.handleBackupSubmit()` — for문 내부 개별 처리
- 응답: `BackupBatchResponse` 에 항목별 상태 반환

## 3. 재시도 패턴

- **지수 백오프**: 1s → 2s → 4s → 8s → 16s → 32s → 60s(max)
- **상태 전이**: PENDING → 전송 시도 → ACCEPTED / RETRYABLE_FAILURE / FAILED
- **retryCount 추적**: 실패할 때마다 증가, 5회 초과 시 UI 알림 트리거
- 구현: `BackupClient.submitPendingChanges()` — 실패 시 incrementRetry()

## 4. 오프라인 내결함 패턴

- **로컬 우선 원칙**: CT-03 성공 → BackupChange 생성(PENDING) → 전송 시도
- 전송 실패해도 로컬 데이터는 그대로 (BR-BACKUP-01)
- 네트워크 복구 시 PENDING/RETRYABLE_FAILURE 상태인 것 재전송

## 5. 전체 삭제 일관성 패턴

- **양쪽 완료 확인**: DeletionJob이 deviceStatus + serverStatus 추적
- `checkCompletion()`: 양쪽 COMPLETED일 때만 completedAt 설정
- 재시도: 같은 jobId로 미완료 쪽만 다시 시도 (BR-DELETE-03)
- 구현: `DataControlClient.retryDeletion()` — 미완료 경계만 재요청

## 6. 익명성 경계 패턴

- **변환 경계 격리**: OS-04(하드웨어) → APP-02(변환) → anonymousUserId만 밖으로
- 서버는 anonymousUserId만 받음, 역추적 불가
- FakeDeviceIdentitySource로 테스트 시에도 원본값 노출 안 함

## 7. 테스트 대역 교체 패턴

- **Fake 객체**: FakeBackupBoundary, FakeDeviceDataAuthority, FakeDeviceIdentitySource
- 모드 전환: ACCEPT_ALL / PARTIAL_FAILURE / OFFLINE / REJECT_ALL
- 실제 구현과 같은 계약 검증 통과해야 함 (CT-06)
