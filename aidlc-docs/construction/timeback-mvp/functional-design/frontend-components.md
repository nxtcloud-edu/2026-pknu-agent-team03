# TimeBack STEP 01 — 도메인 연계 프런트엔드 구성 요소

## 1. 목적과 원칙

이 문서는 APP-05~APP-09 결과를 사용하는 기존 UI-02~UI-07의 기능 상태와 사용자 작업을 구체화한다. 새 화면, UI 프레임워크, 디자인 시스템, 코드 구조는 만들지 않는다.

핵심 원칙:

- UI는 시간 구간·Waste·Baseline·Saved·Recovered·Recovery Rate를 직접 계산하지 않는다.
- `0`, `관찰 중`, `데이터 불완전`, `결정 필요`, `오류`를 서로 다른 상태로 표시한다.
- App Default와 final Context를 같은 설정으로 보이게 하지 않는다.
- 사용자 확정 결과와 자동 판정 이유를 설명하고 수정 경로를 제공한다.
- 실제 앱·활동·목표 이름을 로그·미리보기 fixture 기본값으로 사용하지 않는다.

## 2. 공통 화면 상태

### FE-C01. 비동기 상태

모든 조회·변경 영역은 다음 상태를 구분한다.

- `IDLE`
- `LOADING`
- `CONTENT`
- `EMPTY`
- `DECISION_REQUIRED`
- `DATA_INCOMPLETE`
- `ERROR`

이전 `CONTENT`가 있는 새로고침 실패는 기존 값을 지우지 않고 오래된 값임을 표시한다. 저장 실패는 사용자가 입력한 편집 값을 복구 가능하게 유지하되 성공으로 표시하지 않는다.

### FE-C02. 지표 표시 상태

| 도메인 상태 | UI 의미 |
|---|---|
| `VALUE` | 숫자와 기간 표시 |
| `OBSERVING` | 남은 유효일 수와 관찰 이유 표시, Saved·Rate 숨김 |
| `DATA_INCOMPLETE` | 수집 공백 안내, 확정 개선 수치 표시 금지 |
| `SAVED_ZERO` | Saved 0 표시, Recovery Rate는 계산 불가로 표시 |
| `DECISION_REQUIRED` | 사용자 확인 전 확정 합계와 pending을 구분 |
| `IN_PROGRESS` | 현재 진행 중 기간의 잠정 상태임을 표시 |

UI는 `NOT_AVAILABLE`을 숫자 0%로 바꾸지 않는다.

### FE-C03. 오류 표시

- 업무 검증 오류: 수정할 입력과 이유를 해당 입력 가까이에 표시
- 상태 충돌: 최신 상태를 다시 조회하고 사용자가 선택할 수 있게 함
- 의존성 오류: 기존 데이터 보존, 재시도 제공
- 상세 package name·행동 시각을 일반 오류 문구나 분석 로그에 포함하지 않음

## 3. UI-04 앱 관리 — APP-05

### FE-04-01. 목록 상태

| 상태 | 표시 |
|---|---|
| `LOADING` | 앱 목록과 저장 분류 조회 중 |
| `CONTENT` | 앱별 표시 이름과 현재 Default |
| `UNCLASSIFIED_PRESENT` | 미분류 앱을 명시적으로 구분 |
| `EMPTY` | 설치 앱 목록을 읽을 수 있으나 항목 없음 |
| `ERROR` | 조회 실패, 기존 저장 분류 훼손 금지 |

`UNCLASSIFIED`는 `NEUTRAL`과 다른 표시다. 자동 분석에서 NEUTRAL fallback이 사용된다는 점을 필요 시 설명하되, 사용자가 이미 NEUTRAL을 선택한 것처럼 표시하지 않는다.

### FE-04-02. 분류 변경 흐름

```text
앱 선택
  → PRODUCTIVE/LEISURE/WASTE/NEUTRAL 중 하나 선택
  → 저장 중
  ├─ 성공: 새 Default 표시 + 관련 자동 Context 재계산 상태
  └─ 실패: 이전 Default 유지 + 재시도
```

