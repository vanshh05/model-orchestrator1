package com.yourcompany.orchestrator.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ProviderRateLimitException.class)
    public ResponseEntity<ErrorResponse> handleRateLimit(ProviderRateLimitException ex) {
        return ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS)
                .body(new ErrorResponse(429, "Too Many Requests", ex.getMessage()));
    }

    @ExceptionHandler(ProviderUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleUnavailable(ProviderUnavailableException ex) {
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ErrorResponse(503, "Service Unavailable", ex.getMessage()));
    }

    @ExceptionHandler(ProviderException.class)
    public ResponseEntity<ErrorResponse> handleProviderError(ProviderException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_GATEWAY)
                .body(new ErrorResponse(502, "Bad Gateway", ex.getMessage()));
    }

    @ExceptionHandler(NoRuleFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoRule(NoRuleFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(404, "Not Found", ex.getMessage()));
    }

    @ExceptionHandler(InvalidPromptException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPrompt(InvalidPromptException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(400, "Bad Request", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(500, "Internal Server Error", ex.getMessage()));
    }
}