---
name: debug-transaction
description: Debug Spring transaction issues including rollback, propagation, and locking problems.
---

# Debug Transaction

## Goal
Identify root cause of transaction-related bugs.

## Checklist

1. @Transactional 위치 확인
2. propagation 설정 확인
3. isolation level 확인
4. self-invocation 여부 확인
5. DB lock 여부 확인
6. connection pool 상태 확인

## Common Issues

- Transaction not applied (self-invocation)
- Unexpected rollback
- Deadlock
- LazyInitializationException

## Analysis Output

- Root cause
- Why it happened
- Fix strategy
- Code example

## Example

"Why is transaction not working here?"