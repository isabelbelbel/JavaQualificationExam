# JavaQualificationExam

A Maven-based Spring Boot application that calculates calendar schedules for project plans.

Each project plan contains tasks with durations and dependencies. The application automatically calculates a start date and end date for every task while ensuring that dependent tasks start only after all predecessor tasks have completed.

## Features

- Create and view project plans
- Add tasks to a project plan
- Assign task durations in calendar days
- Assign zero or more dependencies to each task
- Automatically calculate task start and end dates
- Schedule independent tasks in parallel
- Support tasks with multiple dependencies
- Detect circular dependencies
- Calculate the overall project end date
- Delete individual tasks
- Automatically recalculate the schedule after task changes
- Delete complete project plans and their tasks
- Persist projects, tasks, dependencies, and calculated dates
- Write audit events to a text file
- Validate project and task input
- Run automated tests using an isolated H2 database

## Technology Stack

- Java 21
- Spring Boot 4.1.1
- Maven
- Spring MVC
- Thymeleaf
- Spring Data JPA
- Hibernate
- PostgreSQL
- H2 Database for automated tests
- JUnit 5
- Logback
- HTML
- CSS

## Scheduling Rules

The scheduling engine applies the following rules:

1. Durations are measured in calendar days.
2. Start and end dates are inclusive.
3. A task without dependencies starts on the project start date.
4. A dependent task starts one day after its latest dependency finishes.
5. Independent tasks may run in parallel.
6. Task duration must be at least one day.
7. A task cannot depend on itself.
8. Dependencies must belong to the same project.
9. Circular dependencies are rejected.
10. The project end date is the latest calculated task end date.

For a task without dependencies:

```text
startDate = projectStartDate
endDate = startDate + durationDays - 1
```

For a task with dependencies:

```text
startDate = latest dependency end date + 1
endDate = startDate + durationDays - 1
```

### Example

Given a project starting on `2026-09-10`:

```text
Task A: 3 days, no dependencies
Task B: 5 days, no dependencies
Task C: 2 days, depends on A
Task D: 4 days, depends on A and B
```

The calculated schedule is:

```text
Task A: 2026-09-10 to 2026-09-12
Task B: 2026-09-10 to 2026-09-14
Task C: 2026-09-13 to 2026-09-14
Task D: 2026-09-15 to 2026-09-18
```

The calculated project end date is:

```text
2026-09-18
```

## Scheduling Algorithm

The application models task dependencies as a directed graph and uses Kahn's topological sorting algorithm.

The scheduling process:

1. Loads all tasks belonging to a project.
2. Validates task durations and dependency relationships.
3. Calculates the in-degree of every task.
4. Places tasks with no unresolved dependencies into a queue.
5. Processes tasks in topological order.
6. Calculates the start and end dates of each task.
7. Decreases the in-degree of dependent tasks.
8. Adds newly available tasks to the processing queue.
9. Detects a cycle if some tasks cannot be processed.
10. Saves the calculated task dates.
11. Sets the project end date to the latest task end date.

Approximate algorithm complexity:

```text
Time: O(V + E)
Space: O(V + E)
```

Where:

```text
V = number of tasks
E = number of dependency relationships
```

## Project Structure

```text
JavaQualificationExam/
├── pom.xml
├── README.md
├── mvnw
├── mvnw.cmd
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/qualification/exam/
│   │   │       ├── controller/
│   │   │       ├── dto/
│   │   │       ├── entity/
│   │   │       ├── exception/
│   │   │       ├── repository/
│   │   │       ├── service/
│   │   │       └── JavaQualificationExamApplication.java
│   │   └── resources/
│   │       ├── META-INF/
│   │       │   └── persistence.xml
│   │       ├── static/
│   │       │   └── css/
│   │       │       └── application.css
│   │       ├── templates/
│   │       │   ├── projects/
│   │       │   └── tasks/
│   │       ├── application.properties
│   │       └── logback-spring.xml
│   └── test/
│       ├── java/
│       │   └── com/qualification/exam/
│       │       ├── JavaQualificationExamApplicationTests.java
│       │       └── service/
│       │           └── ProjectSchedulingServiceTest.java
│       └── resources/
│           └── application-test.properties
└── logs/
    └── audit.txt
```

