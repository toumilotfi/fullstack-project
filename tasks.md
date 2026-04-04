# Microservices Migration — Task List

> Each task is atomic and sequential. Complete them in order.
> Read `plan.md` first for full architecture context.
> Reference files from the old monolith at `backend/ap/src/main/java/com/example/ap/`.

---

## Phase 1: Project Scaffolding

### Task 1.1 — Create Parent POM
**File**: `backend/pom.xml`
**Action**: Create a new Maven parent POM.

```xml
<groupId>com.example</groupId>
<artifactId>fullstack-backend</artifactId>
<version>1.0.0</version>
<packaging>pom</packaging>
```

Properties:
- `java.version`: 21
- `spring-boot.version`: 3.2.2
- `spring-cloud.version`: 2023.0.0

`<dependencyManagement>`:
- Import `spring-boot-dependencies` BOM (version `${spring-boot.version}`, scope=import, type=pom)
- Import `spring-cloud-dependencies` BOM (version `${spring-cloud.version}`, scope=import, type=pom)

`<modules>`:
- shared-lib
- service-registry
- api-gateway
- auth-service
- task-service
- messaging-service
- notification-service

**Verify**: `cd backend && mvn validate` succeeds (will fail until modules exist — that's OK)

---

### Task 1.2 — Create shared-lib Module
**Directory**: `backend/shared-lib/`

**File**: `backend/shared-lib/pom.xml`
- Parent: `com.example:fullstack-backend:1.0.0` with `<relativePath>../pom.xml</relativePath>`
- `artifactId`: shared-lib
- Dependencies:
  - `com.fasterxml.jackson.core:jackson-databind` (no version — from parent BOM)
  - `io.jsonwebtoken:jjwt-api:0.12.5`
  - `io.jsonwebtoken:jjwt-impl:0.12.5` (scope=runtime)
  - `io.jsonwebtoken:jjwt-jackson:0.12.5` (scope=runtime)
  - `org.springframework.boot:spring-boot-starter` (for @Value, @Component)

**File**: `backend/shared-lib/src/main/java/com/example/shared/dto/UserDTO.java`
```java
package com.example.shared.dto;

public class UserDTO {
    private Integer id;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
    private Boolean userActive;

    public UserDTO() {}

    public UserDTO(Integer id, String email, String firstName, String lastName, String role, Boolean userActive) {
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.userActive = userActive;
    }

    // Generate all getters and setters
}
```

**File**: `backend/shared-lib/src/main/java/com/example/shared/dto/AuthResponse.java`
```java
package com.example.shared.dto;

public class AuthResponse {
    private String token;
    private UserDTO user;

    public AuthResponse() {}
    public AuthResponse(String token, UserDTO user) {
        this.token = token;
        this.user = user;
    }
    // Getters and setters
}
```

**File**: `backend/shared-lib/src/main/java/com/example/shared/dto/LoginRequest.java`
```java
package com.example.shared.dto;

public class LoginRequest {
    private String email;
    private String password;

    public LoginRequest() {}
    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }
    // Getters and setters
}
```

**File**: `backend/shared-lib/src/main/java/com/example/shared/event/NotificationEvent.java`
```java
package com.example.shared.event;

import java.io.Serializable;

public class NotificationEvent implements Serializable {
    private Integer userId;  // null = notify admins
    private String title;
    private String message;
    private String eventType; // TASK_ASSIGNED, TASK_RESPONDED, TASK_APPROVED, TASK_DECLINED, TASK_REVISION

    public NotificationEvent() {}
    public NotificationEvent(Integer userId, String title, String message, String eventType) {
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.eventType = eventType;
    }
    // Getters and setters
}
```

**File**: `backend/shared-lib/src/main/java/com/example/shared/event/EmailEvent.java`
```java
package com.example.shared.event;

import java.io.Serializable;

public class EmailEvent implements Serializable {
    private String toEmail;
    private String subject;
    private String body;
    private boolean html;

    public EmailEvent() {}
    public EmailEvent(String toEmail, String subject, String body, boolean html) {
        this.toEmail = toEmail;
        this.subject = subject;
        this.body = body;
        this.html = html;
    }
    // Getters and setters
}
```

**File**: `backend/shared-lib/src/main/java/com/example/shared/event/UserApprovedEvent.java`
```java
package com.example.shared.event;

import java.io.Serializable;

public class UserApprovedEvent implements Serializable {
    private Integer userId;
    private String email;

    public UserApprovedEvent() {}
    public UserApprovedEvent(Integer userId, String email) {
        this.userId = userId;
        this.email = email;
    }
    // Getters and setters
}
```

**File**: `backend/shared-lib/src/main/java/com/example/shared/security/JwtUtil.java`
```java
package com.example.shared.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class JwtUtil {
    private final SecretKey key;
    private static final long EXPIRATION = 24 * 60 * 60 * 1000; // 24 hours

    public JwtUtil(String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Integer userId, String email, String role) {
        return Jwts.builder()
                .subject(email)
                .claim("userId", userId)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(key)
                .compact();
    }

    public Claims extractClaims(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = extractClaims(token);
            return !claims.getExpiration().before(new Date());
        } catch (JwtException e) {
            return false;
        }
    }

    public Integer extractUserId(String token) {
        return extractClaims(token).get("userId", Integer.class);
    }

    public String extractRole(String token) {
        return extractClaims(token).get("role", String.class);
    }

    public String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }
}
```

**Verify**: `cd backend/shared-lib && mvn clean install` — must succeed.

---

### Task 1.3 — Create Service Registry
**Directory**: `backend/service-registry/`

**File**: `backend/service-registry/pom.xml`
- Parent: com.example:fullstack-backend:1.0.0
- artifactId: service-registry
- Dependencies:
  - `org.springframework.cloud:spring-cloud-starter-netflix-eureka-server`

**File**: `backend/service-registry/src/main/java/com/example/registry/ServiceRegistryApplication.java`
```java
package com.example.registry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class ServiceRegistryApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceRegistryApplication.class, args);
    }
}
```

**File**: `backend/service-registry/src/main/resources/application.yml`
```yaml
server:
  port: 8761

spring:
  application:
    name: service-registry

eureka:
  client:
    register-with-eureka: false
    fetch-registry: false
  server:
    enable-self-preservation: false
```

**File**: `backend/service-registry/Dockerfile`
```dockerfile
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8761
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Verify**: `cd backend/service-registry && mvn clean package -DskipTests` — must produce jar.

---

## Phase 2: Auth Service

### Task 2.1 — Create auth-service Scaffold
**Directory**: `backend/auth-service/`

**File**: `backend/auth-service/pom.xml`
- Parent: com.example:fullstack-backend:1.0.0
- artifactId: auth-service
- Dependencies:
  - `org.springframework.boot:spring-boot-starter-web`
  - `org.springframework.boot:spring-boot-starter-data-jpa`
  - `org.springframework.boot:spring-boot-starter-security`
  - `org.springframework.boot:spring-boot-starter-validation`
  - `org.springframework.boot:spring-boot-starter-amqp`
  - `org.springframework.boot:spring-boot-starter-actuator`
  - `org.springframework.cloud:spring-cloud-starter-netflix-eureka-client`
  - `org.postgresql:postgresql` (scope=runtime)
  - `com.example:shared-lib:1.0.0`
  - `org.projectlombok:lombok:1.18.32` (scope=provided)
- Build plugin: spring-boot-maven-plugin (exclude lombok)

**File**: `backend/auth-service/src/main/resources/application.yml`
```yaml
server:
  port: 8081

spring:
  application:
    name: auth-service
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${AUTH_DB_PORT:5433}/auth_db
    username: ${DB_USERNAME:lotfi}
    password: ${DB_PASSWORD:92570533Lt@}
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
  rabbitmq:
    host: ${RABBITMQ_HOST:localhost}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USERNAME:guest}
    password: ${RABBITMQ_PASSWORD:guest}

jwt:
  secret: ${JWT_SECRET:my-super-secret-key-that-is-at-least-32-characters-long}

password:
  migration:
    enabled: false

eureka:
  client:
    service-url:
      defaultZone: ${EUREKA_URL:http://localhost:8761/eureka/}
```

**File**: `backend/auth-service/src/main/java/com/example/auth/AuthServiceApplication.java`
```java
package com.example.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class AuthServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}
```

**File**: `backend/auth-service/Dockerfile`
```dockerfile
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

