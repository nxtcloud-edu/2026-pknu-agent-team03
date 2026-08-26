# TimeBack STEP 01 — APP-05~APP-09 업무 규칙

## 1. 목적

이 문서는 Gate 1에서 승인된 Q1~Q14를 실행 가능한 언어 중립 업무 규칙으로 구체화한다. 각 규칙은 APP-05~APP-09, 관련 FR/NFR/US, `domain-model.md`의 논리 모델에 연결된다.

## 2. 공통 처리 원칙

### BR-C01. 검증 순서

모든 변경 작업은 다음 순서를 따른다.

1. 입력 형식과 필수 값 확인
2. 참조 엔터티 존재 확인
3. 시간 구간 유효성 확인
4. 현재 상태에서 작업 허용 여부 확인
5. 순수 업무 결과 계산
6. APP-11 계약을 통한 원자적 저장 요청
7. 영향 범위 재계산 요청 또는 변경 통지

검증 실패 시 기존 기준 데이터를 변경하지 않는다.

### BR-C02. 구간 연산

- 모든 입력은 `[start, end)`로 정규화한다.
- 경계점 집합을 정렬하고 인접 경계 사이를 원자 구간으로 만든다.
- 0 길이 원자 구간은 만들지 않는다.
- 집계는 해당 분류의 원자 구간 합집합을 한 번만 더한다.
- 여러 Context나 목표 기록이 같은 구간을 가리켜도 원시 duration을 단순 합산하지 않는다.

### BR-C03. 사용자 결정 우선순위

우선순위는 다음과 같다.

1. 현재 원자 구간을 덮는 사용자 확정 Context
2. 사용자의 현재 충돌 확인 답변
3. Activity 의도와 App Default를 사용한 자동 판정
4. App Default 또는 `UNCLASSIFIED → NEUTRAL` fallback

자동 재분석은 1번 결과를 덮어쓰지 않는다.

### BR-C04. 재계산 범위

변경 원인별 최소 영향 범위:

| 변경 | 직접 재계산 | 연쇄 결과 |
|---|---|---|
| App Default 변경 | 해당 앱의 미확정/자동 Context | Waste, Baseline 후보, Saved, Recovery Rate, 조회 결과 |
| Activity 생성·수정 | Activity와 겹치는 세션 Context | Waste, Baseline 후보, Saved, Recovery Rate, 조회 결과 |
| Context 확인·수정 | 해당 Context 기간 | Waste, Baseline 후보, Saved, Recovery Rate, 조회 결과 |
| Baseline 승인 | Saved | Recovery Rate, 조회 결과 |
| Goal/Recovered 변경 | 목표 원자 구간 | GoalProgress, Recovered, Recovery Rate, 조회 결과 |
| 대표 Goal 선택 | 선택 구간 | GoalProgress, Recovered, Recovery Rate, 조회 결과 |

Context 수정은 RecoveredTime 레코드와 지속시간을 바꾸지 않는다.

## 3. APP-05 App Classification

### BR-05-01. 앱 조회

- 설치 앱과 APP-11의 저장 분류를 `packageName`으로 결합한다.
- 저장 분류가 없으면 `UNCLASSIFIED`를 반환한다.
- `UNCLASSIFIED`를 저장된 `NEUTRAL`로 위장하지 않는다.
- 앱 목록 읽기 실패 시 기존 저장 분류를 삭제하거나 초기화하지 않는다.

### BR-05-02. 기본 분류 저장

입력: `packageName`, `PRODUCTIVE|LEISURE|WASTE|NEUTRAL`, 변경 시각.

- 대상 앱이 존재해야 한다.
- `MIXED`는 App Default로 거부한다.
- 저장 성공 후 상태는 `CLASSIFIED`다.
- 같은 값을 다시 저장해도 의미가 달라지지 않는 멱등 결과를 반환한다.
- 영향 앱의 자동 Context만 재분석한다.

### BR-05-03. 확정 Context 보호

App Default가 바뀌어도 해당 앱의 `USER_CONFIRMED` Context는 분류·상태·근거를 유지한다. 재분석 경계에는 이 Context의 시작·종료를 포함해 주변 자동 구간만 다시 계산한다.

## 4. APP-06 Activity Management

### BR-06-01. Activity 생성·수정

필수 입력:

- 정의된 Activity type
- `CUSTOM`이면 공백 제외 비어 있지 않은 `customName`
- `PRODUCTIVE|LEISURE|WASTE|NEUTRAL` 의도 분류
- 유효한 `[startAt, endAt)`

