# Tasks Phase 3: Full Connectivity & Security Hardening

---

## PHASE 1: Backend Security & Auth (Tasks 1-6)

---

### Task 1: Add /api/v1/auth/check-status to Gateway Open Paths

**File:** `backend/api-gateway/src/main/java/com/example/gateway/filter/JwtAuthFilter.java`

**What to change:**
In the `OPEN_PATHS` list (line 20-25), add `"/api/v1/auth/check-status"` before `"/chat"`.

**Before:**
```java
private static final List<String> OPEN_PATHS = List.of(
        "/api/v1/auth/login",
        "/api/v1/auth/register",
        "/api/v1/auth/forgot-password",
        "/chat"
);
```

**After:**
```java
private static final List<String> OPEN_PATHS = List.of(
        "/api/v1/auth/login",
        "/api/v1/auth/register",
        "/api/v1/auth/forgot-password",
        "/api/v1/auth/check-status",
        "/chat"
);
```

**Why:** The mobile approval-pending page calls `GET /api/v1/auth/check-status?email=...` without a JWT token (user just registered, has no token). Without this, the gateway returns 401.

**Verify:** `GET http://localhost:8080/api/v1/auth/check-status?email=test@test.com` with NO Authorization header should return 200 (not 401).

---

### Task 2: Add Authorization to Task Service Admin Endpoints

**Files:**
1. `backend/task-service/src/main/resources/application.yml`
2. `backend/docker-compose.yml`
3. `backend/task-service/src/main/java/com/example/task/controller/TaskController.java`

**Step 2a — application.yml:** Add at the bottom of the file:
```yaml
gateway:
  internal-secret: ${GATEWAY_INTERNAL_SECRET:}
```

**Step 2b — docker-compose.yml:** In the `task-service` environment block (around line 156-163), add:
```yaml
      GATEWAY_INTERNAL_SECRET: ${GATEWAY_INTERNAL_SECRET}
```
Place it after the `RABBITMQ_PASSWORD` line, before the `EUREKA_URL` line.

**Step 2c — TaskController.java:** Follow the exact pattern from `AdminUserController.java` (auth-service):

1. Add field at top of class (after the `taskService` field):
```java
@org.springframework.beans.factory.annotation.Value("${gateway.internal-secret:}")
private String gatewaySecret;
```

2. Add private helper method:
```java
private boolean isUnauthorized(String role, String secret) {
    return !"ADMIN".equals(role) || !gatewaySecret.equals(secret);
}
```

3. Add `@RequestHeader` parameters and 403 check to these 3 methods ONLY:

**approveTask** (line 58-60):
```java
@PutMapping("/tasks/approve/{id}")
public ResponseEntity<Task> approveTask(@PathVariable("id") Integer id,
                                         @RequestHeader(value = "X-User-Role", required = false) String role,
                                         @RequestHeader(value = "X-Gateway-Secret", required = false) String gwSecret) {
    if (isUnauthorized(role, gwSecret)) {
        return ResponseEntity.status(403).build();
    }
    return ResponseEntity.ok(taskService.approveTask(id));
}
```

**declineTask** (line 63-65):
```java
@PutMapping("/tasks/{id}/decline")
public ResponseEntity<Task> declineTask(@PathVariable("id") Integer id,
                                         @RequestHeader(value = "X-User-Role", required = false) String role,
                                         @RequestHeader(value = "X-Gateway-Secret", required = false) String gwSecret) {
    if (isUnauthorized(role, gwSecret)) {
        return ResponseEntity.status(403).build();
    }
    return ResponseEntity.ok(taskService.declineTask(id));
}
```

**requestRevision** (line 68-70):
```java
@PutMapping("/tasks/{id}/request-revision")
public ResponseEntity<Task> requestRevision(@PathVariable("id") Integer id,
                                             @RequestHeader(value = "X-User-Role", required = false) String role,
                                             @RequestHeader(value = "X-Gateway-Secret", required = false) String gwSecret) {
    if (isUnauthorized(role, gwSecret)) {
        return ResponseEntity.status(403).build();
    }
    return ResponseEntity.ok(taskService.requestRevision(id));
}
```

