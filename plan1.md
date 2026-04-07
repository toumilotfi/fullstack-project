# Plan 1: Fullstack Security & Correctness Hardening

## 1. What Was Audited

Full audit of a fullstack project with:
- **Backend**: 6 Spring Boot microservices (api-gateway, auth-service, task-service, messaging-service, notification-service, service-registry) + shared-lib
- **Frontend Web**: Angular 21 admin dashboard (port 4203)
- **Frontend Mobile**: Ionic Angular mobile app (port 4201)
- **Infrastructure**: Docker Compose, PostgreSQL x4, RabbitMQ, Eureka

## 2. What's Broken

### Security (CRITICAL)
| Issue | Impact |
|-------|--------|
| All 4 service `application.yml` files have real DB password `92570533Lt@` as default | Anyone with repo access has prod credentials |
| Notification-service has real Gmail app-password `ljhdmbvmxiagfeku` in YAML | Email account compromised |
| JWT secret hardcoded as YAML default | Token forgery possible |
| Admin endpoints trust `X-User-Role` header blindly | Direct service access = full admin rights |
| WebSocket allows `*` origins | Cross-site WebSocket hijacking |
| Email templates link to `localhost:3000` | Broken links, wrong port |

### Broken Functionality
| Issue | Impact |
|-------|--------|
| Mobile `rejectTask()` calls `deleteTask()` not `declineTask()` | Permanently deletes tasks instead of declining |
| Mobile `markRead()` sets `isRead: true` but model uses `read` | Unread count never decreases |
| Mobile has no route guards | Unauthenticated users can access all pages |
| Web `wsUrl` is empty string | Chat/WebSocket completely broken in dev |
| Web dashboard maps `n.isRead` but backend may return `read` | Activity feed shows wrong read status |
| Backend falls back to hardcoded admin userId=1 | Breaks if first user is not admin |

### Dead Code / Cleanup
| Issue | Impact |
|-------|--------|
| `UserNotificationController` duplicates `NotificationController` | Confusing, extra routes |
| Mobile `TaskController` has stub methods that throw `Error` | Crash if accidentally called |
| Mobile Task model has `completed: any` field | Backend has no such field |

## 3. Architecture Decisions

### Credential Removal Strategy
- Remove ALL `:default` values for sensitive env vars in YAML
- Services fail to start without proper `.env` — fail loud > fail silent
- Create `.env.example` with empty placeholders for developer onboarding
- Keep `.env` with real values (already gitignored)

### Gateway Secret for Internal Auth
- Gateway injects `X-Gateway-Secret` header on all forwarded requests (via `default-filters`)
- Admin controllers validate this header before trusting `X-User-Role`
- Lightweight alternative to mutual TLS
- Cost: 1 new env var, ~10 lines of code per admin controller

### WebSocket Origin Restriction
- Inject allowed origins from `application.yml` config
- Same list as gateway CORS: `localhost:4200,4201,4202,8100,capacitor://localhost`
- Overridable via `ALLOWED_ORIGINS` env var for production

### Frontend Field Mapping
- Backend `Notification.isRead` (boolean) → Jackson may serialize as both `"read"` and `"isRead"`
- Fix: map `n.read ?? n.isRead` defensively
- Mobile model: consistently use `read` (matching Jackson's boolean getter convention)

## 4. What We're NOT Changing (and Why)

| Item | Why |
|------|-----|
| Password reset via temp email | Acceptable for MVP; proper reset tokens = separate project |
| @Valid/@NotNull annotations | Big refactor across all entities + controllers; separate PR |
| @ControllerAdvice exception handlers | Medium effort; doesn't break anything currently |
| Users component bypassing service | Works correctly, just code hygiene |
| Mobile `wsUrl: '/chat'` | Already correct — proxied through Angular dev server |

## 5. Task Execution Order

**Priority 1 — Security (Tasks 1–13)**: Remove creds, add gateway secret, restrict WebSocket, fix email URLs
**Priority 2 — Bug Fixes (Tasks 14–21)**: Fix broken mobile actions, field mismatches, route guards
**Priority 3 — Cleanup (Tasks 22–27)**: Remove dead code, add error handlers, naming
**Priority 4 — Verify (Tasks 28–31)**: Docker build, Angular builds, smoke tests

All 31 tasks are in `tasks1.md` with exact file paths, find-and-replace instructions, and expected outcomes.
