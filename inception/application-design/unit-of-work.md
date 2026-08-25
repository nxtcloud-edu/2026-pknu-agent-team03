# Unit of Work — 작업 단위 정의

## 단위 목록 및 실행 순서

| 순서 | 단위 이름 | 포함 컴포넌트 | 관련 FR | 설명 |
|---|---|---|---|---|
| 1 | data-collection | C1, C2, C15, S1, S2 | FR-1, FR-2, FR-15 | 데이터 수집 기반 + 동기화 + 인증 |
| 2 | context-engine | C3, C4, C5, C6, S3 | FR-3, FR-4, FR-5, FR-6 | 활동 기록 + Context 분석 + 낭비 계산 |
| 3 | time-recovery | C7, C8, C9, S4, S5 | FR-7, FR-8, FR-9, FR-10 | Baseline + 목표 + 타이머 + 회수율 |
| 4 | presentation | C10, C11, C12, C13, C14, S6, S7 | FR-11, FR-12, FR-13, FR-14 | 대시보드 + Timeline + 리포트 + 알림 |

---

## 단위별 상세

### Unit 1: data-collection (데이터 수집 기반)

**범위:**
- UsageStatsManager 권한 처리 및 이벤트 수집 (C1)
- 앱 목록 관리 및 기본 분류 설정 (C2)
- 서버 동기화 기반 구축 (C15)
- 사용자 인증 (로컬 PIN) (C14 → S1)
- 백엔드 UsageData 저장/조회 API (S2)

**의존**: 없음 (첫 번째 단위)

**완료 기준:**
- 앱 사용 이벤트가 수집되어 AppSession으로 재구성된다
- 앱 분류를 설정/변경할 수 있다
- 데이터가 서버에 동기화된다
- PIN으로 앱을 보호할 수 있다

---

### Unit 2: context-engine (Context 분석 엔진)

**범위:**
- 오프라인 활동 직접 기록 (C3)
- AppSession-Activity 시간 중첩 분석 및 Context 생성 (C4)
- 활동 충돌 감지 및 사용자 확인 (C5)
- 낭비시간 합산 계산 (C6)
- 백엔드 Context 저장/조회/통계 API (S3)

**의존**: Unit 1 (AppSession, 앱 분류 데이터 필요)

**완료 기준:**
- 활동을 기록하면 해당 시간대 앱 세션과 자동으로 중첩 분석된다
- 충돌 시 사용자에게 선택지가 제시된다
- 일별/주별 낭비시간이 정확히 계산된다
- 복합 활동이 실제 경과시간을 초과하지 않는다

---

### Unit 3: time-recovery (시간 되찾기)

**범위:**
- Baseline 측정 (7일) 및 확보시간 계산 (C7)
- 목표 CRUD 및 진행률 (C8)
- 타이머 + 수동 기록 + 되찾은 시간 저장 (C9)
- 시간 회수율 계산
- 백엔드 Baseline/Goal/RecoveredTime API (S4, S5)

**의존**: Unit 2 (낭비시간 데이터 필요)

**완료 기준:**
- 7일 후 Baseline이 자동 생성된다
- 확보한 시간이 계산되어 표시된다
- 타이머로 활동을 기록하면 되찾은 시간에 누적된다
- 회수율이 정확히 계산된다

---

### Unit 4: presentation (화면 및 알림)

**범위:**
- 메인 대시보드 화면 (C10)
- Timeline 화면 + 분류 수정 (C11)
- 주간 리포트 생성 + 과거 조회 (C12)
- 실시간 낭비 경고 + 리포트 알림 (C13)
- 백엔드 Report/Notification API (S6, S7)

**의존**: Unit 1, 2, 3 (모든 데이터 필요)

**완료 기준:**
- 대시보드에 오늘/이번주 핵심 수치가 2초 이내 표시된다
- Timeline에서 시간대별 활동이 시각적으로 표시된다
- Timeline에서 분류를 수정할 수 있다
- 주간 리포트가 생성되고 푸시 알림이 발송된다
- 낭비 임계치 초과 시 실시간 알림이 발송된다

---

## 의존 그래프

```
Unit 1: data-collection
    │
    ▼
Unit 2: context-engine
    │
    ▼
Unit 3: time-recovery
    │
    ▼
Unit 4: presentation
```

---

## CONSTRUCTION 진행 요약

각 단위마다 STEP 01~05를 순서대로 돌고, 4개 단위를 모두 마친 뒤 STEP 06(빌드와 테스트)을 한 번 돌린다.

총 예상: 4단위 × 5단계 + 1(빌드/테스트) = 21 CONSTRUCTION 단계
