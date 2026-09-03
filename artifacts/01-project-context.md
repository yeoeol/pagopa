# Project Test Context

- 상태: `main` 기준 초기 베이스라인
- 확인 날짜: 2026-09-03
- 기준 커밋: `7848578554b0f914f23a7715d82e1282e2348cf3`
- 실제 테스트 대상: 확인 필요

## 컨텍스트 제외

사용자 지시에 따라 `ai/**`, `.claude/**`, `CLAUDE.md`, `GEMINI.md`는 읽거나 적용하지 않는다.

## 기술 스택

- Java 25 toolchain
- Spring Boot 4.0.4
- Gradle과 JUnit Platform
- `spring-boot-starter-test`, AssertJ, Mockito
- Spring Boot Testcontainers와 MySQL Testcontainers
- H2 test runtime

근거: `build.gradle`, `src/test/**`

## 관찰된 기존 테스트 스타일

- JUnit Jupiter `@Test`, `@ParameterizedTest`를 사용한다.
- assertion은 주로 AssertJ를 사용하고 Mockito의 BDD stubbing과 verify도 사용한다.
- 단위 테스트는 의존성을 직접 mock하고 대상 객체를 직접 생성하는 사례가 있다.
- 저장소·동시성 테스트는 `@SpringBootTest`와 MySQL Testcontainers를 사용한다.
- `support/fixture`의 Object Mother 형태 Fixture를 재사용한다.
- 테스트 메서드는 영어 `method_condition_result` 형태가 주류지만 동시성 테스트에는 한글 이름도 있다.
- Given/When/Then 주석은 일부 파일에만 있으므로 모든 파일에 일괄 강제하지 않는다.

## 현재 가독성·회귀 위험 관찰

- `StockConcurrencyTest`는 약 415줄이며 준비·실행·집계 코드가 여러 테스트에서 반복된다.
- 큰 참여자 수와 thread 수, 긴 timeout은 정확성 검증과 부하 성격을 섞을 위험이 있다.
- 일부 동시성 케이스는 완료 여부와 예상한 BusinessException 외의 결과를 더 엄격히 연결할 여지가 있다.
- 기존 테스트와 Fixture에는 들여쓰기와 생성자 접근 제한의 불일치가 있다.

이 관찰은 자동 리팩터링 승인이 아니다. A 방식에서는 사용자가 지정하고 승인한 대상 테스트 파일
내부의 변경만 허용한다.

## 실제 실행에서 추가할 내용

- 대상 운영 코드와 observable behavior
- 가장 가까운 기존 테스트의 구체적 스타일
- 재사용 가능한 Fixture와 테스트 설정
- 공식 문서 URL, 적용 버전, 확인 날짜
- 기존 사용자 변경과 충돌 가능성
- 테스트 환경과 외부 의존성
