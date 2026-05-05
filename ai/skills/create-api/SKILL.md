---
name: create-api
description: Create a new API following DDD structure (presentation, application, domain, infrastructure). Use when implementing a new feature or endpoint.
---

# Create API (DDD)

## Goal
Create a complete API with proper layering and responsibilities.

## Steps

1. Define request/response DTO (application layer)
2. Create Controller (presentation)
3. Create Application Service (use case)
4. Implement domain logic (domain model)
5. Define repository interface (domain)
6. Implement repository (infrastructure)
7. Add validation and exception handling

## Rules

- Follow DDD boundaries strictly
- Do NOT put business logic in Controller
- Domain must contain business logic
- DTO must not leak into domain
- Use @Transactional in application layer only

## Output Format

- Controller
- Application Service
- DTO (request/response)
- Domain logic (if needed)
- Repository (interface + implementation)

## Example Invocation

"Create API for order cancel"