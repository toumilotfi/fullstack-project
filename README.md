# Fullstack Task Management System

A distributed task management platform built with a microservices backend and separate web and mobile frontends. Users can create and assign tasks, exchange real-time messages, and receive email notifications.

## Architecture

```
                         +------------------+
                         |  Frontend (Web)  |  Angular 21 / Ionic UI
                         |  :4200           |
                         +--------+---------+
                                  |
                         +--------v---------+
                         |   API Gateway    |  Spring Cloud Gateway
                         |   :8080          |
                         +--------+---------+
                                  |
          +-----------+-----------+-----------+-----------+
          |           |           |           |           |
  +-------v--+ +------v---+ +----v------+ +--v--------+  |
  |   Auth   | |   Task   | | Messaging | | Notification| |
  |  Service | |  Service | |  Service  | |  Service  |  |
  |  :8081   | |  :8082   | |  :8083    | |  :8084    |  |
  +----+-----+ +----+-----+ +----+------+ +----+------+  |
       |            |             |              |        |
  +----v-----+ +----v-----+ +----v-------+ +----v------+ |
  | Auth DB  | | Task DB  | |Messaging DB| |Notif. DB  | |
  | PG :5433 | | PG :5434 | | PG :5435   | | PG :5436  | |
  +----------+ +----------+ +------------+ +-----------+ |
                                                          |
                         +------------+                   |
                         |  RabbitMQ  |<------------------+
                         |  :5672     |  Async events between services
                         +------------+
                                  ^
                         +--------+----------+
                         | Service Registry  |  Eureka
                         | :8761             |
                         +-------------------+
```

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 21, Spring Boot 3.2, Spring Cloud 2023.0 |
| Databases | PostgreSQL 17 (one per service) |
| Messaging | RabbitMQ 3 |
| Service Discovery | Eureka |
| Web Frontend | Angular 21, Ionic 8 |
| Mobile Frontend | Ionic 8, Capacitor 8, Angular 20 |
| Containerization | Docker, Docker Compose |

## Repository Structure

```
fullstack-project/
  backend/
    api-gateway/          Spring Cloud Gateway
    auth-service/         Authentication and JWT
    task-service/         Task CRUD and assignment
    messaging-service/    Real-time chat (WebSocket)
    notification-service/ Email notifications
    service-registry/     Eureka discovery server
    shared-lib/           Shared DTOs and utilities
    phase9-data/          SQL seed data
    docker-compose.yml    Full stack orchestration
    .env.example          Environment variable template
  frontend-web/app/       Angular web application
  frontend-mobile/app/    Ionic + Angular mobile application
```

---

## Quick Start (Docker)

> This is the fastest way to run the entire project. The only prerequisite is **Docker Desktop**.

### 1. Clone and configure

```bash
git clone <repository-url>
cd fullstack-project

# Create the environment file from the template
cp backend/.env.example backend/.env
```

### 2. Edit `backend/.env`

Open `backend/.env` and set the required values:

```dotenv
# --- Required: change these before starting ---
JWT_SECRET=your-long-random-secret-string-here
GATEWAY_INTERNAL_SECRET=another-random-secret-string
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-email-app-password
MAIL_FROM=your-email@gmail.com

# --- Optional: defaults work for local development ---
DB_USERNAME=appuser
DB_PASSWORD=change-me
RABBITMQ_USERNAME=guest
RABBITMQ_PASSWORD=guest
FRONTEND_URL=http://localhost:4200
WEB_API_URL=http://localhost:8080/api/v1
WEB_WS_URL=http://localhost:8080/chat
ALLOWED_ORIGINS=http://localhost:4200,http://localhost:4201,http://localhost:4202,http://localhost:8100,capacitor://localhost,http://localhost
```

