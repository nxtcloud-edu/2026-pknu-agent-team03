# TimeBack 웹 합성 대시보드 개선 보고서

## 판정

- 구현: **완료**
- Spring demo 계약: **통과**
- 전체 회귀: **92개 통과**
- Android clean test·APK·lint: **통과**
- 데스크톱·모바일 브라우저 검증: **통과**
- production `/api/**` 영향: **없음**

## 주요 개선

| 영역 | 결과 |
|---|---|
| 레이아웃 | 최대 1120px 데스크톱 2열과 900px·620px 반응형 breakpoint 적용 |
| 모바일 | 390px에서 문서 가로 오버플로 없이 2열 지표와 단일 열 콘텐츠 제공 |
| 상태 | page별 skeleton, error banner, retry, empty state, toast, 연결 badge 추가 |
| API | 7초 timeout, 비 JSON·HTTP·network 오류 분리, 입력 오류 시 연결 상태 유지 |
| 접근성 | skip link, tab/tabpanel/dialog/live region, 키보드 탭 이동·modal focus 복원 |
| Timeline | `div onclick` 대신 접근 가능한 button과 안전한 text rendering 사용 |
| Goal | 이름·목표 시간 선검증, 요청 중 disabled 상태, 성공·실패 feedback 추가 |
| Settings | 합성 동기화, 보관 기간 실패 시 이전 선택 복원, 삭제 상태 경계 표시 |
| 복원 | `POST /demo-api/reset`으로 삭제된 합성 데이터 복원 |
| 독립 실행 | 외부 폰트 CDN 제거, Spring Boot JAR 내부 정적 파일만 사용 |

## 자동 검증

```bash
./gradlew --no-daemon verifyAll
./gradlew --no-daemon \
  :app:clean \
  :app:testDebugUnitTest \
  :app:assembleDebug \
  :app:lintDebug \
  :server:bootJar
./gradlew --no-daemon -p server/docker clean bootJar
```

모든 명령이 `BUILD SUCCESSFUL`로 종료됐다. 서버 JUnit은 기본 프로필의 demo API 차단 1개,
실제 HTTP/H2 계약 3개, demo 프로필 화면·API 계약 3개를 포함한다. reset 계약은 삭제 검증 안에서
합성 목표 4개와 월요일 Timeline 7개 복원을 확인한다.

## 브라우저 검증

| 시나리오 | 확인 결과 |
|---|---|
| 1280×800 홈 | shell 1120px, 문서 `scrollWidth=innerWidth`, 콘솔 오류 없음 |
| 390×844 홈 | shell 362px, 지표 2열, 문서 가로 오버플로 없음 |
| 목표 추가 | `대시보드 검증 목표`, 75분 생성과 성공 toast 확인 |
| Timeline 수정 | dialog 노출, 활동·분류 수정, 결과와 성공 toast 확인 |
| 서버 중단 | 오류 banner, page error, `연결 확인 필요` 표시, 미처리 콘솔 오류 없음 |
| 서버 복구 | `다시 시도`로 앱 사용량과 연결 badge 복원 |
| 합성 삭제 | `deviceStatus=PENDING`, `serverStatus=COMPLETED`, Timeline empty state 표시 |
| 합성 복원 | reset 뒤 홈 Timeline 6개와 전체 월요일 데이터 7개 복원 |
| 외부 자원 | 로드된 외부 stylesheet 없음 |

## 산출물

| 파일 | SHA-256 |
|---|---|
| `app/build/outputs/apk/debug/app-debug.apk` | `dc4454749c50383b6c6ef56ce8862b17d02ff1cd8e3b02c583b6244dadbaeda5` |
| `server/build/libs/server-0.1.0.jar` | `75a521072101755f1e8ae69cbf8851f7b5e913b09c7e112e648b15b3c15a0de9` |
| `server/docker/build/libs/timeback-server.jar` | `19029493a70258f54994735be4e0815f5d191d15fc4705b8a38b149bee9bc11d` |

## 실행

```bash
./gradlew :server:bootRun --args='--spring.profiles.active=demo'
```

브라우저에서 `http://localhost:8080/demo/index.html`을 연다. 이 화면은 합성 데이터 전용이며 실제
Android·Room·CT-05 상태를 대신하지 않는다.
