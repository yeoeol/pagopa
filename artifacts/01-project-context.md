# Project Test Context

- 대상: `SearchHistoryService.saveHistory(Long userId, String sessionId, String keyword)`의 MySQL 동시성 통합 테스트
- 기준 브랜치/커밋: `test/search-history-concurrency` / `bef0348065ff8049e768f96d4bf983965bc1c762`
- 확인 날짜: 2026-09-03
- 기존 사용자 변경: 분석 시작 시 `git status --short`는 `M artifacts/00-request.md` 한 건이다. 이 변경은 현재 요청을 기록한 하네스 산출물이며 운영 코드와 테스트 코드의 기존 변경은 없다.
- 컨텍스트 제외: 지침에 따라 `ai/**`, `.claude/**`, `CLAUDE.md`, `GEMINI.md`는 읽거나 적용하지 않았다.
- 실행 여부: 승인 전 단계이므로 컴파일과 테스트를 실행하지 않았다.

## 기술 스택

- Gradle Wrapper 9.4.0, Java 25 toolchain, Spring Boot 4.0.4, JUnit Platform을 사용한다. 근거: `gradle/wrapper/gradle-wrapper.properties`, `build.gradle`.
- `spring-boot-starter-test`가 JUnit Jupiter, AssertJ, Mockito 계열 테스트 API를 제공한다. 개별 버전은 빌드 파일에 고정하지 않고 Spring Boot 의존성 관리에 맡긴다. 근거: `build.gradle`, 기존 테스트의 `org.junit.jupiter.*`, AssertJ 및 Mockito import.
- DB 런타임 드라이버는 MySQL Connector/J이고 테스트 런타임에는 H2도 존재한다. 통합 테스트용으로 `spring-boot-testcontainers`, `testcontainers-junit-jupiter`, `testcontainers-mysql`이 명시되어 있다. 근거: `build.gradle`.
- 기존 `TestcontainersConfig`는 `mysql:8.0.36` 이미지를 `MySQLContainer` Bean으로 만들고 `@ServiceConnection`으로 JDBC 연결 정보를 공급한다. 대상 테스트가 `@SpringBootTest`와 `@Import(TestcontainersConfig.class)`를 사용하면 실제 MySQL을 대상으로 할 수 있다. 근거: `src/test/java/com/commerce/pagopa/support/testcontainers/TestcontainersConfig.java`, `src/test/java/com/commerce/pagopa/order/application/StockConcurrencyTest.java`.
- 저장소에는 추적된 `application.yml`/`application.properties`, `schema.sql`, Flyway/Liquibase 마이그레이션이 없고 실제 파일도 확인되지 않았다. MySQL 같은 비임베디드 DB에서 Spring Boot의 기본 Hibernate `ddl-auto`는 `none`이므로, 컨테이너 스키마 생성 설정이 외부 환경에만 의존할 가능성이 있다.

## 관찰 가능한 동작과 불변식

- `keyword`가 `null`, 빈 문자열 또는 공백뿐이면 저장하지 않는다. 현재 정규화는 `String.trim()`이므로 **앞뒤 공백만** 제거한다. `" keyword "`와 `"keyword"`는 같아지지만 `"key word"`와 `"keyword"`는 같아지지 않는다. 근거: `src/main/java/com/commerce/pagopa/searchhistory/application/SearchHistoryService.java`.
- `userId != null`이면 로그인 회원 분기가 우선하며 `sessionId`는 무시된다. 사용자가 없으면 `BusinessException(ErrorCode.USER_NOT_FOUND)`를 던지고 저장하지 않는다. `userId == null`이고 `sessionId`에 텍스트가 있으면 세션 분기로 저장하며, 둘 다 유효하지 않으면 no-op이다. 근거: `SearchHistoryService.saveHistory`, `UserRepository.existsById`, `ErrorCode.USER_NOT_FOUND`.
- `search_history` 테이블에는 `(user_id, keyword)`와 `(session_id, keyword)` 두 복합 유니크 제약이 선언되어 있다. 로그인 행은 전자, 세션 행은 후자가 동일 주체·동일 검색어 중복을 식별한다. 근거: `src/main/java/com/commerce/pagopa/searchhistory/domain/model/SearchHistory.java`.
- 두 저장 분기는 각각 MySQL 네이티브 `INSERT ... ON DUPLICATE KEY UPDATE last_searched_at = :lastSearchedAt`를 실행한다. 중복 키면 새 행을 추가하지 않고 기존 행의 검색 시각만 갱신하는 것이 의도된 불변식이다. 근거: `src/main/java/com/commerce/pagopa/searchhistory/infrastructure/persistence/SearchHistoryJpaRepository.java`.
- 동일 사용자/세션의 서로 다른 keyword는 각각 한 행이어야 한다. 같은 사용자/세션에 같은 normalized keyword를 동시에 보내면 최종 한 행이어야 하며 모든 호출이 정상 완료되어야 한다.
- `keyword` 길이는 100, `session_id` 길이는 255이다. 이번 요청의 직접 경계값은 길이보다 공백 정규화와 무효 입력의 no-op 계약이다. 근거: `SearchHistory.java`.
- `saveHistory`는 호출마다 `@Transactional` 경계를 갖는다. 작업 스레드별 별도 트랜잭션/DB 연결로 native upsert가 수행되므로 실제 경합을 검증할 수 있다. 테스트 메서드의 `@Transactional` 롤백이 작업 스레드의 커밋까지 정리한다고 가정하면 안 된다.
- 사용자/세션별 repository 조회 또는 `SearchHistoryService.getHistories`로 최종 행 수와 keyword를 관찰할 수 있다. “예상하지 못한 예외, 미완료 작업, timeout 없음”은 행 수만으로 부족하므로 모든 작업의 완료 여부, 작업별 예외, 제한 시간과 executor 종료를 별도로 assertion해야 한다.