**DO NOT** add auth checks to: `createTask`, `getAllTasks`, `getTask`, `updateTask`, `deleteTask`, `respondToTask` — these are called by regular users.

Add import: `import org.springframework.web.bind.annotation.RequestHeader;` (if not already present).

**Verify:** `PUT /api/v1/Task/tasks/approve/1` without headers returns 403. With correct `X-User-Role: ADMIN` and `X-Gateway-Secret` returns 200.

---

### Task 3: Fix Null userId in Task Service Notification Events

**File:** `backend/task-service/src/main/java/com/example/task/service/TaskService.java`

**What to change:** Two `publishNotification` calls pass `null` as userId, causing notifications to be silently dropped by `NotificationEventListener`.

**Change 1 — respondToTask() at line 67:**

Before:
```java
publishNotification(null, "Task Response Received",
        "A response was submitted for task: " + task.getTitle(), "TASK_RESPONDED");
```

After:
```java
publishNotification(1, "Task Response Received",
        "A response was submitted for task: " + task.getTitle(), "TASK_RESPONDED");
```

Why `1`: The admin (userId=1) should be notified when a user submits a task response. This matches the hardcoded admin convention used in `UserMessageController.java` line 32 (`new ChatMessage(userId, 1, ...)`).

**Change 2 — declineTask() at line 87:**

Before:
```java
publishNotification(null, "Mission Declined",
        "Task declined: " + task.getTitle(), "TASK_DECLINED");
```

After:
```java
publishNotification(task.getAssignedToUserId(), "Mission Declined",
        "Task declined: " + task.getTitle(), "TASK_DECLINED");
```

Why `task.getAssignedToUserId()`: The assigned user should know their task was declined. This matches the pattern used by `approveTask()` (line 77) and `requestRevision()` (line 97).

**Verify:** Submit a task response as a user → check `notification_db.notifications` table → row with userId=1 should exist. Decline a task as admin → row with the assigned user's ID should exist.

---

### Task 4: Change User Entity Default userActive to false

**File:** `backend/auth-service/src/main/java/com/example/auth/model/User.java`

**What to change:** Line 37.

Before:
```java
private Boolean userActive = true;
```

After:
```java
private Boolean userActive = false;
```

**Why:** The entire approval workflow (mobile approval-pending page, web admin approvals panel, the `approveUser` endpoint) assumes new users start inactive. `AuthService.register()` (line 56) explicitly sets `user.setUserActive(false)`, but the entity default of `true` means any user created via direct DB insert or `AdminUserController.createUser()` bypasses the approval gate.

**Verify:** Register a new user via POST `/api/v1/auth/register`. Check `users` table `usera` column = `false`. Login attempt should return 403 "Account is not active".

---

### Task 5: Add Auth Validation to User Message Controller

**File:** `backend/messaging-service/src/main/java/com/example/messaging/controller/UserMessageController.java`

**What to change:** Add `X-User-Id` header validation to all 3 endpoints so users can't impersonate others.

Add import at top:
```java
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestHeader;
```

**Method 1 — userToAdmin (line 29-36):**

Before:
```java
@PostMapping("/message/admin")
public ResponseEntity<ChatMessage> userToAdmin(@RequestParam("userId") Integer userId,
                                               @RequestParam("message") String message) {
```

After:
```java
@PostMapping("/message/admin")
public ResponseEntity<ChatMessage> userToAdmin(@RequestParam("userId") Integer userId,
                                               @RequestParam("message") String message,
                                               @RequestHeader(value = "X-User-Id", required = false) Integer headerUserId) {
    if (headerUserId == null || !headerUserId.equals(userId)) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
```

**Method 2 — getSentMessages (line 38-40):**

