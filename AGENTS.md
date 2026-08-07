# AGENTS.md

## Project

Momentory backend.

- Java 21
- Spring Boot 4.1
- Gradle Kotlin DSL
- PostgreSQL
- Spring Data JPA
- Flyway
- Spring Security
- springdoc-openapi
- Docker

## Non-negotiable safeguards

- Do not change API paths, HTTP methods, request/response fields, status codes, or error codes without approval.
- Never edit an already-applied Flyway migration; add a new migration for schema changes.
- Never trust a user ID supplied in a request body for current-user operations; obtain it from the authenticated principal.
- Never commit secrets, `.env`, private keys, tokens, or passwords, or log tokens, personal diary content, or other sensitive user data.
- Do not delete tests, weaken assertions, or add `@Disabled` merely to pass the build.
- Do not run `git reset --hard`, `git clean -fd`, or destructive Git, Docker, or database commands without explicit approval.

## Working Principles

Before modifying code:

- Inspect the relevant existing code and tests and follow existing coding style and naming conventions.
- Do not guess requirements affecting API contracts, persistence, security, or business behavior; ask when they conflict or remain ambiguous.
- Prefer the simplest implementation that satisfies the requirement, and do not modify or refactor unrelated code.

## Package Architecture

The project uses feature-centered packages inside each top-level domain.

The structure below is illustrative, not exhaustive. Before adding or moving packages, inspect the current source tree and follow the structure of the closest existing domain.

```text
com.momentory
├─ auth
│  └─ <feature>
│     ├─ domain            # when the feature owns meaningful domain state/rules
│     ├─ application
│     ├─ infrastructure    # when persistence or external integrations are needed
│     └─ presentation
│
├─ user
│  ├─ domain
│  ├─ infrastructure
│  ├─ application
│  └─ <feature>
│     ├─ application
│     └─ presentation
│
├─ <domain>
│  ├─ domain
│  ├─ application
│  ├─ infrastructure
│  └─ presentation
│
├─ common
└─ config
```

Rules:

- The package tree above is a structural guideline, not an exhaustive inventory of current packages.
- Inspect the existing source tree before creating a package and follow the closest established pattern.
- Do not update `AGENTS.md` merely because a new feature package was added.
- Update this architecture section only when the project's package conventions or dependency rules change.
- Place feature-specific code inside its feature package; add shared code only for genuinely shared concepts.
- Do not move code to a shared package merely to avoid imports.
- Do not accumulate unrelated features in top-level `application` or `presentation`.
- Do not create unnecessary subpackages such as `controller`, `request`, `response`, or `service`.
- Do not add package layers without a clear responsibility.
- Tests should generally follow the same package structure as production code.

## Dependency Direction

- `presentation` handles HTTP concerns and calls `application`; controllers contain no business logic and do not access repositories directly.
- `application` coordinates use cases, transactions, domain objects, repositories, and external clients.
- `application` must not depend on `presentation`.
- `domain` must not depend on `application`, `presentation`, or infrastructure implementations; `infrastructure` may depend on domain types.
- Avoid circular dependencies and direct sibling-feature dependencies unless they represent a genuine shared capability.

## Domain Design

- Organize code around domains and features rather than only technical layers.
- Keep business rules close to their owning domain object. Protect entity state and avoid public setters.
- Change entity state through intention-revealing methods.
- Do not expose mutable internal collections; use defensive copies when receiving or returning them.
- Application services coordinate use cases; domain objects protect their invariants.
- Keep aggregate boundaries small and consistency-based. Modify aggregate state through its root, reference another aggregate by ID when navigation is unnecessary, and use repositories for aggregate roots or meaningful persistence boundaries.
- Apply DDD selectively; do not treat every JPA entity as an aggregate or force complex patterns onto simple CRUD.
- Do not introduce interfaces, factories, strategies, ports, value objects, or domain events without a concrete benefit. Prefer cohesive, readable classes and avoid duplicated validation, mapping, and conversion logic.

