# TaskFlow — Distributed Task Management System

A microservices-based task management platform (Spring Boot + Angular + Ionic) with real-time messaging and email notifications.

## Quick Start

**Requirements:** Docker + Docker Compose.

```bash
cd backend
docker-compose up --build -d
```

That's it. The whole system (4 microservices, 4 databases, API Gateway, Eureka, RabbitMQ, web frontend) starts in a single command.

Wait ~60 seconds for all services to register with Eureka, then open:

- **Web admin panel:** http://localhost:4200
- **API Gateway:** http://localhost:8080
- **Eureka dashboard:** http://localhost:8761
- **RabbitMQ management:** http://localhost:15672 (guest / guest)

## Default Admin Account

```
Email:    lotfitoumi56@gmail.com
Password: admin123
```

## Stop the System

```bash
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
