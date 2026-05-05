---
name: refactor-service
description: Refactor application/service layer code to follow DDD principles and improve maintainability.
---

# Refactor Service

## Goal
Improve readability, enforce boundaries, and remove bad practices.

## Checklist

- Is business logic inside domain?
- Is application layer only orchestrating?
- Are transactions correctly placed?
- Any duplicated logic?

## Refactoring Rules

- Move business logic → domain
- Split large methods
- Remove side effects
- Enforce single responsibility

## Common Problems

- Fat Service
- Anemic Domain Model
- Transaction misuse

## Output

- Before code
- After code
- Explanation (why improved)

## Example

"Refactor this order service"