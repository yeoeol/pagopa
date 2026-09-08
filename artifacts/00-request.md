# Test Generation Request

- 상태: 구현 및 독립 검증 완료 — LIMITED PASS, 커밋 전 사용자 검토 대기
- 요청 일시: 2026-09-03
- 요청자: 현재 사용자
- 기준 브랜치: `test/search-history-concurrency`
- 기준 커밋: `bef0348065ff8049e768f96d4bf983965bc1c762`
- 테스트 대상: `SearchHistoryService.saveHistory`
- 보호할 동작 또는 재현할 결함: 동시 저장 시 사용자 또는 세션 기준의 동일 검색어가 중복 저장되는 회귀 방지
- 원하는 테스트 수준: 기존 Testcontainers 설정과 실제 MySQL을 사용하는 동시성 통합 테스트
- 완료 기준:
  - 동일 `userId`와 동일 `keyword`의 동시 요청 후 검색 기록 1건
  - 동일 `sessionId`와 동일 `keyword`의 동시 요청 후 검색 기록 1건
  - 서로 다른 `keyword`의 동시 요청은 각각 저장
  - 공백 제거 후 같은 값인 `keyword`의 동시 요청은 중복 없이 1건
  - 이미 존재하는 동일 주체·동일 keyword 행은 새 행으로 교체되지 않고 `lastSearchedAt`이 갱신
  - 예상하지 못한 예외, 미완료 작업, timeout 없음
  - 정상·예외·경계값·회귀 관점으로 테스트를 분류
  - 테스트 메서드명은 영어 `snake_case`로 일관되게 작성
- 수정 허용 파일: 승인 전에는 `artifacts/00-request.md`, `artifacts/01-project-context.md`, `artifacts/02-test-cases.md`; 승인 후에는 A 방식으로 확정된 대상 테스트 파일 내부만
- 수정 금지 파일: 운영 코드, `build.gradle`, 공용 Fixture, 공용 테스트 유틸리티, 대상 외 테스트 파일
- 기존 사용자 변경: `git status --short` 기준 없음

## 확인 필요

- 대상 테스트 파일의 기존 유무와 가장 가까운 테스트 스타일
- 현재 스키마와 저장 로직이 MySQL 동시 요청에서 요구 불변식을 보장하는 방식
- 예외 분류는 기존 행의 동시 update가 예외 없이 완료되는 독립 케이스로 구성하고 `USER_NOT_FOUND` 등 다른 계약은 제외
- Docker/Testcontainers 실행 가능 여부는 구현 승인 후 검증 단계에서 확인
