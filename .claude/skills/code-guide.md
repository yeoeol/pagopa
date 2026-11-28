---
description: 5권 도서 원칙으로 코드 작성 가이드 — 새 코드 짤 때 체크리스트
argument-hint: <대상 영역> (예: function, class, exception, test, all)
---

# /code-guide — 코드 작성 가이드

대상: `$ARGUMENTS`

위키의 [[guide-code-authoring-and-review]] 를 베이스로 5권 도서 (오브젝트·Effective Java·리팩터링·Clean Code·TDD) 원칙으로 코드 작성을 안내한다.

## 절차

### 1. 가이드 본문 로드

먼저 가이드 본문을 로드:
- **로컬 my-wiki 프로젝트**: `wiki/guide-code-authoring-and-review.md` 직접 읽기
- **다른 프로젝트** (`wiki/` 디렉터리 없음): WebFetch 로 `https://wons-wiki.web.app/guide-code-authoring-and-review/` fetch

우선 활용 섹션:
- **2. 코드 작성 체크리스트** (Writing)
- **1. 6가지 핵심 설계 원칙** — 객체 협력·추상 의존·합성>상속·불변·단순 설계·테스트

### 2. 대상 영역 분기

`$ARGUMENTS` 가 비어있으면 **전체 작성 체크리스트** 7 카테고리 적용.

영역별:
- `function` — 2.1 함수 (Clean Code 3장 + EJ 49~56 + 리팩터링 6장)
- `class` — 2.2 객체·클래스 (오브젝트 4·5·10·11장 + EJ 17·18·20)
- `dependency` — 2.3 의존성 (오브젝트 8·9장 + EJ 5·64)
- `exception` — 2.4 예외·null (Clean Code 7장 + EJ 69~77)
- `domain` — 2.5 도메인 모델 (오브젝트 + DDD)
- `test` — 2.6 테스트 (TDD + Clean Code 9장)
- `concurrency` — 2.7 동시성 (EJ 78~84 + Clean Code 13장)
- `all` 또는 생략 — 전체

### 3. 사용자 요구사항 청취

"어떤 코드를 작성할 것인가" 사용자 입력 받기:
- 도메인 (주문·결제·인증 등)
- 기술 스택 (Spring Boot 4·JPA·WebFlux 등)
- 제약 사항 (성능·테스트 가능성 등)

### 4. 코드 작성 + 원칙 인용

작성하면서 어떤 원칙을 적용했는지 **인라인 주석 또는 별도 보고**:

```java
// EJ Item 5 — 생성자 주입
public OrderService(OrderRepository repo, PaymentGateway gateway) {
    this.repo = repo;
    this.gateway = gateway;
}

// Clean Code G19 — 서술적 변수
public Money totalFee(Order order) {
    Money baseAmount = order.items().stream().map(OrderItem::amount).reduce(Money.ZERO, Money::plus);
    Money discount = discountPolicy.apply(order);
    return baseAmount.minus(discount);
}
```

### 5. 자기 점검 + 보고

작성 완료 후 체크리스트 자기 점검 결과 표로 보고:

| 카테고리 | 항목 | 결과 |
|---------|------|------|
| 함수 | 한 화면 이내 | ✅ |
| 함수 | 한 가지 일 | ✅ |
| ... | ... | ... |

위반 항목이 있으면 트레이드오프 명시.

## 참조 페이지

**my-wiki 프로젝트에서**: 위키 링크로 직접 읽기
- [[guide-code-authoring-and-review]] — 본 가이드 (전체 체크리스트)
- [[guide-java-book-study-lab]] — 실습 환경 (Java 17·JUnit 5)
- 5권 entity: [[entity-object]]·[[entity-effective-java]]·[[entity-refactoring]]·[[entity-clean-code]]·[[entity-tdd]]

**다른 프로젝트에서**: WebFetch URL
- https://wons-wiki.web.app/guide-code-authoring-and-review/ — 본 가이드
- https://wons-wiki.web.app/guide-java-book-study-lab/ — 실습 환경
- https://wons-wiki.web.app/entity-object/ ·  /entity-effective-java/ · /entity-refactoring/ · /entity-clean-code/ · /entity-tdd/

## 다른 프로젝트로 가져가기

이 파일을 새 프로젝트의 `/code-guide.md` 로 두면 끝. 두 방법:

```bash
mkdir -p .claude/skills
# (a) GitHub 최신본 내려받기
curl -fsSL https://raw.githubusercontent.com/goodjwon/my-wiki/main/.claude/commands/code-guide.md \
  -o .claude/skills/code-guide.md
# (b) 또는 이 파일 내용을 그대로 복사해 위 경로에 저장 (오프라인)
```

→ 다른 프로젝트엔 `wiki/` 가 없으므로 WebFetch 로 위키 본문 자동 fetch (인터넷 필요).
전체 안내: https://wons-wiki.web.app/guide-code-authoring-and-review/ 7절.

## 사용 예

```
/code-guide                # 전체 체크리스트로 작성 안내
/code-guide function       # 함수 작성에만 집중
/code-guide test           # TDD + F.I.R.S.T. 로 테스트 작성
/code-guide class          # 클래스 설계
```