## 기존 테스트 스타일

- 검색 기록 패키지에는 기존 테스트가 없다. 새 대상 파일 후보는 `src/test/java/com/commerce/pagopa/searchhistory/application/SearchHistoryServiceConcurrencyTest.java`이며, 이 파일 하나만 만들면 A 방식 경계를 지킬 수 있다.
- 가장 가까운 동시성 통합 테스트 `StockConcurrencyTest`는 `@SpringBootTest`, `@Import(TestcontainersConfig.class)`, AssertJ, 고정 스레드 풀, `CyclicBarrier`, `CountDownLatch`, 제한 시간 대기, 결과 카운터를 사용하며 테스트명은 한글 동작 문장이다. 근거: `src/test/java/com/commerce/pagopa/order/application/StockConcurrencyTest.java`.
- `StockConcurrencyTest`의 N=50/200/1000과 Hikari 풀 50은 주문 부하 성격도 섞인 사례다. 검색 기록 불변식 검증에는 연결 풀을 고갈시키지 않는 작은 동시 요청 수도 충분하므로 같은 규모를 답습할 근거는 없다.
- 기존 동시성 테스트 일부는 `finished`를 계산만 하고 assertion하지 않거나 특정 예외를 카운트하지 않은 채 삼키는 불일치가 있다. 이번 요구는 `finished == true`, 예상하지 못한 예외 0, 성공/완료 수 일치, executor 종료를 모두 엄격히 검증해야 한다. 이는 기존 파일 리팩터링 승인이 아니다.
- `OrderRepositoryTest`와 `ProductRepositoryTest`도 `@SpringBootTest`/`@Import(TestcontainersConfig.class)`/AssertJ/Object Mother Fixture를 사용한다. 다만 클래스 수준 `@Transactional`은 단일 테스트 스레드 롤백용이므로 이번 작업 스레드 테스트에는 그대로 적용하기 부적절하다.
- 일반 저장소 테스트의 영어 `method_condition_result` 이름과 동시성 테스트의 한글 이름이 공존하므로 어느 하나를 저장소 전체의 유일한 규칙이라고 단정할 수 없다.

## 재사용 가능한 Fixture와 설정

- `TestcontainersConfig`: MySQL 8.0.36 컨테이너와 서비스 연결을 그대로 재사용한다. 새 컨테이너 유틸리티나 공용 베이스 클래스는 필요 없다.
- `UserFixture.aUser(String suffix)`와 `UserRepository.save`: 로그인 분기에 필요한 실제 사용자 한 명을 만들 수 있다. 검색 기록 저장에는 역할이 필요하지 않다. 이메일/provider 식별자가 유니크하므로 테스트별 고유 suffix가 필요하다.
- 세션 분기는 사용자 Fixture 없이 테스트별 고유 `sessionId`로 격리할 수 있다.
- 검색 기록 Fixture는 없으며 필요하지 않다. 서비스가 직접 행을 생성해야 통합 경로를 검증한다.
- repository의 사용자/세션별 삭제 또는 고유 식별자를 사용해 데이터를 격리할 수 있다. 작업 스레드 커밋 때문에 테스트 트랜잭션 자동 롤백만 의존하지 않는다.
- 대상 파일 내부의 작은 동시 실행 helper는 A 방식 허용 범위지만 공용 Fixture/공용 테스트 유틸 추출은 범위 밖이다.

