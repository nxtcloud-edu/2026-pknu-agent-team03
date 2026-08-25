# TimeBack (시간 되찾기) — INCEPTION 결과 요약

## 한 줄 설명
스마트폰 사용시간과 시간대별 앱 사용 패턴을 분석하여 무의식적 낭비 시간을 파악하고, 목표 활동으로 전환하도록 돕는 개인화 시간 관리 Android 앱

---

## 산출물 목록

```
aidlc-docs/inception/
├── requirements/
│   ├── requirements.md              ← 기능/비기능 요구사항 (FR 15개, NFR 5개)
│   ├── detailed-business-rules.md   ← 코딩 가능한 수준의 세부 규칙
│   └── requirement-verification-questions.md  ← Q&A 기록
├── user-stories/
│   ├── stories.md                   ← 11 에픽, 19 유저스토리 (AC 포함)
│   └── personas.md                  ← 페르소나 3명
├── plans/
│   ├── execution-plan.md            ← 실행 계획 (전 단계 실행 판정)
│   ├── story-generation-plan.md
│   ├── application-design-plan.md
│   └── unit-of-work-plan.md
└── application-design/
    ├── components.md                ← 시스템 구성도 (Android 15 + Backend 7)
    ├── unit-of-work.md              ← 작업 단위 4개 + 팀 분담 가이드
    ├── data-model.md                ← 통합 데이터 모델 (엔티티 10개, ER 다이어그램)
    ├── api-contracts.md             ← API 인터페이스 계약 (21개 엔드포인트, JSON 예시)
    ├── user-flows.md                ← 유저 플로우 7개 (설치~일상~타이머~리포트)
    └── wireframes.md                ← 화면 와이어프레임 8개 + 프로토타입 연결
```

---

## 핵심 요약

### 앱이 하는 것
1. 스마트폰 앱 사용시간 자동 수집 (Android UsageStatsManager)
2. 시간대별 Context 분석 (운동 중 YouTube = 낭비 아님)
3. 진짜 낭비시간 계산 + 개인 Baseline 대비 비교
4. 목표 활동 타이머로 "되찾은 시간" 누적
5. 시간 회수율 = 되찾은 시간 / 확보한 시간

### 기술 판정
- **Complexity**: Complex
- **Requirements Depth**: Comprehensive
- **팀 규모**: 4~6인
- **플랫폼**: Android + Backend API (서버 동기화)

### 작업 단위 (4개)
| 단위 | 핵심 |
|---|---|
| data-collection | 데이터 수집 + 세션 재구성 + 인증 + 동기화 |
| context-engine | 활동 기록 + Context 분석 + 낭비 계산 |
| time-recovery | Baseline + 목표 + 타이머 + 회수율 |
| presentation | 대시보드 + Timeline + 리포트 + 알림 |

### API 전체 목록 (21개)
- Auth: 1개 (register)
- Usage: 4개 (sessions sync/get, apps sync/get)
- Context: 5개 (activities sync/get, contexts sync, waste daily/weekly)
- Goals: 6개 (CRUD + recovered + recovery-rate)
- Reports: 2개 (weekly/list)
- Notifications: 3개 (token/settings get/put)

---

## 읽는 순서 (추천)

1. 이 파일 (요약)
2. `requirements/requirements.md` (뭘 만드는지)
3. `requirements/detailed-business-rules.md` (어떻게 동작하는지)
4. `application-design/user-flows.md` (사용자가 어떤 순서로 쓰는지)
5. `application-design/wireframes.md` (화면이 어떻게 생겼는지)
6. `application-design/data-model.md` (데이터 구조)
7. `application-design/api-contracts.md` (API 스펙)
8. `application-design/unit-of-work.md` (누가 뭘 맡는지)
