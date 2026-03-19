package com.yourcompany.orchestrator.exception;

public class ProviderUnavailableException extends RuntimeException {
    public ProviderUnavailableException(String provider) {
        super("Provider '" + provider + "' is currently unavailable. Please try again later.");
    }
}