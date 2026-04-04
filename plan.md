# Microservices Migration Plan

## Overview

Migrate the monolithic Spring Boot backend (`backend/ap/`) into 6 microservices + 1 shared library, fronted by an API Gateway. The frontends (Angular web + Ionic mobile) continue working through the gateway on port 8080 with the same `/api/v1/*` URL structure.

---

## Target Architecture

```
Clients (Web :4200, Mobile :8100)
          │
          ▼
┌──────────────────────┐
│   API Gateway :8080  │  ← JWT validation, CORS, routing
└──────────┬───────────┘
           │
    ┌──────┼────────────────────────┐
    │      │                        │
    ▼      ▼                        ▼
┌────────┐ ┌──────────┐ ┌──────────────────┐
│Auth Svc│ │Task Svc  │ │Messaging Svc     │
│:8081   │ │:8082     │ │:8083 + WebSocket │
│auth_db │ │task_db   │ │messaging_db      │
└────────┘ └──────────┘ └──────────────────┘
    │           │              │
    │     ┌─────▼──────┐       │
    └────►│Notif. Svc  │◄──────┘
          │:8084+Email │   Events via RabbitMQ
          │notif_db    │
          └────────────┘

Service Registry (Eureka :8761) — discovery
```

---

## Services Breakdown

### 1. shared-lib (Maven module, not a runnable service)
- **Purpose**: Shared DTOs, events, JWT utility
- **Contains**:
  - `dto/UserDTO.java` — id, email, firstName, lastName, role, userActive
  - `dto/AuthResponse.java` — token + UserDTO
  - `dto/LoginRequest.java` — email, password
  - `event/NotificationEvent.java` — userId, title, message, eventType
  - `event/EmailEvent.java` — toEmail, subject, body, html
  - `event/UserApprovedEvent.java` — userId, email
  - `security/JwtUtil.java` — generate/validate JWT tokens (JJWT 0.12.5, HMAC-SHA256, 24h expiry)

### 2. service-registry (port 8761)
- **Purpose**: Eureka Server for service discovery
- **Dependencies**: spring-cloud-starter-netflix-eureka-server
- **Config**: standalone mode (register-with-eureka=false, fetch-registry=false)

### 3. api-gateway (port 8080)
- **Purpose**: Single entry point for all clients
- **Dependencies**: spring-cloud-starter-gateway, eureka-client, shared-lib
- **Key features**:
  - JWT validation filter (GlobalFilter)
  - Skips JWT for: `/api/v1/auth/login`, `/api/v1/auth/register`, `/api/v1/auth/forgot-password`, `/chat/**`
  - Extracts JWT claims → adds headers: `X-User-Id`, `X-User-Role`, `X-User-Email`
  - Returns 401 for invalid/missing tokens
  - Centralized CORS config (replaces per-controller @CrossOrigin)

**Route Table** (order matters — most specific first):

| Path Pattern | Destination |
|---|---|
| `/api/v1/auth/**` | lb://auth-service |
| `/api/v1/User/logout` | lb://auth-service |
| `/api/v1/admin/users/**` | lb://auth-service |
| `/api/v1/admin/users` | lb://auth-service |
| `/api/v1/admin/test-email` | lb://auth-service |
| `/api/v1/Task/**` | lb://task-service |
| `/api/v1/User/message/**` | lb://messaging-service |
| `/api/v1/User/messages/**` | lb://messaging-service |
| `/api/v1/admin/message/**` | lb://messaging-service |
| `/api/v1/admin/messages` | lb://messaging-service |
| `/api/v1/admin/messages/**` | lb://messaging-service |
| `/chat/**` | lb:ws://messaging-service |
| `/api/v1/Not/**` | lb://notification-service |
| `/api/v1/User/not/**` | lb://notification-service |
| `/api/v1/User/read/**` | lb://notification-service |

