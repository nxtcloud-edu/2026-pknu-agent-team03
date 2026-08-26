# TimeBack STEP 01 — 도메인 모델

## 1. 문서 목적과 범위

이 문서는 `UOW-01 timeback-mvp`의 CT-01·CT-02를 구체화하고 `track-domain-engine`이 소유하는 APP-05~APP-09의 논리 데이터를 정의한다. 저장 제품, Java 자료형, 프레임워크, 직렬화 형식은 결정하지 않는다.

- 입력 기준: Gate 1 승인 `Q1=A, Q2=B, Q3=A, Q4=A, Q5=A, Q6=A, Q7=A, Q8=A, Q9=A, Q10=A, Q11=A, Q12=A, Q13=A, Q14=A`
- 관련 요구: FR-3~FR-8, FR-9.4, NFR-2.1~2.4, NFR-3.1~3.2, NFR-5.1~5.2
- 관련 스토리: US-04~US-07, US-09~US-13, US-16~US-21
- 코드 상태: 생성 금지

## 2. 공통 값 의미

### DM-01. 식별자

식별자는 같은 종류의 레코드를 구분하는 불투명한 값이다. 생성 방식과 구현 자료형은 STEP 02 이후에 정한다.

- `appId` 대신 운영체제 경계에서 받은 `packageName`을 App의 논리 식별자로 사용한다.
- `activityId`, `contextId`, `baselineId`, `goalId`, `recoveredId`, `timerId`, `resolutionId`는 서로 다른 식별자 공간이다.
- 식별자 문자열에서 사용자 행동, 앱 분류, 시간 의미를 추론하지 않는다.

### DM-02. 시간 구간

모든 구간은 시작을 포함하고 종료를 포함하지 않는 `[startAt, endAt)`이다.

| 항목 | 규칙 |
|---|---|
| 유효성 | `endAt > startAt` |
| 지속시간 | 절대 시간선에서 `endAt - startAt`으로 계산 |
| 경계 접촉 | `endA == startB`이면 겹치지 않음 |
| 중첩 | `max(startA, startB) < min(endA, endB)` |
| 현지 날짜 분할 | 해당 시점의 지역 시간 규칙에 따른 자정 경계 사용 |
| 주간 경계 | 현지 월요일 00:00부터 다음 월요일 00:00 직전 |

저장된 `duration`이 있더라도 시작·종료 시각에서 계산한 값이 기준이다. `endAt <= startAt`은 저장 전에 거부하며 다음 날로 자동 보정하지 않는다.

### DM-03. 기간과 상태의 분리

숫자 `0`과 계산 불가 상태를 구분한다.

- `VALUE`: 계산 가능한 값이 존재한다. 값은 0일 수 있다.
- `OBSERVING`: Baseline 관찰 중이다.
- `NOT_AVAILABLE`: 선행 조건이 없어 계산할 수 없다.
- `DATA_INCOMPLETE`: 수집 공백 또는 불완전 기간이다.
- `DECISION_REQUIRED`: 사용자 대표 선택이나 Context 확인이 필요하다.

## 3. 분류 값

### DM-04. ContextClassification

- `PRODUCTIVE`
- `LEISURE`
- `WASTE`
- `MIXED`
- `NEUTRAL`

`MIXED`는 사용자 확인 전 충돌 상태이며 App의 기본 분류로 저장하지 않는다.

### DM-05. AppDefaultClassificationState

| 상태 | defaultClassification | 의미 |
|---|---|---|
| `UNCLASSIFIED` | 없음 | 사용자가 아직 기본 분류를 정하지 않음 |
| `CLASSIFIED` | `PRODUCTIVE`, `LEISURE`, `WASTE`, `NEUTRAL` 중 하나 | 사용자가 기본 분류를 저장함 |

`UNCLASSIFIED` 앱은 자동 Context 판정에서만 `NEUTRAL` fallback을 사용한다. fallback은 App 레코드에 `NEUTRAL`을 저장하거나 사용자가 분류했다고 표시하지 않는다.

### DM-06. ActivityType과 의도 분류

Activity 종류는 `EXERCISE`, `STUDY`, `DEVELOPMENT`, `READING`, `LEISURE`, `CUSTOM`을 표현한다. 각 Activity는 별도의 `intentClassification`을 가진다.

