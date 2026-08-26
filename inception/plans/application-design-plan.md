# INCEPTION STEP 06 — inception1 고정본 기반 선별 보강 계획

## 1. 목적

- `aidlc-docs/inceptionc/`의 inception1 결과를 변경하지 않는 고정본으로 유지한다.
- 표준 경로 `aidlc-docs/inception/`에 별도의 병합본을 만든다.
- inception2의 유용한 설계 표현만 inception1의 승인된 경계 안으로 이식한다.
- 최종 작업 단위 `timeback-mvp`와 네 책임 트랙을 변경하지 않는다.
- CONSTRUCTION 계획·설계·코드는 만들지 않는다.

## 2. 고정할 파일

병합본의 다음 파일은 inception1 고정본과 바이트 단위로 같아야 한다.

- `requirements/requirement-verification-questions.md`
- `requirements/requirements.md`
- `user-stories/stories.md`
- `plans/story-generation-plan.md`
- `plans/execution-plan.md`
- `plans/unit-of-work-plan.md`
- `application-design/unit-of-work.md`

`application-design/components.md`의 기존 본문도 고치거나 삭제하지 않고 끝에 보강 섹션만 덧붙인다.

## 3. inception2에서 선별할 내용

| donor 내용 | 병합 방식 | inception1 보호 기준 |
|---|---|---|
| 데이터 모델 | 구현 타입·제품명을 제거한 논리 필드 계약으로 재작성 | APP-11이 기기 기준 데이터 소유, 서버는 백업 사본만 보관 |
| 유저 플로우 | 권한, 일상 사용, Timeline 수정, 목표 기록, 리포트, 데이터 관리 흐름으로 재작성 | US-01–US-25 범위 안에서만 작성 |
| 텍스트 와이어프레임 | 기존 UI-01–UI-08을 기준으로 화면 정보 구조와 상태를 구체화 | 새 화면이나 새 기능을 추가하지 않음 |
| API 예시 | 익명 백업·상태·보관·전체 삭제의 논리 요청·응답 계약만 작성 | Context·Baseline·Goal·Report 계산을 서버로 옮기지 않음 |
| 통합 편의 표현 | 데이터·UI·백업 계약을 CT-01–CT-06 및 네 책임 트랙에 연결 | `unit-of-work.md`의 배정과 의존 방향 유지 |

## 4. 가져오지 않을 내용

- PIN·비밀번호·JWT·회원 인증
- 실시간 낭비 알림, 푸시 알림, 오버레이
- 미니게임·상식 퀴즈, 일일 피드백 문구
- 임의의 1분·5초·10초·30분·60분·90일·목표 10개 같은 미승인 수치
- 서버의 Context·Baseline·Goal·Report 업무 계산
- 서버를 기준 데이터로 취급하는 데이터 모델
- `data-collection → context-engine → time-recovery → presentation` 순차 작업 단위
- Room, WorkManager, FCM 등 기술 제품 선택
- ZIP에 존재하지 않는 프로토타입을 존재한다고 표시하는 설명

## 5. 생성할 보강 섹션

`application-design/components.md` 끝에 다음 섹션을 추가한다.

- `15. 논리 데이터 계약 상세`
- `16. 핵심 사용자 흐름 상세`
- `17. UI-01–UI-08 텍스트 와이어프레임`
- `18. 익명 백업 경계 계약 상세`
- `19. 책임 트랙별 병렬 시작 자료`
- `20. inception2 선별 이식 검증`

## 6. 결과물

- 표준 병합 폴더: `aidlc-docs/inception/`
- 검토 대상: `aidlc-docs/inception/application-design/components.md`
- 공유용 압축: 워크스페이스 루트 `inception-merged.zip`
- 고정 원본: `aidlc-docs/inceptionc/` 및 사용자가 제공한 ZIP 파일은 변경하지 않음

## 7. 검증 체크리스트

- [x] 고정 대상 7개 파일이 inception1과 바이트 단위로 같은지 확인한다.
- [x] 기존 `components.md` 본문이 그대로 유지되고 보강 섹션만 뒤에 추가됐는지 확인한다.
- [x] FR-1–FR-10, NFR-1–NFR-5, US-01–US-25의 의미가 바뀌지 않았는지 확인한다.
- [x] OS-01–OS-05, APP-01–APP-13, SRV-01–SRV-03, UI-01–UI-08의 소유권이 유지되는지 확인한다.
- [x] `timeback-mvp`와 네 책임 트랙 배정이 그대로인지 확인한다.
- [x] 기기가 기준 데이터이고 서버가 익명 백업 사본만 갖는지 확인한다.
- [x] 서버 경계에 도메인 계산 API가 추가되지 않았는지 확인한다.
- [x] PIN·JWT·알림·미니게임·퀴즈·일일 피드백이 들어가지 않았는지 확인한다.
- [x] 미승인 수치와 기술 제품 선택이 들어가지 않았는지 확인한다.
- [x] 8개 기존 UI만 구체화되고 새 화면이 추가되지 않았는지 확인한다.
- [x] 압축 최상위가 `inception/`이고 `__MACOSX` 같은 메타데이터가 없는지 확인한다.
- [x] CONSTRUCTION 파일과 코드를 만들지 않았는지 확인한다.

## 8. 확인 질문

추가 질문 없음. 사용자가 inception1을 고정하고 inception2에서 필요한 부분만 선별하라고 범위와 판단 권한을 명확히 지정했다.