### Task 2.2 — Auth Service: User Model
**File**: `backend/auth-service/src/main/java/com/example/auth/model/User.java`

**CRITICAL**: Copy the entity from `backend/ap/src/main/java/com/example/ap/models/User.java` and preserve ALL `@Column` names exactly:
- `@Column(name = "fName")` for firstName
- `@Column(name = "lName")` for lastName
- `@Column(name = "password")` for secretPassword
- `@Column(name = "userA")` for userActive
- `@Column(name = "created_at")` for createdAt

**ADD** this new field:
```java
@Column(name = "role")
private String role = "USER";
```

Add getter/setter for role. Keep `@Table(name = "users")` and `@Inheritance(strategy = InheritanceType.JOINED)`.

---

### Task 2.3 — Auth Service: Repository
**File**: `backend/auth-service/src/main/java/com/example/auth/repository/UserRepository.java`

```java
package com.example.auth.repository;

import com.example.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);
}
```

---

### Task 2.4 — Auth Service: Config Classes

**File**: `backend/auth-service/src/main/java/com/example/auth/config/SecurityConfig.java`
```java
package com.example.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

**File**: `backend/auth-service/src/main/java/com/example/auth/config/JwtConfig.java`
```java
package com.example.auth.config;

import com.example.shared.security.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfig {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Bean
    public JwtUtil jwtUtil() {
        return new JwtUtil(jwtSecret);
    }
}
```

**File**: `backend/auth-service/src/main/java/com/example/auth/config/RabbitConfig.java`
```java
package com.example.auth.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String EVENT_EXCHANGE = "event_exchange";

    @Bean
    public TopicExchange eventExchange() {
        return new TopicExchange(EVENT_EXCHANGE);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }
}
```

---

### Task 2.5 — Auth Service: AuthService (Business Logic)
**File**: `backend/auth-service/src/main/java/com/example/auth/service/AuthService.java`

Reference: `backend/ap/src/main/java/com/example/ap/Service/AuthService.java`

```java
package com.example.auth.service;

import com.example.auth.config.RabbitConfig;
import com.example.auth.model.User;
import com.example.auth.repository.UserRepository;
import com.example.shared.dto.AuthResponse;
import com.example.shared.dto.UserDTO;
import com.example.shared.event.EmailEvent;
import com.example.shared.security.JwtUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RabbitTemplate rabbitTemplate;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil, RabbitTemplate rabbitTemplate) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.rabbitTemplate = rabbitTemplate;
    }

    public AuthResponse login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid email or password."));

        if (!passwordEncoder.matches(password, user.getSecretPassword())) {
            throw new RuntimeException("Invalid email or password.");
        }

        if (!user.getUserActive()) {
            throw new RuntimeException("Account is not active. Please wait for admin approval.");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole());
        UserDTO userDTO = toDTO(user);
        return new AuthResponse(token, userDTO);
    }

    public UserDTO register(User user) {
        user.setSecretPassword(passwordEncoder.encode(user.getSecretPassword()));
        user.setCreatedAt(LocalDateTime.now());
        user.setUserActive(false);
        user.setRole("USER");
        User saved = userRepository.save(user);
        return toDTO(saved);
    }

    public UserDTO updateUser(Integer id, User updatedUser) {
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (updatedUser.getFirstName() != null) existing.setFirstName(updatedUser.getFirstName());
        if (updatedUser.getLastName() != null) existing.setLastName(updatedUser.getLastName());
        if (updatedUser.getSecretPassword() != null && !updatedUser.getSecretPassword().isEmpty()) {
            existing.setSecretPassword(passwordEncoder.encode(updatedUser.getSecretPassword()));
        }
        User saved = userRepository.save(existing);
        return toDTO(saved);
    }

    public String forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email not found"));
        String tempPassword = UUID.randomUUID().toString().substring(0, 8);
        user.setSecretPassword(passwordEncoder.encode(tempPassword));
        userRepository.save(user);

        EmailEvent event = new EmailEvent(email, "Password Reset",
                "Your temporary password is: " + tempPassword, false);
        rabbitTemplate.convertAndSend(RabbitConfig.EVENT_EXCHANGE, "event.email.forgot-password", event);

        return "Temporary password sent to your email.";
    }

    public UserDTO toDTO(User user) {
        return new UserDTO(user.getId(), user.getEmail(), user.getFirstName(),
                user.getLastName(), user.getRole(), user.getUserActive());
    }
}
```

---

### Task 2.6 — Auth Service: UserService (Admin Operations)
**File**: `backend/auth-service/src/main/java/com/example/auth/service/UserService.java`

Reference: `backend/ap/src/main/java/com/example/ap/Controllers/AdminController.java`

```java
package com.example.auth.service;

import com.example.auth.config.RabbitConfig;
import com.example.auth.model.User;
import com.example.auth.repository.UserRepository;
import com.example.shared.dto.UserDTO;
import com.example.shared.event.EmailEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RabbitTemplate rabbitTemplate;
    private final AuthService authService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       RabbitTemplate rabbitTemplate, AuthService authService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.rabbitTemplate = rabbitTemplate;
        this.authService = authService;
    }

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream().map(authService::toDTO).collect(Collectors.toList());
    }

    public UserDTO getUserById(Integer id) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        return authService.toDTO(user);
    }

    public UserDTO createUser(User user) {
        user.setSecretPassword(passwordEncoder.encode(user.getSecretPassword()));
        user.setCreatedAt(LocalDateTime.now());
        user.setUserActive(true);
        if (user.getRole() == null) user.setRole("USER");
        User saved = userRepository.save(user);
        return authService.toDTO(saved);
    }

    public UserDTO updateUser(Integer id, User updatedUser) {
        User existing = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        if (updatedUser.getFirstName() != null) existing.setFirstName(updatedUser.getFirstName());
        if (updatedUser.getLastName() != null) existing.setLastName(updatedUser.getLastName());
        if (updatedUser.getEmail() != null) existing.setEmail(updatedUser.getEmail());
        if (updatedUser.getSecretPassword() != null && !updatedUser.getSecretPassword().isEmpty()) {
            existing.setSecretPassword(passwordEncoder.encode(updatedUser.getSecretPassword()));
        }
        if (updatedUser.getUserActive() != null) existing.setUserActive(updatedUser.getUserActive());
        User saved = userRepository.save(existing);
        return authService.toDTO(saved);
    }

    public void deleteUser(Integer id) {
        userRepository.deleteById(id);
    }

    public UserDTO approveUser(Integer id) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        user.setUserActive(true);
        userRepository.save(user);

        // Publish email event for approval email
        String htmlBody = "<html><body><h1>Account Approved</h1>"
                + "<p>Your account has been approved. You can now login.</p>"
                + "<a href='http://localhost:3000/login'>Login Here</a></body></html>";
        EmailEvent emailEvent = new EmailEvent(user.getEmail(), "Account Approved", htmlBody, true);
        rabbitTemplate.convertAndSend(RabbitConfig.EVENT_EXCHANGE, "event.email.approval", emailEvent);

        return authService.toDTO(user);
    }

    public String testEmail() {
        EmailEvent event = new EmailEvent("test@test.com", "Test Email", "This is a test email.", false);
        rabbitTemplate.convertAndSend(RabbitConfig.EVENT_EXCHANGE, "event.email.test", event);
        return "Test email event published.";
    }
}
```

---

### Task 2.7 — Auth Service: Controllers

**File**: `backend/auth-service/src/main/java/com/example/auth/controller/AuthController.java`
```java
package com.example.auth.controller;

import com.example.auth.model.User;
import com.example.auth.service.AuthService;
import com.example.shared.dto.AuthResponse;
import com.example.shared.dto.LoginRequest;
import com.example.shared.dto.UserDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<UserDTO> register(@RequestBody User user) {
        return ResponseEntity.ok(authService.register(user));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Integer id, @RequestBody User user) {
        return ResponseEntity.ok(authService.updateUser(id, user));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestParam String email) {
        return ResponseEntity.ok(authService.forgotPassword(email));
    }
}
```

**File**: `backend/auth-service/src/main/java/com/example/auth/controller/UserLogoutController.java`
```java
package com.example.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/User")
public class UserLogoutController {

    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        return ResponseEntity.ok("Logout successful");
    }
}
```

**File**: `backend/auth-service/src/main/java/com/example/auth/controller/AdminUserController.java`
```java
package com.example.auth.controller;

