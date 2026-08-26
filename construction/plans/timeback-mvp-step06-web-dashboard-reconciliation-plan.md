# `timeback-mvp` CONSTRUCTION STEP 06 웹 대시보드 통합 보정 계획

## 1. 배경

- PR #7의 STEP 06 검증 이후 `main`에 PR #8의 `webapp/index.html`, `webapp/server.py`가 추가됐다.
- Git 파일 단위 충돌은 없지만 `server.py`는 승인된 Java 17·Spring Boot 서버와 별도의 Python/Flask 메모리 서버를 만든다.
- 웹 대시보드의 `/api/deletion`은 실제 Android 기기와 서버 데이터를 지우지 않고 양쪽을 `COMPLETED`로 응답하므로 CT-05와 US-25의 완료 의미를 위반한다.
- 웹 대시보드의 Timeline·Goal·지표 데이터는 합성 데모 데이터이며 APP-11 Room, 도메인 엔진, SRV-01~SRV-03의 실제 데이터가 아니다.

## 2. 통합 결정

1. Java 17·Spring Boot·H2를 유일한 서버 런타임으로 유지한다.
2. 웹 화면의 기능과 API 형태는 보존하되 Spring Boot의 선택적 `demo` 프로필로 옮긴다.
3. 데모 API는 `/api/**`가 아니라 `/demo-api/**`에 격리해 CT-05의 `/api/v1/**`와 구분한다.
4. 데모 데이터는 메모리 합성 데이터임을 화면과 문서에 명시한다.
5. 데모 삭제는 Android 기기 삭제를 수행할 수 없으므로 `deviceStatus=PENDING`; 서버 데모 데이터 삭제가 끝난 경우에만 `serverStatus=COMPLETED`를 반환한다.
6. 기본·Docker 실행에서는 데모 API를 비활성화하고 `demo` 프로필을 명시한 로컬 시연에서만 활성화한다.

## 3. 작업 순서

- [ ] `origin/main`의 PR #8 결과를 현재 STEP 06 브랜치에 반영한다.
- [ ] Flask 서버를 제거하고 같은 화면 기능을 제공하는 Java Spring 데모 컨트롤러·상태 저장소를 추가한다.
- [ ] 웹 정적 파일을 Spring Boot 실행 JAR에 포함하고 API prefix를 `/demo-api`로 변경한다.
- [ ] 입력 검증, 없는 자원, 잘못된 분류·보관 기간, 삭제 상태의 계약 테스트를 추가한다.
- [ ] 기본 프로필에서 데모 API가 노출되지 않고 `demo` 프로필에서만 동작하는지 검증한다.
- [ ] 기존 `verifyAll`, Android clean build·lint, 서버 실행 JAR 검증을 다시 실행한다.
- [ ] STEP 06 보고서·상태·audit에 PR #8 보정 결과와 남은 실기기 경계를 기록한다.

## 4. 완료 조건

- Python/Flask가 production 또는 demo 실행의 필수 런타임으로 남지 않는다.
- 웹 대시보드는 `demo` 프로필의 Spring Boot JAR 하나로 실행된다.
- 웹 데모가 실제 CT-05 백업·삭제 성공을 가장하지 않는다.
- PR #8 반영 후에도 전체 Java/Android/Room/Retrofit/Spring 회귀가 통과한다.
- Android 14 이상 실기기 미연결 상태는 기존과 같이 NFR-3.3 미완료로 유지한다.

