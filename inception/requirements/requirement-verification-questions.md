# 요구사항 검증 질문

> `spec.md` 에 없는 것만 묻는다. 이미 적힌 것을 다시 묻지 않는다.

---

## Q1. Baseline 측정 기간

Baseline(평소 낭비시간)을 측정하는 초기 기간은 얼마로 설정할까요?

A) 3일  
B) 7일 (1주)  
C) 14일 (2주)  
D) 사용자가 직접 선택  
E) Other (please describe after [Answer]: tag below)

[Answer]: BB

---

## Q2. 팀 규모와 역할

이 프로젝트를 진행하는 팀 구성은 어떻게 되나요?

A) 1인 개발 (풀스택)  
B) 2~3인 (프론트/백엔드 분리)  
C) 4~6인 (프론트/백엔드/디자인 분리)  
D) 6인 이상 (역할별 전문화)  
E) Other (please describe after [Answer]: tag below)

[Answer]: CC

---

## Q3. 데이터 저장 방식

앱 데이터(세션, 활동, Context 등)의 저장 위치는 어떻게 할까요?

A) 로컬 전용 (기기 내 SQLite 등)  
B) 서버 동기화 포함 (백엔드 API + DB)  
C) 로컬 우선 + 선택적 클라우드 백업  
D) MVP는 로컬, 이후 서버 추가  
E) Other (please describe after [Answer]: tag below)

[Answer]: BB

---

## Q4. 인증/계정 체계

사용자 인증(로그인)이 MVP에 필요한가요?

A) 불필요 — 기기 단독 사용  
B) 간단한 로컬 PIN/비밀번호  
C) 소셜 로그인 (Google 등)  
D) 자체 회원가입 + 로그인  
E) Other (please describe after [Answer]: tag below)

[Answer]: BB

---

## Q5. 알림 및 트리거

MVP에서 사용자에게 알림(Notification)을 보내는 기능이 포함되나요?

A) MVP에서 제외 (2차 기능에 이미 포함됨을 확인)  
B) 하루 요약 알림 정도는 포함  
C) 실시간 낭비 경고 알림 포함  
D) 타이머 종료 알림만 포함  
E) Other (please describe after [Answer]: tag below)

[Answer]: CC

---

## Q6. 주간 리포트 전달 방식

주간 리포트는 어떤 형태로 사용자에게 전달되나요?

A) 앱 내 화면에서만 확인  
B) 앱 내 + 푸시 알림으로 안내  
C) 앱 내 + 이메일 발송  
D) 앱 내 + 공유 가능한 이미지 생성  
E) Other (please describe after [Answer]: tag below)

[Answer]: BB

---

## Q7. Context 자동 분류의 초기 동작

MVP에서 Context 분류가 자동으로 되지 않는 앱 세션은 어떻게 처리할까요?

A) 모두 기본 분류(앱 설정값)대로 처리하고, 사용자가 수정  
B) 활동과 겹치지 않는 세션만 기본 분류 적용, 겹치면 항상 질문  
C) 임계값(예: 겹침 50% 이상)에 따라 질문 여부 결정  
D) 모든 세션을 사용자에게 확인 요청  
E) Other (please describe after [Answer]: tag below)

[Answer]: AA

