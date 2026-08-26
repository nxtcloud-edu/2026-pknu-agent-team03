# TimeBack STEP 01 — APP-05~APP-09 추적성 및 검증 매트릭스

## 1. 목적

이 문서는 `track-domain-engine`의 기능 설계가 요구사항, 사용자 스토리, 공통 계약, APP-05~APP-09, 업무 규칙, 서비스·화면 계약, 검증 사례로 이어지는지 확인한다.

기준 문서:

- `inception/requirements/requirements.md`
- `inception/user-stories/stories.md`
- `inception/application-design/components.md`
- `inception/application-design/unit-of-work.md`
- `aidlc-docs/construction/plans/timeback-mvp-functional-design-plan.md`
- 같은 폴더의 `domain-model.md`, `business-rules.md`, `service-contracts.md`, `frontend-components.md`

## 2. Gate 1 결정 추적

| 결정 | 승인 답 | 설계 반영 |
|---|---|---|
| Q1 기존 Inception 기준 채택 | A | 이 문서의 FR/NFR/US 근거로 현재 시점부터 사용, 과거 Gate 이력은 복원하지 않음 |
| Q2 새 앱 초기 상태 | B | DM-05, BR-05-01, BR-07-03, FE-04-01 |
| Q3 Activity 의도 | A | DM-06·DM-08, BR-06-01, FE-03-02 |
| Q4 명백한 충돌 | A | BR-07-04, FE-03-03 |
| Q5 단독 구간 | A | DM-12, BR-07-03 |
| Q6 확인 답 매핑 | A | DM-13, BR-07-05, SC-07-02, FE-03-03 |
| Q7 7일 Baseline | A | DM-16·DM-18, BR-08-03·08-05 |
| Q8 유효일·공백 | A | DM-14·DM-15, BR-08-02·08-03 |
| Q9 Saved 음수 처리 | A | DM-18, BR-08-06, FE-02-02 |
| Q10 목표 중첩 대기 | A | DM-22~DM-24, BR-09-05·09-06, FE-05-04 |
| Q11 Context와 Recovered | A | BR-C04, BR-09-09, FE-F02 |
| Q12 Recovery Rate 경계 | A | DM-25, BR-09-08, FE-02-03 |
| Q13 시간 유효성 | A | DM-02, BR-C02, SC-C05 |
| Q14 Goal·타이머 유효성 | A | DM-19·DM-20, BR-09-01~09-04 |

## 3. 기능 요구사항 추적

### FR-3 앱 기본 분류

| 요구 | 스토리 | CT/APP | 모델·규칙 | 계약·UI | 검증 |
|---|---|---|---|---|---|
| FR-3.1 조회·수정 | US-04 | CT-03·04 / APP-05 | DM-07, BR-05-01·02 | SC-05-01·02, FE-04-01·02 | TR-01, TR-02 |
| FR-3.2 허용 enum | US-04 | CT-01 / APP-05 | DM-04·05, BR-05-02 | SC-05-02 | TR-03 |
| FR-3.3 Default와 final Context 분리 | US-04·09 | CT-01·03 / APP-05·07 | DM-I03, BR-05-03·BR-C03 | SC-05-02, FE-04-02·FE-03-05 | TR-04 |

### FR-4 활동과 목표

| 요구 | 스토리 | CT/APP | 모델·규칙 | 계약·UI | 검증 |
|---|---|---|---|---|---|
| FR-4.1 Activity 직접 기록 | US-05 | CT-01·03·04 / APP-06 | DM-08, BR-06-01 | SC-06-01·02, FE-03-02 | TR-05, TR-06 |
| FR-4.2 종류·사용자 정의 | US-05 | CT-01 / APP-06 | DM-06·08 | SC-06-01, FE-03-02 | TR-07 |
| FR-4.3 Goal 생성 | US-16 | CT-02·03·04 / APP-09 | DM-19, BR-09-01 | SC-09-01, FE-06-01 | TR-25, TR-26 |
| FR-4.4 누적·진행률 | US-16·19 | CT-02·04 / APP-09 | DM-24, BR-09-07 | SC-09-07, FE-06-02 | TR-31, TR-32 |

### FR-5 Context

