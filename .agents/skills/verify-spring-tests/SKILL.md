---
name: verify-spring-tests
description: >
  구현된 Spring 테스트를 독립적으로 실행·검토해 통과 여부, 가독성, 불안정성과 회귀 위험을
  증거로 판정한다. 테스트 작성, assertion 자동 완화 또는 커밋에는 사용하지 않는다.
---

# Verify Spring Tests

## 목적

구현자가 만든 테스트를 낙관적으로 승인하지 않고 실행 결과와 diff에 기반해 판정한다.

## 절차

1. `artifacts/02-test-cases.md`, `artifacts/03-implementation.md`와 실제 diff를 읽는다.
2. 승인 범위 밖 파일이 변경되지 않았는지 확인한다.
3. 테스트 목록의 각 ID가 코드와 assertion에 연결되는지 대조한다.
4. 빌드 파일과 Wrapper를 감지한 뒤 가장 좁은 대상 테스트를 실행하고 정확한 명령, 종료 코드,
   실행 시간과 결과를 기록한다.
5. 공유 Fixture나 연관 동작 위험이 있으면 관련 테스트를 실행한다.
6. 환경과 시간이 허용하면 전체 테스트를 실행한다. 실행하지 못한 범위는 미검증으로 남긴다.
7. 동시성 테스트는 합리적인 범위에서 반복 실행해 flaky 신호, timeout, 누락된 outcome과 자원
   정리를 점검한다.
8. 결과와 회귀 위험을 `artifacts/04-verification.md`에 저장한다.

## 빌드 도구 선택

- `build.gradle` 또는 `build.gradle.kts`가 있으면 해당 모듈의 `gradlew`/`gradlew.bat`을 사용한다.
- `pom.xml`이 있으면 해당 모듈의 `mvnw`/`mvnw.cmd`를 사용한다.
- Windows에서는 `.bat` 또는 `.cmd`, Unix 계열에서는 실행 가능한 wrapper script를 선택한다.
- 대상 테스트는 Gradle의 `test --tests <FQCN>` 또는 Maven의 `-Dtest=<ClassName> test`로 좁혀
  실행하고, 관련·전체 테스트는 같은 Wrapper와 모듈 경계를 유지한다.
- 빌드 파일은 있지만 대응하는 Wrapper가 없으면 전역 Gradle/Maven으로 임의 대체하지 않는다.
  사용 가능한 실행 경로를 확인하고 확보할 수 없으면 BLOCKED 또는 LIMITED PASS 근거로 기록한다.
- 모든 실행의 working directory, 전체 명령, 종료 코드, 실행 시간과 통과·실패·미실행 범위를
  `artifacts/04-verification.md`에 남긴다.

## 판정

- `PASS`: 계획된 케이스가 모두 구현되고 대상·관련·전체 테스트가 실행되어 통과했다.
- `LIMITED PASS`: 구현과 실행된 범위는 통과했지만 환경 제약으로 일부 범위를 실행하지 못했다.
- `FAIL`: assertion 실패, 컴파일 실패, 승인 범위 이탈, flaky 신호 또는 가독성 기준 위반이 있다.
- `BLOCKED`: Docker, 외부 서비스, 권한 등 필요한 환경을 확보할 수 없다.

애매하면 PASS가 아니라 LIMITED PASS 또는 FAIL로 판정한다.

## 회귀 위험 체크

- 보호해야 할 운영 동작이 assertion에 직접 연결되는가?
- 정상·예외·경계값·회귀 중 누락 또는 형식적 케이스가 있는가?
- 시간, 순서, 현재 시각, 무작위 값, 공유 DB 상태에 불필요하게 의존하는가?
- mock이 구현 세부사항에 과도하게 결합되어 있는가?
- 통합 테스트가 필요한 동작을 단위 mock 테스트로만 대신했는가?
- 동시성 테스트가 작업 미완료나 예상 밖 예외를 성공처럼 처리할 수 있는가?
- 성공 수와 승인된 예상 실패 수가 전체 작업 수와 일치하고, 예상 밖 실패 수가 별도로 0인지
  assertion하는가?
- 승인된 예상 실패의 예외 타입·오류 코드별 개수를 검증하며, 예상 밖 실패의 타입·오류 코드·메시지가
  진단에 남는가?
- 테스트 비용과 실행 시간이 CI에서 감당 가능한가?

## 금지 행동

- 검증 중 코드를 직접 고치지 않는다.
- 테스트 미실행을 PASS로 기록하지 않는다.
- 문제 위치와 근거 없이 “가독성이 낮다” 또는 “괜찮아 보인다”고 판정하지 않는다.
- `git add`, `git commit`, `git push`를 실행하지 않는다.
