# STEP 05 실행 계획

## 체크리스트

- [ ] Complexity/Depth 기반 CONSTRUCTION 조건 단계 판정
- [ ] CONSTRUCTION 단계별 실행 여부 결정
- [ ] 전체 진행 순서 정리

---

## 1. 판정 근거

- **Complexity**: Complex
- **Requirements Depth**: Comprehensive
- **팀 규모**: 4~6인
- **구성**: Android 클라이언트 + 백엔드 API + 서버 동기화

Complex + Comprehensive이므로 CONSTRUCTION 조건 단계를 **모두 실행**한다.

---

## 2. CONSTRUCTION 단계 실행 여부

| STEP | 단계 | 실행 여부 | 사유 |
|---|---|---|---|
| 01 | 기능 설계 | ✅ 실행 | Complex — 비즈니스 규칙(Context 판정, 시간 중첩 분석, Baseline 계산)이 복잡 |
| 02 | 비기능 요구 | ✅ 실행 | 서버 동기화, 실시간 알림, 4~6인 팀 — 기술 스택 결정 필요 |
| 03 | 비기능 설계 | ✅ 실행 | 성능 요구(3초/2초), 동기화 패턴, 알림 아키텍처 설계 필요 |
| 04 | 인프라 설계 | ✅ 실행 | 서버 배포, DB, API 구조 설계 필요 |
| 05 | 코드 생성 | ✅ 항상 | — |
| 06 | 빌드와 테스트 | ✅ 항상 | — |

---

## 3. 전체 진행 순서

```
INCEPTION (현재)
  STEP 06 애플리케이션 설계 → components.md
  STEP 07 작업 단위 쪼개기 → unit-of-work.md

CONSTRUCTION (단위별 반복)
  각 단위마다:
    STEP 01 기능 설계
    STEP 02 비기능 요구 (tech-stack-decisions.md)
    STEP 03 비기능 설계
    STEP 04 인프라 설계
    STEP 05 코드 생성
  전체 합산:
    STEP 06 빌드와 테스트
```

---

## 4. 참고

- 기술 스택(언어, 프레임워크, 런타임)은 CONSTRUCTION STEP 02에서 결정한다.
- INCEPTION 단계에서는 기술 중립적으로 설계한다.
- 코드 생성 순서: 업무 규칙 → API → 저장 → 화면 (층마다 테스트 동반)
