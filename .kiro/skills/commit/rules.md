# TimeBack Git 커밋 규칙

<!-- 목적: 커밋 판단 기준 통일 | 역할: 타입·분리·메시지·금지사항 정의 | 장점: 리뷰와 rollback 단순화 | 흐름: 변경 목적 확인→타입 선택→범위 검사→메시지 작성 -->

## 메시지

- 한글 한 줄: `타입: 간결한 설명`
- FR/NFR 번호와 Android, UsageStats 같은 고유명사는 사용할 수 있다.
- 여러 목적이나 상세 본문을 메시지에 넣지 않는다.
- `Co-Authored-By`를 자동 추가하지 않는다.

예:

```text
docs: TimeBack 요구사항 검증 질문 추가
feat: FR-1 사용 이벤트 세션화 구현
fix: 자정 경계 세션 중복 집계 수정
tidy: Context 계산 클래스를 도메인 모듈로 이동
chore: TimeBack Hook 검증 설정 추가
```

## 타입

| 타입 | 용도 |
|---|---|
| `tidy` | 기능 변화 없는 정리, 파일 이동·이름변경 |
| `feat` | 새 사용자/시스템 동작 |
| `fix` | 확정 동작의 결함 수정 |
| `test` | test-only 변경 |
| `perf` | 측정 근거가 있는 성능 개선 |
| `docs` | 요구·설계·상태·설명 문서 |
| `style` | 의미 변화 없는 서식 |
| `chore` | 빌드, Hook, Skill, CI, 개발 설정 |

관련 테스트는 `feat`/`fix`를 검증하는 일부로 같은 커밋에 포함할 수 있다.

## 분리 기준

- 정리와 동작 변경을 분리한다.
- 서로 다른 FR 또는 독립적인 rollback 단위를 분리한다.
- 제품 atomic commit과 AI-DLC 단계 체크포인트를 분리한다.
- generated 파일은 원인이 되는 설정 변경과 함께 둘 수 있지만 수동 변경량 계산에서 제외한다.
- 큰 변경은 줄 수 자체로 금지하지 않고 독립 검증·rollback 가능성으로 분리한다.

## 이동과 이름변경

- 옛 경로 삭제와 새 경로 추가를 같은 `tidy` 커밋에 둔다.
- 이동과 내용 변경이 크면 이동을 먼저 commit하고 내용 변경을 다음 commit으로 분리한다.
- 승인 후 옛/새 경로를 명시적으로 staging한다.
- `git diff --cached --name-status -M`에서 rename 감지를 확인한다.

## 금지사항

- 승인 전 `git add` 또는 `git commit`
- `git add .`, blanket `git add -A`, 무관한 파일 staging
- 실패한 필수 검증을 숨기고 commit
- 비밀, 실제 사용자 사용 데이터, 로컬 DB commit
- `--no-verify`, force push, hard reset, destructive clean
- 명시 요청 없는 amend, push, branch 삭제
- 단계 승인 없이 AI-DLC 완료 checkpoint 생성

## commit 전 확인

- [ ] 목적이 하나인가?
- [ ] 대상 파일이 정확히 승인됐는가?
- [ ] 비밀·민감정보·생성물이 제외됐는가?
- [ ] 관련 검증이 통과했는가?
- [ ] staged diff에 무관한 변경이 없는가?
- [ ] 이동은 `R`로 확인됐는가?
- [ ] 메시지가 실제 staged diff와 일치하는가?
- [ ] AI-DLC checkpoint라면 Gate 2, state, audit, 검증이 모두 완료됐는가?
