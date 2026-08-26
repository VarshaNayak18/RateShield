# RateShield

A distributed, policy-driven API rate limiting service built with **Java 21, Spring Boot, PostgreSQL, Redis, Spring Security, and Docker**.

RateShield provides API-key-based request throttling with dynamic policies, endpoint-specific limits, distributed Redis-backed counters, concurrent request handling, and multi-instance deployment.

---

### Overview

RateShield is designed to demonstrate how a production-style rate limiting service can be built using Spring Boot and Redis.

The system supports:

- JWT authentication
- API key authentication
- API key lifecycle management
- Dynamic rate-limit policies
- API-key-to-policy assignment
- Endpoint-specific rate-limit policies
- Redis-backed distributed rate limiting
- Atomic Redis operations
- Rate-limit response headers
- Structured JSON error responses
- Automated integration testing
- Concurrent request testing
- Multi-instance deployment
- Docker Compose
- Spring Boot Actuator metrics

---

### Key Features

- JWT-based user authentication
- API key generation, validation, expiration, and revocation
- Secure API key storage using hashes
- Dynamic rate-limit policies
- API-key-to-policy assignment
- Endpoint-specific rate-limit policies
- Fixed-window rate limiting
- Redis-backed distributed counters
- Atomic Redis `INCR` + `EXPIRE` operations
- SHA-256 client fingerprinting for Redis keys
- Standard rate-limit response headers
- Structured JSON error responses
- Concurrent request handling and testing
- Multi-instance distributed deployment
- Docker Compose support
- Spring Boot Actuator health and metrics

---

### Architecture

```text
                           Client
                              |
                              | X-API-Key
                              v
                    +----------------------+
                    |    RateShield API    |
                    |    Spring Boot       |
                    +----------+-----------+
                               |
                +--------------+--------------+
                |                             |
                v                             v
        +---------------+             +---------------+
        |  PostgreSQL   |             |     Redis     |
        |               |             |               |
        | Users         |             | Rate counters |
        | API Keys      |             | TTL           |
        | Policies      |             | Shared state  |
        | Endpoints     |             | Atomic ops    |
        +---------------+             +---------------+

```

---

### Multi-instance deployment

RateShield can run multiple application instances sharing the same Redis state:

```text

                       Client
                         |
               +---------+---------+
               |                   |
               v                   v
       +---------------+   +---------------+
       | RateShield #1 |   | RateShield #2 |
       |   :8081       |   |   :8082       |
       +-------+-------+   +-------+-------+
               |                   |
               +---------+---------+
                         |
                         v
                    +---------+
                    |  Redis  |
                    +---------+
                         |
                         v
                 Shared rate state
```
Because the rate-limit counter is stored in Redis instead of local JVM memory, both instances enforce the same limit.

---

### Tech Stack

| Technology | Purpose |
|---|---|
| Java 21 | Application language and runtime |
| Spring Boot 4.1 | Backend framework |
| Spring Security | Authentication and authorization |
| Spring Data JPA | Data access and persistence |
| Hibernate | ORM and database mapping |
| PostgreSQL 18 | Relational database |
| Redis 7 | Distributed rate-limit state and counters |
| Maven | Build and dependency management |
| JUnit 5 | Automated testing |
| Mockito | Unit testing and mocking |
| Micrometer | Application metrics |
| Spring Boot Actuator | Health checks and monitoring |
| Docker | Containerization |
| Docker Compose | Multi-service and multi-instance deployment |

---

### Rate Limiting

RateShield currently implements a **fixed-window rate limiting algorithm**.

A rate-limit policy defines:

- Maximum number of requests
- Window duration in seconds
- Active/inactive status

Example:

| Policy | Requests | Window |
|---|---:|---:|
| DEFAULT | 5 | 60 seconds |
| PRO | 100 | 60 seconds |

The rate limit is resolved dynamically from PostgreSQL rather than being hard-coded in the controller.

The request flow is:

```text
Client
   |
   | X-API-Key
   v
API Key Authentication
   |
   v
Resolve Rate-Limit Policy
   |
   v
Redis Counter
   |
   +----> Allowed
   |
   +----> 429 Too Many Requests
```

--- 

### Distributed Rate Limiting

