# Architecture (Pagopa Backend)

## 1. Architecture Style

- Domain-Driven Design (DDD)
- Layered + Partial Hexagonal Architecture
- Modular Monolith (current)

Each domain is independently structured and loosely coupled.


## 2. Domain Boundaries

Each domain is isolated:

- order
- payment
- product
- cart
- user
- review
- scrap
- category
- image
- searchhistory

Admin / Seller are separate bounded contexts.

Cross-domain access must go through application layer.


## 3. Communication Strategy

### Within same application
- Direct method call (application layer)

### Cross-domain
- Use application service
- Do NOT access another domain’s repository directly

Bad: orderService → productRepository ❌
Good: orderService → productApplicationService


## 4. Transaction Strategy

- Default: single transaction per use case
- Avoid distributed transaction

### Payment / Order
- Use eventual consistency when needed
- External API failure must not break domain consistency


## 5. External System Integration

Handled via Port & Adapter:

- Toss Payment → payment.infrastructure.tossapi
- Azure Image → image.infrastructure.azure

Rules:
- Never call external API directly from application
- Always go through port interface


## 6. Persistence Strategy

- JPA (Hibernate)
- Repository pattern

Rules:
- Domain defines repository interface
- Infrastructure implements it

### Performance
- Use fetch join to avoid N+1
- Use batch size where needed
- Use projection for read-heavy queries


## 7. Security Architecture

- JWT-based authentication
- OAuth integration (Kakao, Naver, Google, etc.)

Structure:
- auth module handles authentication
- domain does not depend on auth

User identity is passed via:
- argument resolver
- security context


## 8. Caching Strategy (if used)

- Prefer Redis (shared cache)
- Avoid local cache (Caffeine) in multi-instance


## 9. Scheduler / Background Jobs

Located in:
- infrastructure.scheduler

Rules:
- Scheduler triggers application use case
- Must be idempotent


## 10. Error Handling

- Global exception handler
- Standard response wrapper

Structure:
- global.exception
- global.response


## 11. API Design

- RESTful
- Use clear resource naming

Example:
- POST /orders
- POST /orders/{id}/cancel


## 12. Testing Strategy

- Domain: unit test (pure Java)
- Application: integration test
- Infrastructure: slice test (@DataJpaTest)

Avoid full @SpringBootTest unless necessary


## 13. Scaling Strategy (Future)

- Transition to MSA possible

Separation candidates:
- order / payment
- product / search

Must prepare:
- event-driven architecture
- Kafka (optional)


## 14. Key Design Principles

- High cohesion within domain
- Loose coupling between domains
- Explicit boundaries
- Business logic in domain
- Side effects controlled in application


## 15. Anti-Corruption Layer (Optional)

When integrating external systems:
- Translate external model → internal domain model
- Do not leak external schema into domain


## 16. Absolute Rules (Non-Negotiable)

- Domain must be pure (no Spring, no DB)
- Repository interface in domain only
- Transaction only in application
- Cross-domain = application call only