### 4. auth-service (port 8081, auth_db)
- **Purpose**: Authentication + User management
- **From monolith**: AuthenticationController, AuthService, AdminController (user endpoints), UserRepository, User model
- **New features**: JWT generation, BCrypt hashing, role field on User, password migration runner
- **Endpoints**:
  - `POST /api/v1/auth/login` → returns `{ token, user: {id,email,firstName,lastName,role,userActive} }`
  - `POST /api/v1/auth/register` → returns UserDTO
  - `PUT /api/v1/auth/update/{id}` → returns UserDTO
  - `POST /api/v1/auth/forgot-password` → publishes EmailEvent, returns String
  - `POST /api/v1/User/logout` → returns String
  - `GET /api/v1/admin/users` → returns List<UserDTO> (ADMIN only via X-User-Role)
  - `GET /api/v1/admin/users/{id}` → returns UserDTO
  - `POST /api/v1/admin/users/new` → returns UserDTO
  - `PUT /api/v1/admin/users/{id}` → returns UserDTO
  - `DELETE /api/v1/admin/users/{id}` → returns String
  - `PUT /api/v1/admin/users/approve/{id}` → publishes EmailEvent + UserApprovedEvent
  - `GET /api/v1/admin/test-email` → publishes EmailEvent
- **Publishes events**: EmailEvent (forgot-password, approval), UserApprovedEvent
- **Database**: Preserves exact column names from monolith (fName, lName, password, userA, created_at) + new `role` column

### 5. task-service (port 8082, task_db)
- **Purpose**: Task management
- **From monolith**: TasksController, TaskService, TaskRepository, Task model
- **Endpoints** (same as monolith):
  - `POST /api/v1/Task/tasks` → creates task, publishes NotificationEvent
  - `GET /api/v1/Task/tasks` → all tasks
  - `GET /api/v1/Task/tasks/{id}` → single task
  - `PUT /api/v1/Task/tasks/{id}` → update task
  - `DELETE /api/v1/Task/tasks/{id}` → delete task
  - `PUT /api/v1/Task/tasks/{id}/respond` → status=SUBMITTED, publishes NotificationEvent
  - `PUT /api/v1/Task/tasks/approve/{id}` → status=APPROVED, publishes NotificationEvent
  - `PUT /api/v1/Task/tasks/{id}/decline` → status=DECLINED, publishes NotificationEvent
  - `PUT /api/v1/Task/tasks/{id}/request-revision` → status=REVISION_REQUESTED, publishes NotificationEvent
- **Publishes events**: NotificationEvent for every status change

### 6. messaging-service (port 8083, messaging_db)
- **Purpose**: Chat + real-time messaging
- **From monolith**: ChatMessage model, ChatMessageRepository, ChatProducer, ChatConsumer, RabbitConfig, WebSocketConfig, chat-related endpoints from UserControler + AdminController
- **Endpoints**:
  - `POST /api/v1/User/message/admin` → saves message, sends to RabbitMQ
  - `GET /api/v1/User/messages/sent/{userId}` → sent messages
  - `GET /api/v1/User/messages/inbox/{userId}` → received messages
  - `POST /api/v1/admin/message/user` → saves message, sends to RabbitMQ (ADMIN only)
  - `GET /api/v1/admin/messages` → admin sent messages (uses X-User-Id header, not hardcoded 1)
  - `GET /api/v1/admin/messages/inbox` → admin received messages (uses X-User-Id header)
  - WebSocket: `/chat` endpoint with STOMP, topics `/topic/user` and `/topic/admin`
- **RabbitMQ**: Preserves chat_exchange (DirectExchange), admin_queue, user_queue
- **Fixes**: Removes duplicate admin_queue consumer bug from monolith

### 7. notification-service (port 8084, notification_db)
- **Purpose**: Notifications + email sending
- **From monolith**: NotificationController, NotificationService, NotificationRepository, Notification model, EmailService
- **Endpoints**:
  - `POST /api/v1/Not/notifications` → create notification
  - `POST /api/v1/Not/notify/{userId}` → notify specific user
  - `POST /api/v1/Not/notify/all` → notify all (calls auth-service for user list)
  - `GET /api/v1/Not/notifications/status/{userId}` → user notifications
  - `PUT /api/v1/Not/read/{id}` → mark read
  - `GET /api/v1/User/not/{userId}` → alias for status endpoint
  - `PUT /api/v1/User/read/{id}` → alias for mark-read
- **Listens for events**: NotificationEvent → creates notification, EmailEvent → sends email
- **Inter-service**: Calls auth-service via @LoadBalanced RestTemplate for "notify all"

