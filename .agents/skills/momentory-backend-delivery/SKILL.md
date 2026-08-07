---
name: momentory-backend-delivery
description: Implement and verify non-trivial Momentory backend changes with scoped testing, full verification, independent review, and iterative fixes while following repository AGENTS.md rules.
---

# Momentory Backend Delivery

## 1. Establish constraints

1. Read the repository `AGENTS.md` before inspecting or changing code. Treat it as the authoritative project rule set; do not duplicate it into this skill.
2. Confirm that the request authorizes implementation. For analysis-only or design-only requests, do not use this workflow.
3. Convert the request into a concise, verifiable checklist covering behavior, API contract, persistence, security, migrations, and tests as applicable.
4. Stop and ask the user before proceeding when requirements conflict, an API contract would change, an authentication policy would change, or production data could be damaged.
5. Do not commit, push, merge, rebase, or amend unless the user explicitly requests it. Never use destructive Git commands such as `git reset --hard` or `git clean -fd`.

## 2. Inspect before implementation

1. Inspect the relevant production code, tests, package structure, migrations, and existing API documentation before editing.
2. Inspect the working tree, including tracked, staged, unstaged, and untracked files. Preserve unrelated user changes.
3. Determine the smallest change set that satisfies the checklist. Do not refactor unrelated code.
4. Identify which verification gates apply to this change:
    - API or OpenAPI gate when an endpoint or API contract is added or changed.
    - Persistence and migration checks when JPA mappings or database schema are added or changed.
    - Security checks when authentication, authorization, or user-owned data access is affected.
5. If delegating implementation work, assign disjoint file ownership. Never allow multiple agents to modify the same file concurrently.

## 3. Implement and verify locally

1. Implement the minimum scoped change in accordance with `AGENTS.md`.

2. When an API endpoint is added or changed, complete the API/OpenAPI gate before considering the endpoint finished:

    - Inspect the actual exception handlers, validation messages, authentication failure handlers, and feature-specific exceptions relevant to the endpoint.
    - Determine the endpoint's real error matrix:
        - triggering condition;
        - HTTP status;
        - runtime error schema;
        - application error code;
        - runtime message.
    - Write OpenAPI responses from the actual runtime behavior. Do not invent error codes, messages, or response cases.
    - Ensure client-facing request schemas contain only actual request fields; internal validation or derived properties must not leak into JSON or OpenAPI schemas.
    - Add or update the OpenAPI contract test for the changed endpoint.
    - Inspect `/v3/api-docs` through the automated contract test or an equivalent executed verification and confirm that documented success schemas, error schemas, examples, and no-content responses match the runtime contract.

3. When persistence or schema changes are involved:

    - Compare the JPA mapping with the Flyway migration.
    - Verify relevant PK, FK, UNIQUE, NULL, deletion, enum, and index behavior.
    - Do not modify an already-applied migration.

4. Add or update focused tests for changed behavior. Cover relevant invalid input, ownership, rollback, persistence, and contract behavior. Do not delete tests or use `@Disabled` to obtain a passing build.

5. Run the relevant targeted tests first and inspect their actual results.

6. Run the full verification command required by `AGENTS.md`:

    - Windows: `.\gradlew.bat clean check`
    - Unix-like environments: `./gradlew clean check`

7. Never state that a test or verification passed unless it was actually run and passed. If a command cannot run, record the command, failure, and impact.

## 4. Independently review and iterate

1. Inspect the complete Git state and diff, including untracked source, test, and migration files. Run `git diff --check`.

2. Compare the original checklist against the actual diff. Identify missing requirements, unintended changes, contract mismatches, and validation gaps.

3. For API changes, explicitly compare the final controller and DTO annotations, runtime exception handling, and OpenAPI contract tests against each other. A build passing alone is not sufficient evidence that the documented API contract is correct.

