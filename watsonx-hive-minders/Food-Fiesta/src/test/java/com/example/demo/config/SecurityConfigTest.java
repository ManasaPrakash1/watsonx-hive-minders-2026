package com.example.demo.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies that SecurityConfig correctly registers its beans and that the
 * PasswordEncoder works as a BCrypt encoder.
 */
@SpringBootTest
class SecurityConfigTest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SecurityFilterChain securityFilterChain;

    // ── PasswordEncoder bean ─────────────────────────────────────────────

    @Test
    void passwordEncoderBean_isNotNull() {
        assertNotNull(passwordEncoder);
    }

    @Test
    void passwordEncoderBean_isBCrypt() {
        assertTrue(passwordEncoder instanceof BCryptPasswordEncoder,
                "PasswordEncoder should be a BCryptPasswordEncoder");
    }

    @Test
    void passwordEncoderBean_encodesPasswordCorrectly() {
        String raw = "admin123";
        String encoded = passwordEncoder.encode(raw);

        assertNotNull(encoded);
        assertNotEquals(raw, encoded, "Encoded password must differ from raw");
        assertTrue(encoded.startsWith("$2"), "BCrypt hash should start with $2");
    }

    @Test
    void passwordEncoderBean_matchesEncodedPassword() {
        String raw = "mySecretPass";
        String encoded = passwordEncoder.encode(raw);

        assertTrue(passwordEncoder.matches(raw, encoded));
    }

    @Test
    void passwordEncoderBean_doesNotMatchWrongPassword() {
        String raw = "mySecretPass";
        String encoded = passwordEncoder.encode(raw);

        assertFalse(passwordEncoder.matches("wrongPass", encoded));
    }

    @Test
    void passwordEncoderBean_differentEncodingsAreUnique() {
        String raw = "same";
        String enc1 = passwordEncoder.encode(raw);
        String enc2 = passwordEncoder.encode(raw);

        // BCrypt uses a random salt — each encoding must be unique
        assertNotEquals(enc1, enc2);
        assertTrue(passwordEncoder.matches(raw, enc1));
        assertTrue(passwordEncoder.matches(raw, enc2));
    }

    // ── SecurityFilterChain bean ─────────────────────────────────────────

    @Test
    void securityFilterChainBean_isNotNull() {
        assertNotNull(securityFilterChain);
    }
}
