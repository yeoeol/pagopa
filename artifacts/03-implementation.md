# Test Implementation Record

- 상태: 구현 완료, 독립 검증 대기
- 승인된 테스트 목록: N-01, E-01, B-01, R-01, R-02
- 구현 기준 커밋: `bef0348065ff8049e768f96d4bf983965bc1c762`
- 구현 후 커밋: 생성하지 않음

## 변경 파일

| 파일 | 테스트 케이스 ID | 변경 이유 | A 방식 범위 준수 |
| --- | --- | --- | --- |
| `src/test/java/com/commerce/pagopa/searchhistory/application/SearchHistoryServiceConcurrencyTest.java` | N-01, E-01, B-01, R-01, R-02 | 실제 MySQL에서 `saveHistory`의 동시 upsert 불변식과 완료·예외·timeout 조건 검증 | 신규 대상 테스트 파일과 파일 내부 helper만 추가 |
| `artifacts/03-implementation.md` | 전체 | 승인 케이스와 구현 결정의 매핑 기록 | 하네스가 요구한 구현 산출물 |

## 구현 결정

- 사용한 기존 테스트 패턴: `StockConcurrencyTest`의 AssertJ, 고정 thread pool, `CyclicBarrier`, `CountDownLatch` 구성을 따랐다. 참여자 수는 승인된 8명으로 제한했다.
- 테스트 컨텍스트: 전체 애플리케이션 대신 JPA slice를 로드하는 `@DataJpaTest`에 대상 클래스 한정 `spring.jpa.hibernate.ddl-auto=create-drop`을 적용했다. `SearchHistoryService`, 기존 `TestcontainersConfig`와 repository 구현에 필요한 `QueryDSLConfig`를 명시적으로 import하고, `@AutoConfigureTestDatabase(replace = NONE)` 및 DB metadata assertion으로 실제 MySQL 사용을 보장한다.
- 트랜잭션 경계: `@DataJpaTest`의 기본 test-managed transaction을 `@Transactional(propagation = NOT_SUPPORTED)`로 비활성화했다. 테스트 스레드에서 먼저 저장한 회원과 E-01 기존 행이 커밋되어 worker thread의 `SearchHistoryService` 트랜잭션에서 보이게 한다.
- 분류와 명명: N-01/E-01/B-01/R-01/R-02를 각각 `normal`/`exception`/`boundary`/`regression` tag로 표시하고 승인된 영어 `snake_case` 메서드명을 사용했다.
- 추가한 대상 파일 내부 helper: `execute_concurrently`가 8-thread executor, 5초 gate, 15초 completion latch와 5초 executor 종료 제한을 관리한다. `ConcurrencyResult`에 성공 수, 예상 실패 수, 예상 밖 실패 목록, 미완료 수와 종료 여부를 보존하며 `assert_successful_execution`이 전체 outcome 합계와 예외 0건을 검증한다.
- E-01 갱신 검증: guest 행을 `2000-01-01T00:00:00Z`로 먼저 저장한 뒤 같은 키로 동시 upsert하여 행 1건, 기존 id 유지, `lastSearchedAt` 증가를 확인한다.
- 데이터 격리: 테스트별 UUID 기반 session/user suffix를 사용하고 `finally`에서 생성한 검색 기록을 명시적으로 삭제한다. R-01의 fixture 회원은 저장소에 회원 삭제 API가 없으므로 고유 suffix로 격리한다.
- 선택하지 않은 대안과 이유: 공용 동시성 helper, 공용 fixture 변경, rollback 기반 테스트 트랜잭션, 대규모 thread 수는 승인된 A 방식과 테스트 목적을 벗어나 추가하지 않았다.
- 운영 코드 결함 또는 확인 필요: 구현자는 테스트를 실행하지 않았다. 검증자는 Docker/Testcontainers 기동, 컴파일, MySQL DDL 및 5개 테스트의 실제 통과 여부를 독립적으로 확인해야 한다.

## 검증 피드백 반영

- 1차 독립 검증에서 Docker 기동 후 `compileTestJava`는 성공했으나, 기존 `@SpringBootTest`가 대상과 무관한 전체 애플리케이션 설정의 `${jwt.secret}` placeholder를 요구해 테스트 context 생성 전에 실패했다.
- A 방식 범위 안에서 전체 context를 `@DataJpaTest` slice로 축소하고 실제 서비스와 기존 MySQL Testcontainers 설정만 포함하도록 조정했다. 운영 설정 값을 테스트 파일에서 임의로 채우지 않았다.
- slice의 embedded database 자동 대체를 금지했고 기본 테스트 트랜잭션도 비활성화하여, 실제 MySQL 및 worker별 서비스 트랜잭션이라는 승인된 검증 조건을 유지했다.
- 조정 후 컴파일과 테스트 실행 결과는 다음 독립 검증에서 확인해야 한다.
- 2차 독립 검증에서는 실제 MySQL 컨테이너와 Hibernate 초기화까지 성공했으나, JPA slice가 함께 생성한 `OrderRepositoryCustomImpl`에 필요한 `JPAQueryFactory` bean이 없어 context가 중단됐다.
- 해당 bean 하나를 정의하는 기존 운영 `QueryDSLConfig`를 테스트 import에 추가했다. 운영 코드나 공용 테스트 설정을 복제·수정하지 않았으며, 조정 후 결과는 최종 독립 검증에서 확인해야 한다.
- 사용자가 추가 승인한 권장안에 따라 R-01의 fixture suffix에서 UUID 부분을 hyphen 없는 12자리 hex로 축약했다. `same-user-keyword` 17자 + 구분자 1자 + UUID 12자로 suffix는 30자이고, `UserFixture`의 `nick-` 접두사까지 포함한 `User.name`은 35자로 50자 제약 안에 들어간다. 기존 36자 UUID를 그대로 쓰면 name이 59자가 되어 제약을 초과한다.

## 범위 확인

- [x] 승인된 `src/test/**` 파일만 변경했다.
- [x] 운영 코드와 빌드 파일을 변경하지 않았다.
- [x] 공용 Fixture와 테스트 유틸리티를 변경하지 않았다.
- [x] assertion을 약화하거나 예외를 삼키지 않았다.
- [x] git add/commit/push를 실행하지 않았다.
