# `timeback-mvp` CONSTRUCTION STEP 06 빌드·테스트 보고서

## 1. 판정

- 판정일: 2026-08-26
- 로컬 빌드·자동 검증: **통과**
- Spring Boot/H2 HTTP 통합: **통과**
- Android debug APK: **생성·lint 통과**
- Android 14 이상 실기기: **미실행 — 연결 기기 없음**
- UOW 최종 상태: **실제 기기 OS-04/NFR-3.3 확인 전 `INTEGRATION_IN_PROGRESS` 유지**

실제 기기가 없는 경우 수동 체크리스트와 미완료 상태를 남길 수 있다는 UOW STEP 06 조건은 충족했다.
다만 OS-04 하드웨어 식별원 자체가 미검증이므로 identity 의존 기능을 실제 완료로 판정하지 않는다.

## 2. 검증 환경

| 항목 | 값 |
|---|---|
| 언어 | Java 17 |
| 빌드 | Gradle 8.13, Kotlin DSL |
| Android | AGP 8.11.1, compile/min/target SDK 34 |
| 앱 저장 | Room 2.6.1, SQLite WAL |
| 앱 HTTP | Retrofit 2.11.0 + Gson |
| 서버 | Spring Boot 3.4.13, H2 |
| 격리 방식 | 랜덤 포트 Spring 통합 테스트, 실행 JAR 포트 18080, Docker Compose 정의 |

## 3. 자동 검증 결과

실행 명령:

```bash
./gradlew --no-daemon verifyAll
```

결과: `BUILD SUCCESSFUL`

| 영역 | 검증 수 | 결과 |
|---|---:|---|
| device-data 순수 계약 | 13 | 통과 |
| domain-engine | 14 | 통과 |
| backup·data-control | 15 | 통과 |
| 기존 UI ViewModel | 26 | 통과 |
| 네 트랙 인프로세스 통합 | 10 | 통과 |
| Retrofit 네트워크 실패·재시도 | 2 | 통과 |
| Room 영속 저장·재개·격리·삭제·성능 | 5 | 통과 |
| Spring Boot/H2 HTTP | 3 | 통과 |
| **합계** | **88** | **통과** |

Room 성능 검증은 10,000개 UsageEvent와 변경 통지를 한 트랜잭션으로 저장한다. 테스트 케이스 전체
시간은 3.145초였고, 실제 저장 측정값이 4초 이내인지 테스트에서 단언한다. 기존 ID는 일괄 조회하여
이벤트별 N+1 쿼리를 사용하지 않는다.

## 4. 클린 Android 빌드·lint

실행 명령:

```bash
./gradlew --no-daemon \
  :app:clean \
  :app:testDebugUnitTest \
  :app:assembleDebug \
  :app:lintDebug
```

결과: `BUILD SUCCESSFUL`

- 외부 공유 소스는 패키지 루트를 보존한 Gradle 생성 소스로 동기화한다.
- debug만 로컬 `http://10.0.2.2:8080/`을 허용한다.
- release는 `https://backup.invalid/`로 실패 폐쇄되어 실제 배포 주소·인증서 결정 전 전송하지 않는다.
- `PACKAGE_USAGE_STATS`는 Manifest 선언과 사용자 설정 화면 승인이 모두 필요한 AppOps 권한으로 유지한다.

산출물:

| 파일 | 크기 | SHA-256 |
|---|---:|---|
| `app/build/outputs/apk/debug/app-debug.apk` | 약 14 MB | `dc4454749c50383b6c6ef56ce8862b17d02ff1cd8e3b02c583b6244dadbaeda5` |
| `server/build/libs/server-0.1.0.jar` | 약 23 MB | `265fe10e9990293f24d26f071949da274d46bd13a6b5edced1dd48a6dcc2577c` |

## 5. 실제 HTTP/JDBC 실행 검증

서버 실행:

```bash
java -jar server/build/libs/server-0.1.0.jar --server.port=18080
```