4. Use a separate reviewer subagent for a read-only review. Instruct it explicitly:

    - Review the request, applicable `AGENTS.md`, relevant code and tests, Git status, and complete diff including untracked files.
    - Report substantiated findings only, using Critical, Major, Minor, or Unable to verify:
        - **Critical**: A merge-blocking security bypass, data corruption risk, migration failure, major feature unavailability, or equivalent issue.
        - **Major**: A required fix for a requirement mismatch, incorrect API or OpenAPI contract, transaction or rollback defect, missing core test, or equivalent issue.
        - **Minor**: A non-blocking issue with a real maintainability or readability impact.
        - **Unable to verify**: A matter that cannot be sufficiently checked in the current environment, such as an external system, production database, device integration, or concurrency environment.
    - Do not report subjective preferences, trivial formatting differences, or inconsequential naming preferences as findings.
    - Focus on correctness, API/OpenAPI contracts, security, persistence and Flyway compatibility, transaction behavior, tests, and scope.
    - For changed APIs, verify that documented error schemas, codes, messages, request fields, and examples are supported by actual runtime behavior.
    - Do not edit files, run mutating commands, commit, or modify Git state.

5. If a reviewer subagent is unavailable or fails to start, do not silently skip the review or claim that an independent reviewer ran. Perform a fresh read-only review pass separated from implementation: re-read the original request, applicable `AGENTS.md`, complete Git diff and untracked files, and relevant code and tests from the beginning. Record the limitation for the final report. A self-review used as this fallback cannot result in more than PASS WITH WARNINGS.

6. Treat a reviewer report or fallback self-review as evidence to investigate, not as proof. Confirm every Critical or Major finding against the actual code and requirements.

7. Fix every confirmed Critical or Major issue, add regression tests when appropriate, and rerun the relevant targeted tests.

8. Rerun the reviewer or, when unavailable, the fallback read-only review and necessary verification after each fix round. Perform at most three review rounds total. Do not claim a finding is fixed without confirming it in code and tests.

9. Document rejected reviewer findings with the concrete reason they do not apply. Do not modify code merely to satisfy an incorrect finding.

## 5. Final verification and decision

1. Before finishing, rerun full verification after the final code change.
2. Recheck Git status, tracked and untracked files, `git diff --check`, and the original checklist against the final diff.
3. For API changes, confirm that the API/OpenAPI gate was actually completed and that its contract tests passed.
4. For persistence changes, confirm that the final JPA mappings and Flyway migrations still agree.
5. Make one final reviewer pass when fewer than three rounds have been used and changes occurred since the most recent review. If a reviewer subagent is unavailable, perform the fallback read-only review and report that limitation.
6. Assign one final result:
    - **PASS**: All applicable checklist items and delivery gates are satisfied, required tests passed, a separate reviewer subagent completed successfully, and no confirmed Critical or Major issue remains.
    - **PASS WITH WARNINGS**: Core requirements and applicable delivery gates are satisfied and no confirmed Critical or Major issue remains, but the review used the fallback self-review or only non-blocking Unable to verify items or other explicit non-blocking verification limits remain.
    - **FAIL**: A requirement or applicable delivery gate is unmet, required verification fails or cannot provide sufficient confidence, a confirmed Critical or Major issue remains after the allowed review rounds, or an important Unable to verify item remains.

## Final report

Report only facts supported by the final repository state and executed commands. Include:

- Final result: PASS, PASS WITH WARNINGS, or FAIL.
- Implemented behavior and main design decisions.
- Created, modified, and deleted files.
- Commands actually run for targeted tests, full verification, Git inspection, and their results.
- Applicable delivery gates completed, including API/OpenAPI or persistence verification when relevant.
- Number of reviewer rounds.
- Whether a reviewer subagent ran; if not, the fallback self-review and its limitation.
- Confirmed reviewer issues found and fixed, including regression tests added.
- Rejected reviewer findings and why they were inapplicable.
- Remaining risks, failed or unexecuted verification, and unclear requirements.
- Whether the final changes are ready to commit, open as a PR, or merge. Do not perform those actions unless explicitly requested.
