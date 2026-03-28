# Task Management Service

A RESTful backend service for managing tasks, built with Kotlin, Spring Boot, Project Reactor, and JdbcClient.

## Tech Stack

- Kotlin 2.1
- Spring Boot 3.4
- Spring WebFlux (Netty)
- Project Reactor (Mono/Flux)
- Spring JDBC — JdbcClient with native SQL
- Flyway — database migrations
- H2 — in-memory database
- JUnit 5 + Mockito Kotlin — unit tests

## Project Structure

```
src/main/kotlin/com/example/taskmanagement/
├── controller/         # HTTP layer — TaskController
├── service/            # Business logic — TaskService
├── repository/         # TaskRepository (JdbcClient + native SQL), TaskRowMapper
├── model/              # Task entity, TaskStatus enum
├── dto/                # TaskRequest, TaskResponse, PageResponse
├── exception/          # GlobalExceptionHandler (@RestControllerAdvice)
└── TaskManagementApplication.kt

src/test/kotlin/com/example/taskmanagement/
├── controller/         # TaskControllerTest (WebTestClient)
├── service/            # TaskServiceTest (mocked repository)
└── repository/         # TaskRepositoryTest (mocked JdbcClient), TaskRowMapperTest
```

## Domain Model

### Task

| Field         | Type          | Description                        |
|---------------|---------------|------------------------------------|
| id            | Long          | Auto-generated primary key         |
| title         | String        | 3–100 characters, required         |
| description   | String?       | Optional                           |
| status        | TaskStatus    | Defaults to NEW                    |
| createdAt     | LocalDateTime | Set on creation                    |
| updatedAt     | LocalDateTime | Updated on status change           |

### TaskStatus

| Value       |
|-------------|
| NEW         |
| IN_PROGRESS |
| DONE        |
| CANCELLED   |

## API Reference

### Create a task

```
POST /api/tasks
Content-Type: application/json

{
  "title": "Prepare report",
  "description": "Monthly financial report"
}
```

Response `201 Created`:
```json
{
  "id": 1,
  "title": "Prepare report",
  "description": "Monthly financial report",
  "status": "NEW",
  "createdAt": "2026-03-28T12:00:00",
  "updatedAt": "2026-03-28T12:00:00"
}
```

---

### Get task by ID

```
GET /api/tasks/{id}
```

Response `200 OK` or `404 Not Found`.

---

### Get task list (paginated + filtered)

```
GET /api/tasks?page=0&size=10&status=NEW
```

- `page` — required, min 0
- `size` — required, min 1
- `status` — optional filter (`NEW`, `IN_PROGRESS`, `DONE`, `CANCELLED`)
- Sorted by `createdAt` descending

Response `200 OK`:
```json
{
  "content": [...],
  "page": 0,
  "size": 10,
  "totalElements": 1,
  "totalPages": 1
}
```

---

### Update task status

```
PATCH /api/tasks/{id}/status
Content-Type: application/json

{
  "status": "IN_PROGRESS"
}
```

Response `200 OK` with updated task. Also updates `updatedAt`.
Returns `404 Not Found` if task does not exist.

---

### Delete a task

```
DELETE /api/tasks/{id}
```

Response `204 No Content`.
Returns `404 Not Found` if task does not exist.

---

## Error Response Format

All errors return a consistent JSON body:

```json
{
  "status": 400,
  "message": "title: Title must not be blank"
}
```

## Running the Application

### Prerequisites

- JDK 25 (Temurin or any distribution)
- No additional setup needed — H2 in-memory database is used

### Start the server

```bash
./gradlew bootRun
```

The server starts on `http://localhost:8080`.

### H2 Console (optional)

Available at `http://localhost:8080/h2-console`

- JDBC URL: `jdbc:h2:mem:tasksdb`
- Username: `sa`
- Password: *(empty)*

## Running Tests

```bash
./gradlew test
```

Test report is generated at `build/reports/tests/test/index.html`.

### Test Coverage

| Layer      | Test Class              | Tests |
|------------|-------------------------|-------|
| Service    | TaskServiceTest         | 9     |
| Controller | TaskControllerTest      | 10    |
| Repository | TaskRepositoryTest      | 8     |
| Mapper     | TaskRowMapperTest       | 3     |

**Service tests** — pure unit tests with mocked `TaskRepository`, using `StepVerifier` to assert reactive streams.

**Controller tests** — slice tests with `@WebFluxTest` and `WebTestClient`, verifying HTTP status codes, response bodies, and validation.

**Repository tests** — unit tests with mocked `JdbcClient`, verifying SQL queries and parameter bindings.

**Mapper tests** — isolated unit tests for `TaskRowMapper`, verifying correct mapping of all fields, nullable `description`, and all `TaskStatus` enum values.

## Example curl Commands

```bash
# Create
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{"title": "Prepare report", "description": "Monthly financial report"}'

# Get by ID
curl http://localhost:8080/api/tasks/1

# List with pagination
curl "http://localhost:8080/api/tasks?page=0&size=10"

# List filtered by status
curl "http://localhost:8080/api/tasks?page=0&size=10&status=NEW"

# Update status
curl -X PATCH http://localhost:8080/api/tasks/1/status \
  -H "Content-Type: application/json" \
  -d '{"status": "IN_PROGRESS"}'

# Delete
curl -X DELETE http://localhost:8080/api/tasks/1
```
