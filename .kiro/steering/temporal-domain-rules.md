---
inclusion: auto
name: timeback-temporal-domain-rules
description: TimeBack의 UsageEvent 세션화, 시간 구간, Activity 중첩, Context 판정, Baseline·Saved·Recovered Time 계산을 설계·구현·검토할 때 적용한다.
---

<!-- 목적: 시간 계산 정확성 보장 | 역할: 세션·중첩·Context·집계 불변식 제공 | 장점: 중복·음수·경계 오류 예방 | 흐름: 시간 도메인 작업 감지→구간 정규화→판정→불변식 검사 -->

# 시간 및 Context 도메인 규칙

## 시간 표현

- 내부 시간 구간은 반개구간 `[start, end)`으로 취급한다.
- 유효 구간은 항상 `end > start`를 만족해야 한다.
- 지속시간은 저장된 값보다 `end - start`에서 계산하는 것을 우선한다.
- 저장 기준 시각과 사용자 표시 시간대를 분리한다.
- 자정 통과, 시간대 변경, 시스템 시간 변경을 정상적인 경계 조건으로 다룬다.

## AppSession 재구성

- Foreground/Background 이벤트를 시간순으로 안정적으로 정렬한다.
- 중복·역순·누락 이벤트가 음수 또는 무한 세션을 만들지 못하게 한다.
- 종료 이벤트가 없는 경우 확정 요구사항의 추정 규칙을 적용하고 `inferred` 여부를 보존한다.
- 다음 앱의 Foreground가 나타나도 이전 앱 종료를 무조건 단정하지 않는다. Android 이벤트 의미와 멀티윈도우 가능성을 검토한다.
- 원본 이벤트와 파생 세션의 추적 관계를 유지한다.

## 중첩과 Context

두 구간의 중첩은 `max(startA, startB) < min(endA, endB)`일 때 존재한다.

- 불연속, 경계 접촉, 부분 중첩, 완전 포함, 동일 구간, 다중 중첩을 구분한다.
- Activity와 AppSession이 겹치면 겹친 구간과 나머지 구간을 분할해 각각 판정할 수 있어야 한다.
- 운동 60분과 그 안의 YouTube 50분을 110분으로 집계하지 않는다.
- 사용자 확인·수정 Context는 재분석 이후에도 자동 판정으로 덮어쓰지 않는다.
- Context 판정에는 근거, confidence, userConfirmed 여부를 남긴다.

## 계산 불변식

- `WasteTime >= 0`
- `SavedTime`의 음수 처리 정책은 확정 요구사항을 따른다.
- `RecoveredTime >= 0`
- Saved Time이 0일 때 0으로 나누지 않는다.
- 회수율의 100% 초과 처리 정책은 확정 요구사항을 따른다.
- 일·주·월 집계는 동일 Context 구간을 중복 반영하지 않는다.

도메인 규칙과 Baseline 기간·세션 추정 제한 같은 조정 수치를 서로 다른 구성 요소로 분리한다.
