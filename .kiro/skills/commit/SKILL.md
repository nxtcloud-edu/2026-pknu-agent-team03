---
name: commit
description: TimeBack 변경사항을 검토해 원자적 Git 커밋으로 분리하고, 검증·사용자 승인·명시적 staging·한글 메시지·커밋 결과 확인을 수행할 때 사용한다. "커밋해줘", "버전 관리해줘", "/commit" 요청에 적용한다.
---

<!--
목적: TimeBack 변경사항을 검증 가능하고 되돌리기 쉬운 원자적 Git 이력으로 남긴다.
역할: 변경과 비밀 유출 위험을 분석하고, 커밋 후보 분리·검증·승인된 staging·메시지 선택·결과 확인을 수행한다.
사용 흐름: 저장소 현황 확인 → 안전성 검사 → 목적별 변경 분리 → 관련 검증 → staging 승인 → 명시적 staging → 메시지 승인 → commit 및 상태 확인.
사용 시점: 사용자가 커밋, 버전 관리, `/commit`을 명시적으로 요청했을 때만 적용한다.
주의: 자동 commit·push·amend·force·hook 우회를 하지 않으며 `git add .` 또는 `git add -A`를 사용하지 않는다.
-->

# TimeBack Git 커밋 Skill

## 원칙

- 커밋 하나는 한 가지 목적만 가진다.
- 사용자 승인 전에는 staging하거나 commit하지 않는다.
- `git add .`, `git add -A`로 전체 변경을 일괄 staging하지 않는다.
- 제품 코드 변경은 관련 테스트와 함께 검증 가능한 원자 단위로 만든다.
- AI-DLC 단계 체크포인트는 Gate 2 승인, 상태 갱신, audit 기록, 검증 완료 뒤 별도 `docs` 커밋으로 남긴다.
- push, amend, hook 우회, force 작업은 별도 명시 요청 없이는 수행하지 않는다.

## STEP 1: 저장소와 현황 확인

1. `git rev-parse --show-toplevel`로 현재 루트가 Git 저장소인지 확인한다.
2. 저장소가 아니면 현재 상태를 알리고 `git init` 실행 승인을 요청한 뒤 멈춘다.
3. 저장소라면 `git status --short --branch`, staged/unstaged diff, 최근 log를 읽기 전용으로 확인한다.
4. 변경이 없으면 종료한다.

## STEP 2: 안전성 검사

- `.env`, key, token, 인증서, 로컬 DB, 사용자 사용 기록 등 비밀·민감 데이터가 포함됐는지 확인한다.
- generated/build/IDE 파일이 `.gitignore` 대상인지 확인한다.
- append-only 기록이 덮어쓰기 되지 않았는지 확인한다.
- 파일 이동은 삭제와 추가를 같은 커밋에 넣어 rename 이력을 보존한다.

## STEP 3: 변경 분류와 분리

`rules.md`를 기준으로 파일별 목적과 추천 타입을 표로 제시한다.

- 순서: `tidy → feat/fix → test → perf → docs/style/chore`
- 기능을 증명하는 관련 테스트는 같은 `feat`/`fix` 커밋에 포함할 수 있다.
- test-only 변경만 `test`로 분리한다.
- 큰 변경은 줄 수로 금지하지 않고 독립 검증·rollback 가능성으로 분리한다.

## STEP 4: 검증

각 커밋 후보에 가장 좁고 관련성 높은 검증을 실행한다.

- 문서/Hook: JSON 파싱, PowerShell 모의 입력, frontmatter 검사
- Android 도메인: 관련 단위 테스트
- Android 설정/통합: 필요한 compile, lint, targeted test

실행할 수 없는 검증은 이유와 대체 확인 방법을 명시한다. 실패한 변경은 원칙적으로 commit하지 않는다.

## STEP 5: staging 승인

1. 타입, 목적, 정확한 파일 목록, 검증 결과를 제시한다.
2. `1) 수정 요청`, `2) 이 파일들을 staging` 두 선택지로 승인을 받는다.
3. 승인 후 `git add -- <paths>`로 파일을 정확히 나열해 staging한다.
4. 이동은 옛/새 경로를 함께 staging하고 `git diff --cached --name-status -M`에서 `R`인지 확인한다.
5. `git diff --cached --check`, stat, name-status, staged diff를 재검토한다.

## STEP 6: 메시지와 commit 승인

1. staged diff에 맞는 한글 한 줄 `타입: 설명` 메시지 2~3개를 제안한다.
2. 사용자가 메시지를 선택하거나 수정하게 한다.
3. 선택된 메시지로 commit한다.
4. `git show --stat --oneline -1`과 `git status --short`로 결과를 확인한다.

## STEP 7: AI-DLC 체크포인트

다음 조건을 모두 만족할 때만 단계 checkpoint를 제안한다.

- Gate 2 답변이 audit에 존재한다.
- state와 실제 산출물이 일치한다.
- 필수 검증 결과가 기록되어 있다.
- 제품 변경은 먼저 atomic commit으로 정리됐다.
- checkpoint 파일과 메시지를 사용자가 승인했다.

메시지는 `docs: 요구사항 분석 결과 승인`처럼 한 단계와 한 목적을 나타낸다. commit hash를 audit 또는 상태 기록에 연결한다.

## 완료 보고

- commit hash와 메시지
- commit별 파일과 검증 결과
- 남은 unstaged/staged 변경
- push 여부(기본값: 수행하지 않음)

세부 타입과 금지사항은 `rules.md`를 따르며, 저장소 초기화는 별도 사용자 승인이 있어야 한다.
