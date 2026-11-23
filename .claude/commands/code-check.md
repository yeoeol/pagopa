---
description: 5권 도서 원칙으로 코드 점검 — 현재 diff 또는 지정 파일을 24 악취·EJ 20·Clean Code 휴리스틱으로
argument-hint: <대상> (예: 파일 경로, "diff", "staged", 생략 시 git diff HEAD)
---

# /code-check — 코드 점검

대상: `$ARGUMENTS`

위키의 [[guide-code-authoring-and-review]] 를 베이스로 5권 도서 원칙 (24 악취·EJ 20 핵심·Clean Code 17장 휴리스틱·GRASP·오브젝트 책임 주도) 으로 코드를 점검한다.

## 절차

### 1. 대상 코드 수집

`$ARGUMENTS` 분기:
- 비어있으면 → `git diff HEAD` (워킹 트리 변경)
- `diff` → `git diff HEAD`
- `staged` → `git diff --cached`
- 파일 경로 → 그 파일 통째
- `main` / `<branch>` → `git diff <branch>...HEAD`

### 2. 가이드 본문 로드

먼저 가이드 본문 로드:
- **로컬 my-wiki 프로젝트**: `wiki/guide-code-authoring-and-review.md` 직접 읽기
- **다른 프로젝트** (`wiki/` 디렉터리 없음): WebFetch 로 `https://wons-wiki.web.app/guide-code-authoring-and-review/`

다음 섹션의 **3. 코드 점검 체크리스트** 활용:
- 3.1 리팩터링 24 악취
- 3.2 Clean Code 17장 ⭐ 휴리스틱 (G19·G23·G25·G30·G34·N1·T5 등)
- 3.3 EJ ⭐ 20 (Item 5·17·18·49·64·77·78·79·85)
- 3.4 GRASP 책임 할당

### 3. 점검 + 표 보고

각 발견을 표준 형식으로:

| 위치 | 코드 | 점검 | 처방 | 우선순위 |
|------|------|------|------|---------|
| `OrderService.java:42` | `public boolean process(Order o, boolean async, boolean retry)` | 리팩터링 3.4 (긴 매개변수) + EJ Item 51 (boolean 플래그) | `process(Order, ProcessOption)` 매개변수 객체화 | 🔴 High |
| `User.java:18` | `public List<Order> getOrders() { return orders; }` | EJ Item 50 (방어적 복사) | `return List.copyOf(orders)` | 🟡 Medium |
| `PriceCalculator.java:55` | `if (type == 1) ... else if (type == 2) ...` | 리팩터링 3.12 + Clean Code G23 | 다형성 (Strategy) 또는 enum + switch | 🟡 Medium |
| `OrderService.java:88` | `} catch (Exception e) {}` | Clean Code Item 77 | 최소 로그 + 의도 주석 또는 처리 | 🔴 High |

### 4. 우선순위 분류

| 우선순위 | 기준 | 예 |
|---------|------|-----|
| 🔴 **High** | 운영 사고 직결·보안·LSP 위배 | 빈 catch·SQL injection·전역 가변·LSP 위배 |
| 🟡 **Medium** | 가독성·유지보수·결합도 폭증 | 긴 함수·boolean 플래그·기본형 집착·중복 |
| 🟢 **Low** | 스타일·미시 정련 | 이름·서술적 변수·가짜 메서드 인라인 |

### 5. 종합 요약

점검 후:
- **발견 N건** (High X·Medium Y·Low Z)
- **반복 패턴** — 같은 악취가 여러 곳이면 묶어 보고
- **다음 단계 제안** — 즉시 수정 / PR 코멘트 / 별도 리팩터링 작업

### 6. 자동 수정 옵션 (선택)

사용자가 "수정해" 요청하면:
- High 우선순위만 자동 수정 후 보고
- Medium 은 제안만 (수정 트레이드오프 큼)
- Low 는 스킵
- 수정 후 빌드·테스트 통과 확인

## 참조 페이지

**my-wiki 프로젝트에서**: 위키 링크로 직접
- [[guide-code-authoring-and-review]] — 본 점검 체크리스트
- 24 악취: [[lecture-refactoring-ch3]]
- 17장 휴리스틱: [[lecture-clean-code-ch17]]
- EJ ⭐ 20: [[entity-effective-java]]
- GRASP: [[lecture-object-ch5]]

**다른 프로젝트에서**: WebFetch URL
- https://wons-wiki.web.app/guide-code-authoring-and-review/
- https://wons-wiki.web.app/lecture-refactoring-ch3/ — 24 악취
- https://wons-wiki.web.app/lecture-clean-code-ch17/ — 17장 휴리스틱
- https://wons-wiki.web.app/entity-effective-java/ — EJ ⭐ 20
- https://wons-wiki.web.app/lecture-object-ch5/ — GRASP

## 다른 프로젝트로 가져가기

이 파일을 새 프로젝트의 `.claude/commands/code-check.md` 로 두면 끝. 두 방법:

```bash
mkdir -p .claude/commands
# (a) GitHub 최신본 내려받기
curl -fsSL https://raw.githubusercontent.com/goodjwon/my-wiki/main/.claude/commands/code-check.md \
  -o .claude/commands/code-check.md
# (b) 또는 이 파일 내용을 그대로 복사해 위 경로에 저장 (오프라인)
```

→ 전체 안내: https://wons-wiki.web.app/guide-code-authoring-and-review/ 7절.

## 사용 예

```
/code-check                       # 현재 diff
/code-check staged                # staged 변경만
/code-check src/main/...Service.java  # 특정 파일
/code-check main                  # main 대비 현재 브랜치 diff
```

## 안전 규칙

- **점검 결과 보고만** — 사용자가 명시 요청해야 자동 수정.
- **위반 ≠ 무조건 잘못** — 트레이드오프 명시. 도그마 X.
- **운영 사고 직결 (High)** 만 강하게 권고. Medium·Low 는 검토 후보.
- *Effective Java* Item·*리팩터링* 카탈로그 번호 등 **출처 명시** — "왜 그렇게 해야 하는지" 추적 가능.
