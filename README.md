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
  frontend-web/app/       Angular web application (Admin Panel)
  frontend-mobile/app/    Ionic + Angular mobile application (User App)
```

---

## Quick Start (Docker)

> The fastest way to run the entire project. Only prerequisite is **Docker Desktop**.

### 1. Clone and enter the project

```bash
git clone <repository-url>
cd fullstack-project
```

### 2. Create and configure the environment file

```bash
cp backend/.env.example backend/.env
```

Open `backend/.env` and fill in **all required values**:

```dotenv
# --- Required ---
DB_USERNAME=yourdbuser
DB_PASSWORD=yourdbpassword
RABBITMQ_USERNAME=yourrabbituser
RABBITMQ_PASSWORD=yourrabbitpassword
JWT_SECRET=your-long-random-secret-at-least-64-chars
GATEWAY_INTERNAL_SECRET=another-random-secret-string
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-gmail-app-password
MAIL_FROM=your-email@gmail.com

# --- Optional: defaults work for local development ---
FRONTEND_URL=http://localhost:4200
WEB_API_URL=http://localhost:8080/api/v1
WEB_WS_URL=http://localhost:8080/chat
ALLOWED_ORIGINS=http://localhost:4200,http://localhost:4201,http://localhost:4202,http://localhost:8100,capacitor://localhost,http://localhost
```

> **Important — Gmail users:** `MAIL_PASSWORD` must be a **Gmail App Password**, not your regular Gmail password.
> Create one at: Google Account > Security > 2-Step Verification > App Passwords.
> The app password is 16 characters with no spaces (e.g. `abcdefghijklmnop`).

> **Important — Secrets:** Generate strong random values for `JWT_SECRET` and `GATEWAY_INTERNAL_SECRET`.
> You can use this command:
> ```bash
> openssl rand -hex 64   # for JWT_SECRET
> openssl rand -hex 32   # for GATEWAY_INTERNAL_SECRET
> ```

### 3. Start everything

```bash
cd backend
docker compose up --build
```

First build takes several minutes (Maven downloads + npm install). Subsequent starts are much faster.

### 4. Load seed data (first time only)

After all containers are healthy, load the development data:

```bash
docker exec -i auth-db psql -U yourdbuser -d auth_db < phase9-data/users_data.sql
docker exec -i task-db psql -U yourdbuser -d task_db < phase9-data/tasks_data.sql
docker exec -i messaging-db psql -U yourdbuser -d messaging_db < phase9-data/messages_data.sql
docker exec -i notification-db psql -U yourdbuser -d notification_db < phase9-data/notifications_data.sql
```

> Replace `yourdbuser` with the value you set for `DB_USERNAME` in `.env`.

### 5. Set the admin password

The seed data contains plain text passwords. You must BCrypt-encode the admin password before logging in.

Install Python bcrypt and run:

```bash
pip install bcrypt
python -c "import bcrypt; h = bcrypt.hashpw(b'YourAdminPassword', bcrypt.gensalt(10)); print(h.decode())"
```

Then update the admin user in the database:

```bash
docker exec -i auth-db psql -U yourdbuser -d auth_db -c "UPDATE users SET password = 'PASTE_HASH_HERE' WHERE id = 6;"
```

### 6. Open the app

| Service | URL |
|---|---|
| Admin Web Panel | http://localhost:4200 |
| Mobile App (browser) | http://localhost:8100 |
| API Gateway | http://localhost:8080 |
| RabbitMQ Dashboard | http://localhost:15672 |
| Eureka Dashboard | http://localhost:8761 |

**Admin login credentials:**
- Email: the email set in `MAIL_FROM`
- Password: the password you set in step 5

---

## Local Development (IntelliJ / IDE)

Use this when you want to edit code and see changes without rebuilding Docker images.

### Prerequisites

| Tool | Version | Notes |
|---|---|---|
| Docker Desktop | Latest | Required for databases and RabbitMQ |
| JDK | **21 exactly** | Do NOT use JDK 25 or odd-numbered versions |
| Maven | 3.9+ | |
| Node.js | 22+ | Even-numbered LTS version only |
| npm | 11+ | |

> **JDK Warning:** Use JDK 21 only. Odd-numbered Node.js versions (e.g. 25) and non-LTS JDKs will cause build failures.

### Step 1: Configure application-local.yml files

Each service has an `application-local.yml` file. Update all of them with your actual credentials.

**`backend/auth-service/src/main/resources/application-local.yml`:**
```yaml
spring:
  datasource:
    url: jdbc:postgresql://127.0.0.1:5433/auth_db?sslmode=disable
    username: yourdbuser
    password: yourdbpassword
  rabbitmq:
    host: 127.0.0.1
    port: 5672
    username: yourrabbituser
    password: yourrabbitpassword

