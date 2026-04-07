# Tasks: Fullstack Security & Correctness Hardening

> 31 atomic tasks. Execute in order. Each task has an exact file path, what to find, and what to replace.
> Base path: `C:\fullstack-project\.claude\worktrees\elated-elbakyan\`

---

## SECTION A: Security (Tasks 1–13)

### Task 1 — Remove hardcoded credentials from auth-service
**File**: `backend/auth-service/src/main/resources/application.yml`

Replace:
```yaml
    username: ${DB_USERNAME:lotfi}
    password: ${DB_PASSWORD:92570533Lt@}
```
With:
```yaml
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
```

Replace:
```yaml
    username: ${RABBITMQ_USERNAME:guest}
    password: ${RABBITMQ_PASSWORD:guest}
```
With:
```yaml
    username: ${RABBITMQ_USERNAME}
    password: ${RABBITMQ_PASSWORD}
```

Replace:
```yaml
jwt:
  secret: ${JWT_SECRET:my-super-secret-key-that-is-at-least-32-characters-long}
```
With:
```yaml
jwt:
  secret: ${JWT_SECRET}

gateway:
  internal-secret: ${GATEWAY_INTERNAL_SECRET:}

frontend:
  url: ${FRONTEND_URL:http://localhost:4200}
```

---

### Task 2 — Remove hardcoded credentials from task-service
**File**: `backend/task-service/src/main/resources/application.yml`

Replace:
```yaml
    username: ${DB_USERNAME:lotfi}
    password: ${DB_PASSWORD:92570533Lt@}
```
With:
```yaml
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
```

Replace:
```yaml
    username: ${RABBITMQ_USERNAME:guest}
    password: ${RABBITMQ_PASSWORD:guest}
```
With:
```yaml
    username: ${RABBITMQ_USERNAME}
    password: ${RABBITMQ_PASSWORD}
```

---

### Task 3 — Remove hardcoded credentials from messaging-service
**File**: `backend/messaging-service/src/main/resources/application.yml`

Replace:
```yaml
    username: ${DB_USERNAME:lotfi}
    password: ${DB_PASSWORD:92570533Lt@}
```
With:
```yaml
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
```

Replace:
```yaml
    username: ${RABBITMQ_USERNAME:guest}
    password: ${RABBITMQ_PASSWORD:guest}
```
With:
```yaml
    username: ${RABBITMQ_USERNAME}
    password: ${RABBITMQ_PASSWORD}
```

Add at the end of the file (after the eureka block):
```yaml

gateway:
  internal-secret: ${GATEWAY_INTERNAL_SECRET:}

websocket:
  allowed-origins: ${ALLOWED_ORIGINS:http://localhost:4200,http://localhost:4201,http://localhost:4202,http://localhost:8100,capacitor://localhost,http://localhost}
```

---

### Task 4 — Remove hardcoded credentials from notification-service
**File**: `backend/notification-service/src/main/resources/application.yml`

Replace:
```yaml
    username: ${DB_USERNAME:lotfi}
    password: ${DB_PASSWORD:92570533Lt@}
```
With:
```yaml
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
```

Replace:
```yaml
    username: ${RABBITMQ_USERNAME:guest}
    password: ${RABBITMQ_PASSWORD:guest}
```
With:
```yaml
    username: ${RABBITMQ_USERNAME}
    password: ${RABBITMQ_PASSWORD}
```

Replace:
```yaml
    username: ${MAIL_USERNAME:lotfitoumi56@gmail.com}
    password: ${MAIL_PASSWORD:ljhdmbvmxiagfeku}
```
With:
```yaml
    username: ${MAIL_USERNAME}
    password: ${MAIL_PASSWORD}
```

Replace:
```yaml
    from: ${MAIL_FROM:lotfitoumi56@gmail.com}
```
With:
```yaml
    from: ${MAIL_FROM}
```

Add at the end of the file (after the eureka block):
```yaml

frontend:
  url: ${FRONTEND_URL:http://localhost:4200}
```

---

### Task 5 — Remove hardcoded JWT default from api-gateway
**File**: `backend/api-gateway/src/main/resources/application.yml`

Replace:
```yaml
jwt:
  secret: ${JWT_SECRET:my-super-secret-key-that-is-at-least-32-characters-long}
```
With:
```yaml
jwt:
  secret: ${JWT_SECRET}
```

---

### Task 6 — Update .env with all required variables
**File**: `backend/.env`

Replace entire file with:
```
DB_USERNAME=lotfi
DB_PASSWORD=92570533Lt@
RABBITMQ_USERNAME=guest
RABBITMQ_PASSWORD=guest
JWT_SECRET=my-super-secret-jwt-key-that-is-at-least-256-bits-long-for-hmac-sha
MAIL_USERNAME=lotfitoumi56@gmail.com
MAIL_PASSWORD=ljhdmbvmxiagfeku
MAIL_FROM=lotfitoumi56@gmail.com
FRONTEND_URL=http://localhost:4200
ALLOWED_ORIGINS=http://localhost:4200,http://localhost:4201,http://localhost:4202,http://localhost:8100,capacitor://localhost,http://localhost
GATEWAY_INTERNAL_SECRET=dev-gateway-secret-change-in-production
```

---

### Task 7 — Create .env.example template
**File**: `backend/.env.example` (NEW FILE — create it)

```
DB_USERNAME=
DB_PASSWORD=
RABBITMQ_USERNAME=
RABBITMQ_PASSWORD=
JWT_SECRET=
MAIL_USERNAME=
MAIL_PASSWORD=
MAIL_FROM=
FRONTEND_URL=http://localhost:4200
ALLOWED_ORIGINS=http://localhost:4200,http://localhost:8100
GATEWAY_INTERNAL_SECRET=
```

---

### Task 8 — Add gateway internal secret header injection
**File**: `backend/api-gateway/src/main/resources/application.yml`

Find (line 8–9):
```yaml
  cloud:
    gateway:
      globalcors:
```

Replace with:
```yaml
  cloud:
    gateway:
      default-filters:
        - AddRequestHeader=X-Gateway-Secret, ${GATEWAY_INTERNAL_SECRET:}
      globalcors:
```

---

### Task 9 — Validate gateway secret in AdminUserController
**File**: `backend/auth-service/src/main/java/com/example/auth/controller/AdminUserController.java`

Add this field after line 23 (`private final UserService userService;`):
```java

    @org.springframework.beans.factory.annotation.Value("${gateway.internal-secret:}")
    private String gatewaySecret;

    private boolean isUnauthorized(String role, String secret) {
        return !"ADMIN".equals(role) || !gatewaySecret.equals(secret);
    }
```

Then for EVERY method in this file, add this parameter:
```java
@RequestHeader(value = "X-Gateway-Secret", required = false) String gwSecret
```

And replace every occurrence of:
```java
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(403).build();
        }
```
With:
```java
        if (isUnauthorized(role, gwSecret)) {
            return ResponseEntity.status(403).build();
        }
```

There are 7 methods to update: `getAllUsers`, `getUserById`, `createUser`, `updateUser`, `deleteUser`, `approveUser`, `testEmail`.

---

### Task 10 — Validate gateway secret in AdminMessageController
**File**: `backend/messaging-service/src/main/java/com/example/messaging/controller/AdminMessageController.java`

Add after line 27 (after `this.chatProducer = chatProducer;` closing brace):
```java

    @org.springframework.beans.factory.annotation.Value("${gateway.internal-secret:}")
    private String gatewaySecret;
```

In `adminToUser` method (line 31), add parameter:
```java
            @RequestHeader(value = "X-Gateway-Secret", required = false) String gwSecret,
```

Replace line 37:
```java
        if (!"ADMIN".equalsIgnoreCase(userRole)) {
```
With:
```java
        if (!"ADMIN".equalsIgnoreCase(userRole) || !gatewaySecret.equals(gwSecret)) {
```

In `getAdminMessages` method (line 48), add parameter:
```java
            @RequestHeader(value = "X-Gateway-Secret", required = false) String gwSecret
```

In `getAdminInbox` method (line 56), add parameter:
```java
            @RequestHeader(value = "X-Gateway-Secret", required = false) String gwSecret
```

---

### Task 11 — Restrict WebSocket allowed origins
**File**: `backend/messaging-service/src/main/java/com/example/messaging/config/WebSocketConfig.java`

Replace the ENTIRE file with:
```java
package com.example.messaging.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${websocket.allowed-origins:http://localhost:4200}")
    private String allowedOrigins;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/chat")
                .setAllowedOrigins(allowedOrigins.split(","))
                .withSockJS();
    }
}
```

---

### Task 12 — Replace hardcoded localhost:3000 in UserService
**File**: `backend/auth-service/src/main/java/com/example/auth/service/UserService.java`

Add this field after line 22 (`private final AuthService authService;`):
```java

    @org.springframework.beans.factory.annotation.Value("${frontend.url:http://localhost:4200}")
    private String frontendUrl;
```

Replace line 93:
```java
                + "<a href='http://localhost:3000/login'>Login Here</a></body></html>";
```
With:
```java
                + "<a href='" + frontendUrl + "/login'>Login Here</a></body></html>";
```

---

### Task 13 — Replace hardcoded localhost:3000 in EmailService
**File**: `backend/notification-service/src/main/java/com/example/notification/service/EmailService.java`

Add this field after line 17 (`private String fromEmail;`):
```java

    @Value("${frontend.url:http://localhost:4200}")
    private String frontendUrl;
```

Replace line 44:
```java
                            "<a href='http://localhost:3000/login' " +
```
With:
```java
                            "<a href='" + frontendUrl + "/login' " +
```

---

## SECTION B: Bug Fixes (Tasks 14–21)

### Task 14 — Fix empty wsUrl in frontend-web
**File**: `frontend-web/app/src/environments/environment.ts`

Replace:
```typescript
  wsUrl: ''
```
With:
```typescript
  wsUrl: '/chat'
```

---

### Task 15 — Fix notification isRead field mapping in dashboard
**File**: `frontend-web/app/src/app/controllers/dashboard.ts`

Replace line 49:
```typescript
            isRead: n.isRead,
```
With:
```typescript
            isRead: n.read ?? n.isRead,
```

---

### Task 16 — Fix rejectTask calling deleteTask
**File**: `frontend-mobile/app/src/app/controllers/task.controller.ts`

Replace:
```typescript
rejectTask(taskId: number): Observable<void> {
  return this.taskApi.deleteTask(taskId);
}
```
With:
```typescript
rejectTask(taskId: number): Observable<any> {
  return this.taskApi.declineTask(taskId);
}
```

---

### Task 17 — Fix notification markRead field name
**File**: `frontend-mobile/app/src/app/controllers/notification.controller.ts`

Replace line 27:
```typescript
        prev.map(n => n.id === notificationId ? { ...n, isRead: true } : n)
```
With:
```typescript
        prev.map(n => n.id === notificationId ? { ...n, read: true } : n)
```

---

### Task 18 — Add createdAt to mobile Task model
**File**: `frontend-mobile/app/src/app/models/task.model.ts`

Replace the entire file with:
```typescript
export interface Task {
  id?: number;
  title: string;
  description: string;
  status?: string;
  assignedToUserId?: number;
  userResponse?: string;
  responseAt?: string;
  createdAt?: string;
  tempResponse?: string;
}
```

(Note: `completed: any;` is removed — the backend has no `completed` field, only `status`)

---

### Task 19 — Fix hardcoded admin userId=1 in NotificationEventListener
**File**: `backend/notification-service/src/main/java/com/example/notification/listener/NotificationEventListener.java`

Replace lines 20–24:
```java
        if (event.getUserId() != null) {
            notificationService.createNotification(event.getUserId(), event.getTitle(), event.getMessage());
        } else {
            notificationService.createNotification(1, event.getTitle(), event.getMessage());
        }
```
With:
```java
        if (event.getUserId() != null) {
            notificationService.createNotification(event.getUserId(), event.getTitle(), event.getMessage());
        } else {
            System.err.println("WARN: Notification event with null userId skipped. Title: " + event.getTitle());
        }
```

---

### Task 20 — Fix hardcoded admin userId=1 in AdminMessageController
**File**: `backend/messaging-service/src/main/java/com/example/messaging/controller/AdminMessageController.java`

Replace lines 51–52:
```java
        Integer adminId = userId != null ? userId : 1;
        return ResponseEntity.ok(chatMessageRepository.findBySenderIdOrderByCreatedAtAsc(adminId));
```
With:
```java
        if (userId == null) {
            return ResponseEntity.badRequest().body(null);
        }
        return ResponseEntity.ok(chatMessageRepository.findBySenderIdOrderByCreatedAtAsc(userId));
```

Replace lines 59–60:
```java
        Integer adminId = userId != null ? userId : 1;
        return ResponseEntity.ok(chatMessageRepository.findByReceiverIdOrderByCreatedAtAsc(adminId));
```
With:
```java
        if (userId == null) {
            return ResponseEntity.badRequest().body(null);
        }
        return ResponseEntity.ok(chatMessageRepository.findByReceiverIdOrderByCreatedAtAsc(userId));
```

---

### Task 21 — Add auth guards to mobile routes
**Step 1**: Create NEW file `frontend-mobile/app/src/app/guards/auth.guard.ts`:
```typescript
import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthController } from '../controllers/auth.controller';

export const authGuard: CanActivateFn = () => {
  const auth = inject(AuthController);
  const router = inject(Router);

  if (auth.currentUser()) {
    return true;
  }
  return router.createUrlTree(['/login']);
};
```

**Step 2**: Replace entire `frontend-mobile/app/src/app/app.routes.ts` with:
```typescript
import { Routes } from '@angular/router';
import { authGuard } from './guards/auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'landing', pathMatch: 'full' },
  { path: 'landing', loadComponent: () => import('./views/landing/landing.page').then(m => m.LandingPage) },
  { path: 'login', loadComponent: () => import('./views/login/login.page').then(m => m.LoginPage) },
  { path: 'register', loadComponent: () => import('./views/register/register.page').then(m => m.RegisterPage) },
  { path: 'approval-pending', loadComponent: () => import('./views/approval-pending/approval-pending.page').then(m => m.ApprovalPendingPage) },
  { path: 'forgot-password', loadComponent: () => import('./views/forgot-password/forgot-password.page').then(m => m.ForgotPasswordPage) },
  { path: 'home', canActivate: [authGuard], loadComponent: () => import('./views/home/home.page').then(m => m.HomePage) },
  { path: 'profile', canActivate: [authGuard], loadComponent: () => import('./views/profile/profile.page').then(m => m.ProfilePage) },
  { path: 'messaging', canActivate: [authGuard], loadComponent: () => import('./views/messaging/messaging.page').then(m => m.MessagingPage) },
  { path: 'notifications', canActivate: [authGuard], loadComponent: () => import('./views/notifications/notifications.page').then(m => m.NotificationsPage) },
  { path: 'tasks', canActivate: [authGuard], loadComponent: () => import('./views/tasks/tasks.page').then(m => m.TasksPage) },
  { path: 'tasks-card', canActivate: [authGuard], loadComponent: () => import('./components/task-card/task-card.component').then(m => m.TaskCardComponent) }
];
```

---

## SECTION C: Cleanup (Tasks 22–27)

### Task 22 — Delete duplicate UserNotificationController
**File**: `backend/notification-service/src/main/java/com/example/notification/controller/UserNotificationController.java`

**Action**: DELETE the entire file. Its endpoints (`/api/v1/User/not/{userId}` and `/api/v1/User/read/{id}`) are duplicated by `NotificationController` which handles `/api/v1/Not/*`.

---

### Task 23 — Remove dead gateway route for UserNotificationController
**File**: `backend/api-gateway/src/main/resources/application.yml`

Remove these 4 lines (the `user-notifications` route block):
```yaml
        - id: user-notifications
          uri: lb://notification-service
          predicates:
            - Path=/api/v1/User/not/**,/api/v1/User/read/**
```

---

### Task 24 — Remove broken stub methods from TaskController
**File**: `frontend-mobile/app/src/app/controllers/task.controller.ts`

Remove these lines:
```typescript
  create(arg0: { message: string; duration: number; position: string; color: string; }) {
    throw new Error('Method not implemented.');
  }
  approveTask(id: number) {
    throw new Error('Method not implemented.');
  }
```

---

### Task 25 — Add error handlers to admin.service.ts
**File**: `frontend-web/app/src/app/services/admin.service.ts`

Replace `loadUsers()`:
```typescript
   loadUsers() {
    return this.http.get<User[]>(`${this.baseUrl}/admin/users`).subscribe((data: User[]) => {
      this.users.set(data);
    });
  }
```
With:
```typescript
   loadUsers() {
    return this.http.get<User[]>(`${this.baseUrl}/admin/users`).subscribe({
      next: (data: User[]) => this.users.set(data),
      error: (err: any) => console.error('Failed to load users', err)
    });
  }
```

Replace `loadTasks()`:
```typescript
   loadTasks() {
    return this.http.get<Task[]>(`${this.baseUrl}/Task/tasks`).subscribe((data: Task[]) => {
      this.tasks.set(data);
    });
  }
```
With:
```typescript
   loadTasks() {
    return this.http.get<Task[]>(`${this.baseUrl}/Task/tasks`).subscribe({
      next: (data: Task[]) => this.tasks.set(data),
      error: (err: any) => console.error('Failed to load tasks', err)
    });
  }
```

---

### Task 26 — Fix TaskCardComponent class name
**File**: `frontend-mobile/app/src/app/components/task-card/task-card.component.ts`

Replace line 18:
```typescript
export class TasksPage implements OnInit {
```
With:
```typescript
export class TaskCardComponent implements OnInit {
```

(Note: The route import in `app.routes.ts` was already updated in Task 21 to use `m.TaskCardComponent`)

---

### Task 27 — Remove `completed` field from mobile Task model
**(Already handled in Task 18)** — The new Task interface in Task 18 does not include `completed: any`.

---

## SECTION D: Verification (Tasks 28–31)

### Task 28 — Add GATEWAY_INTERNAL_SECRET + ALLOWED_ORIGINS to docker-compose.yml
**File**: `backend/docker-compose.yml`

In `auth-service` environment block (after `JWT_SECRET: ${JWT_SECRET}`), add:
```yaml
      GATEWAY_INTERNAL_SECRET: ${GATEWAY_INTERNAL_SECRET}
```

In `messaging-service` environment block (after `RABBITMQ_PASSWORD`), add:
```yaml
      GATEWAY_INTERNAL_SECRET: ${GATEWAY_INTERNAL_SECRET}
      ALLOWED_ORIGINS: ${ALLOWED_ORIGINS}
```

In `notification-service` environment block (after `MAIL_FROM`), add:
```yaml
      FRONTEND_URL: ${FRONTEND_URL}
```

In `api-gateway` environment block (after `JWT_SECRET`), add:
```yaml
      GATEWAY_INTERNAL_SECRET: ${GATEWAY_INTERNAL_SECRET}
```

Also add to `auth-service` environment:
```yaml
      FRONTEND_URL: ${FRONTEND_URL}
```

---

### Task 29 — Build backend (verify all services compile)
```bash
cd backend && docker-compose build
```
All 6 images should build without errors.

---

### Task 30 — Build frontend-web
```bash
cd frontend-web/app && npx ng build
```
Zero TypeScript compilation errors expected.

---

### Task 31 — Build frontend-mobile
```bash
cd frontend-mobile/app && npx ng build
```
Zero TypeScript compilation errors expected.

---

## Smoke Test Checklist (Manual)
- [ ] `docker-compose up` — all services register in Eureka at :8761
- [ ] POST `/api/v1/auth/login` via gateway → returns JWT
- [ ] GET `/api/v1/admin/users` with Bearer token → returns user list
- [ ] GET `/api/v1/Task/tasks` with Bearer token → returns tasks
- [ ] Direct GET to auth-service:8081 `/api/v1/admin/users` WITHOUT `X-Gateway-Secret` → 403
- [ ] Mobile: unauthenticated access to `/home` → redirects to `/login`
- [ ] Mobile: reject task → sends PUT decline (not DELETE)
- [ ] Web dashboard: stats numbers populate after login
- [ ] WebSocket `/chat` connects from `localhost:4200`
- [ ] WebSocket `/chat` rejects connection from unknown origin
