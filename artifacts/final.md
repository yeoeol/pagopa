# SearchHistoryService 동시성 테스트 결과

- 상태: 구현 및 독립 검증 완료
- 판정: **LIMITED PASS**
- 기준 브랜치/커밋: `test/search-history-concurrency` / `bef0348065ff8049e768f96d4bf983965bc1c762`
- 방식: A — 신규 대상 테스트 파일 내부의 외과적 변경
- 검증 일시: 2026-09-04 (Asia/Seoul)
- 커밋 상태: 사용자 검토 전, `git add`/`commit`/`push` 미실행

## 최종 테스트 코드

- 파일: `src/test/java/com/commerce/pagopa/searchhistory/application/SearchHistoryServiceConcurrencyTest.java`
- N-01 정상: 서로 다른 keyword 8개를 동시에 저장하면 8건 모두 유지
- E-01 예외: 기존 행에 대한 동시 upsert 8건이 예외 없이 완료되고 기존 ID 유지 및 `lastSearchedAt` 증가
- B-01 경계값: 앞뒤 공백 변형을 동시에 저장해도 정규화된 keyword 1건
- R-01 회귀: 동일 userId/keyword의 최초 insert 경합 후 1건
- R-02 회귀: 동일 sessionId/keyword의 최초 insert 경합 후 1건
- 테스트와 lifecycle 메서드명은 영어 `snake_case`로 통일

## 구현 결정

- 기존 `TestcontainersConfig`의 MySQL 8.0.36과 Hibernate `create-drop` 스키마를 사용한다.
- `@DataJpaTest`, `@AutoConfigureTestDatabase(replace = NONE)`, 기존 `QueryDSLConfig`와 실제 `SearchHistoryService` import로 대상 계층만 통합한다.
- 전체 `@SpringBootTest`는 대상과 무관한 `${jwt.secret}`과 외부 애플리케이션 설정에 의해 컨텍스트가 실패해 JPA slice로 축소했다. 실제 서비스 transaction, repository, native upsert와 MySQL은 mock하지 않는다.
- JPA slice의 기본 테스트 transaction은 비활성화해 사전 데이터와 worker별 서비스 transaction을 실제 commit 경계로 분리한다.
- 8-thread executor, `CyclicBarrier`, latch와 유한 timeout을 사용하며 모든 outcome, 예상/예상 밖 예외, 미완료 작업과 executor 종료를 assertion한다.
- 회원 fixture suffix는 12자리 UUID hex를 사용해 `User.name` 50자 제한을 만족한다.

## 검증 결과

| 범위 | 결과 | 근거 |
| --- | --- | --- |
| 대상 테스트 최초 실행 | PASS — 5/5 | 실제 MySQL 8.0.36, exit 0 |
| 대상 강제 반복 1 | PASS — 5/5 | `--rerun-tasks`, exit 0 |
| 대상 강제 반복 2 | PASS — 5/5 | `--rerun-tasks`, exit 0 |
| 대상 합계 | PASS — 15/15 | flaky, timeout, 미완료, 예외, executor 종료 실패 없음 |
| 관련 MySQL repository 테스트 | FAIL — 10/10 context 실패 | 기존 full-context `${jwt.secret}` 미설정, 대상 변경과 무관 |
| 전체 테스트 | FAIL — 36개 중 13 PASS/23 FAIL | 대상 5건과 auth 8건 PASS; 기존 full-context 테스트 23건은 같은 설정 문제 |
| 변경 범위 및 whitespace | PASS | 운영 코드·빌드·공용 Fixture 변경 없음, `git diff --check` exit 0 |

전체 suite가 녹색이 아니므로 최종 판정은 PASS가 아니라 LIMITED PASS다. 상세 명령, 시간과 XML 근거는 `artifacts/04-verification.md`에 기록했다.

## 회귀 위험과 미검증 영역

- 관련 repository 테스트와 기존 `StockConcurrencyTest` 본문은 `${jwt.secret}` 미설정으로 실행되지 않았다.
- 운영 migration은 저장소에 없어 검증하지 않았으며, Hibernate가 생성한 MySQL 스키마에서 native upsert와 유니크 제약을 검증했다.
- R-01이 만든 회원 fixture는 고유 suffix로 격리되고 컨테이너 종료 시 제거되지만 테스트 메서드 종료 시 개별 삭제하지 않는다.
- 승인 설계의 `@SpringBootTest`는 검증 피드백에 따라 실제 MySQL JPA slice로 조정됐으며 변경 이유는 `artifacts/03-implementation.md`에 보존했다.

## 변경 파일

- `src/test/java/com/commerce/pagopa/searchhistory/application/SearchHistoryServiceConcurrencyTest.java`
- `artifacts/00-request.md`
- `artifacts/01-project-context.md`
- `artifacts/02-test-cases.md`
- `artifacts/03-implementation.md`
- `artifacts/04-verification.md`
- `artifacts/final.md`

## 커밋 전 사용자 확인

- [ ] 변경 파일과 diff를 직접 확인했다.
- [x] 승인한 5개 테스트가 구현됐다.
- [x] 대상 테스트를 실제 MySQL에서 3회 실행했다.
- [ ] LIMITED PASS 사유와 미검증 영역을 확인했다.
- [ ] 커밋을 별도로 승인했다.