- 기본 종류는 UI가 제안 분류를 제공할 수 있으나 사용자가 저장 전에 변경할 수 있다.
- `CUSTOM`은 사용자가 `customName`과 의도 분류를 직접 정한다.
- Activity 이름이나 종류만으로 저장 이후 의도를 다시 추론하지 않는다.
- Activity 의도 분류는 `PRODUCTIVE`, `LEISURE`, `WASTE`, `NEUTRAL` 중 하나다.

## 4. 핵심 엔터티

### DM-07. App

| 필드 | 필수 | 의미 |
|---|---:|---|
| `packageName` | 예 | 운영체제가 제공한 앱 식별값 |
| `displayName` | 예 | 사용자 표시 이름, 분류 근거가 아님 |
| `classificationState` | 예 | `UNCLASSIFIED` 또는 `CLASSIFIED` |
| `defaultClassification` | 조건부 | `CLASSIFIED`일 때만 존재 |
| `updatedAt` | 예 | 사용자가 기본 분류를 마지막으로 변경한 시각 |

불변식:

- `UNCLASSIFIED`와 기본 분류 값은 동시에 존재하지 않는다.
- App 기본 분류 변경은 기존 `userConfirmed=true` Context를 바꾸지 않는다.

### DM-08. Activity

| 필드 | 필수 | 의미 |
|---|---:|---|
| `activityId` | 예 | Activity 식별자 |
| `type` | 예 | Activity 종류 |
| `customName` | 조건부 | `CUSTOM`일 때 공백 제외 비어 있지 않은 이름 |
| `intentClassification` | 예 | 사용자가 확정한 Activity 의도 |
| `startAt`, `endAt` | 예 | 원래 Activity 구간 |
| `createdAt`, `updatedAt` | 예 | 생성·수정 시각 |

원본 Activity는 전체 구간을 유지한다. 일·주·월 조회와 계산에서는 현지 자정 경계로 `ActivitySlice`를 투영한다.

### DM-09. ActivitySlice

`ActivitySlice`는 기준 레코드가 아니라 기간 계산용 파생 값이다.

| 필드 | 의미 |
|---|---|
| `activityId` | 원본 Activity 추적 |
| `periodStart`, `periodEnd` | 원본 구간과 요청 기간·현지 날짜의 교집합 |
| `intentClassification` | 원본 Activity의 확정 의도 |

모든 slice 지속시간의 합은 요청 범위 안 원본 Activity 지속시간과 같아야 한다.

### DM-10. AppSession 입력 모델

APP-04가 제공하며 APP-07은 수정하지 않는다.

| 필드 | 의미 |
|---|---|
| `sessionId` | 세션 식별자 |
| `packageName` | App 조회 키 |
| `startAt`, `endAt` | 유효한 세션 구간 |
| `sourceEventIds` | 원본 이벤트 추적 근거 |
| `inferred` | 종료 시각 추정 여부 |

### DM-11. AtomicSegment

APP-07이 Context 판정 전에 만드는 파생 구간이다.

| 필드 | 의미 |
|---|---|
| `startAt`, `endAt` | 세션·Activity·기존 사용자 확정 Context의 모든 경계로 분할한 구간 |
| `activeSessionIds` | 이 구간에 포함되는 세션 목록 |
| `activeActivityIds` | 이 구간에 포함되는 Activity 목록 |
| `preservedContextIds` | 보존해야 할 사용자 확정 Context 목록 |

서로 다른 AtomicSegment는 겹치지 않는다. 원래 입력 구간의 합집합을 빈틈 없이 표현해야 한다.

### DM-12. Context

| 필드 | 필수 | 의미 |
|---|---:|---|
| `contextId` | 예 | Context 식별자 |
| `sessionId` | 예 | 기준 AppSession |
| `activityId` | 아니요 | 앱 단독 구간이면 없음 |
| `startAt`, `endAt` | 예 | AtomicSegment와 일치하는 구간 |
| `classification` | 예 | 최종 또는 확인 대기 분류 |
| `decisionStatus` | 예 | 판정 상태 |
| `decisionReason` | 예 | 판정 이유 코드 |
| `userConfirmed` | 예 | 사용자가 확인·수정했는지 여부 |
| `supersedesContextId` | 아니요 | 사용자 수정이 대체한 Context 추적 |

`decisionStatus`:

