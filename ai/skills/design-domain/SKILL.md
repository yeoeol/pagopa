---
name: design-domain
description: Design domain model with proper business logic and invariants.
---

# Design Domain

## Goal
Create rich domain model (not anemic).

## Rules

- Domain must contain behavior
- Use intention-revealing method names
- Enforce invariants inside entity

## Example

Bad:
order.setStatus(CANCELLED)

Good:
order.cancel()

## Output

- Domain entity
- Methods (behavior)
- Explanation of business rules

## Example

"Design order domain model"