# TimeBack AI-DLC State

> 2026-08-26 복구 승인 이후 현재 워크스페이스에서 검증 가능한 사실만으로 초기화한 상태다. 과거 Gate 승인이나 날짜를 소급해 만들지 않는다.

## 현재 상태

| 항목 | 값 |
|---|---|
| 프로젝트 | TimeBack |
| 공식 작업 단위 | `UOW-01 timeback-mvp` |
| 현재 책임 초점 | UOW-01 CONSTRUCTION 종료 인계와 실제 기기 후속 검증 경계 |
| 완료 단계 | 네 트랙 STEP 01–05 통합, STEP 06 전체 자동 검증·APK·서버 JAR·lint·PR #8 웹 데모 보정 |
| 활성 단계 | 없음 — CONSTRUCTION 종료, 외부 환경 후속 검증만 대기 |
| 단계 상태 | `CONSTRUCTION_COMPLETE_WITH_DEVICE_FOLLOWUP` — 92개 자동 검증 통과, OS-04·NFR-3.3 실기기 미완료 명시 |
| STEP 04 Gate 2 | `APPROVED` — 2026-08-26, 사용자 응답 `2` |
| STEP 05 계획 파일 | `aidlc-docs/construction/plans/timeback-mvp-step05-implementation-plan.md` |
| STEP 05 Gate 1 | `APPROVED` — 2026-08-26, 사용자 응답 `2` |
| STEP 05 Gate 2 | `track-domain-engine` 범위 승인 기록; UOW 전체 통합 승인을 뜻하지 않음 |
| 애플리케이션 코드 생성 | `INTEGRATED` — Java 17 Android·domain·backup·server 모듈 |
| 기술 스택 결정 | `DECIDED` — Java 17, Room, Retrofit, Spring Boot 3.x, H2 |
| 최종 갱신일 | 2026-08-26 |

## 구현 경계

- 통합 구현: `device-core`, `domain`, `backup`, `server`, `app` Gradle 모듈과 각 트랙의 Java 소스.
- 실제 어댑터: APP-11 Room/WAL, CT-05 Retrofit, SRV-01–SRV-03 Spring Boot/H2.
- 자동 검증: `./gradlew --no-daemon verifyAll`에서 92개 device·domain·backup·UI·Room·HTTP·웹 데모 통합 회귀를 실행한다.
- 설치·실행 산출물: debug APK와 Spring Boot 실행 JAR, Docker Compose 정의.
- PR #8 웹 화면: Spring `demo` 프로필과 `/demo-api/**`의 합성 데이터 전용 경계. Python/Flask 런타임 제거.
- 미검증 경계: OS-04 실제 식별원, Android 14 이상 Usage Access·UsageEvent, Docker 데몬 컨테이너 실행, release HTTPS 주소·인증서.

## 다음 허용 행동

1. Android 14 이상 기기에서 APK 설치와 Usage Access·UsageEvent 체크리스트를 실행한다.
2. OS-04 하드웨어 식별원 위험 게이트를 검증하고 승인된 결과를 기록한다.
3. 검증된 식별원을 production 조립에 연결한 뒤 identity 의존 UI·백업 여정을 실행한다.
4. Docker 데몬이 준비되면 `docker compose up --build`로 컨테이너 경계를 반복 확인한다.
5. STEP 06 종료 변경을 PR #7에 반영하고 `main`에 병합한다.
