# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**MOISAM (모이샘)** is a meeting-point finder service — given multiple departure locations, it calculates the optimal meeting spot with transit/driving route information. The deployed service is at https://www.moisam.kr.

Stack: Java 21, Spring Boot 3.4.4, PostgreSQL + PostGIS, Redis, QueryDSL, Spring Batch, Spring Security + OAuth2 (Kakao), JWT.

## Build & Run Commands

```bash
# Build (automatically copies config YMLs and generates OpenAPI spec)
./gradlew build

# Run tests (requires Docker for Testcontainers)
./gradlew test

# Run a single test class
./gradlew test --tests "com.meetup.server.event.application.EventServiceTest"

# Run a single test method
./gradlew test --tests "com.meetup.server.event.application.EventServiceTest.methodName"

# Run the application locally
./gradlew bootRun

# Generate OpenAPI spec and copy to swagger-ui resources
./gradlew copyOasToSwagger

# Manually copy config YMLs from config/ to src/main/resources/ (done automatically during build)
./gradlew copyConfig

# Clean also deletes QueryDSL generated sources in src/main/generated/
./gradlew clean
```

## Configuration

Application YMLs live in `/config/` (not in `src/main/resources/`) and are copied automatically during `processResources`. Profiles: `local`, `stg`, `prod`, `test`. Integration tests use `@ActiveProfiles("test")` which relies on Testcontainers (Docker must be running).

QueryDSL Q-classes are auto-generated into `src/main/generated/` during compilation. This directory is included in the source set automatically.

## Architecture

### Domain Package Structure

Each domain follows a consistent layered structure:

```
<domain>/
├── application/     # Service classes orchestrating use cases; owns transactions
├── domain/          # JPA entities and value objects
├── dto/             # Request/response DTOs (records)
├── exception/       # Domain-specific XxxException and XxxErrorType (implements ErrorType)
├── implement/       # Reusable business logic components (Readers, Processors, Validators)
├── infrastructure/  # JPA repositories, QueryDSL custom repos, Redis repos, CSV loaders
└── presentation/    # @RestController classes
```

**Layer responsibilities:**
- `application` — orchestrates `implement` components, manages transactions (`@Transactional`), never directly uses repositories
- `implement` — fine-grained, reusable components (e.g., `EventReader`, `EventProcessor`, `EventValidator`) that hold detailed business logic; these are what `application` services compose
- `infrastructure` — data access: JPA `Repository` interfaces, QueryDSL `CustomRepositoryImpl`, Redis repositories, CSV loaders
- `presentation` — REST controllers, never contains business logic

### Domains

| Domain | Purpose |
|--------|---------|
| `auth` | Kakao OAuth2 login, JWT issuance/refresh, cookie management |
| `event` | Core domain — meeting events, meeting-point route calculation with caching |
| `startpoint` | Departure points registered by users/guests for an event |
| `place` | Candidate meeting places (spatial queries via PostGIS) |
| `subway` | Subway station data, used in route calculation |
| `parkinglot` | Parking lot data near meeting places |
| `review` | User reviews of places |
| `user` | User profile management |
| `batch` | Spring Batch jobs for seeding place data from CSV and updating place images |
| `log` | Inflow logging |
| `admin` | Admin-only features |
| `global` | Cross-cutting: configs, external API clients, error handling, utilities |

### Global Package

- `global/clients/` — OpenFeign/RestClient-based external API clients: Kakao Local, Kakao Mobility, ODsay (transit routes), Google Places/Photos, Clova Studio (AI)
- `global/clients/ratelimit/` — Redis-based daily rate limiter (`@LimitRequestPerDay` annotation via AOP)
- `global/clients/resilience4j/` — Circuit breaker + retry event consumers
- `global/config/` — Spring configs (Security, Redis, JPA, QueryDSL, OpenFeign, CORS, Swagger)
- `global/support/error/` — `ErrorType` interface, `GlobalException`, `ErrorMessage`; each domain defines its own `XxxErrorType implements ErrorType`
- `global/support/response/` — `ApiResponse<T>` record (all controllers return this)
- `global/support/Performance.java` — `@Performance` annotation for AOP-based execution timing

### Error Handling Pattern

Domains define errors as enums implementing `ErrorType`:
```java
public enum EventErrorType implements ErrorType { ... }
public class EventException extends GlobalException {
    public EventException(EventErrorType errorType) { super(errorType); }
}
```

All controllers return `ApiResponse<T>` with `{ result: "SUCCESS"|"ERROR", data: ..., error: ... }`.

### Testing

- **Controller tests**: Use the `@ControllerTest` meta-annotation (custom `@WebMvcTest` that excludes Security auto-config and JWT filters). Extend `ControllerTestSupport` for MockMvc + REST Docs setup.
- **Integration tests**: Extend `IntegrationTestContainer` (abstract class with Testcontainers for PostGIS `postgis/postgis:17-3.5-alpine` and Redis `redis:7.4-alpine`).
- **Fixtures**: Test data objects live in `src/test/java/com/meetup/server/fixture/`.
- REST Docs snippets are generated during tests and assembled into OpenAPI spec via `./gradlew openapi3`.

### Key Architectural Notes

- **Route calculation**: `EventService.getMeetingPointRoutes()` uses a double-checked locking pattern with `ReentrantLock` (via `EventLockManager`) + Redis cache to prevent thundering herd on concurrent route calculation requests.
- **Meeting point algorithm**: `MeetingPointCalculator` computes candidate midpoints from all start-point coordinates; `RouteProcessor` then fetches transit/driving routes from ODsay/Kakao Mobility APIs for each candidate.
- **Spatial data**: Places use PostGIS geometry types (via `hibernate-spatial`) for proximity searches.
- **Commit convention**: `feat`, `fix`, `refactor`, `test`, `docs`, `chore`, `ci/cd` — commit messages include Jira ticket: e.g., `REFACTOR: (MOISAM-255) description`.
