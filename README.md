# Springboot Academy – Course & Enrollment Management REST API

REST API for managing **students**, **courses** and **enrollments** at *Springboot Academy*.
Built with Spring Boot 3.3, Java 17, Spring Data JPA, MapStruct, ModelMapper, springdoc-openapi.

## Quick start

Requirements: **JDK 17+**, **Maven 3.9+**, and a local **MySQL server listening on port 3307** (or open the folder in IntelliJ / Eclipse / VS Code and run `SpringbootAcademyApplication`).

```bash
mvn clean install        # compiles, runs unit + integration tests
mvn spring-boot:run      # starts on http://localhost:8080
```

| URL | Purpose |
|---|---|
| http://localhost:8080/ | Redirects to Swagger UI |
| http://localhost:8080/swagger-ui.html | Swagger UI (Students / Courses / Enrollments) |
| http://localhost:8080/v3/api-docs | OpenAPI JSON |

By default the app uses MySQL at `localhost:3307/springboot_academy`. Database creation is enabled in the JDBC URL, and the configured credentials are in `application.properties`. The app inserts 3 demo courses and 3 demo students on startup (`app.seed-data=true`; set it to `false` to disable).

### Using the MySQL profile
The `mysql` profile uses `application-mysql.properties`. Adjust its credentials if needed, then run:
```bash
mvn spring-boot:run "-Dspring-boot.run.profiles=mysql"
```

## Project structure
```
com.springbootacademy.api
├── controller     StudentController, CourseController, EnrollmentController
├── service        StudentService, CourseService, EnrollmentService (interfaces)
│   └── impl       StudentServiceImpl, CourseServiceImpl, EnrollmentServiceImpl
├── repository     Student/Course/EnrollmentRepository (+ spec/ JPA Specifications for filters)
├── entity         Student, Course, Enrollment + enums (StudentStatus, CourseStatus, EnrollmentStatus)
├── dto            *RequestDTO, *ResponseDTO, StandardResponse<T>
├── mapper         MapStruct mappers + PageMapper (ModelMapper)
├── exception      ResourceNotFound / Business / DuplicateResource exceptions + GlobalExceptionHandler
└── config         OpenApiConfig, ModelMapperConfig, PaginationConfig, DataLoader
```

## Endpoints (all under `/api/v1`)

### Students
| Method | Path | Description |
|---|---|---|
| POST | `/students` | Create student (unique email) → 201 |
| GET | `/students/{id}` | Get by id |
| GET | `/students?page=0&size=10&sort=fullName,asc&search=john` | Paginated list, optional search on name **or** email |
| PUT | `/students/{id}` | Update |
| DELETE | `/students/{id}` | **Soft delete** → status = `INACTIVE` |

### Courses
| Method | Path | Description |
|---|---|---|
| POST | `/courses` | Create course (unique code, e.g. `SB-101`) → 201. `availableSeats` = `totalSeats` |
| GET | `/courses/{id}` | Get by id |
| GET | `/courses?page=0&size=10&sort=fee,desc&status=ACTIVE&minFee=100&maxFee=500` | Pagination + sorting + filters |
| PUT | `/courses/{id}` | Update (seat count is re-balanced, see below) |
| PATCH | `/courses/{id}/status` | Body `{"status":"INACTIVE"}` – activate / deactivate |

### Enrollments
| Method | Path | Description |
|---|---|---|
| POST | `/enrollments` | Body `{"studentId":1,"courseId":1,"paid":true}` → 201 |
| GET | `/enrollments?studentId=&courseId=&status=&page=&size=&sort=` | Paginated list with filters |
| GET | `/enrollments/{id}` | Details incl. basic student & course info |
| PATCH | `/enrollments/{id}/cancel` | Cancel (only if `ENROLLED`) |

### Business rules (enrollment)
* Student must be `ACTIVE`, course must be `ACTIVE`, `availableSeats > 0`.
* A student cannot be `ENROLLED` twice in the same course (re-enrolling after a cancellation is allowed).
* `enrollStudent` and `cancelEnrollment` are `@Transactional`: the enrollment row and the seat change succeed or fail together
  (any exception rolls everything back). The course row is read with a **pessimistic write lock**, so two simultaneous
  requests can never take the last seat.