import com.example.auth.model.User;
import com.example.auth.service.UserService;
import com.example.shared.dto.UserDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> getAllUsers(@RequestHeader(value = "X-User-Role", required = false) String role) {
        if (!"ADMIN".equals(role)) return ResponseEntity.status(403).build();
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Integer id,
                                                @RequestHeader(value = "X-User-Role", required = false) String role) {
        if (!"ADMIN".equals(role)) return ResponseEntity.status(403).build();
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PostMapping("/users/new")
    public ResponseEntity<UserDTO> createUser(@RequestBody User user,
                                               @RequestHeader(value = "X-User-Role", required = false) String role) {
        if (!"ADMIN".equals(role)) return ResponseEntity.status(403).build();
        return ResponseEntity.ok(userService.createUser(user));
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Integer id, @RequestBody User user,
                                               @RequestHeader(value = "X-User-Role", required = false) String role) {
        if (!"ADMIN".equals(role)) return ResponseEntity.status(403).build();
        return ResponseEntity.ok(userService.updateUser(id, user));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Integer id,
                                              @RequestHeader(value = "X-User-Role", required = false) String role) {
        if (!"ADMIN".equals(role)) return ResponseEntity.status(403).build();
        userService.deleteUser(id);
        return ResponseEntity.ok("User deleted successfully");
    }

    @PutMapping("/users/approve/{id}")
    public ResponseEntity<UserDTO> approveUser(@PathVariable Integer id,
                                                @RequestHeader(value = "X-User-Role", required = false) String role) {
        if (!"ADMIN".equals(role)) return ResponseEntity.status(403).build();
        return ResponseEntity.ok(userService.approveUser(id));
    }

    @GetMapping("/test-email")
    public ResponseEntity<String> testEmail(@RequestHeader(value = "X-User-Role", required = false) String role) {
        if (!"ADMIN".equals(role)) return ResponseEntity.status(403).build();
        return ResponseEntity.ok(userService.testEmail());
    }
}
```

---

### Task 2.8 — Auth Service: Password Migration Runner
**File**: `backend/auth-service/src/main/java/com/example/auth/config/PasswordMigrationRunner.java`

```java
package com.example.auth.config;

import com.example.auth.model.User;
import com.example.auth.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@ConditionalOnProperty(name = "password.migration.enabled", havingValue = "true")
public class PasswordMigrationRunner implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public PasswordMigrationRunner(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        List<User> users = userRepository.findAll();
        for (User user : users) {
            String pwd = user.getSecretPassword();
            if (pwd != null && !pwd.startsWith("$2a$") && !pwd.startsWith("$2b$")) {
                user.setSecretPassword(passwordEncoder.encode(pwd));
                userRepository.save(user);
                System.out.println("Migrated password for user: " + user.getEmail());
            }
        }
        System.out.println("Password migration complete.");
    }
}
```

**Verify**: `cd backend/auth-service && mvn clean package -DskipTests` — must produce jar.

---

## Phase 3: Task Service

### Task 3.1 — Create task-service
**Directory**: `backend/task-service/`

**File**: `backend/task-service/pom.xml`
- Parent: com.example:fullstack-backend:1.0.0
- artifactId: task-service
- Dependencies: spring-boot-starter-web, spring-boot-starter-data-jpa, spring-boot-starter-amqp, spring-boot-starter-actuator, spring-cloud-starter-netflix-eureka-client, postgresql (runtime), shared-lib, lombok

**File**: `backend/task-service/src/main/resources/application.yml`
```yaml
server:
  port: 8082

spring:
  application:
    name: task-service
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${TASK_DB_PORT:5434}/task_db
    username: ${DB_USERNAME:lotfi}
    password: ${DB_PASSWORD:92570533Lt@}
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
  rabbitmq:
    host: ${RABBITMQ_HOST:localhost}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USERNAME:guest}
    password: ${RABBITMQ_PASSWORD:guest}

eureka:
  client:
    service-url:
      defaultZone: ${EUREKA_URL:http://localhost:8761/eureka/}
```

**File**: `backend/task-service/src/main/java/com/example/task/TaskServiceApplication.java`
```java
package com.example.task;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class TaskServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(TaskServiceApplication.class, args);
    }
}
```

**File**: `backend/task-service/Dockerfile`
```dockerfile
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

### Task 3.2 — Task Service: Model + Repository

**File**: `backend/task-service/src/main/java/com/example/task/model/Task.java`
- Copy from `backend/ap/src/main/java/com/example/ap/models/Task.java`
- Change package to `com.example.task.model`
- Keep ALL @Column names exactly as monolith

**File**: `backend/task-service/src/main/java/com/example/task/repository/TaskRepository.java`
```java
package com.example.task.repository;

import com.example.task.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Integer> {
    List<Task> findByAssignedToUserId(Integer userId);
}
```

---

### Task 3.3 — Task Service: Service + RabbitConfig

**File**: `backend/task-service/src/main/java/com/example/task/config/RabbitConfig.java`
```java
package com.example.task.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    public static final String EVENT_EXCHANGE = "event_exchange";

    @Bean
    public TopicExchange eventExchange() {
        return new TopicExchange(EVENT_EXCHANGE);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }
}
```

**File**: `backend/task-service/src/main/java/com/example/task/service/TaskService.java`

Reference: `backend/ap/src/main/java/com/example/ap/Service/TaskService.java` and `backend/ap/src/main/java/com/example/ap/Controllers/TasksController.java`

The key change: instead of calling `notificationService.createNotification()`, publish `NotificationEvent` to RabbitMQ.

```java
package com.example.task.service;

import com.example.shared.event.NotificationEvent;
import com.example.task.config.RabbitConfig;
import com.example.task.model.Task;
import com.example.task.repository.TaskRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final RabbitTemplate rabbitTemplate;

    public TaskService(TaskRepository taskRepository, RabbitTemplate rabbitTemplate) {
        this.taskRepository = taskRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    public Task getTaskById(Integer id) {
        return taskRepository.findById(id).orElseThrow(() -> new RuntimeException("Task not found"));
    }

    public Task createTask(Task task) {
        task.setCreatedAt(LocalDateTime.now());
        task.setStatus("ASSIGNED");
        Task saved = taskRepository.save(task);

        publishNotification(task.getAssignedToUserId(), "New Task Assigned",
                "You have been assigned: " + task.getTitle(), "TASK_ASSIGNED");
        return saved;
    }

    public Task updateTask(Integer id, Task updatedTask) {
        Task existing = taskRepository.findById(id).orElseThrow(() -> new RuntimeException("Task not found"));
        if (updatedTask.getTitle() != null) existing.setTitle(updatedTask.getTitle());
        if (updatedTask.getDescription() != null) existing.setDescription(updatedTask.getDescription());
        if (updatedTask.getAssignedToUserId() != null) existing.setAssignedToUserId(updatedTask.getAssignedToUserId());
        return taskRepository.save(existing);
    }

    public void deleteTask(Integer id) {
        taskRepository.deleteById(id);
    }

    public Task respondToTask(Integer id, String response) {
        Task task = taskRepository.findById(id).orElseThrow(() -> new RuntimeException("Task not found"));
        task.setUserResponse(response);
        task.setStatus("SUBMITTED");
        task.setResponseAt(LocalDateTime.now());
        Task saved = taskRepository.save(task);

        // Notify admins (userId=null means admin notification)
        publishNotification(null, "Task Response Received",
                "A response was submitted for task: " + task.getTitle(), "TASK_RESPONDED");
        return saved;
    }

    public Task approveTask(Integer id) {
        Task task = taskRepository.findById(id).orElseThrow(() -> new RuntimeException("Task not found"));
        task.setStatus("APPROVED");
        Task saved = taskRepository.save(task);

        publishNotification(task.getAssignedToUserId(), "Task Approved",
                "Your task has been approved: " + task.getTitle(), "TASK_APPROVED");
        return saved;
    }

    public Task declineTask(Integer id) {
        Task task = taskRepository.findById(id).orElseThrow(() -> new RuntimeException("Task not found"));
        task.setStatus("DECLINED");
        Task saved = taskRepository.save(task);

        // Notify admins about decline
        publishNotification(null, "Mission Declined",
                "Task declined: " + task.getTitle(), "TASK_DECLINED");
        return saved;
    }

    public Task requestRevision(Integer id) {
        Task task = taskRepository.findById(id).orElseThrow(() -> new RuntimeException("Task not found"));
        task.setStatus("REVISION_REQUESTED");
        Task saved = taskRepository.save(task);

        publishNotification(task.getAssignedToUserId(), "Revision Requested",
                "Please revise your task: " + task.getTitle(), "TASK_REVISION");
        return saved;
    }

    public List<Task> getTasksForUser(Integer userId) {
        return taskRepository.findByAssignedToUserId(userId);
    }

    private void publishNotification(Integer userId, String title, String message, String eventType) {
        NotificationEvent event = new NotificationEvent(userId, title, message, eventType);
        rabbitTemplate.convertAndSend(RabbitConfig.EVENT_EXCHANGE, "event.notification.task", event);
    }
}
```

