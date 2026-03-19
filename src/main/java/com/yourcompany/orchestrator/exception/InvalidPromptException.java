package com.yourcompany.orchestrator.exception;

public class InvalidPromptException extends RuntimeException {
    public InvalidPromptException(String message) {
        super(message);
    }
}