안내:

- 앱 분류는 평소 기본값이며 시간대별 final Context와 다를 수 있다.
- Default 변경이 사용자 확정 Context를 덮어쓰지 않는다고 설명한다.

## 4. UI-03 Timeline — APP-06·APP-07

### FE-03-01. Timeline 항목

각 시간 구간은 가능한 범위에서 다음을 표시한다.

- 시작·종료 시각과 실제 지속시간
- App 표시 정보
- Activity 종류/사용자 정의 이름
- final classification
- 자동 판정 또는 사용자 확인 상태
- 판정 이유 요약
- 복합 활동 여부
- 데이터 완전성

여러 관계가 같은 실제 구간에 있으면 겹침을 시각적으로 표현하되 지속시간을 더해 실제 경과시간보다 크게 표시하지 않는다.

### FE-03-02. Activity 생성·수정

입력:

- Activity 종류
- CUSTOM 이름
- 제안된 의도 분류와 사용자의 변경
- 시작·종료 시각

검증:

- 종료가 시작보다 뒤인지 확인
- CUSTOM 이름이 있는지 확인
- 의도 분류를 사용자가 확인했는지 확인

UI 사전 검증과 별개로 APP-06 결과를 최종 기준으로 사용한다.

성공 후:

- 영향 구간 `RECALCULATING` 표시 가능
- 재조회된 Context와 지표로 화면 갱신
- 이전 화면 값만 바꾸고 저장 결과를 추정하지 않음

### FE-03-03. MIXED 확인

`CONFIRMATION_REQUIRED` Context에만 질문 작업을 표시한다.

선택:

- 생산 목적
- 보조 사용
- 딴짓
- 의도적 휴식
- 기타

`기타` 선택 시 `PRODUCTIVE|LEISURE|WASTE|NEUTRAL` 중 최종 분류를 추가 선택해야 완료 버튼을 활성화한다.

미응답 상태:

- `MIXED` 유지
- Waste 확정 합계에 포함하지 않음
- 질문이 필요한 구간임을 표시

### FE-03-04. Context 직접 수정

- 확정 선택은 `PRODUCTIVE|LEISURE|WASTE|NEUTRAL`
- `MIXED`를 최종 수정값으로 제공하지 않음
- 저장 성공 뒤 `사용자 확인됨`과 수정 이유 표시
- App Default 변경 작업과 명확히 분리
- 실패 시 기존 final Context 유지

### FE-03-05. 사용자 결정 설명

사용자 확정 구간에는 다음 의미를 설명할 수 있어야 한다.

- 자동 판정이 무엇이었는지
- 어떤 사용자 답변·수정으로 바뀌었는지
- 이후 자동 분석에서도 사용자 결정을 우선한다는 점

내부 식별자나 원본 UsageEvent 전체는 표시하지 않는다.

## 5. UI-02 홈 대시보드 — APP-08·APP-09

### FE-02-01. Baseline 관찰 중

표시:

- 연속 유효일 수
- 남은 유효일 수
- 관찰이 다시 시작된 경우 수집 공백·권한 조건에 대한 일반 안내
- 오늘 Waste 상태

숨김/대체:

- Saved 숫자 숨김
- Recovery Rate 숫자 숨김
- `0`으로 대신 표시하지 않음

### FE-02-02. Baseline 활성 상태

표시:

- 오늘 또는 선택 기간 Waste
- expected Baseline
- signed 변화량
- non-negative Saved
- 기간이 진행 중인지 여부

Waste가 Baseline보다 많을 때 Saved는 0이지만 증가분은 별도 변화 방향으로 표시한다.

### FE-02-03. Recovery 상태

| 상태 | 표시 |
|---|---|
| `AVAILABLE` | Recovered, Saved, 비율; 100% 초과 허용 |
| `SAVED_ZERO` | Recovered와 Saved 0, 비율은 계산 불가 |
| `BASELINE_OBSERVING` | Recovered는 표시 가능, rate는 관찰 중 |
| `DATA_INCOMPLETE` | 확정 비교 불가 안내 |
| `DECISION_REQUIRED` | 확정 Recovered와 대표 선택 대기 시간을 구분 |

표시 반올림은 원본 비율을 바꾸지 않으며 구체 자릿수는 STEP 02에서 확정한다.

### FE-02-04. Baseline 재산정 제안

- 현재 Baseline과 새 7일 후보를 구분해 표시
- 승인 전 자동 교체되지 않는다고 설명
- `승인`과 `유지` 동작 제공
- 저장 실패 시 현재 Baseline 유지

## 6. UI-05 시간 되찾기 — APP-09

### FE-05-01. 목표 없음

- `EMPTY` 상태와 목표 생성 이동 제공
- 타이머 시작·수동 기록 동작 비활성

### FE-05-02. 타이머 대기·실행

대기:

- Goal 선택
- 시작 동작

실행:

- 선택 Goal
- 시작 시각
- 현재 경과 표시값
- 완료와 취소

화면의 실시간 경과 표시는 참고이며 완료 RecoveredTime은 APP-09가 시작·완료 시각으로 계산한 값을 사용한다.

동시에 다른 타이머 시작 시:

- 기존 실행 Goal 안내
- 기존 타이머 완료·취소 경로 제공
- 두 번째 타이머가 시작됐다고 표시하지 않음

### FE-05-03. 직접 기록

입력:

- Goal
- 시작·종료 시각

- 기존 기록과 겹친다는 이유만으로 입력을 차단하지 않는다.
- 저장 후 overlap 분석 결과에 따라 대표 Goal 선택 상태로 이동할 수 있다.

### FE-05-04. 대표 Goal 선택

표시:

- 겹친 시간 구간
- 후보 Goal 목록
- 현재 확정 누적과 pending 시간

선택 후:

- 겹친 구간을 대표 Goal 하나에만 반영
- 전체 Recovered가 실제 경과시간을 초과하지 않는 결과 표시

후보가 아닌 Goal을 선택하거나 원본 기록이 바뀐 경우 최신 overlap을 다시 불러온다.

## 7. UI-06 목표 — APP-09

### FE-06-01. Goal 생성

입력:

- 공백 제외 비어 있지 않은 이름
- 양수 목표시간

오류 입력은 저장하지 않고 필드별 이유를 표시한다.

### FE-06-02. Goal 목록·상세

표시:

- target duration
- 확정 accumulated duration
- 상한 없는 progress ratio
- 대표 선택 대기 시간
- 타이머/직접 기록 이동

100% 초과 진행률을 데이터 오류로 취급하거나 100%로 잘라 저장하지 않는다. 시각 표현의 최대 길이와 텍스트 표시는 STEP 02 UI 품질 결정으로 넘긴다.

## 8. UI-07 리포트 — APP-08·APP-09·APP-10

### FE-07-01. 기간 선택

- 일간·주간·월간 기간을 APP-10에 전달
- UI가 현지 자정·월요일 경계를 직접 계산하지 않음
- APP-10/OS-05가 반환한 `[periodStart, periodEnd)`를 표시 기준으로 사용

### FE-07-02. 리포트 결과

표시:

- 기간과 `COMPLETE|IN_PROGRESS`
- Waste
- Baseline 상태와 expected Baseline
- signed 변화량과 Saved
- Goal별 Recovered
- 전체 Recovered
- Recovery Rate 상태와 조건부 값
- pending overlap

### FE-07-03. 데이터 없음과 0 구분

- `EMPTY`: 해당 기간에 기준 데이터가 없음
- `VALUE=0`: 완전한 데이터로 계산된 0
- `DATA_INCOMPLETE`: 데이터는 있으나 확정 비교 불가
- `OBSERVING`: Baseline 선행 조건 진행 중

빈 상태에서 임의의 0 통계를 만들지 않는다.

## 9. 화면 간 갱신 흐름

### FE-F01. App Default 변경

