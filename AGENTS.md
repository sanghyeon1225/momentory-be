# AGENTS.md

## Project

Momentory backend.

- Java 21
- Spring Boot 4.1
- Gradle Kotlin DSL
- PostgreSQL
- JPA
- Flyway
- Docker

## Specification

Before implementing a feature:

- Read the related API and feature specifications through Notion MCP.
- Do not guess undocumented requirements.
- Do not change API paths, request/response fields, status codes, or error codes without approval.
- If the specification and code conflict, report it before implementing.
- If Notion cannot be accessed, say so instead of pretending it was checked.

## Development Principles

- State important assumptions before non-trivial work.
- Prefer the simplest implementation that satisfies the requirement.
- Do not modify or refactor unrelated code.
- Follow the existing project style.
- Every changed line must relate to the requested task.
- Define success criteria and verify them with tests.

## Object-Oriented Design

- Keep business rules close to the domain object that owns them.
- Protect entity state and avoid public setters.
- Prefer intention-revealing methods over external state manipulation.
- Application services coordinate use cases; domain objects enforce business rules.
- Do not create abstractions or value objects without a clear domain benefit.

## DDD

- Organize code by domain, not only by technical layer.
- Keep aggregate boundaries small and based on consistency requirements.
- Modify aggregates through their aggregate root.
- Reference other aggregates by ID when direct object navigation is unnecessary.
- Use repositories for aggregate roots, not automatically for every table.
- Apply DDD selectively; do not force patterns onto simple CRUD.

## EDA

- Use events only when asynchronous separation provides a clear benefit.
- Prefer synchronous calls when immediate results or strong consistency are required.
- Use past-tense event names such as `DiaryCompleted`.
- Event handlers must consider duplicate delivery and retries.
- Do not add Kafka or RabbitMQ unless the use case justifies the complexity.

## API and Persistence

- Controllers must not access repositories directly.
- Do not expose JPA entities through APIs.
- Separate request and response DTOs.
- Use Bean Validation.
- Manage production schema changes with new Flyway migrations.
- Never edit an already-applied migration.
- Avoid eager loading as a generic solution to N+1 problems.

## Security

- Never commit secrets, `.env`, private keys, tokens, or passwords.
- Do not log tokens, personal diary content, or sensitive user data.
- Do not expose PostgreSQL port 5432 publicly.
- Do not use destructive Docker or database commands without explicit approval.

## Verification

After changes:

```bash
./gradlew check
```
Do not claim tests passed unless they were actually run.

At completion, briefly report:

- Changed files
- Main implementation details
- Tests executed and results
- Remaining risks or unclear requirements
