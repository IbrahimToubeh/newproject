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
    private Long expiration = 86400000L;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "secretKey", validSecret);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpiration", expiration);

        userDetails = new CustomUserDetails(
                123L,
                "testuser",
                "test@example.com",
                "password",
                Collections.emptyList(),
                true
        );
    }

    @Test
    void generateToken_ShouldReturnValidToken() {
        String token = jwtTokenProvider.generateTokenFromUserId(123L);

        assertNotNull(token);
        assertTrue(token.length() > 0);
    }

    @Test
    void extractUserId_WithValidToken_ShouldReturnUserId() {
        String token = jwtTokenProvider.generateTokenFromUserId(123L);

        String userId = jwtTokenProvider.extractUserId(token);

        assertEquals("123", userId);
    }

    @Test
    void validateToken_WithValidToken_ShouldReturnTrue() {
        String token = jwtTokenProvider.generateTokenFromUserId(123L);

        boolean isValid = jwtTokenProvider.validateToken(token, userDetails);

        assertTrue(isValid);
    }

    @Test
    void validateToken_WithExpiredToken_ShouldReturnFalse() {
        JwtTokenProvider expiredProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(expiredProvider, "secretKey", validSecret);
        ReflectionTestUtils.setField(expiredProvider, "jwtExpiration", 0L);


        String token = expiredProvider.generateTokenFromUserId(123L);

        // Sleep briefly to ensure the token actually expires
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            // Ignore
        }

        boolean isValid = jwtTokenProvider.validateToken(token, userDetails);
        assertFalse(isValid);
    }

    @Test
    void extractUserId_WithMalformedToken_ShouldThrowException() {
        String malformedToken = "not.a.valid.jwt.token";

        assertThrows(MalformedJwtException.class,
                () -> jwtTokenProvider.extractUserId(malformedToken));
    }

    @Test
    void extractUserId_WithNullToken_ShouldThrowException() {
        assertThrows(Exception.class,
                () -> jwtTokenProvider.extractUserId(null));
    }

    @Test
    void extractUserId_WithEmptyToken_ShouldThrowException() {
        assertThrows(Exception.class,
                () -> jwtTokenProvider.extractUserId(""));
    }

    @Test
    void validateToken_WithNullToken_ShouldReturnFalse() {
        boolean isValid = jwtTokenProvider.validateToken(null, userDetails);

        assertFalse(isValid);
    }

    @Test
    void generateToken_WithDifferentUserIds_ShouldGenerateDifferentTokens() {
        String token1 = jwtTokenProvider.generateTokenFromUserId(1L);
        String token2 = jwtTokenProvider.generateTokenFromUserId(2L);

        assertNotEquals(token1, token2);
    }


    @Test
    void validateToken_WithGenericUserDetails_ShouldOnlyCheckExpiration() {
        UserDetails genericUser = User.builder()
                .username("generic")
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        String token = jwtTokenProvider.generateTokenFromUserId(123L);

        // Should return true because it only checks expiration for non-CustomUserDetails
        assertTrue(jwtTokenProvider.validateToken(token, genericUser));
    }
}