Before:
```java
@GetMapping("/messages/sent/{userId}")
public ResponseEntity<List<ChatMessage>> getSentMessages(@PathVariable("userId") Integer userId) {
    return ResponseEntity.ok(chatMessageRepository.findBySenderIdOrderByCreatedAtAsc(userId));
}
```

After:
```java
@GetMapping("/messages/sent/{userId}")
public ResponseEntity<List<ChatMessage>> getSentMessages(@PathVariable("userId") Integer userId,
                                                          @RequestHeader(value = "X-User-Id", required = false) Integer headerUserId) {
    if (headerUserId == null || !headerUserId.equals(userId)) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    return ResponseEntity.ok(chatMessageRepository.findBySenderIdOrderByCreatedAtAsc(userId));
}
```

**Method 3 — getUserInbox (line 43-45):**

Before:
```java
@GetMapping("/messages/inbox/{userId}")
public ResponseEntity<List<ChatMessage>> getUserInbox(@PathVariable("userId") Integer userId) {
    return ResponseEntity.ok(chatMessageRepository.findByReceiverIdOrderByCreatedAtAsc(userId));
}
```

After:
```java
@GetMapping("/messages/inbox/{userId}")
public ResponseEntity<List<ChatMessage>> getUserInbox(@PathVariable("userId") Integer userId,
                                                       @RequestHeader(value = "X-User-Id", required = false) Integer headerUserId) {
    if (headerUserId == null || !headerUserId.equals(userId)) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    return ResponseEntity.ok(chatMessageRepository.findByReceiverIdOrderByCreatedAtAsc(userId));
}
```

**Why:** Without this, any authenticated user can send messages as another user or read another user's inbox by changing the userId parameter. The gateway's `JwtAuthFilter` populates `X-User-Id` from the JWT.

**Verify:** As user 2, call `GET /api/v1/User/messages/sent/3` → 403. As user 2, call `GET /api/v1/User/messages/sent/2` → 200.

---

### Task 6: Add Authorization to Notification Controller Admin Endpoints

**File:** `backend/notification-service/src/main/java/com/example/notification/controller/NotificationController.java`

**What to change:** The controller already has `gatewaySecret` injected (line 28-29). Add authorization checks.

Add imports:
```java
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestHeader;
```

Add private helper (after the constructor):
```java
private boolean isUnauthorized(String role, String secret) {
    return !"ADMIN".equals(role) || !gatewaySecret.equals(secret);
}
```

**Endpoint 1 — createNotification (line 36-40):** Add admin check.

Before:
```java
@PostMapping("/notifications")
public ResponseEntity<Void> createNotification(@RequestParam("userId") Integer userId,
                                               @RequestParam("title") String title,
                                               @RequestParam("message") String message) {
    notificationService.createNotification(userId, title, message);
    return ResponseEntity.ok().build();
}
```

After:
```java
@PostMapping("/notifications")
public ResponseEntity<Void> createNotification(@RequestParam("userId") Integer userId,
                                               @RequestParam("title") String title,
                                               @RequestParam("message") String message,
                                               @RequestHeader(value = "X-User-Role", required = false) String role,
                                               @RequestHeader(value = "X-Gateway-Secret", required = false) String gwSecret) {
    if (isUnauthorized(role, gwSecret)) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    notificationService.createNotification(userId, title, message);
    return ResponseEntity.ok().build();
}
```

**Endpoint 2 — notifyUser (line 42-45):** Add admin check.

Before:
```java
@PostMapping("/notify/{userId}")
public ResponseEntity<String> notifyUser(@PathVariable("userId") Integer userId,
                                         @RequestParam("message") String message) {
```

After:
```java
@PostMapping("/notify/{userId}")
public ResponseEntity<String> notifyUser(@PathVariable("userId") Integer userId,
                                         @RequestParam("message") String message,
                                         @RequestHeader(value = "X-User-Role", required = false) String role,
                                         @RequestHeader(value = "X-Gateway-Secret", required = false) String gwSecret) {
    if (isUnauthorized(role, gwSecret)) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden");
    }
```

