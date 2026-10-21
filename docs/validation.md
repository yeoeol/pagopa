# Pagopa 대상 테스트 검증 기록

검증일: 2026.10.10

검증한 소스 커밋: `dbf57d4d307615fa7cf4729096c316277a06ab66`

현재 코드에서 재고 동시성, 모듈 의존 관계, 추천 집계·조회·이벤트 흐름을 대상으로 테스트를 실행했습니다. Gradle은 `BUILD SUCCESSFUL`로 종료했으며, JUnit XML 결과를 기준으로 총 28개가 통과했습니다.

## 실행 환경과 명령

- Windows, Java 25 toolchain, Gradle Wrapper 9.4.0
- Docker Server 28.3.0
- Testcontainers: MySQL 8.0.36, Azurite 3.35.0
- 테스트 설정: `../src/test/resources/application-test.yml`
- DB 통합 테스트는 Testcontainers의 격리된 환경 사용

```powershell
.\gradlew.bat test --tests '*StockConcurrencyTest' --tests '*ApplicationModulithTest' --tests '*RecommendationProjectionServiceTest' --tests '*RecommendationQueryServiceTest' --tests '*RecommendationFlowIntegrationTest' --no-daemon --console=plain
```

## 결과

| 테스트 클래스 | 실행 수 | 실패 | 오류 | 건너뜀 |
| --- | ---: | ---: | ---: | ---: |
| `ApplicationModulithTest` | 1 | 0 | 0 | 0 |
| `StockConcurrencyTest` | 12 | 0 | 0 | 0 |
| `RecommendationProjectionServiceTest` | 6 | 0 | 0 | 0 |
| `RecommendationQueryServiceTest` | 7 | 0 | 0 | 0 |
| `RecommendationFlowIntegrationTest` | 2 | 0 | 0 | 0 |
| **합계** | **28** | **0** | **0** | **0** |

각 클래스의 보고서 원본은 실행 후 `build/test-results/test/TEST-*.xml`에서 확인할 수 있습니다. 빌드 산출물은 다시 실행하면 갱신되므로 이 문서는 해당 날짜의 검증 기록입니다.

## 재고·취소 시나리오

각 시나리오를 `N=50`, `200`, `1,000`으로 실행해 총 12개를 검증했습니다. 서비스 메서드를 여러 스레드에서 호출하는 테스트이며, 외부 HTTP 부하 도구를 사용한 벤치마크는 아닙니다.

| 시나리오 | 테스트의 성공 조건 | 결과 |
| --- | --- | --- |
| 재고 10개인 동일 상품에 N개 동시 주문 | 주문 10개 성공, N−10개 품절 처리, 기타 예외 0개, 최종 재고 0개 | 세 규모 모두 통과 |
| 서로 다른 N개 상품에 각각 주문 | N개 성공, 품절 0개, 기타 예외 0개 | 세 규모 모두 통과 |
| 동일 주문에 N개 동시 취소 | 취소 1개 성공, 나머지 업무 예외 처리, 기타 예외 0개, 상태 CANCELED, 세 상품 재고 10·20·30으로 복구 | 세 규모 모두 통과 |
| 같은 상품을 주문한 서로 다른 N개 주문의 동시 취소 | N개 취소 성공, 업무·기타 예외 0개, 최종 재고 N개로 복구 | 세 규모 모두 통과 |

이 결과는 해당 테스트 데이터와 로컬 실행 환경에서의 정합성 검증입니다. 처리량·응답 시간 개선율·운영 환경의 동시 사용자 수를 의미하지 않습니다.

## 모듈과 추천 검증

- `ApplicationModulithTest`: 모듈 의존 관계 검증
- `RecommendationProjectionServiceTest`: 검색·주문 관심도 반영, 중복 이벤트 처리, 트랜잭션 롤백 관련 검증
- `RecommendationQueryServiceTest`: 관심 상품·키워드 후보·기본 상품을 이용한 추천 조회 검증
- `RecommendationFlowIntegrationTest`: 이벤트를 통한 추천 집계 흐름 검증

## 검증 범위

이번 실행은 위 다섯 클래스의 대상 테스트입니다. 전체 테스트 스위트, GitHub Actions의 최근 실행 결과, 실제 결제 사업자 승인·취소, 실제 Azure·Supabase 이미지 업로드·삭제, 운영 모니터링과 HTTP 성능 측정은 이번 통과 결과에 포함하지 않습니다.
