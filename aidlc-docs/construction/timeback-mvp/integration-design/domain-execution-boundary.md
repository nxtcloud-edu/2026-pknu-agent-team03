# TimeBack UOW-01 — CONSTRUCTION STEP 04 도메인 실행·통합 경계

## 1. 승인 근거와 범위

- Gate 1 승인: 2026-08-26, 사용자 응답 `2`
- 채택 답변: `Q1=A, Q2=A, Q3=A, Q4=A`
- 대상: APP-05~APP-09 Java pure domain 실행 및 CT-06 fake 통합

이 문서는 코드 생성 단계가 호출할 실행 경계만 정한다. Android, 실제 저장소, UI, 백업·네트워크 adapter는 구현하지 않는다.

## 2. 실행 경계

```text
verify-domain.ps1
  ├─ javac: src/main/java + src/test/java → build/domain-classes
  └─ java: io.timeback.domain.DomainEngineTestRunner
        ├─ pure domain model/interval/context/metrics/recovery
        └─ in-memory port fakes + controlled time + synthetic fixtures
```

- `build/domain-classes`는 매 실행 재생성 가능한 출력이다.
- production source는 Android SDK·JDBC·HTTP·파일 I/O를 import하지 않는다.
- port는 Session 입력, 시간, 저장 변경·조회, change notification의 의미만 표현한다.
- test fake는 저장 전후 snapshot을 노출해 명령 실패 시 불변성을 검사한다.
- 코드가 실제 외부 시스템에 접속하거나 실제 앱 사용 정보를 읽지 않는다.

## 3. 실패·완료 경계

| 조건 | 결과 |
|---|---|
| Java 컴파일 실패 | 스크립트 non-zero, runner 미실행 |
| 테스트 assertion/예외 | 스크립트 non-zero, 실패한 명명 테스트 출력 |
| fake 저장 실패 | 명령 실패, 기존 snapshot 유지 |
| timer 완료 저장 실패 | Recovered·timer 모두 이전 상태 유지 |
| 외부 adapter 필요 | 구현하지 않고 port/fake 경계에 남김 |

도메인 구현 완료는 `verify-domain.ps1` 한 번으로 Java 17 compile과 핵심 합성 회귀가 통과하는 상태다. 이는 전체 Android MVP 완료나 실제 저장·UI·백업 통합 완료를 의미하지 않는다.

## 4. STEP 05 전달

STEP 05는 다음 파일만 새로 만들 수 있다.

- `src/main/java/io/timeback/domain/**`
- `src/test/java/io/timeback/domain/**`
- `.kiro/scripts/verify-domain.ps1`

구현 순서는 interval → Context decision → Waste/Baseline/Saved → Goal/Recovered/Rate → fake/test runner이며, 모든 외부 기능은 port 또는 fake로 유지한다.

## 5. Gate 2 완료 검사

- [x] 실행 명령·source/test/output 경계를 확정했다.
- [x] production pure domain과 fake 통합을 분리했다.
- [x] 실패·snapshot 보존·timer 원자성 경계를 정했다.
- [x] 실제 Android/DB/UI/backup 접근을 제외했다.
- [x] STEP 05 허용 파일·구현 순서를 제한했다.

## 6. Gate 2 요청

1) 수정 요청
2) 다음 단계로
