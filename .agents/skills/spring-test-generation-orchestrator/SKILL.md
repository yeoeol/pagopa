---
name: spring-test-generation-orchestrator
description: >
  Spring Boot 프로젝트에서 테스트 코드 작성·수정, 정상·예외·경계값·회귀 시나리오,
  동시성 테스트와 기존 테스트 보강을 요청할 때 분석부터 승인, 구현, 검증, 커밋 전 검토까지
  조정한다. 운영 코드 기능 구현만 요청하거나 테스트 결과를 단순 설명하는 요청에는 사용하지 않는다.
---

# Spring Test Generation Orchestrator

## 목적

기존 테스트와 코드 스타일을 먼저 이해하고, 승인된 테스트만 최소 범위로 구현한 뒤 독립적인
검증 결과와 설명을 남긴다. 빠른 완성보다 읽기 쉬움, 재현 가능성, 회귀 방지를 우선한다.

## 입력

- 테스트 대상 클래스 또는 동작
- 해결하려는 결함이나 보호하려는 동작
- 필요한 테스트 수준: 단위, 슬라이스, 통합, 동시성 중 확인된 범위
- 사용자가 지정한 제약과 완료 기준

대상이나 기대 동작이 불분명하면 한 가지 핵심 질문을 하고 구현하지 않는다.

## 컨텍스트 제외

이 하네스에서는 `ai/**`, `.claude/**`, `CLAUDE.md`, `GEMINI.md`를 읽거나 지침으로 적용하지
않는다. 프로젝트의 빌드 설정, 운영 코드, 기존 테스트와 사용자가 직접 제공한 자료만 사용한다.

## 실행 모드

1. `artifacts/README.md`와 단계별 파일을 확인한다.
2. 대상과 기존 산출물이 없으면 초기 실행으로 분류한다.
3. 같은 대상의 일부 단계만 다시 요청했고 입력과 코드가 바뀌지 않았다면 부분 재실행한다.
4. 대상, 요구사항, 기준 브랜치 또는 관련 코드가 달라졌다면 기존 결과를
   `artifacts/archive/{YYYYMMDD-HHMMSS}/`에 보존하고 새 실행을 시작한다.
5. 테스트 목록 승인 후 승인된 테스트 구현 diff를 제외하고 기준 커밋 이후 대상 코드, 빌드 설정
   또는 사용자 변경이 달라졌다면 테스트 계획을 stale로 표시하고 다시 승인받는다.

## Phase 0. 현재 상황 확인

- `git branch --show-current`, `git status --short`, 최근 diff를 확인한다.
- 사용자 변경과 하네스가 만든 변경을 구분한다.
- 최종 산출물과 커밋 전 승인 조건을 확인한다.
- 요청을 `artifacts/00-request.md`에 저장한다.

## Phase 1. 도메인 분석

`analyze-spring-test-context`를 사용해 빌드, 대상 코드, 인접 테스트, Fixture, 외부 의존성과
스타일을 분석하고 `artifacts/01-project-context.md`에 저장한다.

## Phase 2. 산출물 정의

최종 산출물은 실제 `src/test/**` 코드, 구현 설명, 테스트 실행 결과와 회귀 위험 보고서다.
중간 산출물은 승인 가능한 테스트 목록이다.

## Phase 3. 팀 패턴 선택

Pipeline과 Producer-Reviewer를 함께 사용한다. 분석 → 설계 → 승인 → 구현 → 독립 검증 순서를
지키며 구현자가 자기 결과를 최종 합격 처리하지 않는다.

## Phase 4. Agent 설계

- `spring-test-context-analyst`: 읽기 전용 프로젝트 분석과 근거 수집
- `spring-test-code-builder`: 승인된 테스트의 A 방식 최소 구현
- `spring-test-quality-reviewer`: 테스트 실행과 회귀·가독성 판정

## Phase 5. Skill 설계

다음 순서로 작업 Skill을 사용한다.

1. `analyze-spring-test-context`
2. `design-spring-test-cases`
3. `implement-spring-tests`
4. `verify-spring-tests`

## Phase 6. 승인 구현

1. 분석 결과에서 테스트 가능한 동작과 위험을 확인한다.
2. `design-spring-test-cases`로 정상·예외·경계값·회귀 목록을 작성한다.
3. 승인 게이트 1에서 테스트 목록과 예상 수정 파일을 사용자에게 보여주고 멈춘다.
4. 명시적 승인 후에만 `implement-spring-tests`로 테스트 코드를 수정한다.

