# Fullstack Project

Distributed task management system built as a microservices-based backend with separate web and mobile clients.

## Overview

The project is organized around a Spring Boot / Spring Cloud backend and two frontend clients:

- `frontend-web/app`: Angular web dashboard
- `frontend-mobile/app`: Ionic + Angular mobile client
- `backend`: Java 21 microservices, PostgreSQL, RabbitMQ, Docker Compose

The main use case is task assignment and tracking across users, with asynchronous messaging and notifications handled by dedicated services.

## Architecture

Frontend clients talk to the system through the API Gateway.

Backend services:

- `service-registry`: Eureka service discovery
- `api-gateway`: single entry point for backend APIs
- `auth-service`: authentication and JWT handling
- `task-service`: task management
- `messaging-service`: messaging and websocket-related features
- `notification-service`: notifications and email integration
- `shared-lib`: shared backend code used by the services

Infrastructure:

- PostgreSQL database per service
- RabbitMQ for async messaging
- Docker Compose for local orchestration

## Repository Structure

```text
backend/            Spring Boot microservices and Docker Compose stack
frontend-web/app/   Angular web application
frontend-mobile/app/Ionic Angular mobile application
docs/               Project documentation
diagram/            Diagrams and architecture assets
```

Note: `backend/ap` is a legacy module and is not part of the active Maven multi-module backend build.

## Tech Stack

- Java 21
- Spring Boot 3.2
- Spring Cloud 2023.0
- PostgreSQL 17
- RabbitMQ 3
- Angular 21 for the web app
- Ionic + Angular 20 for the mobile app
- Docker and Docker Compose

## Service Ports

### Core services

- API Gateway: `http://localhost:8080`
- Auth Service: `http://localhost:8081`
- Task Service: `http://localhost:8082`
- Messaging Service: `http://localhost:8083`
- Notification Service: `http://localhost:8084`
- Service Registry: `http://localhost:8761`

### Databases

- Auth DB: `localhost:5433`
- Task DB: `localhost:5434`
- Messaging DB: `localhost:5435`
- Notification DB: `localhost:5436`

### Messaging and frontend

- RabbitMQ AMQP: `localhost:5672`
- RabbitMQ Management: `http://localhost:15672`
- Web app: `http://localhost:4200`

## Quick Start With Docker

### 1. Prepare backend environment

Copy the backend env template:

```powershell
Copy-Item backend\.env.example backend\.env
```

The default development placeholders are safe local defaults such as:

- `DB_USERNAME=appuser`
- `DB_PASSWORD=change-me`
- `RABBITMQ_USERNAME=guest`
- `RABBITMQ_PASSWORD=guest`

Update any values you want to customize in `backend/.env`.

### 2. Start the backend stack

```powershell
Set-Location backend
docker compose up --build
```

This starts:

- 4 PostgreSQL containers
- RabbitMQ
- Eureka service registry
- API Gateway
- Auth, Task, Messaging, and Notification services
- the web frontend container

## Local Development

### Prerequisites

- JDK 21
- Docker Desktop
- Node.js and npm
- IntelliJ IDEA for backend development
- Android Studio / Xcode if you want native mobile builds

### Backend

The backend is a Maven multi-module project rooted in `backend/pom.xml`.

Modules:

- `shared-lib`
- `service-registry`
- `api-gateway`
- `auth-service`
- `task-service`
- `messaging-service`
- `notification-service`

If you do not have Maven installed locally, you can compile with a Maven container:

```powershell
docker run --rm `
  -v "C:\fullstack-project\backend:/workspace" `
  -v "C:\Users\$env:USERNAME\.m2:/root/.m2" `
  -w /workspace `
  maven:3.9.9-eclipse-temurin-21 `
  mvn -o compile -DskipTests
```

### Running services from IntelliJ

Use the `local` Spring profile for local IDE runs:

```text
-Dspring.profiles.active=local
```

The local profile files already point services at:

- `127.0.0.1` instead of `localhost`
- per-service DB ports (`5433` to `5436`)
- `sslmode=disable`

This avoids IPv6 / SSL negotiation issues on Windows.

### Important Docker Desktop note

On Windows, Docker Desktop port forwarding can occasionally go stale for a specific Postgres container. The symptom looks like one of these:

- `FATAL: database "..._db" does not exist`
- `java.io.EOFException`
- `The connection attempt failed`

If that happens while the container itself is healthy, restart only the affected DB container:

```powershell
docker restart auth-db
docker restart task-db
docker restart messaging-db
docker restart notification-db
```

## Frontend Development

### Web app

```powershell
Set-Location frontend-web\app
npm install
npm start
```

### Mobile app

```powershell
Set-Location frontend-mobile\app
npm install
npm start
```

For native mobile workflows, continue with the normal Ionic / Capacitor commands from `frontend-mobile/app`.

## Default Local URLs

- Web API base URL: `http://localhost:8080/api/v1`
- Web socket URL setting: `http://localhost:8080/chat`
- Frontend URL: `http://localhost:4200`

## Notes

- `backend/.env.example` is the source of truth for local environment variables.
- The root `README.md` is for the whole repository.
- `backend/README.md` can be used for backend-only details if you want to expand service-level documentation later.
