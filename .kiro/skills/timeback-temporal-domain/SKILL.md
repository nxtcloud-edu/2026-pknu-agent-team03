---
name: timeback-temporal-domain
description: TimeBack의 UsageEvent 세션화, 시간 구간 중첩, Context 분할·병합, 낭비·확보·되찾은 시간과 회수율 계산을 설계하거나 검토할 때 사용한다.
---

<!--
목적: 세션화와 시간 계산이 실제 경과시간을 초과하거나 사용자 확정 Context를 잃는 오류를 방지한다.
역할: UsageEvent 상태 전이, 반개구간, Activity 중첩 분할, Context 우선순위, Waste/Baseline/Saved/Recovered 계산과 경계값을 검토한다.
사용 흐름: 관련 요구 확인 → 시간 기준·구간 정규화 → 세션 상태 전이 작성 → 원자 구간 분할 → Context 판정 → 중복 없는 집계 → 0·음수·상한 정책 및 테스트 검증.
사용 시점: AppSession, Timeline, Context, Baseline, 확보시간, 되찾은 시간, 회수율 설계·구현·버그 조사에 적용한다.
주의: 앱 이름만으로 최종 Context를 확정하거나 겹치는 지속시간을 단순 합산하지 않는다.
-->

# TimeBack 시간 도메인 검토 Skill

## 목적

시간 계산이 실제 경과시간을 초과하거나 이벤트 누락 때문에 잘못된 낭비시간을 만드는 것을 방지한다.

## 사용 시점

- AppSession 재구성 설계 또는 구현
- Activity와 AppSession 중첩 분석
- Context 생성·수정·재분석
- Waste/Baseline/Saved/Recovered 계산
- Timeline·주간 집계 오류 조사
- 관련 테스트 작성과 리뷰

## 절차

1. 관련 FR/NFR과 확정된 검증 답변을 먼저 읽는다.
2. 입력 이벤트와 파생 데이터의 시간 단위, 정렬, 시간대 기준을 확인한다.
3. 모든 구간을 `[start, end)`로 정규화하고 유효하지 않은 구간 처리 방식을 정한다.
4. 세션 재구성 상태 전이를 표로 작성한다.
   - 정상 Foreground → Background
   - 중복 Foreground
   - Background 누락
   - orphan Background
   - 화면 꺼짐·재부팅·날짜 경계
5. 구간 경계점을 기준으로 AppSession과 Activity를 원자 구간으로 분할한다.
6. 각 원자 구간에 Default, Activity, 사용자 확인의 우선순위를 적용한다.
7. 사용자 확정 Context가 재분석으로 덮어써지지 않는지 확인한다.
8. 일·주 집계에서 동일 원자 구간을 한 번만 합산한다.
9. Baseline 데이터 부족, Saved Time 0/음수, Recovered 초과 정책을 검증한다.
10. `references/temporal-invariants.md`의 사례를 테스트로 연결한다.

## 설계 산출물

- 시간 표현과 정규화 규칙
- 세션화 상태 전이 또는 의사코드
- 중첩 분할 예시
- Context 우선순위 표
- 계산식과 0/음수/상한 정책
- 필수 단위 테스트 목록

## 금지

- 앱 이름만으로 최종 Context 확정
- 겹치는 Activity와 AppSession 지속시간 단순 합산
- 누락 종료 이벤트에 무제한 현재 시각 적용
- 사용자 수정 결과를 자동 재분석으로 덮어쓰기
- 요구사항 미확정 수치를 코드 상수로 확정
