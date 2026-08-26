---
name: timeback-requirements
description: TimeBack의 원문 요구를 FR/NFR, MVP/2차 범위, 검증 질문, 작업 단위로 추적할 때 사용한다. 요구사항 추가·수정·검토, scope 확인, spec과 requirements 대조 요청에 적용한다.
---

<!--
목적: 사용자 원문에서 검증 가능한 TimeBack 요구사항만 도출하고 범위 발명을 방지한다.
역할: 최신 spec을 FR/NFR·기술 제약·MVP 제외·미확정 질문으로 분류하고 요구사항과 후속 산출물의 추적성을 관리한다.
사용 흐름: state·spec 확인 → 최신 원문 append 확인 → 기존 FR/NFR 대조 → 새 요구 분류 → 모순·미확정 질문 작성 → 수용 조건·범위 검사 → Gate 승인 요청.
사용 시점: 요구사항 추가·수정·리뷰, scope 확인, spec과 requirements 대조 요청에 적용한다.
주의: 원문에 없는 플랫폼·라이브러리·수치·정책을 확정 요구로 만들지 않는다.
-->

# TimeBack 요구사항 추적 Skill

## 목적

`spec.md` 원문을 손상하지 않고 검증 가능한 요구사항으로 정리하며, 이후 설계·구현이 근거 없는 기능을 만들지 않게 한다.

## 입력

- 루트 `spec.md`
- `aidlc-docs/inception/requirements/requirements.md`
- `requirement-verification-questions.md`
- 사용자가 제공한 최신 원문과 게이트 답변

## 절차

1. `aidlc-state.md`에서 현재 단계를 확인한다.
2. 최신 요청이 `spec.md`에 원문 그대로 추가됐는지 확인한다. 누락됐다면 append-only로 추가한다.
3. 기존 FR/NFR 각각을 원문 절과 대조한다.
4. 새 내용을 다음으로 분류한다.
   - 기능 요구(FR)
   - 비기능 요구(NFR)
   - 기술 제약(사용자가 이미 지정한 것만)
   - MVP 제외/2차 기능
   - 미확정 질문
5. 원문에 없는 플랫폼·라이브러리·수치·정책을 요구사항으로 만들지 않는다.
6. 모순과 미확정 사항은 객관식 질문으로 작성하고 마지막 선택지를 `Other`로 둔다.
7. 요구사항별로 검증 가능한 수용 조건과 관련 NFR을 확인한다.
8. 변경 요약, 근거, 미확정 항목을 보고하고 게이트 승인을 요청한다.

## TimeBack 범위 검사

- Android MVP와 2차 개인화 기능이 섞이지 않았는가?
- 앱 기본 분류가 Context 최종 판정으로 잘못 취급되지 않았는가?
- 사용자 수정 우선순위가 명시됐는가?
- 시간 중복 집계 금지와 회수율 경계가 검증 가능한가?
- UsageStats와 활동 데이터의 민감성이 NFR에 반영됐는가?
- 기술 선택이 Construction 이전 문서에 유입되지 않았는가?

## 출력

- 갱신된 `requirements.md` 또는 검토 결과
- 원문 근거가 있는 FR/NFR 목록
- 미확정 질문
- MVP/2차/제외 범위 차이
- 다음 게이트에서 사람이 결정해야 할 사항

상세 확인에는 `references/traceability-checklist.md`를 사용한다.
