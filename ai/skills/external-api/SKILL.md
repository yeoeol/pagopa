---
name: external-api
description: Integrate external API using port and adapter pattern.
---

# External API Integration

## Goal
Integrate external systems safely using DDD principles.

## Steps

1. Define Port (application layer)
2. Implement Adapter (infrastructure)
3. Map external response → domain model
4. Handle errors properly

## Rules

- Never call external API directly from application
- Always go through port
- Do not leak external DTO into domain

## Output

- Port interface
- Adapter implementation
- DTO mapping

## Example

"Integrate Toss payment API"