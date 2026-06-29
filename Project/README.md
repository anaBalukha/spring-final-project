# Student-Course Management API

A Spring Boot REST API for managing students, courses, and application users.

The project demonstrates a layered architecture:

```text
Controller → Service → Repository → Database
```

It includes CRUD operations, DTOs, validation, global exception handling, authentication and authorization, Spring profiles, internationalization, structured logging, automated testing, Swagger/OpenAPI documentation, and Spring Boot Actuator monitoring.

---

## Technologies Used

| Area                 | Technology                                                                 |
| -------------------- | -------------------------------------------------------------------------- |
| Language             | Java 21                                                                    |
| Build tool           | Maven with Maven Wrapper                                                   |
| Framework            | Spring Boot 4                                                              |
| Web                  | Spring Web MVC                                                             |
| Persistence          | Spring Data JPA and Hibernate                                              |
| Development database | H2 in-memory database                                                      |
| Production database  | PostgreSQL                                                                 |
| Security             | Spring Security with HTTP Basic Authentication                             |
| Validation           | Jakarta Bean Validation                                                    |
| API documentation    | Springdoc OpenAPI and Swagger UI                                           |
| Monitoring           | Spring Boot Actuator and Micrometer                                        |
| Logging              | SLF4J and Logback                                                          |
| Testing              | JUnit 5, Mockito, AssertJ, MockMvc, TestRestTemplate, Spring Security Test |
| Code coverage        | JaCoCo                                                                     |
| Utilities            | Lombok                                                                     |

---

## Main Features

* Layered Controller, Service, and Repository architecture
* Student CRUD operations
* Course CRUD operations
* User CRUD operations
* Student-to-Course one-to-many relationship
* Request and response DTOs
* Request validation
* Localized validation and error messages
* Global exception handling
* HTTP Basic authentication
* USER and ADMIN roles
* Development and production profiles
* Externalized configuration
* Swagger/OpenAPI documentation
* Console and rolling file logging
* Unit, controller, repository, and integration tests
* Spring Boot Actuator monitoring
* Custom health indicator
* Custom Micrometer metrics

---

## Prerequisites

The following software is required:

* Java Development Kit 21
* Git, if the project is cloned from a repository
* PostgreSQL only when running the prod profile

The project includes Maven Wrapper, so a separate Maven installation is not required.

Verify Java:
```
java -version
```

The output should show Java 21.

---

## Running the Application

### Windows

Run the application using the default development profile:

.\mvnw.cmd spring-boot:run

Run all tests:

.\mvnw.cmd clean test

Build the executable JAR:

.\mvnw.cmd clean package

Run the packaged JAR:

java -jar target/Homework-0.0.1-SNAPSHOT.jar

### macOS or Linux

Run the application:

./mvnw spring-boot:run

Run all tests:

./mvnw clean test

Build the executable JAR:

./mvnw clean package

Run the packaged JAR:

java -jar target/Homework-0.0.1-SNAPSHOT.jar

The application starts on:

```text
http://localhost:8080
```

Opening the root address redirects to Swagger UI.

Swagger UI is also available directly at:

```text
http://localhost:8080/swagger-ui/index.html
```
The OpenAPI document is available at:

```text
http://localhost:8080/api-docs
```
---

## Development Credentials

The following demonstration accounts are created automatically only when the dev profile is active.

### Administrator

text
Username: admin
Password: admin123
Role: ADMIN

### Standard user

text
Username: user
Password: user123
Role: USER

These credentials are intended only for local development and testing.

The default accounts are not created in the production profile.

---

## Authentication

The application uses HTTP Basic Authentication.

In Swagger UI:

1. Click *Authorize*.
2. Enter the username.
3. Enter the password.
4. Click *Authorize* again.
5. Close the authorization window.

For command-line requests:

curl -u admin:admin123 http://localhost:8080/api/auth/me

The login endpoint can also validate credentials:

POST /api/auth/login

Because the API uses stateless HTTP Basic Authentication, clients must send credentials with every protected request.

The logout endpoint clears the current Spring Security context. The client must also stop sending or remove its stored Basic Authentication credentials.

---

## Registration Security

Public account registration is available through:

POST /api/auth/register

Public registration always creates a user with the USER role.

A client cannot create an administrator through the public registration endpoint, even if it submits:
```
{
"username": "example",
"password": "secret123",
"role": "ADMIN"
}
```

The resulting account is still created as:
```
{
"role": "USER"
}
```

Administrator accounts can be managed only through protected ADMIN endpoints.

---

## Roles and Permissions

### USER

A user can:

* View students
* View courses
* Create students
* Update students
* Create courses
* Update courses
* View their own user profile
* Use the logout endpoint

### ADMIN

An administrator can perform everything available to a USER and can also:

* Delete students
* Delete courses
* View all registered users
* Get a registered user by ID
* Update registered users
* Delete registered users
* Access Actuator metrics

---

## API Endpoints

### Public Endpoints

No authentication is required.

#### Application information

GET /api/info

#### Students

GET /api/students
GET /api/students/{id}

#### Courses

GET /api/courses
GET /api/courses/{id}
GET /api/courses/student/{studentId}

#### Authentication

POST /api/auth/register
POST /api/auth/login

#### Documentation

GET /swagger-ui/index.html
GET /api-docs

#### Monitoring

GET /actuator/health
GET /actuator/info

---

### USER or ADMIN Endpoints

Authentication is required.

#### Students

POST /api/students
PUT /api/students/{id}

#### Courses

POST /api/courses
PUT /api/courses/{id}

#### Current account

GET /api/auth/me
POST /api/auth/logout

GET /api/auth/me returns only the currently authenticated user's profile.

---

### ADMIN-Only Endpoints

Administrator authentication is required.

#### Students

DELETE /api/students/{id}

#### Courses

DELETE /api/courses/{id}

#### User management

GET /api/auth/users
GET /api/auth/users/{id}
PUT /api/auth/users/{id}
DELETE /api/auth/users/{id}