jwt:
  secret: your-jwt-secret

gateway:
  internal-secret: your-gateway-secret

password:
  migration:
    enabled: true
```

**`backend/task-service/src/main/resources/application-local.yml`:**
```yaml
spring:
  datasource:
    url: jdbc:postgresql://127.0.0.1:5434/task_db?sslmode=disable
    username: yourdbuser
    password: yourdbpassword
  rabbitmq:
    host: 127.0.0.1
    port: 5672
    username: yourrabbituser
    password: yourrabbitpassword

gateway:
  internal-secret: your-gateway-secret
```

**`backend/messaging-service/src/main/resources/application-local.yml`:**
```yaml
spring:
  datasource:
    url: jdbc:postgresql://127.0.0.1:5435/messaging_db?sslmode=disable
    username: yourdbuser
    password: yourdbpassword
  rabbitmq:
    host: 127.0.0.1
    port: 5672
    username: yourrabbituser
    password: yourrabbitpassword

gateway:
  internal-secret: your-gateway-secret
```

**`backend/notification-service/src/main/resources/application-local.yml`:**
```yaml
spring:
  datasource:
    url: jdbc:postgresql://127.0.0.1:5436/notification_db?sslmode=disable
    username: yourdbuser
    password: yourdbpassword
  rabbitmq:
    host: 127.0.0.1
    port: 5672
    username: yourrabbituser
    password: yourrabbitpassword
  mail:
    username: your-email@gmail.com
    password: your-gmail-app-password
    from: your-email@gmail.com

gateway:
  internal-secret: your-gateway-secret