- `AUTO_CLASSIFIED`: 충돌 없이 자동 판정됨
- `CONFIRMATION_REQUIRED`: `MIXED`, 사용자 답변 대기
- `USER_CONFIRMED`: 충돌 질문에 답하거나 Timeline에서 수정함

`decisionReason` 예:

- `APP_DEFAULT_ONLY`
- `UNCLASSIFIED_APP_NEUTRAL_FALLBACK`
- `ACTIVITY_INTENT_PRIORITY`
- `NON_NEUTRAL_CONFLICT`
- `USER_PURPOSE_CONFIRMATION`
- `USER_TIMELINE_EDIT`
- `PRESERVED_USER_DECISION`

`AUTO_CLASSIFIED`의 `WASTE`는 충돌이 없는 최종 판정으로 Waste 계산 대상이다. `userConfirmed=false`는 자동 판정임을 뜻하며 미완성 결과를 뜻하지 않는다.

### DM-13. 사용자 Context 답변

- `PRODUCTIVE_PURPOSE`
- `ASSISTIVE_USE`
- `DISTRACTION`
- `INTENTIONAL_REST`
- `OTHER`

`OTHER`는 최종 `ContextClassification`을 추가로 선택해야 완료된다.

## 5. Baseline과 시간 지표

### DM-14. MeasurementDay

| 필드 | 의미 |
|---|---|
| `localDate` | 현지 날짜 |
| `coverageStatus` | `COMPLETE`, `INCOMPLETE`, `PERMISSION_MISSING` |
| `wasteDuration` | `COMPLETE`일 때 확정된 WASTE 구간 합집합 |
| `confirmedZero` | 완전 수집 결과 Waste가 0임을 구분 |

데이터가 없다는 사실만으로 `confirmedZero=true`가 되지 않는다.

### DM-15. BaselineObservation

| 필드 | 의미 |
|---|---|
| `observationId` | 관찰 식별자 |
| `status` | `OBSERVING`, `COMPLETED`, `INVALIDATED` |
| `startLocalDate` | 첫 완전 측정일 |
| `consecutiveValidDays` | 현재 연속 유효일 수, 0~7 |
| `dailyWasteDurations` | 각 유효일의 Waste 값 |
| `invalidatedReason` | 수집 공백·권한 누락 등 |

불완전 날짜가 발생하면 현재 연속 관찰은 `INVALIDATED`가 되고 다음 완전 날짜부터 새 관찰을 시작한다.

### DM-16. Baseline

| 필드 | 의미 |
|---|---|
| `baselineId` | Baseline 식별자 |
| `observationStart`, `observationEnd` | 7개 연속 유효 현지 날짜 범위 |
| `weeklyWasteDuration` | 7일 Waste 합계 |
| `dailyAverageWasteDuration` | `weeklyWasteDuration / 7`의 논리 값 |
| `status` | `ACTIVE`, `RECALCULATION_PROPOSED`, `SUPERSEDED` |
| `supersedesBaselineId` | 재산정으로 대체한 기존 Baseline |

최초 Baseline은 자동 교체하지 않는다. 이후 연속 7일 유효 창에서 후보를 만들 수 있지만 사용자가 승인해야 새 Baseline이 `ACTIVE`가 된다.

### DM-17. BaselineCandidate

| 필드 | 의미 |
|---|---|
| `candidateId` | 후보 식별자 |
| `sourceObservation` | 후속 7일 관찰 근거 |
| `weeklyWasteDuration`, `dailyAverageWasteDuration` | 후보 값 |
| `decisionStatus` | `PROPOSED`, `APPROVED`, `REJECTED` |

정량 임계값을 발명하지 않는다. 활성 Baseline 이후 완성된 비중첩 7일 창의 값이 현재 값과 다르면 후보를 제안하며, 승인 전 기존 Baseline을 유지한다.

### DM-18. PeriodMetrics

| 필드 | 의미 |
|---|---|
| `periodStart`, `periodEnd` | 조회 기간 |
| `periodStatus` | `COMPLETE` 또는 `IN_PROGRESS` |
| `wasteDuration` | WASTE Context 구간 합집합 |
| `expectedBaselineDuration` | 활성 Baseline 일평균 × 비교 유효 일수 |
| `wasteDelta` | `expectedBaselineDuration - wasteDuration`, 부호 유지 |
| `savedDuration` | `max(0, wasteDelta)` |
| `status` | `VALUE`, `OBSERVING`, `DATA_INCOMPLETE` |