> **Gmail users:** use an [App Password](https://support.google.com/accounts/answer/185833), not your regular password.

### 3. Start everything

```bash
cd backend
docker compose up --build
```

First build takes a few minutes (Maven downloads + npm install). Subsequent starts are much faster.

### 4. Open the app

| Service | URL |
|---|---|
| Web App | http://localhost:4200 |
| API Gateway | http://localhost:8080 |
| RabbitMQ Dashboard | http://localhost:15672 (guest/guest) |
| Eureka Dashboard | http://localhost:8761 |

---

## Local Development

Use this setup when you want to edit code and see changes without rebuilding Docker images.

### Prerequisites

| Tool | Version | Notes |
|---|---|---|
| Docker Desktop | Latest | Required for databases and RabbitMQ |
| JDK | 21 | Backend microservices |
| Maven | 3.9+ | Or use the Docker Maven wrapper (see below) |
| Node.js | 22+ | Frontend builds |
| npm | 11+ | Package management |

Optional for mobile development:
- Android Studio (Android builds)
- Xcode (iOS builds, macOS only)

### Step 1: Start infrastructure

Start only the databases and RabbitMQ:

```bash
cd backend
docker compose up auth-db task-db messaging-db notification-db rabbitmq -d
```

### Step 2: Build the backend

**With Maven installed:**

```bash
cd backend
mvn clean install -DskipTests
```

**Without Maven (using Docker):**

```bash
docker run --rm \
  -v "$(pwd)/backend:/workspace" \
  -v "$HOME/.m2:/root/.m2" \
  -w /workspace \
  maven:3.9.9-eclipse-temurin-21 \
  mvn clean install -DskipTests
```

### Step 3: Run backend services

Run each service from your IDE or terminal. Use the `local` Spring profile:

```bash
java -Dspring.profiles.active=local -jar auth-service/target/*.jar
java -Dspring.profiles.active=local -jar task-service/target/*.jar
java -Dspring.profiles.active=local -jar messaging-service/target/*.jar
java -Dspring.profiles.active=local -jar notification-service/target/*.jar
java -Dspring.profiles.active=local -jar api-gateway/target/*.jar
java -jar service-registry/target/*.jar
```

> The `local` profile connects to `127.0.0.1` on the mapped DB ports (5433-5436) with SSL disabled, which avoids IPv6 and SSL issues on Windows.

**Start order:** service-registry first, then the other services in any order.

### Step 4: Run a frontend

**Web:**

```bash
cd frontend-web/app
npm install
npm start
# Opens at http://localhost:4200 with proxy to localhost:8080
```

**Mobile (browser preview):**

```bash
cd frontend-mobile/app
npm install
npm start
# Opens at http://localhost:8100 with proxy to localhost:8080
```

**Mobile (native):**

```bash
cd frontend-mobile/app
npx cap sync
npx cap open android   # or: npx cap open ios
```

---

## Service Ports Reference

| Service | Port |
|---|---|
| API Gateway | 8080 |
| Auth Service | 8081 |
| Task Service | 8082 |
| Messaging Service | 8083 |
| Notification Service | 8084 |
| Eureka Registry | 8761 |
| Auth DB (Postgres) | 5433 |
| Task DB (Postgres) | 5434 |
| Messaging DB (Postgres) | 5435 |
| Notification DB (Postgres) | 5436 |
| RabbitMQ (AMQP) | 5672 |
| RabbitMQ (Management UI) | 15672 |
| Web Frontend | 4200 |

---

## Seed Data

SQL dumps for development data are in `backend/phase9-data/`. To load them into a running database:

```bash
docker exec -i auth-db psql -U appuser -d auth_db < backend/phase9-data/users_data.sql
docker exec -i task-db psql -U appuser -d task_db < backend/phase9-data/tasks_data.sql
docker exec -i messaging-db psql -U appuser -d messaging_db < backend/phase9-data/messages_data.sql
docker exec -i notification-db psql -U appuser -d notification_db < backend/phase9-data/notifications_data.sql
```

---

## Troubleshooting

### Database connection errors after Docker restart

On Windows, Docker Desktop port forwarding can go stale for individual Postgres containers. Symptoms:

- `FATAL: database "..._db" does not exist`
- `java.io.EOFException`
- `The connection attempt failed`

Fix: restart the affected DB container:

```bash
docker restart auth-db       # or task-db, messaging-db, notification-db
```

### Notification service fails to start

The notification service requires valid SMTP credentials. If `MAIL_USERNAME` and `MAIL_PASSWORD` are still set to placeholder values, the service will fail on startup. Set real email credentials in `backend/.env`.

### Frontend can't reach the backend

When running the web frontend locally with `npm start`, requests are proxied to `http://localhost:8080` via `proxy.conf.json`. Make sure the API Gateway is running on port 8080.

---

## Environment Variables

| Variable | Required | Description |
|---|---|---|
| `DB_USERNAME` | No | Postgres username (default: `appuser`) |
| `DB_PASSWORD` | No | Postgres password (default: `change-me`) |
| `RABBITMQ_USERNAME` | No | RabbitMQ user (default: `guest`) |
| `RABBITMQ_PASSWORD` | No | RabbitMQ password (default: `guest`) |
| `JWT_SECRET` | **Yes** | Secret key for signing JWT tokens |
| `GATEWAY_INTERNAL_SECRET` | **Yes** | Shared secret for service-to-gateway auth |
| `MAIL_USERNAME` | **Yes** | SMTP email address |
| `MAIL_PASSWORD` | **Yes** | SMTP email password or app password |
| `MAIL_FROM` | **Yes** | Sender address for outgoing emails |
| `FRONTEND_URL` | No | Frontend origin for CORS (default: `http://localhost:4200`) |
| `WEB_API_URL` | No | API base URL injected into the web frontend container |
| `WEB_WS_URL` | No | WebSocket URL injected into the web frontend container |
| `ALLOWED_ORIGINS` | No | Comma-separated CORS origins |
