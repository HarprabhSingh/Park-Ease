# ParkEasy Learning Log

## Learning contract

**Recorded:** 2026-08-16  
**Primary objective:** Build a deployable, resume-worthy product while enabling the engineer to understand and explain every important decision from product discovery through production deployment.

### Mentoring rules

For every new technology, term, annotation, pattern, or infrastructure component:

1. Define it in plain language before relying on it.
2. Explain the problem it solves in ParkEasy.
3. Explain what happens underneath the abstraction.
4. Compare realistic alternatives and tradeoffs.
5. Identify failure modes and how to observe or debug them.
6. Let the engineer reason about the design before providing substantial implementation.
7. Break implementation into small tasks the engineer can own.
8. Review the engineer's work and explain corrections.
9. Verify behavior with meaningful tests and measurements.
10. Revisit the concept through interview-style questions after implementation.

### Mental-model-first protocol

**Reconfirmed by the engineer:** 2026-08-19

Before introducing or using a technology, framework feature, design pattern, command, annotation, or technical term, the mentor must establish a connected mental model. The explanation must cover, in plain language:

1. the concrete problem being solved;
2. what the term means and what it does not mean;
3. where the component sits in the ParkEasy architecture;
4. the runtime sequence from user action through application, infrastructure, and stored result;
5. what happens underneath the abstraction;
6. why this option was selected instead of realistic alternatives;
7. costs, limitations, and failure modes;
8. how a developer configures, tests, observes, and debugs it;
9. how the concept changes application code, deployment, and operations; and
10. what the engineer should be able to explain in an interview.

Substantial implementation must be preceded by this explanation and a short understanding check. Previously explained ideas may be referenced briefly, but new jargon must not be used as unexplained shorthand. The goal is a coherent picture of how the product, code, runtime, database, infrastructure, and user experience connect—not memorization of isolated definitions.

### Development rules

- Understanding takes priority over code-generation speed.
- New jargon is defined when introduced; unfamiliarity is not treated as assumed knowledge.
- Spring annotations and framework behavior are explained rather than used as magic.
- Java language features are introduced with their runtime and design implications.
- Database behavior is explained at PostgreSQL and transaction level, not only through JPA.
- Generated code is never considered complete until the engineer can explain it.
- Debugging follows reproduce, evidence, hypothesis, experiment, fix, and regression-test steps.
- Infrastructure is added only for a demonstrated product or operational reason.
- Resume bullets use implemented, tested, and measured facts only.
- Product discovery continues in parallel and does not unnecessarily block engineering progress.

### Completion standard for a feature

A feature is not considered educationally complete until the engineer can explain:

- the user problem and requirements;
- the API contract;
- the domain and database model;
- the important Java and Spring mechanisms;
- transaction and concurrency behavior;
- security boundaries;
- expected failures and debugging evidence;
- test strategy;
- deployment and observability implications; and
- any measured performance claim.

## Progress log

### Product and architecture foundation

- Defined and iteratively reviewed PRD.
- Selected a modular monolith and explained why microservices do not automatically provide modularity or remove single points of failure.
- Defined feature-oriented module boundaries and public-contract rules.
- Distinguished owner-offered availability from booking-owned occupancy.

### Booking-domain learning

- Distinguished `BookingAttempt`, `PaymentAttempt`, `Booking`, and `Cancellation`.
- Defined idempotency-key behavior, request fingerprints, and PostgreSQL uniqueness as the final duplicate-request guard.
- Distinguished idempotency from general concurrent-booking protection.
- Selected minimal persisted booking state and time-derived views.
- Defined driver and owner cancellation principles.
- Added a provisional turnover buffer and explained why applied policy is snapshotted.

### Database learning

- Corrected foreign-key direction using one-to-many relationships.
- Preserved bookings after cancellation and user/listing deactivation.
- Selected PostgreSQL-generated UUIDv7 identifiers after comparing generation ownership, UUID versions, B-tree page splits, and cache locality.
- Selected authoritative scalar times plus a database-generated PostgreSQL range.
- Explained per-listing pessimistic locking and serialization.
- Selected application pre-check, listing-row lock, and PostgreSQL exclusion constraint as layered booking protection.

### HTTP and authentication learning

- Distinguished request method, path, headers, body, status, and response.
- Distinguished authentication from authorization.
- Applied the rule that clients express intent while the backend derives authoritative identity, price, status, and internal policy.
- Compared server-side session cookies with JWT bearer tokens.
- Selected server-side session authentication with CSRF protection for the first-party browser MVP.
- Kept the internal turnover buffer out of customer responses to avoid changing the contractual departure meaning.

### Repository and database foundation

**Implemented and verified:** 2026-08-19

- Created the Java 21, Maven, and Spring Boot 4.1 application foundation.
- Distinguished Maven's project model, dependencies, starters, plugins, lifecycle phases, and executable JAR packaging.
- Added an application-context test and verified the packaged application's Actuator health endpoint.
- Distinguished Docker images, containers, Compose services, named volumes, port mappings, and health checks.
- Added a PostgreSQL 18.4 Compose service on host port `5434` without disturbing unrelated PostgreSQL services on ports `5432` and `5433`.
- Added JDBC, HikariCP connection pooling, Flyway, the PostgreSQL driver, and Testcontainers.
- Added Flyway migration `V1` to enable the `btree_gist` extension required by the future booking non-overlap constraint.
- Proved with a real PostgreSQL Testcontainer that Flyway applied exactly one migration and that `btree_gist` was installed.
- Started the packaged application against the Compose database, observed Flyway migrate it to version 1, and received Actuator status `UP`.
- Diagnosed JVM and Docker CLI crashes from system-wide RAM/page-file exhaustion rather than treating them as application or assertion failures.

### Accounts database foundation

**Implemented and verified:** 2026-08-24

- Distinguished a business module from a folder, Java class, deployable service, and PostgreSQL named schema.
- Assigned account identity, local credentials, roles, and account status to the Accounts & Access module.
- Distinguished authentication (who the user is) from authorization (what the user may do).
- Modeled multi-role membership with a separate `user_roles` relation and composite primary key.
- Isolated password hashes in a one-to-one `user_credentials` table; plaintext passwords are never persisted.
- Added Flyway migration `V2` for `users`, `user_credentials`, and `user_roles` with UUIDv7 defaults, foreign keys, uniqueness, and check constraints.
- Demonstrated transaction atomicity by forcing role insertion to fail and proving that the user and credential inserts were rolled back.
- Verified V1 and V2 plus account invariants with five tests against a real PostgreSQL 18.4 Testcontainer.
- Diagnosed OneDrive archive locking in the Maven dependency cache and separated disposable build-cache data from project source.

## Upcoming learning

1. HTTP and REST fundamentals through the booking API.
2. API error and idempotency semantics.
3. Java domain modeling without framework dependence.
4. PostgreSQL booking-table migration and exclusion-constraint experiment.
5. Spring dependency injection, validation, transactions, and persistence.
6. Booking integration and concurrency testing with Testcontainers.
