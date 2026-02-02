package com.example.auth.security;

import com.example.auth.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Mock
    private FilterChain filterChain;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        // Ensure the mocked user is enabled, otherwise validation fails immediately
        userDetails = new CustomUserDetails(123L, "testuser", "test@example.com", "password", Collections.emptyList(), true);
    }

    @Test
    void doFilterInternal_WithValidToken_ShouldAuthenticate() throws ServletException, IOException {
        String token = "valid.jwt.token";
        request.addHeader("Authorization", "Bearer " + token);

        when(jwtTokenProvider.extractUserId(token)).thenReturn("123");
        when(userDetailsService.loadUserById(123L)).thenReturn(userDetails);
        when(jwtTokenProvider.validateToken(token, userDetails)).thenReturn(true);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtTokenProvider).extractUserId(token);
        verify(userDetailsService).loadUserById(123L);
    }

    @Test
    void doFilterInternal_WithNoAuthHeader_ShouldContinueWithoutAuthentication() throws ServletException, IOException {
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtTokenProvider, never()).extractUserId(anyString());
    }

    @Test
    void doFilterInternal_WithInvalidTokenFormat_ShouldContinueWithoutAuthentication() throws ServletException, IOException {
        request.addHeader("Authorization", "InvalidFormat token");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtTokenProvider, never()).extractUserId(anyString());
    }

    @Test
    void doFilterInternal_WithEmptyToken_ShouldContinueWithoutAuthentication() throws ServletException, IOException {
        request.addHeader("Authorization", "Bearer ");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtTokenProvider, never()).extractUserId(anyString());
    }

    @Test
    void doFilterInternal_WithInvalidToken_ShouldContinueWithoutAuthentication() throws ServletException, IOException {
        String token = "invalid.jwt.token";
        request.addHeader("Authorization", "Bearer " + token);

        when(jwtTokenProvider.extractUserId(token)).thenReturn("123");
        when(userDetailsService.loadUserById(123L)).thenReturn(userDetails);
        when(jwtTokenProvider.validateToken(eq(token), any())).thenReturn(false);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        // validateToken might not be called if extraction throws exception, but here we mocked extraction to succeed
        verify(jwtTokenProvider).validateToken(eq(token), any());
    }

    @Test
    void doFilterInternal_WithExpiredToken_ShouldContinueWithoutAuthentication() throws ServletException, IOException {
        String token = "expired.jwt.token";
        request.addHeader("Authorization", "Bearer " + token);

        when(jwtTokenProvider.extractUserId(token)).thenThrow(new RuntimeException("Token expired"));

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithNullUserId_ShouldContinueWithoutAuthentication() throws ServletException, IOException {
        String token = "valid.jwt.token";
        request.addHeader("Authorization", "Bearer " + token);

        when(jwtTokenProvider.extractUserId(token)).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(userDetailsService, never()).loadUserById(anyLong());
    }


    @Test
    void doFilterInternal_WithExistingAuthentication_ShouldSkipAuthentication() throws ServletException, IOException {
        String token = "valid.jwt.token";
        request.addHeader("Authorization", "Bearer " + token);

        // Simulate existing authentication
        when(jwtTokenProvider.extractUserId(token)).thenReturn("123");
        SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken("user", "pass", Collections.emptyList())
        );

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(userDetailsService, never()).loadUserById(anyLong());
    }

    @Test
    void doFilterInternal_WithDisabledUser_ShouldNotAuthenticate() throws ServletException, IOException {
        String token = "valid.jwt.token";
        request.addHeader("Authorization", "Bearer " + token);

        // Disabled user
        UserDetails disabledUser = new CustomUserDetails(123L, "testuser", "test@example.com", "password", Collections.emptyList(), false);

        when(jwtTokenProvider.extractUserId(token)).thenReturn("123");
        when(userDetailsService.loadUserById(123L)).thenReturn(disabledUser);
        when(jwtTokenProvider.validateToken(token, disabledUser)).thenReturn(true);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