The /api/auth/users/** endpoints are ADMIN-only because they expose or modify registered application accounts.

Normal users can still access their own profile through:

GET /api/auth/me

#### Monitoring

GET /actuator/metrics
GET /actuator/metrics/{metricName}

---

## Example Requests

### Register a user
```
curl -X POST http://localhost:8080/api/auth/register \
-H "Content-Type: application/json" \
-d '{
"username": "newuser",
"password": "secret123"
}'
```

### Get all students
```
curl http://localhost:8080/api/students
```

### Create a student
```
curl -X POST http://localhost:8080/api/students \
-u user:user123 \
-H "Content-Type: application/json" \
-d '{
"firstName": "Nino",
"lastName": "Beridze",
"email": "nino@example.com"
}'
```

### Create a course
```
curl -X POST http://localhost:8080/api/courses \
-u user:user123 \
-H "Content-Type: application/json" \
-d '{
"name": "Spring Boot",
"description": "Introduction to Spring Boot",
"studentId": 1
}'
```

### Get the current user's profile
```
curl -u user:user123 \
http://localhost:8080/api/auth/me
```

### Get all registered users as ADMIN
```
curl -u admin:admin123 \
http://localhost:8080/api/auth/users
```

### Update a registered user as ADMIN
```
curl -X PUT http://localhost:8080/api/auth/users/2 \
-u admin:admin123 \
-H "Content-Type: application/json" \
-d '{
"username": "updatedUser",
"password": null,
"role": "USER"
}'
```

A null password keeps the user's current password unchanged.

---

## Entity Relationship

The project contains the following primary domain entities:

* Student
* Course
* User

A student can have multiple courses.

```text
Student 1 ─────── * Course
```

The relationship is implemented as:

* @OneToMany in Student
* @ManyToOne in Course

Deleting a student also removes the student's related courses according to the configured cascade behavior.

---

## DTOs

JPA entities are not returned directly from REST controllers.

The application uses separate request and response DTOs, including:

### Request DTOs

* StudentRequest
* CourseRequest
* RegisterRequest
* LoginRequest
* UserUpdateRequest

### Response DTOs

* StudentResponse
* CourseResponse
* UserResponse
* ErrorResponse

This prevents persistence implementation details and sensitive fields such as encoded passwords from being exposed through the API.

---

## Validation

Request DTOs use Jakarta validation annotations such as:

* @NotNull
* @NotBlank
* @Size
* @Email

Controllers use @Valid on request bodies.

Example invalid request:
```
{
"firstName": "",
"lastName": "",
"email": "invalid-email"
}
```

The application returns a structured 400 Bad Request response instead of crashing.

Example:
```
{
"status": 400,
"message": "Validation failed",
"errors": [
"firstName: First name is required",
"email: Email must be a valid address"
],
"timestamp": "2026-06-29T12:00:00"
}
```

---

## Exception Handling

GlobalExceptionHandler provides consistent responses for:

* Validation failures
* Missing resources
* Duplicate usernames
* Duplicate student emails
* Invalid JSON request bodies
* Invalid path parameter types
* Authentication failures
* Authorization failures
* Database constraint conflicts
* Unknown endpoints
* Unexpected application errors

Typical status codes:

| Situation                     |               HTTP status |
| ----------------------------- | ------------------------: |
| Invalid request body          |           400 Bad Request |
| Invalid username or password  |          401 Unauthorized |
| Insufficient permission       |             403 Forbidden |
| Resource not found            |             404 Not Found |
| Duplicate or conflicting data |              409 Conflict |
| Unexpected server error       | 500 Internal Server Error |

Unknown addresses return 404 Not Found instead of being incorrectly reported as server errors.

---

## Profiles

The project supports development and production profiles.

Configuration files:

| File                          | Purpose                                                 |
| ----------------------------- | ------------------------------------------------------- |
| application.properties      | Common configuration and default profile                |
| application-dev.properties  | H2 database, development logging, H2 console, test data |
| application-prod.properties | PostgreSQL and production-specific settings             |

If no profile is specified, the application uses dev.

properties
spring.profiles.default=dev

---

## Development Profile

Run explicitly with the development profile:

### Windows
```
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
```

### macOS or Linux
```
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

The development profile uses an in-memory H2 database.

The database is recreated when the application restarts.

H2 Console:

```text
http://localhost:8080/h2-console
```

Connection settings:

```text
JDBC URL: jdbc:h2:mem:homework_db
Username: sa
Password: leave empty
```

The development profile creates demonstration students, courses, and users.

---

## Production Profile

The production profile uses PostgreSQL.

Create the database first:

CREATE DATABASE homework_db;

Production database credentials are not stored directly in the project.

The following environment variables are used:

```text
DB_URL
DB_USERNAME
DB_PASSWORD
```

### PowerShell example
```
$env:DB_URL="jdbc:postgresql://localhost:5432/homework_db"
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="your-secure-password"
```
```
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=prod"
```

### macOS or Linux example

export DB_URL="jdbc:postgresql://localhost:5432/homework_db"
export DB_USERNAME="postgres"
export DB_PASSWORD="your-secure-password"

```
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

Run the packaged application with the production profile:

java -jar target/Homework-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod

Default development accounts are not created in production.

---

## Externalized Configuration

Custom configuration properties use the prefix:

properties
app.settings

They are mapped to AppSettingsProperties using @ConfigurationProperties.

| Property                             | Purpose                                                    |
| ------------------------------------ | ---------------------------------------------------------- |
| app.settings.title                 | Application title shown in Swagger and /api/info         |
| app.settings.contact-email         | Contact email shown in API documentation                   |
| app.settings.show-environment-info | Controls whether environment details appear in /api/info |

The properties are validated during startup.

Examples of validation include:

* @NotBlank
* @Email

Invalid configuration prevents the application from starting with an incorrect state.

The student list does not use an artificial configurable limit. GET /api/students returns all stored students as required by the CRUD specification.

---

## Internationalization

The application supports:

* English (en)
* Georgian (ka)

English is the default language.

The locale is selected using the standard Accept-Language request header.

Message bundles:

```text
src/main/resources/messages.properties
src/main/resources/messages_en.properties
src/main/resources/messages_ka.properties
```

Localized content includes:

* Application welcome messages
* Logout responses
* Validation messages
* Resource-not-found messages
* Duplicate-resource messages
* Authentication errors
* General API errors

### English request
```
curl -H "Accept-Language: en" \
http://localhost:8080/api/info
```

### Georgian request
```
curl -H "Accept-Language: ka" \
http://localhost:8080/api/info
```

### Georgian validation error
```
curl -X POST http://localhost:8080/api/auth/register \
-H "Content-Type: application/json" \
-H "Accept-Language: ka" \
-d '{
"username": "ab",
"password": "123"
}'
```

---

## Swagger and OpenAPI

Swagger UI displays all REST endpoints and their short descriptions.

Open Swagger:

```text
http://localhost:8080/swagger-ui/index.html
```

The OpenAPI JSON document is available at:

```text
http://localhost:8080/api-docs
```

HTTP Basic Authentication is configured in the OpenAPI specification.

Protected operations display a lock icon and can be tested using Swagger's *Authorize* button.

Recommended development credentials:

```text
admin / admin123
```

---

## Testing

Run the complete test suite:

### Windows

.\mvnw.cmd clean test

### macOS or Linux

./mvnw clean test

The test suite includes positive and negative scenarios.

| Test type         | Purpose                                           | Main tools                          |
| ----------------- | ------------------------------------------------- | ----------------------------------- |
| Unit tests        | Test service business logic in isolation          | JUnit 5, Mockito, AssertJ           |
| Controller tests  | Test routing, validation, responses, and security | @WebMvcTest, MockMvc              |
| Repository tests  | Test JPA repositories and database constraints    | @DataJpaTest, H2                  |
| Integration tests | Test complete HTTP request flows                  | @SpringBootTest, TestRestTemplate |

The tests cover cases such as:

* Successful student creation
* Duplicate student email
* Student not found
* Successful course creation
* Invalid student reference
* Public and protected endpoint access
* USER and ADMIN permissions
* Public registration always assigning the USER role
* Invalid login returning 401
* Unknown URLs returning 404
* User CRUD operations
* Actuator endpoint security
* OpenAPI Basic Authentication configuration

Parameterized tests and lifecycle methods are also used where appropriate.

---

## Code Coverage

JaCoCo is configured in the Maven build.

Run tests and generate the report:

.\mvnw.cmd clean test

Open the generated report:

```text
target/site/jacoco/index.html
```

The report shows package, class, method, line, instruction, and branch coverage.

---

## Monitoring

Spring Boot Actuator is enabled.

Only the required monitoring endpoints are exposed over HTTP.

| Endpoint                   | Access     | Description                                  |
| -------------------------- | ---------- | -------------------------------------------- |
| /actuator/health         | Public     | Application and database health              |
| /actuator/info           | Public     | Application, build, Java, and OS information |
| /actuator/metrics        | ADMIN only | Available Micrometer metrics                 |
| /actuator/metrics/{name} | ADMIN only | Details for a specific metric                |

### Health

curl http://localhost:8080/actuator/health

Example response:
```
{
"status": "UP"
}
```

The project includes a custom student database health indicator that reports whether student data can be accessed.

### Info

curl http://localhost:8080/actuator/info

### Metrics

curl -u admin:admin123 \
http://localhost:8080/actuator/metrics

Example custom metric:

curl -u admin:admin123 \
http://localhost:8080/actuator/metrics/app.students.total

Custom metrics include:

```text
app.students.total
app.courses.total
app.students.created
app.courses.created
```

Actuator security is configured in SecurityConfig:

* Health and info are public.
* Metrics and other Actuator endpoints require the ADMIN role.

---

## Logging

The application uses SLF4J through Lombok's @Slf4j.

Logging statements use parameterized messages:

log.info("Student created: id={}, email={}", student.getId(), student.getEmail());

This avoids unnecessary string concatenation.

Logging is configured in:

```text
src/main/resources/logback-spring.xml
```

The configuration includes:

* Console logging
* File logging
* Asynchronous file logging
* Parameterized messages
* Different logging levels
* Daily log rotation
* Size-based log rotation
* Compressed archived logs
* Retention limits
* Profile-specific logging levels

Current log file:

```text
logs/app.log
```

Archived logs:

```text
logs/archive/
```

Rolling behavior:

* A new archive is created daily.
* A file rolls when it reaches the configured maximum size.
* Old archives are automatically removed according to retention settings.

Development logging:

```text
Root: INFO
com.example.homework: DEBUG
```

Production logging:

```text
Root: WARN
com.example.homework: INFO
```

---

## Project Structure

text
src
├── main
│   ├── java
│   │   └── com.example.homework
│   │       ├── config
│   │       ├── controller
│   │       ├── dto
│   │       │   ├── request
│   │       │   └── response
│   │       ├── entity
│   │       ├── exception
│   │       ├── monitoring
│   │       ├── repository
│   │       ├── security
│   │       ├── service
│   │       │   └── impl
│   │       └── HomeworkApplication.java
│   └── resources
│       ├── application.properties
│       ├── application-dev.properties
│       ├── application-prod.properties
│       ├── logback-spring.xml
│       ├── messages.properties
│       ├── messages_en.properties
│       └── messages_ka.properties
└── test
└── java
└── com.example.homework
├── controller
├── integration
├── repository
└── service

---

## Building the Final Submission

Before creating the final ZIP, run:

.\mvnw.cmd clean test

The command should end with:

```text
BUILD SUCCESS
```

Then run:

.\mvnw.cmd clean package

The command should also end with:

```text
BUILD SUCCESS
```

Verify the following before submission:

* The application starts successfully with Java 21.
* All tests pass.
* Swagger UI opens.
* Swagger shows the Authorize button.
* Public registration always creates a USER.
* A USER cannot access administrator endpoints.
* An ADMIN can use all User CRUD endpoints.
* Invalid login returns 401.
* Unknown resources return 404.
* Duplicate data returns 409.
* Actuator health reports UP.
* Metrics require an administrator.
* The production properties file contains no real password.
* The README instructions match the current implementation.
* The complete runnable project is included in the ZIP.

---

## Author
Ana Balukhashvili

Student-Course Management API

Spring Boot final project