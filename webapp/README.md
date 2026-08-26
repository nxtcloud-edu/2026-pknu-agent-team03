# TimeBack 웹 대시보드 데모

PR #8에서 추가된 웹 화면은 Java/Spring 통합 서버의 선택적 합성 데이터 데모로 편입됐다.

```bash
./gradlew :server:bootRun --args='--spring.profiles.active=demo'
```

실행 후 `http://localhost:8080/demo/index.html`을 연다. 데모 API는 `/demo-api/**`이며 실제
CT-05 백업 API `/api/**`, Android Room 데이터, 실제 기기 삭제 상태를 대신하지 않는다. 기본
프로필과 Docker 실행에서는 데모 API가 비활성화된다.
