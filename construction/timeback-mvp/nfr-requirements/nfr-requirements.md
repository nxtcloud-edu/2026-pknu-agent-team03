# STEP 02 비기능 요구사항 — track-backup-server (M4)

## 1. 성능

| ID | 요구 | 측정 기준 |
|---|---|---|
| NFR-PERF-01 | 백업 전송 응답 | 단일 BackupBatch (10변경) < 2초 |
| NFR-PERF-02 | 삭제 요청 수락 | < 500ms (비동기 처리) |
| NFR-PERF-03 | 재시도 간격 | 지수 백오프: 1→2→4→8→최대 60초 |

## 2. 정확성

| ID | 요구 | 검증 |
|---|---|---|
| NFR-ACC-01 | changeId 멱등 | 동일 배치 2회 → 서버 1건 |
| NFR-ACC-02 | 부분 성공 일관성 | 성공 항목만 ACCEPTED |
| NFR-ACC-03 | 삭제 양쪽 완료 조건 | completedAt은 양쪽 COMPLETED 시만 |

## 3. 보안

| ID | 요구 | 구현 |
|---|---|---|
| NFR-SEC-01 | 원본 HW 미전송 | APP-02 변환 후 anonymousUserId만 사용 |
| NFR-SEC-02 | 전송 암호화 | HTTPS TLS 1.2+ |
| NFR-SEC-03 | 역추적 불가 | IP 비저장, 계정 없음 |
| NFR-SEC-04 | 삭제 시 복원 불가 | 물리 삭제 |

## 4. 가용성

| ID | 요구 | 전략 |
|---|---|---|
| NFR-AVAIL-01 | 오프라인 시 로컬 정상 | 백업 실패 ≠ 로컬 실패 |
| NFR-AVAIL-02 | 서버 장애 시 유실 없음 | PENDING 유지 → 복구 후 재시도 |
| NFR-AVAIL-03 | 재시도 5회 초과 시 | UI-08에 사용자 알림 |

## 5. 기술 스택 (팀 합의: Java)

| 항목 | 결정 |
|---|---|
| 언어 | Java 17+ |
| 서버 | Spring Boot 3.x |
| DB (MVP) | H2 인메모리 → PostgreSQL |
| 클라이언트 | Android SDK + OkHttp/Retrofit |
| 로컬 저장 | Room (SQLite) |
| 테스트 | JUnit 5 |
| 빌드 | Gradle |
