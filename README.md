# JavaQualificationExam

A Java 21 and Spring Boot project-planning application for creating projects, defining dependent tasks, and automatically calculating project schedules.

The application provides two interfaces over the same service layer:

- A Thymeleaf browser interface for end users
- A secured REST API for API clients, Postman, and Swagger UI

## Features

- Create and view project plans
- Define a project start date
- Create tasks with durations measured in calendar days
- Assign dependencies between tasks
- Automatically calculate task start and end dates
- Schedule independent tasks in parallel
- Recalculate the complete schedule after task changes
- Calculate the overall project end date
- Delete tasks and projects
- Record successful operations and failed requests in an audit log
- Protect API endpoints with HTTP Basic authentication
- Generate OpenAPI 3 documentation and Swagger UI
- Persist application data in PostgreSQL
- Run isolated mapper unit tests and H2-backed scheduling integration tests
- Generate test coverage reports with JaCoCo

## Technology Stack

- Java 21
- Spring Boot 4.1.1
- Spring MVC
- Spring Security
- Spring Data JPA
- Hibernate
- Thymeleaf
- PostgreSQL
- H2 Database for tests
- Maven and Maven Wrapper
- JUnit Jupiter
- Logback
- Springdoc OpenAPI 3 and Swagger UI
- JaCoCo

## Architecture

```text
Browser or API client
        |
Spring Security filter chain
        |
MVC or REST controller
        |
Application service
        |
Business service
        |
Repository
        |
JPA and Hibernate
        |
PostgreSQL
```

### Main Packages

```text
com.qualification.exam
|-- api
|   |-- controller
|   |   |-- ProjectRestController
|   |   `-- TaskRestController
|   |-- exception
|   |   `-- ApiExceptionHandler
|   |-- mapper
|   |   |-- ProjectApiMapper
|   |   `-- TaskApiMapper
|   |-- request
|   |   |-- CreateProjectRequest
|   |   `-- CreateTaskRequest
|   `-- response
|       |-- ProjectResponse
|       `-- TaskResponse
|-- config
|   |-- ApiAuthenticationEntryPoint
|   |-- OpenApiConfig
|   `-- SecurityConfig
|-- controller
|-- dto
|-- entity
|-- exception
|-- repository
`-- service
```

### Responsibility Summary

- `controller`: Thymeleaf MVC controllers that return HTML views and redirects
- `api.controller`: REST controllers that consume and produce JSON
- `api.request`: API input contracts and validation rules
- `api.response`: Stable JSON response contracts
- `api.mapper`: Entity-to-response conversion
- `api.exception`: Centralized REST exception handling
- `config`: Security, authentication, and OpenAPI configuration
- `service`: Business operations, scheduling, transactions, and audit events
- `repository`: PostgreSQL access through Spring Data JPA
- `entity`: JPA persistence models

## Scheduling Rules

1. Durations are measured in calendar days.
2. Start and end dates are inclusive.
3. A task without dependencies starts on the project start date.
4. Independent tasks may run in parallel.
5. A dependent task starts one day after its latest dependency finishes.
6. The project end date is the latest calculated task end date.
7. A task cannot depend on itself.
8. Dependencies must belong to the same project.
9. Circular dependencies are rejected.

### Date Formulas

For an independent task:

```text
start date = project start date
end date   = start date + duration days - 1
```

For a dependent task:

```text
start date = latest dependency end date + 1
end date   = start date + duration days - 1
```

### Example

Project start date: `2026-09-20`

```text
Task A: 3 days, no dependencies
Task B: 5 days, no dependencies
Task C: 2 days, depends on Task A
Task D: 4 days, depends on Tasks A and B
```

Calculated schedule:

```text
Task A: 2026-09-20 to 2026-09-22
Task B: 2026-09-20 to 2026-09-24
Task C: 2026-09-23 to 2026-09-24
Task D: 2026-09-25 to 2026-09-28

Project end date: 2026-09-28
```

## Scheduling Algorithm

Task dependencies are modeled as a directed graph. The scheduling service uses Kahn's topological sorting algorithm.

```text
1. Calculate the in-degree of every task.
2. Add all zero-in-degree tasks to a ready queue.
3. Schedule each ready task.
4. Reduce the in-degree of its dependent tasks.
5. Add newly released tasks to the ready queue.
6. Continue until the queue is empty.
```