합성 사용자 `step06-anonymous`와 합성 변경 `step06-change-1`만 사용했다.

| 순서 | 요청 | 확인 결과 |
|---:|---|---|
| 1 | `POST /api/backup` | `ACCEPTED` |
| 2 | 동일 `changeId` 재전송 | `ACCEPTED`, 중복 저장 없음 |
| 3 | 같은 사용자 상태 조회 | `ACCEPTED` |
| 4 | 다른 사용자 상태 조회 | `PENDING`, 사용자 범위 격리 |
| 5 | `PUT /api/retention` | `APPLIED` |
| 6 | `POST /api/deletion` | `COMPLETED` |
| 7 | 삭제 작업 재조회 | `COMPLETED` |
| 8 | 삭제 뒤 백업 상태 조회 | `PENDING`, 잔존 백업 없음 |

네트워크 503 뒤 동일 `changeId` 재시도는 Retrofit 계약 테스트에서
`RETRYABLE_FAILURE → ACCEPTED`로 확인했다. 서버는 요청 본문과 익명 ID를 로그에 출력하지 않는다.

## 6. Docker 검증

```bash
docker compose config
```

Compose 해석은 통과했다. 현재 Docker 데몬이 실행 중이지 않아 `docker compose up`과 이미지 실행은
미실행이다. Dockerfile은 Android SDK를 요구하지 않는 서버 전용 Gradle 빌드를 사용한다. 다음 독립
빌드가 통과했으며 같은 서버 소스로 `timeback-server.jar`를 생성했다.

```bash
./gradlew --no-daemon -p server/docker clean bootJar
```

루트 빌드의 `server-0.1.0.jar`는 로컬 H2와 포트 18080에서 직접 실행하여 API 흐름을 검증했다.
Docker 데몬이 준비되면 다음으로 반복할 수 있다.

```bash
docker compose up --build --abort-on-container-exit
```

## 7. CP 체크포인트

| 체크포인트 | 상태 | 증거 |
|---|---|---|
| CP-1 M1 핵심 여정 | 자동 검증 통과 | 권한 차단, 이벤트, 세션, Context, Timeline 계약, 낭비시간 |
| CP-2 M2 Baseline·리포트 | 자동 검증 통과 | 7일 관찰, 부분 coverage, Saved, UI 상태 |
| CP-3 M3 목표·회수 | 자동 검증 통과 | 목표, timer, 중첩, 대표 목표, 회수율 |
| CP-4 M4 백업·통제 | HTTP/H2 통과 | 멱등 백업, 실패 후 재시도, 보관, 양측 삭제 계약 |
| CP-5 전체 통합 | 부분 통과 | 88개 자동 검증·APK·서버 JAR 통과, 실제 기기만 미실행 |

## 8. 실제 기기 미완료 상태

Android SDK의 `adb devices -l` 결과 연결 기기가 없었다. 따라서 다음은 통과로 기록하지 않는다.

- NFR-3.3 실제 Android 14 이상 Usage Access 허용·거부·회수
- 제조사별 UsageEvent, 앱 전환, 화면 종료 사건
- OS-04 하드웨어 식별원 접근 가능성·업데이트 뒤 안정성·개인정보 영향
- 실제 식별원 연결 뒤 UI-02–UI-08 및 Retrofit 백업 사용자 여정

수동 절차는 `construction/timeback-mvp/code-generation/tracks/device-data-android-checklist.md`를 사용한다.
OS-04 승인 전 production gateway는 `IDENTITY_UNAVAILABLE`로 실패 폐쇄하며 임의 식별자를 생성하지 않는다.

## 9. 재현 명령

```bash
./gradlew --no-daemon verifyAll
./gradlew --no-daemon :app:assembleDebug :app:lintDebug :server:bootJar
java -jar server/build/libs/server-0.1.0.jar
adb install -r app/build/outputs/apk/debug/app-debug.apk
```