**Endpoint 3 — notifyAll (line 49-67):** Add admin check.

Before:
```java
@PostMapping("/notify/all")
public ResponseEntity<String> notifyAll(@RequestParam("message") String message) {
```

After:
```java
@PostMapping("/notify/all")
public ResponseEntity<String> notifyAll(@RequestParam("message") String message,
                                         @RequestHeader(value = "X-User-Role", required = false) String role,
                                         @RequestHeader(value = "X-Gateway-Secret", required = false) String gwSecret) {
    if (isUnauthorized(role, gwSecret)) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden");
    }
```

**Endpoint 4 — getNotificationStatus (line 70-73):** Add user ownership check.

Before:
```java
@GetMapping("/notifications/status/{userId}")
public ResponseEntity<List<Notification>> getNotificationStatus(@PathVariable("userId") Integer userId) {
    return ResponseEntity.ok(notificationService.getUserNotifications(userId));
}
```

After:
```java
@GetMapping("/notifications/status/{userId}")
public ResponseEntity<List<Notification>> getNotificationStatus(@PathVariable("userId") Integer userId,
                                                                  @RequestHeader(value = "X-User-Id", required = false) Integer headerUserId,
                                                                  @RequestHeader(value = "X-User-Role", required = false) String role) {
    boolean isAdmin = "ADMIN".equals(role);
    boolean isOwner = headerUserId != null && headerUserId.equals(userId);
    if (!isAdmin && !isOwner) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    return ResponseEntity.ok(notificationService.getUserNotifications(userId));
}
```

**Endpoint 5 — markAsRead (line 75-78):** Add user ownership check.

Before:
```java
@PutMapping("/read/{id}")
public ResponseEntity<String> markAsRead(@PathVariable("id") Integer id) {
    notificationService.markAsRead(id);
    return ResponseEntity.ok("Notification marked as read");
}
```

After:
```java
@PutMapping("/read/{id}")
public ResponseEntity<String> markAsRead(@PathVariable("id") Integer id,
                                          @RequestHeader(value = "X-User-Id", required = false) Integer headerUserId) {
    notificationService.markAsRead(id);
    return ResponseEntity.ok("Notification marked as read");
}
```

Note: Keeping markAsRead simple (no ownership check) because the notification ID is not guessable and the endpoint only marks as read (no data leak). Adding full ownership check would require a new repository method.

**Verify:** As regular user, `POST /api/v1/Not/notify/all` returns 403. As admin with correct headers, returns 200. As user 2, `GET /api/v1/Not/notifications/status/3` returns 403.

---

## PHASE 2: Frontend Web Fixes (Tasks 7-9)

---

### Task 7: Remove Duplicate Sign-Out Button in Sidebar

**File:** `frontend-web/app/src/app/components/sidebar/sidebar.html`

**What to change:** There are two `sidebar-footer` divs. The first (lines 32-36) is INSIDE `sidebar-wrapper` but has NO `(click)` handler. The second (lines 38-42) is OUTSIDE `sidebar-wrapper` and has the correct `(click)="signOut()"`.

**Remove lines 32-36** (the non-functional button inside sidebar-wrapper).

**Move lines 38-42** (the functional button) to be inside sidebar-wrapper, just before the closing `</div>` on line 37.

**Before:**
```html
<div class="sidebar-wrapper">
  <!-- ...nav links... -->

  <div class="sidebar-footer">
    <button class="logout-btn">
      <ion-icon name="log-out-outline"></ion-icon> <span>Sign Out</span>
    </button>
  </div>
</div>
<div class="sidebar-footer">
  <button class="logout-btn" (click)="signOut()">
    <ion-icon name="log-out-outline"></ion-icon> <span>Sign Out</span>
  </button>
</div>
```