If the queue becomes empty while unscheduled tasks remain, the graph contains a circular dependency or tasks blocked by one. The service throws `CircularDependencyException`, and the transaction rolls back.

The current create-only workflow normally produces an acyclic graph because new tasks can depend only on existing tasks. Cycle detection remains a defensive safeguard for tests, imports, direct database changes, and future task-update endpoints.

## Database Model

The application uses three principal tables:

```text
project_plan
project_task
task_dependency
```

### Relationships

```text
ProjectPlan 1 ---- * ProjectTask
ProjectTask  * ---- * ProjectTask dependencies
```

The `task_dependency` join table stores:

```text
task_id
dependency_task_id
```

Task keys are unique within a project.

Both principal entities use a JPA `@Version` field for optimistic locking. Hibernate increments the version after successful updates and rejects stale concurrent updates rather than silently overwriting newer data.

## Browser Interface

The Thymeleaf interface is available at:

```text
http://localhost:6767/projects
```

The browser interface supports project and task creation, dependency selection, calculated schedule display, and deletion operations.

## REST API

Base URL:

```text
http://localhost:6767/api
```

### Project Endpoints

```text
GET    /api/projects
POST   /api/projects
GET    /api/projects/{projectId}
DELETE /api/projects/{projectId}
```

### Task Endpoints

```text
GET    /api/projects/{projectId}/tasks
POST   /api/projects/{projectId}/tasks
GET    /api/projects/{projectId}/tasks/{taskId}
DELETE /api/projects/{projectId}/tasks/{taskId}
```

### Create a Project

```http
POST /api/projects
Content-Type: application/json
Authorization: Basic <credentials>
```

```json
{
  "name": "API Project",
  "startDate": "2026-09-20"
}
```

Expected status:

```text
201 Created
```

### Create a Task

```http
POST /api/projects/{projectId}/tasks
Content-Type: application/json
Authorization: Basic <credentials>
```

```json
{
  "taskKey": "A",
  "name": "Gather Requirements",
  "durationDays": 3,
  "dependencyIds": []
}
```

A dependent task uses existing task database IDs:

```json
{
  "taskKey": "B",
  "name": "Design Solution",
  "durationDays": 2,
  "dependencyIds": [10]
}
```

## API Authentication

All `/api/**` routes require HTTP Basic authentication and the `API_USER` role.

The Thymeleaf interface and OpenAPI documentation remain publicly accessible in the current configuration.

Configuration uses environment variables with local fallback values:

```properties
app.security.username=${API_USERNAME:examuser}
app.security.password=${API_PASSWORD:change-me}
```

Set secure values before running outside local development:

```bat
set "API_USERNAME=examuser"
set "API_PASSWORD=replace-with-a-secure-password"
```

Database credentials are also supplied through environment variables and must not be committed to source control.

> HTTP Basic credentials must be protected by HTTPS in a production deployment.

## Exception Handling

`ApiExceptionHandler` is a `@RestControllerAdvice` limited to the REST controller package. It converts exceptions into consistent JSON responses and records failed requests in the audit log.

```text
ResourceNotFoundException       -> 404 Not Found
InvalidProjectPlanException     -> 400 Bad Request
CircularDependencyException     -> 409 Conflict
MethodArgumentNotValidException -> 400 Bad Request
HttpMessageNotReadableException -> 400 Bad Request
Unexpected Exception            -> 500 Internal Server Error
```

Authentication failures occur before controller execution and are therefore handled separately by `ApiAuthenticationEntryPoint`, which returns a JSON `401 Unauthorized` response and records the failed authentication attempt.

## Audit Logging

Audit events are written to:

```text
logs/audit.txt
```

The audit log records successful business operations and failed requests, including:

```text
CREATE_PROJECT
CREATE_TASK
DELETE_TASK
DELETE_PROJECT
HTTP_REQUEST failures
AUTHENTICATION failures
```

User-controlled values are sanitized before logging to reduce log-forging risk.

The `logs/` directory is excluded from Git.

## OpenAPI 3 and Swagger UI

Interactive API documentation:

```text
http://localhost:6767/swagger-ui.html
```

OpenAPI JSON:

```text
http://localhost:6767/v3/api-docs
```

OpenAPI YAML:

```text
http://localhost:6767/v3/api-docs.yaml
```

`OpenApiConfig` contributes API metadata and the HTTP Basic security scheme. Springdoc combines that configuration with the REST controller mappings, request records, response records, validation annotations, and OpenAPI annotations.

Swagger UI complements the Thymeleaf interface. Thymeleaf is the end-user interface, while Swagger UI is developer-facing API documentation and an interactive API client.

## Local Setup

### Prerequisites

- Java 21 JDK
- PostgreSQL
- Git
- A terminal or Spring Tools for Eclipse

### Clone

```bash
git clone https://github.com/YOUR_USERNAME/JavaQualificationExam.git
cd JavaQualificationExam
```

### Configure Environment Variables on Windows

```bat
set "DB_USERNAME=your_database_user"
set "DB_PASSWORD=your_database_password"
set "API_USERNAME=examuser"
set "API_PASSWORD=your_secure_api_password"
```

### Run with Maven Wrapper

Windows:

```bat
mvnw.cmd spring-boot:run
```

Linux or macOS:

```bash
./mvnw spring-boot:run
```

The application starts on:

```text
http://localhost:6767
```

## Build the Executable JAR

Windows:

```bat
mvnw.cmd clean package
```

Linux or macOS:

```bash
./mvnw clean package
```

Generated JAR:

```text
target/java-qualification-exam-0.0.1-SNAPSHOT.jar
```

Run it with:

```bat
java -jar target\java-qualification-exam-0.0.1-SNAPSHOT.jar
```

## Testing

The project uses JUnit Jupiter.

### Unit Tests

The API mapper tests run without Spring or a database:

```text
TaskApiMapperTest
ProjectApiMapperTest
```

These verify entity-to-response conversion using the Arrange, Act, Assert pattern.

Run only mapper unit tests:

```bat
mvnw.cmd clean test "-Dtest=*ApiMapperTest"
```

### Integration Tests

The scheduling tests use Spring Boot, JPA, Hibernate, and an isolated H2 test database:

```text
JavaQualificationExamApplicationTests
ProjectSchedulingServiceTest
```

The scheduling suite covers:

- One-day tasks
- Independent parallel tasks
- Single dependencies
- Multiple dependencies
- Dependency chains
- Project end-date calculation
- Empty projects
- Invalid durations
- Self-dependencies
- Circular dependencies
- Recalculation of existing schedules

Run the complete suite:

```bat
mvnw.cmd clean test
```

### Current Test Status

At the time of this README update:

- The 3 API mapper unit tests pass.
- The Spring context-based tests currently require resolution of an application-context startup error introduced during the security and API configuration changes.
- The repeated `ApplicationContext failure threshold exceeded` messages are consequences of the first context startup failure, not independent scheduling assertion failures.

The first meaningful `Caused by:` entry in the Surefire report should be used to diagnose the test-context issue.

## JaCoCo Coverage

JaCoCo is integrated through the Maven build.

Run mapper tests and generate coverage:

```bat
mvnw.cmd clean test "-Dtest=*ApiMapperTest"
```

Open the generated report:

```bat
start target\site\jacoco\index.html
```

The report includes instruction, branch, line, method, class, and complexity coverage.

JUnit verifies expected behavior through assertions. JaCoCo measures which production-code paths were executed. Coverage alone does not prove correctness.

## Source Control Safety

The repository excludes:

```text
target/
logs/
.env files
Eclipse and STS metadata
operating-system metadata
```

Do not commit:

- Database passwords
- API passwords
- Personal access tokens
- Authorization headers
- Generated build output
- Runtime logs

## Known Limitations and Future Enhancements

- Add project and task update endpoints using `PUT` or `PATCH`
- Make circular-dependency validation reachable through authenticated task updates
- Add REST controller and security tests using MockMvc
- Add Mockito unit tests for business services
- Resolve the current Spring test-context startup issue
- Add database-backed user management or an external identity provider
- Add authorization roles beyond one API user
- Add Flyway database migrations
- Add working-day and holiday calendars
- Add critical-path analysis and Gantt visualization
- Add deployment configuration and HTTPS

## License

No license has been selected yet.
