# CONSTRUCTION STEP 01 — `timeback-mvp` 기능 설계 계획 및 검증 질문

## 1. 문서 상태

| 항목 | 값 |
|---|---|
| 공식 작업 단위 | `UOW-01 timeback-mvp` |
| Construction 단계 | STEP 01 기능 설계 |
| 현재 상태 | Gate 1 승인, 기능 설계 본 산출물 생성 중 |
| 우선 상세화 책임 | `track-domain-engine` — APP-05~APP-09 |
| Gate 1 | `APPROVED` — 2026-08-26, 사용자 응답 `2` |
| Gate 2 | `PENDING` |
| 코드 생성 | 금지 |

이 문서는 기능 설계 본 산출물이 아니다. 설계 범위, 작성 순서, 검증 질문과 완료 기준을 정의한다.

### Gate 1 결정

- 사용자는 `2) 다음 단계로`를 선택했다.
- Q1~Q14는 모두 각 질문의 `[Recommended]` 답으로 채택한다.
- 채택 답: `Q1=A, Q2=B, Q3=A, Q4=A, Q5=A, Q6=A, Q7=A, Q8=A, Q9=A, Q10=A, Q11=A, Q12=A, Q13=A, Q14=A`.
- 기존 Inception 문서는 현재 시점부터 STEP 01 설계 입력으로 채택하되 과거 Gate 이력은 복원하지 않는다.
- Java 세부 기술은 STEP 02로 이관하며 STEP 01에서는 언어 중립 설계만 생성한다.

## 2. 목적

- `timeback-mvp` 하나의 작업 단위와 하나의 STEP 01 Gate를 유지한다.
- CT-01~CT-06 공통 계약을 먼저 구체화해 네 책임 트랙이 같은 의미를 사용하게 한다.
- 사용자가 요청한 `track-domain-engine`의 APP-05~APP-09를 우선 상세화한다.
- 앱·활동 기본 분류, Context, Waste, Baseline, Saved, Goal, Recovered, Recovery Rate의 미확정 정책을 구현 전에 결정한다.
- 가짜 AppSession·Activity·저장소·제어 시간을 사용해 순수 업무 규칙을 검증할 수 있는 입력·출력·오류 계약을 정의한다.
- 실제 경과시간 중복 집계와 사용자 확정 Context 덮어쓰기를 설계 수준에서 방지한다.

## 3. 복구 전제와 입력 효력

### 확정 원문

- 루트 `spec.md`
- 사용자의 `track-domain-engine` 구현 시작 요청
- 실제 구현 언어를 Java로 통일하라는 사용자 지정 기술 제약

### 복구 참고 자료

- `inception/requirements/requirements.md`
- `inception/requirements/requirement-verification-questions.md`
- `inception/user-stories/stories.md`
- `inception/application-design/components.md`
- `inception/application-design/unit-of-work.md`

기존 Inception 문서는 상세한 요구·설계 참고 자료지만 과거 audit가 없으므로 소급 승인된 것으로 취급하지 않는다. Gate 1에서 현재 시점의 설계 입력으로 채택할지 확인한다.

## 4. 범위

### 4.1 단일 작업 단위 공통 범위

- CT-01: UsageEvent, AppSession, Activity, Context의 구간·상태 계약
- CT-02: Goal, RecoveredTime, Baseline과 시간 지표 계약
- CT-03: 엔터티별 저장·조회·변경 통지 계약
- CT-04: 사용자 작업, 조회 결과, 로딩·빈 상태·오류 상태 계약
- CT-05: 익명 식별자와 백업 상태의 기능 경계만 연결
- CT-06: 고정 이벤트, 제어 시간, 가짜 저장소·서버 응답 계약

CT-05의 기술·보안·인프라 상세는 STEP 02~04로 넘기고, STEP 01에서는 도메인 입력·출력에 필요한 기능 의미만 정의한다.

### 4.2 `track-domain-engine` 우선 범위

| 구성 요소 | 기능 설계 대상 | 주요 추적 근거 |
|---|---|---|
| APP-05 App Classification | 앱 기본 분류 조회·수정, 미분류 상태, 사용자 확정 Context와 분리 | FR-3, US-04, NFR-5.2 |
| APP-06 Activity Management | Activity 생성·수정, 활동 의미, 유효 구간, 자정 분할 | FR-4.1~4.2, FR-9.4, US-05 |
| APP-07 Context Analysis | 원자 구간 분할, 자동 판정, 충돌, MIXED, 사용자 확정·수정 보존 | FR-5, US-06~07·09, NFR-2.1·2.3 |
| APP-08 Time Metrics | Waste, Baseline 관찰, 재산정, Saved, 기간 정렬 | FR-7, US-10~13, NFR-3.1 |
| APP-09 Goal Recovery | Goal, TIMER/MANUAL, 목표 중첩, 누적·진행률, Recovered, Recovery Rate | FR-4.3~4.4, FR-8, US-16~21, NFR-2.4 |