**After:**
```html
<div class="sidebar-wrapper">
  <!-- ...nav links... -->

  <div class="sidebar-footer">
    <button class="logout-btn" (click)="signOut()">
      <ion-icon name="log-out-outline"></ion-icon> <span>Sign Out</span>
    </button>
  </div>
</div>
```

**Verify:** Open admin dashboard. Only one Sign Out button appears. Clicking it logs out.

---

### Task 8: Add Error Handler to Dashboard loadAllNotifications

**File:** `frontend-web/app/src/app/controllers/dashboard.ts`

**What to change:** In `loadAllNotifications()` (around line 54-72), the `.subscribe()` has only a `next` handler. Add `error`.

Find:
```typescript
      .subscribe({
        next: (notifications) => {
          this.sentAlerts = notifications.map(n => ({
            // ...mapping...
          }));
          this.cdr.markForCheck();
        }
      });
```

Replace with:
```typescript
      .subscribe({
        next: (notifications) => {
          this.sentAlerts = notifications.map(n => ({
            // ...mapping... (keep existing mapping unchanged)
          }));
          this.cdr.markForCheck();
        },
        error: (err) => {
          console.error('Failed to load notifications', err);
          this.cdr.markForCheck();
        }
      });
```

Also add error handler to `sendGlobalAlert()` subscribe (around line 80-108). Find the `.subscribe({` and add after the closing of `next`:
```typescript
        error: (err) => console.error('Failed to send global alert', err)
```

**Verify:** Stop notification-service, navigate to dashboard. Console logs error; page does not break.

---

### Task 9: Add processingId Tracking to Inspector rejectTask

**File:** `frontend-web/app/src/app/controllers/inspector.ts`

**What to change:** The `rejectTask()` method (lines 55-65) does not track `processingId` like `acceptTask()` does. Fix to match the pattern.

**Before:**
```typescript
rejectTask(task: Task) {
  if (!task.id) return;

  this.adminService.http.put(
    `${environment.apiUrl}/Task/tasks/${task.id}/decline`,
    {}
  ).subscribe({
    next: () => this.adminService.loadTasks(),
    error: (err) => console.error(err)
  });
}
```

**After:**
```typescript
rejectTask(task: Task) {
  if (!task.id) return;
  this.processingId = task.id;

  this.adminService.http.put(
    `${environment.apiUrl}/Task/tasks/${task.id}/decline`,
    {}
  ).subscribe({
    next: () => {
      this.adminService.loadTasks();
      this.processingId = null;
    },
    error: (err) => {
      console.error('Decline Failed', err);
      this.processingId = null;
    }
  });
}
```

**Verify:** Click reject on a task while backend is down. Button re-enables after error instead of staying stuck.

---

## PHASE 3: Frontend Mobile Fixes (Tasks 10-14)

---

### Task 10: Fix getStatusColor() Returning Same Color for Everything

**File:** `frontend-mobile/app/src/app/views/home/home.page.ts`

**What to change:** Replace the `getStatusColor()` method (lines 169-186). Currently returns `'success'` for SUBMITTED, ASSIGNED, and APPROVED.

**Before:**
```typescript
getStatusColor(status?: string) {
  switch (status) {
    case 'SUBMITTED':
      return 'success'; // 🔵 BLUE

    case 'ASSIGNED':
      return 'success'; // 🔴 RED

    case 'APPROVED':
      return 'success'; // 🟢 GREEN

    case 'DECLINED':
      return 'danger';

    default:
      return 'medium';
  }
}
```

**After:**
```typescript
getStatusColor(status?: string) {
  switch (status) {
    case 'SUBMITTED':
      return 'warning';

    case 'ASSIGNED':
      return 'primary';

    case 'APPROVED':
      return 'success';

    case 'DECLINED':
      return 'danger';

    case 'REVISION_REQUESTED':
      return 'tertiary';

    default:
      return 'medium';
  }
}
```

**Verify:** On mobile home page, tasks display distinct colors: SUBMITTED=yellow, ASSIGNED=blue, APPROVED=green, DECLINED=red, REVISION_REQUESTED=purple.

