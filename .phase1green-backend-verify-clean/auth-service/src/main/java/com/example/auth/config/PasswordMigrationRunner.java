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
            String password = user.getSecretPassword();
            if (password != null && !password.startsWith("$2a$") && !password.startsWith("$2b$")) {
                user.setSecretPassword(passwordEncoder.encode(password));
                userRepository.save(user);
                System.out.println("Migrated password for user: " + user.getEmail());
            }
        }
        System.out.println("Password migration complete.");
    }
}
