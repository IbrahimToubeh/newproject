package com.example.auth.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExceptionConstructorTest {

    @Test
    void badRequestException_WithMessage_ShouldCreateException() {
        String message = "Bad request error";
        BadRequestException exception = new BadRequestException(message);
        
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
    }

    @Test
    void resourceNotFoundException_WithMessage_ShouldCreateException() {
        String message = "Resource not found";
        ResourceNotFoundException exception = new ResourceNotFoundException(message);
        
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
    }

    @Test
    void unauthorizedException_WithMessage_ShouldCreateException() {
        String message = "Unauthorized access";
        UnauthorizedException exception = new UnauthorizedException(message);
        
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
    }

    @Test
    void badRequestException_ShouldBeRuntimeException() {
        BadRequestException exception = new BadRequestException("test");
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void resourceNotFoundException_ShouldBeRuntimeException() {
        ResourceNotFoundException exception = new ResourceNotFoundException("test");
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void unauthorizedException_ShouldBeRuntimeException() {
        UnauthorizedException exception = new UnauthorizedException("test");
        assertTrue(exception instanceof RuntimeException);
    }
}