유효하지 않으면 저장하지 않고 오류를 반환한다. UI 제안 분류는 사용자가 저장한 의도 분류를 대체하지 않는다.

### BR-06-02. 현지 날짜 분할

원본 Activity는 하나로 유지한다. 날짜별 계산 시 요청 기간과 Activity의 교집합을 자정 경계로 분할한다.

예시(합성 시각):

```text
Activity: D1 23:40 ~ D2 00:20
D1 slice: 23:40 ~ 24:00 = 20분
D2 slice: 00:00 ~ 00:20 = 20분
합계: 40분
```

DST나 시간대 전환일에도 표시 시각 차이가 아니라 절대 시간선의 실제 지속시간 합계가 보존돼야 한다.

### BR-06-03. Activity 수정 영향

수정 전 구간과 수정 후 구간의 합집합을 영향 범위로 삼아 Context를 재분석한다. 삭제가 후속 요구로 허용될 경우에도 같은 영향 규칙을 사용하되, 삭제 기능 자체는 현재 문서에서 새 요구로 만들지 않는다.

## 5. APP-07 Context Analysis

### BR-07-01. 분석 입력

분석 범위의 다음 데이터를 읽는다.

- 유효 AppSession
- App 분류 상태
- 겹치는 Activity와 의도 분류
- 겹치는 기존 `USER_CONFIRMED` Context

기존 사용자 확정 Context의 경계도 원자 구간 경계점에 포함한다.

### BR-07-02. 원자 구간 생성

1. 분석 범위 시작·종료, Session 시작·종료, Activity 시작·종료, 사용자 확정 Context 시작·종료를 경계점으로 수집한다.
2. 중복 경계를 제거해 시간순으로 정렬한다.
3. 인접 경계 쌍마다 실제 입력이 하나 이상 존재하는 구간만 AtomicSegment로 만든다.
4. 각 세션과 Activity 조합별 Context 후보를 만든다.
5. 입력 Session이 겹쳐도 Waste 집계는 최종 WASTE 구간 합집합으로 계산한다.

### BR-07-03. 앱 단독 구간

Activity가 없는 AppSession 원자 구간:

- `CLASSIFIED`: App Default로 `AUTO_CLASSIFIED` Context 생성
- `UNCLASSIFIED`: `NEUTRAL` fallback으로 `AUTO_CLASSIFIED` Context 생성
- `activityId`: 없음
- fallback 사용 여부를 `decisionReason`에 남긴다.

Activity만 있고 AppSession이 없는 구간은 Activity로 유지하며 앱 기반 Context나 Waste를 만들지 않는다.

### BR-07-04. 앱·Activity 중첩 판정

각 세션·Activity 원자 구간에 다음 표를 적용한다.

| App 의미 | Activity 의도 | 결과 |
|---|---|---|
| 같은 분류 | 같은 분류 | Activity 의도, `AUTO_CLASSIFIED` |
| 어느 한쪽 `NEUTRAL` | 모든 유효 분류 | Activity 의도, `AUTO_CLASSIFIED` |
| 서로 다른 두 비중립 분류 | 서로 다름 | `MIXED`, `CONFIRMATION_REQUIRED` |
| App `UNCLASSIFIED` fallback | 모든 Activity 의도 | Activity 의도, `AUTO_CLASSIFIED` |

질문은 `CONFIRMATION_REQUIRED` 구간에만 생성한다.

### BR-07-05. 사용자 답변 매핑

| 답변 | 최종 분류 | 추가 입력 |
|---|---|---|
| `PRODUCTIVE_PURPOSE` | `PRODUCTIVE` | 없음 |
| `ASSISTIVE_USE` | `PRODUCTIVE` | 없음 |
| `DISTRACTION` | `WASTE` | 없음 |
| `INTENTIONAL_REST` | `LEISURE` | 없음 |
| `OTHER` | 사용자가 고른 final enum | `PRODUCTIVE|LEISURE|WASTE|NEUTRAL` 중 하나 |

완료된 답변은 `USER_CONFIRMED`, `userConfirmed=true`로 저장한다. `OTHER`에 final enum이 없으면 기존 `MIXED`를 유지하고 저장 완료로 처리하지 않는다.

### BR-07-06. Timeline 직접 수정