진행 중인 오늘·현재 주 결과는 `IN_PROGRESS`로 표시해 확정 기간과 구분한다.

## 6. Goal과 Recovered 모델

### DM-19. Goal

| 필드 | 의미 |
|---|---|
| `goalId` | Goal 식별자 |
| `name` | 공백 제거 후 비어 있지 않은 이름 |
| `targetDuration` | 0보다 큰 목표시간 |
| `createdAt`, `updatedAt` | 생성·수정 시각 |

진행률은 파생 값이며 저장된 RecoveredTime 배정 결과에서 재현한다. 100%를 초과할 수 있으며 도메인에서 상한을 두지 않는다.

### DM-20. RunningTimer

| 필드 | 의미 |
|---|---|
| `timerId` | 실행 식별자 |
| `goalId` | 선택한 Goal |
| `startAt` | 제어 시간 경계에서 받은 시작 시각 |
| `status` | `RUNNING` |

사용자별 실행 중 타이머는 하나만 존재한다. 완료 시 유효한 RecoveredTime을 생성하고 RunningTimer를 제거한다.

### DM-21. RecoveredTime

| 필드 | 의미 |
|---|---|
| `recoveredId` | 기록 식별자 |
| `goalId` | 원래 선택 Goal |
| `method` | `TIMER` 또는 `MANUAL` |
| `startAt`, `endAt` | 실제 목표 활동 구간 |
| `duration` | 시작·종료에서 계산한 값 |
| `createdAt`, `updatedAt` | 생성·수정 시각 |

Context 수정은 RecoveredTime의 구간과 duration을 바꾸지 않는다.

### DM-22. RecoveryAtomicSegment

RecoveredTime 경계로 분할한 파생 구간이다.

| 필드 | 의미 |
|---|---|
| `startAt`, `endAt` | 겹치지 않는 원자 구간 |
| `sourceRecoveredIds` | 이 구간을 덮는 기록 |
| `candidateGoalIds` | 연결된 Goal 집합 |
| `assignmentStatus` | `ASSIGNED`, `REPRESENTATIVE_REQUIRED` |
| `representativeGoalId` | 배정 완료 시 한 Goal |

하나의 후보 Goal만 있으면 자동 배정한다. 여러 Goal이면 사용자 선택 전 겹친 부분을 총 Recovered와 Goal 누적에서 모두 보류한다.

### DM-23. OverlapResolution

| 필드 | 의미 |
|---|---|
| `resolutionId` | 선택 기록 식별자 |
| `segmentStart`, `segmentEnd` | 충돌 원자 구간 |
| `candidateGoalIds` | 선택 가능한 Goal |
| `representativeGoalId` | 사용자가 고른 Goal |
| `resolvedAt` | 선택 시각 |

원본 RecoveredTime을 삭제하거나 분할 저장하지 않고 배정 결과를 별도로 유지한다.

### DM-24. GoalProgress

| 필드 | 의미 |
|---|---|
| `goalId` | Goal |
| `accumulatedDuration` | 해당 Goal에 배정된 원자 구간 합계 |
| `targetDuration` | Goal 목표시간 |
| `progressRatio` | `accumulatedDuration / targetDuration`, 상한 없음 |
| `pendingOverlapDuration` | 대표 선택 대기 구간 |

### DM-25. RecoveryMetrics

| 필드 | 의미 |
|---|---|
| `periodStart`, `periodEnd` | 조회 기간 |
| `recoveredDuration` | 배정 완료된 RecoveryAtomicSegment 합계 |
| `savedDuration` | 같은 기간의 APP-08 결과 |
| `recoveryRateStatus` | `AVAILABLE`, `BASELINE_OBSERVING`, `SAVED_ZERO`, `DATA_INCOMPLETE`, `DECISION_REQUIRED` |
| `recoveryRate` | `AVAILABLE`일 때 `recoveredDuration / savedDuration` |

- Saved가 0이면 `SAVED_ZERO`이며 비율 값은 없다.
- Recovered가 0이고 Saved가 양수면 `AVAILABLE`과 비율 0이다.
- 100% 상한과 도메인 반올림을 적용하지 않는다.
- 대표 선택 대기 구간이 있으면 확정 합계와 `pendingOverlapDuration`을 함께 제공한다.

