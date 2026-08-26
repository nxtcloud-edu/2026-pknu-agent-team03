# INCEPTION STEP 06 — 애플리케이션 설계 계획

## 1. 단계 실행 판단

- 실행 계획에서 STEP 06 실행이 승인되었다.
- Android UsageStats 경계, 세션·Context·시간 계산, 목표 활동, 화면, 익명 백업 서버 사이의 책임을 분리해야 한다.
- 따라서 애플리케이션 설계를 실행한다.

## 2. 입력과 산출물

- 입력: `inception/requirements/requirements.md`
- 입력: `inception/user-stories/stories.md`
- 입력: `inception/plans/execution-plan.md`
- 산출물: `inception/application-design/components.md`
- 범위: 논리 구성 요소, 책임, 데이터 소유권, 인터페이스 방향, 데이터·오류 흐름, 요구사항 추적
- 제외: 언어, 프레임워크, 런타임, 데이터베이스 제품, 배포 제품 선택

## 3. 설계 체크리스트

- [x] 아래 확인 질문의 답변을 검증한다.
- [x] Android 운영체제 경계와 앱 내부 구성 요소를 구분한다.
- [x] 세션 재구성, Context, 시간 지표, 목표 회수 영역의 책임을 분리한다.
- [x] 화면 구성 요소와 각 도메인 구성 요소의 의존 방향을 정의한다.
- [x] 익명 사용자, 기기 데이터, 백업 서버의 데이터 소유권을 정의한다.
- [x] 이벤트 수집부터 Timeline·대시보드·리포트까지 데이터 흐름을 작성한다.
- [x] 백업, 보관 기간, 전체 삭제 및 오프라인 오류 흐름을 작성한다.
- [x] 제어 가능한 시간과 실제 기기 검증을 위한 논리 검증 경계를 정의한다.
- [x] 모든 FR/NFR 및 US-01–US-25를 구성 요소에 추적한다.
- [x] 기술 스택 선택이나 MVP 제외 기능이 들어가지 않았는지 검증한다.

## 4. 확인 질문

### Q1. Android 앱 내부의 최상위 구성 요소는 어떤 기준으로 나눌까요?

A) 사용 이벤트·Context·시간 지표·목표 회수·데이터 관리 같은 도메인 능력별로 나누고 화면은 이를 사용
B) 홈·Timeline·앱 관리·시간 되찾기·목표·리포트 같은 화면별로 나눔
C) 앱 전체를 UI·업무 로직·데이터 접근의 공통 계층으로만 나눔
D) Android 앱 하나와 백업 서버 하나의 두 구성 요소만 정의
E) Other (please describe after [Answer]: tag below)

[Answer]: A

### Q2. 기기 데이터와 서버 백업 중 어느 쪽을 기준 데이터로 볼까요?

A) 기기 데이터를 기준으로 사용하고 서버는 백업 사본만 보관
B) 서버 데이터를 기준으로 사용하고 기기는 화면 표시용 사본을 보관
C) 기기와 서버를 동등한 기준으로 보고 양방향 변경을 병합
D) 원본 UsageEvent만 기기에 두고 나머지 데이터는 서버를 기준으로 사용
E) Other (please describe after [Answer]: tag below)

[Answer]: A

### Q3. 서버 백업은 언제 요청할까요?

A) 로컬 데이터 변경 후 자동 요청하고 실패분은 보관했다가 앱 시작 시 다시 시도
B) 사용자가 백업 버튼을 누를 때만 요청
C) 앱을 시작하거나 새로고침할 때 현재 전체 데이터를 한 번에 요청
D) 하루 한 번 정해진 시각에 일괄 요청
E) Other (please describe after [Answer]: tag below)

[Answer]: A

### Q4. 네트워크가 없을 때 앱은 어떻게 동작할까요?

A) 모든 로컬 수집·분석·목표 기능을 계속 제공하고 백업만 대기
B) Timeline 조회만 허용하고 생성·수정 기능은 제한
C) 목표·타이머·직접 기록만 허용하고 UsageStats 분석은 제한
D) 네트워크 연결 전까지 주요 화면 진입을 제한
E) Other (please describe after [Answer]: tag below)

[Answer]: A

### Q5. 익명 사용자 식별자는 어디서 처음 만들까요?

A) 기기에서 임의 식별자를 만들고 서버는 불투명한 식별값으로만 취급
B) 서버가 새 식별자를 발급하고 기기가 보관
C) 기기 하드웨어 정보를 변환하여 식별자로 사용
D) 사용자가 직접 생성한 복구 코드를 익명 식별자로 사용
E) Other (please describe after [Answer]: tag below)

[Answer]: C

### Q6. 전체 데이터 삭제는 기기와 서버에서 어떻게 조정할까요?

A) 기기와 서버 삭제를 하나의 작업으로 추적하고 양쪽이 끝난 뒤에만 완료 표시
B) 기기 데이터를 먼저 삭제하고 서버 삭제는 백그라운드에서 별도로 완료
C) 서버 데이터를 먼저 삭제한 뒤 성공한 경우에만 기기 데이터를 삭제
D) 기기 데이터 삭제와 서버 백업 삭제 버튼을 분리
E) Other (please describe after [Answer]: tag below)

[Answer]: A

### Q7. `components.md`에 화면 구성 요소를 어느 수준까지 포함할까요?

A) 주요 화면별 책임과 사용하는 도메인 구성 요소까지만 포함
B) 화면별 상태, 사용자 동작, 오류·빈 상태까지 포함
C) 화면은 하나의 UI 구성 요소로만 표시하고 상세 책임은 후속 기능 설계로 미룸
D) 화면 구성 요소는 제외하고 도메인과 서버 구성 요소만 작성
E) Other (please describe after [Answer]: tag below)

[Answer]: B