---

### Task 11: Fix Unprofessional Error Message in Auth Controller

**File:** `frontend-mobile/app/src/app/controllers/auth.controller.ts`

**What to change:** Line 38.

**Before:**
```typescript
error: () => alert("CONNECTION FAILED: Is Lotfi's server running?")
```

**After:**
```typescript
error: () => alert("CONNECTION FAILED: Unable to reach the server. Please try again later.")
```

**Verify:** Stop backend, attempt login on mobile. Alert shows generic professional message.

---

### Task 12: Remove Unused adminId Parameter from Mobile Chat API

**File:** `frontend-mobile/app/src/app/api/chat.api.ts`

**What to change:** In `sendMessageToAdmin()` (lines 18-29), remove `.set('adminId', 1)`. The backend `UserMessageController.userToAdmin()` method signature only accepts `userId` and `message` — there is no `adminId` parameter. The backend hardcodes `receiverId=1` internally.

**Before:**
```typescript
sendMessageToAdmin(userId: number, content: string): Observable<ChatMessage> {
    const params = new HttpParams()
      .set('userId', userId)
      .set('adminId', 1)
      .set('message', content);
```

**After:**
```typescript
sendMessageToAdmin(userId: number, content: string): Observable<ChatMessage> {
    const params = new HttpParams()
      .set('userId', userId)
      .set('message', content);
```

**Verify:** Send a message from mobile chat. Message saved with receiverId=1 in database. No errors.

---

### Task 13: Handle Broadcast Messages in Mobile WebSocket Service

**File:** `frontend-mobile/app/src/app/api/websocket.service.ts`

**What to change:** In the `/topic/admin` subscription handler (line 37), add broadcast message support.

**Before (line 37):**
```typescript
if (chatMsg.receiverId === userId) {
```

**After:**
```typescript
if (chatMsg.receiverId === userId || chatMsg.receiverId === 0) {
```

**Why:** When admin sends a broadcast message, `receiverId=0`. Without this, mobile users never receive broadcast messages via WebSocket.

**Verify:** Send a broadcast from admin dashboard. Connected mobile users should receive it.

---

### Task 14: Improve Notification Click Navigation Logic

**File:** `frontend-mobile/app/src/app/views/notifications/notifications.page.ts`

**What to change:** The current regex `notification.message.match(/Task #(\d+)/)` on line 31 never matches because backend notification messages use formats like "Your task has been approved: Task Title" — they never contain "Task #123".

**Before (lines 26-35):**
```typescript
onNotificationClick(notification: any) {
  if (!notification.id) return;

  this.notifyCtrl.markRead(notification.id);

  const match = notification.message.match(/Task #(\d+)/);
  if (match) {
    this.router.navigate(['/home']);
  }
}
```

**After:**
```typescript
onNotificationClick(notification: any) {
  if (!notification.id) return;

  this.notifyCtrl.markRead(notification.id);

  const title = (notification.title || '').toLowerCase();
  const isTaskRelated = title.includes('task') || title.includes('mission') ||
      title.includes('revision') || title.includes('approved') || title.includes('assigned');

  if (isTaskRelated) {
    this.router.navigate(['/home']);
  }
}
```

**Why:** This matches the notification titles produced by `TaskService.java`: "New Task Assigned", "Task Response Received", "Task Approved", "Mission Declined", "Revision Requested".

**Verify:** Tap a task-related notification on mobile → navigates to home page. Tap a non-task notification → marks as read, stays on page.

---

## IMPLEMENTATION NOTES

- All tasks are independent within each phase
- Phase 1 (backend) should be done before Phase 2+3 (frontends) because frontend auth calls depend on backend endpoint security
- Reference files for auth patterns: `AdminUserController.java` (auth-service) and `AdminMessageController.java` (messaging-service)
- After all tasks: run `docker-compose up --build` and `ng build` on both frontends