### 4.3 협업 경계

- 입력: APP-04가 제공하는 AppSession
- 저장: APP-11의 엔터티 저장·조회·변경 계약
- 시간: OS-05의 현재 시각, 현지 날짜·주 경계, 제어 시간 계약
- 출력: APP-10과 UI 트랙이 소비하는 최종 Context, 지표, 목표 상태, 계산 불가 상태
- 검증: CT-06의 합성 데이터와 제어 시간을 사용하는 `verify-domain`

### 4.4 제외 범위

- Java/JDK 버전, Android SDK 도구, 프레임워크, Gradle/Maven, 테스트 라이브러리 선택
- 데이터베이스 제품과 실제 APP-11 구현
- Android UI, UsageStats 어댑터, 서버·네트워크·배포 구현
- `src/`, `.java`, 빌드 파일과 테스트 코드 생성
- MVP 제외 기능인 개인화 추천, AI 설명, 알림, 앱 제한, 콘텐츠·위치 감시

## 5. Gate 1 승인 후 생성할 기능 설계 산출물

```text
aidlc-docs/construction/timeback-mvp/functional-design/
├── domain-model.md
├── business-rules.md
├── service-contracts.md
├── frontend-components.md
└── traceability.md
```

### `domain-model.md`

- CT-01·CT-02 논리 엔터티, 값, 상태 전이, 선택 필드
- App default와 final Context의 분리
- `[start, end)` 구간과 기간·현지 경계의 논리 표현
- Baseline 관찰·확정·재산정 상태
- Goal, 실행 중 타이머, RecoveredTime, 대표 Goal 선택 상태

### `business-rules.md`

- APP-05~APP-09의 정상·경계·오류 흐름
- 원자 구간 분할과 Context 판정 우선순위
- Waste/Baseline/Saved/Recovered/Recovery Rate 계산식
- 사용자 수정 후 재계산 영향 범위
- 중복 제거와 불변식

### `service-contracts.md`

- CT-03·CT-04·CT-06의 기능 호출 입력·출력·오류
- 가짜 AppSession·Activity·저장소·제어 시간 계약
- 도메인 결과의 `값 있음`, `계산 불가`, `사용자 결정 대기`, `데이터 부족` 구분
- APP-04·APP-11·OS-05·APP-10과의 의존 방향

### `frontend-components.md`

- UI-02~UI-07이 소비하는 도메인 상태와 사용자 작업
- Baseline 관찰 중, MIXED 확인 대기, 대표 Goal 선택 대기, 계산 오류 상태
- UI가 계산 규칙을 다시 구현하지 않도록 하는 표시 계약

### `traceability.md`

- FR/NFR → US → CT → APP-05~APP-09 → 업무 규칙 → 검증 사례 연결
- 미확정·후속 단계 전달 항목
- STEP 02 Java 기술 결정 전달 항목

## 6. 작성 순서

1. Gate 1 답변과 질문별 결정을 audit에 append한다.
2. CT-01·CT-02의 엔터티와 상태를 먼저 확정한다.
3. APP-05·APP-06의 분류와 Activity 입력 규칙을 정의한다.
4. APP-07의 구간 분할, 충돌 행렬, 사용자 확정 우선순위를 정의한다.
5. APP-08의 Waste, Baseline, Saved 계산과 기간 정렬을 정의한다.
6. APP-09의 Goal, 타이머, 수동 기록, 중첩, Recovered, Recovery Rate를 정의한다.
7. CT-03·CT-04·CT-06의 저장·조회·가짜 입력 계약을 연결한다.
8. UI 상태와 도메인 결과를 연결하되 UI에 계산을 복제하지 않는다.
9. FR/NFR/US 추적표와 정상·경계·오류 검증 사례를 작성한다.
10. 산출물 간 용어·계산식·상태가 일치하는지 검증한다.
11. 변경 요약과 미결정을 제시하고 Gate 2를 요청한다.

## 7. 설계 불변식