사용자는 Context를 `PRODUCTIVE`, `LEISURE`, `WASTE`, `NEUTRAL` 중 하나로 수정할 수 있다. 직접 수정 결과는 `USER_CONFIRMED`이며 이전 Context를 대체한 관계를 남긴다. `MIXED`를 확정 결과로 직접 선택하지 않는다.

### BR-07-07. 사용자 확정 결과 재적용

재분석 시 기존 확정 Context와 새 원자 구간의 교집합에 기존 최종 분류를 적용한다. 기존 확정 범위 밖에서만 자동 판정한다. 입력 변경으로 확정 구간 일부에 Session이 없어지면 원본 확정 기록은 감사 근거로 유지할 수 있으나 지표에는 실제 Session과 겹치는 부분만 반영한다.

### BR-07-08. 여러 Activity 또는 Session

- 하나의 Session이 여러 Activity와 겹치면 각 `sessionId + activityId + atomic interval` 관계를 보존한다.
- 같은 분류 구간이 여러 Context로 표현돼도 집계는 시간 합집합을 사용한다.
- 의미가 서로 다른 Context가 같은 실제 구간에 존재할 수 있으나 총 지표는 기간의 실제 경과시간을 초과하지 않는다.
- UI는 복합 관계를 표시할 수 있지만 임의로 하나를 최종 대표 의미로 만들지 않는다.

## 6. APP-08 Time Metrics

### BR-08-01. Waste 계산

입력 기간의 Context 중 다음 조건을 만족하는 구간만 사용한다.

- `classification == WASTE`
- `decisionStatus == AUTO_CLASSIFIED` 또는 `USER_CONFIRMED`
- 실제 AppSession과 겹치는 유효 구간

모든 대상 구간의 합집합 길이가 Waste다. `MIXED`, `CONFIRMATION_REQUIRED`, Activity 단독 구간은 제외한다.

```text
Waste(P) = duration(union(WASTE context intervals ∩ P))
```

### BR-08-02. MeasurementDay 완전성

- Usage Access와 필요한 수집 범위가 현지 날짜 전체에 유지돼야 `COMPLETE`다.
- 완전 수집 결과 WASTE가 없으면 `confirmedZero=true`, Waste 0으로 유효하다.
- 권한 누락·철회 또는 수집 공백은 `INCOMPLETE`이며 0으로 대체하지 않는다.
- 권한을 하루 중간에 처음 허용한 날은 완전 날짜가 아니며 다음 현지 자정 이후 첫 전체 날짜부터 관찰할 수 있다.

### BR-08-03. 최초 Baseline

1. 최초 `COMPLETE` MeasurementDay에서 관찰을 시작한다.
2. 연속 날짜의 `COMPLETE` 결과를 누적한다.
3. 하나라도 불완전하면 현재 관찰을 무효화하고 다음 완전 날짜부터 다시 시작한다.
4. 7개 연속 유효일에 도달하면 합계와 일평균을 계산한다.
5. 최초 Baseline을 `ACTIVE`로 저장한다.

```text
weeklyWaste = sum(dayWaste[1..7])
dailyAverage = weeklyWaste / 7
```

관찰 중에는 Saved와 Recovery Rate를 계산하지 않고 남은 유효일 수를 제공한다.

### BR-08-04. 재산정 후보

활성 Baseline 이후 완성된 다음 비중첩 7일 유효 창의 값이 현재 Baseline과 다르면 후보를 `PROPOSED`로 제공한다. 사용자가 승인하기 전 기존 Baseline은 유지된다.

- 승인: 기존 Baseline `SUPERSEDED`, 후보를 새 `ACTIVE`로 저장
- 거절: 후보 `REJECTED`, 기존 Baseline 유지
- 정량 차이 임계값은 만들지 않는다.

### BR-08-05. 기간별 expected Baseline

```text
expectedBaseline(P) = activeBaseline.dailyAverage × validDayEquivalent(P)
```

- 완전 일간: 1
- 완전 주간: 포함된 유효 날짜 수
- 월간: 포함된 유효 날짜 수
- 현재 진행 중 일·주: 동일 식을 사용하되 `periodStatus=IN_PROGRESS`로 표시
- 불완전 날짜가 포함되면 `DATA_INCOMPLETE`로 구분하며 확정 개선 결과로 사용하지 않는다.

### BR-08-06. Saved와 변화량

```text
wasteDelta = expectedBaseline - currentWaste
savedDuration = max(0, wasteDelta)
```

Waste가 Baseline보다 많으면 `savedDuration=0`이고 음수 `wasteDelta`로 증가 사실을 별도 표현한다. Baseline 미완료·데이터 불완전 상태에서는 Saved 값이 없다.