---

### Task 3.4 — Task Service: Controller

**File**: `backend/task-service/src/main/java/com/example/task/controller/TaskController.java`

Reference: `backend/ap/src/main/java/com/example/ap/Controllers/TasksController.java`

```java
package com.example.task.controller;

import com.example.task.model.Task;
import com.example.task.service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/Task")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping("/tasks")
    public ResponseEntity<Task> createTask(@RequestBody Task task) {
        return ResponseEntity.ok(taskService.createTask(task));
    }

    @GetMapping("/tasks")
    public ResponseEntity<List<Task>> getAllTasks() {
        return ResponseEntity.ok(taskService.getAllTasks());
    }

    @GetMapping("/tasks/{id}")
    public ResponseEntity<Task> getTask(@PathVariable Integer id) {
        return ResponseEntity.ok(taskService.getTaskById(id));
    }

    @PutMapping("/tasks/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable Integer id, @RequestBody Task task) {
        return ResponseEntity.ok(taskService.updateTask(id, task));
    }

    @DeleteMapping("/tasks/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Integer id) {
        taskService.deleteTask(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/tasks/{id}/respond")
    public ResponseEntity<Task> respondToTask(@PathVariable Integer id, @RequestBody String response) {
        return ResponseEntity.ok(taskService.respondToTask(id, response));
    }

    @PutMapping("/tasks/approve/{id}")
    public ResponseEntity<Task> approveTask(@PathVariable Integer id) {
        return ResponseEntity.ok(taskService.approveTask(id));
    }

    @PutMapping("/tasks/{id}/decline")
    public ResponseEntity<Task> declineTask(@PathVariable Integer id) {
        return ResponseEntity.ok(taskService.declineTask(id));
    }

    @PutMapping("/tasks/{id}/request-revision")
    public ResponseEntity<Task> requestRevision(@PathVariable Integer id) {
        return ResponseEntity.ok(taskService.requestRevision(id));
    }
}
```

**Verify**: `cd backend/task-service && mvn clean package -DskipTests`

---

## Phase 4: Messaging Service

### Task 4.1 — Create messaging-service Scaffold
**Directory**: `backend/messaging-service/`

**File**: `backend/messaging-service/pom.xml`
- Parent: com.example:fullstack-backend:1.0.0
- artifactId: messaging-service
- Dependencies: spring-boot-starter-web, spring-boot-starter-data-jpa, spring-boot-starter-amqp, spring-boot-starter-websocket, spring-boot-starter-actuator, spring-cloud-starter-netflix-eureka-client, postgresql (runtime), shared-lib, lombok

**File**: `backend/messaging-service/src/main/resources/application.yml`
```yaml
server:
  port: 8083

spring:
  application:
    name: messaging-service
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${MESSAGING_DB_PORT:5435}/messaging_db
    username: ${DB_USERNAME:lotfi}
    password: ${DB_PASSWORD:92570533Lt@}
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
  rabbitmq:
    host: ${RABBITMQ_HOST:localhost}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USERNAME:guest}
    password: ${RABBITMQ_PASSWORD:guest}

eureka:
  client:
    service-url:
      defaultZone: ${EUREKA_URL:http://localhost:8761/eureka/}
```

**File**: `backend/messaging-service/src/main/java/com/example/messaging/MessagingServiceApplication.java`
```java
package com.example.messaging;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class MessagingServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(MessagingServiceApplication.class, args);
    }
}
```

**File**: `backend/messaging-service/Dockerfile`
```dockerfile
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

### Task 4.2 — Messaging Service: Model + Repository

**File**: `backend/messaging-service/src/main/java/com/example/messaging/model/ChatMessage.java`
- Copy from `backend/ap/src/main/java/com/example/ap/models/ChatMessage.java`
- Change package to `com.example.messaging.model`
- Keep ALL @Column names exactly

**File**: `backend/messaging-service/src/main/java/com/example/messaging/repository/ChatMessageRepository.java`
```java
package com.example.messaging.repository;

import com.example.messaging.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Integer> {
    List<ChatMessage> findBySenderIdOrderByCreatedAtAsc(Integer senderId);
    List<ChatMessage> findByReceiverIdOrderByCreatedAtAsc(Integer receiverId);
}
```

---

### Task 4.3 — Messaging Service: Config (RabbitMQ + WebSocket)

**File**: `backend/messaging-service/src/main/java/com/example/messaging/config/RabbitConfig.java`

Reference: `backend/ap/src/main/java/com/example/ap/rabbit/RabbitConfig.java`

```java
package com.example.messaging.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "chat_exchange";
    public static final String ADMIN_QUEUE = "admin_queue";
    public static final String USER_QUEUE = "user_queue";

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(EXCHANGE);
    }

    @Bean
    public Queue adminQueue() {
        return new Queue(ADMIN_QUEUE);
    }

    @Bean
    public Queue userQueue() {
        return new Queue(USER_QUEUE);
    }

    @Bean
    public Binding bindingAdmin(Queue adminQueue, DirectExchange exchange) {
        return BindingBuilder.bind(adminQueue).to(exchange).with("admin");
    }

    @Bean
    public Binding bindingUser(Queue userQueue, DirectExchange exchange) {
        return BindingBuilder.bind(userQueue).to(exchange).with("user");
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }
}
```

**File**: `backend/messaging-service/src/main/java/com/example/messaging/config/WebSocketConfig.java`

Reference: `backend/ap/src/main/java/com/example/ap/WebSocketConfig/WebSocketConfig.java`

```java
package com.example.messaging.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/chat").setAllowedOriginPatterns("*").withSockJS();
    }
}
```

---

### Task 4.4 — Messaging Service: Producer + Consumer

**File**: `backend/messaging-service/src/main/java/com/example/messaging/service/ChatProducer.java`

Reference: `backend/ap/src/main/java/com/example/ap/Service/ChatProducer.java`

```java
package com.example.messaging.service;

import com.example.messaging.config.RabbitConfig;
import com.example.messaging.model.ChatMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class ChatProducer {

    private final RabbitTemplate rabbitTemplate;

    public ChatProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendToAdmin(ChatMessage message) {
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, "admin", message);
    }

    public void sendToUser(ChatMessage message) {
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, "user", message);
    }
}
```

**File**: `backend/messaging-service/src/main/java/com/example/messaging/service/ChatConsumer.java`

Reference: `backend/ap/src/main/java/com/example/ap/Service/ChatConsumer.java`
NOTE: Only ONE consumer per queue (fixes the duplicate listener bug from monolith)

```java
package com.example.messaging.service;

