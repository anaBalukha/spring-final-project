# Student-Course Management API

Spring Boot REST API for managing students and their enrolled courses. It demonstrates a
production-style layered architecture (Controller → Service → Repository) with DTOs, validation,
global exception handling, Spring Security, externalized configuration (dev/prod profiles),
internationalization (i18n), structured logging, automated tests and Actuator-based monitoring.

## Technologies used

| Area | Technology |
|------|------------|
| Language / build | Java 21, Maven (wrapper included) |
| Framework | Spring Boot 4 (Web, Data JPA, Security, Validation, Actuator) |
| Persistence | Spring Data JPA / Hibernate · H2 (dev) · PostgreSQL (prod) |
| API docs | springdoc-openapi (Swagger UI) |
| Observability | Spring Boot Actuator · Micrometer · SLF4J + Logback |
| Testing | JUnit 5, Mockito, AssertJ, MockMvc, TestRestTemplate, Spring Security Test, JaCoCo |
| Utilities | Lombok |

## Running the application

```bash
# dev profile (default): in-memory H2 + seeded data, zero external dependencies
./mvnw spring-boot:run

# build a jar and run it
./mvnw clean package
java -jar target/Homework-0.0.1-SNAPSHOT.jar
```

The API starts on http://localhost:8080. See [Profiles](#profiles-dev--prod) for prod/PostgreSQL.

## Auth

HTTP Basic Auth. Send `Authorization: Basic <base64(username:password)>` on every protected request.

In Swagger UI, click the lock button and enter credentials.

## Default credentials

- admin / admin123 (ADMIN role)
- user / user123 (USER role)

Created automatically on first startup. New accounts: `POST /api/auth/register`

## Roles

- USER: view, create, update students and courses
- ADMIN: everything USER can + delete students/courses + view all users

## Endpoints

Public (no auth):
- GET /api/info — application metadata (localized welcome message)
- GET /api/students
- GET /api/students/{id}
- GET /api/courses
- GET /api/courses/{id}
- GET /api/courses/student/{studentId}
- POST /api/auth/register
- POST /api/auth/login

USER or ADMIN:
- POST /api/students
- PUT /api/students/{id}
- POST /api/courses
- PUT /api/courses/{id}
- GET /api/auth/me
- POST /api/auth/logout

ADMIN only:
- DELETE /api/students/{id}
- DELETE /api/courses/{id}
- GET /api/auth/users

Swagger UI: http://localhost:8080/swagger-ui.html

---

## Profiles (dev / prod)

Settings live in three files under `src/main/resources`:

| File | Purpose |
|------|---------|
| `application.properties` | Common settings + custom `app.settings.*` defaults |
| `application-dev.properties` | **dev**: in-memory H2 DB (recreated each start, seeded with test students/courses/users), H2 console, SQL logging |
| `application-prod.properties` | **prod**: persistent PostgreSQL, `ddl-auto=update`, SQL logging off |

If no profile is given, **dev is the default** (`spring.profiles.default=dev`), so the app runs
out of the box with zero external dependencies.

### Run with a profile — command line

```bash
# dev (H2 in-memory + test data)
./mvnw spring-boot:run "-Dspring-boot.run.profiles=dev"

# prod (requires PostgreSQL with database homework_db)
./mvnw spring-boot:run "-Dspring-boot.run.profiles=prod"

# or with a packaged jar
java -jar target/Homework-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

### Run with a profile — IDE (IntelliJ)

Run → Edit Configurations → your Spring Boot run configuration →
**Active profiles**: `dev` or `prod` (or add `-Dspring.profiles.active=prod` to VM options).

For prod, first create the database: `CREATE DATABASE homework_db;` and adjust the credentials
in `application-prod.properties`.

Dev extras: H2 console at http://localhost:8080/h2-console
(JDBC URL `jdbc:h2:mem:homework_db`, user `sa`, empty password).

## Custom configuration properties (`app.settings.*`)

Bound to the validated `AppSettingsProperties` class (`@ConfigurationProperties(prefix = "app.settings")`,
JSR-303 validated — invalid values fail startup):

| Property | Validation | Role |
|----------|------------|------|
| `app.settings.title` | `@NotBlank` | Application title shown in `/api/info` and the Swagger header (dev appends "(DEV)") |
| `app.settings.pagination-limit` | `@Min(1) @Max(500)` | Caps the number of students returned by `GET /api/students` (10 in dev, 100 in prod) |
| `app.settings.contact-email` | `@NotBlank @Email` | Contact e-mail published in `/api/info` and Swagger |
| `app.settings.show-environment-info` | — (feature flag) | When `true` (dev only), `/api/info` also returns active profiles and Java version |

Injected into `AppInfoController`, `StudentServiceImpl` and `OpenApiConfig`.

## Internationalization (i18n)

Locale is resolved from the standard **`Accept-Language`** HTTP header
(`AcceptHeaderLocaleResolver`). Supported: `en` (default) and `ka` (Georgian).
Bundles: `messages.properties`, `messages_en.properties`, `messages_ka.properties` (UTF-8).

Localized responses:
- `GET /api/info` — welcome message
- `POST /api/auth/logout` — success message
- All error payloads from the `GlobalExceptionHandler`: 404 "not found", 409 "already exists",
  400 "validation failed" and the per-field validation messages (DTO constraints reference
  bundle keys via `{key}`, e.g. `@NotBlank(message = "{validation.student.firstname.required}")`)

Test it:

```bash
# English (default)
curl http://localhost:8080/api/info
curl http://localhost:8080/api/students/9999

