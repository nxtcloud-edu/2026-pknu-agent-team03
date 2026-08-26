# TimeBack 웹 대시보드 데모

PR #8에서 추가된 웹 화면은 Java/Spring 통합 서버의 선택적 합성 데이터 대시보드로 편입됐다.

```bash
./gradlew :server:bootRun --args='--spring.profiles.active=demo'
```

실행 후 `http://localhost:8080/demo/index.html`을 연다. 데모 API는 `/demo-api/**`이며 실제
CT-05 백업 API `/api/**`, Android Room 데이터, 실제 기기 삭제 상태를 대신하지 않는다. 기본
프로필과 Docker 실행에서는 데모 API가 비활성화된다.

대시보드는 다음을 포함한다.

- 1120px 데스크톱 그리드와 390px 모바일 반응형 화면
- 페이지별 loading·error·empty 상태와 API 재시도
- 키보드로 이동할 수 있는 탭·Timeline·수정 dialog
- 목표 추가·시간 기록, Timeline 분류 수정, 합성 동기화·보관 기간
- 서버 합성 데이터 삭제 뒤 `/demo-api/reset`을 통한 기본 데이터 복원
- 외부 CDN 없이 실행 JAR 하나로 제공되는 정적 화면