import com.example.messaging.config.RabbitConfig;
import com.example.messaging.model.ChatMessage;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class ChatConsumer {

    private final SimpMessagingTemplate messagingTemplate;

    public ChatConsumer(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @RabbitListener(queues = RabbitConfig.USER_QUEUE)
    public void receiveUser(ChatMessage message) {
        messagingTemplate.convertAndSend("/topic/user", message);
    }

    @RabbitListener(queues = RabbitConfig.ADMIN_QUEUE)
    public void receiveAdmin(ChatMessage message) {
        messagingTemplate.convertAndSend("/topic/admin", message);
    }
}
```

---

### Task 4.5 — Messaging Service: Controllers

**File**: `backend/messaging-service/src/main/java/com/example/messaging/controller/UserMessageController.java`

Reference: `backend/ap/src/main/java/com/example/ap/Controllers/UserControler.java` (chat endpoints)

```java
package com.example.messaging.controller;

import com.example.messaging.model.ChatMessage;
import com.example.messaging.repository.ChatMessageRepository;
import com.example.messaging.service.ChatProducer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/User")
public class UserMessageController {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatProducer chatProducer;

    public UserMessageController(ChatMessageRepository chatMessageRepository, ChatProducer chatProducer) {
        this.chatMessageRepository = chatMessageRepository;
        this.chatProducer = chatProducer;
    }

    @PostMapping("/message/admin")
    public ResponseEntity<ChatMessage> sendMessageToAdmin(@RequestParam Integer userId,
                                                           @RequestParam String message) {
        ChatMessage chatMessage = new ChatMessage(userId, 1, "USER", message, LocalDateTime.now());
        chatMessageRepository.save(chatMessage);
        chatProducer.sendToAdmin(chatMessage);
        return ResponseEntity.ok(chatMessage);
    }

    @GetMapping("/messages/sent/{userId}")
    public ResponseEntity<List<ChatMessage>> getSentMessages(@PathVariable Integer userId) {
        return ResponseEntity.ok(chatMessageRepository.findBySenderIdOrderByCreatedAtAsc(userId));
    }

    @GetMapping("/messages/inbox/{userId}")
    public ResponseEntity<List<ChatMessage>> getInboxMessages(@PathVariable Integer userId) {
        return ResponseEntity.ok(chatMessageRepository.findByReceiverIdOrderByCreatedAtAsc(userId));
    }
}
```

**File**: `backend/messaging-service/src/main/java/com/example/messaging/controller/AdminMessageController.java`

Reference: `backend/ap/src/main/java/com/example/ap/Controllers/AdminController.java` (message endpoints)
NOTE: Uses X-User-Id header instead of hardcoded 1

```java
package com.example.messaging.controller;

import com.example.messaging.model.ChatMessage;
import com.example.messaging.repository.ChatMessageRepository;
import com.example.messaging.service.ChatProducer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminMessageController {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatProducer chatProducer;

    public AdminMessageController(ChatMessageRepository chatMessageRepository, ChatProducer chatProducer) {
        this.chatMessageRepository = chatMessageRepository;
        this.chatProducer = chatProducer;
    }

    @PostMapping("/message/user")
    public ResponseEntity<String> sendMessageToUser(@RequestParam Integer adminId,
                                                     @RequestParam Integer userId,
                                                     @RequestParam String message,
                                                     @RequestHeader(value = "X-User-Role", required = false) String role) {
        if (!"ADMIN".equals(role)) return ResponseEntity.status(403).body("Forbidden");
        ChatMessage chatMessage = new ChatMessage(adminId, userId, "ADMIN", message, LocalDateTime.now());
        chatMessageRepository.save(chatMessage);
        chatProducer.sendToUser(chatMessage);
        return ResponseEntity.ok("Message sent to user");
    }

    @GetMapping("/messages")
    public ResponseEntity<List<ChatMessage>> getSentMessages(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader) {
        Integer adminId = userIdHeader != null ? Integer.parseInt(userIdHeader) : 1;
        return ResponseEntity.ok(chatMessageRepository.findBySenderIdOrderByCreatedAtAsc(adminId));
    }

    @GetMapping("/messages/inbox")
    public ResponseEntity<List<ChatMessage>> getInboxMessages(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader) {
        Integer adminId = userIdHeader != null ? Integer.parseInt(userIdHeader) : 1;
        return ResponseEntity.ok(chatMessageRepository.findByReceiverIdOrderByCreatedAtAsc(adminId));
    }
}
```

**Verify**: `cd backend/messaging-service && mvn clean package -DskipTests`

---

## Phase 5: Notification Service

### Task 5.1 — Create notification-service Scaffold
**Directory**: `backend/notification-service/`

**File**: `backend/notification-service/pom.xml`
- Parent: com.example:fullstack-backend:1.0.0
- artifactId: notification-service
- Dependencies: spring-boot-starter-web, spring-boot-starter-data-jpa, spring-boot-starter-amqp, spring-boot-starter-mail, spring-boot-starter-actuator, spring-cloud-starter-netflix-eureka-client, postgresql (runtime), shared-lib, lombok

**File**: `backend/notification-service/src/main/resources/application.yml`
```yaml
server:
  port: 8084

spring:
  application:
    name: notification-service
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${NOTIFICATION_DB_PORT:5436}/notification_db
    username: ${DB_USERNAME:lotfi}
    password: ${DB_PASSWORD:92570533Lt@}
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
  rabbitmq:
    host: ${RABBITMQ_HOST:localhost}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USERNAME:guest}
    password: ${RABBITMQ_PASSWORD:guest}
  mail:
    host: smtp.gmail.com
    port: 587
    username: ${MAIL_USERNAME:lotfitoumi56@gmail.com}
    password: ${MAIL_PASSWORD:ljhdmbvmxiagfeku}
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
          connectiontimeout: 5000
          timeout: 5000
          writetimeout: 5000
    from: ${MAIL_FROM:lotfitoumi56@gmail.com}

eureka:
  client:
    service-url:
      defaultZone: ${EUREKA_URL:http://localhost:8761/eureka/}
```

**File**: `backend/notification-service/src/main/java/com/example/notification/NotificationServiceApplication.java`
```java
package com.example.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class NotificationServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}
```

**File**: `backend/notification-service/Dockerfile`
```dockerfile
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

### Task 5.2 — Notification Service: Model + Repository

**File**: `backend/notification-service/src/main/java/com/example/notification/model/Notification.java`
- Copy from `backend/ap/src/main/java/com/example/ap/models/Notification.java`
- Change package to `com.example.notification.model`
- Keep ALL @Column names exactly

**File**: `backend/notification-service/src/main/java/com/example/notification/repository/NotificationRepository.java`
```java
package com.example.notification.repository;

import com.example.notification.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    List<Notification> findByUserIdOrderByCreatedAtDesc(Integer userId);
}
```

---

### Task 5.3 — Notification Service: Services

**File**: `backend/notification-service/src/main/java/com/example/notification/service/NotificationService.java`

Reference: `backend/ap/src/main/java/com/example/ap/Service/NotificationService.java`

```java
package com.example.notification.service;

import com.example.notification.model.Notification;
import com.example.notification.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public void createNotification(Integer userId, String title, String message) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setIsRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }

    public List<Notification> getUserNotifications(Integer userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public void markAsRead(Integer id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setIsRead(true);
        notificationRepository.save(notification);
    }
}
```

**File**: `backend/notification-service/src/main/java/com/example/notification/service/EmailService.java`

Copy from `backend/ap/src/main/java/com/example/ap/Service/EmailService.java`. Change package to `com.example.notification.service`. Keep sendApprovalEmail and sendEmail methods exactly.

---

### Task 5.4 — Notification Service: RabbitMQ Config + Event Listeners

**File**: `backend/notification-service/src/main/java/com/example/notification/config/RabbitConfig.java`
```java
package com.example.notification.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String EVENT_EXCHANGE = "event_exchange";
    public static final String NOTIFICATION_QUEUE = "notification_events_queue";
    public static final String EMAIL_QUEUE = "email_events_queue";

    @Bean
    public TopicExchange eventExchange() {
        return new TopicExchange(EVENT_EXCHANGE);
    }

    @Bean
    public Queue notificationQueue() {
        return new Queue(NOTIFICATION_QUEUE);
    }

    @Bean
    public Queue emailQueue() {
        return new Queue(EMAIL_QUEUE);
    }

    @Bean
    public Binding notificationBinding(Queue notificationQueue, TopicExchange eventExchange) {
        return BindingBuilder.bind(notificationQueue).to(eventExchange).with("event.notification.#");
    }

    @Bean
    public Binding emailBinding(Queue emailQueue, TopicExchange eventExchange) {
        return BindingBuilder.bind(emailQueue).to(eventExchange).with("event.email.#");
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }
}
```