---

## Security Design

### JWT Flow
1. Client POSTs `/api/v1/auth/login` with `{ email, password }`
2. auth-service validates with BCrypt, generates JWT with claims: userId, email, role
3. Returns `{ token: "eyJ...", user: { id, email, firstName, lastName, role, userActive } }`
4. Client stores token in localStorage, sends `Authorization: Bearer <token>` on every request
5. Gateway's JwtAuthFilter validates token, extracts claims, adds X-User-Id/X-User-Role/X-User-Email headers
6. Downstream services read headers for authorization decisions

### Password Migration
- auth-service includes `PasswordMigrationRunner` (CommandLineRunner)
- Enabled via `password.migration.enabled=true` in application.yml
- Iterates all users, BCrypt-encodes any password not starting with `$2a$` or `$2b$`
- Run once after data migration, then disable

---

## Inter-Service Communication

### RabbitMQ Exchanges
1. **chat_exchange** (DirectExchange) — existing, used by messaging-service
   - admin_queue (routing key: "admin")
   - user_queue (routing key: "user")

2. **event_exchange** (TopicExchange) — NEW, for cross-service events
   - notification_events_queue (routing key: `event.notification.#`)
   - email_events_queue (routing key: `event.email.#`)

### Event Flow Examples
- Task created → task-service publishes NotificationEvent → notification-service creates notification
- User approved → auth-service publishes EmailEvent → notification-service sends approval email
- Forgot password → auth-service publishes EmailEvent → notification-service sends temp password email

---

## Frontend Changes

### Web (frontend-web/app/)
- Update `src/environments/environment.ts`: apiUrl → `http://localhost:8080/api/v1`
- Fix hardcoded URLs in `src/app/controllers/chat.ts` (3 URLs)
- Login response changes from string to `{ token, user }` — update auth controller

### Mobile (frontend-mobile/app/)
- Update `src/environments/environment.ts` and `environment.prod.ts`: add apiUrl
- Replace hardcoded base URLs in all 4 service files
- Add auth interceptor (reads token from localStorage, adds Bearer header)
- Update login flow to handle JWT response
- Update WebSocket URL to use environment config

---

## Infrastructure

### Docker Compose Services
| Service | Image | Port | Volume |
|---|---|---|---|
| auth-db | postgres:17 | 5433 | auth_db_data |
| task-db | postgres:17 | 5434 | task_db_data |
| messaging-db | postgres:17 | 5435 | messaging_db_data |
| notification-db | postgres:17 | 5436 | notification_db_data |
| rabbitmq | rabbitmq:3-management | 5672, 15672 | rabbitmq_data |
| service-registry | custom | 8761 | — |
| auth-service | custom | 8081 | — |
| task-service | custom | 8082 | — |
| messaging-service | custom | 8083 | — |
| notification-service | custom | 8084 | — |
| api-gateway | custom | 8080 | — |

### Environment Variables (.env)
- DB_USERNAME, DB_PASSWORD
- RABBITMQ_USERNAME, RABBITMQ_PASSWORD
- JWT_SECRET
- MAIL_USERNAME, MAIL_PASSWORD, MAIL_FROM
- EUREKA_URL

---

## Database Column Reference (MUST preserve)

These non-standard column names from the monolith MUST be kept exactly:

**users table**: id, email, `fName`, `lName`, `password` (Java field: secretPassword), `userA` (Java field: userActive), `created_at` + NEW: `role`

**tasks table**: id, status, title, description, `assigned_to_user_id`, `created_at`, `user_response`, `response_at`

**chat_messages table**: id, `sender_id`, `receiver_id`, `sender_role`, content, `created_at`, `is_read`

**notifications table**: id, title, message, `user_id`, `is_read`, `created_at`

---

## Key Risks
1. **Database column names**: Mismatched @Column names will create new columns and lose data
2. **WebSocket through gateway**: May need direct connection fallback
3. **Mobile login breaking change**: String → JSON response requires frontend update
4. **Route conflicts**: /api/v1/User and /api/v1/admin split across services — precise path predicates needed
5. **RabbitMQ serialization**: Jackson converter must be identical across all services
