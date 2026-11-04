# Employee MVC REST API

Spring Boot MVC application for creating, reading, and updating employee records.
Employee JSON fields are `id` (integer), `name`, `department`, `salary` (number),
and `age` (integer).

## Run

```bash
mvn spring-boot:run
```

The downstream base URL is configured with `employee.downstream.base-url` in
`src/main/resources/application.properties`. It is initially `TO_BE_DEFINE`.
Until a URL is configured, or when the downstream service is unavailable, the
API uses an in-memory local fallback. Fallback responses include
`X-Data-Source: local-fallback`; successful downstream responses include
`X-Data-Source: downstream`. Fallback data is not persisted across restarts.

The downstream service is expected to expose the same routes listed below.

## Endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| GET | `/api/employees?department=Engineering` | Read employees, optionally filtered by department |
| POST | `/api/employees` | Add an employee using a JSON request body |
| PUT | `/api/employees/{id}` | Update an employee; the path ID must match the body ID |

Example request body:

```json
{
  "id": 1,
  "name": "Ava Morgan",
  "department": "Engineering",
  "salary": 95000.0,
  "age": 30
}
```

Schema, business, and downstream operation errors are returned as JSON with
`timestamp`, `status`, `code`, `message`, and `path` fields.

Run the integration tests with:

```bash
mvn test
```