## Phase 7. 독립 검증·개선

1. `verify-spring-tests`로 대상 테스트, 관련 테스트와 가능한 범위의 전체 테스트를 검증한다.
2. 검증 실패가 테스트 구현 문제이면 최대 2회까지 구현→검증을 반복한다. 매 라운드 diff와
   결과를 보존하고 악화되면 직전 best로 돌아간다.
3. 운영 코드 결함, 환경 차단, 두 번의 수정 후 미통과이면 자동 통과시키지 않고 사용자에게
   선택지를 보고한다.
4. `artifacts/final.md`, Git diff와 상태를 보여주고 승인 게이트 2에서 멈춘다.
5. 사용자가 최종 결과를 직접 확인하고 별도로 승인해야만 커밋할 수 있다.

## 승인 게이트 1: 테스트 계획

```md
## 테스트 구현 승인 요청

- 대상:
- 테스트 목록: `artifacts/02-test-cases.md`
- 수정 예정 파일:
- 추가할 새 테스트와 파일 내부 private helper:
- 기존 테스트 setup·assertion 변경: 없음 또는 승인받을 정확한 항목
- 별도 승인 없이는 수정하지 않을 파일:
- 주요 트레이드오프:
- 승인하면 일어나는 일: 승인된 테스트만 A 방식으로 구현
- 지금 상태: 사람 승인 필요 — 코드 수정 전
```

`artifacts/02-test-cases.md`의 승인 상태와 대화의 명시적 승인을 모두 확인한다. 승인 라벨만
기록하고 다음 단계로 넘어가면 안 된다.

## 승인 게이트 2: 커밋 전 검토

```md
## 커밋 전 검토 요청

- 변경 파일:
- 테스트 결과:
- 회귀 위험:
- 미검증 영역:
- 확인할 diff:
- 지금 상태: 사람 승인 필요 — add/commit/push 미실행
```

초기 요청에 커밋 문구가 포함되어 있어도 구현 후 실제 diff를 보여준 다음 별도 승인을 받는다.

## 파일 기반 산출물 계약

| Phase | 파일 | 만드는 역할 | 다음에 읽는 역할 |
| --- | --- | --- | --- |
| Phase 0 | `artifacts/00-request.md` | Orchestrator | 모든 역할 |
| Phase 1 | `artifacts/01-project-context.md` | Context Analyst | Test Designer, Builder |
| Phase 2 | `artifacts/02-test-cases.md` | Test Designer | 사용자, Builder |
| Phase 6 | `artifacts/03-implementation.md` | Builder | Reviewer |
| Phase 7 | `artifacts/04-verification.md` | Reviewer | Orchestrator, 사용자 |
| Phase 7 | `artifacts/final.md` | Orchestrator | 사용자, 다음 실행 |
| Phase 7 | `artifacts/improvement-log.md` | Orchestrator | 다음 하네스 개선 |

지정 경로 저장에 실패하면 한 번 재시도하고, 다시 실패하면 차단으로 보고한다. 저장한 척 대화
요약으로 대체하지 않는다.

## 실패 처리

- 의미 있는 네 가지 유형 중 하나를 만들 수 없으면 억지 케이스 대신 이유와 대안을 승인
  목록에 적는다.
- 테스트 환경이나 Docker가 준비되지 않으면 실행하지 않은 테스트를 PASS로 표시하지 않는다.
- 필요한 의존성이 없으면 임의로 추가하지 않고 장단점과 대안을 사용자에게 제시한다.
- 테스트가 현재 운영 코드 결함을 재현하면 assertion을 약화하거나 운영 코드를 자동 수정하지 않는다.
- 사용자 변경과 충돌하면 해당 파일 수정을 멈추고 범위를 다시 확인한다.

## 사용 예시

자연어 요청:

```text
OrderService 재고 차감에 정상, 예외, 경계값, 회귀 테스트를 추가해줘.
```

직접 호출:

```text
$spring-test-generation-orchestrator OrderService 재고 차감 테스트를 설계하고 구현해줘.
```

## 하네스 자체 테스트

`artifacts/evals/test-prompts.md`의 정상, 애매함, 실패 위험, 부정, 반복, 결함 주입 테스트를
사용하고 결과를 개선 기록에 남긴다.