**File**: `backend/notification-service/src/main/java/com/example/notification/listener/NotificationEventListener.java`
```java
package com.example.notification.listener;

import com.example.notification.config.RabbitConfig;
import com.example.notification.service.NotificationService;
import com.example.shared.event.NotificationEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationEventListener {

    private final NotificationService notificationService;

    public NotificationEventListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @RabbitListener(queues = RabbitConfig.NOTIFICATION_QUEUE)
    public void handleNotificationEvent(NotificationEvent event) {
        if (event.getUserId() != null) {
            notificationService.createNotification(event.getUserId(), event.getTitle(), event.getMessage());
        } else {
            // Notify admin (userId=1 as default admin)
            notificationService.createNotification(1, event.getTitle(), event.getMessage());
        }
    }
}
```

**File**: `backend/notification-service/src/main/java/com/example/notification/listener/EmailEventListener.java`
```java
package com.example.notification.listener;

import com.example.notification.config.RabbitConfig;
import com.example.notification.service.EmailService;
import com.example.shared.event.EmailEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class EmailEventListener {

    private final EmailService emailService;

    public EmailEventListener(EmailService emailService) {
        this.emailService = emailService;
    }

    @RabbitListener(queues = RabbitConfig.EMAIL_QUEUE)
    public void handleEmailEvent(EmailEvent event) {
        if (event.isHtml()) {
            emailService.sendApprovalEmail(event.getToEmail());
        } else {
            emailService.sendEmail(event.getToEmail(), event.getSubject(), event.getBody());
        }
    }
}
```

---

### Task 5.5 — Notification Service: Controllers

**File**: `backend/notification-service/src/main/java/com/example/notification/controller/NotificationController.java`

Reference: `backend/ap/src/main/java/com/example/ap/Controllers/NotificationController.java`

```java
package com.example.notification.controller;

import com.example.notification.model.Notification;
import com.example.notification.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/Not")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/notifications")
    public ResponseEntity<Void> createNotification(@RequestParam Integer userId,
                                                    @RequestParam String title,
                                                    @RequestParam String message) {
        notificationService.createNotification(userId, title, message);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/notify/{userId}")
    public ResponseEntity<String> notifyUser(@PathVariable Integer userId, @RequestParam String message) {
        notificationService.createNotification(userId, "Admin Notification", message);
        return ResponseEntity.ok("Notification sent to user " + userId);
    }

    @PostMapping("/notify/all")
    public ResponseEntity<String> notifyAll(@RequestParam String message) {
        // TODO: Call auth-service to get all user IDs, then create notification for each
        // For now, this is a placeholder — implement with RestTemplate to auth-service
        return ResponseEntity.ok("Notification sent to all users");
    }

    @GetMapping("/notifications/status/{userId}")
    public ResponseEntity<List<Notification>> getNotificationStatus(@PathVariable Integer userId) {
        return ResponseEntity.ok(notificationService.getUserNotifications(userId));
    }

    @PutMapping("/read/{id}")
    public ResponseEntity<String> markAsRead(@PathVariable Integer id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok("Notification marked as read");
    }
}
```

**File**: `backend/notification-service/src/main/java/com/example/notification/controller/UserNotificationController.java`
```java
package com.example.notification.controller;

import com.example.notification.model.Notification;
import com.example.notification.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/User")
public class UserNotificationController {

    private final NotificationService notificationService;

    public UserNotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/not/{userId}")
    public ResponseEntity<List<Notification>> getUserNotifications(@PathVariable Integer userId) {
        return ResponseEntity.ok(notificationService.getUserNotifications(userId));
    }

    @PutMapping("/read/{id}")
    public ResponseEntity<String> markAsRead(@PathVariable Integer id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok("Notification marked as read");
    }
}
```

**Verify**: `cd backend/notification-service && mvn clean package -DskipTests`

---

## Phase 6: API Gateway

### Task 6.1 — Create api-gateway
**Directory**: `backend/api-gateway/`

**File**: `backend/api-gateway/pom.xml`
- Parent: com.example:fullstack-backend:1.0.0
- artifactId: api-gateway
- Dependencies:
  - `org.springframework.cloud:spring-cloud-starter-gateway` (NOT spring-boot-starter-web!)
  - `org.springframework.cloud:spring-cloud-starter-netflix-eureka-client`
  - `org.springframework.boot:spring-boot-starter-actuator`
  - `com.example:shared-lib:1.0.0`

**File**: `backend/api-gateway/src/main/resources/application.yml`
```yaml
server:
  port: 8080

spring:
  application:
    name: api-gateway
  cloud:
    gateway:
      globalcors:
        cors-configurations:
          '[/**]':
            allowedOrigins:
              - "http://localhost:4200"
              - "http://localhost:8100"
              - "capacitor://localhost"
              - "http://localhost"
            allowedMethods:
              - GET
              - POST
              - PUT
              - DELETE
              - OPTIONS
            allowedHeaders: "*"
            allowCredentials: true
      routes:
        # Auth Service routes
        - id: auth-login
          uri: lb://auth-service
          predicates:
            - Path=/api/v1/auth/**

        - id: user-logout
          uri: lb://auth-service
          predicates:
            - Path=/api/v1/User/logout

        - id: admin-users
          uri: lb://auth-service
          predicates:
            - Path=/api/v1/admin/users,/api/v1/admin/users/**,/api/v1/admin/test-email

        # Task Service routes
        - id: task-service
          uri: lb://task-service
          predicates:
            - Path=/api/v1/Task/**

        # Messaging Service routes
        - id: user-messages
          uri: lb://messaging-service
          predicates:
            - Path=/api/v1/User/message/**,/api/v1/User/messages/**

        - id: admin-messages
          uri: lb://messaging-service
          predicates:
            - Path=/api/v1/admin/message/**,/api/v1/admin/messages,/api/v1/admin/messages/**

        - id: websocket
          uri: lb:ws://messaging-service
          predicates:
            - Path=/chat/**

        # Notification Service routes
        - id: notification-api
          uri: lb://notification-service
          predicates:
            - Path=/api/v1/Not/**

        - id: user-notifications
          uri: lb://notification-service
          predicates:
            - Path=/api/v1/User/not/**,/api/v1/User/read/**

jwt:
  secret: ${JWT_SECRET:my-super-secret-key-that-is-at-least-32-characters-long}

eureka:
  client:
    service-url:
      defaultZone: ${EUREKA_URL:http://localhost:8761/eureka/}
```

**File**: `backend/api-gateway/src/main/java/com/example/gateway/ApiGatewayApplication.java`
```java
package com.example.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ApiGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
```

**File**: `backend/api-gateway/Dockerfile`
```dockerfile
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

### Task 6.2 — API Gateway: JWT Filter

**File**: `backend/api-gateway/src/main/java/com/example/gateway/config/JwtConfig.java`
```java
package com.example.gateway.config;

import com.example.shared.security.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfig {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Bean
    public JwtUtil jwtUtil() {
        return new JwtUtil(jwtSecret);
    }
}
```

**File**: `backend/api-gateway/src/main/java/com/example/gateway/filter/JwtAuthFilter.java`

IMPORTANT: This is a WebFlux filter (Spring Cloud Gateway is reactive). Uses `GatewayFilter` patterns.

```java
package com.example.gateway.filter;

import com.example.shared.security.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import java.util.List;

@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;

    // Paths that don't require authentication
    private static final List<String> OPEN_PATHS = List.of(
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/forgot-password",
            "/chat"
    );

    public JwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // Skip JWT for open paths
        for (String openPath : OPEN_PATHS) {
            if (path.startsWith(openPath)) {
                return chain.filter(exchange);
            }
        }

        // Extract Authorization header
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);

        if (!jwtUtil.isTokenValid(token)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // Extract claims and add as headers for downstream services
        Claims claims = jwtUtil.extractClaims(token);
        Integer userId = claims.get("userId", Integer.class);
        String role = claims.get("role", String.class);
        String email = claims.getSubject();

        ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                .header("X-User-Id", String.valueOf(userId))
                .header("X-User-Role", role)
                .header("X-User-Email", email)
                .build();

        return chain.filter(exchange.mutate().request(modifiedRequest).build());
    }

    @Override
    public int getOrder() {
        return -1; // Run before other filters
    }
}
```

**Verify**: `cd backend/api-gateway && mvn clean package -DskipTests`

---

## Phase 7: Docker Compose

### Task 7.1 — Create docker-compose.yml and .env

**File**: `backend/.env`
```env
DB_USERNAME=lotfi
DB_PASSWORD=92570533Lt@
RABBITMQ_USERNAME=guest
RABBITMQ_PASSWORD=guest
JWT_SECRET=my-super-secret-jwt-key-that-is-at-least-256-bits-long-for-hmac-sha
MAIL_USERNAME=lotfitoumi56@gmail.com
MAIL_PASSWORD=ljhdmbvmxiagfeku
MAIL_FROM=lotfitoumi56@gmail.com
```

**File**: `backend/docker-compose.yml`
```yaml
version: '3.8'