## 7. APP-09 Goal Recovery

### BR-09-01. Goal 생성

- 이름은 공백 제거 후 비어 있지 않아야 한다.
- `targetDuration > 0`이어야 한다.
- 진행률은 저장 필드가 아니라 배정 완료된 RecoveredTime에서 다시 계산한다.

### BR-09-02. 타이머 시작

- 대상 Goal이 존재해야 한다.
- 사용자에게 실행 중 타이머가 없어야 한다.
- 시작 시각은 OS-05 제어 시간 계약에서 받는다.
- 동일 시작 요청의 중복 저장을 방지할 호출 식별자를 계약에서 사용할 수 있으나 구현 방식은 STEP 02로 넘긴다.

실행 중 타이머가 있으면 `TIMER_ALREADY_RUNNING`을 반환하고 기존 타이머를 유지한다.

### BR-09-03. 타이머 완료·취소

완료:

- 현재 실행 중 타이머가 존재해야 한다.
- 완료 시각이 시작보다 뒤여야 한다.
- `[startAt, completedAt)`의 `TIMER` RecoveredTime을 저장한다.
- 저장 성공 뒤 실행 중 타이머를 제거한다.

취소:

- RecoveredTime을 만들지 않는다.
- 실행 중 타이머를 제거한다.

저장 실패 시 기존 타이머를 임의로 완료 처리하지 않는다.

### BR-09-04. 수동 기록

- Goal이 존재해야 한다.
- `[startAt, endAt)`가 유효해야 한다.
- `MANUAL`로 저장한다.
- 기존 기록과의 중첩을 이유로 입력 자체를 거부하지 않는다.

### BR-09-05. 목표 원자 구간과 대표 선택

1. 조회 기간 내 모든 RecoveredTime의 시작·종료를 경계로 분할한다.
2. 한 Goal만 덮는 원자 구간은 그 Goal에 자동 배정한다.
3. 여러 Goal이 덮는 원자 구간은 기존 OverlapResolution을 찾는다.
4. 유효한 대표 선택이 없으면 `REPRESENTATIVE_REQUIRED`로 보류한다.
5. 선택되면 겹친 구간 전체를 대표 Goal 하나에 배정한다.

원본 기록은 유지하고 배정 결과만 별도로 저장·재현한다.

합성 예시:

```text
Goal-A: 10:00~11:00
Goal-B: 10:30~11:30

자동 배정 A: 10:00~10:30 = 30분
대표 필요: 10:30~11:00 = 30분
자동 배정 B: 11:00~11:30 = 30분

대표 선택 전 확정 Recovered = 60분, pending = 30분
A 선택 후 확정 Recovered = 90분
A 누적 = 60분, B 누적 = 30분
```

### BR-09-06. 대표 Goal 선택

- 후보 Goal 중 하나만 선택할 수 있다.
- 선택 구간은 실제 미결정 overlap segment와 일치해야 한다.
- 선택 변경 시 같은 구간의 이전 배정을 대체하고 총 경과시간은 변하지 않는다.
- 삭제·수정된 원본 기록으로 후보 집합이 바뀌면 기존 resolution의 유효성을 다시 확인한다.

### BR-09-07. 누적시간과 진행률

```text
accumulated(goal, P) = sum(assigned atomic durations for goal within P)
progressRatio = accumulated / targetDuration
```

- 대표 선택 대기 구간은 누적에서 제외하고 별도 pending으로 제공한다.
- 진행률은 100%를 초과할 수 있고 상한을 두지 않는다.
- 목표별 합계와 전체 Recovered 모두 같은 배정 원자 구간을 사용한다.

### BR-09-08. Recovered와 Recovery Rate

```text
recovered(P) = sum(all assigned recovery atomic durations within P)
```

동일 기간 APP-08 결과에 따라:

| 조건 | 상태 | 비율 |
|---|---|---|
| Baseline 관찰 중 | `BASELINE_OBSERVING` | 없음 |
| 데이터 불완전 | `DATA_INCOMPLETE` | 없음 |
| Saved = 0 | `SAVED_ZERO` | 없음 |
| Saved > 0, Recovered = 0 | `AVAILABLE` | 0 |
| Saved > 0 | `AVAILABLE` | `Recovered / Saved` |

Recovered는 Saved를 초과해도 자르지 않으며 비율은 100%를 초과할 수 있다. 도메인은 반올림하지 않은 비율을 반환한다.

