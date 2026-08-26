# TimeBack 요구사항 확인 질문

## 작성 방법

각 질문에서 하나만 선택해 `[Answer]:` 뒤에 보기 문자를 적어 주세요. 보기로 표현되지 않는 답은 `E`를 고르고 같은 줄에 설명해 주세요.

## 질문

### Q1. 이번 작업에서 원하는 최종 산출물 범위는 무엇인가요?

A) 명세의 필수 기능 전체가 실제로 동작하는 Android MVP 앱
B) UsageStats 수집·세션 재구성·Context·핵심 지표에 집중한 Android 핵심 기능 앱
C) 요구사항·설계·구현 계획 문서까지만 작성하고 코드는 만들지 않음
D) 실제 UsageStats 연동 없이 전체 사용자 흐름을 보여주는 UI 프로토타입
E) Other (please describe after [Answer]: tag below)

[Answer]: A

### Q2. MVP의 사용자 계정과 데이터 저장 범위는 무엇인가요?

A) 로그인 없이 한 기기 안에만 저장
B) 계정 로그인과 서버 동기화까지 포함
C) 익명 사용자 식별자와 서버 백업만 포함
D) 기기 저장을 기본으로 하고 사용자가 선택할 때만 내보내기·가져오기 지원
E) Other (please describe after [Answer]: tag below)

[Answer]: C

### Q3. 지원할 최소 Android 버전 범위는 무엇인가요?

A) Android 10 이상
B) Android 12 이상
C) Android 14 이상
D) 현재 팀의 테스트 기기 버전만 우선 지원
E) Other (please describe after [Answer]: tag below)

[Answer]: C

### Q4. 최초 Baseline을 산정할 관찰 기간은 얼마인가요?

A) 연속 7일
B) 연속 14일
C) 연속 28일
D) 사용자가 7일·14일·28일 중 선택
E) Other (please describe after [Answer]: tag below)

[Answer]: A

### Q5. 최초 산정 후 Baseline은 어떻게 관리하나요?

A) 최초 값을 고정하고 사용자가 요청할 때만 다시 산정
B) 최근 28일 데이터로 매주 자동 갱신
C) 매월 직전 달 데이터로 자동 갱신
D) 최초 값을 유지하되 앱이 재산정을 제안하고 사용자가 승인
E) Other (please describe after [Answer]: tag below)

[Answer]: D

### Q6. Baseline 관찰 기간이 끝나기 전에는 확보한 시간과 회수율을 어떻게 표시하나요?

A) Baseline 관련 지표를 숨기고 남은 관찰 기간만 표시
B) 현재까지의 잠정값을 `추정`으로 표시
C) 첫날부터 당일 값을 임시 Baseline으로 계산
D) 사용자가 초기 Baseline을 직접 입력하도록 요청
E) Other (please describe after [Answer]: tag below)

[Answer]: A

### Q7. Foreground 또는 Background 짝이 없는 UsageEvent는 어떻게 처리하나요?

A) 다음 앱 전환이나 화면 종료 이벤트 시각으로 세션을 닫음
B) 정해진 최대 세션 길이에서 잘라 임시 세션으로 저장
C) 불완전한 이벤트는 지표에서 제외하고 사용자에게 데이터 누락을 표시
D) 사용자가 Timeline에서 종료 시각을 직접 보정할 때까지 보류
E) Other (please describe after [Answer]: tag below)

[Answer]: A 

### Q8. 활동과 앱 기본 분류가 충돌할 때 자동 판정의 우선순위는 무엇인가요?

A) 활동을 우선하되 명백한 충돌은 확인 전까지 `MIXED`로 둠
B) 앱의 기본 분류를 우선하고 사용자가 바꿀 때만 활동을 반영
C) 겹친 시간은 모두 `MIXED`로 두고 사용자가 반드시 확정
D) 자동 판정하지 않고 모든 겹침을 사용자 확인 대상으로 둠
E) Other (please describe after [Answer]: tag below)

[Answer]: A

### Q9. Context 확인 질문은 언제 띄우나요?

A) 활동 분류와 앱 기본 분류가 충돌할 때만
B) 모든 활동·앱 중첩이 처음 발생할 때
C) 자동 판정 신뢰도가 기준 이하일 때만
D) 자동 질문은 띄우지 않고 Timeline 수정 기능만 제공
E) Other (please describe after [Answer]: tag below)

[Answer]: A

### Q10. `되찾은 시간`과 `확보한 시간`의 관계는 어떻게 제한하나요?

A) 목표 활동 시간은 기록하되 기간별 되찾은 시간은 확보한 시간을 넘지 않게 제한
B) 목표 활동 시간을 전부 기록하여 회수율이 100%를 넘을 수 있게 함
C) 확보한 시간 안에서 시작한 타이머 기록만 되찾은 시간으로 인정
D) 목표 활동 기록마다 사용자가 되찾은 시간 포함 여부를 직접 선택
E) Other (please describe after [Answer]: tag below)

[Answer]: B

### Q11. 목표 활동 기록끼리 시간이 겹칠 때 누적 시간은 어떻게 계산하나요?

A) 실제 경과시간의 합집합만 한 번 계산하고 대표 목표 하나를 선택
B) 실제 경과시간은 한 번만 계산하되 사용자가 목표별 비율로 나눔
C) 목표별로 각각 누적하여 중복 계산을 허용
D) 겹치는 목표 활동 기록의 저장을 막음
E) Other (please describe after [Answer]: tag below)

[Answer]: A

### Q12. 일간·주간 집계 경계는 무엇인가요?

A) 기기 현지 시간 기준 자정, 주간은 월요일 00:00 시작
B) 기기 현지 시간 기준 자정, 주간은 일요일 00:00 시작
C) 현재 시각 기준 최근 24시간·최근 7일의 이동 구간
D) 사용자가 하루 시작 시각과 주 시작 요일을 설정
E) Other (please describe after [Answer]: tag below)

[Answer]: A

### Q13. 자정을 넘어가는 앱 세션이나 활동 기록은 어떻게 집계하나요?

A) 자정에서 나눠 각 날짜에 실제 겹친 시간만 배분
B) 시작한 날짜에 전체 시간을 귀속
C) 종료한 날짜에 전체 시간을 귀속
D) Timeline에는 하나로 보이되 통계에서만 날짜별로 나눔
E) Other (please describe after [Answer]: tag below)

[Answer]: A

### Q14. Usage Access 권한이 없거나 회수된 경우 MVP는 어떻게 동작하나요?

A) 권한 안내를 표시하고 활동·목표·타이머의 수동 기능은 계속 제공
B) 권한을 허용할 때까지 앱의 주요 화면 진입을 막음
C) 샘플 데이터로 기능을 체험하게 하고 실제 기록은 저장하지 않음
D) 앱 사용 분석만 중지하고 마지막 계산 결과를 계속 표시
E) Other (please describe after [Answer]: tag below)

[Answer]: B

### Q15. 사용 기록의 보관 기간과 삭제 범위는 무엇인가요?

A) 사용자가 삭제할 때까지 모든 원본 이벤트와 파생 데이터를 보관
B) 원본 UsageEvent는 90일 후 삭제하고 집계·목표 기록은 유지
C) 원본과 파생 데이터를 모두 1년 보관 후 삭제
D) 사용자가 보관 기간을 선택하고 전체 데이터 즉시 삭제 기능을 제공
E) Other (please describe after [Answer]: tag below)

[Answer]: D

### Q16. 새 UsageEvent가 수집된 뒤 Timeline과 대시보드는 얼마나 빨리 갱신되어야 하나요?

A) 앱을 열거나 새로고침하면 5초 이내
B) 백그라운드 수집을 포함해 1분 이내
C) 15분 이내 주기 갱신
D) 하루 한 번 일괄 갱신
E) Other (please describe after [Answer]: tag below)

[Answer]: A

### Q17. MVP 완료를 판정할 테스트 수준은 무엇인가요?

A) 핵심 시간 계산 단위 테스트와 주요 화면·저장소 통합 테스트
B) A에 더해 실제 Android 기기의 UsageStats 권한·수집 시나리오 테스트
C) B에 더해 주요 사용자 흐름의 자동 UI 테스트
D) 수동 시나리오 테스트만 수행
E) Other (please describe after [Answer]: tag below)

[Answer]: B

### Q18. 첨부 원문이 `27. 핵심 차별점`의 `YouTube = �`에서 끝난 누락 부분은 어떻게 처리할까요?

A) 누락된 원문을 다시 제공한 뒤 요구사항 생성을 진행
B) 1~26절을 완전한 범위로 보고 끊긴 27절은 요구사항 근거에서 제외
C) 현재 남아 있는 27절과 앞 절의 내용만 근거로 핵심 차별점을 정리
D) 27절 전체를 MVP 범위 밖의 설명으로 보고 구현 요구사항에는 반영하지 않음
E) Other (please describe after [Answer]: tag below)

[Answer]: B
