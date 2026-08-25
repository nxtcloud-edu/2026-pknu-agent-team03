# API 인터페이스 계약

> 4명이 각자 단위를 코딩할 때 서로 맞물리는 지점입니다.
> 이 API 스펙대로 만들면 합칠 때 충돌 없이 연결됩니다.

---

## 공통 사항

- Base URL: `http://{서버}/api`
- 인증: `Authorization: Bearer {JWT}`
- 응답 형식: JSON
- 에러 응답: `{ "success": false, "message": "에러 설명" }`
- 날짜 형식: `YYYY-MM-DD`
- 시간 형식: ISO 8601 (`2026-08-25T19:05:00Z`)

---

## Unit 1: data-collection 담당 API

### POST /api/auth/register
```json
// Request
{ "deviceId": "abc123" }

// Response (200)
{ "userId": "uuid", "token": "jwt-token" }
```

### POST /api/usage/sessions
```json
// Request
{ "sessions": [
    { "sessionId": "uuid", "packageName": "com.youtube", "startTime": "2026-08-25T19:05:00Z", "endTime": "2026-08-25T19:55:00Z", "duration": 3000000, "sessionDate": "2026-08-25" }
] }

// Response (200)
{ "synced": 1, "message": "OK" }
```

### GET /api/usage/sessions?date=2026-08-25
```json
// Response (200)
[ { "sessionId": "uuid", "packageName": "com.youtube", "startTime": "...", "endTime": "...", "duration": 3000000, "sessionDate": "2026-08-25" } ]
```

### POST /api/usage/apps
```json
// Request
{ "classifications": [
    { "packageName": "com.youtube", "appName": "YouTube", "classification": "WASTE" }
] }

// Response (200)
{ "synced": 1, "message": "OK" }
```

### GET /api/usage/apps
```json
// Response (200)
[ { "packageName": "com.youtube", "appName": "YouTube", "classification": "WASTE" } ]
```

---

## Unit 2: context-engine 담당 API

### POST /api/context/activities
```json
// Request
{ "activities": [
    { "activityId": "uuid", "activityType": "RUNNING", "customName": null, "startTime": "2026-08-25T19:00:00Z", "endTime": "2026-08-25T20:00:00Z", "activityDate": "2026-08-25" }
] }

// Response (200)
{ "synced": 1, "message": "OK" }
```

### GET /api/context/activities?date=2026-08-25
```json
// Response (200)
[ { "activityId": "uuid", "activityType": "RUNNING", ... } ]
```

### POST /api/context/contexts
```json
// Request
{ "contexts": [
    { "contextId": "uuid", "sessionId": "session-uuid", "activityId": "activity-uuid", "classification": "MIXED", "overlapRatio": 0.83, "userConfirmed": false, "conflictResolution": null, "contextDate": "2026-08-25" }
] }

// Response (200)
{ "synced": 1, "message": "OK" }
```

### GET /api/context/waste?date=2026-08-25
```json
// Response (200)
{ "date": "2026-08-25", "totalWasteMinutes": 65, "confirmedWasteMinutes": 35, "sessionCount": 3 }
```

### GET /api/context/waste/weekly?startDate=2026-08-19
```json
// Response (200)
{ "startDate": "2026-08-19", "endDate": "2026-08-25", "totalWasteMinutes": 680, "dailyBreakdown": [...] }
```

---

## Unit 3: time-recovery 담당 API

### POST /api/goals
```json
// Request
{ "name": "개인 프로젝트", "targetTime": 6000 }

// Response (200)
{ "goalId": "uuid", "name": "개인 프로젝트", "targetTime": 6000, "accumulatedTime": 0, "progressRate": 0, "isActive": true }
```

### GET /api/goals
```json
// Response (200)
[ { "goalId": "uuid", "name": "...", "targetTime": 6000, "accumulatedTime": 1620, "progressRate": 27.0, "isActive": true } ]
```

### PUT /api/goals/{id}
```json
// Request
{ "name": "새 이름", "targetTime": 3000 }
```

### DELETE /api/goals/{id}
```
// Response (204 No Content)
```

### POST /api/goals/{id}/recovered
```json
// Request
{ "recoveredId": "uuid", "duration": 40, "method": "TIMER", "startTime": "2026-08-25T21:00:00Z", "endTime": "2026-08-25T21:40:00Z", "recoveredDate": "2026-08-25" }

// Response (200)
{ "recoveredId": "uuid", "duration": 40, "method": "TIMER" }
```

### GET /api/goals/recovery-rate?weekStart=2026-08-19
```json
// Response (200)
{ "savedTimeMinutes": 300, "recoveredTimeMinutes": 240, "rate": 80.0 }
```

---

## Unit 4: presentation 담당 API

### GET /api/reports?weekStart=2026-08-19
```json
// Response (200)
{ "reportId": "uuid", "weekStartDate": "2026-08-19", "wasteMinutes": 680, "baselineMinutes": 980, "savedMinutes": 300, "recoveryRate": 80.0 }
```

### GET /api/reports/list
```json
// Response (200)
[ { "reportId": "uuid", "weekStartDate": "2026-08-19", ... } ]
```

### POST /api/notifications/register-token
```json
// Request
{ "token": "fcm-token-string" }

// Response (200)
```

### GET /api/notifications/settings
```json
// Response (200)
{ "wasteThresholdMinutes": 120, "reportAlertEnabled": true, "wasteAlertEnabled": true }
```

### PUT /api/notifications/settings
```json
// Request
{ "wasteThresholdMinutes": 90, "reportAlertEnabled": true, "wasteAlertEnabled": false }
```

---

## 단위 간 의존 정리

| 호출하는 쪽 | 사용하는 API | 제공하는 쪽 |
|---|---|---|
| context-engine | GET /api/usage/sessions | data-collection |
| context-engine | GET /api/usage/apps | data-collection |
| time-recovery | GET /api/context/waste/weekly | context-engine |
| presentation | GET /api/goals | time-recovery |
| presentation | GET /api/context/waste | context-engine |
| presentation | GET /api/goals/recovery-rate | time-recovery |
