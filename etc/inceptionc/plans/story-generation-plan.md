# INCEPTION STEP 04 — 유저 스토리 생성 계획

## 단계 실행 판단

- `requirements.md`의 Complexity Estimate가 `Complex`이고 Requirements Depth가 `Comprehensive`이다.
- Android 권한, 사용 이벤트, Context 판정, 시간 계산, 목표 활동, 리포트, 익명 백업으로 이어지는 사용자 여정이 여러 화면과 오류 흐름에 걸쳐 있다.
- 따라서 STEP 04 유저 스토리를 실행한다.

## 입력과 산출물

- 입력: `inception/requirements/requirements.md`
- 산출물: `inception/user-stories/stories.md`
- 범위: 승인된 MVP 요구사항만 포함하고 2차 기능과 기술 스택 선택은 포함하지 않는다.

## 실행 체크리스트

- [x] 아래 확인 질문의 답변을 검증한다.
- [x] 핵심 사용자와 보조 시스템 행위자를 확정한다.
- [x] FR-1부터 FR-10까지 사용자 가치 기준 Epic으로 묶는다.
- [x] 각 스토리에 식별자, 우선순위, 사용자 스토리 문장, 인수 조건을 작성한다.
- [x] 권한 거부, Baseline 미완료, 이벤트 누락, Context 충돌, 기록 중첩, 백업·삭제 오류 흐름을 인수 조건에 포함한다.
- [x] NFR-1부터 NFR-5까지 관련 스토리의 품질 조건으로 연결한다.
- [x] 모든 FR/NFR과 스토리 사이의 추적성 표를 작성한다.
- [x] MVP 제외 항목이나 언어·프레임워크를 추가하지 않았는지 검증한다.

## 확인 질문

### Q1. 유저 스토리의 우선순위를 정할 때 대표할 핵심 사용자는 누구인가요?

A) 학업과 자기계발 시간을 확보하려는 대학생
B) 업무 외 시간의 무의식적 스마트폰 사용을 줄이려는 직장인
C) 운동과 생활 루틴에 시간을 더 쓰려는 사용자
D) 특정 직업이나 활동으로 한정하지 않은 일반 성인 스마트폰 사용자
E) Other (please describe after [Answer]: tag below)

[Answer]: D

### Q2. 유저 스토리 문서의 구성 방식은 무엇으로 할까요?

A) Epic → 사용자 스토리 → Given/When/Then 인수 조건 → 요구사항 추적
B) 화면 → 사용자 행동 → 체크리스트형 인수 조건 → 요구사항 추적
C) 사용자 여정 단계 → 사용자 스토리 → Given/When/Then 인수 조건
D) 간단한 사용자 스토리 문장과 우선순위만 작성
E) Other (please describe after [Answer]: tag below)

[Answer]:  A

### Q3. 한 개 유저 스토리의 크기는 어느 정도로 나눌까요?

A) 독립적으로 검증 가능한 사용자 결과 하나당 한 스토리
B) 하나의 화면 전체를 한 스토리
C) 요구사항의 FR 영역 하나를 한 스토리
D) 온보딩부터 주간 리포트까지 전체 흐름을 소수의 큰 스토리로 구성
E) Other (please describe after [Answer]: tag below)

[Answer]:  A

### Q4. MVP 시연과 구현 순서에서 가장 먼저 완성할 핵심 사용자 여정은 무엇인가요?

A) 권한 허용 → 사용 이벤트 수집 → Timeline 및 Context 수정 → 낭비시간 확인
B) 목표 생성 → 타이머 및 직접 기록 → 되찾은 시간과 진행률 확인
C) 7일 Baseline 완성 → 확보한 시간 → 대시보드 및 주간 리포트 확인
D) 익명 사용자 생성 → 서버 백업 → 보관 기간 설정 및 전체 삭제
E) Other (please describe after [Answer]: tag below)

[Answer]: A

### Q5. 자동 수집·계산·백업처럼 화면 밖에서 일어나는 시스템 동작은 어떻게 표현할까요?

A) 사용자 스토리 아래의 시스템 인수 조건으로 포함
B) 시스템을 별도 행위자로 둔 독립 스토리로 작성
C) 사용자에게 결과가 보이는 경우만 스토리로 작성하고 내부 동작은 제외
D) 별도의 시스템 동작 부록으로 분리
E) Other (please describe after [Answer]: tag below)

[Answer]: A
