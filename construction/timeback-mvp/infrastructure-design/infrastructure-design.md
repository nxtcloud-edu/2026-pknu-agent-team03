# STEP 04 인프라 설계 — track-backup-server (M4)

## 1. 배포 아키텍처

```
┌─────────────────────────────────────────┐
│            격리 백업 서버                  │
│                                           │
│  ┌─────────────────────────────────────┐ │
│  │  Spring Boot App (Java 17)          │ │
│  │  - POST /api/backup                 │ │
│  │  - GET  /api/backup/status          │ │
│  │  - PUT  /api/retention              │ │
│  │  - POST /api/deletion               │ │
│  │  - GET  /api/deletion/status        │ │
│  └──────────────┬──────────────────────┘ │
│                 │                         │
│  ┌──────────────▼──────────────────────┐ │
│  │  PostgreSQL 15 (또는 H2 MVP)        │ │
│  │  - backup_changes 테이블            │ │
│  │  - retention_settings 테이블        │ │
│  │  - deletion_jobs 테이블             │ │
│  └─────────────────────────────────────┘ │
│                                           │
│  Docker Compose로 패키징                  │
└─────────────────────────────────────────┘
         ▲
         │ HTTPS (TLS 1.2+)
         │
┌────────┴──────────────────────────┐
│  Android 기기 (API 34+)           │
│  - APP-12 BackupClient            │
│  - APP-13 DataControlClient       │
│  - Room DB (BackupChange 상태)    │
└───────────────────────────────────┘
```

## 2. 서버 실행 환경

| 항목 | MVP | 운영 (추후) |
|---|---|---|
| 컨테이너 | Docker Compose | K8s 또는 Cloud Run |
| DB | H2 인메모리 | PostgreSQL 15 |
| 포트 | 8080 | 443 (리버스 프록시) |
| TLS | 개발 시 HTTP | Nginx/Caddy 리버스 프록시 |
| 로깅 | 콘솔 stdout | 구조화 JSON → 로그 수집기 |

## 3. 데이터 저장 설계

### backup_changes 테이블
```sql
CREATE TABLE backup_changes (
    change_id       VARCHAR(64) PRIMARY KEY,
    anonymous_user_id VARCHAR(128) NOT NULL,
    entity_type     VARCHAR(32) NOT NULL,
    entity_id       VARCHAR(64) NOT NULL,
    operation       VARCHAR(16) NOT NULL,
    occurred_at     BIGINT NOT NULL,
    status          VARCHAR(32) NOT NULL DEFAULT 'ACCEPTED',
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_backup_user ON backup_changes(anonymous_user_id);
```

### retention_settings 테이블
```sql
CREATE TABLE retention_settings (
    anonymous_user_id VARCHAR(128) PRIMARY KEY,
    selection         VARCHAR(32) NOT NULL,
    updated_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### deletion_jobs 테이블
```sql
CREATE TABLE deletion_jobs (
    job_id            VARCHAR(64) PRIMARY KEY,
    anonymous_user_id VARCHAR(128) NOT NULL,
    requested_at      BIGINT NOT NULL,
    server_status     VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    completed_at      BIGINT,
    created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## 4. 보관 기간 정리 스케줄

- 서버 내부 스케줄러 (Spring @Scheduled)
- 주기: 매일 03:00 UTC
- 동작: retention_settings 기준으로 만료된 backup_changes 삭제
- 기기 데이터는 서버가 원격 삭제하지 않음 (사용자 주도)

## 5. 네트워크 정책

- 서버 → 기기 Push 없음 (클라이언트 Pull 방식)
- 서버 IP 고정 또는 DNS (팀 합의 시 결정)
- Rate limiting: MVP에서는 미적용, 운영 시 IP당 60req/min
