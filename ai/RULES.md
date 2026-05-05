# Global Engineering Rules

## General
- Always write production-grade code
- Avoid unnecessary abstraction
- Prefer readability over cleverness
- Follow existing project conventions first

## Language
- Use Java 17+
- Use Spring Boot idioms
- Avoid reflection unless necessary

## Error Handling
- Use BusinessException only
- Do not throw generic Exception
- Always include error code

## Logging
- Use structured logging
- Do not log sensitive data

## Naming
- Classes: PascalCase
- Methods: camelCase
- Constants: UPPER_SNAKE_CASE

## AI Behavior
- Do not hallucinate APIs
- If unsure, ask instead of guessing