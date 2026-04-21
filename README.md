# TaskFlow — Distributed Task Management System

A microservices-based task management platform (Spring Boot + Angular + Ionic) with real-time messaging and email notifications.

## Requirements

- Docker + Docker Compose
- Node.js 18+ and npm

## Quick Start

### 1. Start the backend

```bash
cd backend
docker-compose up --build -d
```

This starts all microservices, databases, API Gateway, Eureka, and RabbitMQ. Wait ~60 seconds for all services to register.

### 2. Start the web frontend

```bash
cd frontend-web/app
npm install
npm start
```

Opens at http://localhost:4200

### 3. Start the mobile frontend

```bash
cd frontend-mobile/app
npm install
ionic serve
```

Opens at http://localhost:8100

## Default Admin Account

```
Email:    lotfitoumi56@gmail.com
Password: admin123
```

## Service URLs

| Component | URL |
|---|---|
| Web admin panel | http://localhost:4200 |
| Mobile app | http://localhost:8100 |
| API Gateway | http://localhost:8080 |
| Eureka dashboard | http://localhost:8761 |
| RabbitMQ management | http://localhost:15672 (guest / guest) |

## Stop the System

```bash
cd backend
docker-compose down
```

To also remove the databases:

```bash
docker-compose down -v
```

## Project Structure

```
backend/          4 Spring Boot microservices + API Gateway + Eureka
frontend-web/     Angular 21 admin panel
frontend-mobile/  Ionic + Capacitor mobile app
```
