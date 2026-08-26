# M4 백업·통제 트랙 개발 작업 체크리스트

## 개발 순서 원칙

- 의존성 없는 서버 로직부터 → 클라이언트 로직 → 통합 순서
- 각 항목마다 단위 테스트를 함께 작성
- CT-06 테스트 대역(FakeAnonymousBackupBoundary 등)을 먼저 만들어 독립 검증

---

## Phase 1: 독립 개발 (의존성 없음)

### 1-1. 테스트 대역 정의
- [ ] `FakeAnonymousBackupBoundary` 구현
  - 전부 수락 / 일부 실패 / 오프라인 응답 모드
- [ ] `FakeDeviceIdentitySource` 구현
  - 변환 성공 / 실패 모드
- [ ] `FakeDeviceDataAuthority` (백업 트랙이 사용하는 부분만)
  - CommittedChange 제공 / 삭제 성공·실패 모드

### 1-2. 서버 — SRV-01 백업 수신
- [ ] `POST /backup` 엔드포인트
  - BackupBatch 파싱
  - changeId 기반 멱등 저장
  - 항목별 독립 처리 → BackupItemResult[] 응답
- [ ] `GET /backup/status` 엔드포인트
  - 변경별 상태 조회
- [ ] 단위 테스트
  - 정상 수락
  - 중복 changeId 멱등성
  - 부분 성공 시나리오

### 1-3. 서버 — SRV-02 보관 기간
- [ ] `PUT /retention` 엔드포인트
  - retentionSelection 저장
  - 만료 대상 마킹 로직
- [ ] 단위 테스트
  - 정상 적용
  - 잘못된 selection 거부

### 1-4. 서버 — SRV-03 전체 삭제
- [ ] `POST /deletion` 엔드포인트
  - DeletionJob 수락·생성
  - 비동기 삭제 시작
- [ ] `GET /deletion/status` 엔드포인트
  - 진행 상태 반환
- [ ] 삭제 처리 로직
  - anonymousUserId 기준 전체 데이터 삭제
  - 완료 시 상태 갱신
- [ ] 단위 테스트
  - 정상 삭제 완료
  - 중복 요청 (같은 jobId) 멱등성
  - 부분 실패 → 재시도

### 1-5. 클라이언트 — APP-12 백업 로직 (가짜 서버 사용)
- [ ] BackupChange 생성 로직
  - CommittedChange → BackupChange 변환
  - state=PENDING, retryCount=0
- [ ] BackupBatch 조립 로직
  - 대기 중인 변경 수집·묶음 생성
- [ ] 전송·응답 처리 로직
  - 성공: state=ACCEPTED
  - 부분 성공: 항목별 처리
  - 오프라인: PENDING 유지
- [ ] 재시도 스케줄 로직
  - retryCount 증가
  - 지수 백오프 또는 앱 재시작 트리거
- [ ] 단위 테스트 (FakeAnonymousBackupBoundary 사용)
  - 전체 성공
  - 부분 성공
  - 오프라인 → 복구 → 재시도 성공
  - 멱등 재전송

### 1-6. 클라이언트 — APP-13 데이터 통제 (가짜 서버 사용)
- [ ] 보관 기간 변경 로직
  - 로컬 선택 저장
  - 서버 요청·상태 반영
- [ ] 전체 삭제 로직
  - DeletionJob 생성
  - 기기 삭제 실행 (FakeDeviceDataAuthority)
  - 서버 삭제 요청
  - 상태 폴링·갱신
  - 양쪽 COMPLETED → completedAt 기록
- [ ] 단위 테스트 (FakeAnonymousBackupBoundary 사용)
  - 보관 적용 성공/실패
  - 전체 삭제 정상 완료
  - 서버 삭제 실패 → 재시도 → 완료
  - 기기 삭제 실패 케이스

---

## Phase 2: 의존성 있음 (나중에 개발)

### 2-1. OS-04 + APP-02 익명 식별자
- [ ] 실제 Android 하드웨어 식별원 접근
- [ ] 기기 내부 변환 로직
- [ ] Android 14+ 안정성 검증 (위험 게이트)
- [ ] 검증 실패 시 → 요구사항 변경 승인 요청

### 2-2. APP-12 ↔ APP-11 실제 연동
- [ ] 실제 CommittedChange 구독 연결
- [ ] 실제 저장소에서 레코드 사본 조회

### 2-3. 통합 테스트
- [ ] 클라이언트 ↔ 실제 서버 연동
- [ ] 기기 + 서버 전체 삭제 E2E
- [ ] 백업 → 보관 적용 → 삭제 전체 시나리오

---

## 완료 기준

- [ ] Phase 1 전체 단위 테스트 통과
- [ ] SC-BACKUP-PARTIAL 시나리오 검증
- [ ] SC-DELETE-PARTIAL 시나리오 검증
- [ ] 멱등 재전송 검증
- [ ] 오프라인 → 복구 시나리오 검증
- [ ] 코드가 temp-clone 내 충돌 없는 파일로만 구성됨
