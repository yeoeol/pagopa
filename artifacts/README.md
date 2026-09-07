# Spring Test Generation Artifacts

테스트 생성 하네스의 현재 상태와 단계별 인수인계 파일을 설명한다.

## 현재 상태

- 하네스 버전: v0.1
- 구성 방식: A — 대상 테스트 파일 내부의 외과적 변경
- 현재 브랜치: `test/search-history-concurrency`
- 실행 상태: 구현 및 독립 검증 완료 — **LIMITED PASS (2026-09-04)**
- 테스트 구현 커밋: `3076fe7` (`test: 검색 기록 저장 동시성 및 정상 흐름, 예외 흐름 테스트 코드 구현`)
- 현재 상태: 품질 리뷰에 따른 하네스 계약·산출물 동기화와 사용자 검토 완료 (2026-09-08)

## 산출물 지도

| 파일 | 역할 | 만든 단계 | 다음에 읽는 곳 | 현재 상태 |
| --- | --- | --- | --- | --- |
| `00-request.md` | 요청과 범위 | Phase 0 | 모든 단계 | 요청 기록 완료 |
| `01-project-context.md` | 프로젝트·스타일 분석 | Phase 1 | 테스트 설계·구현 | 분석 완료 |
| `02-test-cases.md` | 정상·예외·경계값·회귀 목록 | Phase 2 | 사용자·구현 | 승인 완료, 최종 컨텍스트 동기화 |
| `03-implementation.md` | 코드 변경과 케이스 매핑 | Phase 6 | 검증 | 구현 완료 |
| `04-verification.md` | 테스트 결과와 회귀 위험 | Phase 7 | 사용자 | LIMITED PASS (2026-09-04) |
| `final.md` | 최종 테스트 설명과 인수인계 | Phase 7 | 사용자·다음 실행 | LIMITED PASS 결과 기록 |
| `improvement-log.md` | 실패와 개선 기록 | Phase 7 | 다음 개선 | 품질 리뷰 반영 기록 |
| `decisions/ADR-001-surgical-test-changes.md` | A 방식 선택 근거 | 설계 | 다음 구조 변경 | 채택 |
| `evals/test-prompts.md` | 하네스 정상·부정·회귀 시험 | Phase 7 | 하네스 검증 | 작성 완료 |
| `evals/trigger-cases.md` | 자연어 라우팅 near-miss | Phase 7 | Skill 트리거 검증 | 작성 완료 |
| `evals/ab-comparison.md` | with-harness와 baseline 비교 | Phase 7 | 하네스 가치 평가 | 실제 요청 대기 |

## 실행 모드와 stale 규칙

- 초기 실행: `00-request.md`에 실제 대상이 없을 때 새 요청을 기록하고 처음부터 진행한다.
- 부분 재실행: 대상, 요구사항, 기준 커밋이 같고 특정 산출물만 수정할 때 해당 단계부터 진행한다.
- 새 실행: 대상 또는 요구사항이 달라지면 기존 파일을 `archive/{YYYYMMDD-HHMMSS}/`에 보존한다.
- stale: 테스트 목록 승인 후 승인된 테스트 구현 diff를 제외하고 기준 커밋 이후 대상 코드,
  빌드 설정 또는 사용자 변경이 달라지면 승인 상태를 폐기하고 컨텍스트 분석과 테스트 목록 승인을
  다시 수행한다.

대화에만 남은 분석·승인·검증은 완료된 산출물로 간주하지 않는다.
