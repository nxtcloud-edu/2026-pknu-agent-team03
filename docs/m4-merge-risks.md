# M4 머지 시 충돌 가능성 및 대응 방안

## 요약

4명이 STEP 5까지 각자 작업 후 한꺼번에 머지하는 전략.
M4(track-backup-server)는 `src/main/java/com/timeback/backup/` 에서만 작업하므로
파일 충돌 가능성은 낮지만, **계약 인터페이스**와 **공통 설정** 레벨에서 조율 필요.

---

## 1. 충돌 가능성 분석

| 위험 영역 | 충돌 가능성 | 원인 | 영향 |
|---|---|---|---|
| `src/` 패키지 구조 | **낮음** | 트랙별 폴더 분리 | 같은 파일 수정 없음 |
| `contracts/` 공통 타입 | **중간** | 1번(device-data)도 CommittedChange, EntityType 사용 | 중복 정의 또는 위치 불일치 |
| Gradle/빌드 설정 | **중간** | 4명이 각자 build.gradle 만들 수 있음 | 머지 시 파일 충돌 |
| 기술 스택 결정 문서 | **낮음** | Java 통일 확정 | 세부 라이브러리 차이 가능 |
| 서버 포트/설정 | **낮음** | M4만 서버 있음 | 다른 트랙과 무관 |
| CT-03 CommittedChange 인터페이스 | **높음** | 1번이 제공자, 4번이 소비자 | 필드명·타입 불일치 시 컴파일 에러 |
| CT-05 BackupChange 인터페이스 | **낮음** | M4만 사용 | 독립적 |

## 2. 위험별 대응 방안

### 2-1. contracts/ 공통 타입 중복

**문제**: 1번(device-data)이 CommittedChange를 자기 패키지에 정의했을 수 있음.

**대응**:
- 머지 전에 1번한테 물어보기: "CommittedChange 어디에 정의했어?"
- 같은 패키지면 → 하나만 남기고 import 통일
- 다른 패키지면 → 공통 `contracts/` 로 이동 후 양쪽 import 수정
- 최악: 필드명이 다르면 → 1번 기준으로 내 코드 수정 (1번이 제공자이므로)

### 2-2. Gradle 빌드 파일 충돌

**문제**: 4명이 각자 build.gradle 작성.

**대응**:
- **내 접근**: 빌드 파일을 최소한으로 유지 (의존성만 명시)
- 머지 시: 한 명이 통합 빌드 파일 정리 (멀티모듈이면 각 트랙을 서브모듈로)
- 지금은 빌드 파일 없이 코드만 올림 → 나중에 팀이 함께 설정

### 2-3. CommittedChange 인터페이스 불일치

**문제**: 내가 정의한 CommittedChange 필드와 1번이 실제 만드는 것이 다를 수 있음.

**대응**:
- 내 CommittedChange는 CT-05 공통 문서(domain-entities.md §7.2)를 **그대로** 따름
- 1번도 같은 문서 기준이면 일치할 것
- 불일치 시: 1번 필드에 맞춰 내 코드 수정 (어댑터 패턴 또는 직접 수정)
- **예방**: 머지 전 1번한테 CommittedChange 클래스 파일 보여달라고 요청

### 2-4. 패키지 네이밍

**문제**: `com.timeback.backup` vs 다른 사람이 `com.timeback.device` 등

**대응**:
- 내 코드는 `com.timeback.backup.*` 하위에서만 작업 → 충돌 없음
- 공통 contracts가 별도 패키지(`com.timeback.contracts`)로 합의되면 → import만 변경
- **현재 가정**: 각 트랙이 `com.timeback.{트랙명}` 사용

### 2-5. 서버 실행 환경

**문제**: M4만 서버가 있어서 다른 트랙과 포트/설정 충돌 없음.

**대응**: 없음 (위험 없음). 단, STEP 6 통합 시 서버 시작 스크립트를 루트에 놓을 수 있으니 그때 조율.

---

## 3. 머지 전 체크리스트

- [ ] 1번한테 CommittedChange 클래스 위치·필드 확인
- [ ] 팀 공통 패키지 구조 합의 (com.timeback.{트랙명} OK인지)
- [ ] Gradle 빌드 파일 통합 담당자 정하기
- [ ] 내 contracts/ 와 다른 트랙 contracts 중복 제거
- [ ] 전체 빌드 한번 돌려서 컴파일 에러 확인

## 4. 머지 순서 추천

1. CP-0 공통 문서 (이미 main에 있음)
2. 1번(device-data) 먼저 (CommittedChange 제공자)
3. 2번(domain) (CT-01~02 구현)
4. **4번(backup)** ← 1번 CommittedChange에 맞춰 확인 후
5. 3번(ui) 마지막 (다른 3명의 API를 호출하므로)
