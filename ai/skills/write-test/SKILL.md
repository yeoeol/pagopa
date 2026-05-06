---
name: write-test
description: Generate tests for Spring Boot application following best practices.
---

# Write Test

## Goal
Write meaningful and maintainable tests.

## Test Types

- Domain: pure unit test
- Application: integration test
- Infrastructure: slice test

## Rules

- Prefer slice tests:
    - @WebMvcTest
    - @DataJpaTest
- Avoid unnecessary @SpringBootTest

## Structure

- given / when / then
- clear naming

## Coverage Focus

- business logic
- edge cases
- failure scenarios

## Output

- Test code
- What is being tested
- Why it matters

## Example

"Write test for order cancel logic"