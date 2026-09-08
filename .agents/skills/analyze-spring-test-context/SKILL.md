---
name: analyze-spring-test-context
description: >
  Spring 테스트를 설계하기 전에 빌드 설정, 대상 운영 코드, 인접 테스트, Fixture와 코드 스타일을
  읽어 근거 있는 컨텍스트 보고서를 만든다. 테스트 코드 구현이나 일반 아키텍처 리뷰에는 사용하지 않는다.
---

# Analyze Spring Test Context

## 목적

테스트 구현자가 추측하지 않도록 현재 프로젝트에서 실제로 사용 가능한 도구, 테스트 수준,
명명·구성 스타일과 변경 위험을 정리한다.

## 절차

1. 현재 브랜치와 변경 파일을 확인하고 사용자 변경을 표시한다.
2. `build.gradle` 또는 `pom.xml`에서 Java, Spring Boot, JUnit, assertion, mocking, DB와
   Testcontainers 의존성을 확인한다.
3. 테스트 대상과 직접 협력하는 운영 코드를 읽어 observable behavior와 불변식을 적는다.
4. 같은 패키지, 같은 계층, 비슷한 의존성을 가진 기존 테스트를 우선해 읽는다.
5. 공통 Fixture와 테스트 설정을 확인하되 개선 대상으로 자동 확장하지 않는다.
6. 프로젝트만으로 판단할 수 없는 API 사용법은 공식 Spring, JUnit, Mockito, Testcontainers 등
   1차 문서에서 확인하고 URL, 적용 버전과 확인 날짜를 기록한다.
7. 결과를 `artifacts/01-project-context.md` 형식으로 저장한다.

`ai/**`, `.claude/**`, `CLAUDE.md`, `GEMINI.md`는 읽지 않는다.

## 출력 형식

```md
# Project Test Context

- 대상:
- 기준 브랜치/커밋:
- 기존 사용자 변경:

## 기술 스택

## 관찰 가능한 동작과 불변식

## 기존 테스트 스타일

## 재사용 가능한 Fixture와 설정

## 공식 근거

## 위험과 확인 필요
```

## 품질 기준

- 스타일 판단마다 근거 파일을 적는다.
- 프로젝트에 없는 라이브러리나 패턴을 사용 가능하다고 가정하지 않는다.
- 기존 테스트의 불일치를 하나의 규칙처럼 단정하지 않는다.
- 읽기 전용으로 작업하며 코드와 빌드 파일을 수정하지 않는다.
- 결과를 다음 단계가 바로 사용할 수 있는 구체적인 제약으로 표현한다.