## 7. 상태 전이

### DM-26. App 분류

```text
UNCLASSIFIED
  → 사용자가 분류 선택 → CLASSIFIED
CLASSIFIED
  → 사용자가 다른 분류 선택 → CLASSIFIED(새 값)
```

분류 변경은 자동 Context 재분석을 유발할 수 있지만 사용자 확정 Context는 보존한다.

### DM-27. Context

```text
AUTO_CLASSIFIED
  → Timeline 사용자 수정 → USER_CONFIRMED
CONFIRMATION_REQUIRED(MIXED)
  → 확인 답변/최종 enum 선택 → USER_CONFIRMED
USER_CONFIRMED
  → 사용자 재수정 → USER_CONFIRMED(새 Context가 이전 Context를 대체)
```

자동 재분석은 `USER_CONFIRMED`를 `AUTO_CLASSIFIED`나 `MIXED`로 되돌리지 않는다.

### DM-28. Baseline

```text
OBSERVING
  → 연속 7개 COMPLETE 날짜 → COMPLETED → ACTIVE Baseline 생성
ACTIVE
  → 후속 7일 후보 생성 → RECALCULATION_PROPOSED
RECALCULATION_PROPOSED
  ├─ 사용자 승인 → 기존 SUPERSEDED + 새 ACTIVE
  └─ 사용자 거절 → 기존 ACTIVE 유지 + 후보 REJECTED
```

### DM-29. Timer

```text
타이머 없음
  → Goal 선택·시작 → RUNNING
RUNNING
  ├─ 완료(end > start) → RecoveredTime(TIMER) 생성 + 타이머 없음
  └─ 취소 → RecoveredTime 생성 없이 타이머 없음
```

## 8. 소유권과 파생 관계

| 데이터 | 기준 소유 | APP-05~APP-09 역할 | 파생/기준 |
|---|---|---|---|
| App | APP-11 | APP-05 변경 | 기준 |
| Activity | APP-11 | APP-06 변경 | 기준 |
| AppSession | APP-11 | APP-07 읽기 | 기준 |
| Context | APP-11 | APP-07 변경 | 기준 파생 엔터티 |
| Baseline | APP-11 | APP-08 변경 | 기준 파생 엔터티 |
| Goal, RecoveredTime | APP-11 | APP-09 변경 | 기준 |
| AtomicSegment, Slice, 지표, 진행률 | 저장 제품에 종속되지 않음 | APP-07~APP-09 계산 | 재현 가능한 파생 값 |

서버는 이 데이터를 백업 사본으로만 보관하며 Context·Baseline·회수율을 다시 판정하지 않는다.

## 9. 모델 불변식

- DM-I01: 유효한 모든 구간은 `endAt > startAt`이다.
- DM-I02: 파생 slice/segment의 합집합은 원본의 요청 범위 내 교집합과 같다.
- DM-I03: App Default와 사용자 확정 Context는 독립적으로 변경된다.
- DM-I04: `CONFIRMATION_REQUIRED` Context의 분류는 `MIXED`다.
- DM-I05: `USER_CONFIRMED` Context는 사용자 동작 없이 자동 상태로 바뀌지 않는다.
- DM-I06: Baseline `ACTIVE`는 한 시점에 하나이며 승인 전 후보가 대체하지 않는다.
- DM-I07: Saved와 Recovered는 음수가 아니다.
- DM-I08: 동일 RecoveryAtomicSegment는 최대 한 Goal에 배정된다.
- DM-I09: 대표 Goal 미선택 중첩 구간은 확정 누적에 들어가지 않는다.
- DM-I10: Recovery Rate는 Saved가 양수일 때만 값이 존재한다.
- DM-I11: 실제 사용자 앱 목록·활동·시간 패턴은 fixture나 예시 데이터로 사용하지 않는다.

## 10. STEP 02 전달

다음은 기능 의미가 아니라 기술 결정이므로 STEP 02에서 확정한다.

- Java 적용 범위와 Java/JDK 수준
- 시각·기간·비율의 구체 자료형
- 식별자 생성 전략
- 영속화 스키마와 트랜잭션 경계
- 테스트 프레임워크와 fake 구현 방식
- 큰 기간 계산의 성능 목표와 실제 `verify-domain` 명령