## 공식 근거

- MySQL 8.0 `INSERT ... ON DUPLICATE KEY UPDATE`: UNIQUE 또는 PRIMARY KEY 중복이면 기존 행을 UPDATE한다. <https://dev.mysql.com/doc/refman/8.0/en/insert-on-duplicate.html> (MySQL 8.0, 확인 2026-09-03)
- MySQL 8.0 InnoDB 잠금: upsert가 중복 유니크 키를 만나면 갱신 대상에 배타적 잠금을 설정한다. <https://dev.mysql.com/doc/refman/8.0/en/innodb-locks-set.html> (MySQL 8.0, 확인 2026-09-03)
- Spring Boot Testcontainers: `@ServiceConnection`은 컨테이너 연결 정보를 자동 구성하고 일반 연결 속성보다 우선하며 `MySQLContainer`를 지원한다. <https://docs.spring.io/spring-boot/reference/testing/testcontainers.html> (Spring Boot 4 계열, 확인 2026-09-03)
- Spring Boot DB 초기화: 비임베디드 DB는 명시 설정/스키마 관리자가 없으면 Hibernate `ddl-auto`가 기본 `none`이다. <https://docs.spring.io/spring-boot/how-to/data-initialization.html> (Spring Boot 4 계열, 확인 2026-09-03)
- Spring TestContext 트랜잭션: 테스트 관리 트랜잭션은 현재 스레드에 결합되므로 별도 스레드 작업과 preemptive timeout/롤백 조합에 주의해야 한다. <https://docs.spring.io/spring-framework/reference/testing/testcontext-framework/tx.html> (Spring Framework 7 계열, 확인 2026-09-03)
- JUnit timeout: `@Timeout`은 테스트/클래스 제한 시간을 제공하며 `assertTimeoutPreemptively`는 별도 스레드와 ThreadLocal 부작용 가능성이 있다. <https://docs.junit.org/6.0.2/api/org.junit.jupiter.api/org/junit/jupiter/api/Timeout.html>, <https://docs.junit.org/6.0.1/api/org.junit.jupiter.api/org/junit/jupiter/api/Assertions.html> (JUnit 6 API, 확인 2026-09-03; 정확한 관리 버전은 승인 후 컴파일로 확인)

## 위험과 확인 필요

1. **스키마 생성 재현성**: 추적된 초기화 설정이 없고 MySQL의 기본 `ddl-auto`는 `none`이다. A 방식만 지키려면 새 대상 테스트 파일의 `@SpringBootTest(properties = ...)`로 테스트 스키마 생성을 명시하는 선택이 가장 국소적이며, 생략하면 환경별 `table not found` 위험이 있다.
2. **“공백 제거” 범위**: 운영 코드는 `trim()`만 한다. 앞뒤 공백 변형을 같은 keyword로 보는 테스트는 현 계약과 맞지만 내부 공백까지 제거하는 요구는 운영 코드 변경이 필요해 A 방식 범위를 벗어난다.
3. **동시성 결과 수집**: 예외를 누락하거나 삼키지 않고 정상 완료 수, throwable 목록, latch 완료, timeout, executor 종료를 함께 검증해야 한다.
4. **트랜잭션/정리**: 작업 스레드의 서비스 트랜잭션은 실제 커밋된다. 클래스 `@Transactional` 자동 롤백 대신 고유 user/session과 명시적 정리 또는 컨테이너 격리를 사용해야 한다.
5. **풀 크기와 하네스 교착**: 참여자 수가 실행 가능한 스레드/DB 풀보다 크면 DB 동작이 아니라 테스트 하네스가 멈출 수 있다. 작은 고정 참여자 수, 일치하는 executor/pool, 유한 대기가 필요하다.
6. **두 유니크 인덱스**: 현재 두 insert 분기는 한 식별자만 채우고 다른 식별자는 null이라 요구 케이스에서는 의도한 복합 키 하나가 충돌한다. userId와 sessionId를 동시에 주는 케이스는 서비스의 user 우선 분기를 별도 기대해야 한다.
7. **예외 분류 확정**: 사용자 피드백에 따라 예외 관점은 기존 행의 동시 upsert update가 예상하지 못한 예외 없이 완료되는 독립 케이스로 둔다. `USER_NOT_FOUND` 등 다른 입력·애그리거트 계약은 이번 범위에서 제외한다.
8. **미실행 확인 사항**: Docker 가용성, 컨테이너 기동, 실제 DDL/유니크 인덱스 생성, 관리되는 정확한 JUnit/Testcontainers 버전과 테스트 통과 여부는 코드 승인 후 검증한다.