| 요구 | 스토리 | CT/APP | 모델·규칙 | 계약·UI | 검증 |
|---|---|---|---|---|---|
| FR-5.1 중첩 Context | US-06 | CT-01·06 / APP-07 | DM-11·12, BR-07-01·02 | SC-07-01, FE-03-01 | TR-08~TR-10 |
| FR-5.2 중복 합산 금지 | US-06 | CT-01·02 / APP-07·08 | DM-I02, BR-C02·BR-08-01 | SC-08-02, FE-03-01 | TR-11, TR-18 |
| FR-5.3 비충돌 Activity 우선 | US-06 | CT-01 / APP-07 | BR-07-04 | SC-07-01 | TR-12 |
| FR-5.4 충돌 MIXED | US-07 | CT-01·04 / APP-07 | DM-12, BR-07-04 | SC-07-01, FE-03-03 | TR-13 |
| FR-5.5 충돌에만 질문 | US-07 | CT-04 / APP-07 | BR-07-04 | SC-07-04, FE-03-03 | TR-14 |
| FR-5.6 답변으로 확정 | US-07 | CT-01·04 / APP-07 | DM-13, BR-07-05 | SC-07-02, FE-03-03 | TR-15 |
| FR-5.7 enum·확인 여부 | US-07 | CT-01 / APP-07 | DM-04·12, DM-I04·05 | SC-07-04 | TR-13, TR-15 |
| FR-5.8 Timeline 수정 | US-09 | CT-03·04 / APP-07 | BR-07-06·07 | SC-07-03, FE-03-04·05 | TR-16, TR-17 |

### FR-7 Waste·Baseline·Saved

| 요구 | 스토리 | CT/APP | 모델·규칙 | 계약·UI | 검증 |
|---|---|---|---|---|---|
| FR-7.1 WASTE 실제 경과시간 | US-10 | CT-01·02 / APP-08 | DM-12·18, BR-08-01 | SC-08-02, FE-02-02 | TR-18, TR-19 |
| FR-7.2 연속 7일 Baseline | US-11 | CT-02·06 / APP-08 | DM-14~16, BR-08-02·03 | SC-08-01·03, FE-02-01 | TR-20~TR-22 |
| FR-7.3 관찰 중 지표 숨김 | US-11 | CT-04 / APP-08·09 | DM-03·25, BR-08-03 | SC-08-05·SC-09-08, FE-02-01 | TR-23 |
| FR-7.4 승인 기반 재산정 | US-12 | CT-02·03·04 / APP-08 | DM-17·28, BR-08-04 | SC-08-03·04, FE-02-04 | TR-24 |
| FR-7.5 감소분 Saved | US-13 | CT-02 / APP-08 | DM-18, BR-08-05·06 | SC-08-05, FE-02-02 | TR-25A, TR-25B |

### FR-8 Goal·Recovered·Recovery Rate

| 요구 | 스토리 | CT/APP | 모델·규칙 | 계약·UI | 검증 |
|---|---|---|---|---|---|
| FR-8.1 Goal 타이머 | US-17 | CT-02·03·04 / APP-09 | DM-20·29, BR-09-02·03 | SC-09-02~03, FE-05-02 | TR-27~TR-29, TR-61 |
| FR-8.2 TIMER/MANUAL | US-17·18 | CT-02·03 / APP-09 | DM-21, BR-09-03·04 | SC-09-03·05, FE-05-02·03 | TR-29, TR-30 |
| FR-8.3 목표 누적·Recovered | US-19 | CT-02 / APP-09 | DM-22~24, BR-09-07·08 | SC-09-07·08, FE-06-02 | TR-31 |
| FR-8.4 초과 Recovered·Rate | US-21 | CT-02·04 / APP-09 | DM-25, BR-09-08 | SC-09-08, FE-02-03 | TR-35 |
| FR-8.5 중첩 합집합·대표 Goal | US-20 | CT-02·03·04 / APP-09 | DM-22·23, DM-I08·09, BR-09-05·06 | SC-09-06, FE-05-04 | TR-32~TR-34 |
| FR-8.6 Saved 존재 시 Rate | US-21 | CT-02 / APP-08·09 | DM-25, BR-09-08 | SC-09-08, FE-02-03 | TR-35~TR-37 |

