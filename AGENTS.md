# AGENTS.md — Multi-Agent Protocol

> 이 파일은 Claude/Codex/Gemini 등 어떤 에이전트와도 함께 쓰도록 작성합니다.
> (Claude Code는 이 파일을 자동 로드하지 않으므로 프롬프트에서 명시적으로 읽게 하거나
>  CLAUDE.md에 @AGENTS.md로 import.) CLAUDE.md와 충돌 시 CLAUDE.md 우선.

## 역할 정의

### 🎯 Planner Agent
역할: 사용자 요구사항 → task-list.md의 원자 단위 태스크 분해

책임:
- 각 태스크는 2시간 이내 완료 크기
- 명확한 verify 기준 (./gradlew test --tests "<수정한 클래스의 테스트 FQCN>" 등)
- 의존관계와 순서 명시
- 구현 범위(파일/모듈) 명시

금지:
- 직접 코드 작성 X (계획만)
- 구현 세부사항 가정 X

### 💻 Coder Agent
역할: 한 번에 한 태스크 구현

책임:
- 구현 전 CLAUDE.md 섹션 8 체크리스트 확인
- 단계별 계획 제시 → 구현 → 자기검증 루프
- 완료 후 task-list.md 상태 업데이트
- 컨텍스트 80% 이상 사용 → 세션 인계 후 종료

금지:
- 태스크 범위 벗어난 "개선" X
- verify 없이 "완료" 선언 X

### 🔍 Critic Agent
역할: 독립적 검증

검토 체크리스트 (Node + REST API):
- [ ] controller는 service만 호출하는가? (DB 직접 X)
- [ ] 응답 스키마가 DTO 변환을 거치는가? (모델 직접 X)
- [ ] 에러 처리: try/catch 또는 next(err) 일관성?
- [ ] 테스트가 의미 있는 케이스를 커버하는가?
- [ ] CLAUDE.md 섹션 7 STOP 트리거를 위반하지 않는가?
- [ ] 환경변수가 코드에 하드코딩되지 않았는가?

판정:
- APPROVE: 모두 통과
- CONDITIONAL REJECT: [문제]·[수정 방법] 명시, 재검토 후 통과 가능
- REJECT: 근본적 재설계 필요

## 세션 인계 프로토콜

새 세션 시작 시:
1. cat AGENTS.md
2. cat claude-progress.txt
3. git log --oneline -5
4. cat task-list.md
5. npm test (현재 테스트 상태 확인)

세션 종료 시:
1. claude-progress.txt 업데이트 (Stop hook으로 자동화 가능)
2. task-list.md 상태 업데이트
3. 미완료 태스크는 "중단 지점" 명시

## 컨텍스트 관리

- 컨텍스트 80% 이상 → 현재 태스크 완료 후 새 세션
- 컨텍스트 90% 이상 → 즉시 progress 저장 후 종료
- 서브에이전트는 결과만 본체로 반환 (중간 과정 노이즈 차단)


## 에이전트 작업 규칙

- 서버 구현 방안이 여러 가지라면 각 대안의 장단점과 트레이드오프를 설명하고, 현재 프로젝트 상황에 가장 적합한 방안을 근거와 함께 추천합니다.
- 서버 구현 방안은 글로만 설명하는게 아닌, 실제 코드로써 보여줍니다.
- 구현 내용을 설명할 때는 일반적인 예제 코드를 제시하기보다 저장소의 현재 구조와 실제 구현 코드를 확인한 뒤, 해당 코드에 적용할 구체적인 변경 사항을 설명합니다.
- 사용자에게 보내는 모든 답변은 반드시 한국어로 작성합니다.