```text
UI-04 저장
  → APP-05 성공
  → APP-07 자동 Context 재분석
  → APP-08 지표 재계산
  → APP-09 Recovery Rate 재계산
  → UI-03·UI-02·UI-07 새 조회 결과
```

USER_CONFIRMED Context는 유지된다.

### FE-F02. Activity 또는 Context 변경

```text
UI-03 저장
  → APP-06 또는 APP-07 성공
  → 영향 구간 재분석
  → Waste/Saved 재계산
  → 같은 기간 Recovery Rate 재계산
  → Timeline·홈·리포트 갱신
```

RecoveredTime duration 자체는 바뀌지 않는다.

### FE-F03. Goal 기록

```text
UI-05 TIMER/MANUAL 저장
  → APP-09 overlap 분석
  ├─ 대표 불필요: GoalProgress·Recovered 갱신
  └─ 대표 필요: 확정 부분 + pending 표시
       → 사용자 대표 선택
       → GoalProgress·Recovered·Rate 갱신
```

## 10. 사용자 문구 의미 기준

문구는 비난·강제가 아니라 상태와 선택을 설명한다.

- `WASTE`: 사용자가 원하지 않았던 시간으로 판정·확정된 구간
- `UNCLASSIFIED`: 앱의 기본값을 아직 선택하지 않은 상태
- `MIXED`: 앱과 활동 의미가 달라 사용자의 확인이 필요한 상태
- `Saved`: Baseline보다 감소한 Waste, 음수가 아님
- `Recovered`: 목표 활동으로 실제 기록·배정된 시간
- `Recovery Rate unavailable`: 실패가 아니라 Saved가 0이거나 Baseline 선행 조건이 없는 상태

추천이나 경고는 사용자가 선택 가능한 다음 행동으로 표현한다.

## 11. 접근성과 개인정보 기능 경계

- 색상만으로 PRODUCTIVE/LEISURE/WASTE/MIXED/NEUTRAL을 구분하지 않는다.
- loading, error, decision required 상태에 텍스트 의미를 제공한다.
- 앱·활동·목표 상세값을 분석 이벤트나 오류 보고에 기본 포함하지 않는다.
- 화면 내용·YouTube 내부 콘텐츠·위치를 수집하거나 판정 근거로 요구하지 않는다.
- 합성 미리보기에는 `예시 앱 A`, `예시 활동`, `예시 목표`만 사용한다.

## 12. 프런트엔드 계약 검증 사례

- FE-T01: UI-04가 UNCLASSIFIED와 사용자가 고른 NEUTRAL을 다르게 표시한다.
- FE-T02: MIXED가 아닌 자동 Context에는 확인 질문이 나타나지 않는다.
- FE-T03: OTHER 답변의 final enum 없이는 확인 완료가 되지 않는다.
- FE-T04: Baseline 1~6일에는 Saved·Rate 숫자가 보이지 않는다.
- FE-T05: 완전 측정 Waste 0과 데이터 없음이 다른 상태로 표시된다.
- FE-T06: Saved 0, Recovered 양수일 때 0% 대신 rate unavailable이 표시된다.
- FE-T07: 150% Recovery Rate가 100%로 잘리지 않는다.
- FE-T08: Goal overlap pending이 확정 Recovered와 분리된다.
- FE-T09: Context 수정 후 Recovered 숫자는 유지되고 Rate만 새 Saved 기준으로 바뀐다.
- FE-T10: 저장 실패 후 이전 기준값을 성공값으로 덮어 표시하지 않는다.
- FE-T11: UI가 주간 경계나 구간 합집합을 직접 계산하지 않는다.
- FE-T12: 합성 fixture와 오류 메시지에 실제 행동 데이터가 없다.

## 13. STEP 02 이후 결정

- 화면 프레임워크와 상태 관리 방식
- 표시 비율 자릿수·반올림과 기간별 형식
- 재계산 진행 상태의 동기·비동기 UX
- 접근성 검증 도구와 UI 자동 테스트 도구
- 5초 갱신 NFR을 측정할 대표 데이터 규모