### FR-9.4 자정 분할

| 요구 | 스토리 | CT/APP | 모델·규칙 | 계약·UI | 검증 |
|---|---|---|---|---|---|
| FR-9.4 Activity 날짜 배분 | US-05·15 | CT-01·06 / APP-06 | DM-02·09, BR-06-02 | SC-06-03, FE-07-01 | TR-38 |

## 4. 비기능 요구사항 추적

| 요구 | 의미 | 설계 근거 | 검증 |
|---|---|---|---|
| NFR-2.1 실제 경과시간 중복 금지 | Context/Waste union | DM-I02, BR-C02, BR-08-01 | TR-11, TR-18, TR-34 |
| NFR-2.2 자정 분할 합계 보존 | 절대 시간 duration 보존 | DM-02·09, BR-06-02 | TR-38, TR-39 |
| NFR-2.3 Context 수정 연쇄 반영 | Waste·Saved·Rate·조회 재계산, Recovered record 유지 | BR-C04, BR-07-06·07, BR-09-09, FE-F02 | TR-17, TR-37 |
| NFR-2.4 목표 중첩 대표 한 번 | atomic segment 단일 배정 | DM-I08·09, BR-09-05·06 | TR-32~TR-34 |
| NFR-3.1 시간 계산 단위 검증 | CT-06 합성 fixture·제어 시간 | SC-F01~F04, BR-T01~T16 | TR-08~TR-39 |
| NFR-3.2 화면·저장 흐름 통합 검증 | 명령 원자성·조회 상태 | SC-C03, SC-V01~V07, FE-F01~F03 | TR-40~TR-44 |
| NFR-5.1 데이터 개념 독립 | Session/Activity/Context/Goal/Recovered 분리 | DM-08·10·12·19·21, 소유권 표 | TR-45 |
| NFR-5.2 App Default/final Context 분리 | 별도 상태·변경 경로 | DM-05·07·12, BR-05-03 | TR-04, TR-46 |

NFR-1.1의 5초 화면 갱신 목표는 기능 경로 SC-05~SC-09와 APP-10 조회 경계까지 연결하되, 대표 데이터 규모와 측정 명령은 STEP 02에서 정한다.

## 5. 사용자 스토리 결과 추적

| 스토리 | 주 결과 | 설계 ID | 인수 검증 |
|---|---|---|---|
| US-04 | App Default 조회·수정 | DM-05·07, BR-05, SC-05, FE-04 | TR-01~TR-04 |
| US-05 | Activity 기록·자정 분할 | DM-06·08·09, BR-06, SC-06, FE-03-02 | TR-05~TR-07, TR-38·39 |
| US-06 | 비충돌 Context·중복 금지 | DM-11·12, BR-07-01~04 | TR-08~TR-12 |
| US-07 | MIXED와 사용자 확인 | DM-12·13, BR-07-04·05 | TR-13~TR-15 |
| US-09 | Context 수정·연쇄 갱신 | BR-C04, BR-07-06·07, SC-07-03 | TR-16·17 |
| US-10 | final WASTE 집계 | BR-08-01, SC-08-02 | TR-18·19 |
| US-11 | 7일 관찰 상태 | DM-14~16, BR-08-02·03 | TR-20~TR-23 |
| US-12 | 재산정 승인 | DM-17·28, BR-08-04 | TR-24 |
| US-13 | Saved | DM-18, BR-08-05·06 | TR-25A·25B |
| US-16 | Goal·진행률 | DM-19·24, BR-09-01·07 | TR-25·26·31 |
| US-17 | Timer | DM-20·21·29, BR-09-02·03 | TR-27~TR-29 |
| US-18 | MANUAL | DM-21, BR-09-04 | TR-30 |
| US-19 | 목표별 누적 | DM-22·24, BR-09-07 | TR-31 |
| US-20 | 중첩·대표 Goal | DM-22·23, BR-09-05·06 | TR-32~TR-34 |
| US-21 | Recovered·Rate | DM-25, BR-09-08·09 | TR-35~TR-37 |

## 6. CT-01~CT-06 연결