The `logs` and `target` directories are generated at runtime and should not be committed to Git.

## Database Model

The application uses three primary database tables.

### `project_plan`

Stores:

- Project ID
- Project name
- Project start date
- Calculated project end date
- Optimistic-lock version

### `project_task`

Stores:

- Task ID
- Project ID
- Task key
- Task name
- Duration in calendar days
- Calculated start date
- Calculated end date
- Optimistic-lock version

### `task_dependency`

Stores the many-to-many task dependency relationships:

```text
task_id -> dependency_task_id
```

## Prerequisites

The following software is required:

- Java 21
- Maven 3.9 or the included Maven Wrapper
- PostgreSQL
- A web browser

## Database Setup

Create a PostgreSQL database:

```sql
CREATE DATABASE java_qualification_exam;
```

The default database URL is:

```text
jdbc:postgresql://localhost:5432/java_qualification_exam
```

Database credentials are provided through environment variables and must not be committed to Git.

### Windows Command Prompt

```bat
set DB_USERNAME=your_postgresql_username
set DB_PASSWORD=your_postgresql_password
```

### Windows PowerShell

```powershell
$env:DB_USERNAME="your_postgresql_username"
$env:DB_PASSWORD="your_postgresql_password"
```

### Linux or macOS

```bash
export DB_USERNAME="your_postgresql_username"
export DB_PASSWORD="your_postgresql_password"
```

A custom database URL may optionally be supplied through:

```text
DB_URL
```

## Running the Application

### Maven Wrapper on Windows

```bat
mvnw.cmd spring-boot:run
```

### Maven Wrapper on Linux or macOS

```bash
./mvnw spring-boot:run
```

### Installed Maven

```bash
mvn spring-boot:run
```

The application runs at:

```text
http://localhost:6767
```

The root URL redirects to the project-plan list.

## Building the Application

Run:

```bash
mvn clean package
```

Or use the Maven Wrapper on Windows:

```bat
mvnw.cmd clean package
```

The executable JAR is generated at:

```text
target/java-qualification-exam-0.0.1-SNAPSHOT.jar
```

Run the packaged application using Java 21:

```bash
java -jar target/java-qualification-exam-0.0.1-SNAPSHOT.jar
```

If the system `java` command does not point to Java 21, use the full path to a Java 21 executable:

```bat
"C:\path\to\jdk-21\bin\java.exe" -jar target\java-qualification-exam-0.0.1-SNAPSHOT.jar
```

## Running the Tests

Run:

```bash
mvn clean test
```

Or use the Maven Wrapper on Windows:

```bat
mvnw.cmd clean test
```

The tests use an isolated H2 in-memory database configured through the Spring `test` profile. The tests do not modify the PostgreSQL development database.

The test suite covers:

- Spring application-context startup
- One-day task calculation
- Independent tasks running in parallel
- Single task dependencies
- Multiple dependencies
- Dependency chains
- Project end-date calculation
- Empty-project validation
- Invalid task durations
- Self-dependencies
- Circular dependencies
- Schedule recalculation after dependency changes

Expected result:

```text
Tests run: 12
Failures: 0
Errors: 0
Skipped: 0
BUILD SUCCESS
```

## Audit Log

Audit events are written to:

```text
logs/audit.txt
```

Audited actions include:

- Project creation
- Task creation
- Task deletion
- Project deletion
- Automatic schedule recalculation

Example audit event:

```text
2026-09-08 18:00:00 | action=CREATE_TASK | projectId=1 | taskId=4 | description=Created task D and recalculated the schedule
```

Audit values are sanitized to prevent project or task names from inserting extra lines into the audit log.

Audit files are rotated according to date and file size. Archived files are stored under:

```text
logs/archive/
```

Generated log files are excluded from Git.

## Configuration

The main application configuration is located at:

```text
src/main/resources/application.properties
```

The test configuration is located at:

```text
src/test/resources/application-test.properties
```

The JPA persistence-unit configuration is located at:

```text
src/main/resources/META-INF/persistence.xml
```

Sensitive values such as database passwords are externalized through environment variables.

## Validation

The application validates:

- Required project names
- Required project start dates
- Required task keys
- Required task names
- Task-key length
- Task-name length
- 