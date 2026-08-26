# `timeback-mvp` 통합 빌드·사용자 검증 안내

## 준비 조건

- JDK 17
- Android SDK API 34 이상
- 로컬 `local.properties`의 `sdk.dir` 또는 `ANDROID_HOME`
- 실제 설치 검증 시 Android 14 이상 기기와 USB 디버깅

## 자동 검증

```bash
./gradlew --no-daemon verifyAll
```

`verifyAll`은 다음을 실행한다.

- `verifyData`: device-data 13개 회귀
- `verifyDomain`: domain 14개 회귀
- `verifyBackup`: backup·삭제 15개 회귀
- `verifyConstructionIntegration`: device→domain→APP-11→backup→전체 삭제 10개 통합 회귀
- `testDebugUnitTest`: UI ViewModel 26개, Retrofit 2개, Room 5개 회귀
- `server:test`: Spring Boot/H2 HTTP 3개 통합 회귀

총 88개 자동 검증을 실행한다.

## 디버그 APK

```bash
./gradlew --no-daemon :app:assembleDebug
```

산출물:

```text
app/build/outputs/apk/debug/app-debug.apk
```

연결 기기 설치:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## 격리 백업 서버

실행 JAR 생성과 실행:

```bash
./gradlew --no-daemon :server:bootJar
java -jar server/build/libs/server-0.1.0.jar
```

Docker 데몬이 실행 중이면 다음으로 동일 H2 격리 서버를 시작한다.

```bash
docker compose up --build
```

Dockerfile의 서버 전용 빌드는 Android SDK에 의존하지 않는다. 컨테이너 없이 해당 경계만 확인하려면
다음을 실행한다.

```bash
./gradlew --no-daemon -p server/docker clean bootJar
```

Android emulator의 debug 앱은 `http://10.0.2.2:8080/`을 사용한다. 평문 HTTP 허용은 debug
Manifest에만 있으며 release는 실제 HTTPS 주소와 인증서가 결정되기 전 실패 폐쇄한다.

## 현재 앱 동작 경계

- 프로덕션 앱은 Android Usage Access 상태를 실제 조회한다.
- 권한이 없으면 `PERMISSION_REQUIRED`로 차단한다.
- OS-04 하드웨어 식별원 검증 전에는 `IDENTITY_UNAVAILABLE`로 차단한다.
- 임의 UUID나 설치 ID를 실제 하드웨어 식별원의 대안으로 사용하지 않는다.
- Fake/Mock gateway는 자동 테스트에서만 직접 생성한다.
- APP-11 production 저장은 Room/WAL 구현을 사용하며 메모리 구현과 별도 계약 회귀를 통과한다.
- CT-05 production 서버 경계는 Retrofit 구현을 사용한다.

## 남은 수동 확인

`construction/timeback-mvp/code-generation/tracks/device-data-android-checklist.md`에 따라 권한 허용·회수,
UsageEvent, 앱 전환, 화면 종료, 개인정보 로그, 성능을 기록한다. OS-04 식별원은 접근 가능성,
앱 재실행·업데이트 뒤 안정성, 원본값 미노출을 별도로 확인해야 한다.

전체 STEP 06 결과와 산출물 체크섬은
`construction/timeback-mvp/build-test/build-test-report.md`에 기록되어 있다.
