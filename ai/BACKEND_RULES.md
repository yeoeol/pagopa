# Backend Rules (DDD + Spring Boot)

## 1. Package Structure (Strict)

Each domain MUST follow this structure:

- application: use case orchestration
- domain: pure business logic
- infrastructure: external implementation (DB, API)
- presentation: controller layer

NEVER break this boundary.


## 2. Layer Responsibilities (Critical)

### presentation
- Handles HTTP request/response only
- Must NOT contain business logic
- Must NOT access repository directly
- Only calls application layer

### application
- Orchestrates use cases
- Coordinates domain objects
- Defines ports (interfaces) when needed
- Handles transaction boundary

### domain
- Contains core business logic
- Must be framework-independent
- Must NOT depend on Spring
- Must NOT depend on infrastructure

### infrastructure
- Implements repository, external API
- Depends on domain
- Must NOT contain business logic


## 3. Dependency Direction (Very Important)

Allowed:

presentation → application → domain  
application → domain  
infrastructure → domain

Forbidden:

domain → application ❌  
domain → infrastructure ❌  
application → presentation ❌


## 4. Domain Model Rules

- Domain model must contain business logic
- Avoid anemic domain model
- Use meaningful method names
Bad: order.setStatus(CANCELLED)
Good: order.cancel()

- Enforce invariants inside domain model


## 5. Repository Rules

- Repository interface must be in domain layer
- Implementation must be in infrastructure

Example:

domain.repository.OrderRepository  
→ infrastructure.persistence.OrderJpaRepository


## 6. DTO Rules

- DTO must exist only in application layer
- DTO must NOT be used in domain layer
- DTO must NOT leak into infrastructure

- Always separate:
    - request DTO
    - response DTO


## 7. Transaction Rules

- @Transactional ONLY in application layer
- NEVER in controller
- NEVER in domain

- One use case = one transaction (default)


## 8. Port & Adapter Rules (Important)

When external system exists:

- Define Port in application layer
- Implement Adapter in infrastructure

Example:

application.port.PaymentGateway
infrastructure.tossapi.TossPaymentAdapter



## 9. Validation Rules

- Input validation: presentation (@Valid)
- Business validation: domain

Example:

Bad: if (price < 0) throw exception
Good: product.validatePrice()



## 10. Exception Handling

- Use BusinessException only
- Domain throws domain-specific exception
- presentation handles via global exception handler


## 11. Security

- Security logic only in presentation/security or global
- Domain must NOT know about authentication


## 12. Scheduler / Async

- Must be in infrastructure layer
- Must call application layer only


## 13. Logging

- Log only at application boundary
- Do not log inside domain unless critical


## 14. Prohibited Patterns (Strict)

- Entity exposure in API ❌
- Repository call from Controller ❌
- Business logic in DTO ❌
- Static utility for domain logic ❌