| 계약 | 이번 설계 내용 | 제공/사용 경계 | 검증 증거 |
|---|---|---|---|
| CT-01 | AppSession·Activity·Context 구간·분류·상태 | APP-04·05·06 → APP-07 → APP-10 | DM-02·04~13, SC-D01, SC-07 |
| CT-02 | Baseline·Goal·Recovered·지표 상태 | APP-08·09 → APP-10 | DM-14~25, SC-08·09·Q |
| CT-03 | 엔터티별 저장·조회·원자 변경 | APP-11 ↔ APP-05~09 | SC-C03, SC-D02·D04 |
| CT-04 | 사용자 작업·조회·오류·결정 상태 | APP-05~10 ↔ UI-02~07 | SC-05~09·Q, FE-C/02~07 |
| CT-05 | 도메인 결과는 기기 기준, 서버 재판정 금지 | APP-11 → 백업 트랙 | domain-model 소유권, SC 의존 방향 |
| CT-06 | 합성 Session·가짜 저장·제어 시간 | 모든 트랙 테스트 대역 | SC-F01~F04, TR-01~TR-48 |

## 7. 검증 시나리오

### 7.1 정상 흐름

| ID | 입력/행동 | 기대 결과 |
|---|---|---|
| TR-01 | 분류 없는 합성 앱 목록 조회 | `UNCLASSIFIED`, fallback과 저장값 구분 |
| TR-02 | App을 PRODUCTIVE로 저장 | CLASSIFIED, 자동 Context 영향 통지 |
| TR-04 | App Default 변경 + 기존 USER_CONFIRMED Context | Default만 변경, Context 보존 |
| TR-05 | STUDY Activity + PRODUCTIVE 의도 + 유효 구간 | Activity 저장, 영향 구간 반환 |
| TR-08 | AppSession과 Activity 부분 중첩 | 경계별 AtomicSegment와 Context 생성 |
| TR-12 | App NEUTRAL + Activity PRODUCTIVE | PRODUCTIVE 자동 Context |
| TR-15 | MIXED에 `DISTRACTION` 답변 | WASTE USER_CONFIRMED |
| TR-16 | Timeline Context를 LEISURE로 수정 | USER_CONFIRMED, 이전 관계 추적 |
| TR-20 | 연속 7개 완전 날짜 | ACTIVE Baseline, 합계·일평균 생성 |
| TR-24 | Baseline 후보 승인 | 기존 SUPERSEDED, 후보 ACTIVE |
| TR-25 | 유효 Goal 생성 | Goal 저장, 진행률 0 |
| TR-27 | Goal 타이머 시작 | 단일 RUNNING timer |
| TR-29 | 타이머 정상 완료 | TIMER RecoveredTime 생성, timer 제거 |
| TR-30 | 수동 목표 구간 기록 | MANUAL RecoveredTime 생성 |
| TR-31 | 단일 Goal 기록 60분 | Goal 누적·Recovered 60분 |
| TR-35 | Saved 60분, Recovered 90분 | rate 1.5, 상한 없음 |

### 7.2 경계 흐름

| ID | 입력/행동 | 기대 결과 |
|---|---|---|
| TR-03 | App Default로 MIXED 입력 | 거부, 기존 분류 유지 |
| TR-06 | Activity `end == start` | INVALID_INTERVAL, 저장 없음 |
| TR-07 | CUSTOM 이름 없음 | CUSTOM_NAME_REQUIRED |
| TR-09 | 세션과 Activity 경계만 접촉 | 중첩 Context 없음 |
| TR-10 | 여러 경계가 같은 시각 | 0 길이 segment 없음 |
| TR-11 | 60분 Activity 안 50분 Session | 전체 시간 60분 초과 집계 금지 |
| TR-13 | WASTE App + PRODUCTIVE Activity | MIXED, CONFIRMATION_REQUIRED |
| TR-14 | 같은 분류 또는 NEUTRAL 포함 | 확인 질문 없음 |
| TR-18 | 겹치는 WASTE Context | union duration만 Waste |
| TR-19 | AUTO_CLASSIFIED WASTE | final Waste에 포함, userConfirmed=false 유지 |
| TR-21 | 완전 측정 Waste 0일 | 유효 Baseline 일자, confirmed zero |
| TR-22 | 관찰 4일 뒤 수집 공백 | 관찰 무효화, 다음 완전일부터 재시작 |
| TR-23 | 관찰 1~6일 | Saved·Rate 값 없음, 남은 일수 제공 |
| TR-25A | expected 100, current 70 | delta 30, Saved 30 |
| TR-25B | expected 100, current 130 | delta -30, Saved 0 |
| TR-26 | Goal 목표시간 0 | INVALID_TARGET_DURATION |
| TR-28 | 실행 중 타이머에서 재시작 | TIMER_ALREADY_RUNNING, 기존 유지 |
| TR-32 | 같은 Goal 기록 중첩 | union 한 번, 추가 대표 선택 불필요 |
| TR-33 | 다른 Goal 기록 30분 중첩 | 중첩 30분 pending, 비중첩만 집계 |
| TR-34 | 대표 Goal 선택 | 중첩 한 번만 대표에 배정 |
| TR-36 | Saved 0, Recovered 양수 | SAVED_ZERO, rate 값 없음 |
| TR-37 | Context 수정으로 Saved 변경 | Recovered 유지, rate 재계산 |
| TR-38 | Activity 자정 통과 | 날짜 slice 합계가 원본 실제 duration과 동일 |
| TR-39 | DST/시간대 전환 합성 구간 | 절대 시간 duration 보존 |