- 모든 시간 구간은 반개구간 `[start, end)`이며 `end > start`여야 한다.
- 동일한 실제 경과시간은 한 지표 또는 전체 합계에서 두 번 합산하지 않는다.
- 앱 기본 분류는 Default이며 최종 Context를 확정하지 않는다.
- 명시적 사용자 확인·수정 Context는 자동 재분석보다 우선하고 보존된다.
- Context에는 판정 근거와 사용자 확인 상태를 설명할 수 있어야 한다.
- Waste는 최종 Context가 `WASTE`로 인정된 원자 구간만 합산한다.
- Baseline 미완료 상태를 숫자 0으로 대체하지 않는다.
- Saved와 Recovered는 서로 다른 개념과 원천 데이터를 유지한다.
- Recovered는 Saved를 초과할 수 있고 Recovery Rate는 100%를 초과할 수 있다.
- 목표 기록 중첩은 합집합을 한 번만 집계하고 대표 Goal 정책을 적용한다.
- 테스트 입력은 실제 사용자의 앱·활동 기록이 아닌 합성 데이터를 사용한다.

## 8. 검증 질문

### 답변 방법

- 각 질문의 `[Recommended]`는 현재 제품 원칙과 기존 문서에서 가장 일관된 제안이다.
- `2) 다음 단계로`를 선택하면 모든 `[Recommended]` 답과 이 계획을 함께 승인한 것으로 기록한다.
- 다른 결정을 원하면 `1) 수정 요청`을 선택하고 `Q번호=보기` 형식으로 알려 준다.
- 보기로 표현되지 않는 답은 마지막 `E) Other`를 선택하고 내용을 적는다.

### Q1. 기존 Inception 문서를 앞으로의 STEP 01 설계 입력으로 어떻게 취급할까요?

A) 현재 시점부터 요구·스토리·구성 요소·작업 단위 기준으로 채택하되 과거 Gate 이력은 복원하지 않음
B) 요구사항 확인 단계부터 다시 수행한 후 Construction으로 돌아옴
C) 현재 `spec.md` 네 줄만 사용하고 제품 요구를 처음부터 다시 수집함
D) 기존 문서는 참고만 하고 이번에는 APP-05~APP-09 요구를 별도 재작성함
E) Other

[Recommended]: A
[Answer]:

### Q2. 새로 발견된 앱의 기본 분류 초기 상태는 무엇인가요?

A) `NEUTRAL`로 저장하고 이미 분류된 것으로 취급
B) `UNCLASSIFIED` 상태를 보존하고, 사용자가 선택하기 전 자동 판정에서는 `NEUTRAL`처럼 처리
C) 사용자가 분류하기 전 해당 앱의 Context 계산을 중단
D) 앱 이름·카테고리에서 기본 분류를 자동 추론
E) Other

[Recommended]: B
[Answer]:

### Q3. Activity가 Context 판정에 제공할 의미는 어떻게 정하나요?

A) Activity에 의도 분류를 명시한다. 기본 활동 종류는 제안값을 가지며 사용자가 변경할 수 있고 사용자 정의 활동은 직접 선택한다.
B) 운동·공부·개발·독서=`PRODUCTIVE`, 여가=`LEISURE`로 고정하고 변경을 허용하지 않는다.
C) Activity 종류만 저장하고 Context 엔진이 이름과 종류로 매번 추론한다.
D) Activity에는 분류를 두지 않고 모든 앱 중첩을 `MIXED`로 만든다.
E) Other

[Recommended]: A
[Answer]:

### Q4. 앱 Default와 Activity 의도 분류의 `명백한 충돌`은 무엇인가요?

A) 서로 다른 두 비중립 분류면 충돌이다. 같은 분류는 비충돌이며 어느 한쪽이 `NEUTRAL`이면 Activity 의미를 우선한다.
B) `PRODUCTIVE`와 `WASTE` 조합만 충돌이고 나머지는 Activity를 우선한다.
C) 서로 다른 모든 조합은 `NEUTRAL` 포함 여부와 관계없이 충돌이다.
D) 고정 행렬 없이 모든 중첩을 사용자 확인 대상으로 둔다.
E) Other

[Recommended]: A
[Answer]:

### Q5. AppSession 또는 Activity가 단독으로 존재하는 구간은 어떻게 표현하나요?