* Updating a course keeps seats consistent: `availableSeats = newTotalSeats − seatsAlreadyTaken`;
  `totalSeats` cannot be set below the seats already taken.

## Standard response
Every response (success **and** error) uses the same envelope:
```json
{ "status": "SUCCESS", "message": "Course created successfully", "data": { "...": "..." } }
```
```json
{ "status": "FAIL", "message": "Validation failed", "data": { "email": "Email must be a valid email address" } }
```
Paged endpoints return `data` as a Spring `Page` (JSON: `content` + `page{size, number, totalElements, totalPages}`).

## Error handling (`GlobalExceptionHandler`, `@ControllerAdvice`)
| Situation | HTTP status |
|---|---|
| `ResourceNotFoundException`, unknown URL | 404 |
| `BusinessException` (no seats, inactive student/course, already enrolled, cancel non-ENROLLED, …) | 400 |
| `MethodArgumentNotValidException` / `ConstraintViolationException` / malformed JSON / bad enum or number / invalid `sort` property | 400 |
| `DuplicateResourceException` (email, course code) and DB unique-constraint violations | 409 |
| Anything unexpected | 500 |

> The assignment says "400 for validation / business errors". Duplicates use **409 Conflict** (the standard HTTP code for that case).
> If your trainer wants 400 there, change one line in `GlobalExceptionHandler.handleDuplicate`.

## MapStruct vs ModelMapper – where and why

**MapStruct** (compile-time generated code, no reflection) is used for all core mappings in `mapper/`:
* `StudentMapper`: `StudentRequestDTO → Student`, `Student → StudentResponseDTO`, in-place update.
* `CourseMapper`: same for `Course`.
* `EnrollmentMapper`: `EnrollmentRequestDTO → Enrollment` and `Enrollment → EnrollmentResponseDTO`, where nested values are
  flattened explicitly (`student.fullName → studentName`, `course.code → courseCode`, …).

Why: it is fast, type-safe (mapping mistakes fail at compile time) and explicit for anything that needs rules
(ignored fields, nested flattening, "update only non-null fields").

**ModelMapper** (runtime, reflection-based) is used in `PageMapper`, which converts a `Page<Entity>` into a `Page<DTO>`
**dynamically for any DTO class** (`pageMapper.toPage(page, StudentResponseDTO.class)`).
It is used for the paginated Student and Course lists, whose DTOs have exactly the same field names as the entities.

Why: one generic, reusable method that works for any entity/DTO pair, without writing a mapper method per type.
Enrollment pages use MapStruct instead, because that mapping is nested and needs explicit flattening.

## Tests
`mvn test` runs:
* `EnrollmentServiceImplTest` – Mockito unit tests of every enrollment business rule (seats, active status, duplicates, cancel).
* `AcademyApiIntegrationTest` – full MockMvc + H2 flow: create/enroll/cancel with seat tracking, rollback on failure,
  validation, 404/409 handling, soft delete, pagination/sorting/filtering, OpenAPI availability.

## Sample requests (curl)
```bash
curl -X POST localhost:8080/api/v1/courses -H "Content-Type: application/json" \
  -d '{"name":"Spring Boot","code":"SB-201","description":"Advanced","fee":25000.00,"totalSeats":30}'

curl -X POST localhost:8080/api/v1/students -H "Content-Type: application/json" \
  -d '{"fullName":"Nimal Perera","email":"nimal.p@example.com","contactNumber":"0771234567"}'

curl -X POST localhost:8080/api/v1/enrollments -H "Content-Type: application/json" \
  -d '{"studentId":1,"courseId":1,"paid":true}'

curl "localhost:8080/api/v1/courses?page=0&size=5&sort=fee,desc&status=ACTIVE"
curl -X PATCH localhost:8080/api/v1/enrollments/1/cancel
```
