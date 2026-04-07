package com.example.auth.config;

import com.example.auth.model.User;
import com.example.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordMigrationRunnerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private PasswordMigrationRunner runner;

    @BeforeEach
    void setUp() {
        runner = new PasswordMigrationRunner(userRepository, passwordEncoder);
    }

    @Test
    void migratesOnlyNonBcryptPasswords() {
        User plain = new User();
        plain.setEmail("plain@example.com");
        plain.setSecretPassword("plain-secret");

        User encoded = new User();
        encoded.setEmail("encoded@example.com");
        encoded.setSecretPassword("$2a$already-encoded");

        when(userRepository.findAll()).thenReturn(List.of(plain, encoded));
        when(passwordEncoder.encode("plain-secret")).thenReturn("encoded-secret");

        runner.run();

        verify(passwordEncoder).encode("plain-secret");
        verify(userRepository).save(plain);
        verify(userRepository, never()).save(encoded);
    }
}