### 7.3 오류·원자성 흐름

| ID | 실패 조건 | 기대 결과 |
|---|---|---|
| TR-40 | App 분류 저장 실패 | 이전 App·Context 유지 |
| TR-41 | Activity 저장 실패 | Activity와 Context 영향 없음 |
| TR-42 | Context 확인 저장 실패 | 기존 MIXED 유지, 성공 표시 금지 |
| TR-43 | 타이머 완료 중 Recovered 저장 실패 | RunningTimer 유지, 부분 완료 금지 |
| TR-44 | 대표 Goal 저장 실패 | 기존 pending·누적 유지 |
| TR-45 | 저장소에서 엔터티 종류 혼합 시도 | 계약 위반으로 실패, 독립 데이터 유지 |
| TR-46 | App Default 변경으로 final Context 덮어쓰기 시도 | 차단·회귀 실패로 보고 |

### 7.4 개인정보·테스트 대역

| ID | 검사 | 기대 결과 |
|---|---|---|
| TR-47 | fixture·오류·호출 기록 점검 | 합성 앱·활동·Goal만 존재, 실제 행동 데이터 없음 |
| TR-48 | UI/APP-10/서버 역할 점검 | Context·Baseline·Rate 계산 복제 없음 |

## 8. 해석 명확화

### CL-01. NFR-2.3의 Recovered 반영

Gate 1 Q11=A에 따라 Context 수정은 RecoveredTime의 실제 기록 구간과 지속시간을 변경하지 않는다. NFR-2.3의 “되찾은 시간에 일관되게 반영”은 다음처럼 해석한다.

- Recovered 조회를 같은 기간으로 다시 수행한다.
- Recovered 값은 Goal 기록이 같으면 유지된다.
- Waste·Saved가 바뀌므로 Recovery Rate와 관련 리포트는 갱신된다.

이 해석은 Gate 2 승인 시 현재 시점의 설계 기준이 된다.

### CL-02. FR-7.1의 “확정된 WASTE”

충돌 없이 `AUTO_CLASSIFIED`된 WASTE는 final classification으로 간주해 Waste에 포함한다. `userConfirmed=false`는 자동 판정이라는 출처이며 미완성 상태가 아니다. `MIXED/CONFIRMATION_REQUIRED`만 확정 지표에서 제외한다.

### CL-03. “평균 주간 Baseline”

Gate 1 Q7=A에 따라 7일 Waste 합계와 일평균을 함께 유지한다. 주간 값은 7일 합계이고 일·월·부분 기간 비교는 일평균을 유효 일수에 맞춘 expected Baseline을 사용한다.

### CL-04. Baseline 재산정 제안

미승인 정량 임계값을 만들지 않는다. 활성 Baseline 이후 완성된 비중첩 7일 유효 창의 값이 현재 값과 다르면 후보를 제안하며 사용자 승인 전 교체하지 않는다. 이 제안 빈도는 Gate 2 검토 대상이다.

## 9. 미결정·후속 단계 전달

### STEP 02에서 결정

