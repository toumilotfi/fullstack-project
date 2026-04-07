# Phase 3: Full Connectivity & Security Hardening Plan

## Context

Phases 1-2 completed: credential hardening, gateway secret validation on auth+messaging admin controllers, frontend bug fixes (OnPush, field mismatches, routing guards), and a full API linking audit (respondToTask body format, notifyAll headers, WebSocket URLs, hardcoded adminIds).

This phase addresses the **remaining 14 issues** found during the full architecture audit. The goal: make every endpoint properly secured, every frontend call work end-to-end, and every service fully connected.

## 3 Phases, 14 Tasks

### Phase 1: Backend Security & Auth (Tasks 1-6)
Secure all remaining unprotected endpoints (task-service, notification-service, messaging user endpoints).

### Phase 2: Frontend Web Fixes (Tasks 7-9)
Fix sidebar duplicate, add error handlers, fix inspector state.

### Phase 3: Frontend Mobile Fixes (Tasks 10-14)
Fix status colors, error messages, WebSocket broadcasts, notification navigation.

---

## Files Modified

### Backend
| File | Tasks |
|------|-------|
| `backend/api-gateway/.../JwtAuthFilter.java` | 1 |
| `backend/task-service/.../TaskController.java` | 2 |
| `backend/task-service/src/main/resources/application.yml` | 2 |
| `backend/docker-compose.yml` | 2 |
| `backend/task-service/.../TaskService.java` | 3 |
| `backend/auth-service/.../model/User.java` | 4 |
| `backend/messaging-service/.../UserMessageController.java` | 5 |
| `backend/notification-service/.../NotificationController.java` | 6 |

### Frontend Web
| File | Tasks |
|------|-------|
| `frontend-web/.../components/sidebar/sidebar.html` | 7 |
| `frontend-web/.../controllers/dashboard.ts` | 8 |
| `frontend-web/.../controllers/inspector.ts` | 9 |

### Frontend Mobile
| File | Tasks |
|------|-------|
| `frontend-mobile/.../views/home/home.page.ts` | 10 |
| `frontend-mobile/.../controllers/auth.controller.ts` | 11 |
| `frontend-mobile/.../api/chat.api.ts` | 12 |
| `frontend-mobile/.../api/websocket.service.ts` | 13 |
| `frontend-mobile/.../views/notifications/notifications.page.ts` | 14 |

---

## Verification
1. `docker-compose up --build` -- all 6 services start
2. `cd frontend-web/app && ng build` -- zero errors
3. `cd frontend-mobile/app && ng build` -- zero errors
4. Admin flow: login, CRUD users, deploy task, approve/decline, chat, notifications
5. User flow: login on mobile, view tasks, respond, check notifications, chat
6. Security: `PUT /Task/tasks/approve/1` without headers returns 403
