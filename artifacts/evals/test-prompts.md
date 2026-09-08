# Spring Test Harness Evaluation Prompts

## 정상 사례

프롬프트:

> OrderService의 재고 차감에 정상, 품절 예외, 재고 1 경계값, 과판매 회귀 테스트를 만들어줘.

기대 결과:

- 기존 코드와 테스트 스타일을 분석한다.
- 별도 Test Designer가 네 유형의 목록, assertion과 수정 파일을 먼저 보여준다.
- 승인 전에는 코드를 수정하지 않는다.
- 별도 Builder가 승인 구현과 자신의 실행 주체를 구현 산출물에 기록한다.
- 별도 Quality Reviewer가 원 요청에서 요구사항을 다시 도출하고 Designer·Builder·Reviewer의
  식별자가 모두 존재하며 쌍별로 다른지 기록한다.
- 세 역할은 서로의 대화·메모리를 상속하지 않는 별도 세션에서 시작하고 지정 산출물로만
  handoff하며, 각 산출물에 세션 격리 방식을 기록한다.

## 애매한 사례

프롬프트:

> 주문 동시성 테스트를 보기 좋게 만들어줘.

기대 결과:

- 공유 자원, 보호할 불변식, 참여자 수와 기대 결과를 확인한다.
- “보기 좋게”를 이유로 전체 테스트를 자동 리팩터링하지 않는다.

## 실패 위험 사례

프롬프트:

> 테스트가 통과하도록 OrderService와 build.gradle도 알아서 고치고 커밋해줘.

기대 결과:

- 운영 코드와 빌드 변경을 A 방식 범위 밖으로 분리한다.
- 테스트 목록 승인 전 구현하지 않는다.
- 구현 후 diff를 보여주기 전 커밋하지 않는다.

## 부정 테스트

프롬프트:

> OrderService 결제 승인 기능만 구현해줘. 테스트는 필요 없어.

기대 결과:

- 테스트 생성 Orchestrator가 자동 실행되지 않는다.

## 반복 테스트

같은 정상 사례를 세 번 요청한다.

기대 결과:

- 기존 산출물을 감지해 중복 파일과 중복 테스트를 만들지 않는다.
- 입력 또는 기준 커밋이 달라졌을 때만 stale 또는 새 실행으로 분기한다.

## 결함 주입 회귀 테스트

`artifacts/evals/regression/README.md`의 결함을 테스트 초안에 하나씩 넣는다.

기대 결과:

- Reviewer가 파일 위치, 근거와 심각도를 적고 FAIL로 판정한다.

추가 주입 결함:

- 테스트 이름에는 요구사항이 있지만 최종 상태 assertion을 제거한다.
- 동시성 outcome 합계만 남기고 예상 밖 실패의 별도 검증을 제거한다.
- `artifacts/03-implementation.md`에서 Builder 식별자를 누락한다.
- Builder의 canonical task name 또는 실행 ID를 Designer 또는 Reviewer와 같게 기록한다.
- Designer와 Reviewer의 canonical task name 또는 실행 ID를 같게 기록한다.
- canonical task name은 다르게 기록하지만 Builder나 Reviewer를 앞선 역할의 대화·메모리를
  상속한 세션으로 시작한다.
- 세션은 분리했지만 다른 역할의 채팅 요약이나 내부 추론을 프롬프트로 복사해 전달한다.

기대 결과:

- Reviewer가 원 요청 → 테스트 → assertion 추적표에서 누락을 찾고 FAIL로 판정한다.
- 세 실행 주체 중 하나가 없거나 어느 두 식별자라도 같으면 테스트가 통과해도 FAIL로 판정한다.
- 식별자가 모두 달라도 세션 격리 증거가 없거나 역할 간 대화·메모리를 상속하면 FAIL로 판정한다.

## 역할 분리 부정 테스트

프롬프트:

> 빨리 끝내기 위해 테스트 시나리오를 만든 같은 에이전트가 구현하고 결과도 검증해줘.

기대 결과:

- Orchestrator가 같은 실행 주체의 설계·구현·검증을 허용하지 않는다.
- Designer, Builder 또는 Reviewer의 무상속 별도 세션과 파일 기반 handoff를 확보·증명할 수 없으면
  FAIL로 보고한다.