- Java 통일 범위: Android·도메인·백업 서버 production/test code 포함 여부
- Java/JDK 수준과 Android 호환 수준
- 시간·기간·정밀 비율의 구체 자료형
- Gradle/Maven 등 빌드 도구와 테스트 프레임워크
- DB 제품, transaction/idempotency 구현 방식
- 비동기 재계산 일관성·실패 복구 방식
- Recovery Rate 표시 자릿수와 UI 반올림
- NFR-1.1 대표 데이터 규모와 5초 측정 명령
- `verify-domain`, `build-debug`, `verify-all` 실제 명령

### 다른 책임 트랙과 함께 확정

- APP-04가 제공할 MeasurementDay coverage 완전성 근거
- APP-11의 사용자 확정 Context 대체·보존 저장 계약
- OS-05의 시간대 변경과 현지 경계 입력 방식
- APP-10의 화면별 조회 조립과 stale/recalculating 상태
- 백업 트랙이 파생 지표를 재판정하지 않는 복사 경계

### 현재 설계에서 추가하지 않은 기능

- App·Activity·Goal 삭제 UX와 정책
- 개인화 분류 추천과 반복 Context 자동 인식
- 알림·앱 제한·화면 내용·위치·콘텐츠 내부 판별
- 목표별 예상 완료 시점

## 10. Gate 2 완료 검사

- [x] Gate 1 Q1~Q14 결정이 모든 설계 문서에 반영됐다.
- [x] FR-3~FR-8, FR-9.4가 APP-05~APP-09 규칙과 계약에 연결됐다.
- [x] 관련 NFR-2/3/5가 불변식과 검증 사례에 연결됐다.
- [x] US-04~07, US-09~13, US-16~21이 사용자 결과와 검증에 연결됐다.
- [x] `[start,end)`, 자정 분할, union 집계, 사용자 확정 우선이 일관된다.
- [x] Baseline 관찰 중·Saved 0·대표 선택 대기 상태가 숫자 결과와 구분된다.
- [x] Recovered와 Saved의 원천과 재계산 영향이 분리됐다.
- [x] APP-04·APP-11·OS-05·APP-10과 CT-01~CT-06 경계가 정의됐다.
- [x] 합성 fixture와 민감 데이터 비로그 원칙이 정의됐다.
- [x] Java·프레임워크·DB 선택이 STEP 02로 이관됐다.
- [x] `src/`, Java 코드, 빌드 설정을 생성하지 않았다.

## 11. Gate 2 검토 시 확인할 설계 선택

Gate 2는 특히 다음 현재 설계 선택을 승인하는지 확인한다.

1. AUTO_CLASSIFIED WASTE를 final Waste에 포함한다.
2. Baseline 재산정 후보를 후속 비중첩 7일 유효 창마다 평가한다.
3. 여러 Activity/Session 관계를 보존하되 총 지표는 union으로 제한한다.
4. 대표 Goal 미선택 중첩은 전체 Recovered에서도 보류한다.
5. Context 수정은 RecoveredTime을 바꾸지 않고 Recovery Rate만 연쇄 갱신한다.
6. 진행 중 기간 결과는 값과 함께 `IN_PROGRESS` 상태를 표시한다.

## 12. 리뷰 보완 추적 — 우선 적용 연결

이 절은 기존 표의 누락을 보완하며, §10의 체크 항목은 아래 항목이 모두 검증될 때만 완료로 해석한다.

### 12.1 추가 요구·스토리 연결