services:
  # === Databases ===
  auth-db:
    image: postgres:17
    container_name: auth-db
    environment:
      POSTGRES_USER: ${DB_USERNAME}
      POSTGRES_PASSWORD: ${DB_PASSWORD}
      POSTGRES_DB: auth_db
    ports:
      - "5433:5432"
    volumes:
      - auth_db_data:/var/lib/postgresql/data
    networks:
      - backend-net
    restart: unless-stopped

  task-db:
    image: postgres:17
    container_name: task-db
    environment:
      POSTGRES_USER: ${DB_USERNAME}
      POSTGRES_PASSWORD: ${DB_PASSWORD}
      POSTGRES_DB: task_db
    ports:
      - "5434:5432"
    volumes:
      - task_db_data:/var/lib/postgresql/data
    networks:
      - backend-net
    restart: unless-stopped

  messaging-db:
    image: postgres:17
    container_name: messaging-db
    environment:
      POSTGRES_USER: ${DB_USERNAME}
      POSTGRES_PASSWORD: ${DB_PASSWORD}
      POSTGRES_DB: messaging_db
    ports:
      - "5435:5432"
    volumes:
      - messaging_db_data:/var/lib/postgresql/data
    networks:
      - backend-net
    restart: unless-stopped

  notification-db:
    image: postgres:17
    container_name: notification-db
    environment:
      POSTGRES_USER: ${DB_USERNAME}
      POSTGRES_PASSWORD: ${DB_PASSWORD}
      POSTGRES_DB: notification_db
    ports:
      - "5436:5432"
    volumes:
      - notification_db_data:/var/lib/postgresql/data
    networks:
      - backend-net
    restart: unless-stopped

  # === Message Broker ===
  rabbitmq:
    image: rabbitmq:3-management
    container_name: rabbitmq
    environment:
      RABBITMQ_DEFAULT_USER: ${RABBITMQ_USERNAME}
      RABBITMQ_DEFAULT_PASS: ${RABBITMQ_PASSWORD}
    ports:
      - "5672:5672"
      - "15672:15672"
    volumes:
      - rabbitmq_data:/var/lib/rabbitmq
    networks:
      - backend-net
    restart: unless-stopped
    healthcheck:
      test: rabbitmq-diagnostics -q ping
      interval: 10s
      timeout: 5s
      retries: 5

  # === Service Registry ===
  service-registry:
    build: ./service-registry
    container_name: service-registry
    ports:
      - "8761:8761"
    networks:
      - backend-net
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "wget", "--spider", "-q", "http://localhost:8761/actuator/health"]
      interval: 10s
      timeout: 5s
      retries: 5

  # === Microservices ===
  auth-service:
    build: ./auth-service
    container_name: auth-service
    ports:
      - "8081:8081"
    environment:
      DB_HOST: auth-db
      AUTH_DB_PORT: 5432
      DB_USERNAME: ${DB_USERNAME}
      DB_PASSWORD: ${DB_PASSWORD}
      RABBITMQ_HOST: rabbitmq
      RABBITMQ_USERNAME: ${RABBITMQ_USERNAME}
      RABBITMQ_PASSWORD: ${RABBITMQ_PASSWORD}
      JWT_SECRET: ${JWT_SECRET}
      EUREKA_URL: http://service-registry:8761/eureka/
    depends_on:
      auth-db:
        condition: service_started
      rabbitmq:
        condition: service_healthy
      service-registry:
        condition: service_healthy
    networks:
      - backend-net
    restart: unless-stopped

  task-service:
    build: ./task-service
    container_name: task-service
    ports:
      - "8082:8082"
    environment:
      DB_HOST: task-db
      TASK_DB_PORT: 5432
      DB_USERNAME: ${DB_USERNAME}
      DB_PASSWORD: ${DB_PASSWORD}
      RABBITMQ_HOST: rabbitmq
      RABBITMQ_USERNAME: ${RABBITMQ_USERNAME}
      RABBITMQ_PASSWORD: ${RABBITMQ_PASSWORD}
      EUREKA_URL: http://service-registry:8761/eureka/
    depends_on:
      task-db:
        condition: service_started
      rabbitmq:
        condition: service_healthy
      service-registry:
        condition: service_healthy
    networks:
      - backend-net
    restart: unless-stopped

  messaging-service:
    build: ./messaging-service
    container_name: messaging-service
    ports:
      - "8083:8083"
    environment:
      DB_HOST: messaging-db
      MESSAGING_DB_PORT: 5432
      DB_USERNAME: ${DB_USERNAME}
      DB_PASSWORD: ${DB_PASSWORD}
      RABBITMQ_HOST: rabbitmq
      RABBITMQ_USERNAME: ${RABBITMQ_USERNAME}
      RABBITMQ_PASSWORD: ${RABBITMQ_PASSWORD}
      EUREKA_URL: http://service-registry:8761/eureka/
    depends_on:
      messaging-db:
        condition: service_started
      rabbitmq:
        condition: service_healthy
      service-registry:
        condition: service_healthy
    networks:
      - backend-net
    restart: unless-stopped

  notification-service:
    build: ./notification-service
    container_name: notification-service
    ports:
      - "8084:8084"
    environment:
      DB_HOST: notification-db
      NOTIFICATION_DB_PORT: 5432
      DB_USERNAME: ${DB_USERNAME}
      DB_PASSWORD: ${DB_PASSWORD}
      RABBITMQ_HOST: rabbitmq
      RABBITMQ_USERNAME: ${RABBITMQ_USERNAME}
      RABBITMQ_PASSWORD: ${RABBITMQ_PASSWORD}
      MAIL_USERNAME: ${MAIL_USERNAME}
      MAIL_PASSWORD: ${MAIL_PASSWORD}
      MAIL_FROM: ${MAIL_FROM}
      EUREKA_URL: http://service-registry:8761/eureka/
    depends_on:
      notification-db:
        condition: service_started
      rabbitmq:
        condition: service_healthy
      service-registry:
        condition: service_healthy
    networks:
      - backend-net
    restart: unless-stopped

  # === API Gateway ===
  api-gateway:
    build: ./api-gateway
    container_name: api-gateway
    ports:
      - "8080:8080"
    environment:
      JWT_SECRET: ${JWT_SECRET}
      EUREKA_URL: http://service-registry:8761/eureka/
    depends_on:
      service-registry:
        condition: service_healthy
    networks:
      - backend-net
    restart: unless-stopped

volumes:
  auth_db_data:
  task_db_data:
  messaging_db_data:
  notification_db_data:
  rabbitmq_data:

networks:
  backend-net:
    driver: bridge
```

**Add** `backend/.env` to root `.gitignore`.

---

## Phase 8: Frontend Updates

### Task 8.1 — Update frontend-web

**File**: `frontend-web/app/src/environments/environment.ts`
- Set `apiUrl: 'http://localhost:8080/api/v1'`
- Set `wsUrl: 'http://localhost:8080'`

**File**: `frontend-web/app/src/app/controllers/chat.ts`
- Replace ALL hardcoded `http://localhost:8080/api/v1/...` URLs with references using the environment apiUrl
- Import the environment file

**File**: `frontend-web/app/src/app/services/` (auth service if exists)
- Update login to POST JSON body `{ email, password }` instead of form params
- Handle response as `{ token, user }` instead of string
- Store token in localStorage under `admin_token`

---

### Task 8.2 — Update frontend-mobile