```

### Step 2: Start infrastructure (databases + RabbitMQ)

```bash
cd backend
docker compose up auth-db task-db messaging-db notification-db rabbitmq -d
```

### Step 3: Build the backend

```bash
cd backend
mvn clean install -DskipTests
```

### Step 4: Run backend services in IntelliJ

For each service, open the Run Configuration and set:

- **VM options:** `-Dspring.profiles.active=local`
- **JDK:** 21

**Start order — service-registry must start first:**

1. `service-registry` — no profile needed, just run it
2. `auth-service` — with `local` profile
3. `task-service` — with `local` profile
4. `messaging-service` — with `local` profile
5. `notification-service` — with `local` profile
6. `api-gateway` — with `local` profile

> **Port conflict:** If you get "Port 8761 already in use", kill the old process:
> ```bash
> # Windows
> netstat -ano | findstr :8761
> taskkill /PID <PID> /F
> ```

> **API Gateway fails to start without local profile:** The `application.yml` has
> `${GATEWAY_INTERNAL_SECRET:}` which defaults to empty string and causes a startup
> error. Always run api-gateway with `-Dspring.profiles.active=local`.

### Step 5: Load seed data (first time only)

```bash
cd backend
docker exec -i auth-db psql -U yourdbuser -d auth_db < phase9-data/users_data.sql
docker exec -i task-db psql -U yourdbuser -d task_db < phase9-data/tasks_data.sql
docker exec -i messaging-db psql -U yourdbuser -d messaging_db < phase9-data/messages_data.sql
docker exec -i notification-db psql -U yourdbuser -d notification_db < phase9-data/notifications_data.sql
```

### Step 6: Set admin password

The seed data uses plain text passwords. The `password.migration.enabled: true` setting
in `application-local.yml` will auto-encode them on startup. However, to set a specific
admin password, update the DB directly:

```bash
pip install bcrypt
python -c "import bcrypt; h = bcrypt.hashpw(b'YourAdminPassword', bcrypt.gensalt(10)); print(h.decode())"
```

```bash
docker exec -i auth-db psql -U yourdbuser -d auth_db -c "UPDATE users SET password = 'PASTE_HASH_HERE' WHERE id = 6;"
```

### Step 7: Run the frontends

**Web Admin Panel:**
```bash
cd frontend-web/app
npm install
npm start
# Opens at http://localhost:4200
```

**Mobile App (browser preview):**
```bash
cd frontend-mobile/app
npm install
npm start
# Opens at http://localhost:8100
```

**Mobile (native Android/iOS):**
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
| Web Frontend (Admin) | 4200 |
| Mobile Frontend | 8100 |

---

## Default Admin Account

After loading seed data and setting the password:

| Field | Value |
|---|---|
| Email | the email in your `.env` `MAIL_FROM` field |
| Role | ADMIN |
| DB user id | 6 |

Login at http://localhost:4200 with those credentials to access the admin panel.

---

## Troubleshooting

### API Gateway fails to start — "must not be empty"

You are running without the `local` Spring profile. Add `-Dspring.profiles.active=local`
to the JVM arguments in your run configuration.

### Admin login returns "Authentication failed"

The password in the database is plain text (from seed data) and must be BCrypt-encoded.
Follow Step 6 above to set a BCrypt-encoded password.

### Services show health status DOWN

Check that all Docker containers are running:
```bash
docker ps
```
And that you started all 6 backend services in the correct order (service-registry first).

### Database connection errors after Docker restart

On Windows, Docker Desktop port forwarding can go stale. Fix:
```bash
docker restart auth-db task-db messaging-db notification-db
```

### Notification service fails to start

Requires valid Gmail App Password. Regular Gmail password will not work.
Create an App Password at: Google Account > Security > 2-Step Verification > App Passwords.

### Port 8761 already in use

A previous service-registry instance is still running. Kill it:
```bash
# Windows — find the PID
netstat -ano | findstr :8761
# Then kill it
taskkill /PID <PID> /F
```

### Frontend can't reach the backend

Ensure the API Gateway is running on port 8080. The frontend proxies all `/api/v1`
and `/chat` requests to `http://localhost:8080` via `proxy.conf.json`.

### Node.js version warning

Use an even-numbered LTS version of Node.js (18, 20, 22). Odd versions (e.g. 25)
are not LTS and may cause unexpected issues.

---

## Environment Variables Reference

| Variable | Required | Description |
|---|---|---|
| `DB_USERNAME` | Yes | Postgres username for all databases |
| `DB_PASSWORD` | Yes | Postgres password for all databases |
| `RABBITMQ_USERNAME` | Yes | RabbitMQ username |
| `RABBITMQ_PASSWORD` | Yes | RabbitMQ password |
| `JWT_SECRET` | Yes | Secret key for signing JWT tokens (min 64 chars) |
| `GATEWAY_INTERNAL_SECRET` | Yes | Shared secret between gateway and services |
| `MAIL_USERNAME` | Yes | Gmail address for sending emails |
| `MAIL_PASSWORD` | Yes | Gmail App Password (16 chars, no spaces) |
| `MAIL_FROM` | Yes | Sender address shown on outgoing emails |
| `FRONTEND_URL` | No | Frontend origin for CORS (default: `http://localhost:4200`) |
| `WEB_API_URL` | No | API base URL for the web frontend container |
| `WEB_WS_URL` | No | WebSocket URL for the web frontend container |
| `ALLOWED_ORIGINS` | No | Comma-separated list of allowed CORS origins |
