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
- `testDebugUnitTest`: UI ViewModel 26개 회귀

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

## 현재 앱 동작 경계

- 프로덕션 앱은 Android Usage Access 상태를 실제 조회한다.
- 권한이 없으면 `PERMISSION_REQUIRED`로 차단한다.
- OS-04 하드웨어 식별원 검증 전에는 `IDENTITY_UNAVAILABLE`로 차단한다.
- 임의 UUID나 설치 ID를 실제 하드웨어 식별원의 대안으로 사용하지 않는다.
- Fake/Mock gateway는 자동 테스트에서만 직접 생성한다.

## 남은 수동 확인

`construction/timeback-mvp/code-generation/tracks/device-data-android-checklist.md`에 따라 권한 허용·회수,
UsageEvent, 앱 전환, 화면 종료, 개인정보 로그, 성능을 기록한다. OS-04 식별원은 접근 가능성,
앱 재실행·업데이트 뒤 안정성, 원본값 미노출을 별도로 확인해야 한다.
