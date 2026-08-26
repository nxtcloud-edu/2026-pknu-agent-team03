---
name: android-usagestats
description: Android UsageStatsManager 권한, queryEvents 수집, UsageEvents를 AppSession으로 변환하는 어댑터와 플랫폼 경계를 설계·구현·검토할 때 사용한다.
---

<!--
목적: Android UsageEvents를 시간 계산에 사용할 수 있는 신뢰 가능한 플랫폼 중립 입력으로 변환한다.
역할: Usage Access 안내, queryEvents 수집 범위, 이벤트 매핑, 누락·중복 대응, AppSession 경계를 설계·구현·검토한다.
사용 흐름: 공식 Android 동작 확인 → 개인정보 요구 확인 → 권한 흐름 정의 → 이벤트 수집·중립 모델 변환 → 누락·중복 정책 적용 → 합성 fixture와 실제 기기 경계 검증.
사용 시점: UsageStatsManager, UsageEvents, 권한 화면, 수집 어댑터, 세션 입력 모델을 다루는 요청에 적용한다.
주의: 실제 앱 사용 기록을 로그나 fixture에 사용하지 않고, 기술 스택 승인 전에는 라이브러리를 확정하지 않는다.
-->

# Android UsageStats Skill

## 목적

Android 사용 이벤트의 플랫폼 제약을 숨기지 않고, TimeBack 도메인 계산과 분리된 신뢰 가능한 수집 경계를 만든다.

## 시작 전

1. 대상 Android API 범위와 기기 정책을 확인한다.
2. 현재 Android 공식 문서에서 `UsageStatsManager`, `UsageEvents.Event`, Usage Access 동작을 확인한다.
3. 요구사항과 `timeback-privacy-review`를 함께 적용한다.
4. 기술 스택이 Construction에서 승인되기 전에는 라이브러리나 아키텍처를 확정하지 않는다.

## 절차

1. Usage Access 상태 확인과 설정 화면 이동 흐름을 정의한다.
2. 권한 미부여·철회 시 수집을 중단하고 사용자에게 제한 기능을 안내한다.
3. 조회 기간을 명시하고 `queryEvents(begin, end)` 결과를 즉시 플랫폼 중립 모델로 변환한다.
4. Android 이벤트 타입을 지원/무시/관찰 대상으로 분류한다.
5. 이벤트를 timestamp 기준으로 정렬하되 동일 timestamp의 안정적인 처리 규칙을 둔다.
6. 세션화 로직은 Android API 객체 밖의 순수 도메인 구성 요소로 전달한다.
7. 이벤트 누락·중복·앱 전환·화면 꺼짐·재부팅·멀티윈도우 가능성을 검토한다.
8. 마지막 수집 지점, 재조회 overlap, 중복 제거 키를 정의한다.
9. 실제 package name이나 사용 기록을 로그에 남기지 않는다.
10. 합성 이벤트 fixture로 수집 어댑터와 세션화 경계를 검증한다.

## 결과물

- 권한 상태 표와 UX 흐름
- 지원 이벤트 타입 표
- Android 이벤트 → 플랫폼 중립 이벤트 매핑
- 수집 범위, 재수집, 중복 제거 정책
- 누락 이벤트 처리 정책
- 테스트 fixture와 검증 결과

## 검토 기준

`references/usagestats-checklist.md`를 사용한다. API 동작이 불확실하면 기억으로 단정하지 말고 공식 Android 문서를 확인한다.