RateShield stores rate-limit counters in Redis instead of local application memory.

This allows multiple RateShield instances to share the same rate-limit state:

```text
                  Client
                    |
          +---------+---------+
          |                   |
          v                   v
  RateShield :8081     RateShield :8082
          |                   |
          +---------+---------+
                    |
                    v
                 Redis
                    |
                    v
           Shared Rate State
```

For example, with a policy of 5 requests per 60 seconds:

Request 1 → :8081 → 200

Request 2 → :8082 → 200

Request 3 → :8081 → 200

Request 4 → :8082 → 200

Request 5 → :8081 → 200

Request 6 → :8082 → 429

Both application instances use the same Redis counter.

Redis operations are executed atomically using a server-side script that performs:INCR, EXPIRE, TTL

This prevents concurrent requests from incorrectly bypassing the configured limit.

---

### Response Headers

RateShield exposes rate-limit information through HTTP response headers.

| Header | Description |
|---|---|
| `X-RateLimit-Limit` | Maximum requests allowed in the current window |
| `X-RateLimit-Remaining` | Number of requests remaining |
| `X-RateLimit-Reset` | Seconds until the current window resets |
| `Retry-After` | Recommended wait time after a `429` response |

---

### Successful Response

```http
HTTP/1.1 200 OK
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 99
X-RateLimit-Reset: 42
```

---

### Security

RateShield implements multiple security layers to protect users, API consumers, API keys, and application secrets.

**1. JWT Authentication**

JWT-based authentication is used for user authentication and protected administrative operations.

`User → POST /api/auth/login → JWT → Protected API endpoints`

**2. API Key Authentication**

API consumers authenticate using an API key supplied through the `X-API-Key` header.

`X-API-Key: rs_live_...`

API keys are validated before the request reaches the rate-limiting layer.

**3. Secure API Key Storage**

Raw API keys are not stored directly in PostgreSQL. They are securely hashed before storage, and the raw key is only exposed when it is initially created.

`Raw API Key → Secure Hash → PostgreSQL`

**4. Redis Key Protection**

Raw API keys are not included directly in Redis rate-limit keys. A SHA-256 fingerprint is used to identify the client, preventing sensitive API-key values from being exposed in Redis.

**5. Environment-Based Secrets**

Sensitive configuration is provided through environment variables rather than hard-coded values.

- `DB_PASSWORD=<database-password>`
- `JWT_SECRET=<jwt-secret>`

A `.env.example` file documents the required variables without containing real credentials. Real secrets should never be committed to Git.

---

### Response Headers

RateShield exposes rate-limit information through HTTP response headers so clients can track their current rate-limit status.

**1. `X-RateLimit-Limit`** — Maximum number of requests allowed in the current rate-limit window.

**2. `X-RateLimit-Remaining`** — Number of requests remaining in the current window.

**3. `X-RateLimit-Reset`** — Number of seconds until the current rate-limit window resets.

**4. `Retry-After`** — Number of seconds the client should wait before retrying after receiving a `429 Too Many Requests` response.

**Example:**

`HTTP/1.1 200 OK`  
`X-RateLimit-Limit: 100`  
`X-RateLimit-Remaining: 99`  
`X-RateLimit-Reset: 42`

When the limit is exceeded:

`HTTP/1.1 429 Too Many Requests`  
`X-RateLimit-Limit: 100`  
`X-RateLimit-Remaining: 0`  
`X-RateLimit-Reset: 6`  
`Retry-After: 6`

---

### Error Response

RateShield uses a consistent JSON structure for application errors and rate-limit failures.

When the configured rate limit is exceeded, the API returns `429 Too Many Requests` along with rate-limit headers and a structured JSON response.

**Example:**

{

  "timestamp": "2026-08-26T04:16:56Z",

  "status": 429,

  "error": "Too Many Requests",

  "message": "Rate limit exceeded",

  "path": "/api/rate-test"

}

**Response fields:**

- `timestamp` — Time at which the error occurred.
- `status` — HTTP status code.
- `error` — HTTP error type.
- `message` — Human-readable error message.
- `path` — API endpoint that produced the error.

This provides clients with a predictable error format that is easy to process programmatically.

---

### API Endpoints