A) 앱 단독 구간은 앱 Default로 Context를 만들고, Activity 단독 구간은 Activity로만 유지해 앱 기반 Waste에 넣지 않는다.
B) 앱 단독과 Activity 단독 모두 Context를 만들며 `sessionId`와 `activityId`를 각각 선택 필드로 바꾼다.
C) 둘이 겹친 구간에만 Context를 만들고 앱 단독 구간은 AppSession만 유지한다.
D) Activity 단독 구간도 앱 사용으로 간주해 Context와 Waste를 계산한다.
E) Other

[Recommended]: A
[Answer]:

### Q6. 충돌 확인 질문의 답을 최종 Context로 어떻게 매핑하나요?

A) 생산 목적·보조 사용=`PRODUCTIVE`, 딴짓=`WASTE`, 의도적 휴식=`LEISURE`, 기타=사용자가 최종 enum을 추가 선택
B) 생산 목적=`PRODUCTIVE`, 보조 사용=`NEUTRAL`, 딴짓=`WASTE`, 의도적 휴식·기타=`LEISURE`
C) 답변 문구와 별도로 항상 최종 enum을 다시 선택
D) 답변은 근거만 저장하고 최종 Context는 자동 엔진이 다시 결정
E) Other

[Recommended]: A
[Answer]:

### Q7. 최초 7일 Baseline과 기간별 비교 기준은 어떻게 계산하나요?

A) 유효한 연속 7일의 Waste 합계와 일평균을 함께 보존하고, 비교 기간의 유효 일수에 일평균을 맞춰 예상 Baseline을 계산
B) 7일 Waste 합계 하나만 보존하고 완료된 주간 비교에만 사용
C) 월~일 각 요일별 Waste를 별도 Baseline으로 보존
D) 매일 최근 7일 이동평균으로 Baseline을 자동 교체
E) Other

[Recommended]: A
[Answer]:

### Q8. Baseline 관찰에서 유효일과 수집 공백은 어떻게 처리하나요?

A) 권한과 필요한 수집 범위가 유지된 완전한 현지 날짜만 유효하다. 확인된 0분은 유효하지만 수집 공백은 무효이며 연속 관찰을 다시 시작한다.
B) 데이터가 없는 날은 모두 Waste 0분인 유효일로 처리한다.
C) 무효일은 건너뛰고 비연속 7개 유효일을 누적한다.
D) 데이터가 없는 날마다 사용자가 유효 여부를 확인한다.
E) Other

[Recommended]: A
[Answer]:

### Q9. 현재 Waste가 비교 Baseline 이상이면 Saved는 어떻게 표현하나요?

A) `SavedTime = max(0, expectedBaseline - currentWaste)`로 유지하고, 증가분은 별도 변화량으로 표현
B) Saved에 음수를 허용해 증가한 Waste를 그대로 표시
C) 감소가 없으면 Saved 값을 생성하지 않고 `NOT_APPLICABLE` 상태로만 표현
D) 사용자가 음수 표시 여부를 설정
E) Other

[Recommended]: A
[Answer]:

### Q10. 여러 Goal 기록이 겹쳤지만 대표 Goal을 아직 선택하지 않은 경우 어떻게 집계하나요?

A) 비중첩 부분만 각 Goal에 반영하고, 겹친 부분은 `REPRESENTATIVE_REQUIRED`로 보류한 뒤 선택된 Goal에 한 번 반영
B) 먼저 시작한 Goal에 겹친 부분을 임시 배정하고 나중에 수정
C) 겹친 부분을 Goal 수로 균등 분배
D) 전체 Recovered에는 한 번 반영하되 Goal별 누적에는 대표 선택 전 반영하지 않음
E) Other

[Recommended]: A
[Answer]:

### Q11. Context 수정은 RecoveredTime에 어떤 영향을 주나요?

A) RecoveredTime 지속시간은 Goal 기록에서만 계산해 유지한다. Context 수정은 Waste·Saved와 그에 따른 Recovery Rate만 재계산한다.
B) 목표 활동 구간의 Context가 `PRODUCTIVE`일 때만 Recovered로 인정하므로 Context 수정이 Recovered 지속시간도 바꾼다.
C) 모든 RecoveredTime 기록이 별도 Context를 생성하고 그 Context 수정에 따라 지속시간을 바꾼다.
D) Context 수정 때마다 사용자에게 Recovered 포함 여부를 질문한다.
E) Other

[Recommended]: A
[Answer]:

### Q12. Saved가 0이거나 없는 경우 Recovery Rate와 정밀도는 어떻게 다루나요?