## Transactions

- Define transaction boundaries in application use-case methods.
- Do not use controllers as transaction boundaries.
- Changes that must succeed or fail together must run in one transaction; verify rollback behavior when multiple entities or collections change.
- Define repeated-request and idempotency behavior, and consider concurrency, when a database constraint or business invariant can be violated.
- Do not add pessimistic locking without a concrete consistency requirement.

## API and Error Handling

- Do not expose JPA entities through APIs; separate request and response DTOs.
- Use Java records for DTOs when consistent with existing code, and use Bean Validation for request-format validation.
- Keep business-state validation in the domain or application layer.
- Preserve the existing `{ "code", "message" }` error-response contract.
- Do not catch broad `Exception` types without a clear boundary-level reason or expose stack traces, SQL, internal class names, or sensitive values.
- A `204 No Content` response must not contain a response body.

## OpenAPI

- Swagger/OpenAPI documentation must match actual runtime behavior.
- Success responses must reference the correct success DTO; error responses must explicitly reference the actual error DTO.
- Do not let springdoc infer a success DTO for an error status, and document only status codes the API can return.
- Every non-2xx response must declare the correct error schema and at least one explicit `ExampleObject`; error documentation that specifies only a schema is not allowed.
- Every error example's `code` and `message` must match an actual runtime response; when an endpoint has a more specific application error code, do not replace it with a generic `INVALID_REQUEST` example.
- When adding or changing an endpoint, update and verify its OpenAPI contract test.
- A `204` response must not define a content schema. When changing OpenAPI annotations, verify `/v3/api-docs`.

## JPA and Flyway

- Persist enums using `EnumType.STRING` and prefer lazy loading.
- Do not use eager loading as a generic N+1 solution.
- Review fetch joins and projections for the use case, and keep database constraints aligned with application validation.
- Review PK, FK, UNIQUE, NOT NULL, CHECK, deletion policies, cascade, and orphan removal explicitly.
- Verify that Flyway migrations and JPA mappings match, and consider existing production data when adding constraints or non-null columns.

## Time Policy

- Use `Asia/Seoul` as the application local-time policy and centralize its identifier in `TimeZonePolicy`.
- Store absolute timestamps such as `createdAt` and `updatedAt` as `Instant`, with Hibernate JDBC time-zone handling in UTC.
- Use `LocalTime` for user-selected wall-clock values such as reflection time, and strictly parse exact `HH:mm` input.
- Do not convert `Instant` to `LocalDateTime` merely for convenience.

## Security and Privacy

- Do not weaken authentication or validation to make tests pass.

## Testing

- Add tests that cover changed behavior, relevant boundary and invalid-input cases, and shared-feature regressions.
- Cover authentication or authorization when applicable.
- Assert database state and rollback behavior when persistence or transactions matter; an HTTP status assertion alone is insufficient.
- Prefer integration tests for JPA, Flyway, security, transactions, and API contracts, and focused domain tests for domain invariants.

## Verification

Run the required full verification:

Windows:

```powershell
.\gradlew.bat clean check
```

Unix-like environments:

```bash
./gradlew clean check
```

For non-trivial backend delivery, follow `momentory-backend-delivery` for the detailed implementation, independent-review, diff-review, and verification loop. Never report tests or verification as successful unless they were actually executed and passed; report commands that could not be run and their impact.

## Git Safety

- Do not commit, push, merge, rebase, or amend unless explicitly requested.
- Do not discard existing user changes. Include untracked source, test, and migration files during review.
- Organize commits by meaningful behavior or architectural purpose, not mechanically by file type.

## Completion Report

Report implemented behavior, created/modified/deleted files, commands actually run and their results, remaining risks or unclear requirements, and whether the changes are ready to commit, open as a PR, or merge. For non-trivial backend delivery, include the additional report items required by `momentory-backend-delivery`.