RateShield exposes REST APIs for authentication, API key management, rate-limit policies, endpoint policies, and rate-limit testing.

#### 1. Authentication

`POST /api/auth/login` — Authenticate a user and obtain a JWT.

#### 2. API Keys

`POST /api/api-keys` — Create a new API key.

`GET /api/api-keys` — Retrieve the API keys associated with the authenticated user.

`PUT /api/api-keys/{apiKeyId}/policy/{policyId}` — Assign a rate-limit policy to an API key. Requires `ADMIN` authorization.

#### 3. Rate-Limit Policies

`POST /api/policies` — Create a rate-limit policy.

`GET /api/policies` — Retrieve all rate-limit policies.

`GET /api/policies/{id}` — Retrieve a specific policy.

`PUT /api/policies/{id}` — Update a rate-limit policy.

`DELETE /api/policies/{id}` — Delete a rate-limit policy.

Policy management requires `ADMIN` authorization.

#### 4. Endpoint Policies

`POST /api/endpoint-policies` — Create an endpoint-specific rate-limit policy.

`GET /api/endpoint-policies` — Retrieve active endpoint policies.

`GET /api/endpoint-policies/{id}` — Retrieve a specific endpoint policy.

`PUT /api/endpoint-policies/{id}` — Update an endpoint policy.

`DELETE /api/endpoint-policies/{id}` — Delete an endpoint policy.

Endpoint policy management requires `ADMIN` authorization.

#### 5. Rate-Limit Testing

`GET /api/rate-test` — Test rate limiting for a GET endpoint.

`POST /api/rate-test` — Test rate limiting for a POST endpoint.

Requests to the rate-test endpoints require a valid `X-API-Key` header.

#### 6. Monitoring

`GET /actuator/health` — Check application health.

`GET /actuator/metrics/rate_limit.allowed` — View the number of allowed rate-limited requests.

`GET /actuator/metrics/rate_limit.rejected` — View the number of rejected rate-limited requests.

---

### Project Structure

The project follows a layered Spring Boot architecture separating controllers, services, persistence, security, rate-limiting logic, DTOs, and exception handling.

```text
rateshield/
├── Dockerfile
├── docker-compose.yml
├── .env.example
├── .gitignore
├── pom.xml
├── mvnw
├── mvnw.cmd
├── README.md
│
├── src/
│   ├── main/
│   │   ├── java/com/rateshield/
│   │   │   ├── controller/
│   │   │   ├── dto/
│   │   │   ├── entity/
│   │   │   ├── exception/
│   │   │   ├── ratelimit/
│   │   │   ├── repository/
│   │   │   ├── security/
│   │   │   └── service/
│   │   │
│   │   └── resources/
│   │       └── application.properties
│   │
│   └── test/
│       ├── java/
│       └── resources/

```

- Controllers handle incoming HTTP requests and API responses.
- Services contain business logic for users, API keys, policies, and endpoint policies.
- Entities represent PostgreSQL data such as users, roles, API keys, rate-limit policies, and endpoint policies.
- Repositories provide database access through Spring Data JPA.
- Security contains JWT authentication, API-key authentication, validation, and security configuration.
- Rate Limiting contains the fixed-window algorithm, Redis-backed limiter, endpoint-policy resolution, client-key generation, and rate-limit result handling.
- DTOs define request and response models used by the REST APIs.
- Exception Handling provides consistent application error responses.

---

### Testing

RateShield uses automated testing at multiple levels to verify business logic, HTTP behavior, concurrency, and distributed rate limiting.

#### 1. Unit Testing

Unit tests cover core components such as:

- Fixed-window rate-limit behavior
- Client-key generation and fingerprinting
- Endpoint policy resolution
- Missing endpoint policy handling

#### 2. Integration Testing

Integration tests verify the complete request flow:

`HTTP Request → API Key Authentication → Endpoint Policy → Redis Rate Limiter → HTTP Response`

The integration tests verify that authenticated requests are allowed within the configured limit and rejected after the limit is exceeded.

Example:

`Request 1 → 200`  
`Request 2 → 200`  
`Request 3 → 200`  
`Request 4 → 200`  
`Request 5 → 200`  
`Request 6 → 429`

#### 3. Concurrency Testing

