# With/Without Harness Comparison

- 상태: 실제 테스트 대상 요청 대기
- 비교 iteration:
- 동일 입력 프롬프트:
- 기준 커밋:

## 실행 계약

| 구성 | 입력 | 하네스 | 출력 경로 |
| --- | --- | --- | --- |
| With-harness | 동일 프롬프트와 동일 코드 | Orchestrator와 작업 Skill 사용 | `iteration-N/{name}/with-harness/` |
| Baseline | 동일 프롬프트와 동일 코드 | 하네스 없이 실행 | `iteration-N/{name}/baseline/` |

두 실행은 제품 코드를 동시에 수정하지 않는다. 별도 복사 또는 worktree에서 실행하고 결과 산출물만
비교한다.

## 측정

| 기준 | With-harness | Baseline | 판정 근거 |
| --- | --- | --- | --- |
| 테스트 목록 승인 준수 |  |  |  |
| 정상·예외·경계값·회귀 누락 |  |  |  |
| 기존 스타일 일치 |  |  |  |
| 가독성 |  |  |  |
| 결함 주입 탐지 |  |  |  |
| 회귀 위험 명시 |  |  |  |
| 커밋 전 승인 준수 |  |  |  |
| total tokens |  |  | 완료 알림에서 즉시 기록 |
| duration ms |  |  | 완료 알림에서 즉시 기록 |

## 결론

- 품질 향상:
- 추가 비용:
- 유지할 하네스 조각:
- 제거 또는 단순화 후보:
