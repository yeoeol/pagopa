# Orchestrator Trigger Cases

명백히 무관한 문장보다 테스트 생성과 인접한 near-miss를 사용해
`spring-test-generation-orchestrator`의 라우팅 경계를 확인한다.

## Should trigger

| ID | 사용자 표현 | 기대 이유 |
| --- | --- | --- |
| ST-01 | `SearchHistoryService 저장 성공과 중복 검색어 경계 테스트를 만들어줘` | Spring 테스트 신규 작성 |
| ST-02 | `StockConcurrencyTest 중복을 줄이되 테스트 목록부터 보여줘` | 기존 테스트 가독성 개선과 승인 흐름 |
| ST-03 | `OrderService 품절 BusinessException 회귀 테스트를 보강해줘` | 예외·회귀 테스트 보강 |
| ST-04 | `Repository 날짜 구간 [start, end) 경계값을 검증해줘` | 경계값 테스트 설계 |
| ST-05 | `이 버그를 재현하는 테스트를 먼저 만들고 통과 여부를 확인해줘` | 회귀 재현과 검증 |
| ST-06 | `기존 Fixture 스타일을 따라 Controller 단위 테스트를 작성해줘` | 기존 스타일 기반 테스트 구현 |

## Should not trigger

| ID | 사용자 표현 | 기대 경로 |
| --- | --- | --- |
| SNT-01 | `OrderService 주문 기능만 구현해줘. 테스트 변경은 하지 마` | 일반 운영 코드 구현 |
| SNT-02 | `현재 실패한 Gradle 로그가 무슨 뜻인지 설명해줘` | 읽기 전용 진단·설명 |
| SNT-03 | `k6로 주문 API 부하 시나리오만 만들어줘` | 부하 테스트 도구 작업 |
| SNT-04 | `Swagger 응답에 맞춰 프론트 API 호출 코드를 수정해줘` | 프론트엔드 API 구현 |
| SNT-05 | `패키지 구조와 도메인 아키텍처만 리뷰해줘` | 일반 아키텍처 리뷰 |
| SNT-06 | `docker-compose의 MySQL 포트만 바꿔줘` | 인프라 설정 변경 |

## 내부 Skill 경계

- 분석만 요청하면 `analyze-spring-test-context`에서 멈추고 구현 Skill을 실행하지 않는다.
- 테스트 목록 승인이 없으면 `implement-spring-tests`를 실행하지 않는다.
- 검증 요청에는 `verify-spring-tests`를 사용하며 구현자가 자기 결과를 PASS 처리하지 않는다.
