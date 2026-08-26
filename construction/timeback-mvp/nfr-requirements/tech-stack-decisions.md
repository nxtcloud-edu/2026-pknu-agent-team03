# STEP 02 기술 스택 결정 — track-backup-server (M4)

## 팀 합의 사항

- 언어: **Java** (4명 통일)
- 머지 방식: STEP 5까지 각자 완료 후 한꺼번에

## 서버 (SRV-01~03)

| 항목 | 선택 | 이유 |
|---|---|---|
| 프레임워크 | Spring Boot 3.x | REST API 표준, 빠른 개발 |
| DB (MVP) | H2 인메모리 | 설정 없이 바로 실행 |
| DB (운영) | PostgreSQL 15+ | 안정성, JSONB 지원 |
| 직렬화 | Jackson | Spring Boot 기본 |
| 빌드 | Gradle (Kotlin DSL) | 팀 표준 |
| 컨테이너 | Docker | 격리 실행 |

## 클라이언트 (APP-12, APP-13)

| 항목 | 선택 | 이유 |
|---|---|---|
| 최소 SDK | API 34 (Android 14) | HW 식별원 접근 |
| HTTP | OkHttp + Retrofit | Android 표준 |
| 로컬 저장 | Room (SQLite) | BackupChange 상태 관리 |
| 비동기 | WorkManager | 백그라운드 재시도 |
| 직렬화 | Gson | Retrofit 통합 |

## 합의 필요 (머지 시 확인)

| 항목 | 가정 | 확인 대상 |
|---|---|---|
| 공통 패키지 위치 | `com.timeback.backup` | 팀 전체 네이밍 컨벤션 |
| CommittedChange 정의 위치 | 내 contracts/ 에 정의 | 1번과 중복 여부 |
| Gradle 루트 설정 | 별도 작성 안 함 | 팀이 통합 빌드 구성 |
| 서버 배포 도메인/IP | 미정 | STEP 06에서 결정 |
