package com.yourcompany.orchestrator.exception;

public class ProviderRateLimitException extends RuntimeException {
    public ProviderRateLimitException(String provider) {
        super("Provider '" + provider + "' is currently rate limited. Please try again shortly.");
    }
}