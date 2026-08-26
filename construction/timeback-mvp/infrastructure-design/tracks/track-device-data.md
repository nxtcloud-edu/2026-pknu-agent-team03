# `timeback-mvp` track-device-data 인프라 설계 판정

## 1. STEP 04 판정

`track-device-data` 자체의 별도 서버·네트워크·배포 인프라는 없으므로 CONSTRUCTION STEP 04의 신규 인프라 설계는 **건너뜀**으로 판정한다. 이는 STEP 04를 누락한 것이 아니라 `inception/plans/execution-plan.md`의 순수 Android 내부 트랙 건너뜀 조건을 적용한 결과다.

## 2. 기기 내부 실행 경계

| 항목 | 결정 |
|---|---|
| 실행 환경 | Android 14 이상 앱 프로세스 |
| 영속 저장 | 앱 전용 Room/SQLite, 외부 공유 저장소 사용 안 함 |
| 백그라운드 작업 | WorkManager 고유 작업, 사용자 범위별 직렬화 |
| OS 권한 | Usage Access 특수 접근; 앱 런타임 권한으로 오인하지 않음 |
| 데이터 외부 이동 | APP-11은 직접 네트워크 전송하지 않고 CT-03 변경 페이지를 APP-12에 제공 |
| 삭제 | APP-13의 확정 요청을 받아 앱 전용 기준·파생 데이터를 기기 경계에서 삭제 |

## 3. 통합 검토 사항

- 백업 서버 주소, TLS, 서버 저장·배포는 `track-backup-server` 소유다.
- Android 앱 모듈의 Gradle·manifest·Room migration 통합은 네 트랙 병합 뒤 STEP 06 전체 빌드에서 한 번 검증한다.
- 실제 기기 데이터는 디버그 로그와 테스트 산출물에 원문으로 남기지 않는다.
