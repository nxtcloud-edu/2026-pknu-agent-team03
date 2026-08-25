# 통합 데이터 모델

> 4명이 동일하게 쓰는 엔티티 정의입니다.
> 필드명, 타입, 제약조건이 여기서 확정됩니다. 임의로 바꾸지 않습니다.

---

## User

| 필드 | 타입 | 제약 | 설명 |
|---|---|---|---|
| userId | String (UUID) | PK | 고유 식별자 |
| deviceId | String | UNIQUE, NOT NULL | 기기 식별자 |
| nickname | String? | - | 표시 이름 |
| pinHash | String? | - | PIN 해시 (미설정=null) |
| baselineWasteTime | Long? | - | 주간 Baseline (분, 미측정=null) |
| baselineStartDate | Date | NOT NULL | 측정 시작일 |
| createdAt | DateTime | NOT NULL | 가입일시 |

---

## App

| 필드 | 타입 | 제약 | 설명 |
|---|---|---|---|
| packageName | String | PK | 앱 패키지명 |
| userId | String | FK→User | 사용자 |
| appName | String | NOT NULL | 표시 이름 |
| defaultClassification | Enum | NOT NULL, DEFAULT=NEUTRAL | PRODUCTIVE/LEISURE/WASTE/NEUTRAL |
| isInstalled | Boolean | NOT NULL | 현재 설치 상태 |
| updatedAt | DateTime | NOT NULL | 마지막 변경 |

---

## UsageEvent

| 필드 | 타입 | 제약 | 설명 |
|---|---|---|---|
| eventId | String (UUID) | PK | 고유 식별자 |
| userId | String | FK→User | 사용자 |
| packageName | String | NOT NULL | 앱 |
| eventType | Enum | NOT NULL | FOREGROUND/BACKGROUND/SCREEN_OFF/SCREEN_ON |
| timestamp | Long | NOT NULL | 발생 시각 (Unix millis) |
| synced | Boolean | NOT NULL, DEFAULT=false | 동기화 여부 |

---

## AppSession

| 필드 | 타입 | 제약 | 설명 |
|---|---|---|---|
| sessionId | String (UUID) | PK | 고유 식별자 |
| userId | String | FK→User | 사용자 |
| packageName | String | NOT NULL | 앱 |
| startTime | Long | NOT NULL | 세션 시작 (Unix millis) |
| endTime | Long | NOT NULL | 세션 종료 (Unix millis) |
| duration | Long | NOT NULL | 지속시간 (밀리초) |
| date | Date | NOT NULL | 속한 날짜 |
| synced | Boolean | NOT NULL, DEFAULT=false | 동기화 여부 |

---

## Activity

| 필드 | 타입 | 제약 | 설명 |
|---|---|---|---|
| activityId | String (UUID) | PK | 고유 식별자 |
| userId | String | FK→User | 사용자 |
| activityType | Enum | NOT NULL | RUNNING/STUDY/DEVELOPMENT/READING/LEISURE/CUSTOM |
| customName | String? | - | CUSTOM일 때 이름 |
| startTime | Long | NOT NULL | 시작 (Unix millis) |
| endTime | Long | NOT NULL | 종료 (Unix millis) |
| date | Date | NOT NULL | 날짜 |
| synced | Boolean | NOT NULL, DEFAULT=false | 동기화 여부 |

---

## Context

| 필드 | 타입 | 제약 | 설명 |
|---|---|---|---|
| contextId | String (UUID) | PK | 고유 식별자 |
| sessionId | String | FK→AppSession | 연결된 세션 |
| activityId | String? | FK→Activity | 연결된 활동 (null=기본분류) |
| userId | String | FK→User | 사용자 |
| classification | Enum | NOT NULL | PRODUCTIVE/LEISURE/WASTE/MIXED/NEUTRAL |
| overlapRatio | Float | NOT NULL, DEFAULT=0 | 중첩 비율 (0.0~1.0) |
| userConfirmed | Boolean | NOT NULL, DEFAULT=false | 사용자 수동 확인 여부 |
| conflictResolution | Enum? | - | STUDY_PURPOSE/SUPPLEMENTARY/DISTRACTION/INTENTIONAL_REST/OTHER/UNRESOLVED |
| date | Date | NOT NULL | 날짜 |
| synced | Boolean | NOT NULL, DEFAULT=false | 동기화 여부 |

---

## Goal

| 필드 | 타입 | 제약 | 설명 |
|---|---|---|---|
| goalId | String (UUID) | PK | 고유 식별자 |
| userId | String | FK→User | 사용자 |
| name | String | NOT NULL | 목표 이름 |
| targetTime | Long | NOT NULL, DEFAULT=0 | 목표시간 (분, 0=무한) |
| accumulatedTime | Long | NOT NULL, DEFAULT=0 | 누적 투자시간 (분) |
| isActive | Boolean | NOT NULL, DEFAULT=true | 활성 여부 |
| createdAt | DateTime | NOT NULL | 생성일 |
| synced | Boolean | NOT NULL, DEFAULT=false | 동기화 여부 |

---

## RecoveredTime

| 필드 | 타입 | 제약 | 설명 |
|---|---|---|---|
| recoveredId | String (UUID) | PK | 고유 식별자 |
| userId | String | FK→User | 사용자 |
| goalId | String | FK→Goal | 연결된 목표 |
| duration | Long | NOT NULL | 되찾은 시간 (분) |
| method | Enum | NOT NULL | TIMER/MANUAL |
| startTime | Long | NOT NULL | 시작 (Unix millis) |
| endTime | Long | NOT NULL | 종료 (Unix millis) |
| date | Date | NOT NULL | 날짜 |
| synced | Boolean | NOT NULL, DEFAULT=false | 동기화 여부 |

---

## WeeklyReport

| 필드 | 타입 | 제약 | 설명 |
|---|---|---|---|
| reportId | String (UUID) | PK | 고유 식별자 |
| userId | String | FK→User | 사용자 |
| weekStartDate | Date | NOT NULL, UNIQUE(userId,weekStartDate) | 주 시작일 |
| wasteMinutes | Long | NOT NULL | 주간 낭비시간 |
| baselineMinutes | Long | NOT NULL | Baseline |
| savedMinutes | Long | NOT NULL | 확보시간 |
| recoveryRate | Float | NOT NULL | 회수율 |
| createdAt | DateTime | NOT NULL | 생성일 |

---

## NotificationSetting

| 필드 | 타입 | 제약 | 설명 |
|---|---|---|---|
| userId | String | PK, FK→User | 사용자 |
| fcmToken | String? | - | FCM 토큰 |
| wasteThresholdMinutes | Long | NOT NULL, DEFAULT=120 | 낭비 임계치 (분) |
| reportAlertEnabled | Boolean | NOT NULL, DEFAULT=true | 리포트 알림 |
| wasteAlertEnabled | Boolean | NOT NULL, DEFAULT=true | 낭비 경고 알림 |

---

## ER 다이어그램 (텍스트)

```
User ─┬── App (1:N)
      ├── UsageEvent (1:N)
      ├── AppSession (1:N) ──── Context (1:1)
      ├── Activity (1:N) ────── Context (N:1, optional)
      ├── Goal (1:N) ────────── RecoveredTime (1:N)
      ├── WeeklyReport (1:N)
      └── NotificationSetting (1:1)
```
