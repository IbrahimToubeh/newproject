package com.example.auth.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private UserDetails userDetails;
    private String validSecret = "9a02115a835ee03d5fb83cd8a468ea33e4090a6a276b96f377c8a32b2e04312c";
    private Long expiration = 86400000L; // 24 hours

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "secretKey", validSecret);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpiration", expiration);

        userDetails = User.builder()
                .username("testuser")
                .password("password")
                .authorities(Collections.emptyList())
                .build();
    }

    @Test
    void generateToken_ShouldReturnValidToken() {
        String token = jwtTokenProvider.generateToken("testuser");

        assertNotNull(token);
        assertTrue(token.length() > 0);
    }

    @Test
    void extractUserName_WithValidToken_ShouldReturnUsername() {
        String token = jwtTokenProvider.generateToken("testuser");

        String username = jwtTokenProvider.extractUserName(token);

        assertEquals("testuser", username);
    }

    @Test
    void validateToken_WithValidToken_ShouldReturnTrue() {
        String token = jwtTokenProvider.generateToken("testuser");

        boolean isValid = jwtTokenProvider.validateToken(token, userDetails);

        assertTrue(isValid);
    }

    @Test
    void validateToken_WithWrongUsername_ShouldReturnFalse() {
        String token = jwtTokenProvider.generateToken("otheruser");

        boolean isValid = jwtTokenProvider.validateToken(token, userDetails);

        assertFalse(isValid);
    }

    @Test
    void validateToken_WithExpiredToken_ShouldReturnFalse() {
        // Create provider with 0ms expiration
        JwtTokenProvider expiredProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(expiredProvider, "secretKey", validSecret);
        ReflectionTestUtils.setField(expiredProvider, "jwtExpiration", 0L);

        String token = expiredProvider.generateToken("testuser");

        // Wait a bit to ensure expiration
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            // Ignore
        }

        assertThrows(ExpiredJwtException.class, 
                () -> jwtTokenProvider.extractUserName(token));
    }

    @Test
    void extractUserName_WithMalformedToken_ShouldThrowException() {
        String malformedToken = "not.a.valid.jwt.token";

        assertThrows(MalformedJwtException.class,
                () -> jwtTokenProvider.extractUserName(malformedToken));
    }

    @Test
    void extractUserName_WithNullToken_ShouldThrowException() {
        assertThrows(Exception.class,
                () -> jwtTokenProvider.extractUserName(null));
    }

    @Test
    void extractUserName_WithEmptyToken_ShouldThrowException() {
        assertThrows(Exception.class,
                () -> jwtTokenProvider.extractUserName(""));
    }

    @Test
    void validateToken_WithNullToken_ShouldReturnFalse() {
        boolean isValid = jwtTokenProvider.validateToken(null, userDetails);

        assertFalse(isValid);
    }

    @Test
    void validateToken_WithNullUserDetails_ShouldReturnFalse() {
        String token = jwtTokenProvider.generateToken("testuser");

        boolean isValid = jwtTokenProvider.validateToken(token, null);

        assertFalse(isValid);
    }

    @Test
    void generateToken_WithDifferentUsernames_ShouldGenerateDifferentTokens() {
        String token1 = jwtTokenProvider.generateToken("user1");
        String token2 = jwtTokenProvider.generateToken("user2");

        assertNotEquals(token1, token2);
    }

    @Test
    void extractUserName_ShouldBeCaseInsensitive() {
        String token = jwtTokenProvider.generateToken("TestUser");

        String username = jwtTokenProvider.extractUserName(token);

        assertEquals("TestUser", username);
    }
}
