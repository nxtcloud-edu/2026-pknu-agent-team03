# TimeBack UOW-01 — CONSTRUCTION STEP 05 코드 생성 계획

## 1. 목적

이 계획은 승인된 STEP 01~04 산출물을 따라 `track-domain-engine`의 APP-05~APP-09 Java 순수 도메인 코드와 합성 검증 runner를 생성하는 범위를 정한다.

## 2. 생성 범위

### Production

- interval: 불변 `[start,end)`와 intersection/union/split
- model: 분류 enum, App/Activity/Session, Context revision/evidence/effective decision
- context: canonical segment 생성, 사용자 결정 우선, MIXED/fallback 판정
- metrics: current-effective Waste, MeasurementDay upsert, Baseline observation, Saved/Rate 상태
- recovery: Goal, RunningTimer 완료, Recovered overlap, representative resolution, lifetime/period summary
- port: 외부 Session/Data/Time/Notifier를 표현하는 SDK 독립 interface

### Test

- fixed time·synthetic fixture·in-memory fake
- `DomainEngineTestRunner`의 명명된 assertion
- `.kiro/scripts/verify-domain.ps1`의 compile+runner 실행

### 제외

- Android SDK, UsageStats, 실제 DB, UI, backup/network, Gradle/Maven, 외부 라이브러리
- 승인되지 않은 timer cancel, 삭제 UX, 추천·AI·알림

## 3. 구현 순서

1. value object·interval과 합성 assertion 지원
2. Context/effective decision과 Waste
3. MeasurementDay/Baseline/Saved/Rate
4. Goal/Timer/Recovered/overlap
5. fake와 runner, verify script

각 묶음은 다음 묶음 전에 compile 가능해야 하며, 코드 전체가 준비된 뒤 `verify-domain.ps1`로 한 번에 검증한다.

## 4. Gate 1 검증 질문

### Q1. 구현 범위

- A. 위 production/test 범위를 한 번에 생성하고 APP-05~APP-09 핵심 규칙을 합성 runner로 검증한다. **(추천)**
- B. interval/Context만 먼저 구현한다.
- Other: 직접 입력

### Q2. 검증 강도

- A. BR-T01~T24 및 TR-49~TR-62에서 순수 도메인 소유 핵심을 명명된 runner test로 구현한다. **(추천)**
- B. 정상 흐름만 검증한다.
- Other: 직접 입력

### Q3. 외부 통합 경계

- A. 실제 외부 adapter 없이 port/in-memory fake로만 구현한다. **(추천)**
- B. Android/DB/UI/backup 구현을 함께 시작한다.
- Other: 직접 입력

## 5. Gate 1 요청

추천안은 `Q1=A, Q2=A, Q3=A`이다.

1) 수정 요청
2) 다음 단계로