A) Saved가 0·미완료이면 `NOT_AVAILABLE`, Saved>0이고 Recovered=0이면 0%, 100% 상한은 두지 않으며 도메인은 반올림하지 않은 비율을 반환
B) Saved가 0이면 항상 0%, 결과는 정수 퍼센트로 반올림
C) Saved가 0이고 Recovered>0이면 무한대로 표시
D) 모든 비율을 최대 100%로 제한
E) Other

[Recommended]: A
[Answer]:

### Q13. 유효하지 않은 시간 구간과 현지 시간 경계는 어떻게 처리하나요?

A) `end <= start`는 저장 전에 거부하고, 지속시간은 절대 시간선으로 계산하며 현지 날짜·월요일 경계는 해당 시점의 지역 시간 규칙으로 분할
B) `end == start`는 0분 기록으로 저장하고 역전 구간만 거부
C) 종료가 시작보다 이르면 다음 날 종료로 자동 보정
D) 모든 입력을 저장하고 집계할 때 오류 구간을 제외
E) Other

[Recommended]: A
[Answer]:

### Q14. Goal과 타이머의 최소 유효성 규칙은 무엇인가요?

A) Goal 이름은 공백 제외 비어 있지 않고 목표시간은 양수여야 한다. 사용자별 실행 중 타이머는 하나만 허용하며 MANUAL 중첩은 저장 후 대표 Goal 규칙으로 해결한다.
B) 이름만 있으면 목표시간 0을 허용하고 여러 타이머를 동시에 실행할 수 있다.
C) 중첩 가능성을 없애기 위해 TIMER와 MANUAL 모두 기존 기록과 겹치면 저장을 거부한다.
D) 유효성 검사는 UI에서만 수행하고 도메인은 모든 값을 허용한다.
E) Other

[Recommended]: A
[Answer]:

## 9. Java 제약의 STEP 02 전달

Gate 1에서 Java의 세부 기술을 결정하지 않는다. 다음 항목을 CONSTRUCTION STEP 02 계획 질문으로 전달한다.

- Java 통일 범위가 Android 앱·도메인·백업 서버의 production code와 automated test code를 모두 포함하는지
- Gradle DSL, XML, JSON, Markdown, PowerShell 같은 설정·문서·자동화 파일은 언어 통일 대상에서 제외하는지
- Java/JDK 언어 수준과 Android 호환 수준
- 빌드 도구, 테스트 프레임워크, 시간·정밀도 표준 라이브러리 사용 기준
- `verify-domain`, `build-debug`, `verify-all`에 연결할 실제 명령

## 10. 검증 계획

### 문서 구조

- 계획 파일이 지정된 `aidlc-docs/construction/plans/` 경로에 있는지 확인한다.
- Gate 1 전 기능 설계 본 산출물과 `src/`가 생성되지 않았는지 확인한다.
- 질문마다 객관식 보기와 마지막 `Other` 선택지가 있는지 확인한다.

### 범위·추적성

- APP-05~APP-09가 FR-3~FR-8 및 관련 US·NFR에 연결되는지 확인한다.
- `track-domain-engine`을 별도 작업 단위나 별도 Gate로 만들지 않았는지 확인한다.
- CT-01~CT-06과 네 트랙의 협업 경계를 유지하는지 확인한다.
- Java 세부 선택이 STEP 01에 유입되지 않았는지 확인한다.

### 시간 도메인

- `[start, end)`, 음수 구간 금지, 실제 경과시간 중복 집계 금지를 확인한다.
- 사용자 확정 Context가 자동 결과보다 우선하는지 확인한다.
- Baseline 미완료, Saved 0, Recovered 초과, 목표 중첩 상태가 값과 구분되는지 확인한다.

### 개인정보·테스트

- 실제 앱 목록·활동 기록을 예시나 fixture에 넣지 않는다.
- 합성 데이터, 가짜 저장소, 제어 시간으로 검증 가능해야 한다.
- 서버나 UI에 TimeBack 도메인 계산을 중복 배치하지 않는다.

## 11. Gate 1 완료 조건

- 사용자가 이 계획과 Q1~Q14의 답을 승인한다.
- 기존 Inception 문서를 앞으로의 설계 입력으로 사용할지 결정된다.
- APP-05~APP-09의 미확정 정책이 기능 설계 가능한 수준으로 결정된다.
- Java 세부 선택이 STEP 02로 명확히 이관된다.
- audit에 Gate 1 응답을 append하고 `aidlc-state.md`를 `ARTIFACT_GENERATION`으로 갱신할 수 있다.

## 12. Gate 1 선택지

1) 수정 요청
2) 다음 단계로