**File**: `frontend-mobile/app/src/environments/environment.ts`
- Add `apiUrl: 'http://localhost:8080/api/v1'`
- Set `wsUrl: 'http://localhost:8080/chat'`

**File**: `frontend-mobile/app/src/environments/environment.prod.ts`
- Same structure with production URLs

**All API service files** (replace hardcoded `http://172.20.10.2:8080/api/v1`):
- Import environment
- Use `environment.apiUrl` as base URL

**New File**: `frontend-mobile/app/src/app/interceptors/auth.interceptor.ts`
```typescript
import { HttpInterceptorFn } from '@angular/common/http';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  if (typeof window !== 'undefined') {
    const token = localStorage.getItem('auth_token');
    if (token) {
      req = req.clone({
        setHeaders: { Authorization: `Bearer ${token}` }
      });
    }
  }
  return next(req);
};
```

Register this interceptor in `app.config.ts` via `provideHttpClient(withInterceptors([authInterceptor]))`.

**Update login flow**:
- POST `/api/v1/auth/login` with JSON body `{ email, password }`
- Response: `{ token, user: { id, email, firstName, lastName, role, userActive } }`
- Store token: `localStorage.setItem('auth_token', response.token)`
- Set currentUser from `response.user`

---

## Phase 9: Data Migration

### Task 9.1 — Migrate Data

1. Start all Docker services: `cd backend && docker-compose up -d`
2. Wait for all databases to be ready
3. Export data from old `project` database (port 5333):
   ```bash
   pg_dump -h localhost -p 5333 -U lotfi -d project -t users --data-only > users_data.sql
   pg_dump -h localhost -p 5333 -U lotfi -d project -t tasks --data-only > tasks_data.sql
   pg_dump -h localhost -p 5333 -U lotfi -d project -t chat_messages --data-only > messages_data.sql
   pg_dump -h localhost -p 5333 -U lotfi -d project -t notifications --data-only > notifications_data.sql
   ```
4. Start services to create tables (Hibernate ddl-auto=update)
5. Import into new databases:
   ```bash
   psql -h localhost -p 5433 -U lotfi -d auth_db < users_data.sql
   psql -h localhost -p 5434 -U lotfi -d task_db < tasks_data.sql
   psql -h localhost -p 5435 -U lotfi -d messaging_db < messages_data.sql
   psql -h localhost -p 5436 -U lotfi -d notification_db < notifications_data.sql
   ```
6. Add role column and set admin:
   ```sql
   -- On auth_db (port 5433)
   UPDATE users SET role = 'USER' WHERE role IS NULL;
   UPDATE users SET role = 'ADMIN' WHERE id = 1;
   ```
7. Run auth-service with `password.migration.enabled=true` to BCrypt all passwords
8. Set `password.migration.enabled=false` after migration

---

## Phase 10: Testing & Verification

### Task 10.1 — Build All Services
```bash
cd backend
mvn clean package -DskipTests
```

### Task 10.2 — Start and Verify
1. `docker-compose up -d` from `backend/`
2. Check Eureka: `http://localhost:8761` — all 5 services should be registered
3. Test auth:
   - `POST http://localhost:8080/api/v1/auth/register` with JSON body
   - `POST http://localhost:8080/api/v1/auth/login` — should return JWT
4. Test tasks (use Bearer token):
   - `POST http://localhost:8080/api/v1/Task/tasks`
   - `GET http://localhost:8080/api/v1/Task/tasks`
5. Test messaging:
   - `POST http://localhost:8080/api/v1/User/message/admin`
   - Connect WebSocket to `ws://localhost:8080/chat`
6. Test notifications:
   - `GET http://localhost:8080/api/v1/Not/notifications/status/1`
7. Test both frontends against gateway

---

## Summary of All Files to Create

```
backend/
├── pom.xml                                    (Task 1.1)
├── .env                                       (Task 7.1)
├── docker-compose.yml                         (Task 7.1)
├── shared-lib/
│   ├── pom.xml                                (Task 1.2)
│   └── src/main/java/com/example/shared/
│       ├── dto/UserDTO.java                   (Task 1.2)
│       ├── dto/AuthResponse.java              (Task 1.2)
│       ├── dto/LoginRequest.java              (Task 1.2)
│       ├── event/NotificationEvent.java       (Task 1.2)
│       ├── event/EmailEvent.java              (Task 1.2)
│       ├── event/UserApprovedEvent.java       (Task 1.2)
│       └── security/JwtUtil.java              (Task 1.2)
├── service-registry/
│   ├── pom.xml                                (Task 1.3)
│   ├── Dockerfile                             (Task 1.3)
│   └── src/.../ServiceRegistryApplication.java (Task 1.3)
├── auth-service/
│   ├── pom.xml                                (Task 2.1)
│   ├── Dockerfile                             (Task 2.1)
│   ├── src/.../AuthServiceApplication.java    (Task 2.1)
│   ├── src/.../model/User.java                (Task 2.2)
│   ├── src/.../repository/UserRepository.java (Task 2.3)
│   ├── src/.../config/SecurityConfig.java     (Task 2.4)
│   ├── src/.../config/JwtConfig.java          (Task 2.4)
│   ├── src/.../config/RabbitConfig.java       (Task 2.4)
│   ├── src/.../service/AuthService.java       (Task 2.5)
│   ├── src/.../service/UserService.java       (Task 2.6)
│   ├── src/.../controller/AuthController.java (Task 2.7)
│   ├── src/.../controller/UserLogoutController.java (Task 2.7)
│   ├── src/.../controller/AdminUserController.java  (Task 2.7)
│   └── src/.../config/PasswordMigrationRunner.java  (Task 2.8)
├── task-service/
│   ├── pom.xml                                (Task 3.1)
│   ├── Dockerfile                             (Task 3.1)
│   ├── src/.../TaskServiceApplication.java    (Task 3.1)
│   ├── src/.../model/Task.java                (Task 3.2)
│   ├── src/.../repository/TaskRepository.java (Task 3.2)
│   ├── src/.../config/RabbitConfig.java       (Task 3.3)
│   ├── src/.../service/TaskService.java       (Task 3.3)
│   └── src/.../controller/TaskController.java (Task 3.4)
├── messaging-service/
│   ├── pom.xml                                (Task 4.1)
│   ├── Dockerfile                             (Task 4.1)
│   ├── src/.../MessagingServiceApplication.java (Task 4.1)
│   ├── src/.../model/ChatMessage.java         (Task 4.2)
│   ├── src/.../repository/ChatMessageRepository.java (Task 4.2)
│   ├── src/.../config/RabbitConfig.java       (Task 4.3)
│   ├── src/.../config/WebSocketConfig.java    (Task 4.3)
│   ├── src/.../service/ChatProducer.java      (Task 4.4)
│   ├── src/.../service/ChatConsumer.java       (Task 4.4)
│   ├── src/.../controller/UserMessageController.java  (Task 4.5)
│   └── src/.../controller/AdminMessageController.java (Task 4.5)
├── notification-service/
│   ├── pom.xml                                (Task 5.1)
│   ├── Dockerfile                             (Task 5.1)
│   ├── src/.../NotificationServiceApplication.java (Task 5.1)
│   ├── src/.../model/Notification.java        (Task 5.2)
│   ├── src/.../repository/NotificationRepository.java (Task 5.2)
│   ├── src/.../service/NotificationService.java (Task 5.3)
│   ├── src/.../service/EmailService.java      (Task 5.3)
│   ├── src/.../config/RabbitConfig.java       (Task 5.4)
│   ├── src/.../listener/NotificationEventListener.java (Task 5.4)
│   ├── src/.../listener/EmailEventListener.java (Task 5.4)
│   ├── src/.../controller/NotificationController.java (Task 5.5)
│   └── src/.../controller/UserNotificationController.java (Task 5.5)
├── api-gateway/
│   ├── pom.xml                                (Task 6.1)
│   ├── Dockerfile                             (Task 6.1)
│   ├── src/.../ApiGatewayApplication.java     (Task 6.1)
│   ├── src/.../config/JwtConfig.java          (Task 6.2)
│   └── src/.../filter/JwtAuthFilter.java      (Task 6.2)
```

**Total new files**: ~55 Java files + 7 pom.xml + 6 application.yml + 6 Dockerfiles + 1 docker-compose.yml + 1 .env = ~76 files