RateShield also tests concurrent requests against the same API key and endpoint.

Example:

`20 concurrent requests → 5 allowed + 15 rejected`

This verifies that Redis-backed atomic operations correctly enforce the limit even when requests arrive simultaneously.

#### 4. Multi-Instance Testing

The application is deployed as two Spring Boot instances using the same Redis instance.

Requests can be distributed between:

`RateShield :8081`  
`RateShield :8082`

while Redis maintains a single shared rate-limit counter.

This verifies that the configured limit is enforced consistently across application instances.

#### 5. Running Tests

Run the complete test suite with:

`.\mvnw.cmd test`

The project currently contains 10 automated tests with all tests passing.

---

### Run Locally

#### 1. Prerequisites

Before running RateShield locally, make sure the following are installed and available in your system environment:

- Java 21
- Maven (or use the included Maven Wrapper)
- PostgreSQL 18
- Redis 7
- Docker Desktop
- Git

Verify Java:

`java -version`

Verify Docker:

`docker --version`

Verify Docker Compose:

`docker compose version`

#### 2. Environment Variables

RateShield uses environment variables for sensitive configuration.

Set the following variables before starting the application:

`DB_PASSWORD=<your-postgresql-password>`

`JWT_SECRET=<your-long-random-jwt-secret>`

The repository includes `.env.example` as a reference:

`DB_PASSWORD=change-me`

`JWT_SECRET=replace-with-a-long-random-secret`

Do not commit real passwords, JWT secrets, or other credentials to Git.

#### 3. Run with Maven

Make sure PostgreSQL and Redis are running and the required environment variables are configured.

From the project root, start the application with the Maven Wrapper:

`.\mvnw.cmd spring-boot:run`

The application will run on:

`http://localhost:8080`

To compile the project:

`.\mvnw.cmd compile`

To run the complete test suite:

`.\mvnw.cmd test`

#### 4. Run with Docker Compose

Docker Compose starts the complete distributed environment, including PostgreSQL, Redis, and two RateShield application instances.

Build and start the environment:

`docker compose up --build -d`

The services are available at:

- RateShield Instance 1: `http://localhost:8081`
- RateShield Instance 2: `http://localhost:8082`
- PostgreSQL: `localhost:5433`
- Redis: `localhost:6379`

Check the service status:

`docker compose ps`

View application logs:

`docker compose logs rateshield-1`

`docker compose logs rateshield-2`

Check application health:

`http://localhost:8081/actuator/health`

`http://localhost:8082/actuator/health`

Stop the Docker environment:

`docker compose down`

---

### Git Development Structure

RateShield was developed incrementally using focused Git commits, with each milestone adding a specific capability or improving system reliability.

#### Development Milestones

`chore: initialize RateShield backend`

`feat: add user and role persistence`

`feat: implement JWT authentication`

`feat: implement API key management`

`feat: implement fixed window rate limiter`

`feat: add distributed rate limiting with Redis`

`feat: implement dynamic rate limit policies`

`feat: add admin rate limit policy API`

`feat: add API key policy assignment`

`feat: implement endpoint-specific rate limits`

`test: add endpoint policy integration coverage`

`test: add integration and rate limit coverage`

`test: add concurrent rate limit coverage`

`feat: add multi-instance distributed deployment`

`feat: add observability and production hardening`

`chore: document environment configuration`

This incremental development history demonstrates the evolution of RateShield from a basic Spring Boot backend into a distributed, policy-driven rate-limiting service.

---

### Future Improvements

Potential future enhancements include:

- Token-bucket rate limiting
- Sliding-window rate limiting
- Weighted request costs
- Multiple rate-limiting algorithms selectable per policy
- Prometheus and Grafana dashboards
- Advanced request analytics
- Rate-limit audit logging
- Policy versioning
- Policy inheritance
- API gateway integration
- OpenAPI/Swagger documentation
- Kubernetes deployment
- Horizontal auto-scaling
- CI/CD pipeline
- Automated security scanning
- Administrative web interface

---

### Author

**Varsha V Nayak**

GitHub: [VarshaNayak18](https://github.com/VarshaNayak18)

Repository: [RateShield](https://github.com/VarshaNayak18/RateShield)