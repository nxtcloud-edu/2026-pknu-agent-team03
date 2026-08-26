# TimeBack AI-DLC State

> 2026-08-26 복구 승인 이후 현재 워크스페이스에서 검증 가능한 사실만으로 초기화한 상태다. 과거 Gate 승인이나 날짜를 소급해 만들지 않는다.

## 현재 상태

| 항목 | 값 |
|---|---|
| 프로젝트 | TimeBack |
| 공식 작업 단위 | `UOW-01 timeback-mvp` |
| 현재 책임 초점 | `track-domain-engine` — APP-05~APP-09 |
| 활성 단계 | CONSTRUCTION STEP 01 기능 설계 |
| 단계 상태 | `ARTIFACT_GENERATION` |
| 계획 파일 | `aidlc-docs/construction/plans/timeback-mvp-functional-design-plan.md` |
| Gate 1 | `APPROVED` — 2026-08-26, 사용자 응답 `2` |
| Gate 2 | `PENDING` |
| 애플리케이션 코드 생성 | `BLOCKED` |
| 기술 스택 결정 | `NOT_STARTED` — STEP 02에서만 수행 |
| 최종 갱신일 | 2026-08-26 |

## 복구 기준

- 루트 `spec.md`의 현재 원문을 확정 입력으로 취급한다.
- Gate 1 승인으로 기존 `inception/` 문서를 현재 시점부터 STEP 01 설계 기준 입력으로 채택한다.
- 기존 문서 내부의 `확정`, 체크 표시, `[Answer]`는 과거 Gate 이력을 복원하는 증거로 사용하지 않는다.
- Gate 1의 사용자 응답 `2`는 기능 설계 계획과 Q1~Q14 추천안 전체를 현재 시점부터 승인한 것이다.
- `track-domain-engine`은 별도 작업 단위가 아니라 `timeback-mvp` 내부 책임 트랙이므로 독립 Construction Gate를 만들지 않는다.

## 사용자 지정 기술 제약

- 실제 구현 언어는 Java로 통일한다는 원문이 `spec.md`에 있다.
- STEP 01은 언어 중립 기능 설계만 수행한다.
- Java 적용 범위, Java/JDK 수준, 빌드·테스트 도구와 실제 명령은 CONSTRUCTION STEP 02의 `tech-stack-decisions.md`에서 질문·승인 후 확정한다.

## 다음 허용 행동

1. `aidlc-docs/construction/timeback-mvp/functional-design/` 아래 승인된 STEP 01 본 산출물 5개를 생성한다.
2. APP-05~APP-09 업무 규칙과 CT-01~CT-06 협업 경계를 FR/NFR/US에 추적한다.
3. 문서 간 정합성과 미결정을 검증한 뒤 Gate 2를 요청한다.
4. Gate 2 승인 전에는 `src/`, Java 코드, 빌드 설정을 생성하지 않는다.