# Georgian
curl -H "Accept-Language: ka" http://localhost:8080/api/info
curl -H "Accept-Language: ka" http://localhost:8080/api/students/9999

# Localized validation errors (Georgian)
curl -X POST http://localhost:8080/api/auth/register \
     -H "Content-Type: application/json" -H "Accept-Language: ka" \
     -d "{\"username\":\"ab\",\"password\":\"123\"}"
```

## Testing

Run the full suite (unit + slice + integration tests) and generate the coverage report:

```bash
./mvnw test
# JaCoCo HTML report: target/site/jacoco/index.html
```

The suite (41 tests) covers positive **and** negative scenarios at every layer:

| Type | Location | Tooling |
|------|----------|---------|
| Unit tests | `service/*ServiceImplTest` | JUnit 5 + Mockito (mocked repositories), parameterized tests, `@BeforeEach` lifecycle, `SimpleMeterRegistry` for metrics |
| Repository slice | `repository/*RepositoryTest` | `@DataJpaTest` + `TestEntityManager` against embedded H2 |
| Controller slice | `controller/StudentControllerTest` | `@WebMvcTest` + MockMvc + Spring Security Test (`@WithMockUser`) — verifies routing, validation, 401/403/404 |
| Integration | `integration/StudentApiIntegrationTest` | `@SpringBootTest(RANDOM_PORT)` + `TestRestTemplate` — full HTTP round-trips incl. security and Actuator |

> Note (Spring Boot 4): the test slices are modular, so the build adds
> `spring-boot-starter-webmvc-test` and `spring-boot-starter-data-jpa-test` alongside
> `spring-boot-starter-test`.

## Monitoring (Actuator)

Spring Boot Actuator is enabled with a least-exposure configuration. Only three endpoints are
published over HTTP:

| Endpoint | Access | Description |
|----------|--------|-------------|
| `GET /actuator/health` | public | Liveness/readiness aggregate. Includes a **custom `studentDatabase` health indicator** reporting the live student count. Full details require authentication (`always` in dev, `never` in prod). |
| `GET /actuator/info` | public | App metadata, Git/Maven **build-info**, and Java/OS details. |
| `GET /actuator/metrics` | **ADMIN only** | Micrometer metrics, including custom meters: `app.students.total` / `app.courses.total` (gauges) and `app.students.created` / `app.courses.created` (counters). |

```bash
curl http://localhost:8080/actuator/health
curl http://localhost:8080/actuator/info
curl -u admin:admin123 http://localhost:8080/actuator/metrics/app.students.total
```

Endpoint access rules are enforced in `SecurityConfig` (health/info public, everything else under
`/actuator/**` restricted to `ADMIN`).

## Logging

SLF4J via Lombok `@Slf4j` with parameterized messages, used across services, controllers,
the exception handler and the data initializers.

Configured in `logback-spring.xml`:

- **Console + file:** colorized console output (Spring Boot's `CONSOLE_LOG_PATTERN`) plus a plain
  file appender at `logs/app.log` (relative to the working directory).
- **Async file logging:** the file appender is wrapped in an `AsyncAppender` so disk I/O never
  blocks request threads.
- **Rolling policy:** daily and at 10 MB per file → `logs/archive/app-YYYY-MM-DD.N.log.gz`,
  14 days / 200 MB total retention (`SizeAndTimeBasedRollingPolicy`).
- **Profile-specific levels** (via `<springProfile>`): dev (and any non-prod profile) → root
  `INFO`, `com.example.homework` at `DEBUG`; prod → root `WARN`, `com.example.homework` at `INFO`.