| 요구/스토리 | 모델·규칙 | 계약·UI | 회귀 검증 |
|---|---|---|---|
| FR-5.1~5.8, FR-7.1, US-06~10 | DM-30·31·DM-I12, BR-10-01·02 | SC-07A, SC-QA, FE-R01 | TR-49, TR-50, TR-57, TR-58 |
| FR-6.1~6.4, US-08, NFR-1.1·2.1 | DM-30, BR-10-02·09 | SC-QA CanonicalTimelineRow, FE-R01·R02 | TR-50, TR-57, TR-62 |
| FR-7.2~7.5, FR-9.1~9.3, US-11~15, NFR-2.2·2.3 | DM-32·33·DM-I13·14, BR-10-03·04·07 | SC-08A, FE-R02·R03 | TR-51, TR-52, TR-56, TR-62 |
| FR-4.4, FR-8.3, FR-9.5, US-16·19·15 | DM-33·DM-I15, BR-10-05 | SC-09A, FE-R03 | TR-53 |
| FR-8.5, US-20, NFR-2.4 | DM-34, BR-10-06 | SC-09A, FE-05-04 | TR-54 |
| FR-8.1~8.3, US-17~19, NFR-3.2 | DM-36·DM-I17, BR-10-08 | SC-D04A, SC-09A, FE-R04 | TR-60, TR-61 |
| FR-8.4·8.6, US-21 | DM-33·DM-I16, BR-10-07 | SC-09A, FE-R03 | TR-56 |
| FR-10.3·10.4, NFR-4.3, US-24·25 | DM-35, BR-10-08 | SC-D02A·D04A, CT-05 | TR-55 |
| FR-3.1~3.3, US-04 | DM-07, BR-05-01, DM-31 | SC-D05, SC-05-01, FE-04-01 | TR-59 |

### 12.2 추가 회귀 검증

| ID | 입력/행동 | 기대 결과 |
|---|---|---|
| TR-49 | current WASTE Context를 PRODUCTIVE로 수정 | history는 보존되고 current Waste/Saved에는 대체된 WASTE가 없음 |
| TR-50 | 다중 Session·Activity의 상충 evidence와 사용자 PRODUCTIVE 확정 | evidence는 보존, canonical final decision 하나, WASTE 미집계 |
| TR-51 | 정상 coverage의 진행 중 23/25시간 날짜와 중간 coverage 공백 날짜 | 전자는 partial `IN_PROGRESS`, 후자는 `DATA_INCOMPLETE`; 둘 다 Baseline valid day 아님 |
| TR-52 | 동일 MeasurementDay 재시도와 과거 Context 수정 | 재시도는 valid day 1회, 새 source revision만 upsert·미승인 후보 재구성 |
| TR-53 | 120분 lifetime Goal과 30분 period report | Goal은 120분 유지, report만 30분; UsageStats incomplete가 lifetime을 숨기지 않음 |
| TR-54 | representative 선택 뒤 query clipping/새 경계 | 같은 source set child는 대표 상속, 달라진 부분만 재선택 |
| TR-55 | retention expiry·full deletion 중 device/backup 한쪽 실패 | registry 전체가 삭제 대상, 하나라도 실패하면 완료 false |
| TR-56 | incomplete/observing/saved-zero 각각과 pending overlap 동시 | rate reason precedence와 별도 pending 상태 |
| TR-57 | 복수 관계·자정 clipping Timeline | canonical row duration union이 실제 경과시간 이하, UI 계산 없음 |
| TR-58 | 서로 다른 PRODUCTIVE answer와 OTHER final enum | answer·결정 시각·OTHER enum provenance 표시 |
| TR-59 | InstalledAppSource unavailable와 신규 App | 기존 분류 보존, 신규는 UNCLASSIFIED+discoveredAt만 가짐 |
| TR-60 | timer completion 성공/실패 | Recovered CREATED와 timer DELETED가 같은 change set, 실패 시 둘 다 미노출 |
| TR-61 | 제공 계약·UI action 목록 검사 | cancel operation/action 없음, completion만 timer 제거 |
| TR-62 | Context/App 변경 후 재계산 성공·실패 | source/computed revision, FRESH/STALE/FAILED, 마지막 성공 snapshot 정확 표시 |

### 12.3 Gate 2 보완 완료 검사

- [x] current-effective Context revision과 superseded WASTE 제외를 설계·계약·TR-49에 연결했다.
- [x] 다중 관계를 final canonical decision 하나로 축약하고 TR-50에 연결했다.
- [x] partial coverage/MeasurementDay upsert를 TR-51·52에 연결했다.
- [x] lifetime Goal·period report 및 stable overlap resolution을 TR-53·54에 연결했다.
- [x] 새 기준·파생 행동 데이터의 보관·백업·전체 삭제를 TR-55에 연결했다.
- [x] Rate reason precedence, canonical Timeline, provenance, InstalledAppSource, DELETED, cancel 제거, freshness를 TR-56~62에 연결했다.
