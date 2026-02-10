package com.example.auth.exception;

import org.springframework.http.HttpStatusCode;

public class ExternalServiceException extends RuntimeException {
    
    private final HttpStatusCode status;

    public ExternalServiceException(String message, HttpStatusCode status) {
        super(message);
        this.status = status;
    }

    public HttpStatusCode getStatus() {
        return status;
    }
}
