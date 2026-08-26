# `timeback-mvp` track-device-data 코드 생성 결과

## 1. 단계와 전략

- 단계: CONSTRUCTION STEP 05
- 상태: 독립 코드 생성·JVM 계약 검증 완료 후보
- 순서: 업무 규칙 → OS API 포트·어댑터 → 저장 → UI 어댑터 경계
- 독립 수단: CT-06 가짜 OS 경계와 메모리 APP-11

## 2. 생성 범위

| 층 | 구현 |
|---|---|
| 계약 | 사용자 범위, 구간, UsageEvent, AppSession, 체크포인트, 변경 커서·페이지 |
| 업무 규칙 | `AccessGate`, `UsageCollector`, `SessionReconstructor` |
| OS API | Usage Access, UsageStats 사건, 화면 종료, 현지 시간 포트와 Android 어댑터 |
| 저장 | `DeviceDataAuthority` 계약과 원자적 `InMemoryDeviceDataAuthority` |
| 테스트 대역 | 권한·사건·화면 종료·시간 제어 가짜 구현 |
| 검증 | DD-ACCESS, DD-COLLECT, DD-SESSION, DD-DATA 대표 자동 테스트 |

Room 실제 저장 구현은 의존성·앱 모듈을 네 트랙이 공유해야 하므로 메모리 구현과 같은 계약 suite를 통과시키는 CP 통합 항목으로 남긴다. 이 트랙의 저장 업무 계약과 원자성 모델은 메모리 구현으로 독립 검증한다.

## 3. 완료 조건

- [x] 미허용·조회 실패에서 수집 소스를 호출하지 않는다.
- [x] 승인된 원본 필드와 안정 ID로 겹친 재조회 중복을 제거한다.
- [x] 원본 사건과 체크포인트를 원자 확정한다.
- [x] 정상 쌍, 다음 앱, 화면 종료, 열린 후보를 재구성한다.
- [x] 현지 자정과 DST 경계에서 기간 합을 보존한다.
- [x] 저장 성공 뒤에만 순서형 `CommittedChange`를 제공한다.
- [x] 사용자 범위 조회와 커서 페이지를 검증한다.
- [x] Android SDK 34에 대해 OS 어댑터를 컴파일한다.
- [ ] Android 14 이상 실제 기기의 NFR-3.3 시나리오를 실행한다. STEP 06 전까지 미완료다.
- [ ] Room 실제 구현, UI·도메인·백업 어댑터를 통합 브랜치에서 검증한다.

## 4. 실행 명령

현재 저장소에는 공통 Gradle 루트가 아직 없으므로 독립 검증은 JDK 17 호환 `javac --release 17`과 SDK 34 `android.jar`를 사용한다. 네 트랙 통합 뒤에는 공통 Gradle의 `verify-data`, `verify-device`, `verify-all` 작업으로 대체한다.
