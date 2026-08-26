# STEP 05 코드 생성 요약 — track-backup-server (M4)

## 코드 위치

실제 코드: `src/main/java/com/timeback/backup/`
테스트: `src/test/java/com/timeback/backup/`

## 구현 범위 (Phase 1 — 독립 개발)

### contracts/ (CT-05 공통 모델 12개)
- `EntityType.java` — 엔터티 종류 enum
- `EntityOperation.java` — 변경 종류 enum
- `BackupChangeState.java` — 백업 변경 상태 enum
- `BackupItemStatus.java` — 서버 응답 상태 enum
- `DeletionStatus.java` — 삭제 진행 상태 enum
- `RetentionServerStatus.java` — 보관 적용 상태 enum
- `CommittedChange.java` — CT-03 로컬 저장 완료 통지
- `BackupChange.java` — 백업 변경 항목 (changeId 기반)
- `BackupBatch.java` — 서버 전송 묶음
- `BackupBatchResponse.java` — 서버 응답
- `BackupItemResult.java` — 항목별 결과
- `DeletionJob.java` — 전체 삭제 추적
- `DeletionRequest.java` — 삭제 요청
- `DeletionStatusResponse.java` — 삭제 상태 응답
- `RetentionRequest.java` — 보관 요청
- `RetentionApplyResult.java` — 보관 적용 결과

### server/ (SRV-01~03 비즈니스 로직)
- `BackupServer.java` — 백업 수신·보관·삭제 핸들러
- `BackupStorage.java` — 인메모리 저장소 (멱등·삭제 구현)

### client/ (APP-12, APP-13)
- `BackupClient.java` — 백업 전송·재시도·부분성공 처리
- `DataControlClient.java` — 보관 변경·전체 삭제·DeletionJob 관리

### fakes/ (CT-06 테스트 대역)
- `FakeBackupBoundary.java` — 서버 응답 모드 4가지
- `FakeDeviceDataAuthority.java` — CommittedChange 제공·삭제 시뮬
- `FakeDeviceIdentitySource.java` — 식별자 변환 성공/실패

### test/ (단위 테스트 15개)
- `BackupServerTest.java` — 6개 (수신·멱등·상태·보관·삭제·삭제멱등)
- `BackupClientTest.java` — 4개 (전송·부분성공·오프라인·빈상태)
- `DataControlClientTest.java` — 5개 (보관성공·실패·삭제성공·실패·재시도)

## 미구현 (Phase 2 — 의존성 있음)

| 항목 | 의존 대상 | 시점 |
|---|---|---|
| OS-04 실제 하드웨어 접근 | Android 기기 | STEP 06 |
| APP-02 실제 변환 | OS-04 검증 결과 | STEP 06 |
| APP-12 ↔ APP-11 실제 연동 | 1번(device-data) | 머지 후 |
| Spring Boot 실제 서버 | 빌드 환경 구성 | 머지 후 |
| Room DB 실제 저장 | Android 프로젝트 구성 | 머지 후 |
| 통합 테스트 (E2E) | 전체 팀 | STEP 06 |

## 검증 결과

- javac 컴파일: 통과
- 15개 단위 테스트: 전부 통과 (`java -ea`)
- 검증 시나리오: SC-BACKUP-PARTIAL, SC-DELETE-PARTIAL, 멱등, 오프라인 복구