### BR-09-09. Context 수정 영향

Context 수정은 RecoveredTime과 목표 배정을 변경하지 않는다. APP-08의 Waste·Saved가 바뀌므로 같은 기간 Recovery Rate만 다시 계산한다.

## 8. 오류 의미

| 오류 코드 | 의미 | 데이터 변경 |
|---|---|---|
| `INVALID_INTERVAL` | `endAt <= startAt` | 없음 |
| `APP_NOT_FOUND` | 대상 App 없음 | 없음 |
| `INVALID_DEFAULT_CLASSIFICATION` | App Default에 허용되지 않은 값 | 없음 |
| `ACTIVITY_NOT_FOUND` | 수정 대상 Activity 없음 | 없음 |
| `CUSTOM_NAME_REQUIRED` | CUSTOM 이름 없음 | 없음 |
| `FINAL_CLASSIFICATION_REQUIRED` | OTHER 답변의 최종 enum 없음 | 없음 |
| `CONTEXT_NOT_FOUND` | 확인·수정 대상 Context 없음 | 없음 |
| `BASELINE_NOT_READY` | Saved 계산 선행 조건 없음 | 없음 |
| `GOAL_NOT_FOUND` | 대상 Goal 없음 | 없음 |
| `INVALID_GOAL_NAME` | Goal 이름이 비어 있음 | 없음 |
| `INVALID_TARGET_DURATION` | 목표시간이 양수가 아님 | 없음 |
| `TIMER_ALREADY_RUNNING` | 기존 실행 타이머 존재 | 기존 타이머 유지 |
| `TIMER_NOT_RUNNING` | 완료·취소 대상 없음 | 없음 |
| `INVALID_REPRESENTATIVE_GOAL` | 후보가 아닌 Goal 선택 | 기존 resolution 유지 |
| `DATA_INCOMPLETE` | 측정 기간 완전성 부족 | 확정 지표 생성 없음 |

저장소·시간 경계·조회 실패는 업무 거부와 구분되는 의존성 오류로 전달한다.

## 9. 필수 합성 검증 사례

- BR-T01: UNCLASSIFIED 앱 단독 세션은 NEUTRAL 자동 Context지만 App은 계속 UNCLASSIFIED다.
- BR-T02: App WASTE와 Activity PRODUCTIVE 중첩은 MIXED이고 질문 대상이다.
- BR-T03: App NEUTRAL과 Activity PRODUCTIVE 중첩은 PRODUCTIVE 자동 Context다.
- BR-T04: 사용자 WASTE 수정은 App Default 변경 뒤에도 유지된다.
- BR-T05: 60분 Activity 안의 50분 Session을 110분으로 집계하지 않는다.
- BR-T06: 겹치는 두 WASTE Context는 합집합만 Waste에 포함한다.
- BR-T07: 완전 수집된 Waste 0일은 Baseline 유효일이다.
- BR-T08: 관찰 4일 뒤 수집 공백이면 다음 완전일에서 1일차로 재시작한다.
- BR-T09: 7일 Waste 합계 700분이면 일평균은 100분이다.
- BR-T10: expected 100분, current 130분이면 Saved 0, delta -30분이다.
- BR-T11: Saved 0이고 Recovered 30분이면 rate는 `SAVED_ZERO`, 숫자 없음이다.
- BR-T12: Saved 60분, Recovered 90분이면 rate는 1.5이며 상한이 없다.
- BR-T13: 목표 중첩 30분은 대표 선택 전 pending이고 확정 누적에서 제외된다.
- BR-T14: Context 수정 후 Recovered duration은 같고 Recovery Rate 분모만 바뀐다.
- BR-T15: 자정 분할 전후 Activity 실제 지속시간 합계가 같다.
- BR-T16: `endAt == startAt`과 역전 구간은 모두 거부된다.

## 10. 금지 사항

- 앱 이름·카테고리로 Default 또는 final Context 추론
- Activity 이름으로 저장된 의도 재추론
- 원시 Context/RecoveredTime duration 단순 합산
- Baseline 미완료를 0으로 대체
- Saved 음수 저장
- Recovery Rate 100% 상한 적용
- 사용자 확정 Context 자동 덮어쓰기
- 대표 Goal 미선택 overlap을 임의 Goal에 배정
- 실제 사용자 행동 데이터를 예시·로그·fixture로 사용
- STEP 01에서 Java 라이브러리·DB·프레임워크 선택
