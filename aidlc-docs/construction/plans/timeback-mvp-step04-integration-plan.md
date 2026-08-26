# TimeBack UOW-01 — CONSTRUCTION STEP 04 통합·실행 경계 계획

## 1. 목적

이 계획은 APP-05~APP-09 Java 순수 도메인 구현을 실제 Android/DB 없이 컴파일·실행·fake 통합할 최소 실행 경계를 정한다. STEP 02의 외부 의존성 없음과 STEP 03의 결정론적 검증을 유지한다.

## 2. 통합 범위

- PowerShell `verify-domain.ps1`가 source/test compile과 test runner 실행을 담당한다.
- `src/main/java`은 pure domain이며 SDK·DB·네트워크를 import하지 않는다.
- `src/test/java`은 Session/Data/Time/Notifier 포트를 메모리 fake와 fixed time으로 대체한다.
- build 출력은 `build/domain-classes`에 한정하고 기준 코드·문서를 변경하지 않는다.
- APP-04, APP-11, OS-05의 실제 adapter와 UI/backup 연결은 계약만 소비하며 이번 책임 트랙 구현에 포함하지 않는다.

## 3. Gate 1 검증 질문

### Q1. 실행 경계

- A. `.kiro/scripts/verify-domain.ps1`만 표준 도메인 검증 진입점으로 두고 `build/domain-classes`는 재생성 가능 출력으로 취급한다. **(추천)**
- B. Gradle/Maven 또는 다른 실행 경계를 새로 도입한다.
- Other: 직접 입력

### Q2. 외부 의존성 처리

- A. production은 port interface만 갖고, 테스트는 in-memory fake/controlled time으로 통합한다. **(추천)**
- B. Android API·DB·네트워크를 이번 도메인 구현에 연결한다.
- Other: 직접 입력

### Q3. 실패 경계

- A. compile/test failure 또는 fake 저장 실패는 non-zero와 snapshot 보존으로 표현하며 실제 원격 재시도·백업은 구현하지 않는다. **(추천)**
- B. 외부 시스템 재시도·백업까지 이번 구현에 포함한다.
- Other: 직접 입력

### Q4. 통합 완료 정의

- A. Java 17에서 스크립트 한 번으로 compile+핵심 합성 회귀가 통과하고 Android/DB/UI 미구현 경계가 문서·코드에서 명시되면 완료다. **(추천)**
- B. 전체 MVP 통합이 될 때까지 도메인 구현을 완료로 보지 않는다.
- Other: 직접 입력

## 4. Gate 1 요청

추천안은 `Q1=A, Q2=A, Q3=A, Q4=A`이다.

1) 수정 요청
2) 다음 단계로
